package com.simwarga.ui.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.data.repository.ExcelImportResult
import com.simwarga.databinding.FragmentBackupBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BackupViewModel by viewModels {
        BackupViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToExcel(requireContext(), uri)
        }
    }

    private val templateLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) {
            viewModel.exportTemplate(requireContext(), uri)
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            showImportConfirmation(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnExportExcel.setOnClickListener {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val defaultFileName = "simwarga_data_$dateStr.xlsx"
            exportLauncher.launch(defaultFileName)
        }

        binding.btnDownloadTemplate.setOnClickListener {
            templateLauncher.launch("simwarga_format_contoh.xlsx")
        }

        binding.btnImportExcel.setOnClickListener {
            val mimeTypes = arrayOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel",
                "text/comma-separated-values",
                "text/csv",
                "*/*"
            )
            importLauncher.launch(mimeTypes)
        }
    }

    private fun showImportConfirmation(uri: android.net.Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.import_confirm_title)
            .setMessage(R.string.import_confirm_msg)
            .setPositiveButton(R.string.lanjutkan) { _, _ ->
                viewModel.importFromExcel(requireContext(), uri)
            }
            .setNegativeButton(R.string.batal, null)
            .show()
    }

    private fun showImportResultDialog(result: ExcelImportResult) {
        val skippedText = if (result.skippedRows.isNotEmpty()) {
            "\n\nCatatan (" + result.skippedRows.size + " baris dilewati):\n" +
                    result.skippedRows.take(5).joinToString("\n") { "• $it" } +
                    if (result.skippedRows.size > 5) "\n• ...dan ${result.skippedRows.size - 5} lainnya." else ""
        } else ""

        val message = getString(
            R.string.import_result_msg,
            result.totalRowsProcessed,
            result.kkAddedOrUpdated,
            result.anggotaAddedOrUpdated,
            skippedText
        )

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.import_result_title)
            .setMessage(message)
            .setPositiveButton(R.string.tutup, null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is ExcelEvent.Loading -> {
                        binding.progressBar.visibility = if (event.isLoading) View.VISIBLE else View.GONE
                        binding.btnExportExcel.isEnabled = !event.isLoading
                        binding.btnImportExcel.isEnabled = !event.isLoading
                        binding.btnDownloadTemplate.isEnabled = !event.isLoading
                    }
                    is ExcelEvent.ExportSuccess -> {
                        Snackbar.make(binding.root, event.messageRes, Snackbar.LENGTH_LONG).show()
                    }
                    is ExcelEvent.TemplateSuccess -> {
                        Snackbar.make(binding.root, event.messageRes, Snackbar.LENGTH_LONG).show()
                    }
                    is ExcelEvent.ImportSuccess -> {
                        showImportResultDialog(event.result)
                    }
                    is ExcelEvent.Error -> {
                        val msg = getString(event.messageRes) + if (event.detail != null) ": ${event.detail}" else ""
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
