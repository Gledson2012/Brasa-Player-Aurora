package com.example.ui.viewmodel.delegate

import com.example.audio.AudioPlayerEngine
import com.example.data.model.CustomPresetEntity
import com.example.data.repository.MusicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EqualizerControllerTest {

    private val repository = mockk<MusicRepository>(relaxed = true)
    private val playerEngine = mockk<AudioPlayerEngine>(relaxed = true)

    @Test
    fun `saveCustomPreset selects the persisted database id`() = runTest {
        coEvery { repository.saveCustomPreset(any()) } returns 42L
        val controller = EqualizerController(repository, playerEngine, this)

        controller.saveCustomPreset("Meu preset")
        advanceUntilIdle()

        assertEquals("custom_42", controller.equalizerState.value.currentPresetId)
        coVerify { repository.saveCustomPreset(match { it.name == "Meu preset" }) }
    }

    @Test
    fun `deleteCustomPreset clears the active custom preset`() = runTest {
        val controller = EqualizerController(repository, playerEngine, this)
        controller.updateCustomPresets(listOf(CustomPresetEntity(id = 7, name = "Sétimo")))
        val customPreset = controller.equalizerState.value.customPresets.single()

        controller.selectPreset(customPreset)
        controller.deleteCustomPreset(customPreset.id)
        advanceUntilIdle()

        coVerify { repository.deleteCustomPreset(7L) }
        assertEquals("custom_user", controller.equalizerState.value.currentPresetId)
    }

    @Test
    fun `toggleEqualizer turns on and off`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        val controller = EqualizerController(repository, playerEngine, this)

        // Initially disabled
        assertEquals(false, controller.equalizerState.value.isEnabled)

        // Turn on
        controller.toggleEqualizer(true)
        advanceUntilIdle()

        assertEquals(true, controller.equalizerState.value.isEnabled)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { repository.saveCustomPreset(any()) }

        // Turn off
        controller.toggleEqualizer(false)
        advanceUntilIdle()

        assertEquals(false, controller.equalizerState.value.isEnabled)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { repository.saveCustomPreset(any()) }
    }

    @Test
    fun `setBandLevel updates the band and marks as custom preset`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        coEvery { repository.saveCustomPreset(any()) } returns 99L
        val controller = EqualizerController(repository, playerEngine, this)

        // Set band 0 to 5
        controller.setBandLevel(0, 5)
        advanceUntilIdle()

        assertEquals(5, controller.equalizerState.value.bandLevels[0])
        assertEquals("custom_user", controller.equalizerState.value.currentPresetId)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { repository.saveCustomPreset(any()) }
    }

    @Test
    fun `selectPreset applies preset band levels`() = runTest {
        val preset = EqualizerPreset(
            id = "preset_default",
            name = "Default",
            bandLevels = listOf(-5, 0, 3, -2, 7),
            bassBoost = 50,
            virtualizer = 25,
            isCustom = false
        )
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        val controller = EqualizerController(repository, playerEngine, this)

        controller.selectPreset(preset)
        advanceUntilIdle()

        assertEquals("preset_default", controller.equalizerState.value.currentPresetId)
        assertEquals(listOf(-5, 0, 3, -2, 7), controller.equalizerState.value.bandLevels)
        assertEquals(50, controller.equalizerState.value.bassBoost)
        assertEquals(25, controller.equalizerState.value.virtualizer)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
    }

    @Test
    fun `resetEqualizer preserves custom presets`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        // Set up with a custom preset first
        val presetEntity = CustomPresetEntity(id = 42L, name = "MyPreset")
        controller.updateCustomPresets(listOf(presetEntity))
        val controller = EqualizerController(repository, playerEngine, this)

        // Reset should keep custom presets
        controller.resetEqualizer()
        advanceUntilIdle()

        assertEquals("custom_user", controller.equalizerState.value.currentPresetId)
        assertEquals(listOf(presetEntity.toPreset()), controller.equalizerState.value.customPresets)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
    }

    @Test
    fun `setBassBoost updates value`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        val controller = EqualizerController(repository, playerEngine, this)

        controller.setBassBoost(75)
        advanceUntilIdle()

        assertEquals(75, controller.equalizerState.value.bassBoost)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { onSettingsChanged?.invoke() }
    }

    @Test
    fun `setVirtualizer updates value`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        val controller = EqualizerController(repository, playerEngine, this)

        controller.setVirtualizer(80)
        advanceUntilIdle()

        assertEquals(80, controller.equalizerState.value.virtualizer)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { onSettingsChanged?.invoke() }
    }

    @Test
    fun `setBalance updates value`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        val controller = EqualizerController(repository, playerEngine, this)

        controller.setBalance(0.5f)
        advanceUntilIdle()

        assertEquals(0.5f, controller.equalizerState.value.balance)
        coVerify { playerEngine.syncEqualizer(controller.equalizerState.value) }
        coVerify { onSettingsChanged?.invoke() }
    }

    @Test
    fun `saveCustomPreset with blank name does nothing`() = runTest {
        coEvery { playerEngine.syncEqualizer(any()) } answers {}
        coEvery { repository.saveCustomPreset(any()) } returns 42L
        val controller = EqualizerController(repository, playerEngine, this)

        // Save with blank name
        controller.saveCustomPreset("   ")
        advanceUntilIdle()

        // Should not change currentPresetId
        assertEquals("custom_user", controller.equalizerState.value.currentPresetId)
        coVerify { repository.saveCustomPreset(match { it.name.isBlank() }) }?.times(0)
    }

    @Test
    fun `deleteCustomPreset with non-existent id does nothing`() = runTest {
        val controller = EqualizerController(repository, playerEngine, this)

        // Delete preset with id that doesn't exist
        controller.deleteCustomPreset("non_existent")
        advanceUntilIdle()

        // currentPresetId should remain unchanged
        assertEquals("custom_user", controller.equalizerState.value.currentPresetId)
    }
}
