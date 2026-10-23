package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.mpazpro.data.repository.AppRepository

/**
 * Factory encargada de crear StudentActivityViewModel.
 *
 * Permite entregarle el repositorio que necesita para
 * consultar y guardar información en la base de datos.
 */
class StudentActivityViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {

    /**
     * Crea una instancia del ViewModel solicitado.
     *
     * Comprueba que corresponda a StudentActivityViewModel
     * antes de construirlo con su repositorio.
     */
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        // Verificamos que el ViewModel solicitado sea compatible.
        if (modelClass.isAssignableFrom(
                StudentActivityViewModel::class.java
            )
        ) {

            // Creamos el ViewModel entregándole AppRepository.
            // El cast es necesario por el tipo genérico T.
            @Suppress("UNCHECKED_CAST")
            return StudentActivityViewModel(repository) as T
        }

        // Informamos si se solicita un ViewModel no compatible.
        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
