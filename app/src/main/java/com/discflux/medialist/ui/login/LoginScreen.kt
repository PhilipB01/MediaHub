package com.discflux.medialist.ui.login

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.discflux.medialist.R
import com.discflux.medialist.ui.theme.MediaListTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val isBypassed by viewModel.isBypassed.collectAsState()
    val viewModelLoading by viewModel.isLoading.collectAsState()
    var isCredentialManagerLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(user, isBypassed) {
        if (user != null || isBypassed) {
            Log.d("LoginScreen", "Navigation triggered: user=$user, isBypassed=$isBypassed")
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        isLoading = viewModelLoading || isCredentialManagerLoading,
        onLoginClick = {
            val serverClientId = context.getString(R.string.default_web_client_id)
            Log.d("LoginScreen", "Starting Google Sign-in")
            
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            coroutineScope.launch {
                isCredentialManagerLoading = true
                try {
                    val activity = context as? android.app.Activity
                    if (activity == null) return@launch

                    val result = credentialManager.getCredential(context = activity, request = request)
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                    }
                } catch (e: GetCredentialException) {
                    Log.e("LoginScreen", "Error: ${e.message}")
                } finally {
                    isCredentialManagerLoading = false
                }
            }
        },
        onBypassClick = {
            Log.d("LoginScreen", "Bypass clicked")
            viewModel.forceBypass()
        }
    )
}

@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onBypassClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Media Hub",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onLoginClick) {
                Text("Sign in with Google")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onBypassClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Bypass Sign-In (Dev Only)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MediaListTheme {
        LoginScreenContent(
            isLoading = false,
            onLoginClick = {},
            onBypassClick = {}
        )
    }
}
