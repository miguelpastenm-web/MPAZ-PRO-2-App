
package cl.duoc.mpazpro.viewmodel.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.database.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import cl.duoc.mpazpro.data.local.preferences.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.repository.AppRepository
import cl.duoc.mpazpro.data.repository.ConexionContenidosException
import cl.duoc.mpazpro.data.repository.ContenidosRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel de las actividades del estudiante.
 *
 * Se encarga de cargar las preguntas del catálogo remoto o de Room,
 * comprobar las respuestas y calcular los puntajes.
 *
 * También administra los estados de carga y guardado
 * para que la pantalla pueda mostrar los cambios.
 *
 * Centraliza el acceso a ConfiguracionEvaluacion para que
 * la pantalla no dependa directamente de la capa de datos.
 */
class StudentActivityViewModel(
    application: Application,
    private val repository: AppRepository,
    private val contenidos: ContenidosRemoteRepository? = null
) : AndroidViewModel(application) {

    /**
     * Constructor secundario que inicializa el repositorio mediante Room.
     * Permite que la pantalla UI instancie el ViewModel directamente sin
     * importar Room ni el Repositorio.
     */
    constructor(application: Application) : this(
        application,
        AppRepository(AppDatabase.getDatabase(application.applicationContext)),
        ContenidosRemoteRepository()
    )

    private val context = application.applicationContext

    // Guardamos las preguntas de la evaluación actual.
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

    private val _avisoContenidos = MutableStateFlow<String?>(null)
    val avisoContenidos = _avisoContenidos.asStateFlow()

    // Indica si Room está guardando los resultados.
    private val _guardandoResultados = MutableStateFlow(false)

    val guardandoResultados: StateFlow<Boolean> =
        _guardandoResultados.asStateFlow()

    // Confirma que los resultados terminaron de guardarse.
    private val _resultadosGuardados = MutableStateFlow(false)

    val resultadosGuardados: StateFlow<Boolean> =
        _resultadosGuardados.asStateFlow()

    private var jobCarga: Job? = null

    companion object {
        // Estudiante ficticio hasta que exista identificación real.
        const val ESTUDIANTE_FICTICIO_ID = 1
    }

    /**
     * La ruta de estudiante identifica la asignatura con el OA precargado de Room.
     * El catálogo remoto se consulta con los identificadores que devuelve el servidor.
     */
    fun cargarActividades(oaId: Int? = null) {
        jobCarga?.cancel()
        jobCarga = viewModelScope.launch {
            _estaCargando.value = true
            _actividades.value = emptyList()
            _mensajeError.value = null
            _avisoContenidos.value = null

            try {
                val asignaturaId = oaId?.takeIf { it > 0 }
                if (asignaturaId != null) {
                    val mensaje = mensajeIntentosAgotados(asignaturaId, ESTUDIANTE_FICTICIO_ID)
                    if (mensaje != null) {
                        _mensajeError.value = mensaje
                        return@launch
                    }
                }

                val remotas = try {
                    contenidos?.let { consultarCatalogo(it, asignaturaId) }
                } catch (e: ConexionContenidosException) {
                    _avisoContenidos.value = "${e.message} Se muestran las actividades locales."
                    null
                }
                if (remotas != null) {
                    _actividades.value = remotas
                    return@launch
                }

                val locales = if (asignaturaId != null) {
                    repository.obtenerActividadesPorObjetivo(asignaturaId)
                } else {
                    repository.obtenerActividades()
                }
                locales.collect { lista ->
                    _actividades.value = lista
                    _estaCargando.value = false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeError.value = e.message ?: "No se pudieron cargar las actividades."
            } finally {
                if (isActive) _estaCargando.value = false
            }
        }
    }

    private suspend fun consultarCatalogo(
        contenidos: ContenidosRemoteRepository,
        asignaturaId: Int?
    ): List<ActividadEntity>? {
        val objetivos = contenidos.listarObjetivos().getOrThrow()
            .filter { it.asignaturaId in 1..4 && (asignaturaId == null || it.asignaturaId == asignaturaId) }
        if (objetivos.isEmpty()) return null
        val publicados = objetivos.filter { it.autorizado }
        if (publicados.isEmpty()) return emptyList()
        return publicados.flatMap { objetivo ->
            contenidos.listarActividades(objetivo.id).getOrThrow().map { actividad ->
                ActividadEntity(
                    id = actividad.id,
                    // Los intentos y reportes de Room conservan el OA local de la asignatura.
                    oaId = objetivo.asignaturaId,
                    titulo = actividad.titulo,
                    tipoDesafio = actividad.tipoDesafio,
                    pregunta = actividad.pregunta,
                    respuestaCorrecta = actividad.respuestaCorrecta,
                    alternativaIncorrecta1 = actividad.alternativaIncorrecta1.orEmpty(),
                    alternativaIncorrecta2 = actividad.alternativaIncorrecta2.orEmpty(),
                    alternativaIncorrecta3 = actividad.alternativaIncorrecta3.orEmpty()
                )
            }
        }.takeIf { it.isNotEmpty() }
    }

    /**
     * Máximo de intentos que definió el docente para el objetivo,
     * o null si no configuró la asignatura (en ese caso no hay límite).
     */
    private fun maxIntentosDe(oaId: Int): Int? {
        val asignatura = obtenerNombreAsignatura(oaId)
        return ConfiguracionEvaluacion.obtenerParametros(context, asignatura)?.maxIntentos
    }

    /**
     * Devuelve el mensaje de error si el estudiante ya usó todos sus
     * intentos en el objetivo, o null si todavía puede rendir.
     * Compara los intentos registrados en Room con el máximo del docente.
     */
    private suspend fun mensajeIntentosAgotados(oaId: Int, estudianteId: Int): String? {
        val max = maxIntentosDe(oaId) ?: return null
        val usados = repository.contarIntentos(estudianteId, oaId)
        return if (usados >= max) {
            "Alcanzaste el máximo de $max intento(s) en ${obtenerNombreAsignatura(oaId)}."
        } else null
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
     * Consulta si las pistas están permitidas para una asignatura específica.
     * Centraliza la lectura de SharedPreferences para que la pantalla no acceda a ConfiguracionEvaluacion.
     */
    fun obtenerPermitirPistas(asignatura: String): Boolean {
        return if (asignatura.isNotEmpty()) {
            ConfiguracionEvaluacion.obtenerPistasPorMateria(context, asignatura)
        } else {
            ConfiguracionEvaluacion.obtenerPistas(context)
        }
    }

    /**
     * Obtiene la configuración de evaluación para una asignatura en formato UI.
     * Centraliza la lectura de SharedPreferences para que la pantalla no acceda a la capa data.
     */
    fun obtenerConfiguracionMateria(asignatura: String): EvaluacionConfigUi? {
        val config = ConfiguracionEvaluacion.obtenerParametros(context, asignatura) ?: return null
        return EvaluacionConfigUi(
            asignatura = config.asignatura,
            docente = config.docente,
            codigoDocente = config.codigoDocente,
            maxIntentos = config.maxIntentos,
            fechaLimite = config.fechaLimite,
            permitirPistas = config.permitirPistas
        )
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
        estudianteId: Int = ESTUDIANTE_FICTICIO_ID
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

                // Intentos ya registrados por objetivo. Con esto aplicamos el
                // límite del docente y numeramos el intento que se guardará.
                val intentosPrevios = actividadesPorObjetivo.keys.associateWith {
                    repository.contarIntentos(estudianteId, it)
                }

                // Si algún objetivo ya llegó al máximo, no se guarda nada.
                val agotado = actividadesPorObjetivo.keys.firstNotNullOfOrNull { oaId ->
                    mensajeIntentosAgotados(oaId, estudianteId)
                }
                if (agotado != null) {
                    _mensajeError.value = agotado
                    return@launch
                }

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

                        // Número de intento: los ya guardados + este.
                        intentosUsados = (intentosPrevios[oaId] ?: 0) + 1,

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

