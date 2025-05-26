package com.unacar.calendarioacademico.ui.eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Evento
import java.text.SimpleDateFormat
import java.util.*

class EventoProfesorAdapter(
    private val eventos: List<Evento>,
    private val nombresMateria: Map<String, String>,
    private val onEventoClick: (Evento) -> Unit,
    private val onEditarEvento: (Evento) -> Unit,
    private val onEliminarEvento: (Evento) -> Unit
) : RecyclerView.Adapter<EventoProfesorAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardEvento: CardView = itemView.findViewById(R.id.cardEvento)
        val tvTituloEvento: TextView = itemView.findViewById(R.id.tvTituloEvento)
        val tvDescripcionEvento: TextView = itemView.findViewById(R.id.tvDescripcionEvento)
        val tvFechaEvento: TextView = itemView.findViewById(R.id.tvFechaEvento)
        val tvHoraEvento: TextView = itemView.findViewById(R.id.tvHoraEvento)
        val tvMateriaEvento: TextView = itemView.findViewById(R.id.tvMateriaEvento)
        val btnOpciones: TextView = itemView.findViewById(R.id.btnOpciones)
        val indicadorTipo: View = itemView.findViewById(R.id.indicadorTipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_profesor, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        val context = holder.itemView.context

        // Configurar datos básicos
        holder.tvTituloEvento.text = evento.titulo
        holder.tvDescripcionEvento.text = evento.descripcion
        holder.tvHoraEvento.text = evento.hora

        // Formatear fecha
        val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
        val fecha = Date(evento.fecha)
        holder.tvFechaEvento.text = dateFormat.format(fecha)

        // Mostrar nombre de la materia
        val nombreMateria = nombresMateria[evento.idMateria] ?: "Materia desconocida"
        holder.tvMateriaEvento.text = "Materia: $nombreMateria"

        // Configurar color del indicador según el tipo
        val colorIndicador = when (evento.tipo) {
            "examen" -> context.getColor(R.color.purple_700)
            "tarea" -> context.getColor(R.color.teal_700)
            "proyecto" -> context.getColor(android.R.color.holo_orange_dark)
            else -> context.getColor(R.color.purple_500)
        }
        holder.indicadorTipo.setBackgroundColor(colorIndicador)

        // Click en la tarjeta
        holder.cardEvento.setOnClickListener {
            onEventoClick(evento)
        }

        // Menú de opciones
        holder.btnOpciones.setOnClickListener { view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_evento_profesor, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_editar_evento -> {
                        onEditarEvento(evento)
                        true
                    }
                    R.id.action_eliminar_evento -> {
                        onEliminarEvento(evento)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount() = eventos.size
}