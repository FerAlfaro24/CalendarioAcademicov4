package com.unacar.calendarioacademico.utilidades

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.unacar.calendarioacademico.modelos.Usuario
import com.unacar.calendarioacademico.modelos.Materia
import com.unacar.calendarioacademico.modelos.Evento
import com.unacar.calendarioacademico.modelos.Notificacion

object AdministradorFirebase {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Referencias a colecciones
    private val coleccionUsuarios = db.collection("usuarios")
    private val coleccionMaterias = db.collection("materias")
    private val coleccionEventos = db.collection("eventos")
    private val coleccionNotificaciones = db.collection("notificaciones")
    private val coleccionInscripciones = db.collection("inscripciones")

    // Autenticación
    fun registrarUsuario(correo: String, contrasena: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(correo, contrasena)
    }

    fun iniciarSesion(correo: String, contrasena: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(correo, contrasena)
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    // Operaciones de usuarios
    fun crearPerfilUsuario(usuario: Usuario): Task<Void> {
        return coleccionUsuarios.document(usuario.id).set(usuario)
    }

    fun obtenerPerfilUsuario(idUsuario: String): DocumentReference {
        return coleccionUsuarios.document(idUsuario)
    }

    // Función para crear un documento de prueba (verificar conexión)
    fun probarConexion(callback: (Boolean, String) -> Unit) {
        val coleccionPrueba = db.collection("prueba")
        val documentoPrueba = HashMap<String, Any>()
        documentoPrueba["prueba"] = "Conexión exitosa"

        coleccionPrueba.add(documentoPrueba)
            .addOnSuccessListener {
                callback(true, "Conexión con Firebase establecida correctamente")
            }
            .addOnFailureListener { e ->
                callback(false, "Error al conectar con Firebase: ${e.message}")
            }
    }

    // ---- FUNCIONES PARA MATERIAS ----

    // Crear una nueva materia
    fun crearMateria(materia: Materia): Task<DocumentReference> {
        return coleccionMaterias.add(materia)
    }

    // Obtener materias de un profesor
    fun obtenerMateriasPorProfesor(idProfesor: String): Query {
        return coleccionMaterias.whereEqualTo("idProfesor", idProfesor)
    }

    // Escuchar materias de un profesor en tiempo real
    fun escucharMateriasPorProfesor(idProfesor: String, listener: (List<Materia>) -> Unit): ListenerRegistration {
        return coleccionMaterias
            .whereEqualTo("idProfesor", idProfesor)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listaMaterias = mutableListOf<Materia>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val materia = documento.toObject(Materia::class.java)
                        if (materia != null) {
                            materia.id = documento.id
                            listaMaterias.add(materia)
                        }
                    }
                }

