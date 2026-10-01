package com.example.data.drive

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthManager(private val context: Context) {

    companion object {
        const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"
        const val OAUTH_SCOPE_PREFIX = "oauth2:$DRIVE_FILE_SCOPE"
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_FILE_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return getGoogleSignInClient().signInIntent
    }

    sealed class AuthResult {
        data class Success(val email: String, val accessToken: String) : AuthResult()
        data class NeedsUserConsent(val recoveryIntent: Intent) : AuthResult()
        data class Failure(val errorMessage: String, val statusCode: Int? = null) : AuthResult()
    }

    suspend fun handleSignInResult(data: Intent?): AuthResult = withContext(Dispatchers.IO) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val email = account.email ?: ""
            val androidAccount = account.account ?: Account(email, "com.google")

            try {
                val token = GoogleAuthUtil.getToken(context, androidAccount, OAUTH_SCOPE_PREFIX)
                AuthResult.Success(email = email, accessToken = token)
            } catch (e: UserRecoverableAuthException) {
                val intent = e.intent
                if (intent != null) {
                    AuthResult.NeedsUserConsent(intent)
                } else {
                    AuthResult.Failure("অনুমতি প্রয়োজন কিন্তু রিকভারি ইন্টেন্ট পাওয়া যায়নি।")
                }
            } catch (e: Exception) {
                AuthResult.Failure("অ্যাক্সেস টোকেন পেতে সমস্যা হয়েছে: ${e.localizedMessage ?: "Unknown error"}")
            }
        } catch (e: ApiException) {
            val msg = when (e.statusCode) {
                12501 -> "ব্যবহারকারী সাইন-ইন বাতিল করেছেন।"
                12500 -> "সাইন-ইন ব্যর্থ হয়েছে। Google Play Services বা নেটওয়ার্ক সংযোগ পরীক্ষা করুন।"
                10 -> "Developer Error (Status 10): Google Cloud Console-এ Android Client ID ও SHA-1 সঠিকভাবে কনফিগার করা নেই।"
                7 -> "নেটওয়ার্ক সংযোগ ত্রুটি (Status 7)।"
                else -> "সাইন-ইন ত্রুটি (Status ${e.statusCode}): ${e.localizedMessage ?: "অজ্ঞাত"}"
            }
            AuthResult.Failure(msg, e.statusCode)
        } catch (e: Exception) {
            AuthResult.Failure("Google Drive সংযোগ করা যায়নি: ${e.localizedMessage ?: "ত্রুটি"}")
        }
    }

    suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
        try {
            getGoogleSignInClient().signOut()
            true
        } catch (_: Exception) {
            false
        }
    }
}
