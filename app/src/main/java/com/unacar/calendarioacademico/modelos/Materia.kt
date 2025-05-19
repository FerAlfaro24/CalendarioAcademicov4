package com.unacar.calendarioacademico.modelos

data class Materia(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val idProfesor: String = "",
    val semestre: String = "",
    val estado: String = "activo",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "", "", "activo", 0, 0)
}