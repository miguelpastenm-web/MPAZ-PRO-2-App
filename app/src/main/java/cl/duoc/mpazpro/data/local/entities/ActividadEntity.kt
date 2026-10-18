package cl.duoc.mpazpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val oaId: Int,
    val titulo: String,
    val tipoDesafio: String,
    val pregunta: String,
    val respuestaCorrecta: String,

    @ColumnInfo(defaultValue = "''")
    val alternativaIncorrecta1: String = "",

    @ColumnInfo(defaultValue = "''")
    val alternativaIncorrecta2: String = "",

    @ColumnInfo(defaultValue = "''")
    val alternativaIncorrecta3: String = ""

)
