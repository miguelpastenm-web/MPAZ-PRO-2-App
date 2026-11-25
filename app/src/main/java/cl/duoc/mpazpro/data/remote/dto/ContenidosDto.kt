package cl.duoc.mpazpro.data.remote.dto

data class ObjetivoDto(
    val id: Int,
    val asignaturaId: Int,
    val codigoMineduc: String,
    val descripcion: String,
    val autorizado: Boolean
)

data class ObjetivoRequest(
    val asignaturaId: Int,
    val codigoMineduc: String,
    val descripcion: String,
    val autorizado: Boolean
)

data class ActividadDto(
    val id: Int,
    val oaId: Int,
    val titulo: String,
    val tipoDesafio: String,
    val pregunta: String,
    val respuestaCorrecta: String,
    val alternativaIncorrecta1: String?,
    val alternativaIncorrecta2: String?,
    val alternativaIncorrecta3: String?
) {
    fun aSolicitud() = ActividadRequest(
        oaId, titulo, tipoDesafio, pregunta, respuestaCorrecta,
        alternativaIncorrecta1.orEmpty(), alternativaIncorrecta2.orEmpty(),
        alternativaIncorrecta3.orEmpty()
    )
}

data class ActividadRequest(
    val oaId: Int,
    val titulo: String,
    val tipoDesafio: String,
    val pregunta: String,
    val respuestaCorrecta: String,
    val alternativaIncorrecta1: String,
    val alternativaIncorrecta2: String,
    val alternativaIncorrecta3: String
)
