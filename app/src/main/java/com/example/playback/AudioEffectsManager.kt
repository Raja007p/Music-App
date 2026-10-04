package com.example.playback

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.model.EqualizerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioEffectsManager(private val context: Context) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _equalizerState = MutableStateFlow(
        EqualizerState(
            isEnabled = true,
            currentPreset = "Rock",
            bandLevels = listOf(4, 2, -1, 3, 5, 2, 0, 3, 4, 6),
            bassBoost = 60,
            virtualizer = 30
        )
    )
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    // 10 preset profiles
    val presets = mapOf(
        "Custom" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Rock" to listOf(4, 2, -1, 3, 5, 2, 0, 3, 4, 6),
        "Pop" to listOf(-1, 1, 3, 4, 3, 0, -1, 2, 4, 5),
        "Classical" to listOf(5, 4, 3, 2, -1, -1, 0, 2, 3, 4),
        "Jazz" to listOf(3, 2, 1, 2, -1, -1, 0, 1, 2, 3),
        "Dance" to listOf(6, 5, 2, 0, 1, 2, 4, 5, 5, 4),
        "Vocal" to listOf(-2, -1, 1, 3, 5, 4, 2, 0, -1, -2),
        "Flat" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Electronic" to listOf(5, 4, 1, 0, -2, 2, 1, 3, 5, 6),
        "Acoustic" to listOf(3, 3, 2, 1, 2, 2, 3, 3, 2, 1)
    )

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        release()
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
                setStrength((_equalizerState.value.bassBoost * 10).toShort())
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _equalizerState.value.isEnabled
                setStrength((_equalizerState.value.virtualizer * 10).toShort())
            }
            applyCurrentState()
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error initializing audio effects for session $audioSessionId", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        _equalizerState.value = _equalizerState.value.copy(isEnabled = enabled)
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error toggling effects", e)
        }
    }

    fun setPreset(name: String) {
        val bands = presets[name] ?: _equalizerState.value.bandLevels
        _equalizerState.value = _equalizerState.value.copy(
            currentPreset = name,
            bandLevels = bands
        )
        applyCurrentState()
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        val current = _equalizerState.value.bandLevels.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = levelDb.coerceIn(-12, 12)
            _equalizerState.value = _equalizerState.value.copy(
                currentPreset = "Custom",
                bandLevels = current
            )
            applyCurrentState()
        }
    }

    fun setBassBoost(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _equalizerState.value = _equalizerState.value.copy(bassBoost = clamped)
        try {
            bassBoost?.setStrength((clamped * 10).toShort())
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error setting bass boost", e)
        }
    }

    fun setVirtualizer(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        _equalizerState.value = _equalizerState.value.copy(virtualizer = clamped)
        try {
            virtualizer?.setStrength((clamped * 10).toShort())
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error setting virtualizer", e)
        }
    }

    private fun applyCurrentState() {
        val eq = equalizer ?: return
        val state = _equalizerState.value
        try {
            val numBands = eq.numberOfBands.toInt()
            val minRange = eq.bandLevelRange[0] // in mB (e.g. -1500)
            val maxRange = eq.bandLevelRange[1] // in mB (e.g. +1500)

            for (i in 0 until numBands) {
                val db = state.bandLevels.getOrElse(i) { 0 }
                // Map -12..12 dB to minRange..maxRange
                val mbLevel = (db * 100).coerceIn(minRange.toInt(), maxRange.toInt()).toShort()
                eq.setBandLevel(i.toShort(), mbLevel)
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error applying band levels", e)
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error releasing effects", e)
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
