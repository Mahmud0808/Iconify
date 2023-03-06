package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.DISABLE_SCROLLING_ANIMATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.models.MenuModel;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    Context context;
    ArrayList<MenuModel> itemList;

    public MenuAdapter(Context context, ArrayList<MenuModel> itemList) {
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
        holder.title.setText(itemList.get(position).getTitle());
        holder.desc.setText(itemList.get(position).getDesc());
        holder.icon.setImageResource(itemList.get(position).getIcon());

        holder.container.setOnClickListener(v -> {
            Intent intent = new Intent(context, itemList.get(position).getaClass());
            context.startActivity(intent);
        });

        if (!Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            holder.container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_anim));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (!Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            holder.container.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_anim));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        if (!Prefs.getBoolean(DISABLE_SCROLLING_ANIMATION, false))
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim));
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
