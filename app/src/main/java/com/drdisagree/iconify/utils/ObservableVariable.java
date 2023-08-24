package com.drdisagree.iconify.utils;

public class ObservableVariable<T> {

    private T value;
    private OnChangeListener<T> listener;

    public void setOnChangeListener(OnChangeListener<T> listener) {
        this.listener = listener;
    }

    public void notifyChanged() {
        if (listener != null) {
            listener.onChange(value);
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        if (value != newValue) {
            value = newValue;
            if (listener != null) {
                listener.onChange(value);
            }
        }
    }

    public interface OnChangeListener<T> {
        void onChange(T newValue);
    }
}
