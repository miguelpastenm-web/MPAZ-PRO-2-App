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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.ui.components.CustomValidatedField
import cl.duoc.mpazpro.viewmodel.TeacherFormViewModel

/**
 * Pantalla de Formulario Docente con validacion desacoplada y soporte visual (IE 2.1.2 y IE 2.2.1)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFormScreen(
    onBack: () -> Unit,
    viewModel: TeacherFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
                label = "Código o Correo Docente (Ficticio)",
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

            // 4. Campo Fecha Limite
            CustomValidatedField(
                value = uiState.fechaLimite,
                onValueChange = { viewModel.onFechaLimiteChange(it) },
                label = "Fecha Límite de Rendición",
                placeholder = "AAAA-MM-DD (Ej. 2026-11-30)",
                leadingIcon = Icons.Default.CalendarToday,
                errorMessage = uiState.fechaLimiteError
            )

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

            // Boton Principal de Guardar
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

            // Boton Secundario de Limpiar
            OutlinedButton(
                onClick = { viewModel.limpiarFormulario() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Limpiar Formulario")
            }
        }
    }
}
