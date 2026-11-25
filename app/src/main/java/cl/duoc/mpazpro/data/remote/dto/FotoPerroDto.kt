package cl.duoc.mpazpro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FotoPerroDto(
    @SerializedName("message") val url: String?,
    @SerializedName("status") val estado: String?
)
