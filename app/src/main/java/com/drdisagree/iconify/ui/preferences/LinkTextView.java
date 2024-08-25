package com.drdisagree.iconify.ui.preferences;

import android.content.Context;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;

/**
 * Copied from setup wizard. This TextView performed two functions. The first is to make it so the
 * link behaves properly and becomes clickable. The second was that it made the link visible to
 * accessibility services, but from O forward support for links is provided natively.
 */
public class LinkTextView extends androidx.appcompat.widget.AppCompatTextView {

    public LinkTextView(Context context) {
        this(context, null);
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (text instanceof Spanned) {
            final ClickableSpan[] spans =
                    ((Spanned) text).getSpans(0, text.length(), ClickableSpan.class);
            if (spans.length > 0) {
                setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
