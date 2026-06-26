package API

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.genshin.data.local.database.AppDatabase
import com.example.genshin.data.local.entity.Bookmarked
import com.example.genshin.data.pref.AuthDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GenshinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val authDataStore = AuthDataStore(application)

    // State untuk list karakter di katalog
    var charactersList by mutableStateOf<List<CharacterDetail>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    // Karakter yang sedang dipilih untuk detail
    var selectedCharacter by mutableStateOf<CharacterDetail?>(null)
        private set

    // Flow untuk karakter yang dibookmark oleh user saat ini
    val bookmarkedCharacters: Flow<List<Bookmarked>> = authDataStore.userId.flatMapLatest { userId ->
        if (userId != null) {
            db.bookmarkedDao().getBookmarksByUser(userId)
        } else {
            emptyFlow()
        }
    }

    init {
        fetchCharactersList()
    }

    // Mengambil list ID dan detail setiap karakter untuk katalog
    fun fetchCharactersList() {
        viewModelScope.launch {
            isLoading = true
            try {
                val ids = GenshinApiClient.service.getCharacterList()
                // Mengambil detail secara paralel untuk setiap ID
                val details = ids.map { id ->
                    async {
                        try {
                            // Map ID ke dalam objek detail agar kita tahu ID-nya
                            GenshinApiClient.service.getCharacterDetail(id).copy(id = id)
                        } catch (e: Exception) {
                            // Fallback jika detail gagal diambil
                            CharacterDetail(id = id, name = id.replaceFirstChar { it.uppercase() })
                        }
                    }
                }.awaitAll()
                charactersList = details
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // Menambah/menghapus bookmark
    fun toggleBookmark(character: CharacterDetail) {
        viewModelScope.launch {
            val userId = authDataStore.userId.first() ?: return@launch
            val isCurrentlyBookmarked = db.bookmarkedDao().isBookmarked(userId, character.id).first()
            
            if (isCurrentlyBookmarked) {
                db.bookmarkedDao().removeBookmarkByIds(userId, character.id)
            } else {
                db.bookmarkedDao().addBookmark(
                    Bookmarked(
                        userId = userId,
                        catalogId = character.id,
                        title = character.name,
                        imageUrl = "https://genshin.jmp.blue/characters/${character.id}/icon-big"
                    )
                )
            }
        }
    }

    // Mengecek apakah karakter dibookmark
    fun isCharacterBookmarked(characterId: String): Flow<Boolean> {
        return authDataStore.userId.flatMapLatest { userId ->
            if (userId != null) {
                db.bookmarkedDao().isBookmarked(userId, characterId)
            } else {
                emptyFlow()
            }
        }
    }

    fun selectCharacter(character: CharacterDetail) {
        selectedCharacter = character
    }

    fun clearSelection() {
        selectedCharacter = null
    }
}
