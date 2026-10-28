
package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.database.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.database.entities.DocenteEntity
import cl.duoc.mpazpro.data.local.database.entities.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio principal de MPAZ PRO.
 *
 * Su función es centralizar el acceso a los datos de Room.
 * Así, los ViewModels pueden consultar y guardar información
 * sin tener que trabajar directamente con los DAO.
 */
class AppRepository(
    private val database: AppDatabase
) {

    // Obtenemos los DAO de cada tabla de la base de datos.
    // Cada DAO contiene las operaciones de consulta y modificación.
    private val docenteDao = database.docenteDao()
    private val objetivoDao = database.objetivoAprendizajeDao()
    private val actividadDao = database.actividadDao()
    private val resultadoDao = database.resultadoPruebaDao()

    // --------------------------------------------------
    // DOCENTES
    // --------------------------------------------------

    // Observa la lista de docentes registrados.
    fun obtenerDocentes(): Flow<List<DocenteEntity>> =
        docenteDao.obtenerTodos()

    // Agrega un docente a la base de datos.
    suspend fun insertarDocente(docente: DocenteEntity) =
        docenteDao.insertar(docente)

    // Modifica los datos de un docente existente.
    suspend fun actualizarDocente(docente: DocenteEntity) =
        docenteDao.actualizar(docente)

    // Elimina un docente de la base de datos.
    suspend fun eliminarDocente(docente: DocenteEntity) =
        docenteDao.eliminar(docente)

    // --------------------------------------------------
    // OBJETIVOS DE APRENDIZAJE
    // --------------------------------------------------

    // Observa todos los objetivos de aprendizaje.
    fun obtenerObjetivos(): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerTodos()

    // Obtiene los objetivos asociados a una asignatura.
    fun obtenerObjetivosPorAsignatura(
        asignaturaId: Int
    ): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerPorAsignatura(asignaturaId)

    // Obtiene los objetivos marcados como autorizados.
    fun obtenerObjetivosAutorizados(): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerAutorizados()

    // Agrega un nuevo objetivo de aprendizaje.
    suspend fun insertarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.insertar(objetivo)

    // Actualiza la información de un objetivo existente.
    suspend fun actualizarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.actualizar(objetivo)

    // Elimina un objetivo de aprendizaje.
    suspend fun eliminarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.eliminar(objetivo)

    // --------------------------------------------------
    // ACTIVIDADES
    // --------------------------------------------------

    // Observa las actividades disponibles para el estudiante.
    fun obtenerActividades(): Flow<List<ActividadEntity>> =
        actividadDao.obtenerTodas()

    // Obtiene las actividades de un objetivo específico.
    fun obtenerActividadesPorObjetivo(
        oaId: Int
    ): Flow<List<ActividadEntity>> =
        actividadDao.obtenerPorObjetivo(oaId)

    // Registra una nueva actividad.
    suspend fun insertarActividad(actividad: ActividadEntity) =
        actividadDao.insertar(actividad)

    // Modifica una actividad registrada.
    suspend fun actualizarActividad(actividad: ActividadEntity) =
        actividadDao.actualizar(actividad)

    // Elimina una actividad.
    suspend fun eliminarActividad(actividad: ActividadEntity) =
        actividadDao.eliminar(actividad)

    // --------------------------------------------------
    // RESULTADOS DE EVALUACIONES
    // --------------------------------------------------

    // Observa todos los resultados almacenados.
    fun obtenerResultados(): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerTodos()

    // Obtiene los resultados de un estudiante específico.
    fun obtenerResultadosPorEstudiante(
        estudianteId: Int
    ): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerPorEstudiante(estudianteId)

    // Obtiene el historial de resultados ordenado por fecha,
    // según la consulta definida en ResultadoPruebaDao.
    fun obtenerHistorialResultados(): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerHistorial()

    // Guarda un resultado individual.
    suspend fun insertarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.insertar(resultado)

    // Guarda todos los resultados de una evaluación juntos.
    // Room ejecuta la inserción múltiple como una transacción.
    suspend fun insertarResultados(
        resultados: List<ResultadoPruebaEntity>
    ) {
        resultadoDao.insertarTodos(resultados)
    }

    // Modifica un resultado almacenado.
    suspend fun actualizarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.actualizar(resultado)

    // Elimina un resultado de la base de datos.
    suspend fun eliminarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.eliminar(resultado)

    // Elimina todos los resultados de la base de datos para mantenerla limpia
    suspend fun borrarTodosLosResultados() =
        resultadoDao.borrarTodos()
}
