package com.unacar.calendarioacademico

import android.app.AlertDialog
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
import com.unacar.calendarioacademico.modelos.Materia
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
                // Para profesores: mostrar selector de materias para crear evento
                mostrarSelectorMaterias(view)
            } else {
                // Si es estudiante, puede filtrar su calendario
                Snackbar.make(view, "Filtrar calendario", Snackbar.LENGTH_LONG)
                    .setAction("Filtrar") {
                        Toast.makeText(this, "Funcionalidad de filtrado pendiente", Toast.LENGTH_SHORT).show()
                    }.show()
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

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
                        configurarMenuSegunUsuario(navView, tipoUsuario ?: "estudiante")

                        // Configurar la navegación después de cargar el tipo de usuario
                        configurarNavegacion(navController, drawerLayout, navView)
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

    private fun mostrarSelectorMaterias(view: View) {
        val usuario = AdministradorFirebase.obtenerUsuarioActual()
        if (usuario != null) {
            AdministradorFirebase.obtenerMateriasPorProfesor(usuario.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        Snackbar.make(view, "Primero debes crear una materia", Snackbar.LENGTH_LONG)
                            .setAction("Crear") {
                                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_crear_materia)
                            }.show()
                    } else {
                        // Crear lista de materias
                        val materias = mutableListOf<Materia>()
                        val nombresMaterias = mutableListOf<String>()

                        for (documento in querySnapshot.documents) {
                            val materia = documento.toObject(Materia::class.java)
                            if (materia != null) {
                                materia.id = documento.id
                                materias.add(materia)
                                nombresMaterias.add(materia.nombre)
                            }
                        }

                        // Mostrar diálogo de selección
                        AlertDialog.Builder(this)
                            .setTitle("Selecciona la materia")
                            .setItems(nombresMaterias.toTypedArray()) { _, which ->
                                val materiaSeleccionada = materias[which]
                                val bundle = Bundle()
                                bundle.putString("materiaId", materiaSeleccionada.id)
                                findNavController(R.id.nav_host_fragment_content_main)
                                    .navigate(R.id.nav_crear_evento, bundle)
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar materias", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun configurarMenuSegunUsuario(navView: NavigationView, tipoUsuario: String) {
        val menu = navView.menu

        if (tipoUsuario == "profesor") {
            Log.d(TAG, "Configurando menú para profesor")
            // Para profesores: quitar notificaciones, agregar eventos
            menu.findItem(R.id.nav_notificaciones)?.isVisible = false
            menu.findItem(R.id.nav_eventos)?.isVisible = true

            // Mostrar opciones específicas para profesores
            menu.findItem(R.id.nav_crear_materia)?.isVisible = true
            menu.findItem(R.id.nav_gestionar_estudiantes)?.isVisible = true

            // Mostrar el botón flotante para profesores
            binding.appBarMain.fab.visibility = View.VISIBLE
            // Cambiar icono del FAB para profesores
            binding.appBarMain.fab.setImageResource(android.R.drawable.ic_input_add)
        } else {
            Log.d(TAG, "Configurando menú para estudiante")
            // Para estudiantes: mantener notificaciones, quitar eventos
            menu.findItem(R.id.nav_notificaciones)?.isVisible = true
            menu.findItem(R.id.nav_eventos)?.isVisible = false

            // Ocultar opciones específicas para profesores
            menu.findItem(R.id.nav_crear_materia)?.isVisible = false
            menu.findItem(R.id.nav_gestionar_estudiantes)?.isVisible = false

            // Cambiar icono del botón flotante para estudiantes (filtro)
            binding.appBarMain.fab.setImageResource(android.R.drawable.ic_menu_search)
        }
    }

    private fun configurarNavegacion(navController: androidx.navigation.NavController, drawerLayout: DrawerLayout, navView: NavigationView) {
        // Configurar la navegación y el menú lateral
        val destinationsSet = if (tipoUsuario == "profesor") {
            setOf(R.id.nav_home, R.id.nav_calendario, R.id.nav_materias, R.id.nav_eventos, R.id.nav_perfil)
        } else {
            setOf(R.id.nav_home, R.id.nav_calendario, R.id.nav_materias, R.id.nav_notificaciones, R.id.nav_perfil)
        }

        appBarConfiguration = AppBarConfiguration(destinationsSet, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Manejar clicks en el menú
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_eventos -> {
                    // Navegar a todos los eventos del profesor
                    navController.navigate(R.id.nav_todos_eventos)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    // Permitir navegación normal para otros elementos
                    try {
                        navController.navigate(menuItem.itemId)
                        drawerLayout.closeDrawers()
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al navegar: ${e.message}")
                        false
                    }
                }
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