package com.simwarga.ui.keluarga

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.databinding.FragmentKeluargaListBinding

class KeluargaListFragment : Fragment() {

    private var _binding: FragmentKeluargaListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: KeluargaListViewModel by viewModels {
        KeluargaListViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private lateinit var adapter: KeluargaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeluargaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = KeluargaAdapter { item ->
            findNavController().navigate(
                R.id.action_keluarga_list_to_keluarga_detail,
                bundleOf("kkId" to item.keluarga.id)
            )
        }
        binding.rvKeluarga.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKeluarga.adapter = adapter
    }

    private fun setupListeners() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }

        binding.fabTambahKk.setOnClickListener {
            findNavController().navigate(
                R.id.action_keluarga_list_to_keluarga_form,
                bundleOf("kkId" to -1)
            )
        }
    }

    private fun observeViewModel() {
        viewModel.keluargaList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val isQuerying = !binding.etSearch.text.isNullOrBlank()
            if (list.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvEmptyMessage.setText(
                    if (isQuerying) R.string.empty_keluarga_cari
                    else R.string.empty_keluarga
                )
            } else {
                binding.layoutEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
