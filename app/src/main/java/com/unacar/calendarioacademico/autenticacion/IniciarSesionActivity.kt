package com.unacar.calendarioacademico.autenticacion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unacar.calendarioacademico.MainActivity
import com.unacar.calendarioacademico.databinding.ActivityIniciarSesionBinding
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIniciarSesionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Comprobar si ya hay sesión iniciada
        if (AdministradorFirebase.obtenerUsuarioActual() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        configurarBotonIniciarSesion()
        configurarEnlaceRegistro()
    }

    private fun configurarBotonIniciarSesion() {
        binding.btnIniciarSesion.setOnClickListener {
            val correo = binding.etCorreo.text.toString().trim()
            val contrasena = binding.etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar progreso
            binding.barraProgreso.visibility = View.VISIBLE

            // Autenticar usuario
            AdministradorFirebase.iniciarSesion(correo, contrasena)
                .addOnCompleteListener { task ->
                    binding.barraProgreso.visibility = View.GONE

                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun configurarEnlaceRegistro() {
        binding.tvRegistrar.setOnClickListener {
            startActivity(Intent(this, RegistrarActivity::class.java))
        }
    }
}