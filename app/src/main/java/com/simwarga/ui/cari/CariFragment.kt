package com.simwarga.ui.cari

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
import com.simwarga.databinding.FragmentCariBinding
import com.simwarga.ui.keluarga.KeluargaAdapter

class CariFragment : Fragment() {

    private var _binding: FragmentCariBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CariViewModel by viewModels {
        CariViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    private lateinit var adapter: KeluargaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCariBinding.inflate(inflater, container, false)
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
                R.id.action_cari_to_keluarga_detail,
                bundleOf("kkId" to item.keluarga.id)
            )
        }
        binding.rvHasilCari.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHasilCari.adapter = adapter
    }

    private fun setupListeners() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.onQueryChanged(text?.toString().orEmpty())
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val currentQuery = binding.etSearch.text?.toString().orEmpty()
            if (currentQuery.isBlank()) {
                binding.layoutInfo.visibility = View.VISIBLE
                binding.tvInfoMessage.setText(R.string.cari_petunjuk)
            } else if (list.isEmpty()) {
                binding.layoutInfo.visibility = View.VISIBLE
                binding.tvInfoMessage.setText(R.string.empty_keluarga_cari)
            } else {
                binding.layoutInfo.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
