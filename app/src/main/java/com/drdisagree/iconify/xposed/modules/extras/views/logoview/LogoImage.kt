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
import android.util.AttributeSet
import android.widget.ImageView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.xposed.HookEntry.Companion.moduleResources
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable
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
    fun updateLogo() {
        if (mLogoStyle == 33) {
            loadCustomLogoAsync()
            return
        }

        val drawable = when (mLogoStyle) {
            0 -> moduleResources.getDrawable(R.drawable.ic_android_logo)
            1 -> moduleResources.getDrawable(R.drawable.ic_adidas)
            2 -> moduleResources.getDrawable(R.drawable.ic_alien)
            3 -> moduleResources.getDrawable(R.drawable.ic_apple_logo)
            4 -> moduleResources.getDrawable(R.drawable.ic_avengers)
            5 -> moduleResources.getDrawable(R.drawable.ic_batman)
            6 -> moduleResources.getDrawable(R.drawable.ic_batman_tdk)
            7 -> moduleResources.getDrawable(R.drawable.ic_beats)
            8 -> moduleResources.getDrawable(R.drawable.ic_biohazard)
            9 -> moduleResources.getDrawable(R.drawable.ic_blackberry)
            10 -> moduleResources.getDrawable(R.drawable.ic_cannabis)
            11 -> moduleResources.getDrawable(R.drawable.ic_emoticon_cool)
            12 -> moduleResources.getDrawable(R.drawable.ic_emoticon_devil)
            13 -> moduleResources.getDrawable(R.drawable.ic_fire)
            14 -> moduleResources.getDrawable(R.drawable.ic_heart)
            15 -> moduleResources.getDrawable(R.drawable.ic_nike)
            16 -> moduleResources.getDrawable(R.drawable.ic_pac_man)
            17 -> moduleResources.getDrawable(R.drawable.ic_puma)
            18 -> moduleResources.getDrawable(R.drawable.ic_rog)
            19 -> moduleResources.getDrawable(R.drawable.ic_spiderman)
            20 -> moduleResources.getDrawable(R.drawable.ic_superman)
            21 -> moduleResources.getDrawable(R.drawable.ic_windows)
            22 -> moduleResources.getDrawable(R.drawable.ic_xbox)
            23 -> moduleResources.getDrawable(R.drawable.ic_ghost)
            24 -> moduleResources.getDrawable(R.drawable.ic_ninja)
            25 -> moduleResources.getDrawable(R.drawable.ic_robot)
            26 -> moduleResources.getDrawable(R.drawable.ic_ironman)
            27 -> moduleResources.getDrawable(R.drawable.ic_captain_america)
            28 -> moduleResources.getDrawable(R.drawable.ic_flash)
            29 -> moduleResources.getDrawable(R.drawable.ic_tux_logo)
            30 -> moduleResources.getDrawable(R.drawable.ic_ubuntu_logo)
            31 -> moduleResources.getDrawable(R.drawable.ic_mint_logo)
            32 -> moduleResources.getDrawable(R.drawable.ic_amogus)
            else -> moduleResources.getDrawable(R.drawable.ic_android_logo)
        }

        if (forceApplyTint) {
            drawable.setTint(mTintColor)
        } else {
            drawable.clearColorFilter()
        }

        setImageDrawable(drawable)
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
                moduleResources.getDrawable(R.drawable.ic_android_logo)
            }

            withContext(Dispatchers.Main) {
                drawable.clearColorFilter()
                setImageDrawable(drawable)
            }
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