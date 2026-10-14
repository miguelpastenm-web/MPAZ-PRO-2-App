package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel que gestiona la logica de validacion del formulario docente (IE 2.2.1)
 * Centraliza validaciones desacopladas de componentes visuales con StateFlow.
 */
class TeacherFormViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherFormUiState())
    val uiState: StateFlow<TeacherFormUiState> = _uiState.asStateFlow()

    fun onNombreDocenteChange(nombre: String) {
        _uiState.update { currentState ->
            currentState.copy(
                nombreDocente = nombre,
                nombreDocenteError = if (nombre.isNotBlank() && nombre.trim().length >= 3) null else currentState.nombreDocenteError,
                isSuccess = false
            )
        }
    }

    fun onCodigoDocenteChange(codigo: String) {
        _uiState.update { currentState ->
            currentState.copy(
                codigoDocente = codigo,
                codigoDocenteError = if (codigo.isNotBlank() && codigo.trim().length >= 4) null else currentState.codigoDocenteError,
                isSuccess = false
            )
        }
    }

    fun onMaxIntentosChange(intentos: String) {
        // Solo permitir caracteres numericos
        val filtrado = intentos.filter { it.isDigit() }
        _uiState.update { currentState ->
            currentState.copy(
                maxIntentos = filtrado,
                maxIntentosError = null,
                isSuccess = false
            )
        }
    }

    fun onFechaLimiteChange(fecha: String) {
        _uiState.update { currentState ->
            currentState.copy(
                fechaLimite = fecha,
                fechaLimiteError = if (fecha.isNotBlank()) null else currentState.fechaLimiteError,
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
    }

    fun validarYGuardar(): Boolean {
        val estadoActual = _uiState.value

        var hayErrores = false
        var errorNombre: String? = null
        var errorCodigo: String? = null
        var errorIntentos: String? = null
        var errorFecha: String? = null

        // 1. Validacion de Nombre Docente
        if (estadoActual.nombreDocente.isBlank()) {
            errorNombre = "El nombre del docente es obligatorio"
            hayErrores = true
        } else if (estadoActual.nombreDocente.trim().length < 3) {
            errorNombre = "El nombre debe tener al menos 3 caracteres"
            hayErrores = true
        }

        // 2. Validacion de Codigo/Correo Ficticio
        if (estadoActual.codigoDocente.isBlank()) {
            errorCodigo = "El codigo o identificador docente es obligatorio"
            hayErrores = true
        } else if (estadoActual.codigoDocente.trim().length < 4) {
            errorCodigo = "El codigo debe tener al menos 4 caracteres (ej: DOC-101)"
            hayErrores = true
        }

        // 3. Validacion de Maximo de Intentos (1 a 5)
        val intentosNum = estadoActual.maxIntentos.toIntOrNull()
        if (intentosNum == null) {
            errorIntentos = "Debe ingresar un numero valido de intentos"
            hayErrores = true
        } else if (intentosNum !in 1..5) {
            errorIntentos = "El limite de intentos debe estar entre 1 y 5"
            hayErrores = true
        }

        // 4. Validacion de Fecha Limite
        if (estadoActual.fechaLimite.isBlank()) {
            errorFecha = "La fecha limite de rendicion es obligatoria"
            hayErrores = true
        }

        _uiState.update { currentState ->
            currentState.copy(
                nombreDocenteError = errorNombre,
                codigoDocenteError = errorCodigo,
                maxIntentosError = errorIntentos,
                fechaLimiteError = errorFecha,
                isSuccess = !hayErrores,
                successMessage = if (!hayErrores) "Parametros guardados exitosamente para ${estadoActual.asignatura}" else null
            )
        }

        return !hayErrores
    }

    fun limpiarFormulario() {
        _uiState.value = TeacherFormUiState()
    }
}
