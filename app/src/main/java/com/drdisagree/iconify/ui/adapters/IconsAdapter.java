package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.Iconify.getAppContext;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.databinding.IconItemBinding;

public class IconsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final CharSequence[] mEntries;
    private final CharSequence[] mEntryValues;
    private Drawable[] mEntryDrawables;
    private int[] mEntryResIds;
    private String mValue;
    private onItemClickListener onItemClickListener;


    public IconsAdapter(CharSequence[] entries,
                        CharSequence[] entryValues,
                        String currentValue,
                        onItemClickListener onItemClickListener) {
        mEntries = entries;
        mEntryValues = entryValues;
        mValue = currentValue;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Sets drawables for the icons
     * @param drawables The drawables of the icons
     */
    public void setDrawables(Drawable[] drawables) {
        mEntryDrawables = drawables;
    }

    /**
     * Set icons from Resources
     * This should be used when the icons are from resources
     * @param resIds The resource ids of the icons
     */
    public void setResIds(int[] resIds) {
        mEntryResIds = resIds;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IconsViewHolder(IconItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((IconsViewHolder)holder).binding.typeTitle.setText(mEntries[position]);

        if (mEntryDrawables != null)
            ((IconsViewHolder) holder).binding.batteryIcon.setImageDrawable(mEntryDrawables[position]);
        else if (mEntryResIds != null)
            ((IconsViewHolder) holder).binding.batteryIcon.setImageDrawable(ContextCompat.getDrawable(((IconsViewHolder)holder).binding.getRoot().getContext(), mEntryResIds[position]));
        else
            throw new IllegalStateException("IconsAdapter - No icons provided");

        if (TextUtils.equals(mEntryValues[position].toString(), mValue)) {
            ((IconsViewHolder)holder).binding.rootLayout.setStrokeColor(getAppContext().getColor(android.R.color.system_accent1_400));
        } else {
            ((IconsViewHolder)holder).binding.rootLayout.setStrokeColor(Color.TRANSPARENT);
        }

        ((IconsViewHolder)holder).binding.rootLayout.setOnClickListener(v -> {
            int previousPosition = Integer.parseInt(mValue);
            mValue = String.valueOf(position);
            notifyItemChanged(previousPosition);
            notifyItemChanged(position);
            onItemClickListener.onItemClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return mEntries.length;
    }

    public void setCurrentValue(String currentValue) {
        mValue = currentValue;
    }

    public static class IconsViewHolder extends RecyclerView.ViewHolder {

        private final IconItemBinding binding;

        IconsViewHolder(@NonNull IconItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    /**
     * Interface for the click on the item
     */
    public interface onItemClickListener {
        void onItemClick(View view, int position);
    }

}
