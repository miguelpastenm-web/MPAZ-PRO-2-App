package cl.duoc.mpazpro

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.database.entities.ActividadEntity
import cl.duoc.mpazpro.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun configurar() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        repository = AppRepository(database)
    }

    @After
    fun cerrar() {
        database.close()
    }

    @Test
    fun insertarYConsultarActividad() = runBlocking {

        val actividad = ActividadEntity(
            oaId = 1,
            titulo = "Prueba de Matemática",
            tipoDesafio = "SeleccionMultiple",
            pregunta = "¿Cuánto es 2 + 2?",
            respuestaCorrecta = "4",
            alternativaIncorrecta1 = "3",
            alternativaIncorrecta2 = "5",
            alternativaIncorrecta3 = "6"
        )

        repository.insertarActividad(actividad)

        val actividades =
            repository.obtenerActividades().first()

        assertEquals(1, actividades.size)
        assertEquals("4", actividades[0].respuestaCorrecta)
        assertEquals("3", actividades[0].alternativaIncorrecta1)
    }

    @Test
    fun consultarActividadesPorObjetivo() = runBlocking {

        repository.insertarActividad(
            ActividadEntity(
                oaId = 2,
                titulo = "Sumas",
                tipoDesafio = "SeleccionMultiple",
                pregunta = "¿Cuánto es 5 + 3?",
                respuestaCorrecta = "8",
                alternativaIncorrecta1 = "6",
                alternativaIncorrecta2 = "7",
                alternativaIncorrecta3 = "9"
            )
        )

        val actividades =
            repository.obtenerActividadesPorObjetivo(2).first()

        assertEquals(1, actividades.size)
        assertEquals(2, actividades[0].oaId)
    }
}
