package cl.duoc.mpazpro.viewmodel.teacher

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import cl.duoc.mpazpro.data.local.preferences.PerfilEstudianteManager
import cl.duoc.mpazpro.data.local.storage.ReportStorageManager
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de exportación de reportes.
 */
data class ExportReportUiState(
    val nombreEstudiante: String = "Estudiante",
    val cursoEstudiante: String = "2.° Básico A",
    val nombreArchivo: String = "reporte_academico_2basico.json",
    val mensajeResultado: String? = null,
    val esError: Boolean = false,
    val rutaFisicaReciente: String? = null
)

/**
 * ViewModel que gestiona la generación y exportación del reporte académico.
 *
 * Desacopla la pantalla de ExportReportScreen de AppDatabase, PerfilEstudianteManager
 * y ReportStorageManager, centralizando todas las operaciones de datos en esta capa.
 */
class ExportReportViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: AppRepository

    private val _uiState = MutableStateFlow(ExportReportUiState())
    val uiState: StateFlow<ExportReportUiState> = _uiState.asStateFlow()

    /**
     * Flow reactivo de los resultados de evaluaciones almacenados en Room.
     */
    val resultados: StateFlow<List<ResultadoPruebaEntity>>

    init {
        val database = AppDatabase.getDatabase(context)
        repository = AppRepository(database)

        resultados = repository.obtenerResultados()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Cargar datos del perfil
        val nombre = PerfilEstudianteManager.obtenerNombreCompleto(context)
        val curso = PerfilEstudianteManager.obtenerCurso(context)
        _uiState.update {
            it.copy(
                nombreEstudiante = nombre,
                cursoEstudiante = curso
            )
        }
    }

    fun onNombreArchivoChange(nombre: String) {
        _uiState.update { it.copy(nombreArchivo = nombre) }
    }

    /**
     * Genera el contenido JSON del reporte académico integral.
     */
    fun generarReporte(): String {
        val state = _uiState.value
        return ReportStorageManager.generarReporteAcademicoEstudiante(
            context = context,
            nombreEstudiante = state.nombreEstudiante,
            curso = state.cursoEstudiante,
            resultados = resultados.value
        )
    }

    /**
     * Guarda el reporte en almacenamiento interno privado (context.filesDir).
     */
    fun guardarEnAlmacenamientoInterno() {
        val contenido = generarReporte()
        val resultado = ReportStorageManager.guardarEnAlmacenamientoInterno(
            context = context,
            nombreArchivo = _uiState.value.nombreArchivo,
            contenido = contenido
        )
        if (resultado.isSuccess) {
            val archivo = resultado.getOrThrow()
            _uiState.update {
                it.copy(
                    mensajeResultado = "Reporte académico guardado con éxito en memoria interna privada.",
                    esError = false,
                    rutaFisicaReciente = archivo.absolutePath
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    mensajeResultado = "Error al escribir en almacenamiento interno: ${resultado.exceptionOrNull()?.message}",
                    esError = true
                )
            }
        }
    }

    /**
     * Exporta el reporte a la ubicación seleccionada por el usuario mediante SAF.
     */
    fun exportarConSAF(uri: Uri) {
        val contenido = generarReporte()
        val resultado = ReportStorageManager.exportarConSAF(context, uri, contenido)
        if (resultado.isSuccess && resultado.getOrDefault(false)) {
            _uiState.update {
                it.copy(
                    mensajeResultado = "Reporte académico descargado y exportado exitosamente mediante SAF.",
                    esError = false,
                    rutaFisicaReciente = uri.toString()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    mensajeResultado = "Error al exportar archivo mediante SAF.",
                    esError = true
                )
            }
        }
    }

    /**
     * Vacía todos los resultados de evaluaciones de la base de datos.
     */
    fun vaciarEvaluaciones() {
        viewModelScope.launch {
            repository.borrarTodosLosResultados()
            _uiState.update {
                it.copy(
                    mensajeResultado = "Base de datos de evaluaciones vaciada correctamente.",
                    esError = false
                )
            }
        }
    }
}
