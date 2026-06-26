package com.example.genshin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "hidden_characters",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HiddenCharacter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val characterId: String
)
