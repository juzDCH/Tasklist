package com.example.tasklist.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.tasklist.Model.ListWithProgress
import com.example.tasklist.R
import com.example.tasklist.util.CircularProgressView

class TaskListAdapter(
    private var items: List<ListWithProgress>,
    private val onClick: (ListWithProgress) -> Unit,
    private val onLongClick: (ListWithProgress, View) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.ListViewHolder>() {

    inner class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvListName: TextView = view.findViewById(R.id.tvListName)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val circularProgress: CircularProgressView = view.findViewById(R.id.circularProgress)
        val tvProgressPercent: TextView = view.findViewById(R.id.tvProgressPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val item = items[position]
        holder.tvListName.text = item.taskList.name
        holder.tvPriority.text = "Prioridad: ${item.averagePriority}"
        holder.tvPriority.setTextColor(getPriorityColor(item.averagePriority))

        holder.circularProgress.setProgress(item.progress)
        holder.tvProgressPercent.text = "${item.progress}%"

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item, it)
            true
        }

        // ✅ Se conserva el fondo y sombra, pero sin cambiar colores visuales generales
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 32f
            setStroke(4, 0x1A000000) // borde gris sutil (semi-transparente)
            setColor(0xFFFFFFFF.toInt()) // blanco de fondo
        }

        holder.itemView.background = background
        holder.itemView.elevation = 6f
        holder.itemView.setPadding(24)
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<ListWithProgress>) {
        items = newItems
        notifyDataSetChanged()
    }

    // ✅ Usamos celeste-azul oscuro como equivalente a rojo, siguiendo el logo
    private fun getPriorityColor(priority: String): Int {
        return when (priority) {
            "Alta" -> 0xFF007ACC.toInt()   // Celeste oscuro
            "Media" -> 0xFF00BFA6.toInt()  // Verde-celeste
            "Baja" -> 0xFFA5D6A7.toInt()   // Verde pastel claro
            else -> 0xFFBDBDBD.toInt()     // Gris neutro
        }
    }
}
