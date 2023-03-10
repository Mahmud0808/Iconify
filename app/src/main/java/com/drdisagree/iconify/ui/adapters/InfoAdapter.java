package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.EASTER_EGG;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.models.InfoModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    final double SECONDS_FOR_CLICKS = 3;
    final int NUM_CLICKS_REQUIRED = 7;
    Context context;
    ArrayList<InfoModel> itemList;
    long[] clickTimestamps = new long[NUM_CLICKS_REQUIRED];
    int oldestIndex = 0;
    int nextIndex = 0;

    public InfoAdapter(Context context, ArrayList<InfoModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemCount() == 1)
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_info_app, parent, false));
        else {
            if (viewType == TYPE_HEADER)
                return new HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_info_header, parent, false));
            else if (viewType == TYPE_ITEM)
                return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_info_item, parent, false));
        }
        throw new RuntimeException("There is no type that matches the type " + viewType + ". + make sure you are using types correctly.");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder)
            ((ViewHolder) holder).container.setOnClickListener(v -> onAppInfoViewClicked(((ViewHolder) holder).container));
        else if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).header.setText(itemList.get(position).getTitle());

            if (Objects.equals(itemList.get(position).getTitle(), "")) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).icon.setImageResource(itemList.get(position).getIcon());
            ((ItemViewHolder) holder).title.setText(itemList.get(position).getTitle());
            ((ItemViewHolder) holder).desc.setText(itemList.get(position).getDesc());
            ((ItemViewHolder) holder).container.setOnClickListener(itemList.get(position).getOnClickListener());

            if (getItemCount() == 2) {
                ((ItemViewHolder) holder).container.setBackground(ContextCompat.getDrawable(context, R.drawable.container));
                ((ItemViewHolder) holder).container.removeView(((ItemViewHolder) holder).divider);
            } else {
                if (position == 1)
                    ((ItemViewHolder) holder).container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_top));
                else if (position == getItemCount() - 1) {
                    ((ItemViewHolder) holder).container.removeView(((ItemViewHolder) holder).divider);
                    ((ItemViewHolder) holder).container.setBackground(ContextCompat.getDrawable(context, R.drawable.container_bottom));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private void onAppInfoViewClicked(View v) {
        long timeMillis = (new Date()).getTime();

        if (nextIndex == (NUM_CLICKS_REQUIRED - 1) || oldestIndex > 0) {
            int diff = (int) (timeMillis - clickTimestamps[oldestIndex]);
            if (diff < SECONDS_FOR_CLICKS * 1000) {
                if (!Prefs.getBoolean(EASTER_EGG)) {
                    Prefs.putBoolean(EASTER_EGG, true);
                    Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_easter_egg), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Iconify.getAppContext(), context.getResources().getString(R.string.toast_easter_egg_activated), Toast.LENGTH_SHORT).show();
                }
                oldestIndex = 0;
                nextIndex = 0;
            } else oldestIndex++;
        }

        clickTimestamps[nextIndex] = timeMillis;
        nextIndex++;

        if (nextIndex == NUM_CLICKS_REQUIRED) nextIndex = 0;

        if (oldestIndex == NUM_CLICKS_REQUIRED) oldestIndex = 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.info_app);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            header = itemView.findViewById(R.id.list_info_header);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, desc;
        LinearLayout container;
        View divider;

        public ItemViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            container = itemView.findViewById(R.id.list_info_item);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}