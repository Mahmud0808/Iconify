package com.drdisagree.iconify.ui.adapters;

import static com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.models.ClockModel;
import com.drdisagree.iconify.utils.WallpaperUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class ClockPreviewAdapter extends RecyclerView.Adapter<ClockPreviewAdapter.ViewHolder> {

    Context context;
    ArrayList<ClockModel> itemList;
    String prefSwitch, prefStyle;
    static Bitmap wallpaperBitmap;
    LinearLayoutManager linearLayoutManager;

    public ClockPreviewAdapter(Context context, ArrayList<ClockModel> itemList, String prefSwitch, String prefStyle) {
        this.context = context;
        this.itemList = itemList;
        this.prefSwitch = prefSwitch;
        this.prefStyle = prefStyle;

        new WallpaperLoaderTask(this).execute();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_clock_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClockModel model = itemList.get(position);

        holder.title.setText(model.getTitle());

        holder.button.setOnClickListener(v -> {
            int index = position + (Objects.equals(prefSwitch, LSCLOCK_SWITCH) ? 0 : 1);
            RPrefs.putInt(prefStyle, index);
            refreshLayout(holder);
        });

        if (RPrefs.getInt(prefStyle, 0) != holder.getBindingAdapterPosition()) {
            holder.checkIcon.setVisibility(View.GONE);
            holder.button.setEnabled(true);
        } else {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.button.setEnabled(false);
        }

        holder.clockContainer.removeAllViews();
        ViewStub viewStub = new ViewStub(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_VERTICAL;
        viewStub.setLayoutParams(params);
        viewStub.setLayoutResource(model.getLayout());
        holder.clockContainer.addView(viewStub);

        if (viewStub.getParent() != null) {
            viewStub.inflate();
        }

        if (wallpaperBitmap != null) {
            holder.wallpaperView.setImageBitmap(wallpaperBitmap);
        }
    }

    // Function to check for layout changes
    private void refreshLayout(ViewHolder holder) {
        int firstVisible = linearLayoutManager.findFirstVisibleItemPosition() - 1;
        int lastVisible = linearLayoutManager.findLastVisibleItemPosition() + 1;

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = linearLayoutManager.findViewByPosition(i);
            if (view == null) continue;

            LinearLayout child = view.findViewById(R.id.clock_preview_child);
            if (child == null) continue;

            boolean isSelected = view == holder.container;
            child.findViewById(R.id.btn_select_style).setEnabled(!isSelected);
            child.findViewById(R.id.icon_selected).setVisibility(!isSelected ?
                    View.GONE :
                    View.VISIBLE
            );
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        if (RPrefs.getInt(prefStyle, 0) != holder.getBindingAdapterPosition()) {
            holder.checkIcon.setVisibility(View.GONE);
            holder.button.setEnabled(true);
        } else {
            holder.checkIcon.setVisibility(View.VISIBLE);
            holder.button.setEnabled(false);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        TextView title;
        LinearLayout clockContainer;
        ImageView checkIcon;
        MaterialButton button;
        ShapeableImageView wallpaperView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.clock_preview_child);
            title = itemView.findViewById(R.id.clock_title);
            clockContainer = itemView.findViewById(R.id.clock_view_container);
            checkIcon = itemView.findViewById(R.id.icon_selected);
            button = itemView.findViewById(R.id.btn_select_style);
            wallpaperView = itemView.findViewById(R.id.wallpaper_view);
        }
    }

    private static class WallpaperLoaderTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ClockPreviewAdapter> adapterRef;

        WallpaperLoaderTask(ClockPreviewAdapter adapter) {
            adapterRef = new WeakReference<>(adapter);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Context context = adapterRef.get().context;
            if (context == null) return null;
            return WallpaperUtil.getCompressedWallpaper(context, 80);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ClockPreviewAdapter adapter = adapterRef.get();
            if (adapter != null && bitmap != null) {
                wallpaperBitmap = bitmap;
                adapter.notifyDataSetChanged();
            }
        }
    }
}
