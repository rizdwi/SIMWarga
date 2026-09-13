package com.simwarga.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simwarga.R
import com.simwarga.SIMWargaApp
import com.simwarga.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(requireActivity().application as SIMWargaApp)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            binding.tvTotalKk.text = summary.totalKk.toString()
            binding.tvTotalJiwa.text = summary.totalJiwa.toString()
            binding.tvTotalLaki.text = summary.totalLaki.toString()
            binding.tvTotalPerempuan.text = summary.totalPerempuan.toString()
        }

        binding.btnLihatSemuaKk.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_keluarga_list)
        }

        binding.btnTambahKk.setOnClickListener {
            findNavController().navigate(
                R.id.action_dashboard_to_keluarga_form,
                bundleOf("kkId" to -1)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
