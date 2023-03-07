package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE;
import static com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.models.HeaderClockModel;
import com.drdisagree.iconify.utils.HelperUtil;

import java.util.ArrayList;

public class HeaderClockAdapter extends RecyclerView.Adapter<HeaderClockAdapter.ViewHolder> {

    Context context;
    ArrayList<HeaderClockModel> itemList;
    RecyclerView recyclerView;

    public HeaderClockAdapter(Context context, ArrayList<HeaderClockModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_header_clock_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.clock.addView(itemList.get(position).getClock());

        holder.clock.setOnClickListener(v -> {
            RPrefs.putInt(HEADER_CLOCK_STYLE, position + 1);
            if (RPrefs.getBoolean(HEADER_CLOCK_SWITCH, false)) {
                new Handler().postDelayed(HelperUtil::forceApply, 200);
            }
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
