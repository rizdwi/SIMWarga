package com.simwarga.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class KeluargaWithAnggota(
    @Embedded val keluarga: Keluarga,
    @Relation(
        parentColumn = "id",
        entityColumn = "kk_id"
    )
    val anggotaList: List<Anggota>
)
