package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Const.SETTINGS_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Preferences.SELECTED_SWITCH;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import com.drdisagree.iconify.ui.models.SwitchModel;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.compiler.OnDemandCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SwitchAdapter extends RecyclerView.Adapter<SwitchAdapter.ViewHolder> {

    Context context;
    ArrayList<SwitchModel> itemList;
    LoadingDialog loadingDialog;
    LinearLayoutManager linearLayoutManager;

    public SwitchAdapter(Context context, ArrayList<SwitchModel> itemList, LoadingDialog loadingDialog) {
        this.context = context;
        this.itemList = itemList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_option_switch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(itemList.get(position).getTitle());
        holder.aSwitch.setTrackDrawable(ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getTrack(), null));
        holder.aSwitch.setThumbDrawable(ResourcesCompat.getDrawable(context.getResources(), itemList.get(position).getThumb(), null));

        holder.aSwitch.setChecked(Prefs.getInt(SELECTED_SWITCH, -1) == position);

        enableOnCheckedChangeListener(holder);

        if (getItemCount() == 1) {
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container));
            holder.container.removeView(holder.divider);
        } else {
            if (position == 0)
                holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_top));
            else if (position == getItemCount() - 1) {
                holder.container.removeView(holder.divider);
                holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_bottom));
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        holder.aSwitch.setChecked(Prefs.getInt(SELECTED_SWITCH, -1) == holder.getBindingAdapterPosition());
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    // Function to check for applied options
    @SuppressLint({"SetTextI18n", "UseSwitchCompatOrMaterialCode"})
    private void refreshSwitches(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);

            if (view != null) {
                Switch aSwitch = view.findViewById(R.id.switch_view);

                if (aSwitch != null)
                    aSwitch.setChecked(i == holder.getAbsoluteAdapterPosition() && Prefs.getInt(SELECTED_SWITCH, -1) == (i - (holder.getAbsoluteAdapterPosition() - holder.getBindingAdapterPosition())));
            }
        }
    }

    private void enableOnCheckedChangeListener(ViewHolder holder) {
        holder.aSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                new Handler().postDelayed(() -> {
                    if (b) {
                        if (!Environment.isExternalStorageManager()) {
                            SystemUtil.getStoragePermission(context);
                            holder.aSwitch.setChecked(false);
                        } else {
                            // Show loading dialog
                            loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

                            Runnable runnable = () -> {
                                AtomicBoolean hasErroredOut = new AtomicBoolean(false);
                                Prefs.putInt(SELECTED_SWITCH, holder.getBindingAdapterPosition());

                                try {
                                    hasErroredOut.set(OnDemandCompiler.buildOverlay("SWITCH", holder.getBindingAdapterPosition() + 1, SETTINGS_PACKAGE));
                                } catch (IOException e) {
                                    hasErroredOut.set(true);
                                    holder.aSwitch.setChecked(false);
                                    Log.e("Switch", e.toString());
                                }

                                ((Activity) context).runOnUiThread(() -> {
                                    new Handler().postDelayed(() -> {
                                        // Hide loading dialog
                                        loadingDialog.hide();

                                        if (!hasErroredOut.get()) {
                                            // Change button visibility
                                            refreshSwitches(holder);

                                            Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Prefs.putInt(SELECTED_SWITCH, -1);
                                            holder.aSwitch.setChecked(false);
                                            Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }, 1000);
                                });
                            };
                            Thread thread = new Thread(runnable);
                            thread.start();
                        }
                    } else {
                        // Show loading dialog
                        loadingDialog.show(context.getResources().getString(R.string.loading_dialog_wait));

                        Runnable runnable = () -> {
                            Prefs.putInt(SELECTED_SWITCH, -1);
                            OverlayUtil.disableOverlay("IconifyComponentSWITCH.overlay");

                            ((Activity) context).runOnUiThread(() -> {
                                new Handler().postDelayed(() -> {
                                    // Hide loading dialog
                                    loadingDialog.hide();

                                    // Change button visibility
                                    refreshSwitches(holder);

                                    Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                                }, 1000);
                            });
                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
                    }
                }, SWITCH_ANIMATION_DELAY);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView title;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch aSwitch;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.list_item_switch);
            title = itemView.findViewById(R.id.title);
            aSwitch = itemView.findViewById(R.id.switch_view);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
