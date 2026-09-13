package com.simwarga.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.simwarga.SIMWargaApp
import com.simwarga.data.repository.WargaRepository
import kotlinx.coroutines.flow.combine

data class DashboardSummary(
    val totalKk: Int = 0,
    val totalJiwa: Int = 0,
    val totalLaki: Int = 0,
    val totalPerempuan: Int = 0
)

class DashboardViewModel(repository: WargaRepository) : ViewModel() {

    val summary = combine(
        repository.countKk(),
        repository.countJiwa(),
        repository.countLaki(),
        repository.countPerempuan()
    ) { kk, jiwa, laki, perempuan ->
        DashboardSummary(
            totalKk = kk,
            totalJiwa = jiwa,
            totalLaki = laki,
            totalPerempuan = perempuan
        )
    }.asLiveData()

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(app.repository) as T
        }
    }
}
