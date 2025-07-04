package com.unacar.calendarioacademico.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class DetalleEventoViewModel : ViewModel() {

    private val _evento = MutableLiveData<Evento>()
    val evento: LiveData<Evento> = _evento

    private val _materia = MutableLiveData<Materia>()
    val materia: LiveData<Materia> = _materia

    private val _esProfesor = MutableLiveData<Boolean>()
    val esProfesor: LiveData<Boolean> = _esProfesor

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _eventoEliminado = MutableLiveData<Boolean>()
    val eventoEliminado: LiveData<Boolean> = _eventoEliminado

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

    fun cargarDetalleEvento(idEvento: String) {
        _cargando.value = true

        AdministradorFirebase.obtenerEvento(idEvento).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val evento = documento.toObject(Evento::class.java)
                    if (evento != null) {
                        evento.id = documento.id
                        _evento.value = evento

                        // Cargar datos de la materia
                        cargarMateria(evento.idMateria)
                    } else {
                        _error.value = "Error al obtener datos del evento"
                        _cargando.value = false
                    }
                } else {
                    _error.value = "El evento no existe"
                    _cargando.value = false
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Error al cargar evento: ${e.message}"
                _cargando.value = false
            }
    }

    private fun cargarMateria(idMateria: String) {
        AdministradorFirebase.obtenerMateria(idMateria).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val materia = documento.toObject(Materia::class.java)
                    if (materia != null) {
                        materia.id = documento.id
                        _materia.value = materia
                    }
                }
                _cargando.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error al cargar materia: ${e.message}"
                _cargando.value = false
            }
    }

    fun eliminarEvento(idEvento: String) {
        _cargando.value = true

        AdministradorFirebase.eliminarEvento(idEvento)
            .addOnSuccessListener {
                _eventoEliminado.value = true
                _cargando.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "Error al eliminar evento: ${e.message}"
                _cargando.value = false
            }
    }
}