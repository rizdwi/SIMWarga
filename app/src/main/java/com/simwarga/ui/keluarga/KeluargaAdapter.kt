package com.simwarga.ui.keluarga

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simwarga.R
import com.simwarga.data.model.KeluargaWithAnggota
import com.simwarga.databinding.ItemKeluargaBinding

class KeluargaAdapter(
    private val onItemClick: (KeluargaWithAnggota) -> Unit
) : ListAdapter<KeluargaWithAnggota, KeluargaAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: ItemKeluargaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KeluargaWithAnggota) {
            val context = binding.root.context
            binding.tvNoKk.text = item.keluarga.noKk
            binding.tvJumlahJiwa.text = context.getString(
                R.string.format_jumlah_jiwa,
                item.anggotaList.size
            )
            binding.tvRtRw.text = context.getString(
                R.string.format_rt_rw,
                item.keluarga.rt,
                item.keluarga.rw
            )
            binding.tvAlamat.text = item.keluarga.alamat

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKeluargaBinding.inflate(
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
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<KeluargaWithAnggota>() {
            override fun areItemsTheSame(
                oldItem: KeluargaWithAnggota,
                newItem: KeluargaWithAnggota
            ): Boolean = oldItem.keluarga.id == newItem.keluarga.id

            override fun areContentsTheSame(
                oldItem: KeluargaWithAnggota,
                newItem: KeluargaWithAnggota
            ): Boolean = oldItem == newItem
        }
    }
}
