package com.simwarga.ui.keluarga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simwarga.SIMWargaApp
import com.simwarga.data.model.Keluarga
import com.simwarga.data.repository.WargaRepository
import com.simwarga.util.ValidationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FormEvent {
    data class Success(val messageRes: Int, val keluargaId: Int) : FormEvent()
    data class ValidationError(val field: String, val messageRes: Int) : FormEvent()
    data class GeneralError(val messageRes: Int) : FormEvent()
}

class KeluargaFormViewModel(private val repository: WargaRepository) : ViewModel() {

    private val _existingKeluarga = MutableStateFlow<Keluarga?>(null)
    val existingKeluarga = _existingKeluarga.asStateFlow()

    private val _events = MutableSharedFlow<FormEvent>()
    val events = _events.asSharedFlow()

    fun loadKeluarga(id: Int) {
        if (id == -1) return
        viewModelScope.launch {
            _existingKeluarga.value = repository.getKeluargaDirect(id)
        }
    }

    fun save(
        id: Int,
        noKk: String,
        alamat: String,
        rt: String,
        rw: String,
        kelurahan: String,
        kecamatan: String,
        kabupaten: String
    ) {
        viewModelScope.launch {
            val cleanNoKk = noKk.trim()
            if (!ValidationHelper.isValidNoKk(cleanNoKk)) {
                _events.emit(FormEvent.ValidationError("noKk", com.simwarga.R.string.err_no_kk_invalid))
                return@launch
            }

            // Check duplicate
            val existing = repository.getByNoKk(cleanNoKk)
            if (existing != null && existing.id != id) {
                _events.emit(FormEvent.ValidationError("noKk", com.simwarga.R.string.err_no_kk_duplicate))
                return@launch
            }

            if (alamat.isBlank()) {
                _events.emit(FormEvent.ValidationError("alamat", com.simwarga.R.string.err_field_required))
                return@launch
            }
            if (rt.isBlank()) {
                _events.emit(FormEvent.ValidationError("rt", com.simwarga.R.string.err_field_required))
                return@launch
            }
            if (rw.isBlank()) {
                _events.emit(FormEvent.ValidationError("rw", com.simwarga.R.string.err_field_required))
                return@launch
            }
            if (kelurahan.isBlank()) {
                _events.emit(FormEvent.ValidationError("kelurahan", com.simwarga.R.string.err_field_required))
                return@launch
            }
            if (kecamatan.isBlank()) {
                _events.emit(FormEvent.ValidationError("kecamatan", com.simwarga.R.string.err_field_required))
                return@launch
            }
            if (kabupaten.isBlank()) {
                _events.emit(FormEvent.ValidationError("kabupaten", com.simwarga.R.string.err_field_required))
                return@launch
            }

            val keluarga = Keluarga(
                id = if (id > 0) id else 0,
                noKk = cleanNoKk,
                alamat = alamat.trim(),
                rt = rt.trim(),
                rw = rw.trim(),
                kelurahan = kelurahan.trim(),
                kecamatan = kecamatan.trim(),
                kabupaten = kabupaten.trim(),
                updatedAt = System.currentTimeMillis()
            )

            if (id > 0) {
                repository.updateKeluarga(keluarga)
                _events.emit(FormEvent.Success(com.simwarga.R.string.msg_simpan_sukses, id))
            } else {
                val newId = repository.insertKeluarga(keluarga)
                _events.emit(FormEvent.Success(com.simwarga.R.string.msg_simpan_sukses, newId.toInt()))
            }
        }
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KeluargaFormViewModel(app.repository) as T
        }
    }
}
