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
import com.unacar.calendarioacademico.databinding.FragmentDetalleEventoBinding
import java.text.SimpleDateFormat
import java.util.*

class DetalleEventoFragment : Fragment() {

    private var _binding: FragmentDetalleEventoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DetalleEventoViewModel
    private val args: DetalleEventoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleEventoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(DetalleEventoViewModel::class.java)

        // Cargar datos del evento
        viewModel.cargarDetalleEvento(args.eventoId)

        configurarObservadores()

        return binding.root
    }

    private fun configurarObservadores() {
        viewModel.evento.observe(viewLifecycleOwner) { evento ->
            if (evento != null) {
                binding.tvTipoEvento.text = evento.getTipoFormateado().uppercase()
                binding.tvTituloEvento.text = evento.titulo
                binding.tvDescripcionEvento.text = evento.descripcion

                // Formatear fecha
                val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                binding.tvFechaEvento.text = dateFormat.format(Date(evento.fecha))
                binding.tvHoraEvento.text = evento.hora

                // Color del tipo según el evento
                val colorTipo = when (evento.tipo) {
                    "examen" -> requireContext().getColor(android.R.color.holo_red_dark)
                    "tarea" -> requireContext().getColor(android.R.color.holo_blue_dark)
                    "proyecto" -> requireContext().getColor(android.R.color.holo_orange_dark)
                    else -> requireContext().getColor(android.R.color.darker_gray)
                }
                binding.tvTipoEvento.setBackgroundColor(colorTipo)
            }
        }

        viewModel.materia.observe(viewLifecycleOwner) { materia ->
            if (materia != null) {
                binding.tvNombreMateria.text = materia.nombre
            }
        }

        // Observar si es profesor para mostrar botón de editar
        viewModel.esProfesor.observe(viewLifecycleOwner) { esProfesor ->
            if (esProfesor) {
                binding.btnEditarEvento.visibility = View.VISIBLE
                binding.btnEditarEvento.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("eventoId", args.eventoId)
                    findNavController().navigate(com.unacar.calendarioacademico.R.id.nav_editar_evento, bundle)
                }
            } else {
                binding.btnEditarEvento.visibility = View.GONE
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
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}