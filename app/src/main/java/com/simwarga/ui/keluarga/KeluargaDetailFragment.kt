package com.simwarga.ui.keluarga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.data.model.Anggota
import com.simwarga.data.model.Keluarga
import com.simwarga.databinding.FragmentKeluargaDetailBinding

class KeluargaDetailFragment : Fragment() {

    private var _binding: FragmentKeluargaDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KeluargaDetailViewModel by viewModels {
        KeluargaDetailViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private var currentKkId: Int = -1
    private var currentKeluarga: Keluarga? = null
    private lateinit var anggotaAdapter: AnggotaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeluargaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentKkId = arguments?.getInt("kkId", -1) ?: -1
        if (currentKkId == -1) {
            findNavController().popBackStack()
            return
        }

        viewModel.setKkId(currentKkId)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.inflateMenu(R.menu.menu_keluarga_detail)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_kk -> {
                    findNavController().navigate(
                        R.id.action_keluarga_detail_to_keluarga_form,
                        bundleOf("kkId" to currentKkId)
                    )
                    true
                }
                R.id.action_hapus_kk -> {
                    showDeleteKkDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        anggotaAdapter = AnggotaAdapter(
            onEditClick = { anggota ->
                findNavController().navigate(
                    R.id.action_keluarga_detail_to_anggota_form,
                    bundleOf(
                        "kkId" to currentKkId,
                        "anggotaId" to anggota.id
                    )
                )
            },
            onDeleteClick = { anggota ->
                showDeleteAnggotaDialog(anggota)
            }
        )
        binding.rvAnggota.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnggota.adapter = anggotaAdapter
    }

    private fun setupListeners() {
        binding.fabTambahAnggota.setOnClickListener {
            findNavController().navigate(
                R.id.action_keluarga_detail_to_anggota_form,
                bundleOf(
                    "kkId" to currentKkId,
                    "anggotaId" to -1
                )
            )
        }
    }

    private fun observeViewModel() {
        viewModel.keluargaWithAnggota.observe(viewLifecycleOwner) { data ->
            if (data == null) {
                // Data telah dihapus, kembali
                return@observe
            }
            currentKeluarga = data.keluarga
            binding.tvDetailNoKk.text = data.keluarga.noKk
            binding.tvDetailAlamat.text = data.keluarga.alamat
            binding.tvDetailRtRw.text = getString(
                R.string.format_rt_rw,
                data.keluarga.rt,
                data.keluarga.rw
            )

            val wilayah = "${data.keluarga.kelurahan}, Kec. ${data.keluarga.kecamatan}, ${data.keluarga.kabupaten}"
            binding.tvDetailWilayah.text = wilayah

            binding.tvSectionAnggotaTitle.text = getString(
                R.string.section_anggota
            ) + " (${data.anggotaList.size} jiwa)"

            anggotaAdapter.submitList(data.anggotaList)

            if (data.anggotaList.isEmpty()) {
                binding.layoutEmptyAnggota.visibility = View.VISIBLE
            } else {
                binding.layoutEmptyAnggota.visibility = View.GONE
            }
        }
    }

    private fun showDeleteKkDialog() {
        val keluarga = currentKeluarga ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.hapus_kk_dialog_title)
            .setMessage(getString(R.string.hapus_kk_dialog_msg, keluarga.noKk))
            .setPositiveButton(R.string.hapus) { _, _ ->
                viewModel.deleteKeluarga(keluarga) {
                    Snackbar.make(binding.root, R.string.msg_hapus_sukses, Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton(R.string.batal, null)
            .show()
    }

    private fun showDeleteAnggotaDialog(anggota: Anggota) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.hapus_anggota_dialog_title)
            .setMessage(getString(R.string.hapus_anggota_dialog_msg, anggota.namaLengkap, anggota.nik))
            .setPositiveButton(R.string.hapus) { _, _ ->
                viewModel.deleteAnggota(anggota) {
                    Snackbar.make(binding.root, R.string.msg_hapus_sukses, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.batal, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
