package com.example.tasklist.Model

data class ListWithProgress(
    val taskList: TaskList,
    val averagePriority: String,
    val progress: Int, // porcentaje
    val completedTasks: Int,
    val totalTasks: Int
)
