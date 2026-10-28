package cl.duoc.mpazpro.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import cl.duoc.mpazpro.data.local.database.entities.ResultadoPruebaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultadoPruebaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(resultado: ResultadoPruebaEntity)

    // Guarda varios resultados dentro de una misma transacción.
    // Si alguna inserción falla, Room revierte toda la operación.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(resultados: List<ResultadoPruebaEntity>)


    @Update
    suspend fun actualizar(resultado: ResultadoPruebaEntity)

    @Delete
    suspend fun eliminar(resultado: ResultadoPruebaEntity)

    @Query("SELECT * FROM resultados_prueba")
    fun obtenerTodos(): Flow<List<ResultadoPruebaEntity>>

    @Query("SELECT * FROM resultados_prueba WHERE estudianteFicticioId = :estudianteId")
    fun obtenerPorEstudiante(estudianteId: Int): Flow<List<ResultadoPruebaEntity>>

    @Query("SELECT * FROM resultados_prueba WHERE oaId = :oaId")
    fun obtenerPorObjetivo(oaId: Int): Flow<List<ResultadoPruebaEntity>>

    @Query("SELECT * FROM resultados_prueba ORDER BY fechaRegistro DESC")
    fun obtenerHistorial(): Flow<List<ResultadoPruebaEntity>>

    @Query("DELETE FROM resultados_prueba")
    suspend fun borrarTodos()
}
