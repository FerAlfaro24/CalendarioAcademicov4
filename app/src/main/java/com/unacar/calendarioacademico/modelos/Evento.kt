package com.unacar.calendarioacademico.modelos

data class Evento(
    var id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0,  // timestamp
    val hora: String = "",
    val idMateria: String = "",
    val tipo: String = "tarea",  // "examen", "tarea", "proyecto"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", 0, "", "", "tarea", 0, 0)

    // Método para obtener el tipo formateado
    fun getTipoFormateado(): String {
        return when (tipo.lowercase()) {
            "examen" -> "Examen"
            "tarea" -> "Tarea"
            "proyecto" -> "Proyecto"
            else -> "Evento"
        }
    }
}