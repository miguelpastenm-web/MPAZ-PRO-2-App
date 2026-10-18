package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.mpazpro.data.local.dao.ActividadDao
import cl.duoc.mpazpro.data.local.dao.DocenteDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoAprendizajeDao
import cl.duoc.mpazpro.data.local.dao.ResultadoPruebaDao
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.entities.DocenteEntity
import cl.duoc.mpazpro.data.local.entities.ObjetivoAprendizajeEntity
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity

@Database(
    entities = [
        DocenteEntity::class,
        ObjetivoAprendizajeEntity::class,
        ActividadEntity::class,
        ResultadoPruebaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao
    abstract fun objetivoAprendizajeDao(): ObjetivoAprendizajeDao
    abstract fun actividadDao(): ActividadDao
    abstract fun resultadoPruebaDao(): ResultadoPruebaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_pro_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
