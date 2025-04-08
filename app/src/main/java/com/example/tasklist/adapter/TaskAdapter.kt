package com.example.tasklist.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasklist.Model.Task
import com.example.tasklist.R
import android.graphics.Color

class TaskAdapter(
    private var tasks: List<Task>,
    private val onClick: (Task) -> Unit,
    private val onLongClick: (Task, View) -> Unit,
    private val onCheckedChange: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvTaskName.text = task.name
        holder.tvPriority.text = "Prioridad: ${task.priority}"
        holder.cbCompleted.isChecked = task.isCompleted

        holder.itemView.alpha = if (task.isCompleted) 0.5f else 1.0f

        // Color por prioridad en el rango verde-celeste-azul
        val color = when (task.priority) {
            "Alta" -> Color.parseColor("#007ACC") // azul oscuro
            "Media" -> Color.parseColor("#00BCD4") // celeste
            "Baja" -> Color.parseColor("#00BFA6") // verde claro
            else -> Color.GRAY
        }

        holder.tvPriority.setTextColor(color)

        // Crear fondo dinámico con borde coloreado
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(Color.WHITE)
            setStroke(6, color)
        }

        holder.itemView.background = drawable

        holder.itemView.setOnClickListener { onClick(task) }
        holder.itemView.setOnLongClickListener {
            onLongClick(task, it)
            true
        }
        holder.cbCompleted.setOnCheckedChangeListener { _, _ ->
            task.isCompleted = holder.cbCompleted.isChecked
            onCheckedChange(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun setTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
