package com.drdisagree.iconify.utils.apksigner;

/**
 * Pair of two elements.
 *
 * <p>Source: <a href="https://android.googlesource.com/platform/build/+/refs/tags/android-7.1.2_r39/tools/signapk/src/com/android/signapk/Pair.java">...</a></p>
 */
public class Pair<A, B> {
    private final A mFirst;
    private final B mSecond;

    private Pair(A first, B second) {
        mFirst = first;
        mSecond = second;
    }

    public static <A, B> Pair<A, B> create(A first, B second) {
        return new Pair<>(first, second);
    }

    public A getFirst() {
        return mFirst;
    }

    public B getSecond() {
        return mSecond;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mFirst == null) ? 0 : mFirst.hashCode());
        result = prime * result + ((mSecond == null) ? 0 : mSecond.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        Pair other = (Pair) obj;
        if (mFirst == null) {
            if (other.mFirst != null) {
                return false;
            }
        } else if (!mFirst.equals(other.mFirst)) {
            return false;
        }
        if (mSecond == null) {
            return other.mSecond == null;
        } else return mSecond.equals(other.mSecond);
    }
}