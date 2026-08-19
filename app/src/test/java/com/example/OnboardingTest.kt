package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datastore.ThemePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingTest {

    private lateinit var dataStore: ThemePreferencesDataStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStore = ThemePreferencesDataStore(context)
    }

    @Test
    fun `onboarding defaults to not completed`() = runTest {
        val completed = dataStore.onboardingCompletedFlow.first()
        assertEquals(false, completed)
    }

    @Test
    fun `setOnboardingCompleted persists true`() = runTest {
        dataStore.setOnboardingCompleted(true)
        val completed = dataStore.onboardingCompletedFlow.first()
        assertEquals(true, completed)
    }

    @Test
    fun `setOnboardingCompleted persists false`() = runTest {
        // First set to true
        dataStore.setOnboardingCompleted(true)
        assertEquals(true, dataStore.onboardingCompletedFlow.first())

        // Then set back to false
        dataStore.setOnboardingCompleted(false)
        val completed = dataStore.onboardingCompletedFlow.first()
        assertEquals(false, completed)
    }

    @Test
    fun `onboarding state toggles correctly`() = runTest {
        // Default is false
        assertEquals(false, dataStore.onboardingCompletedFlow.first())

        // Set to true
        dataStore.setOnboardingCompleted(true)
        assertEquals(true, dataStore.onboardingCompletedFlow.first())

        // Set back to false
        dataStore.setOnboardingCompleted(false)
        assertEquals(false, dataStore.onboardingCompletedFlow.first())
    }
}
