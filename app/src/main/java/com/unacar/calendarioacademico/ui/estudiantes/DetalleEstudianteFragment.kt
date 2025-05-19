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
import com.unacar.calendarioacademico.databinding.FragmentDetalleEstudianteBinding
import com.unacar.calendarioacademico.modelos.Materia

class DetalleEstudianteFragment : Fragment() {

    private var _binding: FragmentDetalleEstudianteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DetalleEstudianteViewModel
    private val args: DetalleEstudianteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEstudianteBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(DetalleEstudianteViewModel::class.java)

        // Configurar RecyclerView
        binding.rvMateriasInscrito.layoutManager = LinearLayoutManager(context)

        // Cargar datos del estudiante
        viewModel.cargarEstudiante(args.estudianteId)

        // Configurar botón de inscribir
        binding.btnInscribirMateria.setOnClickListener {
            val action = DetalleEstudianteFragmentDirections
                .actionDetalleEstudianteToSeleccionarMateria(args.estudianteId)
            findNavController().navigate(action)
        }

        // Observar datos del estudiante
        viewModel.estudiante.observe(viewLifecycleOwner) { estudiante ->
            binding.tvNombreEstudiante.text = estudiante.nombre
            binding.tvCorreoEstudiante.text = estudiante.correo
        }

        // Observar materias
        viewModel.materias.observe(viewLifecycleOwner) { materias ->
            if (materias.isEmpty()) {
                binding.tvNoMaterias.text = "El estudiante no está inscrito en ninguna de tus materias"
                binding.tvNoMaterias.visibility = View.VISIBLE
                binding.rvMateriasInscrito.visibility = View.GONE
            } else {
                binding.tvNoMaterias.visibility = View.GONE
                binding.rvMateriasInscrito.visibility = View.VISIBLE

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

        viewModel.accionExitosa.observe(viewLifecycleOwner) { exitosa ->
            if (exitosa) {
                Toast.makeText(context, "Estudiante desinscrito correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun actualizarListaMaterias(materias: List<Materia>, nombresProfesor: Map<String, String>) {
        binding.rvMateriasInscrito.adapter = MateriaInscripcionAdapter(
            materias,
            nombresProfesor
        ) { materia ->
            // Desinscribir estudiante de la materia
            viewModel.desinscribirDeMateria(args.estudianteId, materia.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}