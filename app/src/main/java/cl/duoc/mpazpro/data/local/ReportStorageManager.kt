package cl.duoc.mpazpro.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import org.json.JSONArray
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity
import cl.duoc.mpazpro.data.local.entities.ActividadEntity
import cl.duoc.mpazpro.data.local.entities.ObjetivoAprendizajeEntity

/**
 * Gestor utilitario de almacenamiento interno y exportacion de archivos
 *
 * Utiliza context.filesDir para persistencia interna privada bajo el sandbox de Android/Linux
 * sin requerir permisos peligrosos en el manifiesto.
 */
object ReportStorageManager {

    /**
     * Genera el Reporte Académico Integral del Estudiante en formato JSON.
     * Unifica todas las evaluaciones reales rendidas por el estudiante en Segundo Básico.
     */
    fun generarReporteAcademicoEstudiante(
        context: Context,
        nombreEstudiante: String,
        curso: String = "2.° Básico A",
        resultados: List<ResultadoPruebaEntity> = emptyList()
    ): String {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val root = JSONObject()

        // 1. Institución
        val institucion = JSONObject().apply {
            put("escuela", "Escuela Escritora Marcela Paz")
            put("comuna", "San Bernardo")
            put("region", "Región Metropolitana")
            put("rbd", "10203-4")
            put("nivelEducativo", "Segundo Básico")
            put("curso", curso)
            put("periodoAcademico", "Año Escolar 2026")
        }
        root.put("institucion", institucion)

        // 2. Estudiante
        val estudiante = JSONObject().apply {
            put("nombreCompleto", nombreEstudiante)
            put("curso", curso)
            put("totalEvaluacionesRegistradas", resultados.size)
        }
        root.put("estudiante", estudiante)

        // 3. Resumen Global de Rendimiento
        val mapMaterias = mapOf(
            1 to Pair("Lenguaje y Comunicación", "LEN-F01"),
            2 to Pair("Matemática", "MAT-F01"),
            3 to Pair("Ciencias Naturales", "CIE-F01"),
            4 to Pair("Historia y Geografía", "HIS-F01")
        )

        val cantidad = resultados.size
        val promedio = if (resultados.isNotEmpty()) {
            Math.round(resultados.map { it.puntaje }.average() * 10.0) / 10.0
        } else {
            0.0
        }
        val nivelDesempeno = when {
            resultados.isEmpty() -> "Sin evaluaciones rendidas aún"
            promedio >= 85.0 -> "Destacado (Nivel 4)"
            promedio >= 70.0 -> "Adecuado (Nivel 3)"
            promedio >= 50.0 -> "Elemental (Nivel 2)"
            else -> "Insuficiente (Nivel 1)"
        }
        val aprobados = resultados.count { it.puntaje >= 60 }
        val requierenRefuerzo = resultados.count { it.puntaje < 60 }

        val resumenGlobal = JSONObject().apply {
            put("cantidadEvaluaciones", cantidad)
            put("promedioLogroGlobal", promedio)
            put("escalaPorcentual", "0% a 100%")
            put("nivelDesempeno", nivelDesempeno)
            put("evaluacionesAprobadas", aprobados)
            put("evaluacionesRequierenRefuerzo", requierenRefuerzo)
            put("estadoPedagogico", if (resultados.isNotEmpty())
                "Reporte integral basado en las actividades reales del estudiante en 2.° Básico."
            else
                "El estudiante aún no ha rendido actividades en la aplicación.")
        }
        root.put("resumenRendimiento", resumenGlobal)

        // 4. Desglose por Asignatura
        val desgloseArray = JSONArray()
        val porMateria = resultados.groupBy { it.oaId }
        porMateria.forEach { (oaId, registros) ->
            val info = mapMaterias[oaId] ?: Pair("Asignatura OA $oaId", "OA-$oaId")
            val promMateria = Math.round(registros.map { it.puntaje }.average() * 10.0) / 10.0
            val nivelMateria = when {
                promMateria >= 85.0 -> "Destacado"
                promMateria >= 70.0 -> "Logrado"
                promMateria >= 50.0 -> "En Desarrollo"
                else -> "Por Lograr"
            }
            val matObj = JSONObject().apply {
                put("asignatura", info.first)
                put("codigoMineduc", info.second)
                put("evaluacionesRendidas", registros.size)
                put("promedioLogro", "$promMateria%")
                put("calificacionConcepto", nivelMateria)
            }
            desgloseArray.put(matObj)
        }
        root.put("desglosePorAsignatura", desgloseArray)

        // 5. Historial Detallado de Evaluaciones
        val historialArray = JSONArray()
        resultados.forEach { res ->
            val fechaFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(res.fechaRegistro))
            val info = mapMaterias[res.oaId] ?: Pair("Asignatura OA ${res.oaId}", "OA-${res.oaId}")
            val concepto = when {
                res.puntaje >= 85 -> "Destacado"
                res.puntaje >= 70 -> "Logrado"
                res.puntaje >= 50 -> "En Desarrollo"
                else -> "Por Lograr"
            }
            val resObj = JSONObject().apply {
                put("registroId", res.id)
                put("asignatura", info.first)
                put("codigoOA", info.second)
                put("puntajeObtenido", res.puntaje)
                put("porcentajeLogro", "${res.puntaje}%")
                put("intentosRealizados", res.intentosUsados)
                put("fechaRendicion", fechaFormat)
                put("calificacionConcepto", concepto)
            }
            historialArray.put(resObj)
        }
        root.put("historialEvaluaciones", historialArray)

