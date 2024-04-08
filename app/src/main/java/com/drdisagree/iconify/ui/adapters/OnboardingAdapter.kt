package com.drdisagree.iconify.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.databinding.ViewOnboardingPageItemBinding;
import com.drdisagree.iconify.ui.entity.OnboardingPage;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PagerViewHolder> {

    private final OnboardingPage[] onBoardingPageList;

    public OnboardingAdapter() {
        this.onBoardingPageList = OnboardingPage.values();
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewOnboardingPageItemBinding binding = ViewOnboardingPageItemBinding.inflate(inflater, parent, false);
        return new PagerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        holder.bind(onBoardingPageList[position]);
    }

    @Override
    public int getItemCount() {
        return onBoardingPageList.length;
    }

    public static class PagerViewHolder extends RecyclerView.ViewHolder {

        private final ViewOnboardingPageItemBinding binding;
        private final TextView titleTv;
        private final TextView subTitleTv;
        private final TextView descTV;
        private final ImageView img;

        public PagerViewHolder(ViewOnboardingPageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.titleTv = binding.titleTv;
            this.subTitleTv = binding.subTitleTv;
            this.descTV = binding.descTV;
            this.img = binding.img;
        }

        public void bind(OnboardingPage onBoardingPage) {
            Context context = binding.getRoot().getContext();
            Resources res = context.getResources();

            titleTv.setText(res.getString(onBoardingPage.getTitleResource()));
            subTitleTv.setText(res.getString(onBoardingPage.getSubTitleResource()));
            descTV.setText(res.getString(onBoardingPage.getDescriptionResource()));
            img.setImageResource(onBoardingPage.getLogoResource());
        }
    }
}
