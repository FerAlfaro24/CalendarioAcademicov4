package com.unacar.calendarioacademico.modelos

data class Notificacion(
    val id: String = "",
    val mensaje: String = "",
    val idUsuario: String = "",
    val idEvento: String = "",
    val estado: String = "no_leida",
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "", "no_leida", 0)
}