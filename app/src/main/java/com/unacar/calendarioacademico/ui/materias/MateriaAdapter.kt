package com.unacar.calendarioacademico.ui.materias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Materia

class MateriaAdapter(
    private val materias: List<Materia>,
    private val esEstudiante: Boolean,
    private val onMateriaClick: (Materia) -> Unit
) : RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder>() {

    class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardMateria: CardView = itemView.findViewById(R.id.cardMateria)
        val tvNombreMateria: TextView = itemView.findViewById(R.id.tvNombreMateria)
        val tvDescripcionMateria: TextView = itemView.findViewById(R.id.tvDescripcionMateria)
        val tvSemestre: TextView = itemView.findViewById(R.id.tvSemestre)
        val tvProfesor: TextView = itemView.findViewById(R.id.tvProfesor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_materia, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materias[position]

        holder.tvNombreMateria.text = materia.nombre
        holder.tvDescripcionMateria.text = materia.descripcion
        holder.tvSemestre.text = "Semestre: ${materia.semestre}"

        // Si es estudiante, mostrar el nombre del profesor (habría que implementar esto)
        holder.tvProfesor.visibility = if (esEstudiante) View.VISIBLE else View.GONE

        // Configurar click en la tarjeta
        holder.cardMateria.setOnClickListener {
            onMateriaClick(materia)
        }
    }

    override fun getItemCount() = materias.size
}