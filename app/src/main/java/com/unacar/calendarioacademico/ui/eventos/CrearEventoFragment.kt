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
import com.unacar.calendarioacademico.databinding.FragmentCrearEventoBinding
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase
import java.text.SimpleDateFormat
import java.util.*

class CrearEventoFragment : Fragment() {

    private var _binding: FragmentCrearEventoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CrearEventoViewModel
    private val args: CrearEventoFragmentArgs by navArgs()

    private var fechaSeleccionada: Long = 0
    private var horaSeleccionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearEventoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CrearEventoViewModel::class.java)

        configurarSpinnerTipo()
        configurarDatePicker()
        configurarTimePicker()
        configurarObservadores()
        cargarNombreMateria()

        binding.btnCrearEvento.setOnClickListener {
            crearEvento()
        }

        return binding.root
    }

    private fun cargarNombreMateria() {
        // Cargar el nombre de la materia para mostrarlo al usuario
        AdministradorFirebase.obtenerMateria(args.materiaId).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val nombreMateria = documento.getString("nombre") ?: "Materia desconocida"
                    binding.tvMateria.text = "Materia: $nombreMateria"
                }
            }
            .addOnFailureListener {
                binding.tvMateria.text = "Materia: Error al cargar"
            }
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
        viewModel.eventoCreado.observe(viewLifecycleOwner) { creado ->
            if (creado) {
                Toast.makeText(context, "Evento creado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
            binding.btnCrearEvento.isEnabled = !cargando
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearEvento() {
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

        // Crear evento
        viewModel.crearEvento(
            titulo = titulo,
            descripcion = descripcion,
            fecha = fechaSeleccionada,
            hora = horaSeleccionada,
            tipo = tipoSeleccionado,
            idMateria = args.materiaId
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}