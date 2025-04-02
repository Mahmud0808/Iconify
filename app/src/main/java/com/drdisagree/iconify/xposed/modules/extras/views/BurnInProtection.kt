package com.drdisagree.iconify.xposed.modules.extras.views

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AodBurnInProtection(private val view: View) {

    private var isMovementEnabled: Boolean = false
    private var originalX: Float = 0f
    private var originalY: Float = 0f
    private var movementJob: Job? = null

    init {
        originalX = view.x
        originalY = view.y
    }

    fun setMovementEnabled(enabled: Boolean) {
        if (enabled != isMovementEnabled) {
            isMovementEnabled = enabled

            if (isMovementEnabled) {
                originalX = view.x
                originalY = view.y
                startMovement()
            } else {
                stopMovement()
            }
        }
    }

    private fun startMovement() {
        if (movementJob?.isActive == true) return

        movementJob = CoroutineScope(Dispatchers.Main).launch {
            while (isMovementEnabled) {
                moveViewSlightly()
                delay(45 * 60 * 1000L) // Delay for 45 minutes
            }
        }
    }

    private fun stopMovement() {
        movementJob?.cancel()
        resetViewPosition()
    }

    private fun moveViewSlightly() {
        val offsetX = (6..10).random().toFloat() * if (Math.random() > 0.5) 1 else -1
        val offsetY = (6..10).random().toFloat() * if (Math.random() > 0.5) 1 else -1

        view.animate()
            .x(view.x + offsetX)
            .y(view.y + offsetY)
            .setDuration(1000)
            .start()
    }

    private fun resetViewPosition() {
        view.animate()
            .x(originalX)
            .y(originalY)
            .setDuration(300)
            .start()
    }

    companion object {
        private val activeMovements = mutableMapOf<View, AodBurnInProtection>()

        fun registerForView(view: View): AodBurnInProtection {
            return activeMovements.getOrPut(view) {
                AodBurnInProtection(view).also {
                    it.startMovement()
                }
            }
        }

        fun unregisterForView(view: View) {
            activeMovements[view]?.apply {
                stopMovement()
            }
            activeMovements.remove(view)
        }
    }
}