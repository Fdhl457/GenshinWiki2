package com.example.genshin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarked",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Bookmarked(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val catalogId: String, // ID from API
    val title: String,
    val imageUrl: String
)
