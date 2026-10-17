
package cl.duoc.mpazpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "objetivos_aprendizaje")
data class ObjetivoAprendizajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val asignaturaId: Int,
    val codigoMineduc: String,
    val descripcion: String,
    val autorizado: Boolean = false
)
