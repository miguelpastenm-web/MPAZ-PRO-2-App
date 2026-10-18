package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(actividad: ActividadEntity)

    @Update
    suspend fun actualizar(actividad: ActividadEntity)

    @Delete
    suspend fun eliminar(actividad: ActividadEntity)

    @Query("SELECT * FROM actividades")
    fun obtenerTodas(): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE oaId = :oaId")
    fun obtenerPorObjetivo(oaId: Int): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE tipoDesafio = :tipo")
    fun obtenerPorTipo(tipo: String): Flow<List<ActividadEntity>>
}
