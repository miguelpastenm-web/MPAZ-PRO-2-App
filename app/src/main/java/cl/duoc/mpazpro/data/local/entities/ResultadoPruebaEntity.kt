package cl.duoc.mpazpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resultados_prueba")
data class ResultadoPruebaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val estudianteFicticioId: Int,
    val oaId: Int,
    val puntaje: Int,
    val intentosUsados: Int,
    val fechaRegistro: Long
)
