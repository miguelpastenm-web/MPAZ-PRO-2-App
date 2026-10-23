package cl.duoc.mpazpro.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity

/**
 * Gestor utilitario de almacenamiento interno y exportacion de archivos
 *
 * Utiliza context.filesDir para persistencia interna privada bajo el sandbox de Android/Linux
 * sin requerir permisos peligrosos en el manifiesto.
 */
object ReportStorageManager {



    /**
     * Genera la estructura del reporte academico en formato JSON para Segundo Basico
     */
    fun generarContenidoReporte(
        docente: String,
        asignatura: String,
        curso: String = "2.° Básico A",
        resultados: List<ResultadoPruebaEntity> = emptyList()
    ): String {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        // Calculamos estadísticas a partir de los registros reales.
        val cantidadRegistros = resultados.size

        val promedioGlobal = if (resultados.isNotEmpty()) {
            resultados.map { it.puntaje }.average()
        } else {
            null
        }

// Agrupamos los resultados por objetivo de aprendizaje.
        val promediosPorObjetivo = resultados

            .groupBy { it.oaId }
            .mapValues { (_, registros) ->
                registros.map { it.puntaje }.average()
            }
        // Generamos los resultados de cada OA en formato JSON.
        val objetivosJson = resultados
            .groupBy { it.oaId }
            .map { (oaId, registros) ->
                val promedio = registros.map { it.puntaje }.average()

                JSONObject().apply {
                    put("codigoOA", "OA_$oaId")
                    put("logro", promedio)
                    put("cantidadRegistros", registros.size)
                }
            }

        val objetivosArray = org.json.JSONArray()
        objetivosJson.forEach { objetivosArray.put(it) }


        return """{
  "escuela": "Escuela Escritora Marcela Paz",
  "comuna": "San Bernardo",
  "nivelEducativo": "Segundo Básico",
  "curso": ${JSONObject.quote(curso)},
  "docenteEvaluador": ${JSONObject.quote(docente)},
  "asignatura": ${JSONObject.quote(asignatura)},
  "fechaEmision": ${JSONObject.quote(fechaActual)},
  "resumenEvaluacion": {
    "cantidadRegistros": $cantidadRegistros,
    "porcentajeLogroGlobal": ${promedioGlobal ?: "null"},
    "estadoPedagogico": "Reporte basado en registros disponibles"
  },
  "objetivosAprendizaje": $objetivosArray,
  "registroInstitucional": {
    "sistema": "MPAZ PRO 2.0",
    "tipoReporte": "Reporte de Rendimiento Escolar",
    "area": "Unidad Técnica Pedagógica (UTP)"
  }
}""".trimIndent()

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
