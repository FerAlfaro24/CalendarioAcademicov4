package com.unacar.calendarioacademico.ui.eventos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.unacar.calendarioacademico.R
import com.unacar.calendarioacademico.databinding.FragmentEditarEventoBinding
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase
import java.text.SimpleDateFormat
import java.util.*

class EditarEventoFragment : Fragment() {

    private var _binding: FragmentEditarEventoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditarEventoViewModel
    private val args: EditarEventoFragmentArgs by navArgs()

    private var fechaSeleccionada: Long = 0
    private var horaSeleccionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarEventoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(EditarEventoViewModel::class.java)

        configurarSpinnerTipo()
        configurarDatePicker()
        configurarTimePicker()
        configurarObservadores()

        // Cargar datos del evento
        viewModel.cargarEvento(args.eventoId)

        binding.btnGuardarCambios.setOnClickListener {
            actualizarEvento()
        }

        return binding.root
    }

    private fun configurarSpinnerTipo() {
        val tipos = arrayOf("Tarea", "Examen", "Proyecto")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = adapter
    }

    private fun configurarDatePicker() {
        binding.btnSeleccionarFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (fechaSeleccionada != 0L) {
                calendar.timeInMillis = fechaSeleccionada
            }

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    fechaSeleccionada = selectedCalendar.timeInMillis

                    val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                    binding.tvFechaSeleccionada.text = dateFormat.format(Date(fechaSeleccionada))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun configurarTimePicker() {
        binding.btnSeleccionarHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    horaSeleccionada = String.format("%02d:%02d", hourOfDay, minute)
                    binding.tvHoraSeleccionada.text = horaSeleccionada
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }

    private fun configurarObservadores() {
        viewModel.evento.observe(viewLifecycleOwner) { evento ->
            if (evento != null) {
                // Llenar campos con datos actuales
                binding.etTitulo.setText(evento.titulo)
                binding.etDescripcion.setText(evento.descripcion)

                // Configurar spinner de tipo
                val tipos = arrayOf("tarea", "examen", "proyecto")
                val posicion = tipos.indexOf(evento.tipo.lowercase())
                if (posicion >= 0) {
                    binding.spinnerTipo.setSelection(posicion)
                }

                // Configurar fecha y hora
                fechaSeleccionada = evento.fecha
                horaSeleccionada = evento.hora

                val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                binding.tvFechaSeleccionada.text = dateFormat.format(Date(evento.fecha))
                binding.tvHoraSeleccionada.text = evento.hora
            }
        }

        viewModel.materia.observe(viewLifecycleOwner) { materia ->
            if (materia != null) {
                binding.tvMateria.text = "Materia: ${materia.nombre}"
            }
        }

        viewModel.eventoActualizado.observe(viewLifecycleOwner) { actualizado ->
            if (actualizado) {
                Toast.makeText(context, "Evento actualizado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
            binding.btnGuardarCambios.isEnabled = !cargando
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarEvento() {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val tipoSeleccionado = binding.spinnerTipo.selectedItem.toString().lowercase()

        // Validaciones
        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "El título es obligatorio"
            return
        } else {
            binding.tilTitulo.error = null
        }

        if (descripcion.isEmpty()) {
            binding.tilDescripcion.error = "La descripción es obligatoria"
            return
        } else {
            binding.tilDescripcion.error = null
        }

        if (fechaSeleccionada == 0L) {
            Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(context, "Selecciona una hora", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar evento
        viewModel.actualizarEvento(
            args.eventoId,
            titulo,
            descripcion,
            fechaSeleccionada,
            horaSeleccionada,
            tipoSeleccionado
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}