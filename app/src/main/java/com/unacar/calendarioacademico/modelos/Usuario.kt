package com.unacar.calendarioacademico.modelos

data class Usuario(
    var id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val tipoUsuario: String = "", // "profesor" o "estudiante"
    val estado: String = "activo",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "", "activo", 0, 0)
}