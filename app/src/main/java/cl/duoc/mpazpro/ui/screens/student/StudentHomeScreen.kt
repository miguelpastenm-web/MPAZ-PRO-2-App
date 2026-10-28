package cl.duoc.mpazpro.ui.screens.student

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import java.io.File
import cl.duoc.mpazpro.data.local.ConfiguracionEvaluacion
import cl.duoc.mpazpro.data.local.EvaluacionConfig
import cl.duoc.mpazpro.data.local.PerfilEstudianteManager

/**
 * Modelo de datos inmutable para representar una materia escolar.
 * Define la estructura que necesita la tarjeta visual.
 */
data class MateriaItem(
    val oaId: Int,
    val nombre: String,
    val codigoMineduc: String,
    val descripcion: String,
    val icono: ImageVector
)

/**
 * Pantalla principal del Estudiante de 2.º Básico.
 *
 * RESPONSABILIDADES:
 * 1. Presentar de forma amigable y ordenada las 4 asignaturas del currículum.
 * 2. Conectar y reflejar los parámetros de evaluación fijados por el docente (fecha, docente, intentos).
 * 3. Acceder a la credencial y perfil escolar del propio estudiante.
 * 4. Desacoplamiento total: no conoce NavController, solo emite callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onBack: () -> Unit,
    onSelectMateria: (Int) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Las 4 asignaturas oficiales de 2.° Básico según el caso Marcela Paz
    val materias = listOf(
        MateriaItem(
            oaId = 1,
            nombre = "Lenguaje y Comunicación",
            codigoMineduc = "OA-1",
            descripcion = "Lectura de palabras, rimas y comprensión de textos breves.",
            icono = Icons.Default.AutoStories
        ),
        MateriaItem(
            oaId = 2,
            nombre = "Matemática",
            codigoMineduc = "OA-2",
            descripcion = "Sumas divertidas, restas y resolución de problemas cotidianos.",
            icono = Icons.Default.Calculate
        ),
        MateriaItem(
            oaId = 3,
            nombre = "Ciencias Naturales",
            codigoMineduc = "OA-3",
            descripcion = "Los seres vivos, las plantas y el cuidado del medioambiente.",
            icono = Icons.Default.Forest
        ),
        MateriaItem(
            oaId = 4,
            nombre = "Historia y Geografía",
            codigoMineduc = "OA-4",
            descripcion = "Nuestra comunidad, lugares importantes y orientación espacial.",
            icono = Icons.Default.Public
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Asignaturas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Mi Credencial y Perfil Escolar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
            val nombreCompleto = remember(context) {
                PerfilEstudianteManager.obtenerNombreCompleto(context)
            }
            val cursoEstudiante = remember(context) {
                PerfilEstudianteManager.obtenerCurso(context)
            }
            val avatarBitmap = remember(context) {
                runCatching {
                    val file = File(context.filesDir, "avatar_perfil.png")
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }.getOrNull()
            }

            // Tarjeta de Bienvenida y Motivación Pedagógica (Clickeable para ver/editar perfil)
            Card(
                onClick = onNavigateToProfile,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Avatar de $nombreCompleto",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, $nombreCompleto!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$cursoEstudiante - Escuela Marcela Paz",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Selecciona una materia para practicar tus desafíos:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Editar credencial",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Listado de Tarjetas por Materia (conectadas a la configuración docente)
            materias.forEach { materia ->
                val config = remember(materia.nombre) {
                    ConfiguracionEvaluacion.obtenerParametros(context, materia.nombre)
                }

                MateriaCard(
                    materia = materia,
                    config = config,
                    onClick = { onSelectMateria(materia.oaId) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón en la sección del Estudiante para gestionar su Credencial Escolar
            OutlinedButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mi Credencial y Perfil Escolar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Componente reutilizable para cada tarjeta de Asignatura.
 * Sigue los principios de accesibilidad para lectores iniciales:
 * - Botones amplios
 * - Tipografía grande y de alto contraste
 * - Información oficial docente integrada
 */
@Composable
private fun MateriaCard(
    materia: MateriaItem,
    config: EvaluacionConfig?,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado de la Tarjeta con Ícono Oficial de Compose
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = materia.icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = materia.nombre,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = materia.codigoMineduc,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Text(
                text = materia.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Panel de Parámetros Docentes Conectados (Sin emojis, usando Icons oficiales)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (config != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Docente: ${config.docente} (${config.codigoDocente})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Límite: ${config.fechaLimite}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatListNumbered,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Intentos: ${config.maxIntentos}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (config.permitirPistas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.permitirPistas) "Pistas didácticas activadas" else "Evaluación estricta sin pistas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Evaluación estándar de Segundo Básico disponible",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Botón interactivo de inicio de actividades
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Iniciar Desafío",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}
