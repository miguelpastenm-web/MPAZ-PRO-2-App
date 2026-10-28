
package cl.duoc.mpazpro.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "docentes")
data class DocenteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correoFicticio: String,
    val asignatura: String
)
