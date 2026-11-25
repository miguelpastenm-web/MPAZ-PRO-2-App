package cl.duoc.mpazpro.viewmodel.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.remote.dto.ActividadDto
import cl.duoc.mpazpro.data.remote.dto.ActividadRequest
import cl.duoc.mpazpro.data.remote.dto.ObjetivoDto
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import cl.duoc.mpazpro.data.repository.ContenidosRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContenidosDocenteUiState(
    val objetivos: List<ObjetivoDto> = emptyList(),
    val objetivoSeleccionado: ObjetivoDto? = null,
    val actividades: List<ActividadDto> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class ContenidosDocenteViewModel(
    private val repository: ContenidosRemoteRepository = ContenidosRemoteRepository()
) : ViewModel() {
    private val _estado = MutableStateFlow(ContenidosDocenteUiState())
    val estado = _estado.asStateFlow()

    fun cargarObjetivos() = ejecutar {
        _estado.value = _estado.value.copy(
            objetivos = emptyList(), objetivoSeleccionado = null, actividades = emptyList()
        )
        val objetivos = repository.listarObjetivos().getOrThrow()
        _estado.value = _estado.value.copy(objetivos = objetivos)
    }

    fun seleccionarObjetivo(objetivo: ObjetivoDto?) = ejecutar {
        _estado.value = _estado.value.copy(objetivoSeleccionado = objetivo, actividades = emptyList())
        if (objetivo != null) {
            val actividades = repository.listarActividades(objetivo.id).getOrThrow()
            _estado.value = _estado.value.copy(actividades = actividades)
        }
    }

    fun guardarObjetivo(id: Int?, solicitud: ObjetivoRequest, alGuardar: () -> Unit) = ejecutar {
        val guardado = if (id == null) repository.crearObjetivo(solicitud).getOrThrow()
        else repository.actualizarObjetivo(id, solicitud).getOrThrow()
        val actual = _estado.value
        _estado.value = actual.copy(
            objetivos = actual.objetivos.filterNot { it.id == guardado.id } + guardado,
            objetivoSeleccionado = actual.objetivoSeleccionado?.let {
                if (it.id == guardado.id) guardado else it
            }
        )
        alGuardar()
    }

    fun eliminarObjetivo(id: Int) = ejecutar {
        repository.eliminarObjetivo(id).getOrThrow()
        val actual = _estado.value
        _estado.value = actual.copy(
            objetivos = actual.objetivos.filterNot { it.id == id },
            objetivoSeleccionado = null,
            actividades = emptyList()
        )
    }

    fun guardarActividad(id: Int?, solicitud: ActividadRequest, alGuardar: () -> Unit) = ejecutar {
        val guardada = if (id == null) repository.crearActividad(solicitud).getOrThrow()
        else repository.actualizarActividad(id, solicitud).getOrThrow()
        val actual = _estado.value
        _estado.value = actual.copy(actividades = actual.actividades.filterNot { it.id == guardada.id } + guardada)
        alGuardar()
    }

    fun eliminarActividad(id: Int) = ejecutar {
        repository.eliminarActividad(id).getOrThrow()
        val actual = _estado.value
        _estado.value = actual.copy(actividades = actual.actividades.filterNot { it.id == id })
    }

    private fun ejecutar(operacion: suspend () -> Unit) {
        if (_estado.value.cargando) return
        _estado.value = _estado.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = e.message ?: "No se pudo completar la operación.")
            } finally {
                _estado.value = _estado.value.copy(cargando = false)
            }
        }
    }
}
