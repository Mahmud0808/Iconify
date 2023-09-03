package com.drdisagree.iconify.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.models.InfoModel;

import java.util.ArrayList;
import java.util.Objects;

public class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    Context context;
    ArrayList<InfoModel> itemList;

    public InfoAdapter(Context context, ArrayList<InfoModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER)
            return new HeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_info_header, parent, false));
        else if (viewType == TYPE_ITEM)
            return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.view_list_info_item, parent, false));

        throw new RuntimeException("There is no type that matches the type " + viewType + ". + make sure you are using types correctly.");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
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
        RelativeLayout container;
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