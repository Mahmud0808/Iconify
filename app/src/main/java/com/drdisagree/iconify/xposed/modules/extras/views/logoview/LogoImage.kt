/*
 * Copyright (C) 2018-2025 Iconify
 * Copyright (C) 2018-2024 crDroid Android Project
 * Copyright (C) 2018-2019 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.drdisagree.iconify.xposed.modules.extras.views.logoview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toCircularDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("AppCompatCustomView")
abstract class LogoImage @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(
    mContext,
    attrs,
    defStyle
) {
    var mAttached = false

    private var mShowLogo = false
    var mLogoPosition: Int = 0
    private var mLogoStyle = 0
    private var forceApplyTint = false
    var mTintColor = Color.WHITE

    protected abstract val isLogoVisible: Boolean

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mAttached) return

        mAttached = true

        updateSettings()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!mAttached) return

        mAttached = false
    }

    @Suppress("deprecation")
    @SuppressLint("UseCompatLoadingForDrawables")
    fun updateLogo(force: Boolean = true) {
        if (!force) {
            drawable?.let { applyTint(it) }
            return
        }

        val newDrawable = when (mLogoStyle) {
            0 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_android)
            1 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_adidas)
            2 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_alien)
            3 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_apple)
            4 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_avengers)
            5 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_batman)
            6 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_batman_tdk)
            7 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_beats)
            8 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_biohazard)
            9 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_blackberry)
            10 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_cannabis)
            11 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_emoticon_cool)
            12 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_emoticon_devil)
            13 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_fire)
            14 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_heart)
            15 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_nike)
            16 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_pac_man)
            17 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_puma)
            18 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_rog)
            19 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_spiderman)
            20 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_superman)
            21 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_windows)
            22 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_xbox)
            23 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_ghost)
            24 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_ninja)
            25 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_robot)
            26 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_ironman)
            27 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_captain_america)
            28 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_flash)
            29 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_tux)
            30 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_ubuntu)
            31 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_mint)
            32 -> modRes.getDrawable(R.drawable.ic_statusbar_logo_amogus)
            33 -> {
                loadCustomLogoAsync()
                return
            }
            else -> modRes.getDrawable(R.drawable.ic_statusbar_logo_android)
        }

        applyTint(newDrawable)
        setImageDrawable(newDrawable)
    }

    @Suppress("deprecation")
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadCustomLogoAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            val drawable = try {
                ImageDecoder.decodeDrawable(
                    ImageDecoder.createSource(STATUSBAR_LOGO_FILE)
                ).toCircularDrawable(mContext)
            } catch (_: Throwable) {
                modRes.getDrawable(R.drawable.ic_statusbar_logo_android)
            }

            withContext(Dispatchers.Main) {
                applyTint(drawable)
                setImageDrawable(drawable)
            }
        }
    }

    private fun applyTint(drawable: Drawable) {
        if (forceApplyTint) {
            drawable.setTint(mTintColor)
        } else {
            drawable.clearColorFilter()
        }
    }

    fun updateSettings(
        showLogo: Boolean = mShowLogo,
        logoPosition: Int = mLogoPosition,
        logoStyle: Int = mLogoStyle,
        applyTint: Boolean = forceApplyTint
    ) {
        mShowLogo = showLogo
        mLogoPosition = logoPosition
        mLogoStyle = logoStyle
        forceApplyTint = applyTint

        if (!mShowLogo || !isLogoVisible) {
            setImageDrawable(null)
            visibility = GONE
            return
        }

        updateLogo()
        visibility = VISIBLE
    }
}