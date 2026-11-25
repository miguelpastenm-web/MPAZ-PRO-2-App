package cl.duoc.mpazpro.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.ui.theme.AzulInstitucional
import cl.duoc.mpazpro.viewmodel.student.ExplorarPerrosUiState
import cl.duoc.mpazpro.viewmodel.student.ExplorarPerrosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorarPerrosScreen(
    onBack: () -> Unit,
    viewModel: ExplorarPerrosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conoce a los perros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a mis asignaturas")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulInstitucional,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val actual = estado) {
                ExplorarPerrosUiState.Cargando -> {
                    Box(Modifier.fillMaxWidth().heightIn(min = 180.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Buscando una foto…")
                        }
                    }
                }
                is ExplorarPerrosUiState.Foto -> {
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = actual.imagen.asImageBitmap(),
                            contentDescription = "Fotografía de Dog CEO para observar cómo es un perro",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                        )
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Observa y describe", style = MaterialTheme.typography.titleLarge)
                            Text("¿De qué color es su pelo? ¿Cómo son sus orejas?")
                            Text("Los perros usan el olfato para conocer lo que hay a su alrededor. ¿Qué parte de su cuerpo usan para oler?")
                        }
                    }
                    Text("Fotos de Dog CEO", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = viewModel::consultar, modifier = Modifier.fillMaxWidth()) {
                        Text("Ver otro perro")
                    }
                }
                ExplorarPerrosUiState.Error -> {
                    Text(
                        "No pudimos mostrar la foto. Revisa tu conexión e inténtalo otra vez.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = viewModel::consultar, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver a consultar")
                    }
                }
            }
        }
    }
}
