package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.FragmentProgressBarBinding
import com.drdisagree.iconify.ui.adapters.ProgressBarAdapter
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.ProgressBarModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader

class ProgressBar : BaseFragment() {

    private lateinit var binding: FragmentProgressBarBinding
    private var loadingDialog: LoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProgressBarBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_progress_bar
        )

        // Loading dialog while enabling or disabling pack
        loadingDialog = LoadingDialog(requireContext())

        // RecyclerView
        binding.progressbarContainer.setLayoutManager(LinearLayoutManager(requireContext()))
        binding.progressbarContainer.setAdapter(initProgressBarItems())
        binding.progressbarContainer.setHasFixedSize(true)

        return view
    }

    private fun initProgressBarItems(): ProgressBarAdapter {
        val pgbList = ArrayList<ProgressBarModel>().apply {
            add(
                ProgressBarModel(
                    "Default",
                    R.drawable.preview_seekbar_default
                )
            )
            add(
                ProgressBarModel(
                    "Divided",
                    R.drawable.preview_seekbar_divided
                )
            )
            add(
                ProgressBarModel(
                    "Gradient Thumb",
                    R.drawable.preview_seekbar_gradient_thumb
                )
            )
            add(
                ProgressBarModel(
                    "Minimal Thumb",
                    R.drawable.preview_seekbar_minimal_thumb
                )
            )
            add(
                ProgressBarModel(
                    "Blocky Thumb",
                    R.drawable.preview_seekbar_blocky_thumb
                )
            )
            add(
                ProgressBarModel(
                    "Outline Thumb",
                    R.drawable.preview_seekbar_outline_thumb
                )
            )
            add(
                ProgressBarModel(
                    "Oldschool Thumb",
                    R.drawable.preview_seekbar_oldschool_thumb
                )
            )
            add(
                ProgressBarModel(
                    "No Thumb",
                    R.drawable.preview_seekbar_no_thumb
                )
            )
            add(
                ProgressBarModel(
                    "Thin Track",
                    R.drawable.preview_seekbar_thin_track
                )
            )
            add(
                ProgressBarModel(
                    "Inline",
                    R.drawable.preview_seekbar_inline
                )
            )
            add(
                ProgressBarModel(
                    "Lighty",
                    R.drawable.preview_seekbar_lighty
                )
            )
        }

        return ProgressBarAdapter(
            requireContext(),
            pgbList,
            loadingDialog!!
        )
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()

        super.onDestroy()
    }
}