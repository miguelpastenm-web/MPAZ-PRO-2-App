package cl.duoc.mpazpro.data.local

import android.content.Context

object ConfiguracionEvaluacion {

    private const val PREFS = "configuracion_evaluacion"
    private const val KEY_PISTAS = "permitir_pistas"

    fun guardarPistas(context: Context, permitir: Boolean) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PISTAS, permitir)
            .apply()
    }

    fun obtenerPistas(context: Context): Boolean {
        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_PISTAS, true)
    }
}
