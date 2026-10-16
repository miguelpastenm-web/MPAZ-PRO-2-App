package cl.duoc.mpazpro.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Modelo de datos simple para representar una actividad didactica de 2.° Basico
 */
data class ActividadDidactica(
    val id: Int,
    val asignatura: String,
    val pregunta: String,
    val audioInstruccion: String,
    val opciones: List<String>,
    val respuestaCorrectaIndex: Int,
    val explicacionGuiada: String
)

/**
 * Pantalla de Actividades para Segundo Basico con soporte de accesibilidad y animaciones (IE 2.1.1 y IE 2.2.2)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentActivityScreen(
    onBack: () -> Unit
) {
    // Lista de actividades didacticas de prueba (alineadas a los OAs de 2.º Basico)
    val actividades = remember {
        listOf(
            ActividadDidactica(
                id = 1,
                asignatura = "Matemática",
                pregunta = "¿Cuánto es 5 + 3?",
                audioInstruccion = "Escucha: suma cinco manzanas más tres manzanas. ¿Cuántas hay?",
                opciones = listOf("7", "8", "9"),
                respuestaCorrectaIndex = 1,
                explicacionGuiada = "Pista: Cuenta desde el 5: seis, siete, ocho. ¡Son 8 en total!"
            ),
            ActividadDidactica(
                id = 2,
                asignatura = "Lenguaje y Comunicación",
                pregunta = "¿Qué palabra rima con 'GATO'?",
                audioInstruccion = "Escucha con atención: busca la palabra que termina igual que gato.",
                opciones = listOf("PATO", "CASA", "PERRO"),
                respuestaCorrectaIndex = 0,
                explicacionGuiada = "Pista: G-ATO y P-ATO terminan con el mismo sonido 'ATO'."
            ),
            ActividadDidactica(
                id = 3,
                asignatura = "Ciencias Naturales",
                pregunta = "¿Qué necesitan las plantas para crecer?",
                audioInstruccion = "Piensa en lo que le pones a una semilla para que viva.",
                opciones = listOf("Juguetes", "Agua y Luz", "Dulces"),
                respuestaCorrectaIndex = 1,
                explicacionGuiada = "Pista: Las plantas beben agua de la tierra y toman sol para fabricar su alimento."
            )
        )
    }

    var indiceActual by remember { mutableIntStateOf(0) }
    var estaCargando by remember { mutableStateOf(true) }
    var opcionSeleccionada by remember { mutableStateOf<Int?>(null) }
    var mostrarPista by remember { mutableStateOf(false) }
    var audioReproduciendo by remember { mutableStateOf(false) }

    val actividadActual = actividades[indiceActual]
    val scrollState = rememberScrollState()

    // Simulacion de carga asincrona para evidenciar el CircularProgressIndicator
    LaunchedEffect(indiceActual) {
        estaCargando = true
        opcionSeleccionada = null
        mostrarPista = false
        audioReproduciendo = false
        delay(600) // Transicion de carga fluida
        estaCargando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Actividad - 2.º Básico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al menu"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Animacion 1: Loader dinamico mientras se cargan las actividades
            if (estaCargando) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando tu desafío...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Indicador de Progreso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Asignatura: ${actividadActual.asignatura}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = "Desafío ${indiceActual + 1} de ${actividades.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tarjeta Principal de la Pregunta (Tipografia grande para lectores iniciales)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = actividadActual.pregunta,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Boton de Audio Instruccional (Apoyo a la lectura)
                            FilledTonalButton(
                                onClick = { audioReproduciendo = !audioReproduciendo },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Escuchar instruccion"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (audioReproduciendo) "Pausar Audio" else "Escuchar Instrucción",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            // Mensaje auditivo pass-through
                            AnimatedVisibility(visible = audioReproduciendo) {
                                Text(
                                    text = "\"${actividadActual.audioInstruccion}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Text(
                        text = "Elige la respuesta correcta:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Opciones de respuesta (Botones grandes de alto contraste)
                    actividadActual.opciones.forEachIndexed { index, opcion ->
                        val esSeleccionada = opcionSeleccionada == index
                        val esCorrecta = index == actividadActual.respuestaCorrectaIndex

                        val botonColor = when {
                            opcionSeleccionada == null -> ButtonDefaults.buttonColors()
                            esSeleccionada && esCorrecta -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                            esSeleccionada && !esCorrecta -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                            else -> ButtonDefaults.outlinedButtonColors()
                        }

                        Button(
                            onClick = {
                                if (opcionSeleccionada == null) {
                                    opcionSeleccionada = index
                                }
                            },
                            colors = botonColor,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            Text(
                                text = opcion,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Animacion 2: AnimatedVisibility con Slide y Fade para retroalimentacion inmediata
                    AnimatedVisibility(
                        visible = opcionSeleccionada != null,
                        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                            animationSpec = tween(400),
                            initialOffsetY = { it / 2 }
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { it / 2 }
                        )
                    ) {
                        val acerto = opcionSeleccionada == actividadActual.respuestaCorrectaIndex

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (acerto) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (acerto) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (acerto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (acerto) "¡Excelente trabajo!" else "¡Casi lo logras! Inténtalo de nuevo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (acerto) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Boton Siguiente
                                Button(
                                    onClick = {
                                        if (indiceActual < actividades.size - 1) {
                                            indiceActual++
                                        } else {
                                            indiceActual = 0 // Reiniciar ciclo
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Siguiente Desafío")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                }
                            }
                        }
                    }

                    // Animacion 3: animateContentSize en tarjeta expandible de Pista / Explicacion guiada
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = tween(350))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ayuda guiada",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                TextButton(onClick = { mostrarPista = !mostrarPista }) {
                                    Text(if (mostrarPista) "Ocultar" else "Ver Pista")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.HelpOutline, contentDescription = null)
                                }
                            }

                            if (mostrarPista) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = actividadActual.explicacionGuiada,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
