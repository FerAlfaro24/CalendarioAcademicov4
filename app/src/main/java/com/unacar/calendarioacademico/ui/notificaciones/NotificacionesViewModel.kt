package com.unacar.calendarioacademico.ui.notificaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Notificacion
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class NotificacionesViewModel : ViewModel() {

    private val _notificaciones = MutableLiveData<List<Notificacion>>()
    val notificaciones: LiveData<List<Notificacion>> = _notificaciones

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var listenerNotificaciones: ListenerRegistration? = null

    init {
        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        _cargando.value = true

        // Escuchar notificaciones en tiempo real
        listenerNotificaciones?.remove()
        listenerNotificaciones = AdministradorFirebase.escucharNotificacionesUsuario(usuario.uid) { notificaciones ->
            _notificaciones.value = notificaciones
            _cargando.value = false
        }
    }

    fun marcarComoLeida(idNotificacion: String) {
        AdministradorFirebase.marcarNotificacionLeida(idNotificacion)
            .addOnFailureListener {
                _error.value = "Error al marcar notificación como leída: ${it.message}"
            }
    }

    fun marcarTodasComoLeidas() {
        val notificacionesActuales = _notificaciones.value ?: return

        for (notificacion in notificacionesActuales) {
            if (!notificacion.leida) {
                AdministradorFirebase.marcarNotificacionLeida(notificacion.id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerNotificaciones?.remove()
    }
}