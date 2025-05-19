package com.unacar.calendarioacademico.ui.materias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class DetalleMateriaViewModel : ViewModel() {

    private val _materia = MutableLiveData<Materia>()
    val materia: LiveData<Materia> = _materia

    private val _profesor = MutableLiveData<Usuario>()
    val profesor: LiveData<Usuario> = _profesor

    private val _estudiantes = MutableLiveData<List<Usuario>>()
    val estudiantes: LiveData<List<Usuario>> = _estudiantes

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _esProfesor = MutableLiveData<Boolean>()
    val esProfesor: LiveData<Boolean> = _esProfesor

    private val _eliminacionExitosa = MutableLiveData<Boolean>()
    val eliminacionExitosa: LiveData<Boolean> = _eliminacionExitosa

    private var listenerEstudiantes: ListenerRegistration? = null

    init {
        verificarTipoUsuario()
    }

    private fun verificarTipoUsuario() {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario != null) {
            AdministradorFirebase.obtenerPerfilUsuario(usuario.uid).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val tipoUsuario = documento.getString("tipoUsuario") ?: "estudiante"
                        _esProfesor.value = tipoUsuario == "profesor"
                    } else {
                        _error.value = "No se encontró el perfil del usuario"
                    }
                }
                .addOnFailureListener {
                    _error.value = "Error al verificar tipo de usuario: ${it.message}"
                }
        }
    }

    fun cargarMateria(idMateria: String) {
        _cargando.value = true

        // Obtener datos de la materia
        AdministradorFirebase.obtenerMateria(idMateria).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val materia = documento.toObject(Materia::class.java)
                    if (materia != null) {
                        materia.id = documento.id
                        _materia.value = materia

                        // Cargar datos del profesor
                        cargarProfesor(materia.idProfesor)

                        // Cargar estudiantes inscritos
                        cargarEstudiantes(idMateria)
                    } else {
                        _error.value = "Error al obtener datos de la materia"
                        _cargando.value = false
                    }
                } else {
                    _error.value = "La materia no existe"
                    _cargando.value = false
                }
            }
            .addOnFailureListener {
                _error.value = "Error al cargar materia: ${it.message}"
                _cargando.value = false
            }
    }

    private fun cargarProfesor(idProfesor: String) {
        AdministradorFirebase.obtenerPerfilUsuario(idProfesor).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val profesor = documento.toObject(Usuario::class.java)
                    if (profesor != null) {
                        profesor.id = documento.id
                        _profesor.value = profesor
                    }
                }
            }
            .addOnFailureListener {
                _error.value = "Error al cargar datos del profesor: ${it.message}"
            }
    }

    private fun cargarEstudiantes(idMateria: String) {
        // Detener listener anterior si existe
        listenerEstudiantes?.remove()

        // Crear nuevo listener en tiempo real
        listenerEstudiantes = AdministradorFirebase.escucharEstudiantesMateria(idMateria) { idsEstudiantes ->
            if (idsEstudiantes.isEmpty()) {
                _estudiantes.value = emptyList()
                _cargando.value = false
                return@escucharEstudiantesMateria
            }

            // Obtener datos de cada estudiante
            val listaEstudiantes = mutableListOf<Usuario>()
            var estudiantesCompletados = 0

            for (idEstudiante in idsEstudiantes) {
                AdministradorFirebase.obtenerPerfilUsuario(idEstudiante).get()
                    .addOnSuccessListener { documento ->
                        estudiantesCompletados++

                        if (documento.exists()) {
                            val estudiante = documento.toObject(Usuario::class.java)
                            if (estudiante != null) {
                                estudiante.id = documento.id
                                listaEstudiantes.add(estudiante)
                            }
                        }

                        if (estudiantesCompletados == idsEstudiantes.size) {
                            _estudiantes.value = listaEstudiantes
                            _cargando.value = false
                        }
                    }
                    .addOnFailureListener {
                        estudiantesCompletados++

                        if (estudiantesCompletados == idsEstudiantes.size) {
                            _estudiantes.value = listaEstudiantes
                            _cargando.value = false
                        }
                    }
            }
        }
    }

    fun eliminarEstudiante(idEstudiante: String, idMateria: String) {
        _cargando.value = true

        AdministradorFirebase.eliminarInscripcion(idEstudiante, idMateria)
            .addOnSuccessListener {
                // No es necesario recargar estudiantes porque el listener lo hace automáticamente
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al eliminar estudiante: ${it.message}"
                _cargando.value = false
            }
    }

    fun eliminarMateria(idMateria: String) {
        _cargando.value = true

        // Primero eliminar todas las inscripciones asociadas a esta materia
        AdministradorFirebase.eliminarInscripcionesMateria(idMateria)
            .addOnSuccessListener { querySnapshot ->
                val batch = FirebaseFirestore.getInstance().batch()

                // Agregar todas las eliminaciones de inscripciones en un lote
                for (documento in querySnapshot.documents) {
                    batch.delete(documento.reference)
                }

                // Ejecutar el lote de eliminaciones
                batch.commit()
                    .addOnSuccessListener {
                        // Ahora eliminar la materia
                        AdministradorFirebase.eliminarMateria(idMateria)
                            .addOnSuccessListener {
                                _eliminacionExitosa.value = true
                                _cargando.value = false
                            }
                            .addOnFailureListener { e ->
                                _error.value = "Error al eliminar materia: ${e.message}"
                                _cargando.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        _error.value = "Error al eliminar inscripciones: ${e.message}"
                        _cargando.value = false
                    }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al obtener inscripciones: ${e.message}"
                _cargando.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener al destruir el ViewModel
        listenerEstudiantes?.remove()
    }
}