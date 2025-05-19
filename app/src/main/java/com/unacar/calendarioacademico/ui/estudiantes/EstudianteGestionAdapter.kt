package com.unacar.calendarioacademico.ui.estudiantes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Usuario

class EstudianteGestionAdapter(
    private val estudiantes: List<Usuario>,
    private val contadoresMaterias: Map<String, Int>,
    private val onVerDetallesClick: (Usuario) -> Unit
) : RecyclerView.Adapter<EstudianteGestionAdapter.EstudianteViewHolder>() {

    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreEstudiante: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvCorreoEstudiante: TextView = itemView.findViewById(R.id.tvCorreoEstudiante)
        val tvContadorMaterias: TextView = itemView.findViewById(R.id.tvContadorMaterias)
        val btnVerDetalles: Button = itemView.findViewById(R.id.btnVerDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_gestion, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = estudiantes[position]

        holder.tvNombreEstudiante.text = estudiante.nombre
        holder.tvCorreoEstudiante.text = estudiante.correo

        // Mostrar contador de materias
        val cantidadMaterias = contadoresMaterias[estudiante.id] ?: 0
        holder.tvContadorMaterias.text = if (cantidadMaterias == 1) {
            "Inscrito en 1 materia"
        } else {
            "Inscrito en $cantidadMaterias materias"
        }

        holder.btnVerDetalles.setOnClickListener {
            onVerDetallesClick(estudiante)
        }
    }

    override fun getItemCount() = estudiantes.size
}