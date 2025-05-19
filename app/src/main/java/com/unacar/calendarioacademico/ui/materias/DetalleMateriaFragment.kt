package com.unacar.calendarioacademico.ui.materias

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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

                // Mostrar botón de opciones
                binding.tvOpciones.visibility = View.VISIBLE
                binding.tvOpciones.setOnClickListener {
                    mostrarMenuOpciones(it)
                }
            } else {
                binding.btnAccion.text = getString(R.string.ver_eventos)
                binding.btnAccion.setOnClickListener {
                    // Navegar a pantalla de eventos de la materia
                    val action = DetalleMateriaFragmentDirections.actionDetalleMateriaToEventosMateria(args.materiaId)
                    findNavController().navigate(action)
                }

                // Ocultar botón de opciones para estudiantes
                binding.tvOpciones.visibility = View.GONE
            }
        }

        // Observar eliminación exitosa
        viewModel.eliminacionExitosa.observe(viewLifecycleOwner) { exitosa ->
            if (exitosa) {
                Toast.makeText(context, "Materia eliminada correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    private fun eliminarEstudiante(estudiante: Usuario) {
        // Mostrar diálogo de confirmación
        // Por ahora, solo eliminamos directamente
        viewModel.eliminarEstudiante(estudiante.id, args.materiaId)
    }

    private fun mostrarMenuOpciones(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_materia, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_editar_materia -> {
                    val action = DetalleMateriaFragmentDirections.actionDetalleMateriaToEditarMateria(args.materiaId)
                    findNavController().navigate(action)
                    true
                }
                R.id.action_eliminar_materia -> {
                    mostrarDialogoConfirmacion()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar materia")
            .setMessage("¿Estás seguro de que deseas eliminar esta materia? Esta acción no se puede deshacer y se eliminarán todas las inscripciones de estudiantes.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarMateria(args.materiaId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}