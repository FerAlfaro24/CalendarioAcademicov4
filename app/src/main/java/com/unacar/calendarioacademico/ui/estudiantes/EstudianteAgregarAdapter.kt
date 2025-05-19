package com.unacar.calendarioacademico.ui.estudiantes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Usuario

class EstudianteAgregarAdapter(
    private val estudiantes: List<Usuario>,
    private val onAgregarEstudiante: (Usuario) -> Unit
) : RecyclerView.Adapter<EstudianteAgregarAdapter.EstudianteViewHolder>() {

    class EstudianteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreEstudiante: TextView = itemView.findViewById(R.id.tvNombreEstudiante)
        val tvCorreoEstudiante: TextView = itemView.findViewById(R.id.tvCorreoEstudiante)
        val btnAgregar: Button = itemView.findViewById(R.id.btnAgregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstudianteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estudiante_agregar, parent, false)
        return EstudianteViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstudianteViewHolder, position: Int) {
        val estudiante = estudiantes[position]

        holder.tvNombreEstudiante.text = estudiante.nombre
        holder.tvCorreoEstudiante.text = estudiante.correo

        holder.btnAgregar.setOnClickListener {
            onAgregarEstudiante(estudiante)
        }
    }

    override fun getItemCount() = estudiantes.size
}