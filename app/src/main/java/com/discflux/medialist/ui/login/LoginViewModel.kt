package com.discflux.medialist.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discflux.medialist.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBypassed = MutableStateFlow(false)
    val isBypassed: StateFlow<Boolean> = _isBypassed.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect {
                _user.value = it
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signInWithGoogle(idToken)
            _isLoading.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInAnonymously()
            result.onFailure {
                Log.e("LoginViewModel", "Anonymous sign-in error: ${it.message}")
                // Fallback to local-only bypass if Firebase is not configured
                _isBypassed.value = true
            }
            _isLoading.value = false
        }
    }

    fun forceBypass() {
        _isBypassed.value = true
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
