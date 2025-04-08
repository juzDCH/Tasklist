package com.example.tasklist.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.tasklist.Model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE listId = :listId")
    fun getTasksForList(listId: Int): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Task): Long

    @Update
    fun update(task: Task): Int

    @Delete
    fun delete(task: Task): Int

    // ✅ Consulta global segura, sin errores de parámetros
    @Query("""
        SELECT * FROM tasks 
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') 
        AND (:priority = '' OR priority = :priority)
        AND (:tags = '' OR tags LIKE '%' || :tags || '%')
    """)
    fun searchGlobalTasks(
        query: String,
        priority: String,
        tags: String
    ): LiveData<List<Task>>
}
