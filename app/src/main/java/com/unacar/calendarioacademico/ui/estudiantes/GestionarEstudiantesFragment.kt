package com.unacar.calendarioacademico.ui.estudiantes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.databinding.FragmentGestionarEstudiantesBinding
import com.unacar.calendarioacademico.modelos.Usuario

class GestionarEstudiantesFragment : Fragment() {

    private var _binding: FragmentGestionarEstudiantesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GestionarEstudiantesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionarEstudiantesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(GestionarEstudiantesViewModel::class.java)

        // Configurar RecyclerView
        binding.rvEstudiantes.layoutManager = LinearLayoutManager(context)

        // Configurar botón de búsqueda
        binding.btnBuscar.setOnClickListener {
            val correo = binding.etBuscarEstudiante.text.toString().trim()
            viewModel.buscarEstudiantePorCorreo(correo)
        }

        // Observar datos de estudiantes
        viewModel.estudiantes.observe(viewLifecycleOwner) { estudiantes ->
            if (estudiantes.isEmpty()) {
                binding.tvNoEstudiantes.visibility = View.VISIBLE
                binding.rvEstudiantes.visibility = View.GONE
            } else {
                binding.tvNoEstudiantes.visibility = View.GONE
                binding.rvEstudiantes.visibility = View.VISIBLE

                // Esperar a que los contadores de materias estén cargados
                val contadores = viewModel.contadoresMaterias.value
                if (contadores != null) {
                    actualizarListaEstudiantes(estudiantes, contadores)
                }
            }
        }

        viewModel.contadoresMaterias.observe(viewLifecycleOwner) { contadores ->
            val estudiantes = viewModel.estudiantes.value
            if (estudiantes != null && estudiantes.isNotEmpty()) {
                actualizarListaEstudiantes(estudiantes, contadores)
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null && error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun actualizarListaEstudiantes(estudiantes: List<Usuario>, contadores: Map<String, Int>) {
        binding.rvEstudiantes.adapter = EstudianteGestionAdapter(
            estudiantes,
            contadores
        ) { estudiante ->
            // Navegar a la vista de detalle del estudiante
            val action = GestionarEstudiantesFragmentDirections
                .actionGestionarEstudiantesToDetalleEstudiante(estudiante.id)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}