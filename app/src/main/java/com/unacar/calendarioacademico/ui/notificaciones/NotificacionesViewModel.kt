package com.unacar.calendarioacademico.ui.notificaciones

import android.util.Log
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
    private val TAG = "NotificacionesViewModel"

    init {
        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.e(TAG, "Usuario no autenticado")
            _error.value = "Usuario no autenticado"
            _cargando.value = false
            return
        }

        Log.d(TAG, "Iniciando carga de notificaciones para usuario: ${usuario.uid}")
        _cargando.value = true

        // Escuchar notificaciones en tiempo real
        listenerNotificaciones?.remove()
        listenerNotificaciones = AdministradorFirebase.escucharNotificacionesUsuario(usuario.uid) { notificaciones ->
            Log.d(TAG, "Notificaciones recibidas: ${notificaciones.size}")

            // Filtrar notificaciones válidas (que tienen ID)
            val notificacionesValidas = notificaciones.filter { it.id.isNotEmpty() }
            Log.d(TAG, "Notificaciones válidas: ${notificacionesValidas.size}")

            _notificaciones.value = notificacionesValidas
            _cargando.value = false

            // Solo mostrar error si hay un problema real, no si simplemente no hay notificaciones
            if (notificacionesValidas.isEmpty() && notificaciones.isNotEmpty()) {
                Log.w(TAG, "Algunas notificaciones no tienen ID válido")
            }
        }
    }

    fun marcarComoLeida(idNotificacion: String) {
        if (idNotificacion.isEmpty()) {
            Log.e(TAG, "ID de notificación inválido")
            _error.value = "ID de notificación inválido"
            return
        }

        Log.d(TAG, "Marcando notificación como leída: $idNotificacion")
        AdministradorFirebase.marcarNotificacionLeida(idNotificacion)
            .addOnSuccessListener {
                Log.d(TAG, "Notificación marcada como leída exitosamente")
                // La notificación se actualizará automáticamente por el listener
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al marcar notificación como leída: ${exception.message}", exception)
                _error.value = "Error al marcar notificación como leída: ${exception.message}"
            }
    }

    fun eliminarNotificacion(idNotificacion: String) {
        if (idNotificacion.isEmpty()) {
            Log.e(TAG, "ID de notificación inválido para eliminar")
            _error.value = "ID de notificación inválido"
            return
        }

        Log.d(TAG, "Eliminando notificación: $idNotificacion")
        val db = FirebaseFirestore.getInstance()
        db.collection("notificaciones").document(idNotificacion)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Notificación eliminada exitosamente")
                _notificacionEliminada.value = true
                // La lista se actualizará automáticamente por el listener
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al eliminar notificación: ${exception.message}", exception)
                _error.value = "Error al eliminar notificación: ${exception.message}"
            }
    }

    fun marcarTodasComoLeidas() {
        val notificacionesActuales = _notificaciones.value ?: return

        Log.d(TAG, "Marcando todas las notificaciones como leídas: ${notificacionesActuales.size}")

        for (notificacion in notificacionesActuales) {
            if (!notificacion.leida && notificacion.id.isNotEmpty()) {
                AdministradorFirebase.marcarNotificacionLeida(notificacion.id)
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error al marcar notificación ${notificacion.id} como leída: ${exception.message}")
                        _error.value = "Error al marcar notificación como leída: ${exception.message}"
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel destruido, limpiando listeners")
        listenerNotificaciones?.remove()
    }
}