
package cl.duoc.mpazpro.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import cl.duoc.mpazpro.data.local.database.entities.DocenteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocenteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(docente: DocenteEntity)

    @Update
    suspend fun actualizar(docente: DocenteEntity)

    @Delete
    suspend fun eliminar(docente: DocenteEntity)

    @Query("SELECT * FROM docentes")
    fun obtenerTodos(): Flow<List<DocenteEntity>>

    @Query("SELECT * FROM docentes WHERE id = :id")
    suspend fun obtenerPorId(id: Int): DocenteEntity?
}
