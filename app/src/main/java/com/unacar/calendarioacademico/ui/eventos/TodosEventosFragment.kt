package com.unacar.calendarioacademico.ui.eventos

import android.app.AlertDialog
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
import com.unacar.calendarioacademico.databinding.FragmentTodosEventosBinding
import com.unacar.calendarioacademico.modelos.Evento

class TodosEventosFragment : Fragment() {

    private var _binding: FragmentTodosEventosBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TodosEventosViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosEventosBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(TodosEventosViewModel::class.java)

        // Configurar RecyclerView
        binding.rvEventos.layoutManager = LinearLayoutManager(context)

        // Observar eventos
        viewModel.eventos.observe(viewLifecycleOwner) { eventos ->
            if (eventos.isEmpty()) {
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvEventos.visibility = View.GONE
            } else {
                binding.tvNoEventos.visibility = View.GONE
                binding.rvEventos.visibility = View.VISIBLE

                // Esperar a que los nombres de materias estén cargados
                val nombresMateria = viewModel.nombresMateria.value ?: emptyMap()
                actualizarListaEventos(eventos, nombresMateria)
            }
        }

        viewModel.nombresMateria.observe(viewLifecycleOwner) { nombresMateria ->
            val eventos = viewModel.eventos.value
            if (eventos != null && eventos.isNotEmpty()) {
                actualizarListaEventos(eventos, nombresMateria)
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

        viewModel.eventoEliminado.observe(viewLifecycleOwner) { eliminado ->
            if (eliminado) {
                Toast.makeText(context, "Evento eliminado correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun actualizarListaEventos(eventos: List<Evento>, nombresMateria: Map<String, String>) {
        binding.rvEventos.adapter = EventoProfesorAdapter(
            eventos,
            nombresMateria,
            onEventoClick = { evento ->
                // Navegar al detalle del evento
                val bundle = Bundle()
                bundle.putString("eventoId", evento.id)
                findNavController().navigate(R.id.action_todos_eventos_to_detalle_evento, bundle)
            },
            onEditarEvento = { evento ->
                // Navegar a editar evento
                val bundle = Bundle()
                bundle.putString("eventoId", evento.id)
                findNavController().navigate(R.id.nav_editar_evento, bundle)
            },
            onEliminarEvento = { evento ->
                mostrarDialogoEliminar(evento)
            }
        )
    }

    private fun mostrarDialogoEliminar(evento: Evento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar evento")
            .setMessage("¿Estás seguro de que deseas eliminar el evento '${evento.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarEvento(evento.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}