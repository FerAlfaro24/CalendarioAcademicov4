package com.unacar.calendarioacademico.ui.materias

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.databinding.FragmentDetalleMateriaBinding
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.ui.estudiantes.EstudianteAdapter

class DetalleMateriaFragment : Fragment() {

    private var _binding: FragmentDetalleMateriaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DetalleMateriaViewModel
    private val TAG = "DetalleMateriaFragment"
    private val args: DetalleMateriaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleMateriaBinding.inflate(inflater, container, false)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(DetalleMateriaViewModel::class.java)

        // Cargar datos
        viewModel.cargarMateria(args.materiaId)

        // Configurar RecyclerView
        binding.rvEstudiantes.layoutManager = LinearLayoutManager(context)

        // Configurar observadores
        viewModel.materia.observe(viewLifecycleOwner) { materia ->
            if (materia != null) {
                binding.tvNombreMateria.text = materia.nombre
                binding.tvSemestre.text = "Semestre: ${materia.semestre}"
                binding.tvDescripcion.text = materia.descripcion
            }
        }

        viewModel.profesor.observe(viewLifecycleOwner) { profesor ->
            if (profesor != null) {
                binding.tvProfesor.text = "Profesor: ${profesor.nombre}"
            }
        }

        viewModel.estudiantes.observe(viewLifecycleOwner) { estudiantes ->
            if (estudiantes.isEmpty()) {
                binding.tvNoEstudiantes.visibility = View.VISIBLE
                binding.rvEstudiantes.visibility = View.GONE
            } else {
                binding.tvNoEstudiantes.visibility = View.GONE
                binding.rvEstudiantes.visibility = View.VISIBLE

                binding.rvEstudiantes.adapter = EstudianteAdapter(
                    estudiantes,
                    viewModel.esProfesor.value == true
                ) { estudiante ->
                    eliminarEstudiante(estudiante)
                }
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error: $error")
            }
        }

        viewModel.esProfesor.observe(viewLifecycleOwner) { esProfesor ->
            // Configurar botón según tipo de usuario
            if (esProfesor) {
                binding.btnAccion.text = getString(R.string.agregar_estudiantes)
                binding.btnAccion.setOnClickListener {
                    // Navegar a pantalla de agregar estudiantes
                    val action = DetalleMateriaFragmentDirections.actionDetalleMateriaToAgregarEstudiantes(args.materiaId)
                    findNavController().navigate(action)
                }
            } else {
                binding.btnAccion.text = getString(R.string.ver_eventos)
                binding.btnAccion.setOnClickListener {
                    // Navegar a pantalla de eventos de la materia
                    val action = DetalleMateriaFragmentDirections.actionDetalleMateriaToEventosMateria(args.materiaId)
                    findNavController().navigate(action)
                }
            }
        }

        return binding.root
    }

    private fun eliminarEstudiante(estudiante: Usuario) {
        // Mostrar diálogo de confirmación
        // Por ahora, solo eliminamos directamente
        viewModel.eliminarEstudiante(estudiante.id, args.materiaId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}