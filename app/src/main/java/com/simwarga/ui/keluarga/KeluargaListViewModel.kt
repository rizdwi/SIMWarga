package com.simwarga.ui.keluarga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.simwarga.SIMWargaApp
import com.simwarga.data.repository.WargaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class KeluargaListViewModel(private val repository: WargaRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val keluargaList = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllKeluarga()
        } else {
            repository.search(query)
        }
    }.asLiveData()

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KeluargaListViewModel(app.repository) as T
        }
    }
}
