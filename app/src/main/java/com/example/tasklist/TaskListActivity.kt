package com.example.tasklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasklist.Database.TaskDatabase
import com.example.tasklist.Model.Task
import com.example.tasklist.Repository.TaskRepository
import com.example.tasklist.ViewModel.TaskViewModel
import com.example.tasklist.ViewModel.TaskViewModelFactory
import com.example.tasklist.adapter.TaskAdapter
import com.example.tasklist.databinding.ActivityTaskListBinding

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private var listId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listId = intent.getIntExtra("listId", -1)
        if (listId == -1) {
            Toast.makeText(this, "Lista no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val repo = TaskRepository(
            TaskDatabase.getDatabase(applicationContext).taskDao(),
            TaskDatabase.getDatabase(applicationContext).taskListDao()
        )
        viewModel = TaskViewModelFactory(repo).create(TaskViewModel::class.java)

        setupRecyclerView()
        observeTasks()

        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            tasks = emptyList(),
            onClick = { showTaskDetails(it) },
            onLongClick = { task, _ -> showTaskOptionsDialog(task) },
            onCheckedChange = { viewModel.updateTask(it) }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun observeTasks() {
        viewModel.getTasksForList(listId).observe(this) { tasks ->
            adapter.setTasks(tasks)
            val completed = tasks.count { it.isCompleted }
            val pending = tasks.size - completed
            binding.tvProgressInfo.text = "Terminado: $completed    Pendientes: $pending"
            binding.progressBarOverall.progress = if (tasks.isEmpty()) 0 else (completed * 100 / tasks.size)
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtTaskName)
        val edtDesc = dialogView.findViewById<EditText>(R.id.edtTaskDescription)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerTaskPriority)

        val priorities = arrayOf("Alta", "Media", "Baja")
        spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)

        AlertDialog.Builder(this)
            .setTitle("Nueva Tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = edtName.text.toString()
                val desc = edtDesc.text.toString()
                val priority = spinnerPriority.selectedItem.toString()
                if (name.isNotEmpty()) {
                    val task = Task(
                        listId = listId,
                        name = name,
                        description = desc,
                        priority = priority,
                        tags = "",
                        isCompleted = false
                    )
                    viewModel.insertTask(task)
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showTaskDetails(task: Task) {
        AlertDialog.Builder(this)
            .setTitle(task.name)
            .setMessage(task.description)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showTaskOptionsDialog(task: Task) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("Opciones")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditTaskDialog(task)
                    1 -> viewModel.deleteTask(task)
                }
            }
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtTaskName)
        val edtDesc = dialogView.findViewById<EditText>(R.id.edtTaskDescription)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerTaskPriority)

        val priorities = arrayOf("Alta", "Media", "Baja")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        spinnerPriority.adapter = adapterSpinner
        spinnerPriority.setSelection(priorities.indexOf(task.priority))

        edtName.setText(task.name)
        edtDesc.setText(task.description)

        AlertDialog.Builder(this)
            .setTitle("Editar Tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                task.name = edtName.text.toString()
                task.description = edtDesc.text.toString()
                task.priority = spinnerPriority.selectedItem.toString()
                viewModel.updateTask(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
