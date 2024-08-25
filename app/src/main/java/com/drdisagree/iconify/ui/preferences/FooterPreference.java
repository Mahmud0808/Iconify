package com.drdisagree.iconify.ui.preferences;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.drdisagree.iconify.R;

/**
 * A custom preference acting as "footer" of a page. It has a field for icon and text. It is added
 * to screen as the last preference.
 */
public class FooterPreference extends Preference {

    public static final String KEY_FOOTER = "footer_preference";
    static final int ORDER_FOOTER = Integer.MAX_VALUE - 1;
    @VisibleForTesting
    View.OnClickListener mLearnMoreListener;
    @VisibleForTesting
    int mIconVisibility = View.VISIBLE;
    private CharSequence mContentDescription;
    private CharSequence mLearnMoreText;
    private FooterLearnMoreSpan mLearnMoreSpan;

    public FooterPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.footerPreferenceStyle);
        init();
    }

    public FooterPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = holder.itemView.findViewById(android.R.id.title);
        if (title != null && !TextUtils.isEmpty(mContentDescription)) {
            title.setContentDescription(mContentDescription);
        }

        TextView learnMore = holder.itemView.findViewById(R.id.settingslib_learn_more);
        if (learnMore != null) {
            if (mLearnMoreListener != null) {
                learnMore.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(mLearnMoreText)) {
                    mLearnMoreText = learnMore.getText();
                } else {
                    learnMore.setText(mLearnMoreText);
                }
                SpannableString learnMoreText = new SpannableString(mLearnMoreText);
                if (mLearnMoreSpan != null) {
                    learnMoreText.removeSpan(mLearnMoreSpan);
                }
                mLearnMoreSpan = new FooterLearnMoreSpan(mLearnMoreListener);
                learnMoreText.setSpan(mLearnMoreSpan, 0,
                        learnMoreText.length(), 0);
                learnMore.setText(learnMoreText);
            } else {
                learnMore.setVisibility(View.GONE);
            }
        }

        View icon = holder.itemView.findViewById(R.id.icon_frame);
        if (icon != null) {
            icon.setVisibility(mIconVisibility);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        setTitle(summary);
    }

    @Override
    public void setSummary(int summaryResId) {
        setTitle(summaryResId);
    }

    @Override
    public CharSequence getSummary() {
        return getTitle();
    }

    /**
     * To set content description of the {@link FooterPreference}. This can use for talkback
     * environment if developer wants to have a customization content.
     *
     * @param contentDescription The resource id of the content description.
     */
    public void setContentDescription(CharSequence contentDescription) {
        if (!TextUtils.equals(mContentDescription, contentDescription)) {
            mContentDescription = contentDescription;
            notifyChanged();
        }
    }

    /**
     * Return the content description of footer preference.
     */
    @VisibleForTesting
    CharSequence getContentDescription() {
        return mContentDescription;
    }

    /**
     * Sets the learn more text.
     *
     * @param learnMoreText The string of the learn more text.
     */
    public void setLearnMoreText(CharSequence learnMoreText) {
        if (!TextUtils.equals(mLearnMoreText, learnMoreText)) {
            mLearnMoreText = learnMoreText;
            notifyChanged();
        }
    }

    /**
     * Assign an action for the learn more link.
     */
    public void setLearnMoreAction(View.OnClickListener listener) {
        if (mLearnMoreListener != listener) {
            mLearnMoreListener = listener;
            notifyChanged();
        }
    }

    /**
     * Set visibility of footer icon.
     */
    public void setIconVisibility(int iconVisibility) {
        if (mIconVisibility == iconVisibility) {
            return;
        }
        mIconVisibility = iconVisibility;
        notifyChanged();
    }

    private void init() {
        setLayoutResource(R.layout.preference_footer);
        if (getIcon() == null) {
            setIcon(R.drawable.ic_info);
        }
        setOrder(ORDER_FOOTER);
        if (TextUtils.isEmpty(getKey())) {
            setKey(KEY_FOOTER);
        }
        setSelectable(false);
    }

    /**
     * The builder is convenient to creat a dynamic FooterPreference.
     */
    public static class Builder {
        private Context mContext;
        private String mKey;
        private CharSequence mTitle;
        private CharSequence mContentDescription;
        private CharSequence mLearnMoreText;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        /**
         * To set the key value of the {@link FooterPreference}.
         *
         * @param key The key value.
         */
        public Builder setKey(@NonNull String key) {
            mKey = key;
            return this;
        }

        /**
         * To set the title of the {@link FooterPreference}.
         *
         * @param title The title.
         */
        public Builder setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        /**
         * To set the title of the {@link FooterPreference}.
         *
         * @param titleResId The resource id of the title.
         */
        public Builder setTitle(@StringRes int titleResId) {
            mTitle = mContext.getText(titleResId);
            return this;
        }

        /**
         * To set content description of the {@link FooterPreference}. This can use for talkback
         * environment if developer wants to have a customization content.
         *
         * @param contentDescription The resource id of the content description.
         */
        public Builder setContentDescription(CharSequence contentDescription) {
            mContentDescription = contentDescription;
            return this;
        }

        /**
         * To set content description of the {@link FooterPreference}. This can use for talkback
         * environment if developer wants to have a customization content.
         *
         * @param contentDescriptionResId The resource id of the content description.
         */
        public Builder setContentDescription(@StringRes int contentDescriptionResId) {
            mContentDescription = mContext.getText(contentDescriptionResId);
            return this;
        }

        /**
         * To set learn more string of the learn more text. This can use for talkback
         * environment if developer wants to have a customization content.
         *
         * @param learnMoreText The resource id of the learn more string.
         */
        public Builder setLearnMoreText(CharSequence learnMoreText) {
            mLearnMoreText = learnMoreText;
            return this;
        }

        /**
         * To set learn more string of the {@link FooterPreference}. This can use for talkback
         * environment if developer wants to have a customization content.
         *
         * @param learnMoreTextResId The resource id of the learn more string.
         */
        public Builder setLearnMoreText(@StringRes int learnMoreTextResId) {
            mLearnMoreText = mContext.getText(learnMoreTextResId);
            return this;
        }


        /**
         * To generate the {@link FooterPreference}.
         */
        public FooterPreference build() {
            final FooterPreference footerPreference = new FooterPreference(mContext);
            footerPreference.setSelectable(false);
            if (TextUtils.isEmpty(mTitle)) {
                throw new IllegalArgumentException("Footer title cannot be empty!");
            }
            footerPreference.setTitle(mTitle);
            if (!TextUtils.isEmpty(mKey)) {
                footerPreference.setKey(mKey);
            }

            if (!TextUtils.isEmpty(mContentDescription)) {
                footerPreference.setContentDescription(mContentDescription);
            }

            if (!TextUtils.isEmpty(mLearnMoreText)) {
                footerPreference.setLearnMoreText(mLearnMoreText);
            }
            return footerPreference;
        }
    }

    /**
     * A {@link URLSpan} that opens a support page when clicked
     */
    static class FooterLearnMoreSpan extends URLSpan {

        private final View.OnClickListener mClickListener;

        FooterLearnMoreSpan(View.OnClickListener clickListener) {
            // sets the url to empty string so we can prevent any other span processing from
            // clearing things we need in this string.
            super("");
            mClickListener = clickListener;
        }

        @Override
        public void onClick(View widget) {
            if (mClickListener != null) {
                mClickListener.onClick(widget);
            }
        }
    }
}
