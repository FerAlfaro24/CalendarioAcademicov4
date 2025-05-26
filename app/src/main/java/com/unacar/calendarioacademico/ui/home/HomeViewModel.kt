package com.unacar.calendarioacademico.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class HomeViewModel : ViewModel() {

    private val _nombre = MutableLiveData<String>()
    val nombre: LiveData<String> = _nombre

    private val _tipoUsuario = MutableLiveData<String>()
    val tipoUsuario: LiveData<String> = _tipoUsuario

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    private val _proximosEventos = MutableLiveData<List<Evento>>()
    val proximosEventos: LiveData<List<Evento>> = _proximosEventos

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var listenerMaterias: ListenerRegistration? = null
    private var listenerInscripciones: ListenerRegistration? = null

    private val TAG = "HomeViewModel"

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
                        _nombre.value = documento.getString("nombre") ?: "Usuario"
                        val tipoUsuario = documento.getString("tipoUsuario") ?: "estudiante"
                        _tipoUsuario.value = tipoUsuario

                        Log.d(TAG, "Tipo de usuario: $tipoUsuario")

                        // Cargar materias según tipo de usuario
                        if (tipoUsuario == "profesor") {
                            cargarMateriasPorProfesor(usuario.uid)
                        } else {
                            cargarMateriasEstudiante(usuario.uid)
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
            Log.d(TAG, "IDs de materias inscritas: ${idsMaterias.size} - $idsMaterias")

            if (idsMaterias.isEmpty()) {
                Log.d(TAG, "Estudiante no tiene materias inscritas")
                _materias.value = emptyList()
                _proximosEventos.value = emptyList()
                _cargando.value = false
                return@escucharMateriasEstudiante
            }

            // Obtener los detalles de cada materia Y cargar próximos eventos
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
                        } else {
                            Log.w(TAG, "Materia no encontrada: $idMateria")
                        }

                        // Si ya procesamos todas las materias, actualizamos y cargamos eventos
                        if (materiasCompletadas == idsMaterias.size) {
                            Log.d(TAG, "Todas las materias procesadas: ${listaMaterias.size}")
                            _materias.value = listaMaterias
                            // Cargar próximos eventos después de cargar materias
                            cargarProximosEventos(idsMaterias)
                        }
                    }
                    .addOnFailureListener { exception ->
                        materiasCompletadas++
                        Log.e(TAG, "Error al cargar materia $idMateria: ${exception.message}")

                        if (materiasCompletadas == idsMaterias.size) {
                            _materias.value = listaMaterias
                            // Cargar próximos eventos incluso si algunas materias fallaron
                            cargarProximosEventos(idsMaterias)
                        }
                    }
            }
        }
    }

    fun cargarProximosEventos() {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) return

        // Obtener materias inscritas del estudiante
        AdministradorFirebase.escucharMateriasEstudiante(usuario.uid) { idsMaterias ->
            cargarProximosEventos(idsMaterias)
        }
    }

    private fun cargarProximosEventos(idsMaterias: List<String>) {
        Log.d(TAG, "Cargando próximos eventos para materias: $idsMaterias")

        if (idsMaterias.isEmpty()) {
            Log.d(TAG, "No hay materias para cargar eventos")
            _proximosEventos.value = emptyList()
            _cargando.value = false
            return
        }

        // Cargar eventos por materia individual para evitar problemas con whereIn
        val db = FirebaseFirestore.getInstance()
        val todosLosEventos = mutableListOf<Evento>()
        var materiasConsultadas = 0
        val fechaActual = System.currentTimeMillis()

        Log.d(TAG, "Fecha actual para filtro: $fechaActual")

        for (idMateria in idsMaterias) {
            Log.d(TAG, "Consultando eventos para materia: $idMateria")

            db.collection("eventos")
                .whereEqualTo("idMateria", idMateria)
                .whereGreaterThanOrEqualTo("fecha", fechaActual)
                .orderBy("fecha", Query.Direction.ASCENDING)
                .limit(3) // Limitar por materia
                .get()
                .addOnSuccessListener { querySnapshot ->
                    materiasConsultadas++
                    Log.d(TAG, "Eventos encontrados para materia $idMateria: ${querySnapshot.size()}")

                    for (documento in querySnapshot.documents) {
                        val evento = documento.toObject(Evento::class.java)
                        if (evento != null) {
                            evento.id = documento.id
                            todosLosEventos.add(evento)
                            Log.d(TAG, "Evento agregado: ${evento.titulo} - Fecha: ${evento.fecha}")
                        }
                    }

                    if (materiasConsultadas == idsMaterias.size) {
                        // Ordenar por fecha y tomar solo los primeros 3
                        todosLosEventos.sortBy { it.fecha }
                        val proximosEventos = todosLosEventos.take(3)
                        Log.d(TAG, "Próximos eventos finales: ${proximosEventos.size}")

                        _proximosEventos.value = proximosEventos
                        _cargando.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    materiasConsultadas++
                    Log.e(TAG, "Error al cargar eventos para materia $idMateria: ${exception.message}", exception)

                    if (materiasConsultadas == idsMaterias.size) {
                        todosLosEventos.sortBy { it.fecha }
                        val proximosEventos = todosLosEventos.take(3)
                        Log.d(TAG, "Próximos eventos finales (con errores): ${proximosEventos.size}")

                        _proximosEventos.value = proximosEventos
                        _cargando.value = false

                        // Solo mostrar error si no se cargaron eventos
                        if (proximosEventos.isEmpty()) {
                            _error.value = "Error al cargar eventos: ${exception.message}"
                        }
                    }
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