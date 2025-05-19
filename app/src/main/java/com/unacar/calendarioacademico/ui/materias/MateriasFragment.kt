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
import androidx.recyclerview.widget.LinearLayoutManager
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.databinding.FragmentMateriasBinding
import com.unacar.calendarioacademico.modelos.Materia

class MateriasFragment : Fragment() {

    private var _binding: FragmentMateriasBinding? = null
    private val binding get() = _binding!!
    private lateinit var materiaViewModel: MateriaViewModel
    private val TAG = "MateriasFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        materiaViewModel = ViewModelProvider(this).get(MateriaViewModel::class.java)

        _binding = FragmentMateriasBinding.inflate(inflater, container, false)

        // Configurar RecyclerView
        binding.rvMaterias.layoutManager = LinearLayoutManager(context)

        // Configurar observadores
        materiaViewModel.materias.observe(viewLifecycleOwner) { materias ->
            if (materias.isEmpty()) {
                binding.tvNoMaterias.visibility = View.VISIBLE
                binding.rvMaterias.visibility = View.GONE
            } else {
                binding.tvNoMaterias.visibility = View.GONE
                binding.rvMaterias.visibility = View.VISIBLE

                // Determinar si es estudiante
                val esEstudiante = materiaViewModel.tipoUsuario.value != "profesor"

                // Configurar adaptador
                binding.rvMaterias.adapter = MateriaAdapter(materias, esEstudiante) { materia ->
                    // Manejar clic en materia
                    onMateriaClick(materia)
                }
            }
        }

        materiaViewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        materiaViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error: $error")
            }
        }

        materiaViewModel.tipoUsuario.observe(viewLifecycleOwner) { tipoUsuario ->
            // Mostrar FAB solo para profesores
            binding.fabCrearMateria.visibility = if (tipoUsuario == "profesor") View.VISIBLE else View.GONE
        }

        // Configurar FAB para crear materia
        binding.fabCrearMateria.setOnClickListener {
            findNavController().navigate(R.id.nav_crear_materia)
        }

        return binding.root
    }

    private fun onMateriaClick(materia: Materia) {
        // Navegar al detalle de la materia usando un Bundle para pasar el ID
        val bundle = Bundle()
        bundle.putString("materiaId", materia.id)
        findNavController().navigate(R.id.nav_materia_detalle, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}