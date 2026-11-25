package cl.duoc.mpazpro.data.repository

import cl.duoc.mpazpro.data.remote.ContenidosApi
import cl.duoc.mpazpro.data.remote.RetrofitClient
import cl.duoc.mpazpro.data.remote.dto.ActividadRequest
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import kotlinx.coroutines.CancellationException
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

class ContenidosRemoteRepository(
    private val api: ContenidosApi = RetrofitClient.contenidos.create(ContenidosApi::class.java)
) {
    suspend fun listarObjetivos() = ejecutar { api.listarObjetivos() }
    suspend fun obtenerObjetivo(id: Int) = ejecutar { api.obtenerObjetivo(id) }
    suspend fun crearObjetivo(objetivo: ObjetivoRequest) = ejecutar { api.crearObjetivo(objetivo) }
    suspend fun actualizarObjetivo(id: Int, objetivo: ObjetivoRequest) =
        ejecutar { api.actualizarObjetivo(id, objetivo) }
    suspend fun eliminarObjetivo(id: Int) = ejecutar(Unit) { api.eliminarObjetivo(id) }

    suspend fun listarActividades(oaId: Int? = null) = ejecutar { api.listarActividades(oaId) }
    suspend fun obtenerActividad(id: Int) = ejecutar { api.obtenerActividad(id) }
    suspend fun crearActividad(actividad: ActividadRequest) = ejecutar { api.crearActividad(actividad) }
    suspend fun actualizarActividad(id: Int, actividad: ActividadRequest) =
        ejecutar { api.actualizarActividad(id, actividad) }
    suspend fun eliminarActividad(id: Int) = ejecutar(Unit) { api.eliminarActividad(id) }

    private suspend fun <T : Any> ejecutar(
        valorSinCuerpo: T? = null,
        llamada: suspend () -> Response<T>
    ): Result<T> = try {
        val respuesta = llamada()
        if (respuesta.isSuccessful) {
            Result.success(respuesta.body() ?: valorSinCuerpo
                ?: error("ms-contenidos respondió sin datos."))
        } else {
            val codigo = respuesta.code()
            val detalle = runCatching {
                respuesta.errorBody()?.use { JsonParser.parseString(it.string()).asJsonObject.get("mensaje")?.asString }
            }.getOrNull()?.takeIf { it.isNotBlank() }
            val mensaje = when (codigo) {
                400 -> detalle ?: "Revisa los datos ingresados."
                404 -> detalle ?: "El contenido ya no existe en el servidor."
                else -> "Error de ms-contenidos."
            }
            Result.failure(IllegalStateException("$mensaje (HTTP $codigo)"))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(ConexionContenidosException(e))
    } catch (e: Exception) {
        Result.failure(IllegalStateException("Respuesta inválida de ms-contenidos.", e))
    }
}

class ConexionContenidosException(causa: IOException) :
    IOException("No se pudo conectar con ms-contenidos.", causa)
