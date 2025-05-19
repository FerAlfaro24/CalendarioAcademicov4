package com.unacar.calendarioacademico.ui.estudiantes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class AgregarEstudiantesViewModel : ViewModel() {

    private val _estudiantes = MutableLiveData<List<Usuario>>()
    val estudiantes: LiveData<List<Usuario>> = _estudiantes

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _estudianteAgregado = MutableLiveData<Boolean>()
    val estudianteAgregado: LiveData<Boolean> = _estudianteAgregado

    private var materiaId: String = ""

    init {
        _estudiantes.value = emptyList()
    }

    fun setMateriaId(id: String) {
        materiaId = id
    }

    fun buscarEstudiantePorCorreo(correo: String) {
        _cargando.value = true

        // Verificar primero que no sea un profesor
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipoUsuario", "estudiante")
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    _error.value = "No se encontró ningún estudiante con ese correo"
                    _estudiantes.value = emptyList()
                    _cargando.value = false
                    return@addOnSuccessListener
                }

                val estudiantes = mutableListOf<Usuario>()

                for (documento in documentos) {
                    val estudiante = documento.toObject(Usuario::class.java)
                    estudiante.id = documento.id

                    // Verificar si ya está inscrito en la materia
                    verificarInscripcion(estudiante) { estaInscrito ->
                        if (!estaInscrito) {
                            estudiantes.add(estudiante)
                        }

                        _estudiantes.value = estudiantes
                        _cargando.value = false
                    }
                }
            }
            .addOnFailureListener {
                _error.value = "Error al buscar estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    private fun verificarInscripcion(estudiante: Usuario, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("inscripciones")
            .whereEqualTo("idEstudiante", estudiante.id)
            .whereEqualTo("idMateria", materiaId)
            .get()
            .addOnSuccessListener { documentos ->
                callback(!documentos.isEmpty)
            }
            .addOnFailureListener {
                _error.value = "Error al verificar inscripción: ${it.message}"
                callback(false) // Asumimos que no está inscrito en caso de error
            }
    }

    fun agregarEstudianteAMateria(idEstudiante: String) {
        _cargando.value = true

        AdministradorFirebase.inscribirEstudiante(idEstudiante, materiaId)
            .addOnSuccessListener {
                _estudianteAgregado.value = true
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al inscribir estudiante: ${it.message}"
                _cargando.value = false
            }
    }
}