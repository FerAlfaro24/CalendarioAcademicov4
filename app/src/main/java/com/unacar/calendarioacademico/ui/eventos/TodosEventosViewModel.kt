package com.unacar.calendarioacademico.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class TodosEventosViewModel : ViewModel() {

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos

    private val _nombresMateria = MutableLiveData<Map<String, String>>()
    val nombresMateria: LiveData<Map<String, String>> = _nombresMateria

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _eventoEliminado = MutableLiveData<Boolean>()
    val eventoEliminado: LiveData<Boolean> = _eventoEliminado

    private var listenerEventos: ListenerRegistration? = null

    init {
        cargarEventosProfesor()
    }

    private fun cargarEventosProfesor() {
        _cargando.value = true
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            _error.value = "Usuario no autenticado"
            _cargando.value = false
            return
        }

        // Primero obtenemos las materias del profesor
        val db = FirebaseFirestore.getInstance()
        db.collection("materias")
            .whereEqualTo("idProfesor", usuario.uid)
            .get()
            .addOnSuccessListener { materiasSnapshot ->
                if (materiasSnapshot.isEmpty) {
                    _eventos.value = emptyList()
                    _nombresMateria.value = emptyMap()
                    _cargando.value = false
                    return@addOnSuccessListener
                }

                val idsMaterias = materiasSnapshot.documents.map { it.id }
                val nombresMateria = mutableMapOf<String, String>()

                // Guardar nombres de materias
                for (documento in materiasSnapshot.documents) {
                    val nombre = documento.getString("nombre") ?: "Materia sin nombre"
                    nombresMateria[documento.id] = nombre
                }
                _nombresMateria.value = nombresMateria

                // Ahora cargar eventos de todas las materias del profesor
                cargarEventosPorMaterias(idsMaterias)
            }
            .addOnFailureListener {
                _error.value = "Error al cargar materias: ${it.message}"
                _cargando.value = false
            }
    }

    private fun cargarEventosPorMaterias(idsMaterias: List<String>) {
        if (idsMaterias.isEmpty()) {
            _eventos.value = emptyList()
            _cargando.value = false
            return
        }

        val db = FirebaseFirestore.getInstance()
        val todosLosEventos = mutableListOf<Evento>()
        var materiasCompletadas = 0

        for (idMateria in idsMaterias) {
            db.collection("eventos")
                .whereEqualTo("idMateria", idMateria)
                .get()
                .addOnSuccessListener { eventosSnapshot ->
                    materiasCompletadas++

                    for (documento in eventosSnapshot.documents) {
                        val evento = documento.toObject(Evento::class.java)
                        if (evento != null) {
                            evento.id = documento.id
                            todosLosEventos.add(evento)
                        }
                    }

                    if (materiasCompletadas == idsMaterias.size) {
                        // Ordenar eventos por fecha (más próximos primero)
                        todosLosEventos.sortBy { it.fecha }
                        _eventos.value = todosLosEventos
                        _cargando.value = false
                    }
                }
                .addOnFailureListener {
                    materiasCompletadas++

                    if (materiasCompletadas == idsMaterias.size) {
                        todosLosEventos.sortBy { it.fecha }
                        _eventos.value = todosLosEventos
                        _cargando.value = false
                    }
                }
        }
    }

    fun eliminarEvento(idEvento: String) {
        _cargando.value = true

        AdministradorFirebase.eliminarEvento(idEvento)
            .addOnSuccessListener {
                _eventoEliminado.value = true
                _cargando.value = false
                // Recargar eventos
                cargarEventosProfesor()
            }
            .addOnFailureListener {
                _error.value = "Error al eliminar evento: ${it.message}"
                _cargando.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener si existe
        listenerEventos?.remove()
    }
}