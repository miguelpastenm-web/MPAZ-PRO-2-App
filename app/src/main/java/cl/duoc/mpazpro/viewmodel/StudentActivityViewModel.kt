
package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.database.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel de las actividades del estudiante.
 *
 * Se encarga de cargar las preguntas desde Room,
 * comprobar las respuestas y calcular los puntajes.
 *
 * También administra los estados de carga y guardado
 * para que la pantalla pueda mostrar los cambios.
 */
class StudentActivityViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // Guardamos las actividades obtenidas desde la base de datos.
    // La pantalla puede observarlas, pero no modificarlas directamente.
    private val _actividades =
        MutableStateFlow<List<ActividadEntity>>(emptyList())

    val actividades: StateFlow<List<ActividadEntity>> =
        _actividades.asStateFlow()

    // Indica si todavía estamos cargando las preguntas.
    private val _estaCargando = MutableStateFlow(true)

    val estaCargando: StateFlow<Boolean> =
        _estaCargando.asStateFlow()

    // Almacena un mensaje cuando ocurre un error.
    private val _mensajeError = MutableStateFlow<String?>(null)

    val mensajeError: StateFlow<String?> =
        _mensajeError.asStateFlow()

    // Indica si Room está guardando los resultados.
    private val _guardandoResultados = MutableStateFlow(false)

    val guardandoResultados: StateFlow<Boolean> =
        _guardandoResultados.asStateFlow()

    // Confirma que los resultados terminaron de guardarse.
    private val _resultadosGuardados = MutableStateFlow(false)

    val resultadosGuardados: StateFlow<Boolean> =
        _resultadosGuardados.asStateFlow()

    private var jobCarga: Job? = null

    /**
     * Consulta las actividades mediante el repositorio.
     * Si se proporciona un oaId válido (> 0), filtra solo las actividades de esa materia desde Room.
     * De lo contrario, consulta todas las actividades disponibles.
     */
    fun cargarActividades(oaId: Int? = null) {
        jobCarga?.cancel()
        jobCarga = viewModelScope.launch {
            _estaCargando.value = true
            _mensajeError.value = null

            try {
                val flow = if (oaId != null && oaId > 0) {
                    repository.obtenerActividadesPorObjetivo(oaId)
                } else {
                    repository.obtenerActividades()
                }
                flow.collect { lista ->
                    _actividades.value = lista
                    _estaCargando.value = false
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _mensajeError.value =
                        "No se pudieron cargar las actividades."
                    _estaCargando.value = false
                }
            }
        }
    }

    /**
     * Relaciona el identificador del objetivo de aprendizaje
     * con el nombre de su asignatura.
     */
    fun obtenerNombreAsignatura(oaId: Int): String {
        return when (oaId) {
            1 -> "Lenguaje y Comunicación"
            2 -> "Matemática"
            3 -> "Ciencias Naturales"
            4 -> "Historia y Geografía"
            else -> "Asignatura desconocida"
        }
    }

    /**
     * Reúne las cuatro alternativas de una actividad.
     *
     * shuffled() mezcla su orden para que la respuesta
     * correcta no aparezca siempre en la misma posición.
     */
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

    // Comprueba si la respuesta elegida es la correcta.
    fun verificarRespuesta(
        actividad: ActividadEntity,
        respuesta: String
    ): Boolean {
        return respuesta == actividad.respuestaCorrecta
    }

    /**
     * Calcula y guarda los resultados de una evaluación.
     *
     * Recibe un mapa donde la clave es el ID de la actividad
     * y el valor indica si fue respondida correctamente.
     *
     * Utilizamos un estudiante ficticio mientras no exista
     * un sistema de identificación de estudiantes.
     */
    fun guardarResultados(
        respuestas: Map<Int, Boolean>,
        estudianteId: Int = 1
    ) {

        // Evitamos iniciar un nuevo guardado si ya hay
        // uno en proceso o la evaluación está registrada.
        if (_guardandoResultados.value ||
            _resultadosGuardados.value
        ) return

        // Comprobamos que existan actividades y que
        // todas tengan una respuesta registrada.
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
                // Agrupamos las preguntas según su objetivo.
                val actividadesPorObjetivo =
                    preguntas.groupBy { it.oaId }

                // Calculamos el puntaje de cada objetivo.
                val resultados = actividadesPorObjetivo.map { (oaId, actividades) ->

                    val aciertos = actividades.count {
                        respuestas[it.id] == true
                    }

                    // Calculamos el porcentaje de respuestas correctas.
                    val puntaje = aciertos * 100 / actividades.size

                    // Preparamos un registro por objetivo.
                    val resultado = ResultadoPruebaEntity(
                        estudianteFicticioId = estudianteId,
                        oaId = oaId,
                        puntaje = puntaje,

                        // Por ahora representa la cantidad de preguntas
                        // del objetivo, no los reintentos individuales.
                        intentosUsados = actividades.size,

                        fechaRegistro = System.currentTimeMillis()
                    )

                    resultado
                }

                // Guardamos los resultados juntos mediante Room.
                repository.insertarResultados(resultados)

                // Informamos a la pantalla que el guardado terminó.
                _resultadosGuardados.value = true

            } catch (e: Exception) {
                _mensajeError.value =
                    "Ocurrió un problema al guardar los resultados."
            } finally {
                // Quitamos el estado de carga, incluso si hubo un error.
                _guardandoResultados.value = false
            }
        }
    }

    /**
     * Prepara el ViewModel para otra evaluación.
     *
     * Reinicia los indicadores de guardado, pero no
     * elimina los resultados existentes en SQLite.
     */
    fun reiniciarGuardado() {
        if (_guardandoResultados.value) return

        _resultadosGuardados.value = false
        _mensajeError.value = null
    }
}
