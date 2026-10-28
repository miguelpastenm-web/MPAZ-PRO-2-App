package cl.duoc.mpazpro.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.DatePickerDialog
import java.util.Calendar
import androidx.compose.material.icons.filled.Event
import cl.duoc.mpazpro.ui.components.CustomValidatedField
import cl.duoc.mpazpro.viewmodel.TeacherFormViewModel
import androidx.compose.ui.platform.LocalContext

/**
 * Pantalla de Formulario Docente con validacion desacoplada y soporte visual.
 *
 * MVVM: La pantalla solo interactúa con TeacherFormViewModel, sin acceso directo
 * a ConfiguracionEvaluacion, AppDatabase o AppRepository.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFormScreen(
    onBack: () -> Unit,
    onNavigateToExport: (() -> Unit)? = null,
    viewModel: TeacherFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Selector interactivo de fecha nativo de Android
    val calendario = remember { Calendar.getInstance() }
    val datePickerDialog = remember(context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.onFechaLimiteChange(fechaFormateada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
    }

    val asignaturas = listOf(
        "Matemática",
        "Lenguaje y Comunicación",
        "Ciencias Naturales",
        "Historia y Geografía"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Evaluación") },
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Parámetros de Segundo Básico",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingrese los datos del docente y configure los intentos de la prueba final por asignatura.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Asignatura a Evaluar:",
                style = MaterialTheme.typography.labelLarge
            )

            // Selector de Asignatura (Chips horizontales)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                asignaturas.take(2).forEach { asignatura ->
                    FilterChip(
                        selected = uiState.asignatura == asignatura,
                        onClick = { viewModel.onAsignaturaChange(asignatura) },
                        label = { Text(asignatura, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                asignaturas.drop(2).forEach { asignatura ->
                    FilterChip(
                        selected = uiState.asignatura == asignatura,
                        onClick = { viewModel.onAsignaturaChange(asignatura) },
                        label = { Text(asignatura, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            // 1. Campo Nombre Docente
            CustomValidatedField(
                value = uiState.nombreDocente,
                onValueChange = { viewModel.onNombreDocenteChange(it) },
                label = "Nombre del Docente",
                placeholder = "Ej. Marcela Paz",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.nombreDocenteError
            )

            // 2. Campo Codigo Docente / Correo Ficticio
            CustomValidatedField(
                value = uiState.codigoDocente,
                onValueChange = { viewModel.onCodigoDocenteChange(it) },
                label = "Código o Correo Institucional",
                placeholder = "Ej. DOC-102 o docente@marcelapaz.cl",
                leadingIcon = Icons.Default.Badge,
                errorMessage = uiState.codigoDocenteError
            )

            // 3. Campo Maximo de Intentos (1 a 5)
            CustomValidatedField(
                value = uiState.maxIntentos,
                onValueChange = { viewModel.onMaxIntentosChange(it) },
                label = "Máximo de Intentos Permitidos (1 - 5)",
                placeholder = "Ej. 3",
                leadingIcon = Icons.Default.FormatListNumbered,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessage = uiState.maxIntentosError
            )

            // 4. Campo Fecha Limite (con selector interactivo de calendario)
            CustomValidatedField(
                value = uiState.fechaLimite,
                onValueChange = { viewModel.onFechaLimiteChange(it) },
                label = "Fecha Límite de Rendición",
                placeholder = "AAAA-MM-DD (Ej. 2026-11-30)",
                leadingIcon = Icons.Default.CalendarToday,
                trailingCustomIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Seleccionar fecha en calendario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                errorMessage = uiState.fechaLimiteError
            )

            // 5. Switch: Permitir pistas didácticas
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = if (uiState.permitirPistas) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Permitir pistas pedagógicas",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (uiState.permitirPistas) "Pistas y apoyos didácticos activados para 2.° Básico" else "Evaluación estricta sin pistas de apoyo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = uiState.permitirPistas,
                        onCheckedChange = { viewModel.onPermitirPistasChange(it) }
                    )
                }
            }

            // 6. Checkbox: Confirmación obligatoria de parámetros antes de guardar
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.confirmarParametrosError != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.confirmarParametros,
                            onCheckedChange = { viewModel.onConfirmarParametrosChange(it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirmo que los parámetros de evaluación han sido revisados y autorizados para el curso.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Mensaje de error visual para el checkbox
                    AnimatedVisibility(
                        visible = uiState.confirmarParametrosError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.confirmarParametrosError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Feedback visual animado ante guardado exitoso
            AnimatedVisibility(
                visible = uiState.isSuccess,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.successMessage ?: "Parámetros registrados con éxito.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Boton Principal de Guardar - toda la persistencia la maneja el ViewModel
            Button(
                onClick = { viewModel.validarYGuardar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Parámetros de Evaluación", style = MaterialTheme.typography.titleMedium)
            }

            // Acción secundaria: Limpiar formulario
            OutlinedButton(
                onClick = { viewModel.limpiarFormulario() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpiar Formulario", style = MaterialTheme.typography.bodyMedium)
            }

            if (onNavigateToExport != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilledTonalButton(
                    onClick = onNavigateToExport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.FolderShared, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar y Exportar Reportes")
                }
            }
        }
    }
}
