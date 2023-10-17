package com.drdisagree.iconify.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.drdisagree.iconify.ui.models.IconPackModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.overlay.manager.IconPackManager;

import java.util.ArrayList;

public class IconPackAdapter extends RecyclerView.Adapter<IconPackAdapter.ViewHolder> {

    Context context;
    ArrayList<IconPackModel> itemList;
    ArrayList<String> ICONPACK_KEY = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;

    public IconPackAdapter(Context context, ArrayList<IconPackModel> itemList, LoadingDialog loadingDialog) {
        this.context = context;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;

        // Preference key
        for (int i = 1; i <= itemList.size(); i++)
            ICONPACK_KEY.add("IconifyComponentIPAS" + i + ".overlay");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_option_iconpack, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.style_name.setText(itemList.get(position).getName());
        holder.desc.setText(context.getResources().getString(itemList.get(position).getDesc()));
        holder.icon1.setImageResource(itemList.get(position).getIcon1());
        holder.icon2.setImageResource(itemList.get(position).getIcon2());
        holder.icon3.setImageResource(itemList.get(position).getIcon3());
        holder.icon4.setImageResource(itemList.get(position).getIcon4());

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

        itemSelected(holder.container, Prefs.getBoolean(ICONPACK_KEY.get(holder.getBindingAdapterPosition())));
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

            if (!Prefs.getBoolean(ICONPACK_KEY.get(holder.getBindingAdapterPosition()))) {
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
            // Show loading dialog
            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

            @SuppressLint("SetTextI18n") Runnable runnable = () -> {
                IconPackManager.enableOverlay(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        holder.btn_enable.setVisibility(View.GONE);
                        holder.btn_disable.setVisibility(View.VISIBLE);
                        refreshBackground(holder);

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 3000);
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
                IconPackManager.disableOverlay(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        // Change button visibility
                        holder.btn_disable.setVisibility(View.GONE);
                        holder.btn_enable.setVisibility(View.VISIBLE);
                        refreshBackground(holder);

                        Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 3000);
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
                LinearLayout child = view.findViewById(R.id.icon_pack_child);

                if (!(view == holder.container) && child != null) {
                    child.findViewById(R.id.enable_iconpack).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_iconpack).setVisibility(View.GONE);
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
                LinearLayout child = view.findViewById(R.id.icon_pack_child);

                if (child != null) {
                    itemSelected(child, i == holder.getAbsoluteAdapterPosition() && Prefs.getBoolean(ICONPACK_KEY.get(i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition()))));
                }
            }
        }
    }

    private void refreshButton(ViewHolder holder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btn_enable.setVisibility(View.GONE);
            holder.btn_disable.setVisibility(View.GONE);
        } else {
            if (Prefs.getBoolean(ICONPACK_KEY.get(selectedItem))) {
                holder.btn_enable.setVisibility(View.GONE);
                holder.btn_disable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.VISIBLE);
                holder.btn_disable.setVisibility(View.GONE);
            }
        }
    }

    private void itemSelected(View parent, boolean state) {
        if (state) {
            parent.setBackground(ContextCompat.getDrawable(context, R.drawable.container_selected));
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            parent.findViewById(R.id.icon_selected).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(0.8f);
        } else {
            parent.setBackground(ContextCompat.getDrawable(context, R.drawable.item_background_material));
            ((TextView) parent.findViewById(R.id.iconpack_title)).setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
            ((TextView) parent.findViewById(R.id.iconpack_desc)).setTextColor(ContextCompat.getColor(context, R.color.text_color_secondary));
            parent.findViewById(R.id.icon_selected).setVisibility(View.INVISIBLE);
            parent.findViewById(R.id.iconpack_desc).setAlpha(1f);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView style_name, desc;
        ImageView icon1, icon2, icon3, icon4;
        Button btn_enable, btn_disable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.icon_pack_child);
            style_name = itemView.findViewById(R.id.iconpack_title);
            desc = itemView.findViewById(R.id.iconpack_desc);
            icon1 = itemView.findViewById(R.id.iconpack_preview1);
            icon2 = itemView.findViewById(R.id.iconpack_preview2);
            icon3 = itemView.findViewById(R.id.iconpack_preview3);
            icon4 = itemView.findViewById(R.id.iconpack_preview4);
            btn_enable = itemView.findViewById(R.id.enable_iconpack);
            btn_disable = itemView.findViewById(R.id.disable_iconpack);
        }
    }
}
