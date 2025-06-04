package com.unacar.calendarioacademico.ui.perfil

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.unacar.calendarioacademico.databinding.FragmentPerfilBinding
import com.unacar.calendarioacademico.ui.home.MateriaSimpleAdapter

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var perfilViewModel: PerfilViewModel
    private val TAG = "PerfilFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        perfilViewModel = ViewModelProvider(this).get(PerfilViewModel::class.java)

        Log.d(TAG, "PerfilFragment iniciado")

        // Configurar RecyclerView para materias
        binding.rvMateriasPerfil.layoutManager = LinearLayoutManager(context)

        // Observar datos del usuario
        perfilViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
            if (usuario != null) {
                Log.d(TAG, "Usuario cargado: ${usuario.nombre}")
                binding.tvNombrePerfil.text = usuario.nombre
                binding.tvCorreoPerfil.text = usuario.correo
                binding.tvTipoUsuarioPerfil.text = if (usuario.tipoUsuario == "profesor") "Profesor" else "Estudiante"

                // Mostrar la inicial del nombre en el avatar
                val inicial = if (usuario.nombre.isNotEmpty()) {
                    usuario.nombre.first().uppercaseChar().toString()
                } else {
                    "U"
                }
                binding.tvAvatarPerfil.text = inicial

                // Mostrar fecha de registro
                val fechaRegistro = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(usuario.fechaCreacion))
                binding.tvFechaRegistroPerfil.text = "Miembro desde: $fechaRegistro"
            }
        }

        // Observar tipo de usuario para mostrar información específica
        perfilViewModel.tipoUsuario.observe(viewLifecycleOwner) { tipo ->
            Log.d(TAG, "Tipo de usuario: $tipo")
            if (tipo == "profesor") {
                binding.tvTituloMateriasPerfil.text = "Materias que imparto"
                binding.tvEstadisticasPerfil.text = "Rol: Profesor"
            } else {
                binding.tvTituloMateriasPerfil.text = "Materias inscritas"
                binding.tvEstadisticasPerfil.text = "Rol: Estudiante"
            }
        }

        // Observar materias
        perfilViewModel.materias.observe(viewLifecycleOwner) { materias ->
            Log.d(TAG, "Materias cargadas: ${materias.size}")
            if (materias.isEmpty()) {
                binding.tvNoMateriasPerfil.visibility = View.VISIBLE
                binding.rvMateriasPerfil.visibility = View.GONE

                val tipoUsuario = perfilViewModel.tipoUsuario.value
                binding.tvNoMateriasPerfil.text = if (tipoUsuario == "profesor") {
                    "No has creado ninguna materia aún"
                } else {
                    "No estás inscrito en ninguna materia"
                }
            } else {
                binding.tvNoMateriasPerfil.visibility = View.GONE
                binding.rvMateriasPerfil.visibility = View.VISIBLE

                // Usar el mismo adapter que en Home para consistencia
                binding.rvMateriasPerfil.adapter = MateriaSimpleAdapter(materias) { materia ->
                    // No hacer nada en el perfil, solo mostrar información
                    Toast.makeText(context, "Materia: ${materia.nombre}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observar estadísticas
        perfilViewModel.estadisticas.observe(viewLifecycleOwner) { stats ->
            Log.d(TAG, "Estadísticas cargadas: $stats")
            val tipoUsuario = perfilViewModel.tipoUsuario.value
            if (tipoUsuario == "profesor") {
                binding.tvContadorMaterias.text = "Materias creadas: ${stats["materias"] ?: 0}"
                binding.tvContadorEventos.text = "Eventos creados: ${stats["eventos"] ?: 0}"
                binding.tvContadorEstudiantes.text = "Estudiantes totales: ${stats["estudiantes"] ?: 0}"
            } else {
                binding.tvContadorMaterias.text = "Materias inscritas: ${stats["materias"] ?: 0}"
                binding.tvContadorEventos.text = "Próximos eventos: ${stats["eventos"] ?: 0}"
                binding.tvContadorEstudiantes.text = "Notificaciones: ${stats["notificaciones"] ?: 0}"
            }
        }

        perfilViewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            Log.d(TAG, "Estado de carga: $cargando")
            binding.progressBarPerfil.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        perfilViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Log.e(TAG, "Error: $error")
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "PerfilFragment destruido")
        _binding = null
    }
}