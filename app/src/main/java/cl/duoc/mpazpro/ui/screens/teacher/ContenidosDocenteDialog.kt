package cl.duoc.mpazpro.ui.screens.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazpro.data.remote.dto.ActividadRequest
import cl.duoc.mpazpro.data.remote.dto.ObjetivoRequest
import cl.duoc.mpazpro.viewmodel.teacher.ContenidosDocenteViewModel

@Composable
internal fun ContenidosDocenteDialog(
    asignaturaId: Int,
    onCerrar: () -> Unit,
    contenidosViewModel: ContenidosDocenteViewModel = viewModel()
) {
    val estado by contenidosViewModel.estado.collectAsStateWithLifecycle()
    val objetivo = estado.objetivoSeleccionado
    var editarObjetivo by remember { mutableStateOf<ObjetivoRequest?>(null) }
    var editarActividad by remember { mutableStateOf<ActividadRequest?>(null) }
    var idEdicion by remember { mutableStateOf<Int?>(null) }
    var eliminar by remember { mutableStateOf<(() -> Unit)?>(null) }
    val editando = editarObjetivo != null || editarActividad != null
    val scrollState = remember(objetivo?.id, editando, eliminar != null) { ScrollState(0) }

    LaunchedEffect(Unit) { contenidosViewModel.cargarObjetivos() }

    AlertDialog(
        onDismissRequest = { if (!estado.cargando) onCerrar() },
        title = { Text(if (objetivo == null) "Objetivos de aprendizaje" else objetivo.codigoMineduc) },
        text = {
            Column(
                Modifier.heightIn(max = 430.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                val solicitudObjetivo = editarObjetivo
                val solicitudActividad = editarActividad
                when {
                    eliminar != null -> Text("¿Confirmas la eliminación de este contenido?")
                    solicitudObjetivo != null -> {
                        CampoContenido("Código Mineduc", solicitudObjetivo.codigoMineduc) {
                            editarObjetivo = solicitudObjetivo.copy(codigoMineduc = it)
                        }
                        CampoContenido("Descripción", solicitudObjetivo.descripcion) {
                            editarObjetivo = solicitudObjetivo.copy(descripcion = it)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = solicitudObjetivo.autorizado,
                                onCheckedChange = { editarObjetivo = solicitudObjetivo.copy(autorizado = it) }
                            )
                            Text("Autorizado")
                        }
                    }
                    solicitudActividad != null -> {
                        CampoContenido("Título", solicitudActividad.titulo) {
                            editarActividad = solicitudActividad.copy(titulo = it)
                        }
                        CampoContenido("Tipo de desafío", solicitudActividad.tipoDesafio) {
                            editarActividad = solicitudActividad.copy(tipoDesafio = it)
                        }
                        CampoContenido("Pregunta", solicitudActividad.pregunta) {
                            editarActividad = solicitudActividad.copy(pregunta = it)
                        }
                        CampoContenido("Respuesta correcta", solicitudActividad.respuestaCorrecta) {
                            editarActividad = solicitudActividad.copy(respuestaCorrecta = it)
                        }
                        CampoContenido("Alternativa incorrecta 1", solicitudActividad.alternativaIncorrecta1) {
                            editarActividad = solicitudActividad.copy(alternativaIncorrecta1 = it)
                        }
                        CampoContenido("Alternativa incorrecta 2", solicitudActividad.alternativaIncorrecta2) {
                            editarActividad = solicitudActividad.copy(alternativaIncorrecta2 = it)
                        }
                        CampoContenido("Alternativa incorrecta 3", solicitudActividad.alternativaIncorrecta3) {
                            editarActividad = solicitudActividad.copy(alternativaIncorrecta3 = it)
                        }
                    }
                    objetivo == null -> {
                        val objetivos = estado.objetivos.filter { it.asignaturaId == asignaturaId }
                        if (objetivos.isEmpty() && !estado.cargando && estado.error == null) {
                            Text("No hay objetivos para esta asignatura.")
                        }
                        objetivos.forEach {
                            OutlinedButton(
                                onClick = { contenidosViewModel.seleccionarObjetivo(it) },
                                enabled = !estado.cargando,
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(it.codigoMineduc) }
                        }
                        TextButton(
                            onClick = {
                                idEdicion = null
                                editarObjetivo = ObjetivoRequest(asignaturaId, "", "", false)
                            },
                            enabled = !estado.cargando
                        ) { Text("Nuevo objetivo") }
                        TextButton(
                            onClick = contenidosViewModel::cargarObjetivos,
                            enabled = !estado.cargando
                        ) { Text("Actualizar lista") }
                    }
                    else -> {
                        Text(objetivo.descripcion)
                        Text(if (objetivo.autorizado) "Autorizado" else "Pendiente de autorización")
                        Row {
                            TextButton(
                                onClick = {
                                    idEdicion = objetivo.id
                                    editarObjetivo = ObjetivoRequest(
                                        objetivo.asignaturaId, objetivo.codigoMineduc,
                                        objetivo.descripcion, objetivo.autorizado
                                    )
                                },
                                enabled = !estado.cargando
                            ) { Text("Editar objetivo") }
                            TextButton(
                                onClick = { eliminar = { contenidosViewModel.eliminarObjetivo(objetivo.id) } },
                                enabled = !estado.cargando
                            ) { Text("Eliminar") }
                        }
                        HorizontalDivider()
                        Text("Actividades", style = MaterialTheme.typography.titleSmall)
                        if (estado.actividades.isEmpty() && !estado.cargando && estado.error == null) {
                            Text("No hay actividades para este objetivo.")
                        }
                        estado.actividades.forEach { actividad ->
                            Text(actividad.titulo, style = MaterialTheme.typography.bodyLarge)
                            Text(actividad.pregunta)
                            Row {
                                TextButton(
                                    onClick = {
                                        idEdicion = actividad.id
                                        editarActividad = actividad.aSolicitud()
                                    },
                                    enabled = !estado.cargando
                                ) { Text("Editar") }
                                TextButton(
                                    onClick = { eliminar = { contenidosViewModel.eliminarActividad(actividad.id) } },
                                    enabled = !estado.cargando
                                ) { Text("Eliminar") }
                            }
                        }
                        TextButton(
                            onClick = {
                                idEdicion = null
                                editarActividad = ActividadRequest(objetivo.id, "", "opcion", "", "", "", "", "")
                            },
                            enabled = !estado.cargando
                        ) { Text("Nueva actividad") }
                        TextButton(
                            onClick = { contenidosViewModel.seleccionarObjetivo(objetivo) },
                            enabled = !estado.cargando
                        ) { Text("Actualizar actividades") }
                    }
                }
            }
        },
        confirmButton = {
            when {
                eliminar != null -> TextButton(
                    enabled = !estado.cargando,
                    onClick = {
                        eliminar?.invoke()
                        eliminar = null
                    }
                ) { Text("Confirmar") }
                editando -> TextButton(
                    enabled = !estado.cargando,
                    onClick = {
                        editarObjetivo?.let {
                            contenidosViewModel.guardarObjetivo(idEdicion, it) { editarObjetivo = null }
                        }
                        editarActividad?.let {
                            contenidosViewModel.guardarActividad(idEdicion, it) { editarActividad = null }
                        }
                    }
                ) { Text("Guardar") }
                else -> TextButton(enabled = !estado.cargando, onClick = onCerrar) { Text("Cerrar") }
            }
        },
        dismissButton = {
            if (editando || eliminar != null || objetivo != null) {
                TextButton(
                    enabled = !estado.cargando,
                    onClick = {
                        if (editando || eliminar != null) {
                            editarObjetivo = null
                            editarActividad = null
                            eliminar = null
                        } else {
                            contenidosViewModel.seleccionarObjetivo(null)
                        }
                    }
                ) { Text(if (editando || eliminar != null) "Cancelar" else "Volver") }
            }
        }
    )
}

@Composable
private fun CampoContenido(etiqueta: String, valor: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth()
    )
}
