package com.unacar.calendarioacademico.ui.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.unacar.calendarioacademico.R

class DetalleEventoFragment : Fragment() {

    private val args: DetalleEventoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Por ahora, solo inflamos un layout básico
        val view = inflater.inflate(R.layout.fragment_detalle_evento, container, false)

        // TODO: Implementar la lógica del detalle del evento
        Toast.makeText(context, "Detalle del evento: ${args.eventoId}", Toast.LENGTH_SHORT).show()

        return view
    }
}