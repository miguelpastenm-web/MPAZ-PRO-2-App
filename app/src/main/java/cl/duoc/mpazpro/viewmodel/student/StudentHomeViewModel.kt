package cl.duoc.mpazpro.viewmodel.student

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import cl.duoc.mpazpro.data.local.preferences.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.local.preferences.PerfilEstudianteManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Modelo de datos de UI inmutable que representa los parámetros de evaluación fijados por el docente.
 * Desacopla la vista visual (StudentHomeScreen) de la capa data/local.
 */
data class EvaluacionConfigUi(
    val asignatura: String,
    val docente: String,
    val codigoDocente: String,
    val maxIntentos: Int,
    val fechaLimite: String,
    val permitirPistas: Boolean
)

/**
 * Estado de la interfaz de la pantalla principal del estudiante.
 */
data class StudentHomeUiState(
    val nombreCompleto: String = "Estudiante",
    val curso: String = "2.° Básico A",
    val avatarBitmap: Bitmap? = null,
    val configuraciones: Map<String, EvaluacionConfigUi?> = emptyMap()
)

/**
 * ViewModel que provee los datos necesarios para StudentHomeScreen.
 *
 * Desacopla la pantalla de PerfilEstudianteManager y ConfiguracionEvaluacion,
 * centralizando la lectura de SharedPreferences en la capa ViewModel.
 */
class StudentHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga el perfil del estudiante y las configuraciones de evaluación
     * de las 4 asignaturas desde SharedPreferences y almacenamiento interno.
     */
    private fun cargarDatos() {
        val nombreCompleto = PerfilEstudianteManager.obtenerNombreCompleto(context)
        val curso = PerfilEstudianteManager.obtenerCurso(context)
        val avatar = runCatching {
            val file = File(context.filesDir, "avatar_perfil.png")
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }.getOrNull()

        val asignaturas = listOf(
            "Lenguaje y Comunicación",
            "Matemática",
            "Ciencias Naturales",
            "Historia y Geografía"
        )

        val configs = asignaturas.associateWith { materia ->
            ConfiguracionEvaluacion.obtenerParametros(context, materia)?.let { config ->
                EvaluacionConfigUi(
                    asignatura = config.asignatura,
                    docente = config.docente,
                    codigoDocente = config.codigoDocente,
                    maxIntentos = config.maxIntentos,
                    fechaLimite = config.fechaLimite,
                    permitirPistas = config.permitirPistas
                )
            }
        }

        _uiState.value = StudentHomeUiState(
            nombreCompleto = nombreCompleto,
            curso = curso,
            avatarBitmap = avatar,
            configuraciones = configs
        )
    }

    /**
     * Obtiene la configuración de evaluación para una asignatura específica.
     */
    fun obtenerConfiguracion(asignatura: String): EvaluacionConfigUi? {
        return _uiState.value.configuraciones[asignatura]
    }
}
