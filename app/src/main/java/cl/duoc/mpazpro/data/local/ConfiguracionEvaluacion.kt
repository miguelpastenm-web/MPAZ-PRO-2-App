package cl.duoc.mpazpro.data.local

import android.content.Context

/**
 * Modelo inmutable de la configuración de evaluación definida por el docente.
 * Conecta los parámetros ingresados en TeacherFormScreen con la visualización del estudiante.
 */
data class EvaluacionConfig(
    val asignatura: String,
    val docente: String,
    val codigoDocente: String,
    val maxIntentos: Int,
    val fechaLimite: String,
    val permitirPistas: Boolean
)

/**
 * Gestor de persistencia de configuración de evaluaciones mediante SharedPreferences.
 * Permite almacenar y recuperar los parámetros de cada asignatura de forma desacoplada y sin duplicados.
 */
object ConfiguracionEvaluacion {

    private const val PREFS = "configuracion_evaluacion"

    /**
     * Guarda la configuración de evaluación asociada a una asignatura específica.
     * Utiliza la clave de asignatura normalizada para evitar duplicados y sobrescribir limpiamente.
     */
    fun guardarParametros(context: Context, config: EvaluacionConfig) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefix = "asignatura_${config.asignatura.trim().lowercase()}_"

        prefs.edit()
            .putString("${prefix}nombre", config.asignatura)
            .putString("${prefix}docente", config.docente)
            .putString("${prefix}codigo", config.codigoDocente)
            .putInt("${prefix}intentos", config.maxIntentos)
            .putString("${prefix}fecha", config.fechaLimite)
            .putBoolean("${prefix}pistas", config.permitirPistas)
            .putBoolean("permitir_pistas", config.permitirPistas) // Compatibilidad global
            .apply()
    }

    /**
     * Recupera los parámetros de evaluación de una asignatura.
     * Retorna null si la asignatura aún no ha sido configurada por el docente.
     */
    fun obtenerParametros(context: Context, asignatura: String): EvaluacionConfig? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val prefix = "asignatura_${asignatura.trim().lowercase()}_"
        val docente = prefs.getString("${prefix}docente", null) ?: return null
        val codigo = prefs.getString("${prefix}codigo", "") ?: ""
        val intentos = prefs.getInt("${prefix}intentos", 1)
        val fecha = prefs.getString("${prefix}fecha", "") ?: ""
        val pistas = prefs.getBoolean("${prefix}pistas", true)

        return EvaluacionConfig(
            asignatura = asignatura,
            docente = docente,
            codigoDocente = codigo,
            maxIntentos = intentos,
            fechaLimite = fecha,
            permitirPistas = pistas
        )
    }

    /**
     * Retorna si las pistas pedagógicas están habilitadas para la asignatura solicitada.
     */
    fun obtenerPistasPorMateria(context: Context, asignatura: String): Boolean {
        val config = obtenerParametros(context, asignatura)
        return config?.permitirPistas ?: obtenerPistas(context)
    }

    fun guardarPistas(context: Context, permitir: Boolean) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("permitir_pistas", permitir)
            .apply()
    }

    fun obtenerPistas(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("permitir_pistas", true)
    }
}
