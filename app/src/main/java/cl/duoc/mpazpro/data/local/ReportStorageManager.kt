package cl.duoc.mpazpro.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor utilitario de almacenamiento interno y exportacion de archivos (IE 2.4.1 y IE 2.4.2)
 *
 * Utiliza context.filesDir para persistencia interna privada bajo el sandbox de Android/Linux
 * sin requerir permisos peligrosos en el manifiesto.
 */
object ReportStorageManager {

    private const val PREFIX_REPORTE = "reporte_"
    private const val EXTENSION_JSON = ".json"

    /**
     * Genera la estructura del reporte academico en formato JSON para Segundo Basico
     */
    fun generarContenidoReporte(
        docente: String,
        asignatura: String,
        curso: String = "2.° Básico A"
    ): String {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        return """{
  "escuela": "Escuela Escritora Marcela Paz",
  "comuna": "San Bernardo",
  "nivelEducativo": "Segundo Básico",
  "curso": "",
  "docenteEvaluador": "",
  "asignatura": "",
  "fechaEmision": "",
  "resumenEvaluacion": {
    "matriculaTotal": 28,
    "evaluacionesRendidas": 26,
    "porcentajeLogroGlobal": 89.2,
    "estadoPedagogico": "Desempeño Destacado"
  },
  "objetivosAprendizaje": [
    {
      "codigoOA": "OA_09",
      "nombre": "Resolución de adiciones y sustracciones con apoyo visual",
      "logro": "92%"
    },
    {
      "codigoOA": "OA_04",
      "nombre": "Lectura fluida y asociación fonética en rimas",
      "logro": "86%"
    },
    {
      "codigoOA": "OA_01",
      "nombre": "Comprensión del ciclo vital de las plantas",
      "logro": "90%"
    }
  ],
  "registroInstitucional": {
    "sistema": "MPAZ PRO 2.0",
    "tipoReporte": "Informe Oficial de Rendimiento Escolar",
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
            val nombreLimpio = if (nombreArchivo.endsWith(".json") || nombreArchivo.endsWith(".txt")) {
                nombreArchivo
            } else {
                ".json"
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
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contenido.toByteArray(Charsets.UTF_8))
                true
            } ?: false
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
