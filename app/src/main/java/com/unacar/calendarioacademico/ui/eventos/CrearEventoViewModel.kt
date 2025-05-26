package com.unacar.calendarioacademico.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Notificacion
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class CrearEventoViewModel : ViewModel() {

    private val _eventoCreado = MutableLiveData<Boolean>()
    val eventoCreado: LiveData<Boolean> = _eventoCreado

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun crearEvento(titulo: String, descripcion: String, fecha: Long, hora: String, tipo: String, idMateria: String) {
        _cargando.value = true

        // Crear objeto evento con los parámetros correctos
        val evento = Evento(
            id = "", // Se asignará automáticamente por Firestore
            titulo = titulo,
            descripcion = descripcion,
            fecha = fecha,
            hora = hora,
            idMateria = idMateria,
            tipo = tipo,
            fechaCreacion = System.currentTimeMillis(),
            fechaActualizacion = System.currentTimeMillis()
        )

        AdministradorFirebase.crearEvento(evento)
            .addOnSuccessListener { documentReference ->
                _eventoCreado.value = true
                _cargando.value = false

                // Crear notificación para estudiantes inscritos
                crearNotificacionParaEstudiantes(documentReference.id, evento.getTipoFormateado(), evento.titulo, idMateria)
            }
            .addOnFailureListener {
                _error.value = "Error al crear evento: ${it.message}"
                _cargando.value = false
            }
    }

    private fun crearNotificacionParaEstudiantes(idEvento: String, tipoEvento: String, tituloEvento: String, idMateria: String) {
        // Obtener estudiantes inscritos en la materia
        val db = FirebaseFirestore.getInstance()
        db.collection("inscripciones")
            .whereEqualTo("idMateria", idMateria)
            .get()
            .addOnSuccessListener { inscripciones ->
                for (documento in inscripciones.documents) {
                    val idEstudiante = documento.getString("idEstudiante")
                    if (idEstudiante != null) {
                        // Crear notificación para cada estudiante
                        val mensaje = "Nuevo $tipoEvento: $tituloEvento"
                        val notificacion = Notificacion(
                            id = "", // Se asignará automáticamente
                            mensaje = mensaje,
                            titulo = tituloEvento,
                            tipo = tipoEvento.lowercase(),
                            idUsuario = idEstudiante,
                            idEvento = idEvento,
                            estado = "no_leida",
                            leida = false,
                            fechaCreacion = System.currentTimeMillis()
                        )

                        AdministradorFirebase.crearNotificacion(notificacion)
                    }
                }
            }
            .addOnFailureListener {
                // No es crítico si falla la creación de notificaciones
                // El evento ya se creó correctamente
            }
    }
}