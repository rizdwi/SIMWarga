package com.simwarga.ui.anggota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simwarga.SIMWargaApp
import com.simwarga.data.model.Anggota
import com.simwarga.data.repository.WargaRepository
import com.simwarga.ui.keluarga.FormEvent
import com.simwarga.util.ValidationHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnggotaFormViewModel(private val repository: WargaRepository) : ViewModel() {

    private val _existingAnggota = MutableStateFlow<Anggota?>(null)
    val existingAnggota = _existingAnggota.asStateFlow()

    private val _events = MutableSharedFlow<FormEvent>()
    val events = _events.asSharedFlow()

    fun loadAnggota(id: Int) {
        if (id == -1) return
        viewModelScope.launch {
            _existingAnggota.value = repository.getAnggotaById(id)
        }
    }

    fun save(
        id: Int,
        kkId: Int,
        nik: String,
        namaLengkap: String,
        jenisKelamin: String,
        tempatLahir: String,
        tanggalLahir: Long,
        statusHubungan: String,
        agama: String,
        pendidikan: String,
        pekerjaan: String,
        statusPerkawinan: String,
        kewarganegaraan: String
    ) {
        viewModelScope.launch {
            val cleanNik = nik.trim()
            if (!ValidationHelper.isValidNik(cleanNik)) {
                _events.emit(FormEvent.ValidationError("nik", com.simwarga.R.string.err_nik_invalid))
                return@launch
            }

            // Check duplicate NIK
            val existing = repository.getByNik(cleanNik)
            if (existing != null && existing.id != id) {
                _events.emit(FormEvent.ValidationError("nik", com.simwarga.R.string.err_nik_duplicate))
                return@launch
            }

            if (namaLengkap.isBlank()) {
                _events.emit(FormEvent.ValidationError("namaLengkap", com.simwarga.R.string.err_field_required))
                return@launch
            }

            if (tanggalLahir <= 0L) {
                _events.emit(FormEvent.ValidationError("tanggalLahir", com.simwarga.R.string.err_field_required))
                return@launch
            }

            if (statusHubungan.isBlank()) {
                _events.emit(FormEvent.ValidationError("statusHubungan", com.simwarga.R.string.err_field_required))
                return@launch
            }

            val anggota = Anggota(
                id = if (id > 0) id else 0,
                kkId = kkId,
                nik = cleanNik,
                namaLengkap = namaLengkap.trim(),
                jenisKelamin = jenisKelamin,
                tempatLahir = tempatLahir.trim(),
                tanggalLahir = tanggalLahir,
                statusHubungan = statusHubungan.trim(),
                agama = agama.trim(),
                pendidikan = pendidikan.trim(),
                pekerjaan = pekerjaan.trim(),
                statusPerkawinan = statusPerkawinan.trim(),
                kewarganegaraan = if (kewarganegaraan.isBlank()) "WNI" else kewarganegaraan.trim(),
                updatedAt = System.currentTimeMillis()
            )

            if (id > 0) {
                repository.updateAnggota(anggota)
                _events.emit(FormEvent.Success(com.simwarga.R.string.msg_simpan_sukses, id))
            } else {
                val newId = repository.insertAnggota(anggota)
                _events.emit(FormEvent.Success(com.simwarga.R.string.msg_simpan_sukses, newId.toInt()))
            }
        }
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnggotaFormViewModel(app.repository) as T
        }
    }
}
