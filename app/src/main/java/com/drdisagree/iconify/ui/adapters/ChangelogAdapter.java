package com.drdisagree.iconify.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.models.ChangelogModel;

import java.util.ArrayList;

public class ChangelogAdapter extends RecyclerView.Adapter<ChangelogAdapter.ViewHolder> {

    Context context;
    ArrayList<ChangelogModel> itemList;
    LinearLayoutManager linearLayoutManager;

    public ChangelogAdapter(Context context, ArrayList<ChangelogModel> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_list_changelog, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(Html.fromHtml(itemList.get(position).getTitle()));
        holder.changes.setText(Html.fromHtml(itemList.get(position).getChanges()));

        if (holder.changes.getText() == "")
            holder.changes.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView title, changes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.list_item_changelog);
            title = itemView.findViewById(R.id.changelog_title);
            changes = itemView.findViewById(R.id.changelog_text);
        }
    }
}
