package cl.duoc.mpazpro.viewmodel.student

import android.graphics.Bitmap
import cl.duoc.mpazpro.data.repository.DogCeoRepository
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
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExplorarPerrosViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<DogCeoRepository>()
    private val datos = byteArrayOf(1, 2, 3)

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun restaurar() {
        Dispatchers.resetMain()
    }

    @Test
    fun iniciaLaCargaYPublicaElBitmapCuandoLaConsultaTermina() {
        val respuesta = CompletableDeferred<Result<ByteArray>>()
        val imagen = mockk<Bitmap>()
        coEvery { repository.obtenerFoto() } coAnswers { respuesta.await() }

        val viewModel = crearViewModel { bytes ->
            assertArrayEquals(datos, bytes)
            imagen
        }
        dispatcher.scheduler.runCurrent()
        assertEquals(ExplorarPerrosUiState.Cargando, viewModel.estado.value)

        respuesta.complete(Result.success(datos))
        dispatcher.scheduler.runCurrent()

        assertEquals(ExplorarPerrosUiState.Foto(imagen), viewModel.estado.value)
    }

    @Test
    fun permiteReintentarTrasUnFalloDeRedSinDecodificarElFallo() {
        val imagen = mockk<Bitmap>()
        var decodificaciones = 0
        coEvery { repository.obtenerFoto() } returns Result.failure(IOException("Sin red"))
        val viewModel = crearViewModel {
            decodificaciones++
            imagen
        }
        dispatcher.scheduler.runCurrent()

        assertEquals(ExplorarPerrosUiState.Error, viewModel.estado.value)
        assertEquals(0, decodificaciones)

        coEvery { repository.obtenerFoto() } returns Result.success(datos)
        viewModel.consultar()
        assertEquals(ExplorarPerrosUiState.Cargando, viewModel.estado.value)
        dispatcher.scheduler.runCurrent()

        assertEquals(ExplorarPerrosUiState.Foto(imagen), viewModel.estado.value)
        assertEquals(1, decodificaciones)
        coVerify(exactly = 2) { repository.obtenerFoto() }
    }

    @Test
    fun muestraErrorSiLosBytesNoRepresentanUnaImagen() {
        coEvery { repository.obtenerFoto() } returns Result.success(datos)
        val viewModel = crearViewModel { null }

        dispatcher.scheduler.runCurrent()

        assertEquals(ExplorarPerrosUiState.Error, viewModel.estado.value)
    }

    @Test
    fun ignoraConsultasRepetidasMientrasHayUnaFotoEnCarga() {
        val respuesta = CompletableDeferred<Result<ByteArray>>()
        val imagen = mockk<Bitmap>()
        coEvery { repository.obtenerFoto() } coAnswers { respuesta.await() }
        val viewModel = crearViewModel { imagen }
        dispatcher.scheduler.runCurrent()

        viewModel.consultar()
        viewModel.consultar()
        dispatcher.scheduler.runCurrent()
        assertEquals(ExplorarPerrosUiState.Cargando, viewModel.estado.value)

        respuesta.complete(Result.success(datos))
        dispatcher.scheduler.runCurrent()

        coVerify(exactly = 1) { repository.obtenerFoto() }
        assertEquals(ExplorarPerrosUiState.Foto(imagen), viewModel.estado.value)
    }

    @Test
    fun alVolverAConsultarReemplazaLaFotoAnterior() {
        val siguientesDatos = byteArrayOf(4, 5, 6)
        val primeraImagen = mockk<Bitmap>()
        val segundaImagen = mockk<Bitmap>()
        coEvery { repository.obtenerFoto() } returnsMany listOf(
            Result.success(datos), Result.success(siguientesDatos)
        )
        val viewModel = crearViewModel { bytes ->
            if (bytes.contentEquals(datos)) primeraImagen else segundaImagen
        }
        dispatcher.scheduler.runCurrent()
        assertEquals(ExplorarPerrosUiState.Foto(primeraImagen), viewModel.estado.value)

        viewModel.consultar()
        assertEquals(ExplorarPerrosUiState.Cargando, viewModel.estado.value)
        dispatcher.scheduler.runCurrent()

        assertEquals(ExplorarPerrosUiState.Foto(segundaImagen), viewModel.estado.value)
    }

    @Test
    fun ofreceElConstructorPublicoQueUsaCompose() {
        assertEquals(0, ExplorarPerrosViewModel::class.java.getConstructor().parameterCount)
    }

    private fun crearViewModel(decodificar: (ByteArray) -> Bitmap?) =
        ExplorarPerrosViewModel(repository, dispatcher, decodificar)
}
