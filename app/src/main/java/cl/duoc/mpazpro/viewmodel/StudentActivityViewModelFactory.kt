package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duoc.mpazpro.data.repository.AppRepository

class StudentActivityViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(
                StudentActivityViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return StudentActivityViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
