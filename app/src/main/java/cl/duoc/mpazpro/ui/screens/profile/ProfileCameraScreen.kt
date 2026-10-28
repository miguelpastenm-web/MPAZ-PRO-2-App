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
import cl.duoc.mpazpro.data.local.preferences.PerfilEstudianteManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color

/**
 * Pantalla de Perfil con acceso a Camara y gestion de permisos en tiempo de ejecucion
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


    var fotoBitmap by remember {
        mutableStateOf<Bitmap?>(
            runCatching {
                val archivoFoto = File(context.filesDir, "avatar_perfil.png")

                if (archivoFoto.exists()) {
                    BitmapFactory.decodeFile(archivoFoto.absolutePath)
                } else {
                    null
                }
            }.getOrNull()
        )
    }
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

    // Parametros dinamicos de presentacion visual
    var usarBordeRedondeado by remember { mutableStateOf(false) }
    var tamanoFotoDp by remember { mutableFloatStateOf(160f) }
    var usarEscalaCrop by remember { mutableStateOf(true) }

    var nombreEstudiante by remember { mutableStateOf(PerfilEstudianteManager.obtenerNombre(context)) }
    var apellidoEstudiante by remember { mutableStateOf(PerfilEstudianteManager.obtenerApellido(context)) }
    var cursoEstudiante by remember { mutableStateOf(PerfilEstudianteManager.obtenerCurso(context)) }

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
    val azulInstitucional = Color(0xFF102B61)
    val azulPrincipal = Color(0xFF2258A2)
    val azulClaro = Color(0xFFDCE6FF)
    val fondoSuave = Color(0xFFF0F4FF)


    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = azulPrincipal,
            onPrimary = Color.White,
            primaryContainer = azulClaro,
            onPrimaryContainer = azulInstitucional,
            secondary = azulPrincipal,
            onSecondary = Color.White,
            secondaryContainer = azulClaro,
            onSecondaryContainer = azulInstitucional,
            background = fondoSuave,
            onBackground = azulInstitucional,
            surface = Color.White,
            onSurface = azulInstitucional,
            surfaceVariant = azulClaro,
            surfaceContainerLow = Color.White,
            outline = Color(0xFF8798B7)
        )
    ) {
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credencial y Perfil Escolar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menu"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = azulInstitucional,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )

            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(fondoSuave)
                .verticalScroll(scrollState)
                .padding(20.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            // Tarjeta de Fundamento Tecnico
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
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

            // Visualizador del Avatar / Fotografia Renderizada
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

                    val nombreCompleto = "$nombreEstudiante $apellidoEstudiante".trim().ifEmpty { "Estudiante" }
                    Text(
                        text = nombreCompleto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$cursoEstudiante - Escuela Marcela Paz",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Formulario de Nombre y Apellido del Estudiante

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp
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
                        value = nombreEstudiante,
                        onValueChange = { nombreEstudiante = it },
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = azulPrincipal,
                            unfocusedBorderColor = Color(0xFF8798B7),
                            focusedLabelColor = azulPrincipal,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = azulPrincipal
                        )


                    )

                    OutlinedTextField(
                        value = apellidoEstudiante,
                        onValueChange = { apellidoEstudiante = it },
                        label = { Text("Apellido") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = azulPrincipal,
                            unfocusedBorderColor = Color(0xFF8798B7),
                            focusedLabelColor = azulPrincipal,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = azulPrincipal
                        )

                    )

                    OutlinedTextField(
                        value = cursoEstudiante,
                        onValueChange = { cursoEstudiante = it },
                        label = { Text("Curso") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = azulPrincipal,
                            unfocusedBorderColor = Color(0xFF8798B7),
                            focusedLabelColor = azulPrincipal,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = azulPrincipal
                        )

                    )

                    Button(
                        onClick = {
                            PerfilEstudianteManager.guardarPerfil(
                                context = context,
                                nombre = nombreEstudiante,
                                apellido = apellidoEstudiante,
                                curso = cursoEstudiante
                            )
                            val completo = "$nombreEstudiante $apellidoEstudiante".trim().ifEmpty { "Estudiante" }
                            mensajeEstado = "Perfil escolar actualizado correctamente para $completo."
                            esError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = azulPrincipal,
                            contentColor = Color.White
                        )
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

            // Panel de Control para Defensa en Vivo: Ajuste de Presentacion Visual
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White),
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
