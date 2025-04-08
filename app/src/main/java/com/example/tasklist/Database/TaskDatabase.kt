package com.example.tasklist.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tasklist.Dao.TaskDao
import com.example.tasklist.Dao.TaskListDao
import com.example.tasklist.Model.Task
import com.example.tasklist.Model.TaskList

@Database(entities = [Task::class, TaskList::class], version = 2, exportSchema = false )
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).fallbackToDestructiveMigration() // opcional, para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
