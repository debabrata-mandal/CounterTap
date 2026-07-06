package com.countertap.customer.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countertap.customer.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String)      : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : ViewModel() {

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            delay(700)
            _isReady.value = true
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activityContext.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCred  = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(authCred).await()
                    _authState.value = AuthState.Success(authResult.user!!)
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Sign-in cancelled")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _authState.value = AuthState.Idle
    }
}
