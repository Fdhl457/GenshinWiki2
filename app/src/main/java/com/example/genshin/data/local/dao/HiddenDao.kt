package com.example.genshin.data.local.dao

import androidx.room.*
import com.example.genshin.data.local.entity.HiddenCharacter
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenDao {
    @Query("SELECT * FROM hidden_characters WHERE userId = :userId")
    fun getHiddenCharactersByUser(userId: Int): Flow<List<HiddenCharacter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHiddenCharacter(hiddenCharacter: HiddenCharacter)

    @Query("DELETE FROM hidden_characters WHERE userId = :userId AND characterId = :characterId")
    suspend fun removeHiddenCharacter(userId: Int, characterId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM hidden_characters WHERE userId = :userId AND characterId = :characterId)")
    fun isHidden(userId: Int, characterId: String): Flow<Boolean>
}
