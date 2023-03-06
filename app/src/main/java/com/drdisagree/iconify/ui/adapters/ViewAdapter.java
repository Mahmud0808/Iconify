package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.DISABLE_SCROLLING_ANIMATION;
import static com.drdisagree.iconify.common.Preferences.NEW_UPDATE;
import static com.drdisagree.iconify.common.Preferences.REBOOT_NEEDED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.AppUpdates;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.utils.SystemUtil;

import java.util.Objects;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {

    Context context;
    int layout;
    boolean animate;
    String identifier;

    public ViewAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;
        this.animate = false;
    }

    public ViewAdapter(Context context, int layout, boolean animate, String identifier) {
        this.context = context;
        this.layout = layout;
        this.animate = false;
        this.identifier = identifier;
    }

    public ViewAdapter(Context context, int layout, boolean animate) {
        this.context = context;
        this.layout = layout;
        this.animate = animate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        init(holder);

        if (animate && !Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_anim));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (animate && !Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_anim));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if (animate && !Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim));
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private void init(ViewHolder holder) {
        if (Objects.equals(identifier, REBOOT_NEEDED)) {
            // Reboot button of reboot dialog

            ((Button) ((LinearLayout) holder.itemView.findViewById(R.id.reboot_reminder)).findViewById(R.id.btn_reboot)).setOnClickListener(v -> SystemUtil.restartDevice());
        } else if (Objects.equals(identifier, NEW_UPDATE)) {
            // New update found dialg

            ((LinearLayout) holder.itemView.findViewById(R.id.check_update)).setOnClickListener(v -> {
                Intent intent = new Intent(context, AppUpdates.class);
                context.startActivity(intent);
            });
            ((TextView) holder.itemView.findViewById(R.id.update_desc)).setText(context.getResources().getString(R.string.update_dialog_desc).replace("{latestVersionName}", HomePage.latest_version));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