                listener(listaMaterias)
            }
    }

    // Obtener detalles de una materia
    fun obtenerMateria(idMateria: String): DocumentReference {
        return coleccionMaterias.document(idMateria)
    }

    // Actualizar una materia
    fun actualizarMateria(idMateria: String, datos: Map<String, Any>): Task<Void> {
        return coleccionMaterias.document(idMateria).update(datos)
    }

    // Eliminar una materia
    fun eliminarMateria(idMateria: String): Task<Void> {
        return coleccionMaterias.document(idMateria).delete()
    }

    // Eliminar todas las inscripciones de una materia
    fun eliminarInscripcionesMateria(idMateria: String): Task<QuerySnapshot> {
        return coleccionInscripciones
            .whereEqualTo("idMateria", idMateria)
            .get()
    }

    // ---- FUNCIONES PARA EVENTOS ----

    // Crear un nuevo evento
    fun crearEvento(evento: Evento): Task<DocumentReference> {
        return coleccionEventos.add(evento)
    }

    // Obtener eventos de una materia
    fun obtenerEventosPorMateria(idMateria: String): Query {
        return coleccionEventos.whereEqualTo("idMateria", idMateria)
    }

    // Actualizar un evento
    fun actualizarEvento(idEvento: String, datos: Map<String, Any>): Task<Void> {
        return coleccionEventos.document(idEvento).update(datos)
    }

    // Eliminar un evento
    fun eliminarEvento(idEvento: String): Task<Void> {
        return coleccionEventos.document(idEvento).delete()
    }

    // ---- FUNCIONES PARA INSCRIPCIONES ----

    // Inscribir un estudiante a una materia
    fun inscribirEstudiante(idEstudiante: String, idMateria: String): Task<Void> {
        val inscripcion = hashMapOf(
            "idEstudiante" to idEstudiante,
            "idMateria" to idMateria,
            "fechaInscripcion" to FieldValue.serverTimestamp()
        )
        return coleccionInscripciones.document("$idEstudiante-$idMateria").set(inscripcion)
    }

    // Obtener las materias en las que está inscrito un estudiante
    fun obtenerMateriasEstudiante(idEstudiante: String): Query {
        return coleccionInscripciones.whereEqualTo("idEstudiante", idEstudiante)
    }

    // Escuchar las inscripciones de un estudiante en tiempo real
    fun escucharMateriasEstudiante(idEstudiante: String, listener: (List<String>) -> Unit): ListenerRegistration {
        return coleccionInscripciones
            .whereEqualTo("idEstudiante", idEstudiante)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val idsMaterias = mutableListOf<String>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val idMateria = documento.getString("idMateria")
                        if (idMateria != null) {
                            idsMaterias.add(idMateria)
                        }
                    }
                }

                listener(idsMaterias)
            }
    }

    // Obtener estudiantes inscritos en una materia
    fun obtenerEstudiantesMateria(idMateria: String): Query {
        return coleccionInscripciones.whereEqualTo("idMateria", idMateria)
    }

    // Escuchar los estudiantes inscritos en una materia en tiempo real
    fun escucharEstudiantesMateria(idMateria: String, listener: (List<String>) -> Unit): ListenerRegistration {
        return coleccionInscripciones
            .whereEqualTo("idMateria", idMateria)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val idsEstudiantes = mutableListOf<String>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val idEstudiante = documento.getString("idEstudiante")
                        if (idEstudiante != null) {
                            idsEstudiantes.add(idEstudiante)
                        }
                    }
                }

                listener(idsEstudiantes)
            }
    }

    // ---- FUNCIONES PARA NOTIFICACIONES ----

    // Crear una notificación
    fun crearNotificacion(notificacion: Notificacion): Task<DocumentReference> {
        return coleccionNotificaciones.add(notificacion)
    }

    // Obtener notificaciones de un usuario
    fun obtenerNotificacionesUsuario(idUsuario: String): Query {
        return coleccionNotificaciones.whereEqualTo("idUsuario", idUsuario)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
    }

    // Marcar notificación como leída
    fun marcarNotificacionLeida(idNotificacion: String): Task<Void> {
        return coleccionNotificaciones.document(idNotificacion)
            .update("estado", "leida")
    }

    // Eliminar inscripción de un estudiante en una materia
    fun eliminarInscripcion(idEstudiante: String, idMateria: String): Task<Void> {
        return coleccionInscripciones.document("$idEstudiante-$idMateria").delete()
    }

    // Obtener todos los estudiantes
    fun obtenerTodosLosEstudiantes(): Query {
        return coleccionUsuarios
            .whereEqualTo("tipoUsuario", "estudiante")
    }

    // Escuchar todos los estudiantes en tiempo real
    fun escucharTodosLosEstudiantes(listener: (List<Usuario>) -> Unit): ListenerRegistration {
        return coleccionUsuarios
            .whereEqualTo("tipoUsuario", "estudiante")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listaEstudiantes = mutableListOf<Usuario>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val estudiante = documento.toObject(Usuario::class.java)
                        if (estudiante != null) {
                            estudiante.id = documento.id
                            listaEstudiantes.add(estudiante)
                        }
                    }
                }

                listener(listaEstudiantes)
            }
    }

    // Buscar estudiante por correo
    fun buscarEstudiantePorCorreo(correo: String): Query {
        return coleccionUsuarios
            .whereEqualTo("tipoUsuario", "estudiante")
            .whereEqualTo("correo", correo)
    }
}