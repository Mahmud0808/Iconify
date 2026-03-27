package com.drdisagree.iconify.core.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withRotation
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.Const
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable

object ViewHelper {

    fun disableNestedScrolling(viewPager: ViewPager2) {
        var recyclerView: RecyclerView? = null

        for (i in 0 until viewPager.childCount) {
            if (viewPager.getChildAt(i) is RecyclerView) {
                recyclerView = viewPager.getChildAt(i) as RecyclerView
                break
            }
        }

        if (recyclerView != null) {
            recyclerView.isNestedScrollingEnabled = false
        }
    }

    fun setHeader(context: Context, toolbar: Toolbar, title: Any) {
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)
        if (title is Int) {
            toolbar.setTitle(title)
        } else if (title is String) {
            toolbar.setTitle(title)
        }
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: Int
    ) {
        setHeader(
            context,
            fragmentManager,
            toolbar,
            context.resources.getString(title)
        )
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: String
    ) {
        toolbar.setTitle(title)
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed(
                { fragmentManager.popBackStack() }, Const.FRAGMENT_BACK_BUTTON_DELAY.toLong()
            )
        }
    }

    fun dp2px(dp: Float): Int {
        return dp2px(dp.toInt())
    }

    fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            appContext.resources.displayMetrics
        ).toInt()
    }

    fun getStatusbarLogoDrawables(context: Context): Array<Drawable> {
        val logoColor = appContext.getColor(R.color.textColorPrimary)

        val predefinedLogos = arrayOf(
            R.drawable.ic_android_logo,
            R.drawable.ic_adidas,
            R.drawable.ic_alien,
            R.drawable.ic_apple_logo,
            R.drawable.ic_avengers,
            R.drawable.ic_batman,
            R.drawable.ic_batman_tdk,
            R.drawable.ic_beats,
            R.drawable.ic_biohazard,
            R.drawable.ic_blackberry,
            R.drawable.ic_cannabis,
            R.drawable.ic_emoticon_cool,
            R.drawable.ic_emoticon_devil,
            R.drawable.ic_fire,
            R.drawable.ic_heart,
            R.drawable.ic_nike,
            R.drawable.ic_pac_man,
            R.drawable.ic_puma,
            R.drawable.ic_rog,
            R.drawable.ic_spiderman,
            R.drawable.ic_superman,
            R.drawable.ic_windows,
            R.drawable.ic_xbox,
            R.drawable.ic_ghost,
            R.drawable.ic_ninja,
            R.drawable.ic_robot,
            R.drawable.ic_ironman,
            R.drawable.ic_captain_america,
            R.drawable.ic_flash,
            R.drawable.ic_tux_logo,
            R.drawable.ic_ubuntu_logo,
            R.drawable.ic_mint_logo,
            R.drawable.ic_amogus
        )

        val logoDrawables = predefinedLogos.map { getDrawable(context, it)!! }.toMutableList()
        val customDrawable = try {
            getCustomLogoFromMediaStore(context)
        } catch (_: Throwable) {
            @Suppress("DEPRECATION")
            getDrawable(context, R.drawable.ic_upload_file)?.apply { setTint(logoColor) }
        }

        logoDrawables.forEach { it.setTint(logoColor) }
        customDrawable?.let { logoDrawables.add(it) }

        return logoDrawables.toTypedArray()
    }

    private fun getCustomLogoFromMediaStore(context: Context): Drawable? {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection =
            "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
        val selectionArgs =
            arrayOf(STATUSBAR_LOGO_FILE.name, "Download/$XPOSED_RESOURCE_FOLDER_NAME%")

        return resolver.query(collection, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    val uri = ContentUris.withAppendedId(collection, id)

                    val source = ImageDecoder.createSource(resolver, uri)
                    ImageDecoder.decodeDrawable(source).toCircularDrawable(context)
                } else null
            }
    }

    private fun getRotateDrawable(d: Drawable, angle: Float): Drawable {
        val arD = arrayOf(d)
        return object : LayerDrawable(arD) {
            override fun draw(canvas: Canvas) {
                canvas.withRotation(
                    angle,
                    d.bounds.width().toFloat() / 2,
                    d.bounds.height().toFloat() / 2
                ) {
                    super.draw(this)
                }
            }

            override fun getConstantState(): ConstantState {
                return RotateDrawableConstantState(d, angle)
            }
        }
    }

    private class RotateDrawableConstantState(
        private val drawable: Drawable,
        private val angle: Float
    ) : Drawable.ConstantState() {
        override fun newDrawable(): Drawable {
            return getRotateDrawable(drawable.constantState?.newDrawable() ?: drawable, angle)
        }

        override fun getChangingConfigurations(): Int {
            return drawable.changingConfigurations
        }
    }

    private fun getDrawable(context: Context, @DrawableRes batteryRes: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, batteryRes, context.theme)
    }

    fun setTextRecursively(viewGroup: ViewGroup, text: String?) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                setTextRecursively(child, text)
            } else if (child is TextView) {
                child.text = text
            }
        }
    }

    fun applyTextSizeRecursively(viewGroup: ViewGroup, textSize: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                applyTextSizeRecursively(child, textSize)
            } else if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            }
        }
    }

}