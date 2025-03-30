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
import android.os.Environment
import android.util.AttributeSet
import android.widget.ImageView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable
import java.io.File

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
        var requiresTint = true
        val drawable = when (mLogoStyle) {
            0 -> modRes.getDrawable(R.drawable.ic_android_logo)
            1 -> modRes.getDrawable(R.drawable.ic_adidas)
            2 -> modRes.getDrawable(R.drawable.ic_alien)
            3 -> modRes.getDrawable(R.drawable.ic_apple_logo)
            4 -> modRes.getDrawable(R.drawable.ic_avengers)
            5 -> modRes.getDrawable(R.drawable.ic_batman)
            6 -> modRes.getDrawable(R.drawable.ic_batman_tdk)
            7 -> modRes.getDrawable(R.drawable.ic_beats)
            8 -> modRes.getDrawable(R.drawable.ic_biohazard)
            9 -> modRes.getDrawable(R.drawable.ic_blackberry)
            10 -> modRes.getDrawable(R.drawable.ic_cannabis)
            11 -> modRes.getDrawable(R.drawable.ic_emoticon_cool)
            12 -> modRes.getDrawable(R.drawable.ic_emoticon_devil)
            13 -> modRes.getDrawable(R.drawable.ic_fire)
            14 -> modRes.getDrawable(R.drawable.ic_heart)
            15 -> modRes.getDrawable(R.drawable.ic_nike)
            16 -> modRes.getDrawable(R.drawable.ic_pac_man)
            17 -> modRes.getDrawable(R.drawable.ic_puma)
            18 -> modRes.getDrawable(R.drawable.ic_rog)
            19 -> modRes.getDrawable(R.drawable.ic_spiderman)
            20 -> modRes.getDrawable(R.drawable.ic_superman)
            21 -> modRes.getDrawable(R.drawable.ic_windows)
            22 -> modRes.getDrawable(R.drawable.ic_xbox)
            23 -> modRes.getDrawable(R.drawable.ic_ghost)
            24 -> modRes.getDrawable(R.drawable.ic_ninja)
            25 -> modRes.getDrawable(R.drawable.ic_robot)
            26 -> modRes.getDrawable(R.drawable.ic_ironman)
            27 -> modRes.getDrawable(R.drawable.ic_captain_america)
            28 -> modRes.getDrawable(R.drawable.ic_flash)
            29 -> modRes.getDrawable(R.drawable.ic_tux_logo)
            30 -> modRes.getDrawable(R.drawable.ic_ubuntu_logo)
            31 -> modRes.getDrawable(R.drawable.ic_mint_logo)
            32 -> modRes.getDrawable(R.drawable.ic_amogus)
            33 -> {
                try {
                    val drawable = ImageDecoder.decodeDrawable(
                        ImageDecoder.createSource(
                            File(
                                Environment.getExternalStorageDirectory(),
                                "/.iconify_files/statusbar_logo.png"
                            )
                        )
                    ).toCircularDrawable(mContext)

                    requiresTint = false
                    drawable
                } catch (_: Throwable) {
                    modRes.getDrawable(R.drawable.ic_android_logo)
                }
            }
            else -> modRes.getDrawable(R.drawable.ic_android_logo)
        }

        if (requiresTint || forceApplyTint) {
            drawable.setTint(mTintColor)
        } else {
            drawable.clearColorFilter()
        }

        setImageDrawable(drawable)
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
            setVisibility(GONE)
            return
        }

        updateLogo()
        setVisibility(VISIBLE)
    }
}