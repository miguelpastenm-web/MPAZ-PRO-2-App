package cl.duoc.mpazpro.viewmodel.teacher

import cl.duoc.mpazpro.data.remote.ContenidosApi
import cl.duoc.mpazpro.data.remote.dto.ActividadDto
import cl.duoc.mpazpro.data.remote.dto.ObjetivoDto
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import cl.duoc.mpazpro.data.repository.ContenidosRemoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ContenidosDocenteViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val api = mockk<ContenidosApi>()
    private val viewModel = ContenidosDocenteViewModel(ContenidosRemoteRepository(api))
    private val objetivo = ObjetivoDto(17, 2, "MA02 OA 01", "Resolver sumas", true)
    private val solicitud = ObjetivoRequest(2, "MA02 OA 01", "Resolver sumas", true)
    private val actividad = ActividadDto(32, 17, "Sumas", "opcion", "¿2+2?", "4", "3", "5", null)

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun restaurar() {
        Dispatchers.resetMain()
    }

    @Test
    fun publicaCargaYObjetivosEIgnoraUnaConsultaSimultanea() {
        val respuesta = CompletableDeferred<Response<List<ObjetivoDto>>>()
        coEvery { api.listarObjetivos() } coAnswers { respuesta.await() }

        viewModel.cargarObjetivos()
        dispatcher.scheduler.runCurrent()
        assertTrue(viewModel.estado.value.cargando)
        assertTrue(viewModel.estado.value.objetivos.isEmpty())

        viewModel.cargarObjetivos()
        respuesta.complete(Response.success(listOf(objetivo)))
        dispatcher.scheduler.runCurrent()

        assertEquals(ContenidosDocenteUiState(objetivos = listOf(objetivo)), viewModel.estado.value)
        coVerify(exactly = 1) { api.listarObjetivos() }
    }

    @Test
    fun seleccionaElObjetivoYCargaActividadesYCancelaLaSeleccion() {
        seleccionarObjetivo()
        assertEquals(objetivo, viewModel.estado.value.objetivoSeleccionado)
        assertEquals(listOf(actividad), viewModel.estado.value.actividades)
        assertFalse(viewModel.estado.value.cargando)

        viewModel.seleccionarObjetivo(null)
        dispatcher.scheduler.runCurrent()

        assertNull(viewModel.estado.value.objetivoSeleccionado)
        assertTrue(viewModel.estado.value.actividades.isEmpty())
        coVerify(exactly = 1) { api.listarActividades(any()) }
    }

    @Test
    fun muestraErrorDeConexionYLimpiaElErrorAlReintentar() {
        coEvery { api.listarObjetivos() } throws IOException("Sin red")
        viewModel.cargarObjetivos()
        dispatcher.scheduler.runCurrent()

        assertEquals("No se pudo conectar con ms-contenidos.", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)

        coEvery { api.listarObjetivos() } returns Response.success(listOf(objetivo))
        viewModel.cargarObjetivos()
        assertTrue(viewModel.estado.value.cargando)
        assertNull(viewModel.estado.value.error)
        dispatcher.scheduler.runCurrent()

        assertEquals(ContenidosDocenteUiState(objetivos = listOf(objetivo)), viewModel.estado.value)
    }

    @Test
    fun guardarObjetivoInvalidoMuestra400YOmiteElCallback() {
        val invalida = solicitud.copy(codigoMineduc = "")
        coEvery { api.crearObjetivo(invalida) } returns errorHttp(400, "El código es obligatorio")
        var guardados = 0

        viewModel.guardarObjetivo(null, invalida) { guardados++ }
        dispatcher.scheduler.runCurrent()

        assertEquals("El código es obligatorio (HTTP 400)", viewModel.estado.value.error)
        assertFalse(viewModel.estado.value.cargando)
        assertTrue(viewModel.estado.value.objetivos.isEmpty())
        assertEquals(0, guardados)
    }

    @Test
    fun guardaYActualizaObjetivoConElIdDelServidorYConservaLaSeleccion() {
        coEvery { api.crearObjetivo(solicitud) } returns Response.success(201, objetivo)
        var guardados = 0
        viewModel.guardarObjetivo(null, solicitud) { guardados++ }
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(objetivo), viewModel.estado.value.objetivos)
        assertEquals(1, guardados)
        seleccionarObjetivo()

        val editado = objetivo.copy(descripcion = "Resolver sumas y restas")
        val edicion = solicitud.copy(descripcion = editado.descripcion)
        coEvery { api.actualizarObjetivo(objetivo.id, edicion) } returns Response.success(editado)
        viewModel.guardarObjetivo(objetivo.id, edicion) { guardados++ }
        dispatcher.scheduler.runCurrent()

        assertEquals(
            ContenidosDocenteUiState(listOf(editado), editado, listOf(actividad)),
            viewModel.estado.value
        )
        assertEquals(2, guardados)
    }

    @Test
    fun eliminarObjetivoConActividadesConservaLaSeleccionSiElServidorLoRechaza() {
        coEvery { api.listarObjetivos() } returns Response.success(listOf(objetivo))
        viewModel.cargarObjetivos()
        dispatcher.scheduler.runCurrent()
        seleccionarObjetivo()
        coEvery { api.eliminarObjetivo(objetivo.id) } returns errorHttp(400, "El objetivo tiene actividades")

        viewModel.eliminarObjetivo(objetivo.id)
        dispatcher.scheduler.runCurrent()

        assertEquals(
            ContenidosDocenteUiState(
                listOf(objetivo), objetivo, listOf(actividad),
                error = "El objetivo tiene actividades (HTTP 400)"
            ),
            viewModel.estado.value
        )
    }

    @Test
    fun guardaEditaYEliminaActividadSinDuplicarElRegistro() {
        val solicitudActividad = actividad.aSolicitud()
        coEvery { api.listarActividades(objetivo.id) } returns Response.success(emptyList())
        viewModel.seleccionarObjetivo(objetivo)
        dispatcher.scheduler.runCurrent()

        var guardados = 0
        coEvery { api.crearActividad(solicitudActividad) } returns Response.success(201, actividad)
        viewModel.guardarActividad(null, solicitudActividad) { guardados++ }
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(actividad), viewModel.estado.value.actividades)

        val editada = actividad.copy(titulo = "Sumas simples")
        coEvery { api.actualizarActividad(actividad.id, editada.aSolicitud()) } returns Response.success(editada)
        viewModel.guardarActividad(actividad.id, editada.aSolicitud()) { guardados++ }
        dispatcher.scheduler.runCurrent()
        assertEquals(listOf(editada), viewModel.estado.value.actividades)
        assertEquals(2, guardados)

        coEvery { api.eliminarActividad(actividad.id) } returns Response.success<Unit>(204, null)
        viewModel.eliminarActividad(actividad.id)
        dispatcher.scheduler.runCurrent()
        assertEquals(ContenidosDocenteUiState(objetivoSeleccionado = objetivo), viewModel.estado.value)
    }

    @Test
    fun editarActividadInexistenteMuestra404YConservaLaLista() {
        seleccionarObjetivo()
        val edicion = actividad.aSolicitud().copy(titulo = "Sumas simples")
        coEvery { api.actualizarActividad(actividad.id, edicion) } returns errorHttp(404, "Actividad no encontrada")
        var guardados = 0

        viewModel.guardarActividad(actividad.id, edicion) { guardados++ }
        dispatcher.scheduler.runCurrent()

        assertEquals("Actividad no encontrada (HTTP 404)", viewModel.estado.value.error)
        assertEquals(listOf(actividad), viewModel.estado.value.actividades)
        assertFalse(viewModel.estado.value.cargando)
        assertEquals(0, guardados)
    }

    @Test
    fun eliminaElObjetivoYRestableceLaSeleccionTrasLaConfirmacionDelServidor() {
        val otro = objetivo.copy(id = 18, codigoMineduc = "MA02 OA 02")
        coEvery { api.listarObjetivos() } returns Response.success(listOf(objetivo, otro))
        viewModel.cargarObjetivos()
        dispatcher.scheduler.runCurrent()
        seleccionarObjetivo()
        coEvery { api.eliminarObjetivo(objetivo.id) } returns Response.success<Unit>(204, null)

        viewModel.eliminarObjetivo(objetivo.id)
        dispatcher.scheduler.runCurrent()

        assertEquals(ContenidosDocenteUiState(objetivos = listOf(otro)), viewModel.estado.value)
    }

    @Test
    fun guardarActividadSinPreguntaMuestraLaValidacionYConservaLasActividades() {
        seleccionarObjetivo()
        val invalida = actividad.aSolicitud().copy(pregunta = "")
        coEvery { api.crearActividad(invalida) } returns errorHttp(400, "La pregunta es obligatoria")
        var guardados = 0

        viewModel.guardarActividad(null, invalida) { guardados++ }
        dispatcher.scheduler.runCurrent()

        assertEquals(
            ContenidosDocenteUiState(
                objetivoSeleccionado = objetivo,
                actividades = listOf(actividad),
                error = "La pregunta es obligatoria (HTTP 400)"
            ),
            viewModel.estado.value
        )
        assertEquals(0, guardados)
    }

    private fun seleccionarObjetivo() {
        coEvery { api.listarActividades(objetivo.id) } returns Response.success(listOf(actividad))
        viewModel.seleccionarObjetivo(objetivo)
        dispatcher.scheduler.runCurrent()
    }

    private fun <T> errorHttp(codigo: Int, mensaje: String): Response<T> {
        val cuerpo = ResponseBody.create(MediaType.get("application/json"), """{"mensaje":"$mensaje"}""")
        return Response.error(codigo, cuerpo)
    }
}
