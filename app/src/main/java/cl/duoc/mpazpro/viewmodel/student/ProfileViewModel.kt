package cl.duoc.mpazpro.viewmodel.student

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import cl.duoc.mpazpro.data.local.preferences.PerfilEstudianteManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream

/**
 * Estado de la interfaz de perfil y credencial escolar.
 */
data class ProfileUiState(
    val nombre: String = "",
    val apellido: String = "",
    val curso: String = "",
    val fotoBitmap: Bitmap? = null,
    val mensajeEstado: String? = null,
    val esError: Boolean = false
)

/**
 * ViewModel que gestiona el perfil del estudiante y la captura fotográfica.
 *
 * Desacopla la pantalla de ProfileCameraScreen de la capa de datos local
 * (PerfilEstudianteManager y filesDir), exponiendo únicamente un StateFlow
 * con el estado actual de la UI.
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarPerfilDesdePreferences()
    }

    /**
     * Carga los datos del perfil almacenados en SharedPreferences
     * y el avatar persistido en el almacenamiento interno.
     */
    private fun cargarPerfilDesdePreferences() {
        val nombre = PerfilEstudianteManager.obtenerNombre(context)
        val apellido = PerfilEstudianteManager.obtenerApellido(context)
        val curso = PerfilEstudianteManager.obtenerCurso(context)
        val avatar = cargarAvatarDesdeArchivo()

        _uiState.value = ProfileUiState(
            nombre = nombre,
            apellido = apellido,
            curso = curso,
            fotoBitmap = avatar
        )
    }

    private fun cargarAvatarDesdeArchivo(): Bitmap? {
        return runCatching {
            val archivo = File(context.filesDir, "avatar_perfil.png")
            if (archivo.exists()) BitmapFactory.decodeFile(archivo.absolutePath) else null
        }.getOrNull()
    }

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(nombre = valor) }
    }

    fun onApellidoChange(valor: String) {
        _uiState.update { it.copy(apellido = valor) }
    }

    fun onCursoChange(valor: String) {
        _uiState.update { it.copy(curso = valor) }
    }

    /**
     * Persiste los datos del perfil en SharedPreferences.
     */
    fun guardarPerfil() {
        val state = _uiState.value
        PerfilEstudianteManager.guardarPerfil(
            context = context,
            nombre = state.nombre,
            apellido = state.apellido,
            curso = state.curso
        )
        val completo = "${state.nombre} ${state.apellido}".trim().ifEmpty { "Estudiante" }
        _uiState.update {
            it.copy(
                mensajeEstado = "Perfil escolar actualizado correctamente para $completo.",
                esError = false
            )
        }
    }

    /**
     * Registra la captura fotográfica y la persiste en filesDir.
     */
    fun onFotoCapturada(bitmap: Bitmap?) {
        if (bitmap != null) {
            _uiState.update {
                it.copy(
                    fotoBitmap = bitmap,
                    mensajeEstado = "Fotografía capturada y renderizada correctamente en la interfaz.",
                    esError = false
                )
            }
            // Persistencia del avatar en context.filesDir
            runCatching {
                val archivoFoto = File(context.filesDir, "avatar_perfil.png")
                FileOutputStream(archivoFoto).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    mensajeEstado = "Captura de fotografía cancelada.",
                    esError = false
                )
            }
        }
    }

    fun onPermisoResultado(concedido: Boolean) {
        if (concedido) {
            _uiState.update {
                it.copy(
                    mensajeEstado = "Permiso de cámara concedido. Presiona 'Abrir Cámara'.",
                    esError = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    mensajeEstado = "Permiso de cámara denegado. Se requiere para registrar el perfil escolar.",
                    esError = true
                )
            }
        }
    }
}
