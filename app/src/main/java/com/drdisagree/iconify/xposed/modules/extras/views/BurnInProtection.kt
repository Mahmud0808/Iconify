package com.drdisagree.iconify.xposed.modules.extras.views

import android.view.View

class AodBurnInProtection(private val view: View) {

    private var isMovementEnabled: Boolean = false
    private var baseTranslationX: Float = 0f
    private var baseTranslationY: Float = 0f
    private var currentOffsetX: Float = 0f
    private var currentOffsetY: Float = 0f

    fun setMovementEnabled(enabled: Boolean) {
        if (enabled == isMovementEnabled) return

        isMovementEnabled = enabled

        if (enabled) {
            baseTranslationX = view.translationX
            baseTranslationY = view.translationY
            currentOffsetX = 0f
            currentOffsetY = 0f
            moveViewSlightly()
        } else {
            resetViewPosition()
        }
    }

    fun onDozeTimeTick() {
        if (!isMovementEnabled) return

        moveViewSlightly()
    }

    private fun moveViewSlightly() {
        val maxOffset = MAX_OFFSET_DP * view.resources.displayMetrics.density

        currentOffsetX = (currentOffsetX + randomStep()).coerceIn(-maxOffset, maxOffset)
        currentOffsetY = (currentOffsetY + randomStep()).coerceIn(-maxOffset, maxOffset)

        view.translationX = baseTranslationX + currentOffsetX
        view.translationY = baseTranslationY + currentOffsetY
    }

    private fun randomStep(): Float {
        return (STEP_MIN_PX..STEP_MAX_PX).random().toFloat() * if (Math.random() > 0.5) 1 else -1
    }

    private fun resetViewPosition() {
        currentOffsetX = 0f
        currentOffsetY = 0f

        view.animate()
            .translationX(baseTranslationX)
            .translationY(baseTranslationY)
            .setDuration(300)
            .start()
    }

    companion object {
        private const val STEP_MIN_PX = 2
        private const val STEP_MAX_PX = 4
        private const val MAX_OFFSET_DP = 8f

        private val activeMovements = mutableMapOf<View, AodBurnInProtection>()

        fun registerForView(view: View): AodBurnInProtection {
            return activeMovements.getOrPut(view) {
                AodBurnInProtection(view)
            }
        }

        fun unregisterForView(view: View) {
            activeMovements.remove(view)?.apply {
                setMovementEnabled(false)
            }
        }

        fun dispatchDozeTimeTick() {
            activeMovements.values.forEach { it.onDozeTimeTick() }
        }
    }
}