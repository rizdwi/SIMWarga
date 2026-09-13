package com.simwarga.ui.keluarga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.databinding.FragmentKeluargaFormBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class KeluargaFormFragment : Fragment() {

    private var _binding: FragmentKeluargaFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KeluargaFormViewModel by viewModels {
        KeluargaFormViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private var currentKkId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeluargaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentKkId = arguments?.getInt("kkId", -1) ?: -1

        setupToolbar()
        setupListeners()
        observeViewModel()

        if (currentKkId != -1) {
            binding.toolbar.setTitle(R.string.form_kk_edit_judul)
            viewModel.loadKeluarga(currentKkId)
        } else {
            binding.toolbar.setTitle(R.string.form_kk_tambah_judul)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.btnBatal.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etNoKk.doAfterTextChanged { binding.tilNoKk.error = null }
        binding.etAlamat.doAfterTextChanged { binding.tilAlamat.error = null }
        binding.etRt.doAfterTextChanged { binding.tilRt.error = null }
        binding.etRw.doAfterTextChanged { binding.tilRw.error = null }
        binding.etKelurahan.doAfterTextChanged { binding.tilKelurahan.error = null }
        binding.etKecamatan.doAfterTextChanged { binding.tilKecamatan.error = null }
        binding.etKabupaten.doAfterTextChanged { binding.tilKabupaten.error = null }

        binding.btnSimpan.setOnClickListener {
            viewModel.save(
                id = currentKkId,
                noKk = binding.etNoKk.text?.toString().orEmpty(),
                alamat = binding.etAlamat.text?.toString().orEmpty(),
                rt = binding.etRt.text?.toString().orEmpty(),
                rw = binding.etRw.text?.toString().orEmpty(),
                kelurahan = binding.etKelurahan.text?.toString().orEmpty(),
                kecamatan = binding.etKecamatan.text?.toString().orEmpty(),
                kabupaten = binding.etKabupaten.text?.toString().orEmpty()
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.existingKeluarga.collectLatest { keluarga ->
                if (keluarga != null) {
                    binding.etNoKk.setText(keluarga.noKk)
                    binding.etAlamat.setText(keluarga.alamat)
                    binding.etRt.setText(keluarga.rt)
                    binding.etRw.setText(keluarga.rw)
                    binding.etKelurahan.setText(keluarga.kelurahan)
                    binding.etKecamatan.setText(keluarga.kecamatan)
                    binding.etKabupaten.setText(keluarga.kabupaten)
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
                            "noKk" -> binding.tilNoKk.error = getString(event.messageRes)
                            "alamat" -> binding.tilAlamat.error = getString(event.messageRes)
                            "rt" -> binding.tilRt.error = getString(event.messageRes)
                            "rw" -> binding.tilRw.error = getString(event.messageRes)
                            "kelurahan" -> binding.tilKelurahan.error = getString(event.messageRes)
                            "kecamatan" -> binding.tilKecamatan.error = getString(event.messageRes)
                            "kabupaten" -> binding.tilKabupaten.error = getString(event.messageRes)
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
