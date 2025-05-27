package com.unacar.calendarioacademico.ui.calendario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Evento

class EventoCalendarioAdapter(
    private val eventos: List<Evento>,
    private val onEventoClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoCalendarioAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardEvento: CardView = itemView.findViewById(R.id.cardEvento)
        val tvTipoEvento: TextView = itemView.findViewById(R.id.tvTipoEvento)
        val tvTituloEvento: TextView = itemView.findViewById(R.id.tvTituloEvento)
        val tvHoraEvento: TextView = itemView.findViewById(R.id.tvHoraEvento)
        val tvDescripcionEvento: TextView = itemView.findViewById(R.id.tvDescripcionEvento)
        val indicadorTipo: View = itemView.findViewById(R.id.indicadorTipo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_calendario, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        holder.tvTipoEvento.text = evento.getTipoFormateado().uppercase()
        holder.tvTituloEvento.text = evento.titulo
        holder.tvHoraEvento.text = evento.hora
        holder.tvDescripcionEvento.text = if (evento.descripcion.isNotEmpty()) {
            evento.descripcion
        } else {
            "Sin descripción"
        }

        // Color según tipo de evento
        val colorTipo = when (evento.tipo) {
            "examen" -> ContextCompat.getColor(holder.itemView.context, R.color.color_examen)
            "tarea" -> ContextCompat.getColor(holder.itemView.context, R.color.color_tarea)
            "proyecto" -> ContextCompat.getColor(holder.itemView.context, R.color.color_proyecto)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
        }

        // Aplicar color al indicador y al tipo
        holder.indicadorTipo.setBackgroundColor(colorTipo)
        holder.tvTipoEvento.setBackgroundColor(colorTipo)

        // Configurar click
        holder.cardEvento.setOnClickListener {
            onEventoClick(evento)
        }
    }

    override fun getItemCount() = eventos.size
}