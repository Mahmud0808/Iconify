package com.drdisagree.iconify.utils.extension

class ObservableVariable<T> {

    var value: T? = null
        private set
    private var listener: OnChangeListener<T?>? = null

    fun setOnChangeListener(listener: OnChangeListener<T?>?) {
        this.listener = listener
    }

    fun notifyChanged() {
        listener?.onChange(value)
    }

    fun setValue(newValue: T) {
        if (value !== newValue) {
            value = newValue

            listener?.onChange(value)
        }
    }

    fun interface OnChangeListener<T> {
        fun onChange(newValue: T)
    }
}
