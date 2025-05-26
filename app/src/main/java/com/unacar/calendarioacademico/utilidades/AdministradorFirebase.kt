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

    // ---- FUNCIONES DE AUTENTICACIÓN ----

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

    // ---- FUNCIONES DE USUARIOS ----

    fun crearPerfilUsuario(usuario: Usuario): Task<Void> {
        return coleccionUsuarios.document(usuario.id).set(usuario)
    }

    fun obtenerPerfilUsuario(idUsuario: String): DocumentReference {
        return coleccionUsuarios.document(idUsuario)
    }

    // ---- FUNCIONES PARA MATERIAS ----

    fun crearMateria(materia: Materia): Task<DocumentReference> {
        return coleccionMaterias.add(materia)
    }

    fun obtenerMateriasPorProfesor(idProfesor: String): Query {
        return coleccionMaterias.whereEqualTo("idProfesor", idProfesor)
    }

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

    fun obtenerMateria(idMateria: String): DocumentReference {
        return coleccionMaterias.document(idMateria)
    }

    fun actualizarMateria(idMateria: String, datos: Map<String, Any>): Task<Void> {
        return coleccionMaterias.document(idMateria).update(datos)
    }

    fun eliminarMateria(idMateria: String): Task<Void> {
        return coleccionMaterias.document(idMateria).delete()
    }

    fun eliminarInscripcionesMateria(idMateria: String): Task<QuerySnapshot> {
        return coleccionInscripciones
            .whereEqualTo("idMateria", idMateria)
            .get()
    }

    // ---- FUNCIONES PARA EVENTOS ----

    fun crearEvento(evento: Evento): Task<DocumentReference> {
        return coleccionEventos.add(evento)
    }

    fun obtenerEventosPorMateria(idMateria: String): Query {
        return coleccionEventos.whereEqualTo("idMateria", idMateria)
    }

    fun escucharEventosPorMateria(idMateria: String, listener: (List<Evento>) -> Unit): ListenerRegistration {
        return coleccionEventos
            .whereEqualTo("idMateria", idMateria)
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listaEventos = mutableListOf<Evento>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val evento = documento.toObject(Evento::class.java)
                        if (evento != null) {
                            evento.id = documento.id
                            listaEventos.add(evento)
                        }
                    }
                }

                listener(listaEventos)
            }
    }

    fun obtenerProximosEventos(idsMateriasInscritas: List<String>, limite: Int = 4): Query {
        val fechaActual = System.currentTimeMillis()
        return coleccionEventos
            .whereIn("idMateria", idsMateriasInscritas)
            .whereGreaterThanOrEqualTo("fecha", fechaActual)
            .orderBy("fecha", Query.Direction.ASCENDING)
            .limit(limite.toLong())
    }

    fun actualizarEvento(idEvento: String, datos: Map<String, Any>): Task<Void> {
        return coleccionEventos.document(idEvento).update(datos)
    }

    fun eliminarEvento(idEvento: String): Task<Void> {
        return coleccionEventos.document(idEvento).delete()
    }

    fun obtenerEvento(idEvento: String): DocumentReference {
        return coleccionEventos.document(idEvento)
    }

    // ---- FUNCIONES PARA INSCRIPCIONES ----

    fun inscribirEstudiante(idEstudiante: String, idMateria: String): Task<Void> {
        val inscripcion = hashMapOf(
            "idEstudiante" to idEstudiante,
            "idMateria" to idMateria,
            "fechaInscripcion" to FieldValue.serverTimestamp()
        )
        return coleccionInscripciones.document("$idEstudiante-$idMateria").set(inscripcion)
    }

    fun obtenerMateriasEstudiante(idEstudiante: String): Query {
        return coleccionInscripciones.whereEqualTo("idEstudiante", idEstudiante)
    }

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

    fun obtenerEstudiantesMateria(idMateria: String): Query {
        return coleccionInscripciones.whereEqualTo("idMateria", idMateria)
    }

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

    fun eliminarInscripcion(idEstudiante: String, idMateria: String): Task<Void> {
        return coleccionInscripciones.document("$idEstudiante-$idMateria").delete()
    }

    // ---- FUNCIONES PARA NOTIFICACIONES ----

    fun crearNotificacion(notificacion: Notificacion): Task<DocumentReference> {
        return coleccionNotificaciones.add(notificacion)
    }

    fun obtenerNotificacionesUsuario(idUsuario: String): Query {
        return coleccionNotificaciones.whereEqualTo("idUsuario", idUsuario)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
    }

    fun escucharNotificacionesUsuario(idUsuario: String, listener: (List<Notificacion>) -> Unit): ListenerRegistration {
        return coleccionNotificaciones
            .whereEqualTo("idUsuario", idUsuario)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val listaNotificaciones = mutableListOf<Notificacion>()
                if (querySnapshot != null) {
                    for (documento in querySnapshot.documents) {
                        val notificacion = documento.toObject(Notificacion::class.java)
                        if (notificacion != null) {
                            // Crear una nueva instancia con el ID del documento
                            val notificacionConId = notificacion.copy(id = documento.id)
                            listaNotificaciones.add(notificacionConId)
                        }
                    }
                }

                listener(listaNotificaciones)
            }
    }

    fun marcarNotificacionLeida(idNotificacion: String): Task<Void> {
        return coleccionNotificaciones.document(idNotificacion)
            .update(mapOf(
                "estado" to "leida",
                "leida" to true
            ))
    }

    fun crearNotificacionParaMateria(idMateria: String, titulo: String, mensaje: String, tipo: String, idEvento: String = "") {
        // Obtener estudiantes inscritos en la materia
        coleccionInscripciones
            .whereEqualTo("idMateria", idMateria)
            .get()
            .addOnSuccessListener { inscripciones ->
                for (documento in inscripciones.documents) {
                    val idEstudiante = documento.getString("idEstudiante")
                    if (idEstudiante != null) {
                        val notificacion = Notificacion(
                            id = "",
                            mensaje = mensaje,
                            titulo = titulo,
                            tipo = tipo,
                            idUsuario = idEstudiante,
                            idEvento = idEvento,
                            estado = "no_leida",
                            leida = false,
                            fechaCreacion = System.currentTimeMillis()
                        )

                        crearNotificacion(notificacion)
                    }
                }
            }
    }

    // ---- FUNCIONES PARA GESTIÓN DE ESTUDIANTES ----

    fun obtenerTodosLosEstudiantes(): Query {
        return coleccionUsuarios
            .whereEqualTo("tipoUsuario", "estudiante")
    }

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

    fun buscarEstudiantePorCorreo(correo: String): Query {
        return coleccionUsuarios
            .whereEqualTo("tipoUsuario", "estudiante")
            .whereEqualTo("correo", correo)
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
}