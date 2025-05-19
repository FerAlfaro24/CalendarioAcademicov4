package com.unacar.calendarioacademico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.unacar.calendarioacademico.autenticacion.IniciarSesionActivity
import com.unacar.calendarioacademico.databinding.ActivityMainBinding
import com.unacar.calendarioacademico.utilidades.AdministradorFirebase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private var tipoUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Verificar que el usuario esté autenticado
        val usuario = AdministradorFirebase.obtenerUsuarioActual()
        if (usuario == null) {
            // Si no hay usuario autenticado, redirigir a login
            startActivity(Intent(this, IniciarSesionActivity::class.java))
            finish()
            return
        }

        // Configurar el botón flotante según el tipo de usuario
        binding.appBarMain.fab.setOnClickListener { view ->
            if (tipoUsuario == "profesor") {
                // Si es profesor, puede crear eventos
                Snackbar.make(view, "Crear nuevo evento", Snackbar.LENGTH_LONG)
                    .setAction("Crear") {
                        // Aquí iría la navegación a la pantalla de creación de eventos
                        // Por ahora mostramos un mensaje
                        Toast.makeText(this, "Funcionalidad de crear evento pendiente", Toast.LENGTH_SHORT).show()
                    }.show()
            } else {
                // Si es estudiante, puede filtrar su calendario
                Snackbar.make(view, "Filtrar calendario", Snackbar.LENGTH_LONG)
                    .setAction("Filtrar") {
                        // Aquí iría el diálogo de filtros
                        // Por ahora mostramos un mensaje
                        Toast.makeText(this, "Funcionalidad de filtrado pendiente", Toast.LENGTH_SHORT).show()
                    }.show()
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configurar la navegación y el menú lateral
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_calendario, R.id.nav_materias, R.id.nav_notificaciones, R.id.nav_perfil
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Personalizar información del usuario en el menú
        if (usuario != null) {
            Log.d(TAG, "Obteniendo datos de usuario con UID: ${usuario.uid}")
            AdministradorFirebase.obtenerPerfilUsuario(usuario.uid).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        tipoUsuario = documento.getString("tipoUsuario")
                        val headerView = navView.getHeaderView(0)
                        val tvNombre = headerView.findViewById<TextView>(R.id.nav_header_title)
                        val tvEmail = headerView.findViewById<TextView>(R.id.nav_header_subtitle)

                        // Actualizar información del encabezado
                        tvNombre.text = documento.getString("nombre") ?: "Usuario"
                        tvEmail.text = usuario.email

                        // Configurar menú según tipo de usuario
                        val menu = navView.menu
                        if (tipoUsuario == "profesor") {
                            Log.d(TAG, "Usuario es profesor")
                            // Mostrar opciones específicas para profesores
                            menu.findItem(R.id.nav_crear_materia)?.isVisible = true
                            menu.findItem(R.id.nav_gestionar_estudiantes)?.isVisible = true

                            // Mostrar el botón flotante para profesores
                            binding.appBarMain.fab.visibility = View.VISIBLE
                        } else {
                            Log.d(TAG, "Usuario es estudiante")
                            // Ocultar opciones específicas para profesores
                            menu.findItem(R.id.nav_crear_materia)?.isVisible = false
                            menu.findItem(R.id.nav_gestionar_estudiantes)?.isVisible = false

                            // Ocular botón flotante para estudiantes o cambiarlo a filtro
                            binding.appBarMain.fab.setImageResource(android.R.drawable.ic_menu_search)
                        }
                    } else {
                        Log.w(TAG, "No se encontró el documento del usuario")
                        // Si no hay perfil, redirigir a login
                        startActivity(Intent(this, IniciarSesionActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener perfil de usuario", e)
                    Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cerrar_sesion -> {
                // Cerrar sesión y volver a la pantalla de login
                AdministradorFirebase.cerrarSesion()
                startActivity(Intent(this, IniciarSesionActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}