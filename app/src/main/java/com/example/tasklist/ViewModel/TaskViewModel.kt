package com.example.tasklist.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasklist.Model.ListWithProgress
import com.example.tasklist.Model.Task
import com.example.tasklist.Model.TaskList
import com.example.tasklist.Repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // Tareas por lista
    fun getTasksForList(listId: Int): LiveData<List<Task>> {
        return repository.getTasksForList(listId)
    }

    // Todas las listas con progreso calculado
    fun getListsWithProgress(): LiveData<List<ListWithProgress>> {
        return repository.getListsWithProgress()
    }

    // Insertar nueva lista
    fun insertList(taskList: TaskList) = viewModelScope.launch {
        repository.insertList(taskList)
    }

    // Insertar nueva tarea
    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    // Actualizar tarea
    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    // Eliminar tarea
    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    // Eliminar lista
    fun deleteList(taskList: TaskList) = viewModelScope.launch {
        repository.deleteList(taskList)
    }

    // Actualizar lista
    fun updateList(taskList: TaskList) = viewModelScope.launch {
        repository.updateList(taskList)
    }

    fun searchGlobalTasks(query: String, priority: String): LiveData<List<Task>> {
        return repository.searchGlobalTasks(query, priority, "")
    }

}
