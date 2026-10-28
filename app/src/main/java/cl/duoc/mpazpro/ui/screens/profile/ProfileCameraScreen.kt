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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.viewmodel.ProfileViewModel

/**
 * Pantalla de Perfil con acceso a Cámara y gestión de permisos en tiempo de ejecución.
 *
 * Integra:
 * 1. Solicitud dinámica de permisos en runtime (Manifest.permission.CAMERA).
 * 2. Captura nativa mediante ActivityResultContracts.TakePicturePreview().
 * 3. Renderizado y manipulación en tiempo real de la presentación visual (bordes y tamaño).
 *
 * MVVM / Arquitectura Limpia:
 * La pantalla no interactúa directamente con la capa de datos (SharedPreferences o archivos en disco).
 * Toda la persistencia y lectura de estado se delegan a [ProfileViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCameraScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Parámetros dinámicos de presentación visual (Estado exclusivo de UI)
    var usarBordeRedondeado by remember { mutableStateOf(false) }
    var tamanoFotoDp by remember { mutableFloatStateOf(160f) }
    var usarEscalaCrop by remember { mutableStateOf(true) }

    // Launcher de captura de imagen (Retorna Bitmap en memoria)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        viewModel.onFotoCapturada(bitmap)
    }

    // Launcher de permisos en tiempo de ejecución (Runtime Permission)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        tienePermiso = isGranted
        viewModel.onPermisoResultado(isGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credencial y Perfil Escolar") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Fundamento Técnico
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
                            text = "Fotografía de Identificación Escolar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permite registrar o actualizar la imagen de la credencial oficial del estudiante o docente, asegurando un registro visual para la ficha escolar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Visualizador del Avatar / Fotografía Renderizada
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

                    if (uiState.fotoBitmap != null) {
                        Image(
                            bitmap = uiState.fotoBitmap!!.asImageBitmap(),
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

                    val nombreCompleto = "${uiState.nombre} ${uiState.apellido}".trim().ifEmpty { "Estudiante" }
                    Text(
                        text = nombreCompleto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.curso} - Escuela Marcela Paz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Formulario de Nombre y Apellido del Estudiante
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Datos del Estudiante",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tu nombre aparecerá en la bienvenida y en los reportes escolares.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.apellido,
                        onValueChange = { viewModel.onApellidoChange(it) },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.curso,
                        onValueChange = { viewModel.onCursoChange(it) },
                        label = { Text("Curso") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.guardarPerfil() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Datos del Estudiante")
                    }
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
                        text = if (uiState.fotoBitmap == null) "Abrir Cámara y Capturar Foto" else "Tomar Nueva Fotografía",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Banner animado de estado
            AnimatedVisibility(
                visible = uiState.mensajeEstado != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.esError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (uiState.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.mensajeEstado ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Panel de Control para Defensa en Vivo: Ajuste de Presentación Visual
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
                        text = "Personalización de la Credencial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ajusta la forma, encuadre y dimensiones de la fotografía escolar:",
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

                    // Control 2: Slider de Tamaño Dinámico
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

                    // Control 3: Modo de Ajuste (Crop vs Fit)
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
