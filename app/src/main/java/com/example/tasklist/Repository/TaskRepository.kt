package com.example.tasklist.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.tasklist.Dao.TaskDao
import com.example.tasklist.Dao.TaskListDao
import com.example.tasklist.Model.ListWithProgress
import com.example.tasklist.Model.Task
import com.example.tasklist.Model.TaskList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
) {
    fun getTasksForList(listId: Int): LiveData<List<Task>> {
        return taskDao.getTasksForList(listId)
    }

    fun getListsWithProgress(): LiveData<List<ListWithProgress>> {
        val result = MediatorLiveData<List<ListWithProgress>>()

        val taskListsLiveData = taskListDao.getAllTaskLists()
        val allTasksLiveData = taskDao.getAllTasks()

        result.addSource(taskListsLiveData) { taskLists ->
            val currentTasks = allTasksLiveData.value ?: emptyList()
            result.value = calculateListWithProgress(taskLists, currentTasks)
        }

        result.addSource(allTasksLiveData) { allTasks ->
            val currentLists = taskListsLiveData.value ?: emptyList()
            result.value = calculateListWithProgress(currentLists, allTasks)
        }

        return result
    }

    private fun calculateListWithProgress(
        lists: List<TaskList>,
        allTasks: List<Task>
    ): List<ListWithProgress> {
        return lists.map { list ->
            val tasks = allTasks.filter { it.listId == list.listId }
            val total = tasks.size
            val completed = tasks.count { it.isCompleted }

            val avgPriority = if (tasks.isEmpty()) {
                "Baja"
            } else {
                val avg = tasks.map { it.priority }.averagePriorityLevel()
                getPriorityFromAverage(avg)
            }

            val progress = if (total == 0) 0 else (completed * 100) / total

            ListWithProgress(
                taskList = list,
                averagePriority = avgPriority,
                progress = progress,
                completedTasks = completed,
                totalTasks = total
            )
        }
    }

    fun insertList(taskList: TaskList) {
        CoroutineScope(Dispatchers.IO).launch {
            taskListDao.insertTaskList(taskList)
        }
    }

    fun insertTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.insert(task)
        }
    }

    fun updateTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.update(task)
        }
    }

    fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.delete(task)
        }
    }

    fun deleteList(taskList: TaskList) {
        CoroutineScope(Dispatchers.IO).launch {
            taskListDao.deleteTaskList(taskList)
        }
    }

    fun updateList(taskList: TaskList) {
        CoroutineScope(Dispatchers.IO).launch {
            taskListDao.updateTaskList(taskList)
        }
    }

    fun searchGlobalTasks(query: String?, priority: String?, tags: String?): LiveData<List<Task>> {
        return taskDao.searchGlobalTasks(
            query = query ?: "",
            priority = priority ?: "",
            tags = tags ?: ""
        )
    }
}

// ✅ Extensión para calcular promedio de prioridad textual en español
private fun List<String>.averagePriorityLevel(): Double {
    return map {
        when (it) {
            "Alta" -> 3.0
            "Media" -> 2.0
            "Baja" -> 1.0
            else -> 1.0
        }
    }.average()
}

private fun getPriorityFromAverage(avg: Double): String {
    return when {
        avg >= 2.5 -> "Alta"
        avg >= 1.5 -> "Media"
        else -> "Baja"
    }
}
