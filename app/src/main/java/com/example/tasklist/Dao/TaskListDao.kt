package com.example.tasklist.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.tasklist.Model.TaskList

@Dao
interface TaskListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTaskList(taskList: TaskList): Long

    @Update
    fun updateTaskList(taskList: TaskList): Int

    @Delete
    fun deleteTaskList(taskList: TaskList): Int

    @Query("SELECT * FROM task_lists")
    fun getAllTaskLists(): LiveData<List<TaskList>>
}
