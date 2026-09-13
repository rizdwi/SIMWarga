package com.simwarga.ui.anggota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.databinding.FragmentAnggotaFormBinding
import com.simwarga.ui.keluarga.FormEvent
import com.simwarga.util.DateHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnggotaFormFragment : Fragment() {

    private var _binding: FragmentAnggotaFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnggotaFormViewModel by viewModels {
        AnggotaFormViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private var currentKkId: Int = -1
    private var currentAnggotaId: Int = -1
    private var selectedTanggalLahir: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnggotaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentKkId = arguments?.getInt("kkId", -1) ?: -1
        currentAnggotaId = arguments?.getInt("anggotaId", -1) ?: -1

        if (currentKkId == -1) {
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupDropdowns()
        setupDatePicker()
        setupListeners()
        observeViewModel()

        if (currentAnggotaId != -1) {
            binding.toolbar.setTitle(R.string.form_anggota_edit_judul)
            viewModel.loadAnggota(currentAnggotaId)
        } else {
            binding.toolbar.setTitle(R.string.form_anggota_tambah_judul)
            binding.actKewarganegaraan.setText("WNI", false)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupDropdowns() {
        val context = requireContext()

        val adapterHubungan = ArrayAdapter.createFromResource(
            context,
            R.array.status_hubungan_array,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.actStatusHubungan.setAdapter(adapterHubungan)

        val adapterAgama = ArrayAdapter.createFromResource(
            context,
            R.array.agama_array,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.actAgama.setAdapter(adapterAgama)

        val adapterPendidikan = ArrayAdapter.createFromResource(
            context,
            R.array.pendidikan_array,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.actPendidikan.setAdapter(adapterPendidikan)

        val adapterPerkawinan = ArrayAdapter.createFromResource(
            context,
            R.array.status_perkawinan_array,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.actStatusPerkawinan.setAdapter(adapterPerkawinan)

        val adapterKewarganegaraan = ArrayAdapter.createFromResource(
            context,
            R.array.kewarganegaraan_array,
            android.R.layout.simple_dropdown_item_1line
        )
        binding.actKewarganegaraan.setAdapter(adapterKewarganegaraan)
    }

    private fun setupDatePicker() {
        val datePickerListener = View.OnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.pilih_tanggal)
                .apply {
                    if (selectedTanggalLahir > 0L) {
                        setSelection(selectedTanggalLahir)
                    }
                }
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                selectedTanggalLahir = selection
                binding.etTanggalLahir.setText(DateHelper.formatDisplay(selection))
                binding.tilTanggalLahir.error = null
            }

            picker.show(childFragmentManager, "DATE_PICKER")
        }

        binding.etTanggalLahir.setOnClickListener(datePickerListener)
        binding.tilTanggalLahir.setEndIconOnClickListener(datePickerListener)
    }

    private fun setupListeners() {
        binding.btnBatal.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etNik.doAfterTextChanged { binding.tilNik.error = null }
        binding.etNamaLengkap.doAfterTextChanged { binding.tilNamaLengkap.error = null }
        binding.actStatusHubungan.doAfterTextChanged { binding.tilStatusHubungan.error = null }

        binding.btnSimpan.setOnClickListener {
            val jenisKelamin = if (binding.rbLaki.isChecked) "L" else "P"
            viewModel.save(
                id = currentAnggotaId,
                kkId = currentKkId,
                nik = binding.etNik.text?.toString().orEmpty(),
                namaLengkap = binding.etNamaLengkap.text?.toString().orEmpty(),
                jenisKelamin = jenisKelamin,
                tempatLahir = binding.etTempatLahir.text?.toString().orEmpty(),
                tanggalLahir = selectedTanggalLahir,
                statusHubungan = binding.actStatusHubungan.text?.toString().orEmpty(),
                agama = binding.actAgama.text?.toString().orEmpty(),
                pendidikan = binding.actPendidikan.text?.toString().orEmpty(),
                pekerjaan = binding.etPekerjaan.text?.toString().orEmpty(),
                statusPerkawinan = binding.actStatusPerkawinan.text?.toString().orEmpty(),
                kewarganegaraan = binding.actKewarganegaraan.text?.toString().orEmpty()
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.existingAnggota.collectLatest { anggota ->
                if (anggota != null) {
                    binding.etNik.setText(anggota.nik)
                    binding.etNamaLengkap.setText(anggota.namaLengkap)
                    if (anggota.jenisKelamin == "P") {
                        binding.rbPerempuan.isChecked = true
                    } else {
                        binding.rbLaki.isChecked = true
                    }
                    binding.etTempatLahir.setText(anggota.tempatLahir)
                    selectedTanggalLahir = anggota.tanggalLahir
                    binding.etTanggalLahir.setText(DateHelper.formatDisplay(anggota.tanggalLahir))
                    binding.actStatusHubungan.setText(anggota.statusHubungan, false)
                    binding.actAgama.setText(anggota.agama, false)
                    binding.actPendidikan.setText(anggota.pendidikan, false)
                    binding.etPekerjaan.setText(anggota.pekerjaan)
                    binding.actStatusPerkawinan.setText(anggota.statusPerkawinan, false)
                    binding.actKewarganegaraan.setText(anggota.kewarganegaraan, false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is FormEvent.Success -> {
                        Snackbar.make(binding.root, event.messageRes, Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is FormEvent.ValidationError -> {
                        when (event.field) {
                            "nik" -> binding.tilNik.error = getString(event.messageRes)
                            "namaLengkap" -> binding.tilNamaLengkap.error = getString(event.messageRes)
                            "tanggalLahir" -> binding.tilTanggalLahir.error = getString(event.messageRes)
                            "statusHubungan" -> binding.tilStatusHubungan.error = getString(event.messageRes)
                        }
                    }
                    is FormEvent.GeneralError -> {
                        Snackbar.make(binding.root, event.messageRes, Snackbar.LENGTH_LONG).show()
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
