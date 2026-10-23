
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

/**
 * Base de datos local de MPAZ PRO.
 *
 * Utilizamos Room para almacenar docentes, objetivos de
 * aprendizaje, actividades y resultados de evaluaciones.
 *
 * La versión 2 incluye los cambios realizados en las
 * actividades mediante una migración.
 */
@Database(
    entities = [
        DocenteEntity::class,
        ObjetivoAprendizajeEntity::class,
        ActividadEntity::class,
        ResultadoPruebaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Cada DAO contiene las operaciones para consultar
    // y modificar la información de su tabla.
    abstract fun docenteDao(): DocenteDao
    abstract fun objetivoAprendizajeDao(): ObjetivoAprendizajeDao
    abstract fun actividadDao(): ActividadDao
    abstract fun resultadoPruebaDao(): ResultadoPruebaDao

    companion object {

        // Guardamos una única instancia de la base de datos.
        // Volatile permite que los hilos vean el valor actualizado.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la base de datos existente o la crea
         * si todavía no se ha inicializado.
         */
        fun getDatabase(context: Context): AppDatabase {

            // Si ya existe una instancia, la reutilizamos.
            // synchronized evita crear varias instancias
            // simultáneamente desde diferentes hilos.
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_pro_database"
                )
                    // Actualiza la estructura desde la versión 1 a la 2.
                    .addMigrations(MIGRATION_1_2)

                    // Permite cargar los datos iniciales del proyecto.
                    .addCallback(DatosInicialesCallback())

                    // Construye la instancia de Room.
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
