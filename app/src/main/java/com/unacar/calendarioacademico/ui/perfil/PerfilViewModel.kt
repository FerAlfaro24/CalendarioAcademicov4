package com.unacar.calendarioacademico.ui.perfil

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class PerfilViewModel : ViewModel() {

    private val _usuario = MutableLiveData<Usuario>()
    val usuario: LiveData<Usuario> = _usuario

    private val _tipoUsuario = MutableLiveData<String>()
    val tipoUsuario: LiveData<String> = _tipoUsuario

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    private val _estadisticas = MutableLiveData<Map<String, Int>>()
    val estadisticas: LiveData<Map<String, Int>> = _estadisticas

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var listenerMaterias: ListenerRegistration? = null
    private var listenerInscripciones: ListenerRegistration? = null

    private val TAG = "PerfilViewModel"

    init {
        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        _cargando.value = true
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {
            Log.d(TAG, "Cargando datos para usuario: ${usuario.uid}")
            AdministradorFirebase.obtenerPerfilUsuario(usuario.uid).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val usuarioObj = documento.toObject(Usuario::class.java)
                        if (usuarioObj != null) {
                            usuarioObj.id = documento.id
                            _usuario.value = usuarioObj
                            _tipoUsuario.value = usuarioObj.tipoUsuario

                            Log.d(TAG, "Usuario cargado: ${usuarioObj.nombre}, tipo: ${usuarioObj.tipoUsuario}")

                            // Cargar materias según tipo de usuario
                            if (usuarioObj.tipoUsuario == "profesor") {
                                cargarMateriasPorProfesor(usuario.uid)
                                cargarEstadisticasProfesor(usuario.uid)
                            } else {
                                cargarMateriasEstudiante(usuario.uid)
                                cargarEstadisticasEstudiante(usuario.uid)
                            }
                        }
                    } else {
                        Log.e(TAG, "No se encontró el perfil del usuario")
                        _error.value = "No se encontró el perfil del usuario"
                        _cargando.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al cargar perfil: ${exception.message}", exception)
                    _error.value = "Error al cargar perfil: ${exception.message}"
                    _cargando.value = false
                }
        } else {
            Log.e(TAG, "Usuario no autenticado")
            _error.value = "Usuario no autenticado"
            _cargando.value = false
        }
    }

    private fun cargarMateriasPorProfesor(idProfesor: String) {
        Log.d(TAG, "Cargando materias del profesor: $idProfesor")
        // Detener listener anterior si existe
        listenerMaterias?.remove()

        // Crear nuevo listener en tiempo real
        listenerMaterias = AdministradorFirebase.escucharMateriasPorProfesor(idProfesor) { materias ->
            Log.d(TAG, "Materias del profesor cargadas: ${materias.size}")
            _materias.value = materias
            _cargando.value = false
        }
    }

    private fun cargarMateriasEstudiante(idEstudiante: String) {
        Log.d(TAG, "Cargando materias del estudiante: $idEstudiante")
        // Detener listeners anteriores si existen
        listenerInscripciones?.remove()

        // Crear nuevo listener en tiempo real para inscripciones
        listenerInscripciones = AdministradorFirebase.escucharMateriasEstudiante(idEstudiante) { idsMaterias ->
            Log.d(TAG, "IDs de materias inscritas: ${idsMaterias.size}")

            if (idsMaterias.isEmpty()) {
                Log.d(TAG, "Estudiante no tiene materias inscritas")
                _materias.value = emptyList()
                _cargando.value = false
                return@escucharMateriasEstudiante
            }

            // Obtener los detalles de cada materia
            val db = FirebaseFirestore.getInstance()
            val listaMaterias = mutableListOf<Materia>()
            var materiasCompletadas = 0

            for (idMateria in idsMaterias) {
                db.collection("materias").document(idMateria)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        materiasCompletadas++

                        if (documentSnapshot.exists()) {
                            val materia = documentSnapshot.toObject(Materia::class.java)
                            if (materia != null) {
                                materia.id = documentSnapshot.id
                                listaMaterias.add(materia)
                                Log.d(TAG, "Materia cargada: ${materia.nombre}")
                            }
                        }

                        // Si ya procesamos todas las materias
                        if (materiasCompletadas == idsMaterias.size) {
                            Log.d(TAG, "Todas las materias procesadas: ${listaMaterias.size}")
                            _materias.value = listaMaterias
                            _cargando.value = false
                        }
                    }
                    .addOnFailureListener { exception ->
                        materiasCompletadas++
                        Log.e(TAG, "Error al cargar materia $idMateria: ${exception.message}")

                        if (materiasCompletadas == idsMaterias.size) {
                            _materias.value = listaMaterias
                            _cargando.value = false
                        }
                    }
            }
        }
    }

    private fun cargarEstadisticasProfesor(idProfesor: String) {
        Log.d(TAG, "Cargando estadísticas del profesor")
        val db = FirebaseFirestore.getInstance()
        val estadisticas = mutableMapOf<String, Int>()

        // Contar materias
        db.collection("materias")
            .whereEqualTo("idProfesor", idProfesor)
            .get()
            .addOnSuccessListener { materiasSnapshot ->
                estadisticas["materias"] = materiasSnapshot.size()
                Log.d(TAG, "Materias del profesor: ${materiasSnapshot.size()}")

                // Contar eventos de todas las materias
                val idsMaterias = materiasSnapshot.documents.map { it.id }
                if (idsMaterias.isNotEmpty()) {
                    db.collection("eventos")
                        .whereIn("idMateria", idsMaterias)
                        .get()
                        .addOnSuccessListener { eventosSnapshot ->
                            estadisticas["eventos"] = eventosSnapshot.size()
                            Log.d(TAG, "Eventos del profesor: ${eventosSnapshot.size()}")

                            // Contar estudiantes inscritos en todas las materias
                            db.collection("inscripciones")
                                .whereIn("idMateria", idsMaterias)
                                .get()
                                .addOnSuccessListener { inscripcionesSnapshot ->
                                    // Contar estudiantes únicos
                                    val estudiantesUnicos = inscripcionesSnapshot.documents
                                        .mapNotNull { it.getString("idEstudiante") }
                                        .toSet()
                                    estadisticas["estudiantes"] = estudiantesUnicos.size
                                    Log.d(TAG, "Estudiantes únicos: ${estudiantesUnicos.size}")

                                    _estadisticas.value = estadisticas
                                }
                        }
                } else {
                    estadisticas["eventos"] = 0
                    estadisticas["estudiantes"] = 0
                    _estadisticas.value = estadisticas
                }
            }
    }

    private fun cargarEstadisticasEstudiante(idEstudiante: String) {
        Log.d(TAG, "Cargando estadísticas del estudiante")
        val db = FirebaseFirestore.getInstance()
        val estadisticas = mutableMapOf<String, Int>()

        // Contar materias inscritas
        db.collection("inscripciones")
            .whereEqualTo("idEstudiante", idEstudiante)
            .get()
            .addOnSuccessListener { inscripcionesSnapshot ->
                val idsMaterias = inscripcionesSnapshot.documents.mapNotNull { it.getString("idMateria") }
                estadisticas["materias"] = idsMaterias.size
                Log.d(TAG, "Materias del estudiante: ${idsMaterias.size}")

                if (idsMaterias.isNotEmpty()) {
                    // Contar próximos eventos
                    val fechaActual = System.currentTimeMillis()
                    db.collection("eventos")
                        .whereIn("idMateria", idsMaterias)
                        .whereGreaterThanOrEqualTo("fecha", fechaActual)
                        .get()
                        .addOnSuccessListener { eventosSnapshot ->
                            estadisticas["eventos"] = eventosSnapshot.size()
                            Log.d(TAG, "Próximos eventos: ${eventosSnapshot.size()}")

                            // Contar notificaciones no leídas
                            db.collection("notificaciones")
                                .whereEqualTo("idUsuario", idEstudiante)
                                .whereEqualTo("leida", false)
                                .get()
                                .addOnSuccessListener { notificacionesSnapshot ->
                                    estadisticas["notificaciones"] = notificacionesSnapshot.size()
                                    Log.d(TAG, "Notificaciones no leídas: ${notificacionesSnapshot.size()}")

                                    _estadisticas.value = estadisticas
                                }
                        }
                } else {
                    estadisticas["eventos"] = 0
                    estadisticas["notificaciones"] = 0
                    _estadisticas.value = estadisticas
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel destruido, limpiando listeners")
        // Limpiar listeners al destruir el ViewModel
        listenerMaterias?.remove()
        listenerInscripciones?.remove()
    }
}