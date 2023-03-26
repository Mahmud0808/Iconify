package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Const.FRAGMENT_TRANSITION_DELAY;
import static com.drdisagree.iconify.common.References.FRAGMENT_STYLES;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.models.MenuFragmentModel;

import java.util.ArrayList;

public class MenuFragmentAdapter extends RecyclerView.Adapter<MenuFragmentAdapter.ViewHolder> {

    Context context;
    ArrayList<MenuFragmentModel> itemList;
    FragmentManager fragmentManager;

    public MenuFragmentAdapter(Context context, ArrayList<MenuFragmentModel> itemList, FragmentManager fragmentManager) {
        this.context = context;
        this.itemList = itemList;
        this.fragmentManager = fragmentManager;
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
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);

            new Handler().postDelayed(() -> {
                fragmentTransaction.replace(R.id.main_fragment, itemList.get(position).getFragment(), itemList.get(position).getTag());
                fragmentManager.popBackStack(FRAGMENT_STYLES, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentTransaction.addToBackStack(itemList.get(position).getTag());
                fragmentTransaction.commit();
            }, FRAGMENT_TRANSITION_DELAY);
        });
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

        LinearLayout container;
        TextView title, desc;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.list_info_item);
            title = itemView.findViewById(R.id.list_title);
            desc = itemView.findViewById(R.id.list_desc);
            icon = itemView.findViewById(R.id.list_preview);
        }
    }
}
