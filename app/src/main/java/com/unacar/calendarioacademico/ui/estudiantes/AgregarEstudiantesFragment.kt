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
import com.unacar.calendarioacademico.databinding.FragmentAgregarEstudiantesBinding

class AgregarEstudiantesFragment : Fragment() {

    private var _binding: FragmentAgregarEstudiantesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AgregarEstudiantesViewModel
    private val args: AgregarEstudiantesFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgregarEstudiantesBinding.inflate(inflater, container, false)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(AgregarEstudiantesViewModel::class.java)
        viewModel.setMateriaId(args.materiaId)

        // Configurar RecyclerView
        binding.rvEstudiantes.layoutManager = LinearLayoutManager(context)

        // Configurar botón de búsqueda
        binding.btnBuscar.setOnClickListener {
            val correo = binding.etBuscar.text.toString().trim()
            if (correo.isNotEmpty()) {
                viewModel.buscarEstudiantePorCorreo(correo)
            } else {
                Toast.makeText(context, "Ingresa un correo para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar observadores
        viewModel.estudiantes.observe(viewLifecycleOwner) { estudiantes ->
            if (estudiantes.isEmpty()) {
                binding.tvNoEstudiantes.visibility = View.VISIBLE
                binding.rvEstudiantes.visibility = View.GONE
            } else {
                binding.tvNoEstudiantes.visibility = View.GONE
                binding.rvEstudiantes.visibility = View.VISIBLE

                binding.rvEstudiantes.adapter = EstudianteAgregarAdapter(estudiantes) { estudiante ->
                    viewModel.agregarEstudianteAMateria(estudiante.id)
                }
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.estudianteAgregado.observe(viewLifecycleOwner) { agregado ->
            if (agregado) {
                Toast.makeText(context, "Estudiante agregado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}