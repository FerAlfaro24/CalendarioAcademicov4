package com.unacar.calendarioacademico.ui.estudiantes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class SeleccionarMateriaViewModel : ViewModel() {

    private val _estudiante = MutableLiveData<Usuario>()
    val estudiante: LiveData<Usuario> = _estudiante

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    private val _nombresProfesor = MutableLiveData<Map<String, String>>()
    val nombresProfesor: LiveData<Map<String, String>> = _nombresProfesor

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _inscripcionExitosa = MutableLiveData<Boolean>()
    val inscripcionExitosa: LiveData<Boolean> = _inscripcionExitosa

    private var listenerInscripciones: ListenerRegistration? = null

    fun cargarEstudiante(idEstudiante: String) {
        _cargando.value = true

        AdministradorFirebase.obtenerPerfilUsuario(idEstudiante).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val estudiante = documento.toObject(Usuario::class.java)
                    if (estudiante != null) {
                        estudiante.id = documento.id
                        _estudiante.value = estudiante

                        // Cargar materias disponibles
                        cargarMateriasDisponibles(idEstudiante)
                    } else {
                        _error.value = "Error al obtener datos del estudiante"
                        _cargando.value = false
                    }
                } else {
                    _error.value = "Estudiante no encontrado"
                    _cargando.value = false
                }
            }
            .addOnFailureListener {
                _error.value = "Error al cargar datos del estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    private fun cargarMateriasDisponibles(idEstudiante: String) {
        _cargando.value = true

        // Obtener el ID del profesor actual
        val idProfesorActual = FirebaseAuth.getInstance().currentUser?.uid

        if (idProfesorActual == null) {
            _error.value = "Error: No hay sesión activa"
            _cargando.value = false
            return
        }

        // Detener listener anterior si existe
        listenerInscripciones?.remove()

        // Crear nuevo listener en tiempo real
        listenerInscripciones = AdministradorFirebase.escucharMateriasEstudiante(idEstudiante) { idsMaterias ->
            val materiasInscritas = idsMaterias.toSet()

            // Ahora cargamos solo las materias del profesor actual
            val db = FirebaseFirestore.getInstance()
            db.collection("materias")
                .whereEqualTo("idProfesor", idProfesorActual)
                .get()
                .addOnSuccessListener { materiasSnapshot ->
                    val materiasDisponibles = mutableListOf<Materia>()
                    val idsProfesor = mutableSetOf<String>()

                    for (documento in materiasSnapshot.documents) {
                        val materia = documento.toObject(Materia::class.java)
                        if (materia != null && !materiasInscritas.contains(documento.id)) {
                            materia.id = documento.id
                            materiasDisponibles.add(materia)
                            idsProfesor.add(materia.idProfesor)
                        }
                    }

                    _materias.value = materiasDisponibles

                    // Si no hay materias disponibles, mostrar mensaje
                    if (materiasDisponibles.isEmpty()) {
                        _error.value = "No tienes materias disponibles para inscribir a este estudiante"
                    }

                    // Cargar nombres de profesores
                    cargarNombresProfesores(idsProfesor.toList())
                }
                .addOnFailureListener {
                    _error.value = "Error al cargar materias: ${it.message}"
                    _cargando.value = false
                }
        }
    }

    private fun cargarNombresProfesores(idsProfesores: List<String>) {
        if (idsProfesores.isEmpty()) {
            _nombresProfesor.value = emptyMap()
            _cargando.value = false
            return
        }

        val db = FirebaseFirestore.getInstance()
        val nombresProfesor = mutableMapOf<String, String>()
        var profesoresCompletados = 0

        for (idProfesor in idsProfesores) {
            db.collection("usuarios").document(idProfesor)
                .get()
                .addOnSuccessListener { documento ->
                    profesoresCompletados++

                    if (documento.exists()) {
                        val nombre = documento.getString("nombre") ?: "Desconocido"
                        nombresProfesor[idProfesor] = nombre
                    }

                    if (profesoresCompletados == idsProfesores.size) {
                        _nombresProfesor.value = nombresProfesor
                        _cargando.value = false
                    }
                }
                .addOnFailureListener {
                    profesoresCompletados++

                    if (profesoresCompletados == idsProfesores.size) {
                        _nombresProfesor.value = nombresProfesor
                        _cargando.value = false
                    }
                }
        }
    }

    fun inscribirEnMateria(idEstudiante: String, idMateria: String) {
        _cargando.value = true

        AdministradorFirebase.inscribirEstudiante(idEstudiante, idMateria)
            .addOnSuccessListener {
                _inscripcionExitosa.value = true
                // No necesitamos recargar materias manualmente, el listener lo hará automáticamente
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al inscribir estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener al destruir el ViewModel
        listenerInscripciones?.remove()
    }
}