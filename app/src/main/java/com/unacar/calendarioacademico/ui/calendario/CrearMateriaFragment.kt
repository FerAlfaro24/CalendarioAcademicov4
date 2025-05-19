package com.unacar.calendarioacademico.ui.materias

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.databinding.FragmentCrearMateriaBinding
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class CrearMateriaFragment : Fragment() {

    private var _binding: FragmentCrearMateriaBinding? = null
    private val binding get() = _binding!!
    private val TAG = "CrearMateriaFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearMateriaBinding.inflate(inflater, container, false)

        // Configurar botón de creación
        binding.btnCrearMateria.setOnClickListener {
            crearMateria()
        }

        return binding.root
    }

    private fun crearMateria() {
        // Validar campos
        val nombre = binding.etNombre.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val semestre = binding.etSemestre.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            return
        } else {
            binding.tilNombre.error = null
        }

        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = "La descripción es obligatoria"
            return
        } else {
            binding.tilDescripcion.error = null
        }

        if (semestre.isEmpty()) {
            binding.tilSemestre.error = "El semestre es obligatorio"
            return
        } else {
            binding.tilSemestre.error = null
        }

        // Mostrar progreso
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCrearMateria.isEnabled = false

        // Obtener ID del profesor actual
        val usuarioActual = AdministradorFirebase.obtenerUsuarioActual()
        if (usuarioActual == null) {
            binding.progressBar.visibility = View.GONE
            binding.btnCrearMateria.isEnabled = true
            Toast.makeText(context, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        val idProfesor = usuarioActual.uid

        // Crear objeto materia
        val materia = Materia(
            nombre = nombre,
            descripcion = descripcion,
            idProfesor = idProfesor,
            semestre = semestre
        )

        // Guardar en Firestore
        AdministradorFirebase.crearMateria(materia)
            .addOnSuccessListener { documentReference ->
                binding.progressBar.visibility = View.GONE
                binding.btnCrearMateria.isEnabled = true

                Log.d(TAG, "Materia creada con ID: ${documentReference.id}")
                Toast.makeText(context, "Materia creada con éxito", Toast.LENGTH_SHORT).show()

                // Navegamos de vuelta a la lista de materias
                findNavController().navigate(R.id.nav_materias)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnCrearMateria.isEnabled = true

                Log.e(TAG, "Error al crear materia", e)
                Toast.makeText(context, "Error al crear materia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}