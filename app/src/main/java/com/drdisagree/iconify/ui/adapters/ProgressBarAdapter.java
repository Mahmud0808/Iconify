package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.SELECTED_PROGRESSBAR;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
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
import com.drdisagree.iconify.ui.models.ProgressBarModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressBarAdapter extends RecyclerView.Adapter<ProgressBarAdapter.ViewHolder> {

    Context context;
    ArrayList<ProgressBarModel> itemList;
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;

    public ProgressBarAdapter(Context context, ArrayList<ProgressBarModel> itemList, LoadingDialog loadingDialog) {
        this.context = context;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_option_progressbar, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.style_name.setText(itemList.get(position).getName());
        holder.progressbar.setBackground(ContextCompat.getDrawable(context, itemList.get(position).getProgress()));

        if (Prefs.getInt(SELECTED_PROGRESSBAR, -1) == position)
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_selected));
        else
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container));

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

        if (Prefs.getInt(SELECTED_PROGRESSBAR, -1) == holder.getBindingAdapterPosition())
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

            if (!(Prefs.getInt(SELECTED_PROGRESSBAR, -1) == holder.getBindingAdapterPosition())) {
                holder.btn_disable.setVisibility(View.GONE);
                if (holder.btn_enable.getVisibility() == View.VISIBLE)
                    holder.btn_enable.setVisibility(View.GONE);
                else holder.btn_enable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.GONE);
                if (holder.btn_disable.getVisibility() == View.VISIBLE)
                    holder.btn_disable.setVisibility(View.GONE);
                else holder.btn_disable.setVisibility(View.VISIBLE);
            }
        });

        // Set onClick operation for Enable button
        holder.btn_enable.setOnClickListener(v -> {
            if (!Environment.isExternalStorageManager()) {
                SystemUtil.getStoragePermission(context);
            } else {
                // Show loading dialog
                loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

                Runnable runnable = () -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(false);
                    Prefs.putInt(SELECTED_PROGRESSBAR, holder.getBindingAdapterPosition());

                    try {
                        hasErroredOut.set(OnDemandCompiler.buildOverlay("PGB", holder.getBindingAdapterPosition() + 1, FRAMEWORK_PACKAGE));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("ProgressBar", e.toString());
                    }

                    ((Activity) context).runOnUiThread(() -> {
                        new Handler().postDelayed(() -> {
                            // Hide loading dialog
                            loadingDialog.hide();

                            if (!hasErroredOut.get()) {
                                // Change button visibility
                                holder.btn_enable.setVisibility(View.GONE);
                                holder.btn_disable.setVisibility(View.VISIBLE);
                                refreshBackground(holder);

                                Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                            } else {
                                Prefs.putInt(SELECTED_PROGRESSBAR, -1);
                                Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                            }
                        }, 1000);
                    });
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        // Set onClick operation for Disable button
        holder.btn_disable.setOnClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                Prefs.putInt(SELECTED_PROGRESSBAR, -1);
                OverlayUtil.disableOverlay("IconifyComponentPGB.overlay");

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
                LinearLayout child = view.findViewById(R.id.progressbar_child);

                if (!(view == holder.container) && child != null) {
                    child.findViewById(R.id.enable_progressbar).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_progressbar).setVisibility(View.GONE);
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
                LinearLayout child = view.findViewById(R.id.progressbar_child);

                if (child != null) {
                    if (i == holder.getAbsoluteAdapterPosition() && Prefs.getInt(SELECTED_PROGRESSBAR, -1) == (i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())))
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
            if (Prefs.getInt(SELECTED_PROGRESSBAR, -1) == selectedItem) {
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
        ImageView progressbar;
        Button btn_enable, btn_disable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.progressbar_child);
            style_name = itemView.findViewById(R.id.progressbar_title);
            progressbar = itemView.findViewById(R.id.progress_bar);
            btn_enable = itemView.findViewById(R.id.enable_progressbar);
            btn_disable = itemView.findViewById(R.id.disable_progressbar);
        }
    }
}
