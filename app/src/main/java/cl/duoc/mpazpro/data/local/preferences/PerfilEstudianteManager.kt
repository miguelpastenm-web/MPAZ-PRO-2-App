package cl.duoc.mpazpro.data.local.preferences

import android.content.Context

/**
 * Gestor de persistencia del perfil del estudiante mediante SharedPreferences.
 * Permite registrar y recuperar nombre, apellido y curso para personalizar la experiencia
 * escolar (saludo en pantalla de inicio, ficha de credencial y autoría de reportes).
 */
object PerfilEstudianteManager {

    private const val PREFS_PERFIL = "perfil_estudiante_prefs"
    private const val KEY_NOMBRE = "estudiante_nombre"
    private const val KEY_APELLIDO = "estudiante_apellido"
    private const val KEY_CURSO = "estudiante_curso"

    fun guardarPerfil(
        context: Context,
        nombre: String,
        apellido: String,
        curso: String = "2.° Básico A"
    ) {
        context.applicationContext
            .getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NOMBRE, nombre.trim())
            .putString(KEY_APELLIDO, apellido.trim())
            .putString(KEY_CURSO, curso.trim().ifEmpty { "2.° Básico A" })
            .apply()
    }

    fun obtenerNombre(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
            .getString(KEY_NOMBRE, "Matías") ?: "Matías"
    }

    fun obtenerApellido(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
            .getString(KEY_APELLIDO, "Sepúlveda") ?: "Sepúlveda"
    }

    fun obtenerCurso(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_PERFIL, Context.MODE_PRIVATE)
            .getString(KEY_CURSO, "2.° Básico A") ?: "2.° Básico A"
    }

    fun obtenerNombreCompleto(context: Context): String {
        val nom = obtenerNombre(context).trim()
        val ape = obtenerApellido(context).trim()
        val completo = "$nom $ape".trim()
        return if (completo.isNotEmpty()) completo else "Estudiante"
    }
}
