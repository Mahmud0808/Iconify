package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;

import java.util.ArrayList;
import java.util.Objects;

public class ClockPreviewAdapter extends RecyclerView.Adapter<ClockPreviewAdapter.ViewHolder> {

    Context context;
    ArrayList<ClockModel> itemList;
    RecyclerView recyclerView;
    String prefSwitch, prefStyle;

    public ClockPreviewAdapter(Context context, ArrayList<ClockModel> itemList, String prefSwitch, String prefStyle) {
        this.context = context;
        this.itemList = itemList;
        this.prefSwitch = prefSwitch;
        this.prefStyle = prefStyle;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_header_clock_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (itemList.get(position).getClock().getParent() == null)
            holder.clock.addView(itemList.get(position).getClock());

        holder.clock.setOnClickListener(v -> {
            RPrefs.putInt(prefStyle, position + 1);
            if (Objects.equals(prefSwitch, LSCLOCK_SWITCH) && RPrefs.getBoolean(prefSwitch, false))
                new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
            else {
                if (RPrefs.getBoolean(prefSwitch, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
            Toast.makeText(context, context.getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
        });

        holder.itemView.requestLayout();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout clock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            clock = (LinearLayout) itemView.findViewById(R.id.header_clock_preview);
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
