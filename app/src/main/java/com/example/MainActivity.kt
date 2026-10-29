package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.MainScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.SipTheme
import com.example.util.TiltSensorHelper
import com.example.util.configureAppCheck
import com.example.viewmodel.SipViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SipViewModel by viewModels()
    private lateinit var tiltSensorHelper: TiltSensorHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required App Check initialization with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        tiltSensorHelper = TiltSensorHelper(this)

        setContent {
            val profile by viewModel.profile.collectAsState()
            val tiltX by tiltSensorHelper.tiltX.collectAsState()

            SipTheme(themeMode = profile.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = profile.hasCompletedOnboarding,
                        animationSpec = tween(350),
                        label = "OnboardingCrossfade"
                    ) { completed ->
                        if (!completed) {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onFinish = {
                                    // Profile hasCompletedOnboarding is set inside viewModel.completeOnboarding
                                }
                            )
                        } else {
                            MainScreen(
                                viewModel = viewModel,
                                tiltFactor = tiltX
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tiltSensorHelper.startListening()
    }

    override fun onPause() {
        super.onPause()
        tiltSensorHelper.stopListening()
    }
}
