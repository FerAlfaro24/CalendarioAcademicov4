package com.unacar.calendarioacademico.autenticacion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.unacar.calendarioacademico.MainActivity
import com.unacar.calendarioacademico.databinding.ActivityRegistrarBinding
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class RegistrarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarBinding
    private val TAG = "RegistrarActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotonRegistrar()
        configurarEnlaceIniciarSesion()
    }

    private fun configurarBotonRegistrar() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()
            val confirmarContrasena = binding.etConfirmarContrasena.text.toString().trim()
            val tipoUsuario = if (binding.rbEstudiante.isChecked) "estudiante" else "profesor"

            // Validaciones
            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena != confirmarContrasena) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar progreso
            binding.barraProgreso.visibility = View.VISIBLE

            // Registrar usuario en Firebase Auth
            Log.d(TAG, "Intentando registrar usuario: $correo")
            AdministradorFirebase.registrarUsuario(correo, contrasena)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Usuario registrado con éxito en Auth")
                        // Obtener el ID del usuario recién creado
                        val userId = task.result?.user?.uid ?: ""

                        // Crear perfil de usuario en Firestore
                        val usuario = Usuario(
                            id = userId,
                            nombre = nombre,
                            correo = correo,
                            tipoUsuario = tipoUsuario
                        )

                        Log.d(TAG, "Guardando perfil en Firestore: $usuario")
                        // Guardar en Firestore
                        AdministradorFirebase.crearPerfilUsuario(usuario)
                            .addOnSuccessListener {
                                Log.d(TAG, "Perfil guardado con éxito")
                                binding.barraProgreso.visibility = View.GONE
                                Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al guardar perfil: ${e.message}", e)
                                binding.barraProgreso.visibility = View.GONE
                                Toast.makeText(this, "Error al crear perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e(TAG, "Error al registrar usuario: ${task.exception?.message}", task.exception)
                        binding.barraProgreso.visibility = View.GONE
                        Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun configurarEnlaceIniciarSesion() {
        binding.tvIniciarSesion.setOnClickListener {
            finish() // Regresar a IniciarSesionActivity
        }
    }
}