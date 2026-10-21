package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.entities.DocenteEntity
import cl.duoc.mpazpro.data.local.entities.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val database: AppDatabase
) {
    private val docenteDao = database.docenteDao()
    private val objetivoDao = database.objetivoAprendizajeDao()
    private val actividadDao = database.actividadDao()
    private val resultadoDao = database.resultadoPruebaDao()

    // DOCENTES
    fun obtenerDocentes(): Flow<List<DocenteEntity>> =
        docenteDao.obtenerTodos()

    suspend fun insertarDocente(docente: DocenteEntity) =
        docenteDao.insertar(docente)

    suspend fun actualizarDocente(docente: DocenteEntity) =
        docenteDao.actualizar(docente)

    suspend fun eliminarDocente(docente: DocenteEntity) =
        docenteDao.eliminar(docente)

    // OBJETIVOS DE APRENDIZAJE
    fun obtenerObjetivos(): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerTodos()

    fun obtenerObjetivosPorAsignatura(
        asignaturaId: Int
    ): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerPorAsignatura(asignaturaId)

    fun obtenerObjetivosAutorizados(): Flow<List<ObjetivoAprendizajeEntity>> =
        objetivoDao.obtenerAutorizados()

    suspend fun insertarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.insertar(objetivo)

    suspend fun actualizarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.actualizar(objetivo)

    suspend fun eliminarObjetivo(objetivo: ObjetivoAprendizajeEntity) =
        objetivoDao.eliminar(objetivo)

    // ACTIVIDADES
    fun obtenerActividades(): Flow<List<ActividadEntity>> =
        actividadDao.obtenerTodas()

    fun obtenerActividadesPorObjetivo(
        oaId: Int
    ): Flow<List<ActividadEntity>> =
        actividadDao.obtenerPorObjetivo(oaId)

    suspend fun insertarActividad(actividad: ActividadEntity) =
        actividadDao.insertar(actividad)

    suspend fun actualizarActividad(actividad: ActividadEntity) =
        actividadDao.actualizar(actividad)

    suspend fun eliminarActividad(actividad: ActividadEntity) =
        actividadDao.eliminar(actividad)

    // RESULTADOS DE PRUEBAS
    fun obtenerResultados(): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerTodos()

    fun obtenerResultadosPorEstudiante(
        estudianteId: Int
    ): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerPorEstudiante(estudianteId)

    fun obtenerHistorialResultados(): Flow<List<ResultadoPruebaEntity>> =
        resultadoDao.obtenerHistorial()

    suspend fun insertarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.insertar(resultado)

    // Guarda todos los resultados de una evaluación
    // en una única operación de Room.
    suspend fun insertarResultados(
        resultados: List<ResultadoPruebaEntity>
    ) {
        resultadoDao.insertarTodos(resultados)
    }


    suspend fun actualizarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.actualizar(resultado)

    suspend fun eliminarResultado(resultado: ResultadoPruebaEntity) =
        resultadoDao.eliminar(resultado)
}
