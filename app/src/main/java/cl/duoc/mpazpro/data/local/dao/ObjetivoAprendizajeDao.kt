package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import cl.duoc.mpazpro.data.local.entities.ObjetivoAprendizajeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjetivoAprendizajeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(objetivo: ObjetivoAprendizajeEntity)

    @Update
    suspend fun actualizar(objetivo: ObjetivoAprendizajeEntity)

    @Delete
    suspend fun eliminar(objetivo: ObjetivoAprendizajeEntity)

    @Query("SELECT * FROM objetivos_aprendizaje")
    fun obtenerTodos(): Flow<List<ObjetivoAprendizajeEntity>>

    @Query("SELECT * FROM objetivos_aprendizaje WHERE asignaturaId = :asignaturaId")
    fun obtenerPorAsignatura(asignaturaId: Int): Flow<List<ObjetivoAprendizajeEntity>>

    @Query("SELECT * FROM objetivos_aprendizaje WHERE autorizado = 1")
    fun obtenerAutorizados(): Flow<List<ObjetivoAprendizajeEntity>>

    @Query("SELECT * FROM objetivos_aprendizaje WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ObjetivoAprendizajeEntity?
}
