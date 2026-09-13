package com.simwarga.ui.keluarga

import androidx.lifecycle.*
import com.simwarga.SIMWargaApp
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga
import com.simwarga.data.model.KeluargaWithAnggota
import com.simwarga.data.repository.WargaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class KeluargaDetailViewModel(private val repository: WargaRepository) : ViewModel() {

    private val kkIdFlow = MutableStateFlow(-1)

    val keluargaWithAnggota: LiveData<KeluargaWithAnggota?> = kkIdFlow.flatMapLatest { id ->
        repository.getKeluargaById(id)
    }.asLiveData()

    fun setKkId(id: Int) {
        kkIdFlow.value = id
    }

    fun deleteAnggota(anggota: Anggota, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAnggota(anggota)
            onComplete()
        }
    }

    fun deleteKeluarga(keluarga: Keluarga, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteKeluarga(keluarga)
            onComplete()
        }
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KeluargaDetailViewModel(app.repository) as T
        }
    }
}
