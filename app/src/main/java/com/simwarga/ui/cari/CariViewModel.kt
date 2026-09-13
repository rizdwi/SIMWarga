package com.simwarga.ui.cari

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.simwarga.SIMWargaApp
import com.simwarga.data.repository.WargaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class CariViewModel(private val repository: WargaRepository) : ViewModel() {

    val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = query.flatMapLatest { q ->
        if (q.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.search(q)
        }
    }.asLiveData()

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CariViewModel(app.repository) as T
        }
    }
}
