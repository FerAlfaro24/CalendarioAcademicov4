package com.unacar.calendarioacademico.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Materia

class MateriaSimpleAdapter(
    private val materias: List<Materia>,
    private val onMateriaClick: (Materia) -> Unit
) : RecyclerView.Adapter<MateriaSimpleAdapter.MateriaViewHolder>() {

    class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMateria: CardView = itemView.findViewById(R.id.cardMateria)
        val tvNombreMateria: TextView = itemView.findViewById(R.id.tvNombreMateria)
        val tvSemestre: TextView = itemView.findViewById(R.id.tvSemestre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia_simple, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]

        holder.tvNombreMateria.text = materia.nombre
        holder.tvSemestre.text = "Semestre: ${materia.semestre}"

        holder.cardMateria.setOnClickListener {
            onMateriaClick(materia)
        }
    }

    override fun getItemCount() = materias.size
}