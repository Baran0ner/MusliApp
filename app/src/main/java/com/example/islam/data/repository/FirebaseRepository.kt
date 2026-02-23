package com.example.islam.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Auth, FCM ve Firestore işlemlerini tek noktada toplar.
 *
 * Kullanım alanları:
 *  - Google ile giriş / çıkış
 *  - Mevcut kullanıcı durumu (Flow)
 *  - FCM token alma + Firestore'a kaydetme
 *  - Kullanıcı profilini Firestore'a yazma / okuma
 *  - FCM topic aboneliği (ramazan, genel duyurular)
 */
@Singleton
class FirebaseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {

    // ─────────────────────────────────────────────────────────────────────────
    // Auth State
    // ─────────────────────────────────────────────────────────────────────────

    /** Kullanıcı oturum durumunu reaktif olarak izler. */
    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn: Boolean get() = auth.currentUser != null

    // ─────────────────────────────────────────────────────────────────────────
    // Google Sign-In
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Google ID token'ı alındıktan sonra Firebase credential ile giriş yapar.
     * OneTap / legacy GoogleSignIn flow'dan gelen idToken buraya gelir.
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("Kullanıcı bilgisi alınamadı"))

            // Auth başarılı → Firestore/FCM işlemleri arka planda, auth'u bloklamasın
            try { saveUserProfile(user) } catch (_: Exception) { }
            try { saveFcmToken(user.uid) } catch (_: Exception) { }
            subscribeToTopics()

            Result.success(user)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        // GoogleSignIn oturumunu da temizle
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Firestore — Kullanıcı Profili
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun saveUserProfile(user: FirebaseUser) {
        val data = mapOf(
            "uid"         to user.uid,
            "displayName" to (user.displayName ?: ""),
            "email"       to (user.email ?: ""),
            "photoUrl"    to (user.photoUrl?.toString() ?: ""),
            "lastSeen"    to com.google.firebase.Timestamp.now()
        )
        firestore.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    /** Kullanıcının namaz streak'ini Firestore'a yazar (opsiyonel senkron).
     *  set(merge) kullanılır — belge yoksa oluşturur, varsa yalnızca bu alanı günceller.
     */
    suspend fun syncStreak(streak: Int) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .set(mapOf("prayerStreak" to streak), SetOptions.merge())
            .await()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FCM — Token & Topics
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun saveFcmToken(uid: String) {
        try {
            val token = messaging.token.await()
            firestore.collection("users").document(uid)
                .update("fcmToken", token)
                .await()
        } catch (_: Exception) { /* Token güncellemesi kritik değil */ }
    }

    private fun subscribeToTopics() {
        // Tüm kullanıcılara genel duyurular
        messaging.subscribeToTopic("general")
        // Ramazan özel bildirimleri
        messaging.subscribeToTopic("ramazan")
    }

    fun unsubscribeFromTopics() {
        messaging.unsubscribeFromTopic("general")
        messaging.unsubscribeFromTopic("ramazan")
    }
}
