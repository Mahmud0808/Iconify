package com.drdisagree.iconify.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.ui.models.MenuModel;
import com.drdisagree.iconify.ui.widgets.MenuWidget;

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
        return new ViewHolder(new MenuWidget(context));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuWidget menu = (MenuWidget) holder.itemView;
        menu.setTitle(itemList.get(position).getTitle());
        menu.setSummary(itemList.get(position).getDesc());
        menu.setIcon(itemList.get(position).getIcon());
        menu.setEndArrowVisibility(View.VISIBLE);
        menu.setOnClickListener(v -> Navigation.findNavController(v).navigate(itemList.get(position).getId()));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
