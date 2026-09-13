package com.simwarga.ui.keluarga

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simwarga.R
import com.simwarga.data.model.Anggota
import com.simwarga.databinding.ItemAnggotaBinding
import com.simwarga.util.DateHelper

class AnggotaAdapter(
    private val onEditClick: (Anggota) -> Unit,
    private val onDeleteClick: (Anggota) -> Unit
) : ListAdapter<Anggota, AnggotaAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemAnggotaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Anggota) {
            val context = binding.root.context
            binding.tvNamaLengkap.text = item.namaLengkap
            binding.tvNik.text = "NIK: ${item.nik}"
            binding.tvHubungan.text = item.statusHubungan

            val genderLabel = if (item.jenisKelamin == "L") "Laki-laki" else "Perempuan"
            val genderColor = if (item.jenisKelamin == "L") R.color.male_color else R.color.female_color
            binding.ivGender.setColorFilter(ContextCompat.getColor(context, genderColor))

            val tglLahirStr = DateHelper.formatDisplay(item.tanggalLahir)
            val info = StringBuilder()
            info.append("$genderLabel | $tglLahirStr")
            if (item.pekerjaan.isNotBlank()) {
                info.append(" | ${item.pekerjaan}")
            }
            binding.tvRincian.text = info.toString()

            binding.btnEditAnggota.setOnClickListener { onEditClick(item) }
            binding.btnHapusAnggota.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnggotaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Anggota>() {
            override fun areItemsTheSame(oldItem: Anggota, newItem: Anggota): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Anggota, newItem: Anggota): Boolean =
                oldItem == newItem
        }
    }
}
