package com.drdisagree.iconify.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.overlaymanager.BrightnessBarManager;
import com.drdisagree.iconify.overlaymanager.BrightnessBarPixelManager;
import com.drdisagree.iconify.ui.models.BrightnessBarModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;
import java.util.Objects;

public class BrightnessBarAdapter extends RecyclerView.Adapter<BrightnessBarAdapter.ViewHolder> {

    Context context;
    ArrayList<BrightnessBarModel> itemList;
    ArrayList<String> BRIGHTNESSBAR_KEY = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;
    String variant;

    public BrightnessBarAdapter(Context context, ArrayList<BrightnessBarModel> itemList, LoadingDialog loadingDialog, String variant) {
        this.context = context;
        this.variant = variant;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;

        // Preference key
        for (int i = 1; i <= itemList.size(); i++)
            BRIGHTNESSBAR_KEY.add("IconifyComponent" + variant + i + ".overlay");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Objects.equals(variant, "BBN") ? R.layout.view_list_option_brightnessbar : R.layout.view_list_option_brightnessbar_pixel, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.style_name.setText(itemList.get(position).getName());
        holder.brightness.setBackground(ContextCompat.getDrawable(context, itemList.get(position).getBrightness()));
        holder.auto_brightness.setBackground(ContextCompat.getDrawable(context, itemList.get(position).getAuto_brightness()));

        if (itemList.get(position).isInverse_color())
            holder.auto_brightness.setColorFilter(ContextCompat.getColor(context, R.color.textColorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        refreshButton(holder);

        enableOnClickListener(holder);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (Prefs.getBoolean(BRIGHTNESSBAR_KEY.get(holder.getBindingAdapterPosition())))
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_selected));
        else
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container));

        refreshButton(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    // Function for onClick events
    private void enableOnClickListener(ViewHolder holder) {
        // Set onClick operation for each item
        holder.container.setOnClickListener(v -> {
            selectedItem = selectedItem == holder.getBindingAdapterPosition() ? -1 : holder.getBindingAdapterPosition();
            refreshLayout(holder);

            if (!Prefs.getBoolean(BRIGHTNESSBAR_KEY.get(holder.getBindingAdapterPosition()))) {
                holder.btn_disable.setVisibility(View.GONE);
                if (holder.btn_enable.getVisibility() == View.VISIBLE)
                    holder.btn_enable.setVisibility(View.GONE);
                else
                    holder.btn_enable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.GONE);
                if (holder.btn_disable.getVisibility() == View.VISIBLE)
                    holder.btn_disable.setVisibility(View.GONE);
                else
                    holder.btn_disable.setVisibility(View.VISIBLE);
            }
        });

        // Set onClick operation for Enable button
        holder.btn_enable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            @SuppressLint("SetTextI18n") Runnable runnable = () -> {
                if (Objects.equals(variant, "BBN"))
                    BrightnessBarManager.enableOverlay(holder.getBindingAdapterPosition() + 1);
                else if (Objects.equals(variant, "BBP"))
                    BrightnessBarPixelManager.enableOverlay(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        holder.btn_enable.setVisibility(View.GONE);
                        holder.btn_disable.setVisibility(View.VISIBLE);
                        refreshBackground(holder);

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        holder.btn_disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                if (Objects.equals(variant, "BBN"))
                    BrightnessBarManager.disable_pack(holder.getBindingAdapterPosition() + 1);
                else if (Objects.equals(variant, "BBP"))
                    BrightnessBarPixelManager.disable_pack(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        holder.btn_disable.setVisibility(View.GONE);
                        holder.btn_enable.setVisibility(View.VISIBLE);
                        refreshBackground(holder);

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to check for layout changes
    private void refreshLayout(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.brightness_bar_child);

                if (!(view == holder.container) && child != null) {
                    child.findViewById(R.id.enable_brightnessbar).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_brightnessbar).setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for applied options
    @SuppressLint("SetTextI18n")
    private void refreshBackground(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.brightness_bar_child);

                if (child != null) {
                    if (i == holder.getAbsoluteAdapterPosition() && Prefs.getBoolean(BRIGHTNESSBAR_KEY.get(i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition()))))
                        child.setBackground(ContextCompat.getDrawable(context, R.drawable.container_selected));
                    else
                        child.setBackground(ContextCompat.getDrawable(context, R.drawable.container));
                }
            }
        }
    }

    private void refreshButton(ViewHolder holder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btn_enable.setVisibility(View.GONE);
            holder.btn_disable.setVisibility(View.GONE);
        } else {
            if (Prefs.getBoolean(BRIGHTNESSBAR_KEY.get(selectedItem))) {
                holder.btn_enable.setVisibility(View.GONE);
                holder.btn_disable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.VISIBLE);
                holder.btn_disable.setVisibility(View.GONE);
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView style_name;
        ImageView brightness, auto_brightness;
        Button btn_enable, btn_disable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.brightness_bar_child);
            style_name = itemView.findViewById(R.id.brightnessbar_title);
            brightness = itemView.findViewById(R.id.brightness_bar);
            auto_brightness = itemView.findViewById(R.id.auto_brightness_icon);
            btn_enable = itemView.findViewById(R.id.enable_brightnessbar);
            btn_disable = itemView.findViewById(R.id.disable_brightnessbar);
        }
    }
}
