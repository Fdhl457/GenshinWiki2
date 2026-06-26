package com.example.genshin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.genshin.data.local.database.AppDatabase
import com.example.genshin.data.local.entity.User
import com.example.genshin.data.pref.AuthDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val authDataStore = AuthDataStore(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = authDataStore.isLoggedIn.first()
            if (isLoggedIn) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun register(user: User, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                if (user.name.isEmpty() || user.email.isEmpty() || user.password.isEmpty()) {
                    onResult(false, "Semua field harus diisi")
                    return@launch
                }
                db.userDao().registerUser(user)
                onResult(true, "Registrasi Berhasil")
            } catch (e: Exception) {
                onResult(false, "Email mungkin sudah terdaftar")
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isEmpty() || password.isEmpty()) {
                onResult(false, "Email dan password tidak boleh kosong")
                return@launch
            }
            val user = db.userDao().loginUser(email, password)
            if (user != null) {
                authDataStore.saveSession(user.id)
                _authState.value = AuthState.Authenticated
                onResult(true, "Login Berhasil")
            } else {
                onResult(false, "Kredensial salah")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authDataStore.clearSession()
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}
