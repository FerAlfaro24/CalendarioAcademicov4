package com.unacar.calendarioacademico.ui.eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Evento
import java.text.SimpleDateFormat
import java.util.*

class EventoAdapter(
    private val eventos: List<Evento>,
    private val onEventoClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardEvento: CardView = itemView.findViewById(R.id.cardEvento)
        val tvTipoEvento: TextView = itemView.findViewById(R.id.tvTipoEvento)
        val tvTituloEvento: TextView = itemView.findViewById(R.id.tvTituloEvento)
        val tvDescripcionEvento: TextView = itemView.findViewById(R.id.tvDescripcionEvento)
        val tvFechaEvento: TextView = itemView.findViewById(R.id.tvFechaEvento)
        val tvHoraEvento: TextView = itemView.findViewById(R.id.tvHoraEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        holder.tvTipoEvento.text = evento.getTipoFormateado()
        holder.tvTituloEvento.text = evento.titulo
        holder.tvDescripcionEvento.text = if (evento.descripcion.isNotEmpty()) evento.descripcion else "Sin descripción"
        holder.tvHoraEvento.text = evento.hora

        // Formatear fecha
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvFechaEvento.text = formatoFecha.format(Date(evento.fecha))

        // Color según tipo de evento
        val colorTipo = when (evento.tipo) {
            "examen" -> ContextCompat.getColor(holder.itemView.context, R.color.color_examen)
            "exposicion" -> ContextCompat.getColor(holder.itemView.context, R.color.color_exposicion)
            "proyecto" -> ContextCompat.getColor(holder.itemView.context, R.color.color_proyecto)
            "tarea" -> ContextCompat.getColor(holder.itemView.context, R.color.color_tarea)
            "dia_libre" -> ContextCompat.getColor(holder.itemView.context, R.color.color_dia_libre)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
        }

        holder.tvTipoEvento.setTextColor(colorTipo)

        // Configurar click
        holder.cardEvento.setOnClickListener {
            onEventoClick(evento)
        }
    }

    override fun getItemCount() = eventos.size
}