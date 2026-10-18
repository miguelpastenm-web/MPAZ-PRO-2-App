package cl.duoc.mpazpro.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            ALTER TABLE actividades
            ADD COLUMN alternativaIncorrecta1 TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE actividades
            ADD COLUMN alternativaIncorrecta2 TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )

        db.execSQL(
            """
            ALTER TABLE actividades
            ADD COLUMN alternativaIncorrecta3 TEXT NOT NULL DEFAULT ''
            """.trimIndent()
        )


// Completar alternativas de las actividades existentes
        val alternativas = listOf(
            listOf("¿Qué animal ladra?", "Gato", "Vaca", "Pez"),
            listOf("¿Cuál es la primera vocal?", "E", "I", "O"),
            listOf("¿Cuánto es 5 + 3?", "6", "7", "9"),
            listOf("¿Cuánto es 10 - 4?", "5", "7", "8"),
            listOf("¿Qué necesitan las plantas para vivir?", "Plástico", "Vidrio", "Metal"),
            listOf("¿Qué animal puede volar?", "Perro", "Gato", "Caballo"),
            listOf("¿Dónde estudian los niños?", "Hospital", "Supermercado", "Estadio"),
            listOf("¿Qué usamos para orientarnos?", "Cuchara", "Pelota", "Lápiz")
        )

        alternativas.forEach { datos ->
            db.execSQL(
                """
        UPDATE actividades
        SET alternativaIncorrecta1 = ?,
            alternativaIncorrecta2 = ?,
            alternativaIncorrecta3 = ?
        WHERE pregunta = ?
        """.trimIndent(),
                arrayOf(datos[1], datos[2], datos[3], datos[0])
            )
        }

    }
}
