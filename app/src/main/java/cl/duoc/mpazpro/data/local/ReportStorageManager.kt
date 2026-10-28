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
}
