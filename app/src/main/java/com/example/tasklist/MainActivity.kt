package com.example.tasklist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasklist.Database.TaskDatabase
import com.example.tasklist.Model.TaskList
import com.example.tasklist.Repository.TaskRepository
import com.example.tasklist.ViewModel.TaskViewModel
import com.example.tasklist.ViewModel.TaskViewModelFactory
import com.example.tasklist.adapter.TaskAdapter
import com.example.tasklist.adapter.TaskListAdapter
import com.example.tasklist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskAdapter: TaskAdapter

    private var currentSortByPriority = false

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            TaskRepository(
                TaskDatabase.getDatabase(applicationContext).taskDao(),
                TaskDatabase.getDatabase(applicationContext).taskListDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchBar()
        observeLists()

        binding.btnAddList.setOnClickListener {
            showAddListDialog()
        }

        binding.btnToggleSort.setOnClickListener {
            currentSortByPriority = !currentSortByPriority
            observeLists() // vuelve a cargar aplicando el orden
        }
    }

    private fun setupRecyclerView() {
        taskListAdapter = TaskListAdapter(
            items = emptyList(),
            onClick = { listWithProgress ->
                val intent = Intent(this, TaskListActivity::class.java)
                intent.putExtra("listId", listWithProgress.taskList.listId)
                startActivity(intent)
            },
            onLongClick = { listWithProgress, _ ->
                showListOptionsDialog(listWithProgress.taskList)
            }
        )

        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onClick = { task ->
                AlertDialog.Builder(this)
                    .setTitle(task.name)
                    .setMessage(task.description)
                    .setPositiveButton("Cerrar", null)
                    .show()
            },
            onLongClick = { task, _ ->
                val options = arrayOf("Editar", "Eliminar")
                AlertDialog.Builder(this)
                    .setTitle("Opciones de tarea")
                    .setItems(options) { _, which ->
                        if (which == 0) {
                            showEditTaskDialog(task)
                        } else {
                            viewModel.deleteTask(task)
                        }
                    }.show()
            },
            onCheckedChange = { task ->
                viewModel.updateTask(task)
            }
        )

        binding.rvTaskLists.layoutManager = LinearLayoutManager(this)
        binding.rvTaskLists.adapter = taskListAdapter
    }

    private fun setupSearchBar() {
        val edtSearch = findViewById<EditText>(R.id.edtSearch)
        val cbFilterPriority = findViewById<CheckBox>(R.id.chkFilterPriority)
        val spinner = findViewById<Spinner>(R.id.spinnerPriority)
        val btnClear = findViewById<ImageButton>(R.id.btnClearSearch)

        val priorities = listOf("", "Alta", "Media", "Baja")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        spinner.visibility = View.GONE

        cbFilterPriority.setOnCheckedChangeListener { _, isChecked ->
            spinner.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnClear.setOnClickListener {
            edtSearch.setText("")
            cbFilterPriority.isChecked = false
            spinner.setSelection(0)
            observeLists()
        }

        edtSearch.setOnEditorActionListener { _, _, _ ->
            val query = edtSearch.text.toString()
            val priority = if (cbFilterPriority.isChecked) spinner.selectedItem.toString() else ""
            searchTasks(query, priority)
            true
        }
    }

    private fun searchTasks(query: String, priority: String) {
        viewModel.searchGlobalTasks(query, priority).observe(this) { tasks ->
            val sortedTasks = if (priority.isNotEmpty()) {
                val (match, rest) = tasks.partition { it.priority.equals(priority, true) }
                match + rest
            } else tasks

            taskAdapter.setTasks(sortedTasks)
            binding.rvTaskLists.adapter = taskAdapter
        }
    }

    private fun observeLists() {
        viewModel.getListsWithProgress().observe(this) { lists ->
            val sorted = if (currentSortByPriority) {
                lists.sortedByDescending {
                    when (it.averagePriority) {
                        "Alta" -> 3
                        "Media" -> 2
                        else -> 1
                    }
                }
            } else lists

            taskListAdapter.setItems(sorted)

            val totalTasks = sorted.sumOf { it.totalTasks }
            val completedTasks = sorted.sumOf { it.completedTasks }
            val globalProgress = if (totalTasks == 0) 0 else (completedTasks * 100) / totalTasks

            binding.progressBarOverall.progress = globalProgress
            binding.tvProgressInfo.text = "Terminado: $completedTasks    Pendientes: ${totalTasks - completedTasks}"
            binding.rvTaskLists.adapter = taskListAdapter
        }
    }

    private fun showAddListDialog() {
        val input = EditText(this)
        input.hint = "Nombre de la lista"

        AlertDialog.Builder(this)
            .setTitle("Nueva Lista")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.insertList(TaskList(name = name))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showListOptionsDialog(taskList: TaskList) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("Opciones de Lista")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditListDialog(taskList)
                    1 -> {
                        viewModel.deleteList(taskList)
                        Toast.makeText(this, "Lista eliminada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showEditListDialog(taskList: TaskList) {
        val input = EditText(this)
        input.setText(taskList.name)

        AlertDialog.Builder(this)
            .setTitle("Editar Lista")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    taskList.name = newName
                    viewModel.updateList(taskList)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditTaskDialog(task: com.example.tasklist.Model.Task) {
        val inputName = EditText(this)
        inputName.setText(task.name)

        val inputDesc = EditText(this)
        inputDesc.setText(task.description)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(inputName)
            addView(inputDesc)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Tarea")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                task.name = inputName.text.toString()
                task.description = inputDesc.text.toString()
                viewModel.updateTask(task)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
