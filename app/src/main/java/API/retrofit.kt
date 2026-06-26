package API

import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

// Model untuk Detail Karakter
@Serializable
data class CharacterDetail(
    val id: String = "",
    val name: String = "",
    val vision: String = "",
    val weapon: String = "",
    val nation: String = "",
    val description: String = "",
    val rarity: Int = 0
)

interface GenshinApiService {
    // Endpoint mendapatkan list semua ID karakter
    @GET("characters")
    suspend fun getCharacterList(): List<String>

    // Endpoint mendapatkan detail satu karakter
    @GET("characters/{id}")
    suspend fun getCharacterDetail(@Path("id") characterId: String): CharacterDetail
}

object GenshinApiClient {
    const val BASE_URL = "https://genshin.jmp.blue/"

    val service: GenshinApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GenshinApiService::class.java)
    }
}