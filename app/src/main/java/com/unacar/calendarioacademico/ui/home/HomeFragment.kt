package com.unacar.calendarioacademico.ui.home

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
import com.unacar.calendarioacademico.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Log.d(TAG, "HomeFragment creado")

        // Configurar RecyclerViews
        binding.rvProximosEventos.layoutManager = LinearLayoutManager(context)
        binding.rvMaterias.layoutManager = LinearLayoutManager(context)

        // Observar datos del usuario
        homeViewModel.nombre.observe(viewLifecycleOwner) { nombre ->
            Log.d(TAG, "Nombre observado: $nombre")
            binding.tvNombre.text = nombre
        }

        homeViewModel.tipoUsuario.observe(viewLifecycleOwner) { tipo ->
            Log.d(TAG, "Tipo de usuario observado: $tipo")
            binding.tvTipoUsuario.text = "Tipo: ${if (tipo == "profesor") "Profesor" else "Estudiante"}"

            // Configurar eventos según tipo de usuario
            if (tipo == "estudiante") {
                Log.d(TAG, "Usuario es estudiante, cargando próximos eventos")
                // Para estudiantes: cargar próximos eventos
                homeViewModel.cargarProximosEventos()
            } else {
                Log.d(TAG, "Usuario es profesor, ocultando próximos eventos")
                // Para profesores: no mostrar próximos eventos
                binding.cardProximosEventos.visibility = View.GONE
            }
        }

        // Observar materias
        homeViewModel.materias.observe(viewLifecycleOwner) { materias ->
            Log.d(TAG, "Materias observadas: ${materias.size}")
            if (materias.isEmpty()) {
                binding.tvNoMaterias.visibility = View.VISIBLE
                binding.rvMaterias.visibility = View.GONE
            } else {
                binding.tvNoMaterias.visibility = View.GONE
                binding.rvMaterias.visibility = View.VISIBLE

                binding.rvMaterias.adapter = MateriaSimpleAdapter(materias) { materia ->
                    // Navegar al detalle de la materia
                    val bundle = Bundle()
                    bundle.putString("materiaId", materia.id)
                    findNavController().navigate(R.id.action_home_to_materia_detalle, bundle)
                }
            }
        }

        // Observar próximos eventos (solo para estudiantes)
        homeViewModel.proximosEventos.observe(viewLifecycleOwner) { eventos ->
            Log.d(TAG, "Próximos eventos observados: ${eventos.size}")

            if (eventos.isEmpty()) {
                Log.d(TAG, "No hay próximos eventos")
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvProximosEventos.visibility = View.GONE
            } else {
                Log.d(TAG, "Mostrando ${eventos.size} próximos eventos")
                binding.tvNoEventos.visibility = View.GONE
                binding.rvProximosEventos.visibility = View.VISIBLE

                // Usar EventoHomeAdapter para mostrar los eventos compactos
                binding.rvProximosEventos.adapter = EventoHomeAdapter(eventos) { evento ->
                    Log.d(TAG, "Click en evento: ${evento.titulo}")
                    // Navegar al detalle del evento
                    val bundle = Bundle()
                    bundle.putString("eventoId", evento.id)
                    findNavController().navigate(R.id.action_home_to_detalle_evento, bundle)
                }
            }
        }

        homeViewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            Log.d(TAG, "Estado de carga: $cargando")
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        homeViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e(TAG, "Error observado: $error")
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HomeFragment destruido")
        _binding = null
    }
}