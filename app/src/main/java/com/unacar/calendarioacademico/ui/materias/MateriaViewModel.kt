package com.unacar.calendarioacademico.ui.materias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class MateriaViewModel : ViewModel() {

    private val _materias = MutableLiveData<List<Materia>>()
    val materias: LiveData<List<Materia>> = _materias

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _tipoUsuario = MutableLiveData<String>()
    val tipoUsuario: LiveData<String> = _tipoUsuario

    init {
        cargarTipoUsuario()
    }

    private fun cargarTipoUsuario() {
        _cargando.value = true
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {
            AdministradorFirebase.obtenerPerfilUsuario(usuario.uid).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val tipo = documento.getString("tipoUsuario") ?: "estudiante"
                        _tipoUsuario.value = tipo

                        // Cargar materias según tipo de usuario
                        if (tipo == "profesor") {
                            cargarMateriasPorProfesor(usuario.uid)
                        } else {
                            cargarMateriasEstudiante(usuario.uid)
                        }
                    } else {
                        _error.value = "No se encontró el perfil del usuario"
                        _cargando.value = false
                    }
                }
                .addOnFailureListener {
                    _error.value = "Error al cargar perfil: ${it.message}"
                    _cargando.value = false
                }
        } else {
            _error.value = "Usuario no autenticado"
            _cargando.value = false
        }
    }

    private fun cargarMateriasPorProfesor(idProfesor: String) {
        AdministradorFirebase.obtenerMateriasPorProfesor(idProfesor)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val listaMaterias = mutableListOf<Materia>()

                for (documento in querySnapshot.documents) {
                    val materia = documento.toObject(Materia::class.java)
                    if (materia != null) {
                        // Asignar el ID del documento al objeto
                        materia.id = documento.id
                        listaMaterias.add(materia)
                    }
                }

                _materias.value = listaMaterias
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al cargar materias: ${it.message}"
                _cargando.value = false
            }
    }

    private fun cargarMateriasEstudiante(idEstudiante: String) {
        // Primero obtenemos las inscripciones del estudiante
        AdministradorFirebase.obtenerMateriasEstudiante(idEstudiante)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val idsMaterias = mutableListOf<String>()

                // Extraer IDs de las materias en las que está inscrito
                for (documento in querySnapshot.documents) {
                    val idMateria = documento.getString("idMateria")
                    if (idMateria != null) {
                        idsMaterias.add(idMateria)
                    }
                }

                if (idsMaterias.isEmpty()) {
                    _materias.value = emptyList()
                    _cargando.value = false
                    return@addOnSuccessListener
                }

                // Ahora obtener los detalles de cada materia
                val db = FirebaseFirestore.getInstance()
                val listaMaterias = mutableListOf<Materia>()
                var materiasCompletadas = 0

                for (idMateria in idsMaterias) {
                    db.collection("materias").document(idMateria)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            materiasCompletadas++

                            val materia = documentSnapshot.toObject(Materia::class.java)
                            if (materia != null) {
                                // Asignar el ID del documento al objeto
                                materia.id = documentSnapshot.id
                                listaMaterias.add(materia)
                            }

                            // Si ya procesamos todas las materias, actualizamos el LiveData
                            if (materiasCompletadas == idsMaterias.size) {
                                _materias.value = listaMaterias
                                _cargando.value = false
                            }
                        }
                        .addOnFailureListener {
                            materiasCompletadas++

                            if (materiasCompletadas == idsMaterias.size) {
                                _materias.value = listaMaterias
                                _cargando.value = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                _error.value = "Error al cargar inscripciones: ${it.message}"
                _cargando.value = false
            }
    }
}