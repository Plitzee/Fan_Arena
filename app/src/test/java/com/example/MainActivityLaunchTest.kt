package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FanArenaViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityLaunchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppOrchestratorLaunchesSuccessfully() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = FanArenaViewModel(application)
        
        composeTestRule.setContent {
            MyApplicationTheme {
                MainAppOrchestrator(viewModel = viewModel)
            }
        }
        
        composeTestRule.waitForIdle()
    }
}
