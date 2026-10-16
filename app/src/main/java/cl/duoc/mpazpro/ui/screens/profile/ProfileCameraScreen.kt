package cl.duoc.mpazpro.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

/**
 * Pantalla de Perfil con acceso a Camara y gestion de permisos en tiempo de ejecucion (IE 2.4.1 y IE 2.4.2)
 *
 * Integra:
 * 1. Solicitud dinamica de permisos en runtime (Manifest.permission.CAMERA).
 * 2. Captura nativa mediante ActivityResultContracts.TakePicturePreview().
 * 3. Renderizado y manipulacion en tiempo real de la presentacion visual (bordes y tamano).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCameraScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var mensajeEstado by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }

    // Parametros dinamicos de presentacion visual (IE 2.4.2)
    var usarBordeRedondeado by remember { mutableStateOf(false) }
    var tamanoFotoDp by remember { mutableFloatStateOf(160f) }
    var usarEscalaCrop by remember { mutableStateOf(true) }

    var nombreEstudiante by remember { mutableStateOf("Matías Sepúlveda") }
    var cursoEstudiante by remember { mutableStateOf("2.° Básico B") }

    // Launcher de captura de imagen (Retorna Bitmap en memoria)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
            mensajeEstado = "Fotografía capturada y renderizada correctamente en la interfaz."
            esError = false

            // Persistencia opcional del avatar en context.filesDir
            runCatching {
                val archivoFoto = File(context.filesDir, "avatar_perfil.png")
                FileOutputStream(archivoFoto).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
            }
        } else {
            mensajeEstado = "Captura de fotografía cancelada."
            esError = false
        }
    }

    // Launcher de permisos en tiempo de ejecucion (Runtime Permission)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        tienePermiso = isGranted
        if (isGranted) {
            mensajeEstado = "Permiso de cámara concedido. Presiona 'Abrir Cámara'."
            esError = false
        } else {
            mensajeEstado = "Permiso de cámara denegado. Se requiere para registrar el perfil escolar."
            esError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil y Recurso Cámara") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Fundamento Tecnico (IE 2.4.1)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recurso Nativo: Cámara (IE 2.4.1)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Implementa ActivityResultContracts.RequestPermission() para gestionar el flujo seguro de permisos en runtime según las directrices modernas de Android, renderizando la captura fotográfica directamente en el árbol de composición.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Visualizador del Avatar / Fotografia Renderizada (IE 2.4.2)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formaFoto = if (usarBordeRedondeado) RoundedCornerShape(24.dp) else CircleShape

                    if (fotoBitmap != null) {
                        Image(
                            bitmap = fotoBitmap!!.asImageBitmap(),
                            contentDescription = "Fotografía de Perfil",
                            contentScale = if (usarEscalaCrop) ContentScale.Crop else ContentScale.Fit,
                            modifier = Modifier
                                .size(tamanoFotoDp.dp)
                                .clip(formaFoto)
                                .border(4.dp, MaterialTheme.colorScheme.primary, formaFoto)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(tamanoFotoDp.dp)
                                .clip(formaFoto)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, formaFoto),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sin fotografía",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = nombreEstudiante,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cursoEstudiante + " - Escuela Marcela Paz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Acciones de Permisos y Captura
            if (!tienePermiso) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Permiso Requerido",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Para capturar la fotografía de identificación, la aplicación debe solicitar autorización a la cámara del dispositivo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solicitar Permiso de Cámara")
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        cameraLauncher.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (fotoBitmap == null) "Abrir Cámara y Capturar Foto" else "Tomar Nueva Fotografía",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Banner animado de estado
            AnimatedVisibility(
                visible = mensajeEstado != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (esError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = mensajeEstado ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Panel de Control para Defensa en Vivo: Ajuste de Presentacion Visual (IE 2.4.2)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ajustes de Presentación en Vivo (IE 2.4.2)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controla la forma, recorte y dimensiones del recurso fotográfico renderizado:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Control 1: Selector de Forma (Circular vs Rectangular Redondeado)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Forma: " + (if (usarBordeRedondeado) "Esquinas Redondeadas" else "Circular"))
                        Switch(
                            checked = usarBordeRedondeado,
                            onCheckedChange = { usarBordeRedondeado = it }
                        )
                    }

                    // Control 2: Slider de Tamano Dinamico
                    Column {
                        Text(
                            text = "Tamaño del Avatar: " + tamanoFotoDp.toInt().toString() + " dp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = tamanoFotoDp,
                            onValueChange = { tamanoFotoDp = it },
                            valueRange = 100f..220f
                        )
                    }

                    // Control 3: Modo de Escala (Crop vs Fit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo de Ajuste: " + (if (usarEscalaCrop) "Recorte Centrado (Crop)" else "Ajuste Completo (Fit)"))
                        Switch(
                            checked = usarEscalaCrop,
                            onCheckedChange = { usarEscalaCrop = it }
                        )
                    }
                }
            }
        }
    }
}
