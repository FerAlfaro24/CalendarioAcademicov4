package com.unacar.calendarioacademico.ui.estudiantes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class GestionarEstudiantesViewModel : ViewModel() {

    private val _estudiantes = MutableLiveData<List<Usuario>>()
    val estudiantes: LiveData<List<Usuario>> = _estudiantes

    private val _contadoresMaterias = MutableLiveData<Map<String, Int>>()
    val contadoresMaterias: LiveData<Map<String, Int>> = _contadoresMaterias

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var listenerEstudiantes: ListenerRegistration? = null

    init {
        cargarEstudiantes()
    }

    private fun cargarEstudiantes() {
        _cargando.value = true

        // Detener listener anterior si existe
        listenerEstudiantes?.remove()

        // Crear nuevo listener en tiempo real
        listenerEstudiantes = AdministradorFirebase.escucharTodosLosEstudiantes { estudiantes ->
            _estudiantes.value = estudiantes

            // Cargar contadores de materias
            cargarContadoresMaterias(estudiantes.map { it.id })
        }
    }

    private fun cargarContadoresMaterias(idsEstudiantes: List<String>) {
        if (idsEstudiantes.isEmpty()) {
            _contadoresMaterias.value = emptyMap()
            _cargando.value = false
            return
        }

        val idProfesorActual = FirebaseAuth.getInstance().currentUser?.uid

        if (idProfesorActual == null) {
            _error.value = "Error: No hay sesión activa"
            _cargando.value = false
            return
        }

        val db = FirebaseFirestore.getInstance()
        val contadores = mutableMapOf<String, Int>()
        var estudiantesCompletados = 0

        for (idEstudiante in idsEstudiantes) {
            // Primero obtenemos todas las inscripciones del estudiante
            db.collection("inscripciones")
                .whereEqualTo("idEstudiante", idEstudiante)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    estudiantesCompletados++

                    if (querySnapshot.isEmpty) {
                        contadores[idEstudiante] = 0

                        if (estudiantesCompletados == idsEstudiantes.size) {
                            _contadoresMaterias.value = contadores
                            _cargando.value = false
                        }
                        return@addOnSuccessListener
                    }

                    // Obtenemos IDs de todas las materias inscritas
                    val idsMaterias = querySnapshot.documents.mapNotNull { it.getString("idMateria") }

                    // Verificamos cuáles son del profesor actual
                    if (idsMaterias.isEmpty()) {
                        contadores[idEstudiante] = 0

                        if (estudiantesCompletados == idsEstudiantes.size) {
                            _contadoresMaterias.value = contadores
                            _cargando.value = false
                        }
                    } else {
                        var materiasProfesor = 0
                        var materiasVerificadas = 0

                        for (idMateria in idsMaterias) {
                            db.collection("materias").document(idMateria)
                                .get()
                                .addOnSuccessListener { docMateria ->
                                    materiasVerificadas++

                                    if (docMateria.exists() &&
                                        docMateria.getString("idProfesor") == idProfesorActual) {
                                        materiasProfesor++
                                    }

                                    if (materiasVerificadas == idsMaterias.size) {
                                        contadores[idEstudiante] = materiasProfesor

                                        if (estudiantesCompletados == idsEstudiantes.size) {
                                            _contadoresMaterias.value = contadores
                                            _cargando.value = false
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    materiasVerificadas++

                                    if (materiasVerificadas == idsMaterias.size) {
                                        contadores[idEstudiante] = materiasProfesor

                                        if (estudiantesCompletados == idsEstudiantes.size) {
                                            _contadoresMaterias.value = contadores
                                            _cargando.value = false
                                        }
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    estudiantesCompletados++

                    contadores[idEstudiante] = 0

                    if (estudiantesCompletados == idsEstudiantes.size) {
                        _contadoresMaterias.value = contadores
                        _cargando.value = false
                    }
                }
        }
    }

    fun buscarEstudiantePorCorreo(correo: String) {
        if (correo.isEmpty()) {
            cargarEstudiantes()
            return
        }

        _cargando.value = true

        // Detener listener anterior si existe
        listenerEstudiantes?.remove()

        // Para búsquedas específicas, usamos una consulta puntual en lugar de un listener
        AdministradorFirebase.buscarEstudiantePorCorreo(correo)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val listaEstudiantes = mutableListOf<Usuario>()

                for (documento in querySnapshot.documents) {
                    val estudiante = documento.toObject(Usuario::class.java)
                    if (estudiante != null) {
                        estudiante.id = documento.id
                        listaEstudiantes.add(estudiante)
                    }
                }

                _estudiantes.value = listaEstudiantes

                if (listaEstudiantes.isNotEmpty()) {
                    cargarContadoresMaterias(listaEstudiantes.map { it.id })
                } else {
                    _contadoresMaterias.value = emptyMap()
                    _cargando.value = false
                    _error.value = "No se encontraron estudiantes con ese correo"
                }
            }
            .addOnFailureListener {
                _error.value = "Error al buscar estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener al destruir el ViewModel
        listenerEstudiantes?.remove()
    }
}