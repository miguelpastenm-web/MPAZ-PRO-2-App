package cl.duoc.mpazpro.viewmodel.student

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.repository.DogCeoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ExplorarPerrosUiState {
    data object Cargando : ExplorarPerrosUiState
    data class Foto(val imagen: Bitmap) : ExplorarPerrosUiState
    data object Error : ExplorarPerrosUiState
}

class ExplorarPerrosViewModel internal constructor(
    private val repository: DogCeoRepository,
    private val dispatcherImagen: CoroutineDispatcher,
    private val decodificar: (ByteArray) -> Bitmap?
) : ViewModel() {

    constructor() : this(DogCeoRepository())

    constructor(repository: DogCeoRepository) :
        this(repository, Dispatchers.Default, ::decodificarBitmap)

    private val _estado = MutableStateFlow<ExplorarPerrosUiState>(ExplorarPerrosUiState.Cargando)
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null

    init {
        consultar()
    }

    fun consultar() {
        if (consulta?.isActive == true) return
        _estado.value = ExplorarPerrosUiState.Cargando
        consulta = viewModelScope.launch {
            val datos = repository.obtenerFoto().getOrNull()
            val imagen = datos?.let { withContext(dispatcherImagen) { decodificar(it) } }
            _estado.value = if (imagen != null) ExplorarPerrosUiState.Foto(imagen) else ExplorarPerrosUiState.Error
        }
    }
}

private fun decodificarBitmap(datos: ByteArray): Bitmap? {
    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(datos, 0, datos.size, opciones)
    if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null

    opciones.inSampleSize = 1
    while (maxOf(opciones.outWidth, opciones.outHeight) / opciones.inSampleSize > 1600) {
        opciones.inSampleSize *= 2
    }
    opciones.inJustDecodeBounds = false
    return BitmapFactory.decodeByteArray(datos, 0, datos.size, opciones)
}
