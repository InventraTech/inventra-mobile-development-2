package com.inventraoficial.inventra.data.remote.auth.google

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val webClientId: String,
    private val firebaseAuth: FirebaseAuth,
) {
    suspend fun signIn(activityContext: Context): Result<String> =
        try {
            Result.success(firebaseIdToken(activityContext))
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleSignInCancelledException(e))
        } catch (e: NoCredentialException) {
            Result.failure(Exception(NO_ACCOUNT_ERROR, e))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(GENERIC_ERROR, e))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception(GENERIC_ERROR, e))
        } catch (e: FirebaseException) {
            Result.failure(Exception(GENERIC_ERROR, e))
        } catch (e: IllegalStateException) {
            Result.failure(Exception(GENERIC_ERROR, e))
        }

    private suspend fun firebaseIdToken(activityContext: Context): String {
        val googleIdToken = getGoogleIdToken(activityContext)
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val user =
            firebaseAuth.signInWithCredential(credential).await().user
                ?: error("O Firebase não devolveu o usuário.")
        val token =
            user.getIdToken(false).await().token
                ?: error("O Firebase não devolveu o token.")
        firebaseAuth.signOut()
        return token
    }

    private suspend fun getGoogleIdToken(activityContext: Context): String {
        val credentialManager = CredentialManager.create(activityContext)
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(option)
                .build()

        val credential = credentialManager.getCredential(activityContext, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            error("Credencial inesperada.")
        }
    }

    private companion object {
        const val NO_ACCOUNT_ERROR = "Nenhuma conta Google encontrada neste aparelho."
        const val GENERIC_ERROR = "Não foi possível entrar com o Google. Tente novamente."
    }
}

class GoogleSignInCancelledException(
    cause: Throwable,
) : Exception("Login com Google cancelado.", cause)
