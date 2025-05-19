package com.unacar.calendarioacademico.ui.estudiantes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unacar.calendarioacademico.databinding.FragmentGestionarEstudiantesBinding

class GestionarEstudiantesFragment : Fragment() {

    private var _binding: FragmentGestionarEstudiantesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionarEstudiantesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}