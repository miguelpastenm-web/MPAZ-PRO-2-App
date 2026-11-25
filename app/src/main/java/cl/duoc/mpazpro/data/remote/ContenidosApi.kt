package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.remote.dto.ActividadDto
import cl.duoc.mpazpro.data.remote.dto.ActividadRequest
import cl.duoc.mpazpro.data.remote.dto.ObjetivoDto
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import retrofit2.Response
import retrofit2.http.*

interface ContenidosApi {
    @GET("api/objetivos")
    suspend fun listarObjetivos(): Response<List<ObjetivoDto>>

    @GET("api/objetivos/{id}")
    suspend fun obtenerObjetivo(@Path("id") id: Int): Response<ObjetivoDto>

    @POST("api/objetivos")
    suspend fun crearObjetivo(@Body objetivo: ObjetivoRequest): Response<ObjetivoDto>

    @PUT("api/objetivos/{id}")
    suspend fun actualizarObjetivo(@Path("id") id: Int, @Body objetivo: ObjetivoRequest): Response<ObjetivoDto>

    @DELETE("api/objetivos/{id}")
    suspend fun eliminarObjetivo(@Path("id") id: Int): Response<Unit>

    @GET("api/actividades")
    suspend fun listarActividades(@Query("oaId") oaId: Int?): Response<List<ActividadDto>>

    @GET("api/actividades/{id}")
    suspend fun obtenerActividad(@Path("id") id: Int): Response<ActividadDto>

    @POST("api/actividades")
    suspend fun crearActividad(@Body actividad: ActividadRequest): Response<ActividadDto>

    @PUT("api/actividades/{id}")
    suspend fun actualizarActividad(@Path("id") id: Int, @Body actividad: ActividadRequest): Response<ActividadDto>

    @DELETE("api/actividades/{id}")
    suspend fun eliminarActividad(@Path("id") id: Int): Response<Unit>
}
