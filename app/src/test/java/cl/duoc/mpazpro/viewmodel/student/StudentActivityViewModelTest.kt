package cl.duoc.mpazpro.viewmodel.student

import android.app.Application
import cl.duoc.mpazpro.data.local.database.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import cl.duoc.mpazpro.data.local.preferences.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.local.preferences.EvaluacionConfig
import cl.duoc.mpazpro.data.repository.AppRepository
import cl.duoc.mpazpro.data.repository.ContenidosRemoteRepository
import cl.duoc.mpazpro.data.remote.ContenidosApi
import cl.duoc.mpazpro.data.remote.dto.ActividadDto
import cl.duoc.mpazpro.data.remote.dto.ObjetivoDto
import cl.duoc.mpazpro.respuestaExitosa
import cl.duoc.mpazpro.respuestaFallida
import java.io.IOException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentActivityViewModelTest {

    private lateinit var repository: AppRepository
    private lateinit var viewModel: StudentActivityViewModel

    // Dos preguntas del mismo objetivo (oaId = 2, Matemática).
    private val actividades = listOf(
        ActividadEntity(id = 1, oaId = 2, titulo = "t1", tipoDesafio = "x", pregunta = "5+3", respuestaCorrecta = "8"),
        ActividadEntity(id = 2, oaId = 2, titulo = "t2", tipoDesafio = "x", pregunta = "10-4", respuestaCorrecta = "6")
    )

    private fun configConMaximo(max: Int) = EvaluacionConfig(
        asignatura = "Matemática",
        docente = "Marta Soto",
        codigoDocente = "DOC-101",
        maxIntentos = max,
        fechaLimite = "2030-01-01",
        permitirPistas = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockkObject(ConfiguracionEvaluacion)
        every { ConfiguracionEvaluacion.obtenerParametros(any(), any()) } returns configConMaximo(2)

        repository = mockk(relaxed = true)
        every { repository.obtenerActividades() } returns flowOf(actividades)
        every { repository.obtenerActividadesPorObjetivo(2) } returns flowOf(actividades)
        coEvery { repository.contarIntentos(any(), any()) } returns 0

        viewModel = StudentActivityViewModel(mockk<Application>(relaxed = true), repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private val todasCorrectas = mapOf(1 to true, 2 to true)

    @Test
    fun cargarActividades_publicaLasActividadesDelRepositorio() {
        viewModel.cargarActividades(2)

        assertEquals(actividades, viewModel.actividades.value)
        assertFalse(viewModel.estaCargando.value)
        assertNull(viewModel.mensajeError.value)
    }

    @Test
    fun cargarActividades_conIntentosAgotados_noCargaYMuestraError() {
        coEvery { repository.contarIntentos(1, 2) } returns 2 // máximo configurado = 2

        viewModel.cargarActividades(2)

        assertTrue(viewModel.actividades.value.isEmpty())
        assertNotNull(viewModel.mensajeError.value)
        assertFalse(viewModel.estaCargando.value)
    }

    @Test
    fun cargarActividades_consultaOaRemotoAutorizadoYConservaLaAsignaturaLocal() {
        val api = mockk<ContenidosApi>()
        val objetivo = ObjetivoDto(17, 2, "MA02 OA 01", "Sumas", true)
        coEvery { api.listarObjetivos() } returns respuestaExitosa(listOf(
            objetivo, objetivo.copy(id = 18, autorizado = false), objetivo.copy(id = 19, asignaturaId = 1)
        ))
        coEvery { api.listarActividades(17) } returns respuestaExitosa(listOf(
            ActividadDto(32, 17, "Sumas", "opcion", "¿2+2?", "4", "3", "5", null)
        ))
        conectarContenidos(api)

        viewModel.cargarActividades(2)

        val pregunta = viewModel.actividades.value.single()
        assertEquals(32, pregunta.id)
        assertEquals(2, pregunta.oaId)
        assertEquals("", pregunta.alternativaIncorrecta3)
        coVerify(exactly = 1) { api.listarActividades(any()) }
        assertFalse(viewModel.estaCargando.value)
    }

    @Test
    fun cargarActividades_conObjetivoRemotoSinActividadesConservaLasPreguntasDeRoom() {
        val api = mockk<ContenidosApi>()
        val objetivo = ObjetivoDto(17, 2, "MA02 OA 01", "Sumas", true)
        coEvery { api.listarObjetivos() } returns respuestaExitosa(listOf(objetivo))
        coEvery { api.listarActividades(17) } returns respuestaExitosa(emptyList())
        conectarContenidos(api)

        viewModel.cargarActividades(2)

        assertEquals(actividades, viewModel.actividades.value)
        assertNull(viewModel.mensajeError.value)
        assertFalse(viewModel.estaCargando.value)
    }

    @Test
    fun cargarActividades_sinConexionMuestraAvisoYCargaRoom() {
        val api = mockk<ContenidosApi>()
        coEvery { api.listarObjetivos() } throws IOException("sin conexión")
        conectarContenidos(api)

        viewModel.cargarActividades(2)

        assertEquals(actividades, viewModel.actividades.value)
        assertNotNull(viewModel.avisoContenidos.value)
        assertNull(viewModel.mensajeError.value)
        assertFalse(viewModel.estaCargando.value)
    }

    @Test
    fun cargarActividades_conErrorHttpMuestraElCodigo() {
        val api = mockk<ContenidosApi>()
        coEvery { api.listarObjetivos() } returns respuestaFallida<List<ObjetivoDto>>(404)
        conectarContenidos(api)

        viewModel.cargarActividades(2)

        assertTrue(viewModel.actividades.value.isEmpty())
        assertTrue(viewModel.mensajeError.value.orEmpty().contains("404"))
        assertFalse(viewModel.estaCargando.value)
    }

    private fun conectarContenidos(api: ContenidosApi) {
        viewModel = StudentActivityViewModel(
            mockk<Application>(relaxed = true), repository, ContenidosRemoteRepository(api)
        )
    }

    @Test
    fun guardarResultados_primerIntento_guardaIntentosUsadosEnUno() {
        viewModel.cargarActividades(2)
        val guardados = slot<List<ResultadoPruebaEntity>>()
        coEvery { repository.insertarResultados(capture(guardados)) } returns Unit

        viewModel.guardarResultados(todasCorrectas)

        assertTrue(viewModel.resultadosGuardados.value)
        assertEquals(1, guardados.captured.single().intentosUsados)
        assertEquals(100, guardados.captured.single().puntaje)
    }

    @Test
    fun guardarResultados_conUnIntentoPrevio_guardaIntentosUsadosEnDos() {
        viewModel.cargarActividades(2)
        coEvery { repository.contarIntentos(1, 2) } returns 1
        val guardados = slot<List<ResultadoPruebaEntity>>()
        coEvery { repository.insertarResultados(capture(guardados)) } returns Unit

        viewModel.guardarResultados(todasCorrectas)

        assertEquals(2, guardados.captured.single().intentosUsados)
    }

    @Test
    fun guardarResultados_conIntentosAgotados_noGuardaYMuestraError() {
        viewModel.cargarActividades(2)
        coEvery { repository.contarIntentos(1, 2) } returns 2

        viewModel.guardarResultados(todasCorrectas)

        assertFalse(viewModel.resultadosGuardados.value)
        assertNotNull(viewModel.mensajeError.value)
        assertFalse(viewModel.guardandoResultados.value)
        coVerify(exactly = 0) { repository.insertarResultados(any()) }
    }

    @Test
    fun guardarResultados_sinConfiguracionDocente_noAplicaLimite() {
        every { ConfiguracionEvaluacion.obtenerParametros(any(), any()) } returns null
        viewModel.cargarActividades(2)
        coEvery { repository.contarIntentos(1, 2) } returns 10

        viewModel.guardarResultados(todasCorrectas)

        assertTrue(viewModel.resultadosGuardados.value)
    }

    @Test
    fun guardarResultados_conPreguntasSinResponder_muestraError() {
        viewModel.cargarActividades(2)

        viewModel.guardarResultados(mapOf(1 to true))

        assertEquals("Faltan preguntas por responder.", viewModel.mensajeError.value)
        coVerify(exactly = 0) { repository.insertarResultados(any()) }
    }

    @Test
    fun guardarResultados_calculaPuntajeComoPorcentajeDeAciertos() {
        viewModel.cargarActividades(2)
        val guardados = slot<List<ResultadoPruebaEntity>>()
        coEvery { repository.insertarResultados(capture(guardados)) } returns Unit

        viewModel.guardarResultados(mapOf(1 to true, 2 to false))

        assertEquals(50, guardados.captured.single().puntaje)
    }

    @Test
    fun guardarResultados_siFallaRoom_informaErrorYLiberaElEstado() {
        viewModel.cargarActividades(2)
        coEvery { repository.insertarResultados(any()) } throws RuntimeException("fallo")

        viewModel.guardarResultados(todasCorrectas)

        assertFalse(viewModel.resultadosGuardados.value)
        assertNotNull(viewModel.mensajeError.value)
        assertFalse(viewModel.guardandoResultados.value)
    }

    @Test
    fun reiniciarGuardado_limpiaIndicadoresParaNuevaEvaluacion() {
        viewModel.cargarActividades(2)
        viewModel.guardarResultados(todasCorrectas)
        assertTrue(viewModel.resultadosGuardados.value)

        viewModel.reiniciarGuardado()

        assertFalse(viewModel.resultadosGuardados.value)
        assertNull(viewModel.mensajeError.value)
    }
}
