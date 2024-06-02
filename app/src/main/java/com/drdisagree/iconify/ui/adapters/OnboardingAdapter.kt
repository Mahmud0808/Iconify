package com.drdisagree.iconify.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.databinding.ViewOnboardingPageItemBinding
import com.drdisagree.iconify.ui.adapters.OnboardingAdapter.PagerViewHolder
import com.drdisagree.iconify.ui.entity.OnboardingPage

class OnboardingAdapter : RecyclerView.Adapter<PagerViewHolder>() {

    private val onBoardingPageList: Array<OnboardingPage> = OnboardingPage.entries.toTypedArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewOnboardingPageItemBinding.inflate(inflater, parent, false)
        return PagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(onBoardingPageList[position])
    }

    override fun getItemCount(): Int {
        return onBoardingPageList.size
    }

    class PagerViewHolder(
        private val binding: ViewOnboardingPageItemBinding
    ) : RecyclerView.ViewHolder(
        binding.getRoot()
    ) {

        private val titleTv: TextView
        private val subTitleTv: TextView
        private val descTV: TextView
        private val img: ImageView

        init {
            titleTv = binding.titleTv
            subTitleTv = binding.subTitleTv
            descTV = binding.descTV
            img = binding.img
        }

        fun bind(onBoardingPage: OnboardingPage) {
            val context = binding.getRoot().context
            val res = context.resources
            titleTv.text = res.getString(onBoardingPage.titleResource)
            subTitleTv.text = res.getString(onBoardingPage.subTitleResource)
            descTV.text = res.getString(onBoardingPage.descriptionResource)
            img.setImageResource(onBoardingPage.logoResource)
        }
    }
}
