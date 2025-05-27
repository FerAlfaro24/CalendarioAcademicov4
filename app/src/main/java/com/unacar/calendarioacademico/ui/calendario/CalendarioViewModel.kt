package com.unacar.calendarioacademico.ui.calendario

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase
import java.text.SimpleDateFormat
import java.util.*

class CalendarioViewModel : ViewModel() {

    private val _eventosDelDia = MutableLiveData<List<Evento>>()
    val eventosDelDia: LiveData<List<Evento>> = _eventosDelDia

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _tipoUsuario = MutableLiveData<String>()
    val tipoUsuario: LiveData<String> = _tipoUsuario

    private var listenerInscripciones: ListenerRegistration? = null
    private val TAG = "CalendarioViewModel"

    init {
        verificarTipoUsuario()
    }

    private fun verificarTipoUsuario() {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario != null) {
            Log.d(TAG, "Usuario autenticado: ${usuario.uid}")
            AdministradorFirebase.obtenerPerfilUsuario(usuario.uid).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val tipo = documento.getString("tipoUsuario") ?: "estudiante"
                        _tipoUsuario.value = tipo
                        Log.d(TAG, "Tipo de usuario verificado: $tipo")

                        if (tipo == "estudiante") {
                            // Cargar eventos para hoy por defecto
                            val hoy = Calendar.getInstance().timeInMillis
                            Log.d(TAG, "Cargando eventos para hoy: $hoy")
                            cargarEventosPorFecha(hoy)
                        } else {
                            Log.d(TAG, "Usuario no es estudiante, no se cargan eventos")
                        }
                    } else {
                        Log.e(TAG, "No se encontró el documento del usuario")
                        _error.value = "No se encontró el perfil del usuario"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al verificar tipo de usuario", e)
                    _error.value = "Error al verificar tipo de usuario: ${e.message}"
                }
        } else {
            Log.e(TAG, "Usuario no autenticado")
            _error.value = "Usuario no autenticado"
        }
    }

    fun cargarEventosPorFecha(fechaSeleccionada: Long) {
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Log.e(TAG, "Usuario no autenticado al cargar eventos")
            _error.value = "Usuario no autenticado"
            return
        }

        // Solo proceder si es estudiante
        if (_tipoUsuario.value != "estudiante") {
            Log.d(TAG, "Usuario no es estudiante, saltando carga de eventos")
            return
        }

        _cargando.value = true

        // Formatear fecha para logs
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        Log.d(TAG, "=== INICIANDO CARGA DE EVENTOS ===")
        Log.d(TAG, "Usuario: ${usuario.uid}")
        Log.d(TAG, "Fecha seleccionada: ${dateFormat.format(Date(fechaSeleccionada))} ($fechaSeleccionada)")

        // Calcular el rango de la fecha seleccionada
        val inicioDelDia = obtenerInicioDelDia(fechaSeleccionada)
        val finDelDia = obtenerFinDelDia(fechaSeleccionada)

        Log.d(TAG, "Rango de búsqueda:")
        Log.d(TAG, "- Inicio del día: ${dateFormat.format(Date(inicioDelDia))} ($inicioDelDia)")
        Log.d(TAG, "- Fin del día: ${dateFormat.format(Date(finDelDia))} ($finDelDia)")

        // Obtener las materias inscritas
        listenerInscripciones?.remove()
        listenerInscripciones = AdministradorFirebase.escucharMateriasEstudiante(usuario.uid) { idsMaterias ->
            Log.d(TAG, "=== MATERIAS DEL ESTUDIANTE ===")
            Log.d(TAG, "Cantidad de materias: ${idsMaterias.size}")
            idsMaterias.forEachIndexed { index, id ->
                Log.d(TAG, "Materia ${index + 1}: $id")
            }

            if (idsMaterias.isEmpty()) {
                Log.w(TAG, "El estudiante no tiene materias inscritas")
                _eventosDelDia.value = emptyList()
                _cargando.value = false
                return@escucharMateriasEstudiante
            }

            // Cargar eventos para esas materias en la fecha específica
            cargarEventosDeMateriasParaFecha(idsMaterias, inicioDelDia, finDelDia)
        }
    }

    private fun cargarEventosDeMateriasParaFecha(idsMaterias: List<String>, inicioDelDia: Long, finDelDia: Long) {
        val db = FirebaseFirestore.getInstance()
        val todosLosEventos = mutableListOf<Evento>()
        var materiasConsultadas = 0

        Log.d(TAG, "=== CONSULTANDO EVENTOS POR MATERIA ===")

        for (idMateria in idsMaterias) {
            Log.d(TAG, "Consultando eventos para materia: $idMateria")

            db.collection("eventos")
                .whereEqualTo("idMateria", idMateria)
                .whereGreaterThanOrEqualTo("fecha", inicioDelDia)
                .whereLessThanOrEqualTo("fecha", finDelDia)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    materiasConsultadas++
                    Log.d(TAG, "--- Materia: $idMateria ---")
                    Log.d(TAG, "Eventos encontrados para la fecha: ${querySnapshot.size()}")

                    for (documento in querySnapshot.documents) {
                        val evento = documento.toObject(Evento::class.java)
                        if (evento != null) {
                            evento.id = documento.id
                            todosLosEventos.add(evento)

                            val fechaEvento = Date(evento.fecha)
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            Log.d(TAG, "Evento: ${evento.titulo} - ${dateFormat.format(fechaEvento)} - ${evento.hora}")
                        }
                    }

                    // Cuando terminemos con todas las materias
                    if (materiasConsultadas == idsMaterias.size) {
                        Log.d(TAG, "=== RESULTADO FINAL ===")
                        Log.d(TAG, "Total eventos encontrados para la fecha: ${todosLosEventos.size}")

                        // Ordenar eventos por hora
                        todosLosEventos.sortBy {
                            // Convertir hora "HH:mm" a minutos para ordenar correctamente
                            val partes = it.hora.split(":")
                            if (partes.size == 2) {
                                partes[0].toIntOrNull()?.times(60)?.plus(partes[1].toIntOrNull() ?: 0) ?: 0
                            } else 0
                        }

                        _eventosDelDia.value = todosLosEventos
                        _cargando.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    materiasConsultadas++
                    Log.e(TAG, "Error al cargar eventos para materia $idMateria", exception)

                    if (materiasConsultadas == idsMaterias.size) {
                        Log.d(TAG, "=== TERMINADO CON ERRORES ===")
                        Log.d(TAG, "Eventos encontrados a pesar de errores: ${todosLosEventos.size}")
                        todosLosEventos.sortBy { it.hora }
                        _eventosDelDia.value = todosLosEventos
                        _cargando.value = false
                    }
                }
        }
    }

    private fun obtenerInicioDelDia(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun obtenerFinDelDia(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel destruido, limpiando listeners")
        listenerInscripciones?.remove()
    }
}