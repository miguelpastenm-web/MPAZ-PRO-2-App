package cl.duoc.mpazpro.viewmodel

/**
 * Estado inmutable de la interfaz del formulario docente (IE 2.2.1)
 * Centraliza los valores y mensajes de error desacoplados de la vista.
 */
data class TeacherFormUiState(
    val nombreDocente: String = "",
    val codigoDocente: String = "",
    val maxIntentos: String = "",
    val fechaLimite: String = "",
    val asignatura: String = "Matemática",
    val nombreDocenteError: String? = null,
    val codigoDocenteError: String? = null,
    val maxIntentosError: String? = null,
    val fechaLimiteError: String? = null,
    val asignaturaError: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
)
