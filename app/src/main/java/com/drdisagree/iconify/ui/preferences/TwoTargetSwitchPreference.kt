package com.drdisagree.iconify.ui.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.annotation.Keep
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.google.android.material.materialswitch.MaterialSwitch


/**
 * A custom preference that provides a switch toggle and a clickable preference.
 */
class TwoTargetSwitchPreference : TwoTargetPreference {

    private var mSwitch: MaterialSwitch? = null
    private var mChecked = false
    private var mCheckedSet = false
    private var mEnableSwitch = true

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    override val secondTargetResId: Int
        get() = R.layout.preference_material_switch

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        (holder.findViewById(R.id.switchWidget) as? MaterialSwitch)?.apply {
            mSwitch = this

            setOnClickListener {
                if (!this.isEnabled) {
                    isChecked = mChecked
                    return@setOnClickListener
                }

                val newChecked = !mChecked
                if (callChangeListener(newChecked)) {
                    isChecked = newChecked
                    mChecked = newChecked

                    putBoolean(key, newChecked)
                }
            }

            // Consumes move events to ignore drag actions.
            setOnTouchListener { _: View?, event: MotionEvent -> event.actionMasked == MotionEvent.ACTION_MOVE }

            contentDescription = title
            isChecked = mChecked
            isEnabled = isPreferenceEnabled
        }

        holder.itemView.findViewById<View?>(R.id.two_target_container)?.setOnClickListener {
            holder.itemView.performClick()
        }

        holder.itemView.findViewById<View?>(R.id.widget_frame)?.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setMinimumHeight((parent as View).height)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

            setOnClickListener {
                if (isEnabled) {
                    mSwitch?.performClick()
                }
            }
        }
    }

    val isPreferenceEnabled: Boolean
        get() = isEnabled && mEnableSwitch

    var isChecked: Boolean
        get() = mChecked
        /**
         * Set the checked status to be `checked`.
         *
         * @param checked The new checked status
         */
        set(checked) {
            // Always persist/notify the first time; don't assume the field's default of false.
            val changed = mChecked != checked
            if (changed || !mCheckedSet) {
                mChecked = checked
                mCheckedSet = true
                persistBoolean(checked)
                if (changed) {
                    notifyDependencyChange(shouldDisableDependents())
                    notifyChanged()
                }
            }
        }

    @get:Keep
    val checkedState: Boolean?
        /**
         * Used to validate the state of mChecked and mCheckedSet when testing, without requiring
         * that a ViewHolder be bound to the object.
         */
        get() = if (mCheckedSet) mChecked else null

    /**
     * Set the Switch to be the status of `enabled`.
     *
     * @param enabled The new enabled status
     */
    fun setSwitchEnabled(enabled: Boolean) {
        mEnableSwitch = enabled
        mSwitch?.isEnabled = enabled
    }

    override fun shouldHideSecondTarget(): Boolean {
        return secondTargetResId == 0
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        isChecked = getPersistedBoolean(defaultValue as? Boolean ?: false)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getBoolean(index, false)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState = SavedState(superState)
        myState.mChecked = isChecked

        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)

        isChecked = myState.mChecked
    }

    internal class SavedState : BaseSavedState {
        var mChecked: Boolean = false

        constructor(source: Parcel) : super(source) {
            mChecked = source.readInt() == 1
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(if (mChecked) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
