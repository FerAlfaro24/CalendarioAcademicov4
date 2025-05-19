package com.unacar.calendarioacademico.ui.home

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
import com.unacar.calendarioacademico.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar RecyclerView de materias
        binding.rvMaterias.layoutManager = LinearLayoutManager(context)

        // Configurar observadores para LiveData
        homeViewModel.nombre.observe(viewLifecycleOwner) { nombre ->
            binding.tvNombre.text = nombre
        }

        homeViewModel.tipoUsuario.observe(viewLifecycleOwner) { tipoUsuario ->
            binding.tvTipoUsuario.text = "Tipo: " +
                    if (tipoUsuario == "profesor") "Profesor" else "Estudiante"
        }

        homeViewModel.materias.observe(viewLifecycleOwner) { materias ->
            if (materias.isEmpty()) {
                binding.tvNoMaterias.visibility = View.VISIBLE
                binding.rvMaterias.visibility = View.GONE
            } else {
                binding.tvNoMaterias.visibility = View.GONE
                binding.rvMaterias.visibility = View.VISIBLE

                binding.rvMaterias.adapter = MateriaSimpleAdapter(materias) { materia ->
                    // Navegar a la vista de detalle de la materia
                    val bundle = Bundle()
                    bundle.putString("materiaId", materia.id)
                    findNavController().navigate(R.id.nav_materia_detalle, bundle)
                }
            }
        }

        homeViewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        homeViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}