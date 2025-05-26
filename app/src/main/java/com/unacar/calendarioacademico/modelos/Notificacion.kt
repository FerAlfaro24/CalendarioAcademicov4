package com.unacar.calendarioacademico.modelos

data class Notificacion(
    val id: String = "",
    val mensaje: String = "",
    val titulo: String = "", // Agregado para compatibilidad
    val tipo: String = "evento", // Agregado para compatibilidad
    val idUsuario: String = "",
    val idEvento: String = "",
    val estado: String = "no_leida", // "leida" o "no_leida"
    val leida: Boolean = false, // Para compatibilidad con código existente
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "evento", "", "", "no_leida", false, 0)

    // Método para obtener icono según tipo
    fun getIconoTipo(): Int {
        return when (tipo.lowercase()) {
            "examen" -> android.R.drawable.ic_dialog_alert
            "tarea" -> android.R.drawable.ic_menu_edit
            "proyecto" -> android.R.drawable.ic_menu_agenda
            else -> android.R.drawable.ic_dialog_info
        }
    }
}