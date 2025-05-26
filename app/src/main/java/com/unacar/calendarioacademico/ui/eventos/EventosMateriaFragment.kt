package com.unacar.calendarioacademico.ui.eventos

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
import com.unacar.calendarioacademico.databinding.FragmentEventosMateriaBinding

class EventosMateriaFragment : Fragment() {

    private var _binding: FragmentEventosMateriaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventosMateriaViewModel
    private val args: EventosMateriaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosMateriaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(EventosMateriaViewModel::class.java)

        // Configurar RecyclerView
        binding.rvEventos.layoutManager = LinearLayoutManager(context)

        // Cargar datos
        viewModel.cargarEventosMateria(args.materiaId)

        // Configurar observadores
        viewModel.materia.observe(viewLifecycleOwner) { materia ->
            binding.tvNombreMateria.text = materia?.nombre ?: "Materia"
        }

        viewModel.eventos.observe(viewLifecycleOwner) { eventos ->
            if (eventos.isEmpty()) {
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
            } else {
                binding.tvNoEventos.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE

                binding.rvEventos.adapter = EventoAdapter(eventos) { evento ->
                    // Navegar al detalle del evento
                    val action = EventosMateriaFragmentDirections
                        .actionEventosMateriaToDetalleEvento(evento.id)
                    findNavController().navigate(action)
                }
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

        viewModel.esProfesor.observe(viewLifecycleOwner) { esProfesor ->
            binding.fabCrearEvento.visibility = if (esProfesor) View.VISIBLE else View.GONE
        }

        // Configurar FAB
        binding.fabCrearEvento.setOnClickListener {
            val action = EventosMateriaFragmentDirections
                .actionEventosMateriaToCrearEvento(args.materiaId)
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}