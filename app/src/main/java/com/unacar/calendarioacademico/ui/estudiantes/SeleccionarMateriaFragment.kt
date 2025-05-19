package com.unacar.calendarioacademico.ui.estudiantes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.unacar.calendarioacademico.databinding.FragmentSeleccionarMateriaBinding
import com.unacar.calendarioacademico.modelos.Materia

class SeleccionarMateriaFragment : Fragment() {

    private var _binding: FragmentSeleccionarMateriaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SeleccionarMateriaViewModel
    private val args: SeleccionarMateriaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeleccionarMateriaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SeleccionarMateriaViewModel::class.java)

        // Configurar RecyclerView
        binding.rvMaterias.layoutManager = LinearLayoutManager(context)

        // Cargar datos del estudiante y materias disponibles
        viewModel.cargarEstudiante(args.estudianteId)

        // Observar datos del estudiante
        viewModel.estudiante.observe(viewLifecycleOwner) { estudiante ->
            binding.tvNombreEstudiante.text = "Inscribir a: ${estudiante.nombre}"
        }

        // Observar materias disponibles
        viewModel.materias.observe(viewLifecycleOwner) { materias ->
            if (materias.isEmpty()) {
                binding.tvNoMaterias.text = "No tienes materias disponibles para inscribir a este estudiante"
                binding.tvNoMaterias.visibility = View.VISIBLE
                binding.rvMaterias.visibility = View.GONE
            } else {
                binding.tvNoMaterias.visibility = View.GONE
                binding.rvMaterias.visibility = View.VISIBLE

                // Esperar a que los nombres de profesores estén cargados
                val nombresProfesor = viewModel.nombresProfesor.value
                if (nombresProfesor != null) {
                    actualizarListaMaterias(materias, nombresProfesor)
                }
            }
        }

        viewModel.nombresProfesor.observe(viewLifecycleOwner) { nombresProfesor ->
            val materias = viewModel.materias.value
            if (materias != null && materias.isNotEmpty()) {
                actualizarListaMaterias(materias, nombresProfesor)
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

        viewModel.inscripcionExitosa.observe(viewLifecycleOwner) { exitosa ->
            if (exitosa) {
                Toast.makeText(context, "Estudiante inscrito correctamente", Toast.LENGTH_SHORT).show()

                // Si ya no hay materias disponibles, regresar a la pantalla anterior
                if (viewModel.materias.value?.isEmpty() == true) {
                    findNavController().navigateUp()
                }
            }
        }

        return binding.root
    }

    private fun actualizarListaMaterias(materias: List<Materia>, nombresProfesor: Map<String, String>) {
        binding.rvMaterias.adapter = MateriaSeleccionAdapter(
            materias,
            nombresProfesor
        ) { materia ->
            // Inscribir estudiante en la materia
            viewModel.inscribirEnMateria(args.estudianteId, materia.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}