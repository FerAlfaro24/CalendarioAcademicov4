package com.unacar.calendarioacademico.ui.notificaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

                binding.rvNotificaciones.adapter = NotificacionAdapter(notificaciones) { notificacion ->
                    // Marcar como leída si no está leída
                    if (!notificacion.leida) {
                        viewModel.marcarComoLeida(notificacion.id)
                    }
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

        // Configurar botón para marcar todas como leídas
        binding.btnMarcarTodasLeidas.setOnClickListener {
            viewModel.marcarTodasComoLeidas()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}