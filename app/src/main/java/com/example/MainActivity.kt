package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.firebase.FirebaseFamilyRepository
import com.example.data.local.PreferencesManager
import com.example.data.model.DeviceRole
import com.example.data.model.DisguiseType
import com.example.service.LocationTrackerService
import com.example.ui.screens.DisguiseCalculatorScreen
import com.example.ui.screens.DisguiseNotesScreen
import com.example.ui.screens.KidScreen
import com.example.ui.screens.ParentMainScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy900

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: FirebaseFamilyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(applicationContext)
        repository = FirebaseFamilyRepository(applicationContext, preferencesManager)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Navy900
                ) {
                    SafeLinkAppContent(
                        preferencesManager = preferencesManager,
                        repository = repository,
                        onStartTrackingService = {
                            LocationTrackerService.startService(this)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SafeLinkAppContent(
    preferencesManager: PreferencesManager,
    repository: FirebaseFamilyRepository,
    onStartTrackingService: () -> Unit
) {
    val deviceRole by preferencesManager.deviceRole.collectAsState()
    val disguiseConfig by preferencesManager.disguiseConfig.collectAsState()
    val kidState by repository.currentKidState.collectAsState()

    AnimatedContent(
        targetState = Pair(deviceRole, disguiseConfig.isDisguiseActive),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppScreenTransition"
    ) { (role, isDisguiseActive) ->
        when (role) {
            DeviceRole.UNSET -> {
                RoleSelectionScreen(
                    onSelectKid = {
                        preferencesManager.setDeviceRole(DeviceRole.KID)
                        onStartTrackingService()
                    },
                    onSelectParent = {
                        preferencesManager.setDeviceRole(DeviceRole.PARENT)
                    }
                )
            }

            DeviceRole.KID -> {
                if (isDisguiseActive) {
                    when (disguiseConfig.disguiseType) {
                        DisguiseType.CALCULATOR -> {
                            DisguiseCalculatorScreen(
                                preferencesManager = preferencesManager,
                                onUnlockSecretSettings = {
                                    // Disguise turned off in prefs
                                }
                            )
                        }
                        else -> {
                            DisguiseNotesScreen(
                                preferencesManager = preferencesManager,
                                onUnlockSecretSettings = {
                                    // Disguise turned off in prefs
                                }
                            )
                        }
                    }
                } else {
                    KidScreen(
                        preferencesManager = preferencesManager,
                        repository = repository,
                        kidState = kidState,
                        onEnterDisguise = {
                            // Prefs updated, recomposition handles screen change
                        },
                        onSwitchRole = {
                            preferencesManager.setDeviceRole(DeviceRole.UNSET)
                        }
                    )
                }
            }

            DeviceRole.PARENT -> {
                ParentMainScreen(
                    preferencesManager = preferencesManager,
                    repository = repository,
                    onSwitchRole = {
                        preferencesManager.setDeviceRole(DeviceRole.UNSET)
                    }
                )
            }
        }
    }
}