        // 6. Registro Institucional y Auditoría
        val registro = JSONObject().apply {
            put("sistema", "MPAZ PRO 2.0")
            put("tipoReporte", "Reporte Académico Integral del Estudiante")
            put("area", "Unidad Técnica Pedagógica (UTP)")
            put("fechaEmision", fechaActual)
            put("validez", "Documento Académico Oficial - Escuela Marcela Paz")
        }
        root.put("registroInstitucional", registro)

        return root.toString(2)
    }

    /**
     * Genera la estructura del reporte academico en formato JSON para Segundo Basico
     * Garantiza una estructura enriquecida, estadísticamente válida y pedagógicamente completa por asignatura.
     */
    fun generarContenidoReporte(
        docente: String,
        asignatura: String,
        curso: String = "2.° Básico A",
        resultados: List<ResultadoPruebaEntity> = emptyList(),
        oaId: Int = 2,
        oaEntity: ObjetivoAprendizajeEntity? = null,
        actividades: List<ActividadEntity> = emptyList(),
        config: EvaluacionConfig? = null
    ): String {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val root = JSONObject()

        // 1. Identificación Institucional
        val institucion = JSONObject().apply {
            put("escuela", "Escuela Escritora Marcela Paz")
            put("comuna", "San Bernardo")
            put("region", "Región Metropolitana")
            put("rbd", "10203-4")
            put("nivelEducativo", "Segundo Básico")
            put("curso", curso)
            put("periodoAcademico", "Año Escolar 2026")
        }
        root.put("institucion", institucion)

        // 2. Información Curricular y Asignatura
        val codigoMineduc = oaEntity?.codigoMineduc ?: when (oaId) {
            1 -> "LEN-F01"
            2 -> "MAT-F01"
            3 -> "CIE-F01"
            4 -> "HIS-F01"
            else -> "OA-0$oaId"
        }
        val descripcionCurricular = oaEntity?.descripcion ?: when (oaId) {
            1 -> "Comprender textos breves y vocabulario contextual en lectura inicial"
            2 -> "Resolver sumas y restas básicas en el ámbito numérico del 0 al 100"
            3 -> "Reconocer características de seres vivos y necesidades del entorno natural"
            4 -> "Identificar elementos representativos del entorno social y geográfico local"
            else -> "Objetivo de Aprendizaje curricular para Segundo Básico"
        }

        val asignaturaObj = JSONObject().apply {
            put("nombre", asignatura)
            put("oaId", oaId)
            put("codigoMineduc", codigoMineduc)
            put("descripcionCurricular", descripcionCurricular)
            put("estadoCurricular", "Priorización Curricular Mineduc")
        }
        root.put("asignatura", asignaturaObj)

        // 3. Parámetros y Configuración Docente
        val parametrosDocente = JSONObject().apply {
            put("docenteEvaluador", docente)
            put("codigoDocente", config?.codigoDocente?.ifEmpty { "DOC-MPAZ-${oaId}0" } ?: "DOC-MPAZ-${oaId}0")
            put("fechaLimite", config?.fechaLimite?.ifEmpty { "30/11/2026" } ?: "30/11/2026")
            put("maxIntentosPermitidos", config?.maxIntentos ?: 3)
            put("permitirPistasPedagogicas", config?.permitirPistas ?: true)
            put("modalidadEvaluacion", "Formativa y Diagnóstica Digital")
        }
        root.put("parametrosDocente", parametrosDocente)

        // 4. Resumen de Rendimiento y Análisis Pedagógico
        val cantidadRegistros = resultados.size
        val promedioGlobal = if (resultados.isNotEmpty()) {
            resultados.map { it.puntaje }.average()
        } else {
            0.0
        }
        val nivelDesempeno = when {
            resultados.isEmpty() -> "Sin Evaluaciones Registradas"
            promedioGlobal >= 85.0 -> "Destacado (Nivel 4)"
            promedioGlobal >= 70.0 -> "Adecuado (Nivel 3)"
            promedioGlobal >= 50.0 -> "Elemental (Nivel 2)"
            else -> "Insuficiente (Nivel 1)"
        }
        val totalAprobados = resultados.count { it.puntaje >= 60 }
        val totalRefuerzo = resultados.count { it.puntaje < 60 }
        val diagnosticoPedagogico = when {
            resultados.isEmpty() -> "Instrumento curricular habilitado y en espera de rendición por los estudiantes de 2.° Básico."
            promedioGlobal >= 70.0 -> "El grupo curso demuestra un logro satisfactorio en los objetivos priorizados para Segundo Básico."
            promedioGlobal >= 50.0 -> "Logro elemental en desarrollo; se recomienda reforzar actividades prácticas antes de la evaluación sumativa."
            else -> "Rendimiento bajo el umbral mínimo; se sugiere intervención pedagógica focalizada en el OA $oaId."
        }

        val resumenEvaluacion = JSONObject().apply {
            put("cantidadRegistros", cantidadRegistros)
            put("porcentajeLogroGlobal", if (resultados.isNotEmpty()) Math.round(promedioGlobal * 10.0) / 10.0 else 0.0)
            put("escalaPorcentual", "0% a 100%")
            put("nivelDesempeno", nivelDesempeno)
            put("estudiantesAprobados", totalAprobados)
            put("estudiantesRequierenRefuerzo", totalRefuerzo)
            put("diagnosticoPedagogico", diagnosticoPedagogico)
            put("estadoProceso", if (resultados.isNotEmpty()) "VÁLIDO CON REGISTROS" else "HABILITADO PARA RENDICIÓN")
        }
        root.put("resumenRendimiento", resumenEvaluacion)

        // Compatibilidad con esquema anterior
        val objetivosArray = JSONArray()
        val resumenOa = JSONObject().apply {
            put("codigoOA", codigoMineduc)
            put("logro", if (resultados.isNotEmpty()) Math.round(promedioGlobal * 10.0) / 10.0 else 0.0)
            put("cantidadRegistros", cantidadRegistros)
            put("descripcion", descripcionCurricular)
        }
        objetivosArray.put(resumenOa)
        root.put("objetivosAprendizaje", objetivosArray)

        // 5. Detalle de Evaluaciones por Estudiante
        val detalleArray = JSONArray()
        resultados.forEach { res ->
            val fechaFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(res.fechaRegistro))
            val concepto = when {
                res.puntaje >= 85 -> "Destacado"
                res.puntaje >= 70 -> "Logrado"
                res.puntaje >= 50 -> "En Desarrollo"
                else -> "Por Lograr"
            }
            val item = JSONObject().apply {
                put("registroId", res.id)
                put("identificadorEstudiante", "Estudiante #${res.estudianteFicticioId} (2.° Básico)")
                put("puntajeObtenido", res.puntaje)
                put("porcentajeLogro", "${res.puntaje}%")
                put("intentosRealizados", res.intentosUsados)
                put("fechaRendicion", fechaFormat)
                put("calificacionConcepto", concepto)
            }
            detalleArray.put(item)
        }
        root.put("detalleEvaluaciones", detalleArray)

        // 6. Banco de Actividades Curriculares Evaluadas
        val bancoArray = JSONArray()
        actividades.forEach { act ->
            val actObj = JSONObject().apply {
                put("actividadId", act.id)
                put("titulo", act.titulo)
                put("tipoDesafio", act.tipoDesafio)
                put("pregunta", act.pregunta)
                put("respuestaCorrecta", act.respuestaCorrecta)
                val altArray = JSONArray().apply {
                    put(act.respuestaCorrecta)
                    put(act.alternativaIncorrecta1)
                    put(act.alternativaIncorrecta2)
                    put(act.alternativaIncorrecta3)
                }
                put("alternativasDisponibles", altArray)
            }
            bancoArray.put(actObj)
        }
        root.put("bancoActividadesEvaluadas", bancoArray)

        // 7. Registro Institucional y Auditoria
        val registroInstitucional = JSONObject().apply {
            put("sistema", "MPAZ PRO 2.0")
            put("tipoReporte", "Reporte de Rendimiento Escolar por Asignatura")
            put("area", "Unidad Técnica Pedagógica (UTP)")
            put("fechaEmision", fechaActual)
            put("firmaDigital", "Validado digitalmente - Escuela Marcela Paz San Bernardo")
        }
        root.put("registroInstitucional", registroInstitucional)

        return root.toString(2)
    }

    /**
     * Guarda el reporte en el almacenamiento interno privado de la aplicacion (context.filesDir)
     * No requiere permisos peligrosos como WRITE_EXTERNAL_STORAGE.
     */
    fun guardarEnAlmacenamientoInterno(
        context: Context,
        nombreArchivo: String,
        contenido: String
    ): Result<File> {
        return runCatching {

            val nombreBase = nombreArchivo.trim()

            require(nombreBase.isNotEmpty()) {
                "El nombre del archivo no puede estar vacío."
            }

            require(
                !nombreBase.contains("/") &&
                        !nombreBase.contains("\\") &&
                        nombreBase != "." &&
                        nombreBase != ".."
            ) {
                "El nombre del archivo contiene caracteres no permitidos."
            }

            val nombreLimpio = if (
                nombreBase.endsWith(".json", ignoreCase = true) ||
                nombreBase.endsWith(".txt", ignoreCase = true)
            ) {
                nombreBase
            } else {
                "$nombreBase.json"
            }


            val archivo = File(context.filesDir, nombreLimpio)
            FileOutputStream(archivo).use { output ->
                output.write(contenido.toByteArray(Charsets.UTF_8))
            }
            archivo
        }
    }

    /**
     * Exporta el contenido a una ubicacion elegida por el usuario mediante SAF (Storage Access Framework)
     */

    /**
     * Exporta un reporte a la ubicación elegida por el usuario.
     * Utiliza Storage Access Framework (SAF).
     */
    fun exportarConSAF(
        context: Context,
        uri: Uri,
        contenido: String
    ): Result<Boolean> {
        return runCatching {

            // Abrimos el archivo seleccionado por el usuario.
            val outputStream = context.contentResolver
                .openOutputStream(uri, "wt")
                ?: error("No se pudo abrir el archivo para exportar.")

            // Escribimos el contenido y cerramos el archivo.
            outputStream.use { archivo ->
                archivo.write(contenido.toByteArray(Charsets.UTF_8))
                archivo.flush()
            }

            // Confirmamos que la escritura terminó.
            true
        }
    }


    /**
     * Lista los reportes guardados en el almacenamiento interno privado
     */
    fun listarArchivosReporte(context: Context): List<File> {
        val directorio = context.filesDir
        return directorio.listFiles { file ->
            file.isFile && (file.name.endsWith(".json") || file.name.endsWith(".txt"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Lee el contenido completo de un archivo local
     */
    fun leerContenidoArchivo(archivo: File): String {
        return if (archivo.exists()) {
            archivo.readText(Charsets.UTF_8)
        } else {
            "El archivo no existe."
        }
    }

    /**
     * Elimina un archivo del almacenamiento interno
     */
    fun eliminarArchivo(archivo: File): Boolean {
        return if (archivo.exists()) archivo.delete() else false
    }
}
