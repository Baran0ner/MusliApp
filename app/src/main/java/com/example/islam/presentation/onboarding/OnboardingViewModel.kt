package com.example.islam.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    /**
     * Onboarding'in tamamlandığını DataStore'a kaydeder.
     * Sonrasında NavGraph startDestination Home olarak yüklenir.
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            // Onboarding tamamlayıp Home'a geçen kullanıcılar için tekrar isim kartı gösterme.
            prefsDataStore.setOnboardingCompleted(suppressNamePrompt = true)
        }
    }

    fun saveDisplayName(name: String) {
        viewModelScope.launch { prefsDataStore.updateDisplayName(name) }
    }

    fun seedDisplayNameIfEmpty(name: String?) {
        viewModelScope.launch { prefsDataStore.seedDisplayNameIfEmpty(name) }
    }
}
