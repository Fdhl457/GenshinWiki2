package com.example.genshin.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.genshin.data.local.entity.Bookmarked
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmarked: Bookmarked): Long

    @Delete
    suspend fun removeBookmark(bookmarked: Bookmarked): Int

    @Query("SELECT * FROM bookmarked WHERE userId = :userId")
    fun getBookmarksByUser(userId: Int): Flow<List<Bookmarked>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked WHERE userId = :userId AND catalogId = :catalogId)")
    fun isBookmarked(userId: Int, catalogId: String): Flow<Boolean>

    @Query("DELETE FROM bookmarked WHERE userId = :userId AND catalogId = :catalogId")
    suspend fun removeBookmarkByIds(userId: Int, catalogId: String): Int
}
