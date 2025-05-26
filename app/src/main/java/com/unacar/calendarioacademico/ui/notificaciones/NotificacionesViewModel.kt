package com.unacar.calendarioacademico.ui.notificaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _notificacionEliminada = MutableLiveData<Boolean>()
    val notificacionEliminada: LiveData<Boolean> = _notificacionEliminada

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
            // Filtrar notificaciones válidas (que tienen ID)
            val notificacionesValidas = notificaciones.filter { it.id.isNotEmpty() }
            _notificaciones.value = notificacionesValidas
            _cargando.value = false
        }
    }

    fun marcarComoLeida(idNotificacion: String) {
        if (idNotificacion.isEmpty()) {
            _error.value = "ID de notificación inválido"
            return
        }

        AdministradorFirebase.marcarNotificacionLeida(idNotificacion)
            .addOnSuccessListener {
                // La notificación se actualizará automáticamente por el listener
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al marcar notificación como leída: ${exception.message}"
            }
    }

    fun eliminarNotificacion(idNotificacion: String) {
        if (idNotificacion.isEmpty()) {
            _error.value = "ID de notificación inválido"
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("notificaciones").document(idNotificacion)
            .delete()
            .addOnSuccessListener {
                _notificacionEliminada.value = true
                // La lista se actualizará automáticamente por el listener
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al eliminar notificación: ${exception.message}"
            }
    }

    fun marcarTodasComoLeidas() {
        val notificacionesActuales = _notificaciones.value ?: return

        for (notificacion in notificacionesActuales) {
            if (!notificacion.leida && notificacion.id.isNotEmpty()) {
                AdministradorFirebase.marcarNotificacionLeida(notificacion.id)
                    .addOnFailureListener { exception ->
                        _error.value = "Error al marcar notificación como leída: ${exception.message}"
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerNotificaciones?.remove()
    }
}