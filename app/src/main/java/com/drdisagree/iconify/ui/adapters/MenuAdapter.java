package com.drdisagree.iconify.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    Context context;
    ArrayList<Object[]> itemList;

    public MenuAdapter(Context context, ArrayList<Object[]> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_menu, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText((String) itemList.get(position)[1]);
        holder.desc.setText((String) itemList.get(position)[2]);
        holder.icon.setImageResource((int) itemList.get(position)[3]);

        holder.container.setOnClickListener(v -> {
            Intent intent = new Intent(context, (Class<?>) itemList.get(position)[0]);
            context.startActivity(intent);
        });

        if (position == itemList.size() - 1)
            ((ViewGroup.MarginLayoutParams) holder.container.getLayoutParams()).setMargins(0, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView title, desc;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.list_item);
            title = itemView.findViewById(R.id.list_title);
            desc = itemView.findViewById(R.id.list_desc);
            icon = itemView.findViewById(R.id.list_preview);
        }
    }
}
