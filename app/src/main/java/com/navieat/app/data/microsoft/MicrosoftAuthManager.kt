package com.navieat.app.data.microsoft

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import com.navieat.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Wraps MSAL for Microsoft personal/work accounts. The scopes we request are
 * the ones needed by Microsoft To Do via Graph (Tasks.ReadWrite).
 *
 * SETUP CHECKLIST (do this once in Azure Portal):
 *   1. portal.azure.com → Microsoft Entra ID → App registrations → New registration
 *   2. Name: NaviEat. Supported account types: "Accounts in any organizational
 *      directory and personal Microsoft accounts".
 *   3. Authentication → Add a platform → Android. Package name: com.navieat.app.
 *      Get the signature hash with:
 *        keytool -exportcert -alias androiddebugkey \
 *          -keystore ~/.android/debug.keystore -storepass android -keypass android \
 *          | openssl sha1 -binary | openssl base64
 *   4. API permissions → Microsoft Graph → Delegated → Tasks.ReadWrite.
 *   5. Copy the Application (client) ID into app/src/main/res/raw/msal_config.json
 *      and update the redirect_uri with your signature hash.
 */
@Singleton
class MicrosoftAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile private var app: ISingleAccountPublicClientApplication? = null
    @Volatile private var cachedUsername: String? = null

    private val scopes = arrayOf("Tasks.ReadWrite")

    /** Cached username (set after [signIn] succeeds). */
    fun currentAccount(): String? = cachedUsername

    /** Interactive sign-in. Returns the username, or null on cancel/error. */
    suspend fun signIn(activity: Activity): String? {
        val instance = ensureApp() ?: return null
        return suspendCancellableCoroutine { cont ->
            instance.signIn(activity, null, scopes, object : AuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) {
                    cachedUsername = result.account.username
                    cont.resume(cachedUsername)
                }
                override fun onError(exception: MsalException) { cont.resume(null) }
                override fun onCancel() { cont.resume(null) }
            })
        }
    }

    /** Acquire an access token silently if a session exists. */
    suspend fun acquireToken(): String? {
        val instance = app ?: return null
        return suspendCancellableCoroutine { cont ->
            try {
                val authority = instance.configuration
                    .defaultAuthority
                    .authorityURL
                    .toString()
                instance.acquireTokenSilentAsync(scopes, authority, object : SilentAuthenticationCallback {
                    override fun onSuccess(result: IAuthenticationResult) {
                        cachedUsername = result.account.username
                        cont.resume(result.accessToken)
                    }
                    override fun onError(exception: MsalException) { cont.resume(null) }
                })
            } catch (_: Exception) { cont.resume(null) }
        }
    }

    fun signOut() {
        try {
            app?.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() { cachedUsername = null }
                override fun onError(exception: MsalException) { /* swallow */ }
            })
        } catch (_: Exception) { /* swallow */ }
    }

    /** Lazily creates the MSAL client. Returns null on init failure. */
    private suspend fun ensureApp(): ISingleAccountPublicClientApplication? {
        app?.let { return it }
        return suspendCancellableCoroutine { cont: CancellableContinuation<ISingleAccountPublicClientApplication?> ->
            PublicClientApplication.createSingleAccountPublicClientApplication(
                context,
                R.raw.msal_config,
                object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                    override fun onCreated(application: ISingleAccountPublicClientApplication) {
                        app = application
                        cont.resume(application)
                    }
                    override fun onError(exception: MsalException) {
                        cont.resume(null)
                    }
                }
            )
        }
    }
}
