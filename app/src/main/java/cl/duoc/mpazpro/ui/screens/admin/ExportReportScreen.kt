package cl.duoc.mpazpro.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.database.AppDatabase
import cl.duoc.mpazpro.data.local.preferences.PerfilEstudianteManager
import cl.duoc.mpazpro.data.local.storage.ReportStorageManager
import kotlinx.coroutines.launch

/**
 * Pantalla simplificada y unificada para la generación y exportación del Reporte Académico del Estudiante.
 *
 * Consolida todas las evaluaciones reales rendidas en Segundo Básico sin precargas artificiales.
 * Permite persistencia interna segura (context.filesDir) y exportación mediante SAF.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val database = remember(context) { AppDatabase.getDatabase(context) }
    val resultadoDao = remember(database) { database.resultadoPruebaDao() }

    val resultados by resultadoDao.obtenerTodos().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    val nombreEstudiante = remember(context) { PerfilEstudianteManager.obtenerNombreCompleto(context) }
    val cursoEstudiante = remember(context) { PerfilEstudianteManager.obtenerCurso(context) }

    var nombreArchivo by remember { mutableStateOf("reporte_academico_2basico.json") }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }
    var rutaFisicaReciente by remember { mutableStateOf<String?>(null) }

    fun generarReporte(): String {
        return ReportStorageManager.generarReporteAcademicoEstudiante(
            context = context,
            nombreEstudiante = nombreEstudiante,
            curso = cursoEstudiante,
            resultados = resultados
        )
    }

    // Launcher de SAF para guardar en ubicación elegida por el usuario
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val contenido = generarReporte()
            val resultado = ReportStorageManager.exportarConSAF(context, uri, contenido)
            if (resultado.isSuccess && resultado.getOrDefault(false)) {
                mensajeResultado = "Reporte académico descargado y exportado exitosamente mediante SAF."
                esError = false
                rutaFisicaReciente = uri.toString()
            } else {
                mensajeResultado = "Error al exportar archivo mediante SAF."
                esError = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte Académico del Estudiante") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menú"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Fundamento y Resguardo Institucional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacidad y Resguardo Institucional",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "El reporte consolida todas las evaluaciones rendidas por el estudiante en Segundo Básico bajo almacenamiento seguro y privado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Ficha del Estudiante y Estado de Evaluaciones Reales
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(
                                text = "Segundo Básico",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Text(
                            text = "${resultados.size} evaluaciones en BD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = nombreEstudiante,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        text = "$cursoEstudiante - Escuela Marcela Paz (San Bernardo)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Acción para limpiar evaluaciones de prueba y dejar la BD vacía si se desea
                    if (resultados.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    resultadoDao.borrarTodos()
                                    mensajeResultado = "Base de datos de evaluaciones vaciada correctamente."
                                    esError = false
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vaciar Evaluaciones de BD (Dejar Limpia)")
                        }
                    }
                }
            }

            // Generador Unificado de Reporte
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Generar Reporte Académico Integral",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nombreArchivo,
                        onValueChange = { nombreArchivo = it },
                        label = { Text("Nombre del Archivo") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botón 1: Guardado en Almacenamiento Interno Privado (context.filesDir)
                    Button(
                        onClick = {
                            val contenido = generarReporte()
                            val resultado = ReportStorageManager.guardarEnAlmacenamientoInterno(
                                context = context,
                                nombreArchivo = nombreArchivo,
                                contenido = contenido
                            )
                            if (resultado.isSuccess) {
                                val archivo = resultado.getOrThrow()
                                mensajeResultado = "Reporte académico guardado con éxito en memoria interna privada."
                                esError = false
                                rutaFisicaReciente = archivo.absolutePath
                            } else {
                                mensajeResultado = "Error al escribir en almacenamiento interno: ${resultado.exceptionOrNull()?.message}"
                                esError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Reporte en Memoria Local", style = MaterialTheme.typography.titleMedium)
                    }

                    // Botón 2: Exportación externa mediante SAF (Storage Access Framework)
                    OutlinedButton(
                        onClick = {
                            safLauncher.launch(nombreArchivo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar / Descargar con Gestor de Archivos", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Banner animado de confirmación y ruta física
            AnimatedVisibility(
                visible = mensajeResultado != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (esError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mensajeResultado ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (rutaFisicaReciente != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ruta física / URI de persistencia:",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = rutaFisicaReciente ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
