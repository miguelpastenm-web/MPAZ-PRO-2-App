package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity

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

    // Nos indica si estamos guardando una evaluación.
    private val _guardandoResultados = MutableStateFlow(false)
    val guardandoResultados: StateFlow<Boolean> =
        _guardandoResultados.asStateFlow()

    // Confirmamos cuando Room termina de guardar.
    private val _resultadosGuardados = MutableStateFlow(false)
    val resultadosGuardados: StateFlow<Boolean> =
        _resultadosGuardados.asStateFlow()


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

    // Guarda las respuestas de una evaluación completa.
    // La clave es el ID de la actividad y el valor indica
    // si el estudiante respondió correctamente.

    // Guardamos los puntajes cuando el estudiante
    // termina de responder todas las preguntas.
    fun guardarResultados(
        respuestas: Map<Int, Boolean>,
        estudianteId: Int = 1
    ) {
        // Evitamos iniciar dos guardados al mismo tiempo.
        if (_guardandoResultados.value ||
            _resultadosGuardados.value
        ) return

        // Solo continuamos si se respondieron todas las preguntas.
        val preguntas = _actividades.value

        if (preguntas.isEmpty() ||
            preguntas.any { !respuestas.containsKey(it.id) }
        ) {
            _mensajeError.value = "Faltan preguntas por responder."
            return
        }

        viewModelScope.launch {
            _guardandoResultados.value = true
            _mensajeError.value = null
            _resultadosGuardados.value = false

            try {
                // Agrupamos las preguntas por objetivo de aprendizaje.
                val actividadesPorObjetivo =
                    preguntas.groupBy { it.oaId }


                // Preparamos los resultados de cada objetivo.
                val resultados = actividadesPorObjetivo.map { (oaId, actividades) ->

                    val aciertos = actividades.count {
                        respuestas[it.id] == true
                    }

                    val puntaje = aciertos * 100 / actividades.size

                    ResultadoPruebaEntity(
                        estudianteFicticioId = estudianteId,
                        oaId = oaId,
                        puntaje = puntaje,
                        intentosUsados = actividades.size,
                        fechaRegistro = System.currentTimeMillis()
                    )
                }

                // Guardamos los cuatro resultados en una sola transacción.
                repository.insertarResultados(resultados)


                // Confirmamos que terminaron las inserciones.
                _resultadosGuardados.value = true

            } catch (e: Exception) {
                _mensajeError.value =
                    "Ocurrió un problema al guardar los resultados."
            } finally {
                _guardandoResultados.value = false
            }
        }
    }

    // Permite comenzar una nueva evaluación.
    fun reiniciarGuardado() {
        if (_guardandoResultados.value) return
        _resultadosGuardados.value = false
        _mensajeError.value = null
    }


}
