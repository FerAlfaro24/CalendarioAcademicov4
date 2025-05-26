package com.unacar.calendarioacademico.ui.notificaciones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.modelos.Notificacion
import java.text.SimpleDateFormat
import java.util.*

class NotificacionAdapter(
    private val notificaciones: List<Notificacion>,
    private val onNotificacionClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder>() {

    class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardNotificacion: CardView = itemView.findViewById(R.id.cardNotificacion)
        val ivIconoTipo: ImageView = itemView.findViewById(R.id.ivIconoTipo)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloNotificacion)
        val tvMensaje: TextView = itemView.findViewById(R.id.tvMensajeNotificacion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaNotificacion)
        val indicadorNoLeida: View = itemView.findViewById(R.id.indicadorNoLeida)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        val notificacion = notificaciones[position]

        // Configurar icono según tipo
        holder.ivIconoTipo.setImageResource(notificacion.getIconoTipo())

        // Configurar textos
        holder.tvTitulo.text = notificacion.titulo.ifEmpty { "Notificación" }
        holder.tvMensaje.text = notificacion.mensaje

        // Formatear fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvFecha.text = dateFormat.format(Date(notificacion.fechaCreacion))

        // Configurar estilo según si está leída
        if (!notificacion.leida) {
            holder.indicadorNoLeida.visibility = View.VISIBLE
            holder.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.purple_200)
            )
        } else {
            holder.indicadorNoLeida.visibility = View.GONE
            holder.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )
        }

        // Click listener
        holder.cardNotificacion.setOnClickListener {
            onNotificacionClick(notificacion)
        }
    }

    override fun getItemCount() = notificaciones.size
}