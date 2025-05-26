package com.unacar.calendarioacademico.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class DetalleEventoViewModel : ViewModel() {

    private val _evento = MutableLiveData<Evento>()
    val evento: LiveData<Evento> = _evento

    private val _materia = MutableLiveData<Materia>()
    val materia: LiveData<Materia> = _materia

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _eventoEliminado = MutableLiveData<Boolean>()
    val eventoEliminado: LiveData<Boolean> = _eventoEliminado

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

    fun actualizarEvento(idEvento: String, titulo: String, descripcion: String, fecha: Long, hora: String, tipo: String) {
        _cargando.value = true

        val datos = mapOf(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "hora" to hora,
            "tipo" to tipo,
            "fechaActualizacion" to System.currentTimeMillis()
        )

        AdministradorFirebase.actualizarEvento(idEvento, datos)
            .addOnSuccessListener {
                // Recargar el evento actualizado
                cargarDetalleEvento(idEvento)
            }
            .addOnFailureListener { e ->
                _error.value = "Error al actualizar evento: ${e.message}"
                _cargando.value = false
            }
    }
}