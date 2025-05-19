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

class DetalleEstudianteViewModel : ViewModel() {

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

    private val _accionExitosa = MutableLiveData<Boolean>()
    val accionExitosa: LiveData<Boolean> = _accionExitosa

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

                        // Cargar materias inscritas
                        cargarMateriasEstudiante(idEstudiante)
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

    private fun cargarMateriasEstudiante(idEstudiante: String) {
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
            if (idsMaterias.isEmpty()) {
                _materias.value = emptyList()
                _nombresProfesor.value = emptyMap()
                _cargando.value = false
                return@escucharMateriasEstudiante
            }

            // Ahora obtener los detalles de cada materia
            val db = FirebaseFirestore.getInstance()
            val listaMaterias = mutableListOf<Materia>()
            val idsProfesor = mutableSetOf<String>()
            var materiasCompletadas = 0

            for (idMateria in idsMaterias) {
                db.collection("materias").document(idMateria)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        materiasCompletadas++

                        val materia = documentSnapshot.toObject(Materia::class.java)
                        if (materia != null) {
                            materia.id = documentSnapshot.id

                            // Solo agregar materias del profesor actual
                            if (materia.idProfesor == idProfesorActual) {
                                listaMaterias.add(materia)
                                idsProfesor.add(materia.idProfesor)
                            }
                        }

                        if (materiasCompletadas == idsMaterias.size) {
                            _materias.value = listaMaterias

                            // Cargar nombres de profesores
                            cargarNombresProfesores(idsProfesor.toList())
                        }
                    }
                    .addOnFailureListener {
                        materiasCompletadas++

                        if (materiasCompletadas == idsMaterias.size) {
                            _materias.value = listaMaterias

                            // Cargar nombres de profesores
                            cargarNombresProfesores(idsProfesor.toList())
                        }
                    }
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

    fun desinscribirDeMateria(idEstudiante: String, idMateria: String) {
        _cargando.value = true

        AdministradorFirebase.eliminarInscripcion(idEstudiante, idMateria)
            .addOnSuccessListener {
                _accionExitosa.value = true
                // No es necesario recargar materias porque el listener lo hace automáticamente
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al desinscribir estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener al destruir el ViewModel
        listenerInscripciones?.remove()
    }
}