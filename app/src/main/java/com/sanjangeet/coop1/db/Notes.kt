package com.sanjangeet.coop1.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Notes (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val content: String
)
