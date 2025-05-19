package com.unacar.calendarioacademico.ui.materias

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class EditarMateriaViewModel : ViewModel() {

    private val _materia = MutableLiveData<Materia>()
    val materia: LiveData<Materia> = _materia

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _actualizacionExitosa = MutableLiveData<Boolean>()
    val actualizacionExitosa: LiveData<Boolean> = _actualizacionExitosa

    fun cargarMateria(idMateria: String) {
        _cargando.value = true

        AdministradorFirebase.obtenerMateria(idMateria).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val materia = documento.toObject(Materia::class.java)
                    if (materia != null) {
                        materia.id = documento.id
                        _materia.value = materia
                    } else {
                        _error.value = "Error al obtener datos de la materia"
                    }
                } else {
                    _error.value = "La materia no existe"
                }
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al cargar materia: ${it.message}"
                _cargando.value = false
            }
    }

    fun actualizarMateria(idMateria: String, nombre: String, descripcion: String, semestre: String) {
        _cargando.value = true

        val datos = mapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "semestre" to semestre,
            "fechaActualizacion" to System.currentTimeMillis()
        )

        AdministradorFirebase.actualizarMateria(idMateria, datos)
            .addOnSuccessListener {
                _actualizacionExitosa.value = true
                _cargando.value = false
            }
            .addOnFailureListener {
                _error.value = "Error al actualizar materia: ${it.message}"
                _cargando.value = false
            }
    }
}