package com.simwarga.ui.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.data.repository.ExcelImportResult
import com.simwarga.data.repository.WargaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class ExcelEvent {
    data class Loading(val isLoading: Boolean) : ExcelEvent()
    data class ExportSuccess(val messageRes: Int) : ExcelEvent()
    data class ImportSuccess(val result: ExcelImportResult) : ExcelEvent()
    data class TemplateSuccess(val messageRes: Int) : ExcelEvent()
    data class Error(val messageRes: Int, val detail: String? = null) : ExcelEvent()
}

class BackupViewModel(private val repository: WargaRepository) : ViewModel() {

    private val _events = MutableSharedFlow<ExcelEvent>()
    val events = _events.asSharedFlow()

    fun exportToExcel(context: Context, destinationUri: Uri) {
        viewModelScope.launch {
            _events.emit(ExcelEvent.Loading(true))
            try {
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    repository.exportToExcel(outputStream)
                }
                _events.emit(ExcelEvent.ExportSuccess(R.string.msg_excel_export_sukses))
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit(ExcelEvent.Error(R.string.msg_excel_export_gagal, e.message))
            } finally {
                _events.emit(ExcelEvent.Loading(false))
            }
        }
    }

    fun exportTemplate(context: Context, destinationUri: Uri) {
        viewModelScope.launch {
            _events.emit(ExcelEvent.Loading(true))
            try {
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    repository.exportTemplate(outputStream)
                }
                _events.emit(ExcelEvent.TemplateSuccess(R.string.msg_template_sukses))
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit(ExcelEvent.Error(R.string.msg_template_gagal, e.message))
            } finally {
                _events.emit(ExcelEvent.Loading(false))
            }
        }
    }

    fun importFromExcel(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            _events.emit(ExcelEvent.Loading(true))
            try {
                val result = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    repository.importFromExcel(inputStream)
                }
                if (result != null) {
                    _events.emit(ExcelEvent.ImportSuccess(result))
                } else {
                    _events.emit(ExcelEvent.Error(R.string.msg_excel_import_gagal, "Berkas tidak dapat dibuka"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit(ExcelEvent.Error(R.string.msg_excel_import_gagal, e.message))
            } finally {
                _events.emit(ExcelEvent.Loading(false))
            }
        }
    }

    class Factory(private val app: SIMWargaApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupViewModel(app.repository) as T
        }
    }
}
