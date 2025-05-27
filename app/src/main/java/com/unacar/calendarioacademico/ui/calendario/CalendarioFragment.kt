package com.unacar.calendarioacademico.ui.calendario

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
import com.unacar.calendarioacademico.databinding.FragmentCalendarioBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarioFragment : Fragment() {

    private var _binding: FragmentCalendarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CalendarioViewModel
    private val TAG = "CalendarioFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CalendarioViewModel::class.java)

        Log.d(TAG, "=== CALENDARIO FRAGMENT INICIADO ===")

        configurarCalendar()
        configurarRecyclerView()
        configurarObservadores()

        return binding.root
    }

    private fun configurarCalendar() {
        val calendar = Calendar.getInstance()
        binding.calendarView.date = calendar.timeInMillis

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        Log.d(TAG, "Calendario configurado para fecha actual: ${formatoFecha.format(calendar.time)}")

        // Configurar listener para cuando se selecciona una fecha
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val fechaSeleccionada = Calendar.getInstance()
            fechaSeleccionada.set(year, month, dayOfMonth)

            Log.d(TAG, "=== FECHA SELECCIONADA EN CALENDARIO ===")
            Log.d(TAG, "Fecha: $dayOfMonth/${month + 1}/$year")
            Log.d(TAG, "Timestamp: ${fechaSeleccionada.timeInMillis}")

            // Cargar eventos para la fecha seleccionada
            viewModel.cargarEventosPorFecha(fechaSeleccionada.timeInMillis)

            // Actualizar el texto de la fecha seleccionada
            val formatoTexto = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
            val textoFecha = formatoTexto.format(fechaSeleccionada.time)
            binding.tvFechaSeleccionada.text = textoFecha
            Log.d(TAG, "Texto fecha actualizado: $textoFecha")
        }

        // Mostrar la fecha actual por defecto
        val formatoTexto = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
        binding.tvFechaSeleccionada.text = formatoTexto.format(calendar.time)

        Log.d(TAG, "Cargando eventos para hoy por defecto...")
        // Cargar eventos para hoy
        viewModel.cargarEventosPorFecha(calendar.timeInMillis)
    }

    private fun configurarRecyclerView() {
        binding.rvEventosDelDia.layoutManager = LinearLayoutManager(context)
        Log.d(TAG, "RecyclerView configurado")
    }

    private fun configurarObservadores() {
        Log.d(TAG, "Configurando observadores...")

        viewModel.eventosDelDia.observe(viewLifecycleOwner) { eventos ->
            Log.d(TAG, "=== EVENTOS OBSERVADOS ===")
            Log.d(TAG, "Cantidad de eventos recibidos: ${eventos.size}")

            eventos.forEachIndexed { index, evento ->
                Log.d(TAG, "Evento ${index + 1}:")
                Log.d(TAG, "  - Título: ${evento.titulo}")
                Log.d(TAG, "  - Hora: ${evento.hora}")
                Log.d(TAG, "  - Tipo: ${evento.tipo}")
                Log.d(TAG, "  - ID: ${evento.id}")
            }

            if (eventos.isEmpty()) {
                Log.d(TAG, "No hay eventos, mostrando mensaje de 'sin eventos'")
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvEventosDelDia.visibility = View.GONE
            } else {
                Log.d(TAG, "Hay eventos, configurando RecyclerView")
                binding.tvNoEventos.visibility = View.GONE
                binding.rvEventosDelDia.visibility = View.VISIBLE

                val adapter = EventoCalendarioAdapter(eventos) { evento ->
                    Log.d(TAG, "Click en evento: ${evento.titulo} (ID: ${evento.id})")
                    try {
                        val bundle = Bundle()
                        bundle.putString("eventoId", evento.id)
                        findNavController().navigate(R.id.action_calendario_to_detalle_evento, bundle)
                        Log.d(TAG, "Navegación exitosa al detalle del evento")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al navegar al detalle del evento", e)
                        Toast.makeText(context, "Error al abrir el evento", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.rvEventosDelDia.adapter = adapter
                Log.d(TAG, "Adapter configurado con ${eventos.size} eventos")
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            Log.d(TAG, "Estado de carga cambiado: $cargando")
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e(TAG, "Error observado: $error")
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.tipoUsuario.observe(viewLifecycleOwner) { tipoUsuario ->
            Log.d(TAG, "Tipo de usuario observado: $tipoUsuario")
            if (tipoUsuario != "estudiante") {
                Log.d(TAG, "Usuario no es estudiante, ocultando calendario")
                binding.tvNoEventos.text = "El calendario de eventos está disponible solo para estudiantes"
                binding.tvNoEventos.visibility = View.VISIBLE
                binding.rvEventosDelDia.visibility = View.GONE
                binding.calendarView.visibility = View.GONE
                binding.tvFechaSeleccionada.visibility = View.GONE
                binding.divider.visibility = View.GONE
            } else {
                Log.d(TAG, "Usuario es estudiante, mostrando calendario completo")
                binding.calendarView.visibility = View.VISIBLE
                binding.tvFechaSeleccionada.visibility = View.VISIBLE
                binding.divider.visibility = View.VISIBLE
            }
        }

        Log.d(TAG, "Todos los observadores configurados")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "CalendarioFragment destruido")
        _binding = null
    }
}