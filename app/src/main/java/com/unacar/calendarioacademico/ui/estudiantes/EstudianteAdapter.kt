package com.unacar.calendarioacademico.ui.estudiantes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Usuario

class EstudianteAdapter(
    private val estudiantes: List<Usuario>,
    private val esProfesor: Boolean,
    private val onEliminarEstudiante: (Usuario) -> Unit
) : RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder>() {

    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreEstudiante: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvCorreoEstudiante: TextView = itemView.findViewById(R.id.tvCorreoEstudiante)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = estudiantes[position]

        holder.tvNombreEstudiante.text = estudiante.nombre
        holder.tvCorreoEstudiante.text = estudiante.correo

        // Solo los profesores pueden eliminar estudiantes
        holder.btnEliminar.visibility = if (esProfesor) View.VISIBLE else View.GONE

        holder.btnEliminar.setOnClickListener {
            onEliminarEstudiante(estudiante)
        }
    }

    override fun getItemCount() = estudiantes.size
}