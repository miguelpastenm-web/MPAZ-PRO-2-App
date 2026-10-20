package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudentActivityViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // Lista de actividades que vienen desde Room.
    private val _actividades =
        MutableStateFlow<List<ActividadEntity>>(emptyList())

    val actividades: StateFlow<List<ActividadEntity>> =
        _actividades.asStateFlow()

    // Indica si estamos esperando los datos.
    private val _estaCargando = MutableStateFlow(true)
    val estaCargando: StateFlow<Boolean> =
        _estaCargando.asStateFlow()

    // Guarda un posible error al consultar la base de datos.
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> =
        _mensajeError.asStateFlow()

    init {
        cargarActividades()
    }

    // Pedimos las preguntas al repositorio.
    fun cargarActividades() {
        viewModelScope.launch {
            _estaCargando.value = true
            _mensajeError.value = null

            try {
                repository.obtenerActividades().collect { lista ->
                    _actividades.value = lista
                    _estaCargando.value = false
                }
            } catch (e: Exception) {
                _mensajeError.value =
                    "No se pudieron cargar las actividades."
                _estaCargando.value = false
            }
        }
    }

    // Relacionamos cada objetivo con su asignatura.
    fun obtenerNombreAsignatura(oaId: Int): String {
        return when (oaId) {
            1 -> "Lenguaje y Comunicación"
            2 -> "Matemática"
            3 -> "Ciencias Naturales"
            4 -> "Historia y Geografía"
            else -> "Asignatura desconocida"
        }
    }

    // Reunimos la respuesta correcta y las incorrectas.
    fun obtenerAlternativas(
        actividad: ActividadEntity
    ): List<String> {
        return listOf(
            actividad.respuestaCorrecta,
            actividad.alternativaIncorrecta1,
            actividad.alternativaIncorrecta2,
            actividad.alternativaIncorrecta3
        ).shuffled()
    }

    // Comprobamos si el estudiante acertó.
    fun verificarRespuesta(
        actividad: ActividadEntity,
        respuesta: String
    ): Boolean {
        return respuesta == actividad.respuestaCorrecta
    }
}
