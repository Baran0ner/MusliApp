package com.example.islam.presentation.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.core.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Google Sign-In ekranı.
 *
 * ÖNEMLI: Bu ekran çalışmadan önce Firebase Console'da
 * Authentication → Sign-in method → Google'ı aktif etmeniz gerekir.
 * google-services.json içinde oauth_client listesi dolu olmalı.
 *
 * Web client ID'yi Firebase Console → Authentication → Settings → Web SDK configuration'dan alın.
 */
private const val WEB_CLIENT_ID =
    "379378206614-gb2ktl83u7snchvuaqj11ski46neggi9.apps.googleusercontent.com"
// ^ Firebase Console → Authentication → Sign-in providers → Google → Web client ID

@Composable
fun GoogleAuthScreen(
    navController: NavController,
    viewModel: GoogleAuthViewModel = hiltViewModel()
) {
    val context  = LocalContext.current
    val state    by viewModel.authState.collectAsState()
    val scope    = rememberCoroutineScope()

    // Giriş başarılıysa geri dön — önce navigate, sonra state sıfırla
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            navController.popBackStack()
            viewModel.resetState()
        }
    }

    // Google Sign-In launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (_: ApiException) {
                // Kullanıcı iptal etti veya hata oluştu
            }
        }
    }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier            = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier           = Modifier.size(64.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )

                Text(
                    text       = "Hesabına Giriş Yap",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )

                Text(
                    text      = "Namaz streak'ini cihazlar arasında senkronize et ve Ramazan bildirimlerini al.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Hata mesajı
                AnimatedVisibility(visible = state is AuthState.Error) {
                    Text(
                        text  = (state as? AuthState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                // Google Giriş Butonu
                // signOut() → hesap seçici her zaman açılır (force account picker)
                OutlinedButton(
                    onClick  = {
                        scope.launch {
                            try { googleSignInClient.signOut().await() } catch (_: Exception) { }
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = state !is AuthState.Loading,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    if (state is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text       = "Google ile Devam Et",
                            modifier   = Modifier.padding(vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp
                        )
                    }
                }

                // Geri dön (zorunlu değil)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        text  = "Şimdi Değil",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
