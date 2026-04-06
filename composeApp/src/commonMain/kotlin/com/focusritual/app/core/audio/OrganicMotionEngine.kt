package com.focusritual.app.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Generates smooth, irregular organic volume drift for enabled sounds.
 *
 * Instead of constant oscillation, the engine picks random drift targets
 * at irregular intervals, lingers near them, then slowly moves on.
 * This creates a natural feel — like rain getting stronger and softer,
 * wind moving in waves, or fire subtly changing intensity.
 */
class OrganicMotionEngine(private val scope: CoroutineScope) {

    companion object {
        // Drift is always downward from base — never exceed user volume
        private const val MAX_DROP = 0.50f   // up to 50% drop from base
        private const val MIN_DROP = 0.10f   // at least 10% drop
        private const val TICK_MS = 35L      // smoother interpolation with faster ticks
        // Target approach — very low = very smooth transitions
        private const val MIN_LERP = 0.004f
        private const val MAX_LERP = 0.018f
        // Dwell (pause) duration range in ticks
        private const val MIN_DWELL_TICKS = 40   // ~1.4s
        private const val MAX_DWELL_TICKS = 120  // ~4.2s
        // Journey (movement) duration range in ticks
        private const val MIN_JOURNEY_TICKS = 80   // ~2.8s
        private const val MAX_JOURNEY_TICKS = 250  // ~8.75s
    }

    private enum class Phase { MOVING, DWELLING }

    private class MotionState(
        var baseVolume: Float,
        var currentOffset: Float = 0f,
        var targetOffset: Float = 0f,
        var lerpSpeed: Float = 0.02f,
        var phase: Phase = Phase.MOVING,
        var phaseTicksRemaining: Int = 0,
    ) {
        fun pickNewTarget() {
            // Always drop below base — never exceed user volume
            // Range: MIN_DROP to MAX_DROP of the base volume
            val dropFraction = MIN_DROP + Random.nextFloat() * (MAX_DROP - MIN_DROP)
            targetOffset = -(dropFraction * baseVolume)
            lerpSpeed = MIN_LERP + Random.nextFloat() * (MAX_LERP - MIN_LERP)
            phase = Phase.MOVING
            phaseTicksRemaining = MIN_JOURNEY_TICKS +
                Random.nextInt(MAX_JOURNEY_TICKS - MIN_JOURNEY_TICKS)
        }

        fun pickReturnTarget() {
            // Return close to base (0 to -5% drop)
            targetOffset = -(Random.nextFloat() * 0.05f * baseVolume)
            lerpSpeed = MIN_LERP + Random.nextFloat() * (MAX_LERP - MIN_LERP)
            phase = Phase.MOVING
            phaseTicksRemaining = MIN_JOURNEY_TICKS +
                Random.nextInt(MAX_JOURNEY_TICKS - MIN_JOURNEY_TICKS)
        }

        fun startDwell() {
            phase = Phase.DWELLING
            phaseTicksRemaining = MIN_DWELL_TICKS +
                Random.nextInt(MAX_DWELL_TICKS - MIN_DWELL_TICKS)
        }

        fun initialDrop() {
            // Start with a visible drop so user sees it working immediately
            val dropFraction = 0.30f + Random.nextFloat() * 0.15f  // 30-45% drop
            targetOffset = -(dropFraction * baseVolume)
            lerpSpeed = 0.025f  // visible but still smooth initial approach
            phase = Phase.MOVING
            phaseTicksRemaining = 100  // ~3.5s
        }
    }

    private val activeSounds = mutableMapOf<String, MotionState>()
    private val _offsets = MutableStateFlow<Map<String, Float>>(emptyMap())
    val offsets: StateFlow<Map<String, Float>> = _offsets.asStateFlow()

    private var job: Job? = null

    fun enable(soundId: String, baseVolume: Float) {
        activeSounds[soundId] = MotionState(baseVolume).also { it.initialDrop() }
        ensureRunning()
    }

    fun disable(soundId: String) {
        activeSounds.remove(soundId)
        _offsets.update { it - soundId }
        if (activeSounds.isEmpty()) {
            job?.cancel()
            job = null
        }
    }

    fun updateBase(soundId: String, volume: Float) {
        activeSounds[soundId]?.baseVolume = volume
    }

    private fun ensureRunning() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive && activeSounds.isNotEmpty()) {
                val result = mutableMapOf<String, Float>()
                for ((id, motion) in activeSounds) {
                    motion.phaseTicksRemaining--

                    when (motion.phase) {
                        Phase.MOVING -> {
                            // Smoothly approach target
                            motion.currentOffset += (motion.targetOffset - motion.currentOffset) * motion.lerpSpeed
                            // When close enough or time's up, start dwelling
                            val reachedTarget = kotlin.math.abs(motion.targetOffset - motion.currentOffset) < 0.005f
                            if (reachedTarget || motion.phaseTicksRemaining <= 0) {
                                motion.startDwell()
                            }
                        }
                        Phase.DWELLING -> {
                            // Micro-drift while dwelling — very gentle
                            motion.currentOffset += (Random.nextFloat() - 0.5f) * 0.002f
                            // Keep offset always negative (never exceed base)
                            motion.currentOffset = motion.currentOffset.coerceAtMost(0f)
                                .coerceAtLeast(-MAX_DROP * motion.baseVolume)
                            if (motion.phaseTicksRemaining <= 0) {
                                // Alternate: drop down or return toward base
                                if (Random.nextFloat() < 0.5f) {
                                    motion.pickNewTarget()
                                } else {
                                    motion.pickReturnTarget()
                                }
                            }
                        }
                    }

                    // Never exceed base volume
                    result[id] = (motion.baseVolume + motion.currentOffset).coerceIn(0.01f, motion.baseVolume)
                }
                _offsets.value = result
                delay(TICK_MS)
            }
        }
    }

    fun release() {
        job?.cancel()
        job = null
        activeSounds.clear()
        _offsets.value = emptyMap()
    }
}
