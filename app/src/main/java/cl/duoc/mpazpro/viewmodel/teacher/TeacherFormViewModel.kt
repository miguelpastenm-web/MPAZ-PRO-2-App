package cl.duoc.mpazpro.viewmodel.teacher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.database.entities.DocenteEntity
import cl.duoc.mpazpro.data.local.preferences.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.local.preferences.EvaluacionConfig
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * ViewModel que gestiona la logica de validacion del formulario docente.
 * Centraliza validaciones desacopladas de componentes visuales con StateFlow.
 *
 * También encapsula la persistencia en SharedPreferences (ConfiguracionEvaluacion)
 * y en Room (DocenteEntity) para que la pantalla no acceda directamente a la capa de datos.
 */
class TeacherFormViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: AppRepository

    private val _uiState = MutableStateFlow(TeacherFormUiState())
    val uiState: StateFlow<TeacherFormUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(context)
        repository = AppRepository(database)
        // Cargar configuración previa de la asignatura por defecto
        cargarConfigDesdePreferences(_uiState.value.asignatura)
    }

    // --- Funciones individuales de validacion logica (Facilita modificacion en vivo en la Defensa) ---

    fun validarNombre(nombre: String): String? {
        return when {
            nombre.isBlank() -> "El nombre del docente es obligatorio"
            nombre.trim().length < 4 -> "El nombre debe tener al menos 4 caracteres"
            else -> null
        }
    }

    fun validarCodigo(codigo: String): String? {
        return when {
            codigo.isBlank() -> "El código o identificador docente es obligatorio"
            codigo.trim().length < 4 -> "El código debe tener al menos 4 caracteres (ej: DOC-101)"
            else -> null
        }
    }

    fun validarIntentos(intentos: String): String? {
        val num = intentos.toIntOrNull()
        return when {
            intentos.isBlank() -> "Debe ingresar el número de intentos"
            num == null -> "El valor ingresado debe ser numérico"
            num !in 1..5 -> "El límite de intentos debe estar entre 1 y 5"
            else -> null
        }
    }

    fun validarFecha(fecha: String): String? {
        if (fecha.isBlank()) {
            return "La fecha límite de rendición es obligatoria"
        }

        val formato = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT)

        val fechaIngresada = try {
            LocalDate.parse(fecha.trim(), formato)
        } catch (e: DateTimeParseException) {
            return "Debe ingresar una fecha válida en formato AAAA-MM-DD"
        }

        if (fechaIngresada.isBefore(LocalDate.now())) {
            return "La fecha límite no puede ser anterior a hoy"
        }

        return null
    }

    fun validarConfirmacion(confirmado: Boolean): String? {
        return if (!confirmado) "Debe confirmar los parámetros antes de guardar" else null
    }

    // --- Manejo de eventos de entrada ---

    fun onNombreDocenteChange(nombre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nombreDocente = nombre,
                nombreDocenteError = if (currentState.nombreDocenteError != null) validarNombre(nombre) else null,
                isSuccess = false
            )
        }
    }

    fun onCodigoDocenteChange(codigo: String) {
        _uiState.update { currentState ->
            currentState.copy(
                codigoDocente = codigo,
                codigoDocenteError = if (currentState.codigoDocenteError != null) validarCodigo(codigo) else null,
                isSuccess = false
            )
        }
    }

    fun onMaxIntentosChange(intentos: String) {
        val filtrado = intentos.filter { it.isDigit() }
        _uiState.update { currentState ->
            currentState.copy(
                maxIntentos = filtrado,
                maxIntentosError = if (currentState.maxIntentosError != null) validarIntentos(filtrado) else null,
                isSuccess = false
            )
        }
    }

    fun onFechaLimiteChange(fecha: String) {
        _uiState.update { currentState ->
            currentState.copy(
                fechaLimite = fecha,
                fechaLimiteError = if (currentState.fechaLimiteError != null) validarFecha(fecha) else null,
                isSuccess = false
            )
        }
    }

    fun onAsignaturaChange(asignatura: String) {
        _uiState.update { currentState ->
            currentState.copy(
                asignatura = asignatura,
                isSuccess = false
            )
        }
        // Al cambiar de asignatura, cargar configuración previamente guardada
        cargarConfigDesdePreferences(asignatura)
    }

    fun onPermitirPistasChange(permitir: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                permitirPistas = permitir,
                isSuccess = false
            )
        }
    }

    fun onConfirmarParametrosChange(confirmado: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmarParametros = confirmado,
                confirmarParametrosError = if (currentState.confirmarParametrosError != null) validarConfirmacion(confirmado) else null,
                isSuccess = false
            )
        }
    }

    /**
     * Carga en el formulario los parámetros previamente guardados para la asignatura seleccionada.
     * Lectura interna de SharedPreferences, la pantalla no necesita conocer ConfiguracionEvaluacion.
     */
    private fun cargarConfigDesdePreferences(asignatura: String) {
        val guardada = ConfiguracionEvaluacion.obtenerParametros(context, asignatura)
        if (guardada != null) {
            cargarConfiguracionPrevia(
                nombre = guardada.docente,
                codigo = guardada.codigoDocente,
                intentos = guardada.maxIntentos.toString(),
                fecha = guardada.fechaLimite,
                pistas = guardada.permitirPistas
            )
        }
    }

    /**
     * Carga en el formulario los parámetros previamente guardados para la asignatura seleccionada
     */
    fun cargarConfiguracionPrevia(
        nombre: String,
        codigo: String,
        intentos: String,
        fecha: String,
        pistas: Boolean
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                nombreDocente = nombre,
                codigoDocente = codigo,
                maxIntentos = intentos,
                fechaLimite = fecha,
                permitirPistas = pistas,
                confirmarParametros = true,
                nombreDocenteError = null,
                codigoDocenteError = null,
                maxIntentosError = null,
                fechaLimiteError = null,
                confirmarParametrosError = null,
                isSuccess = false
            )
        }
    }

    // --- Accion de Guardado ---

    /**
     * Valida el formulario y, si es válido, persiste los parámetros en SharedPreferences
     * y el docente en Room. La pantalla solo necesita llamar este método.
     */
    fun validarYGuardar(): Boolean {
        val actual = _uiState.value

        val errNombre = validarNombre(actual.nombreDocente)
        val errCodigo = validarCodigo(actual.codigoDocente)
        val errIntentos = validarIntentos(actual.maxIntentos)
        val errFecha = validarFecha(actual.fechaLimite)
        val errConfirmacion = validarConfirmacion(actual.confirmarParametros)

        val hayErrores = errNombre != null || errCodigo != null || errIntentos != null || errFecha != null || errConfirmacion != null

        _uiState.update { currentState ->
            currentState.copy(
                nombreDocenteError = errNombre,
                codigoDocenteError = errCodigo,
                maxIntentosError = errIntentos,
                fechaLimiteError = errFecha,
                confirmarParametrosError = errConfirmacion,
                isSuccess = !hayErrores,
                successMessage = if (!hayErrores) "Parámetros guardados exitosamente para ${actual.asignatura}" else null
            )
        }

        if (!hayErrores) {
            // Persistencia en SharedPreferences
            val config = EvaluacionConfig(
                asignatura = actual.asignatura,
                docente = actual.nombreDocente,
                codigoDocente = actual.codigoDocente,
                maxIntentos = actual.maxIntentos.toIntOrNull() ?: 1,
                fechaLimite = actual.fechaLimite,
                permitirPistas = actual.permitirPistas
            )
            ConfiguracionEvaluacion.guardarParametros(context, config)

            // Persistencia en Room (Tabla docentes)
            viewModelScope.launch {
                repository.insertarDocente(
                    DocenteEntity(
                        nombre = actual.nombreDocente,
                        correoFicticio = actual.codigoDocente,
                        asignatura = actual.asignatura
                    )
                )
            }
        }

        return !hayErrores
    }

    fun limpiarFormulario() {
        _uiState.value = TeacherFormUiState()
    }
}
