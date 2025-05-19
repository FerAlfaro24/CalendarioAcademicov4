package com.unacar.calendarioacademico.ui.materias

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.unacar.calendarioacademico.databinding.FragmentEditarMateriaBinding

class EditarMateriaFragment : Fragment() {

    private var _binding: FragmentEditarMateriaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditarMateriaViewModel
    private val args: EditarMateriaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarMateriaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(EditarMateriaViewModel::class.java)

        // Cargar datos de la materia
        viewModel.cargarMateria(args.materiaId)

        // Configurar observadores
        viewModel.materia.observe(viewLifecycleOwner) { materia ->
            binding.etNombre.setText(materia.nombre)
            binding.etDescripcion.setText(materia.descripcion)
            binding.etSemestre.setText(materia.semestre)
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
            binding.btnGuardarCambios.isEnabled = !cargando
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null && error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.actualizacionExitosa.observe(viewLifecycleOwner) { exitosa ->
            if (exitosa) {
                Toast.makeText(context, "Materia actualizada correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // Configurar botón para guardar cambios
        binding.btnGuardarCambios.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val descripcion = binding.etDescripcion.text.toString().trim()
            val semestre = binding.etSemestre.text.toString().trim()

            if (nombre.isEmpty()) {
                binding.tilNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            } else {
                binding.tilNombre.error = null
            }

            if (descripcion.isEmpty()) {
                binding.tilDescripcion.error = "La descripción es obligatoria"
                return@setOnClickListener
            } else {
                binding.tilDescripcion.error = null
            }

            if (semestre.isEmpty()) {
                binding.tilSemestre.error = "El semestre es obligatorio"
                return@setOnClickListener
            } else {
                binding.tilSemestre.error = null
            }

            viewModel.actualizarMateria(args.materiaId, nombre, descripcion, semestre)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}