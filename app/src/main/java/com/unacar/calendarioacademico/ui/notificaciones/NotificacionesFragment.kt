package com.unacar.calendarioacademico.ui.notificaciones

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
import com.unacar.calendarioacademico.databinding.FragmentNotificacionesBinding

class NotificacionesFragment : Fragment() {

    private var _binding: FragmentNotificacionesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificacionesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacionesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[NotificacionesViewModel::class.java]

        // Configurar RecyclerView
        binding.rvNotificaciones.layoutManager = LinearLayoutManager(context)

        // Configurar observadores
        viewModel.notificaciones.observe(viewLifecycleOwner) { notificaciones ->
            if (notificaciones.isEmpty()) {
                binding.tvNoNotificaciones.visibility = View.VISIBLE
                binding.rvNotificaciones.visibility = View.GONE
            } else {
                binding.tvNoNotificaciones.visibility = View.GONE
                binding.rvNotificaciones.visibility = View.VISIBLE

                binding.rvNotificaciones.adapter = NotificacionAdapter(
                    notificaciones,
                    onNotificacionClick = { notificacion ->
                        // Marcar como leída si no está leída
                        if (!notificacion.leida) {
                            viewModel.marcarComoLeida(notificacion.id)
                        }

                        // Navegar al evento relacionado si existe
                        if (notificacion.idEvento.isNotEmpty()) {
                            try {
                                val bundle = Bundle()
                                bundle.putString("eventoId", notificacion.idEvento)
                                findNavController().navigate(
                                    com.unacar.calendarioacademico.R.id.action_notificaciones_to_detalle_evento,
                                    bundle
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al navegar al evento", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Notificación sin evento asociado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onEliminarNotificacion = { notificacion ->
                        mostrarDialogoEliminar(notificacion)
                    }
                )
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

        viewModel.notificacionEliminada.observe(viewLifecycleOwner) { eliminada ->
            if (eliminada) {
                Toast.makeText(context, "Notificación eliminada", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun mostrarDialogoEliminar(notificacion: com.unacar.calendarioacademico.modelos.Notificacion) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar notificación")
            .setMessage("¿Estás seguro de que deseas eliminar esta notificación?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarNotificacion(notificacion.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}