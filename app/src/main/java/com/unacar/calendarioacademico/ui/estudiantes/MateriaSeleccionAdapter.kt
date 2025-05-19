package com.unacar.calendarioacademico.ui.estudiantes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Materia

class MateriaSeleccionAdapter(
    private val materias: List<Materia>,
    private val nombresProfesor: Map<String, String>,
    private val onInscribirClick: (Materia) -> Unit
) : RecyclerView.Adapter<MateriaSeleccionAdapter.MateriaViewHolder>() {

    class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreMateria: TextView = itemView.findViewById(R.id.tvNombreMateria)
        val tvProfesor: TextView = itemView.findViewById(R.id.tvProfesor)
        val tvSemestre: TextView = itemView.findViewById(R.id.tvSemestre)
        val btnInscribir: Button = itemView.findViewById(R.id.btnInscribir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia_seleccion, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]

        holder.tvNombreMateria.text = materia.nombre
        holder.tvSemestre.text = "Semestre: ${materia.semestre}"

        // Mostrar nombre del profesor
        val nombreProfesor = nombresProfesor[materia.idProfesor] ?: "Profesor desconocido"
        holder.tvProfesor.text = "Profesor: $nombreProfesor"

        holder.btnInscribir.setOnClickListener {
            onInscribirClick(materia)
        }
    }

    override fun getItemCount() = materias.size
}