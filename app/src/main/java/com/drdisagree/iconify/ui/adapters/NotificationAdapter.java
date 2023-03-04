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
import com.drdisagree.iconify.overlaymanager.NotificationManager;
import com.drdisagree.iconify.overlaymanager.NotificationPixelManager;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    RecyclerView recyclerView;
    ArrayList<Object[]> itemList;
    ArrayList<String> NOTIFICATION_KEY = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;
    String key;

    public NotificationAdapter(Context context, RecyclerView recyclerView, ArrayList<Object[]> itemList, LoadingDialog loadingDialog, String key) {
        this.key = key;
        this.context = context;
        this.itemList = itemList;
        this.recyclerView = recyclerView;
        this.loadingDialog = loadingDialog;
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        // Generate keys for preference
        for (int i = 0; i < itemList.size(); i++) {
            NOTIFICATION_KEY.add("IconifyComponentNFN" + key + (i + 1) + ".overlay");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_option_notification, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.container.setBackground(ContextCompat.getDrawable(context, (int) itemList.get(position)[1]));
        holder.style_name.setText((String) itemList.get(position)[0]);
        holder.ic_collapse_expand.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_expand_arrow));

        if (Prefs.getBoolean(NOTIFICATION_KEY.get(position))) {
            holder.style_name.setText(holder.style_name.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), "") + ' ' + context.getResources().getString(R.string.opt_applied));
            holder.style_name.setTextColor(context.getResources().getColor(R.color.colorSuccess));
        } else {
            holder.style_name.setText(holder.style_name.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), ""));
            holder.style_name.setTextColor(context.getResources().getColor(R.color.textColorPrimaryNoTint));
        }

        if (position != selectedItem) {
            holder.btn_enable.setVisibility(View.GONE);
            holder.btn_disable.setVisibility(View.GONE);
        }

        enableOnClickListener(holder.container, holder.btn_enable, holder.btn_disable, NOTIFICATION_KEY.get(position), position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Function for onClick events
    private void enableOnClickListener(LinearLayout layout, Button enable, Button disable, String key, int index) {
        // Set onClick operation for each item
        layout.setOnClickListener(v -> {
            selectedItem = index;
            refreshLayout(layout);

            if (!Prefs.getBoolean(key)) {
                disable.setVisibility(View.GONE);
                if (enable.getVisibility() == View.VISIBLE) {
                    enable.setVisibility(View.GONE);
                    layout.findViewById(R.id.notif_arrow).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_expand_arrow));
                } else {
                    enable.setVisibility(View.VISIBLE);
                    layout.findViewById(R.id.notif_arrow).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_collapse_arrow));
                }
            } else {
                enable.setVisibility(View.GONE);
                if (disable.getVisibility() == View.VISIBLE) {
                    disable.setVisibility(View.GONE);
                    layout.findViewById(R.id.notif_arrow).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_expand_arrow));
                } else {
                    disable.setVisibility(View.VISIBLE);
                    layout.findViewById(R.id.notif_arrow).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_collapse_arrow));
                }
            }
        });

        // Set onClick operation for Enable button
        enable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            @SuppressLint("SetTextI18n") Runnable runnable = () -> {
                if (Objects.equals(key, "NFN")) NotificationManager.enableOverlay(index + 1);
                else if (Objects.equals(key, "NFP"))
                    NotificationPixelManager.enableOverlay(index + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        if (loadingDialog != null) loadingDialog.hide();

                        // Change name to " - applied"
                        TextView title = layout.findViewById(R.id.notif_title);
                        title.setText(title.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), "") + ' ' + context.getResources().getString(R.string.opt_applied));
                        title.setTextColor(context.getResources().getColor(R.color.colorSuccess));

                        // Change button visibility
                        enable.setVisibility(View.GONE);
                        disable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Set onClick operation for Disable button
        disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                if (Objects.equals(key, "NFN")) NotificationManager.disable_pack(index + 1);
                else if (Objects.equals(key, "NFP"))
                    NotificationPixelManager.disable_pack(index + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change name back to original
                        TextView title = layout.findViewById(R.id.notif_title);
                        title.setText(title.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), ""));
                        title.setTextColor(context.getResources().getColor(R.color.textColorPrimaryNoTint));

                        // Change button visibility
                        disable.setVisibility(View.GONE);
                        enable.setVisibility(View.VISIBLE);
                        refreshBackground();

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    // Function to check for layout changes
    private void refreshLayout(LinearLayout layout) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.notification_child);

                if (!(view == layout)) {
                    child.findViewById(R.id.enable_notif).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_notif).setVisibility(View.GONE);
                    child.findViewById(R.id.notif_arrow).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_expand_arrow));
                }
            }
        }
    }

    // Function to check for applied options
    @SuppressLint("SetTextI18n")
    private void refreshBackground() {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.notification_child);
                TextView title = child.findViewById(R.id.notif_title);

                if (Prefs.getBoolean(NOTIFICATION_KEY.get(i))) {
                    title.setText(title.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), "") + ' ' + context.getResources().getString(R.string.opt_applied));
                    title.setTextColor(context.getResources().getColor(R.color.colorSuccess));
                } else {
                    title.setText(title.getText().toString().replace(' ' + context.getResources().getString(R.string.opt_applied), ""));
                    title.setTextColor(context.getResources().getColor(R.color.textColorPrimaryNoTint));
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView style_name;
        ImageView ic_collapse_expand;
        Button btn_enable, btn_disable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.notification_child);
            style_name = itemView.findViewById(R.id.notif_title);
            ic_collapse_expand = itemView.findViewById(R.id.notif_arrow);
            btn_enable = itemView.findViewById(R.id.enable_notif);
            btn_disable = itemView.findViewById(R.id.disable_notif);
        }
    }
}
