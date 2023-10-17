package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.ui.utils.ViewBindingHelpers.setDrawable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.models.QsShapeModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.DisplayUtil;
import com.drdisagree.iconify.utils.overlay.manager.QsShapeManager;
import com.drdisagree.iconify.utils.overlay.manager.QsShapePixelManager;

import java.util.ArrayList;
import java.util.Objects;

public class QsShapeAdapter extends RecyclerView.Adapter<QsShapeAdapter.ViewHolder> {

    Context context;
    ArrayList<QsShapeModel> itemList;
    ArrayList<String> QSSHAPE_KEY = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    LoadingDialog loadingDialog;
    int selectedItem = -1;
    String variant;

    public QsShapeAdapter(Context context, ArrayList<QsShapeModel> itemList, LoadingDialog loadingDialog, String variant) {
        this.context = context;
        this.variant = variant;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;

        // Preference key
        for (int i = 1; i <= itemList.size(); i++)
            QSSHAPE_KEY.add("IconifyComponent" + variant + i + ".overlay");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(Objects.equals(variant, "QSSN") ? R.layout.view_list_option_qsshape : R.layout.view_list_option_qsshape_pixel, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.style_name.setText(itemList.get(position).getName());

        setDrawable(holder.qs_tile1, ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getEnabled_drawable(), null));
        setDrawable(holder.qs_tile2, ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getDisabled_drawable(), null));
        setDrawable(holder.qs_tile3, ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getDisabled_drawable(), null));
        setDrawable(holder.qs_tile4, ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getEnabled_drawable(), null));

        int textColor;

        if (Objects.equals(variant, "QSSN")) {
            textColor = itemList.get(position).isInverse_color() ? R.color.textColorPrimary : R.color.textColorPrimaryInverse;
        } else {
            textColor = itemList.get(position).isInverse_color() && SystemUtil.isDarkMode() ? R.color.textColorPrimary : R.color.textColorPrimaryInverse;
        }

        holder.qs_text1.setTextColor(ContextCompat.getColor(context, textColor));
        holder.qs_icon1.setColorFilter(ContextCompat.getColor(context, textColor), android.graphics.PorterDuff.Mode.SRC_IN);
        holder.qs_text4.setTextColor(ContextCompat.getColor(context, textColor));
        holder.qs_icon4.setColorFilter(ContextCompat.getColor(context, textColor), android.graphics.PorterDuff.Mode.SRC_IN);

        if (itemList.get(position).getIcon_margin_start() != null && itemList.get(position).getIcon_margin_end() != null)
            setMargin(holder, itemList.get(position).getIcon_margin_start(), itemList.get(position).getIcon_margin_end());
        else
            setMargin(holder, 0, 10);

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            holder.orientation.setOrientation(LinearLayout.VERTICAL);
        } else {
            holder.orientation.setOrientation(LinearLayout.HORIZONTAL);
        }

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

        itemSelected(holder.container, Prefs.getBoolean(QSSHAPE_KEY.get(holder.getBindingAdapterPosition())));

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

            if (!Prefs.getBoolean(QSSHAPE_KEY.get(holder.getBindingAdapterPosition()))) {
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
                if (Objects.equals(variant, "QSSN"))
                    QsShapeManager.enableOverlay(holder.getBindingAdapterPosition() + 1);
                else if (Objects.equals(variant, "QSSP"))
                    QsShapePixelManager.enableOverlay(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
                if (Objects.equals(variant, "QSSN"))
                    QsShapeManager.disableOverlay(holder.getBindingAdapterPosition() + 1);
                else if (Objects.equals(variant, "QSSP"))
                    QsShapePixelManager.disableOverlay(holder.getBindingAdapterPosition() + 1);

                ((Activity) context).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
                LinearLayout child = view.findViewById(R.id.qsshape_child);

                if (!(view == holder.container) && child != null) {
                    child.findViewById(R.id.enable_qsshape).setVisibility(View.GONE);
                    child.findViewById(R.id.disable_qsshape).setVisibility(View.GONE);
                }
            }
        }
    }

    // Function to check for applied options
    private void refreshBackground(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                LinearLayout child = view.findViewById(R.id.qsshape_child);

                if (child != null) {
                    itemSelected(child, i == holder.getAbsoluteAdapterPosition() && Prefs.getBoolean(QSSHAPE_KEY.get(i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition()))));
                }
            }
        }
    }

    private void refreshButton(ViewHolder holder) {
        if (holder.getBindingAdapterPosition() != selectedItem) {
            holder.btn_enable.setVisibility(View.GONE);
            holder.btn_disable.setVisibility(View.GONE);
        } else {
            if (Prefs.getBoolean(QSSHAPE_KEY.get(selectedItem))) {
                holder.btn_enable.setVisibility(View.GONE);
                holder.btn_disable.setVisibility(View.VISIBLE);
            } else {
                holder.btn_enable.setVisibility(View.VISIBLE);
                holder.btn_disable.setVisibility(View.GONE);
            }
        }
    }

    private void setMargin(ViewHolder holder, int iconMarginLeft, int iconMarginRight) {
        ViewGroup.MarginLayoutParams marginParams;
        LinearLayout.LayoutParams layoutParams;

        marginParams = new ViewGroup.MarginLayoutParams(holder.qs_icon1.getLayoutParams());
        marginParams.setMarginStart(DisplayUtil.IntToDp(iconMarginLeft));
        marginParams.setMarginEnd(DisplayUtil.IntToDp(iconMarginRight));
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        holder.qs_icon1.setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(holder.qs_icon2.getLayoutParams());
        marginParams.setMarginStart(DisplayUtil.IntToDp(iconMarginLeft));
        marginParams.setMarginEnd(DisplayUtil.IntToDp(iconMarginRight));
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        holder.qs_icon2.setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(holder.qs_icon3.getLayoutParams());
        marginParams.setMarginStart(DisplayUtil.IntToDp(iconMarginLeft));
        marginParams.setMarginEnd(DisplayUtil.IntToDp(iconMarginRight));
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        holder.qs_icon3.setLayoutParams(layoutParams);

        marginParams = new ViewGroup.MarginLayoutParams(holder.qs_icon4.getLayoutParams());
        marginParams.setMarginStart(DisplayUtil.IntToDp(iconMarginLeft));
        marginParams.setMarginEnd(DisplayUtil.IntToDp(iconMarginRight));
        layoutParams = new LinearLayout.LayoutParams(marginParams);
        holder.qs_icon4.setLayoutParams(layoutParams);
    }

    private void itemSelected(View parent, boolean state) {
        if (state) {
            parent.setBackground(ContextCompat.getDrawable(context, R.drawable.container_selected));
            ((TextView) parent.findViewById(R.id.list_title_qsshape)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            parent.findViewById(R.id.icon_selected).setVisibility(View.VISIBLE);
        } else {
            parent.setBackground(ContextCompat.getDrawable(context, R.drawable.item_background_material));
            ((TextView) parent.findViewById(R.id.list_title_qsshape)).setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
            parent.findViewById(R.id.icon_selected).setVisibility(View.INVISIBLE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container, orientation;
        TextView style_name;
        Button btn_enable, btn_disable;
        LinearLayout qs_tile1, qs_tile2, qs_tile3, qs_tile4;
        ImageView qs_icon1, qs_icon2, qs_icon3, qs_icon4;
        TextView qs_text1, qs_text2, qs_text3, qs_text4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.qsshape_child);
            style_name = itemView.findViewById(R.id.list_title_qsshape);
            btn_enable = itemView.findViewById(R.id.enable_qsshape);
            btn_disable = itemView.findViewById(R.id.disable_qsshape);
            qs_tile1 = itemView.findViewById(R.id.qs_tile1);
            qs_tile2 = itemView.findViewById(R.id.qs_tile2);
            qs_tile3 = itemView.findViewById(R.id.qs_tile3);
            qs_tile4 = itemView.findViewById(R.id.qs_tile4);
            qs_icon1 = itemView.findViewById(R.id.qs_icon1);
            qs_icon2 = itemView.findViewById(R.id.qs_icon2);
            qs_icon3 = itemView.findViewById(R.id.qs_icon3);
            qs_icon4 = itemView.findViewById(R.id.qs_icon4);
            qs_text1 = itemView.findViewById(R.id.qs_text1);
            qs_text2 = itemView.findViewById(R.id.qs_text2);
            qs_text3 = itemView.findViewById(R.id.qs_text3);
            qs_text4 = itemView.findViewById(R.id.qs_text4);
            orientation = itemView.findViewById(R.id.qs_tile_orientation);
        }
    }
}
