
package cl.duoc.mpazpro.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatosInicialesCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Objetivos de aprendizaje ficticios
        val objetivos = listOf(
            Triple(1, "LEN-F01", "Comprender textos breves"),
            Triple(2, "MAT-F01", "Resolver sumas básicas"),
            Triple(3, "CIE-F01", "Reconocer seres vivos"),
            Triple(4, "HIS-F01", "Identificar elementos del entorno")
        )

        objetivos.forEach { (id, codigo, descripcion) ->
            db.execSQL(
                """
                INSERT INTO objetivos_aprendizaje
                (id, asignaturaId, codigoMineduc, descripcion, autorizado)
                VALUES (?, ?, ?, ?, 1)
                """.trimIndent(),
                arrayOf(id, id, codigo, descripcion)
            )
        }

        // Actividades ficticias

        // Actividades ficticias con cuatro alternativas
        data class PreguntaInicial(
            val oaId: Int,
            val pregunta: String,
            val correcta: String,
            val incorrecta1: String,
            val incorrecta2: String,
            val incorrecta3: String
        )

        val actividades = listOf(
            PreguntaInicial(1, "¿Qué animal ladra?", "Perro", "Gato", "Vaca", "Pez"),
            PreguntaInicial(1, "¿Cuál es la primera vocal?", "A", "E", "I", "O"),

            PreguntaInicial(2, "¿Cuánto es 5 + 3?", "8", "6", "7", "9"),
            PreguntaInicial(2, "¿Cuánto es 10 - 4?", "6", "5", "7", "8"),

            PreguntaInicial(3, "¿Qué necesitan las plantas para vivir?", "Agua", "Plástico", "Vidrio", "Metal"),
            PreguntaInicial(3, "¿Qué animal puede volar?", "Pájaro", "Perro", "Gato", "Caballo"),

            PreguntaInicial(4, "¿Dónde estudian los niños?", "Escuela", "Hospital", "Supermercado", "Estadio"),
            PreguntaInicial(4, "¿Qué usamos para orientarnos?", "Mapa", "Cuchara", "Pelota", "Lápiz")
        )

        actividades.forEachIndexed { index, actividad ->
            db.execSQL(
                """
        INSERT INTO actividades
        (oaId, titulo, tipoDesafio, pregunta, respuestaCorrecta,
         alternativaIncorrecta1, alternativaIncorrecta2, alternativaIncorrecta3)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
                arrayOf(
                    actividad.oaId,
                    "Actividad ${index + 1}",
                    "SeleccionMultiple",
                    actividad.pregunta,
                    actividad.correcta,
                    actividad.incorrecta1,
                    actividad.incorrecta2,
                    actividad.incorrecta3
                )
            )
        }


    }
    }

