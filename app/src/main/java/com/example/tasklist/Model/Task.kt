package com.example.tasklist.Model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskList::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])] // ✅ Índice recomendado por Room
)
data class Task(
    @PrimaryKey(autoGenerate = true) val taskId: Int = 0,
    var listId: Int,
    var name: String,
    var description: String,
    var isCompleted: Boolean = false,
    var priority: String,
    var tags: String
)
