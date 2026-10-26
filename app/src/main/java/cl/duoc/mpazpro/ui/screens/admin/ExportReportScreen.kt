package cl.duoc.mpazpro.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.data.local.ReportStorageManager
import cl.duoc.mpazpro.data.local.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.local.entities.ResultadoPruebaEntity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AsignaturaOpcion(
    val nombre: String,
    val oaId: Int,
    val codigoMineduc: String,
    val slug: String
)

val asignaturasDisponibles = listOf(
    AsignaturaOpcion("Matemática", 2, "MAT-F01", "matematica"),
    AsignaturaOpcion("Lenguaje y Comunicación", 1, "LEN-F01", "lenguaje"),
    AsignaturaOpcion("Ciencias Naturales", 3, "CIE-F01", "ciencias"),
    AsignaturaOpcion("Historia y Geografía", 4, "HIS-F01", "historia")
)

/**
 * Pantalla para la exportacion y validacion de persistencia de reportes en almacenamiento local
 *
 * Demuestra:
 * 1. Escritura segura en context.filesDir (Sandbox privado sin permisos peligrosos).
 * 2. Exportacion mediante SAF (Storage Access Framework) con ActivityResultContracts.CreateDocument().
 * 3. Listado y lectura en tiempo real de archivos fisicos para inspeccion en Device File Explorer.
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
    val actividadDao = remember(database) { database.actividadDao() }
    val objetivoDao = remember(database) { database.objetivoAprendizajeDao() }

    val resultados by resultadoDao.obtenerTodos().collectAsState(initial = emptyList())
    val actividades by actividadDao.obtenerTodas().collectAsState(initial = emptyList())
    val objetivos by objetivoDao.obtenerTodos().collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()
    val chipsScrollState = rememberScrollState()

    var asignaturaSeleccionada by remember { mutableStateOf(asignaturasDisponibles[0]) }
    var nombreArchivo by remember { mutableStateOf("reporte_2basico_${asignaturasDisponibles[0].slug}.json") }
    var nombreDocente by remember {
        val cfg = ConfiguracionEvaluacion.obtenerParametros(context, asignaturasDisponibles[0].nombre)
        mutableStateOf(cfg?.docente ?: "Marcela Paz")
    }
    var cursoSeleccionado by remember { mutableStateOf("2.° Básico A") }

    fun seleccionarAsignatura(opcion: AsignaturaOpcion) {
        asignaturaSeleccionada = opcion
        val config = ConfiguracionEvaluacion.obtenerParametros(context, opcion.nombre)
        if (config != null && config.docente.isNotBlank()) {
            nombreDocente = config.docente
        }
        nombreArchivo = "reporte_2basico_${opcion.slug}.json"
    }

    val resultadosMateria = remember(resultados, asignaturaSeleccionada) {
        resultados.filter { it.oaId == asignaturaSeleccionada.oaId }
    }
    val actividadesMateria = remember(actividades, asignaturaSeleccionada) {
        actividades.filter { it.oaId == asignaturaSeleccionada.oaId }
    }
    val objetivoMateria = remember(objetivos, asignaturaSeleccionada) {
        objetivos.firstOrNull { it.id == asignaturaSeleccionada.oaId }
    }
    val configMateria = remember(context, asignaturaSeleccionada, nombreDocente) {
        ConfiguracionEvaluacion.obtenerParametros(context, asignaturaSeleccionada.nombre)
    }

    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }
    var rutaFisicaReciente by remember { mutableStateOf<String?>(null) }
    var archivoPrevisualizado by remember { mutableStateOf<File?>(null) }
    var contenidoPrevisualizado by remember { mutableStateOf<String?>(null) }

    // Lista reactiva de archivos en context.filesDir
    var listaArchivos by remember { mutableStateOf(ReportStorageManager.listarArchivosReporte(context)) }

    fun actualizarArchivos() {
        listaArchivos = ReportStorageManager.listarArchivosReporte(context)
    }

    fun generarReporteActual(): String {
        return ReportStorageManager.generarContenidoReporte(
            docente = nombreDocente,
            asignatura = asignaturaSeleccionada.nombre,
            curso = cursoSeleccionado,
            resultados = resultadosMateria,
            oaId = asignaturaSeleccionada.oaId,
            oaEntity = objetivoMateria,
            actividades = actividadesMateria,
            config = configMateria
        )
    }

    // Launcher de SAF para guardar en ubicacion externa seleccionada por el usuario
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val contenido = generarReporteActual()
            val resultado = ReportStorageManager.exportarConSAF(context, uri, contenido)
            if (resultado.isSuccess && resultado.getOrDefault(false)) {
                mensajeResultado = "Reporte exportado exitosamente mediante Storage Access Framework."
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
                title = { Text("Reportes y Evaluaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menu"
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
            // Tarjeta de Fundamento Tecnico para Defensa
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
                        text = "Los reportes se guardan en la memoria interna de la aplicación, protegiendo los datos académicos y de evaluación de los estudiantes de Segundo Básico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Selector interactivo de Asignatura con FilterChips
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Seleccionar Asignatura para Reporte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipsScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        asignaturasDisponibles.forEach { opcion ->
                            val esSeleccionada = asignaturaSeleccionada.oaId == opcion.oaId
                            FilterChip(
                                selected = esSeleccionada,
                                onClick = { seleccionarAsignatura(opcion) },
                                label = { Text(opcion.nombre) },
                                leadingIcon = if (esSeleccionada) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    // Tarjeta Informativa de la Materia Seleccionada
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = "${asignaturaSeleccionada.codigoMineduc} - 2.° Básico",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(
                                    text = "${resultadosMateria.size} evaluaciones en BD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Text(
                                text = objetivoMateria?.descripcion ?: "Objetivo curricular de 2.° Básico",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Docente: ${configMateria?.docente ?: nombreDocente}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Límite: ${configMateria?.fechaLimite?.ifEmpty { "30/11/2026" } ?: "30/11/2026"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Botón para generar evaluaciones de prueba si no hay datos en la materia seleccionada
                    if (resultadosMateria.isEmpty()) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val now = System.currentTimeMillis()
                                    val demoResultados = listOf(
                                        ResultadoPruebaEntity(
                                            estudianteFicticioId = 1,
                                            oaId = asignaturaSeleccionada.oaId,
                                            puntaje = 100,
                                            intentosUsados = 2,
                                            fechaRegistro = now - 3600000L
                                        ),
                                        ResultadoPruebaEntity(
                                            estudianteFicticioId = 2,
                                            oaId = asignaturaSeleccionada.oaId,
                                            puntaje = 75,
                                            intentosUsados = 2,
                                            fechaRegistro = now - 1800000L
                                        ),
                                        ResultadoPruebaEntity(
                                            estudianteFicticioId = 3,
                                            oaId = asignaturaSeleccionada.oaId,
                                            puntaje = 88,
                                            intentosUsados = 2,
                                            fechaRegistro = now
                                        )
                                    )
                                    resultadoDao.insertarTodos(demoResultados)
                                    mensajeResultado = "Se registraron 3 evaluaciones válidas de demostración para ${asignaturaSeleccionada.nombre}."
                                    esError = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddChart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generar Evaluaciones Demo para ${asignaturaSeleccionada.nombre}")
                        }
                    }
                }
            }

            // Formulario de Parametros del Reporte
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Generador de Reporte Académico",
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

                    OutlinedTextField(
                        value = nombreDocente,
                        onValueChange = { nombreDocente = it },
                        label = { Text("Docente / Evaluador") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = asignaturaSeleccionada.nombre,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Asignatura Activa") },
                        leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Boton 1: Guardado en Almacenamiento Interno Privado (context.filesDir)
                    Button(
                        onClick = {
                            val contenido = generarReporteActual()
                            val resultado = ReportStorageManager.guardarEnAlmacenamientoInterno(
                                context = context,
                                nombreArchivo = nombreArchivo,
                                contenido = contenido
                            )
                            if (resultado.isSuccess) {
                                val archivo = resultado.getOrThrow()
                                mensajeResultado = "Reporte de ${asignaturaSeleccionada.nombre} guardado con éxito en memoria interna privada."
                                esError = false
                                rutaFisicaReciente = archivo.absolutePath
                                actualizarArchivos()
                            } else {
                                mensajeResultado = "Error al escribir en almacenamiento interno: ${resultado.exceptionOrNull()?.message}"
                                esError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Reporte en Memoria Local")
                    }

                    // Boton 2: Exportacion externa mediante SAF (Storage Access Framework)
                    OutlinedButton(
                        onClick = {
                            safLauncher.launch(nombreArchivo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar / Descargar con Gestor de Archivos")
                    }
                }
            }

            // Banner animado de confirmacion y visualizacion de ruta fisica
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

            // Seccion de Verificacion de Archivos Persistidos en Disco
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reportes Almacenados en el Dispositivo (${listaArchivos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { actualizarArchivos() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refrescar lista")
                        }
                    }

                    Text(
                        text = "Archivos oficiales generados para respaldo o revisión pedagógica.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (listaArchivos.isEmpty()) {
                        Text(
                            text = "Aún no hay archivos de reporte generados en la memoria interna.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        listaArchivos.forEach { archivo ->
                            val fechaFormato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(archivo.lastModified()))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = archivo.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${archivo.length()} bytes | $fechaFormato",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row {
                                            IconButton(onClick = {
                                                if (archivoPrevisualizado == archivo) {
                                                    archivoPrevisualizado = null
                                                    contenidoPrevisualizado = null
                                                } else {
                                                    archivoPrevisualizado = archivo
                                                    contenidoPrevisualizado = ReportStorageManager.leerContenidoArchivo(archivo)
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = if (archivoPrevisualizado == archivo) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Previsualizar"
                                                )
                                            }
                                            IconButton(onClick = {
                                                ReportStorageManager.eliminarArchivo(archivo)
                                                if (archivoPrevisualizado == archivo) {
                                                    archivoPrevisualizado = null
                                                    contenidoPrevisualizado = null
                                                }
                                                actualizarArchivos()
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    // Previsualizacion expandida del contenido del archivo
                                    if (archivoPrevisualizado == archivo && contenidoPrevisualizado != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = contenidoPrevisualizado ?: "",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
