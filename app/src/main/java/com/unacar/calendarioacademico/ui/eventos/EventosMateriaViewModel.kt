package com.unacar.calendarioacademico.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class EventosMateriaViewModel : ViewModel() {

    private val _materia = MutableLiveData<Materia>()
    val materia: LiveData<Materia> = _materia

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _esProfesor = MutableLiveData<Boolean>()
    val esProfesor: LiveData<Boolean> = _esProfesor

    private var listenerEventos: ListenerRegistration? = null

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
                    }
                }
                .addOnFailureListener {
                    _error.value = "Error al verificar tipo de usuario: ${it.message}"
                }
        }
    }

    fun cargarEventosMateria(idMateria: String) {
        _cargando.value = true

        // Cargar datos de la materia
        AdministradorFirebase.obtenerMateria(idMateria).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val materia = documento.toObject(Materia::class.java)
                    if (materia != null) {
                        materia.id = documento.id
                        _materia.value = materia
                    }
                }
            }
            .addOnFailureListener {
                _error.value = "Error al cargar materia: ${it.message}"
            }

        // Escuchar eventos en tiempo real
        listenerEventos?.remove()
        listenerEventos = AdministradorFirebase.escucharEventosPorMateria(idMateria) { eventos ->
            _eventos.value = eventos.sortedBy { it.fecha }
            _cargando.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerEventos?.remove()
    }
}