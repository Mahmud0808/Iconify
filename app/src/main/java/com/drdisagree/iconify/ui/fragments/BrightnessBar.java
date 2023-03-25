package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.References.FRAGMENT_BRIGHTNESSBARPIXEL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.BrightnessBarAdapter;
import com.drdisagree.iconify.ui.adapters.MenuFragmentAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.BrightnessBarModel;
import com.drdisagree.iconify.ui.models.MenuFragmentModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class BrightnessBar extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_brightness_bar, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_brightness_bar, getParentFragmentManager());

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        RecyclerView listView = view.findViewById(R.id.brightness_bar_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        ConcatAdapter adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(requireActivity(), R.layout.view_divider), initBrightnessBarItems());
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);

        return view;
    }

    private MenuFragmentAdapter initActivityItems() {
        ArrayList<MenuFragmentModel> brightnessbar_activity_list = new ArrayList<>();

        brightnessbar_activity_list.add(new MenuFragmentModel(new BrightnessBarPixel(), FRAGMENT_BRIGHTNESSBARPIXEL, getResources().getString(R.string.activity_title_pixel_variant), getResources().getString(R.string.activity_desc_pixel_variant), R.drawable.ic_pixel_device));

        return new MenuFragmentAdapter(requireActivity(), brightnessbar_activity_list, getParentFragmentManager());
    }

    private BrightnessBarAdapter initBrightnessBarItems() {
        ArrayList<BrightnessBarModel> bb_list = new ArrayList<>();

        bb_list.add(new BrightnessBarModel("Rounded Clip", R.drawable.bb_roundedclip, R.drawable.auto_bb_roundedclip, false));
        bb_list.add(new BrightnessBarModel("Rounded Bar", R.drawable.bb_rounded, R.drawable.auto_bb_rounded, false));
        bb_list.add(new BrightnessBarModel("Double Layer", R.drawable.bb_double_layer, R.drawable.auto_bb_double_layer, false));
        bb_list.add(new BrightnessBarModel("Shaded Layer", R.drawable.bb_shaded_layer, R.drawable.auto_bb_shaded_layer, false));
        bb_list.add(new BrightnessBarModel("Outline", R.drawable.bb_outline, R.drawable.auto_bb_outline, true));
        bb_list.add(new BrightnessBarModel("Leafy Outline", R.drawable.bb_leafy_outline, R.drawable.auto_bb_leafy_outline, true));
        bb_list.add(new BrightnessBarModel("Neumorph", R.drawable.bb_neumorph, R.drawable.auto_bb_neumorph, false));
        bb_list.add(new BrightnessBarModel("Inline", R.drawable.bb_inline, R.drawable.auto_bb_rounded, false));
        bb_list.add(new BrightnessBarModel("Neumorph Outline", R.drawable.bb_neumorph_outline, R.drawable.auto_bb_neumorph_outline, true));
        bb_list.add(new BrightnessBarModel("Neumorph Thumb", R.drawable.bb_neumorph_thumb, R.drawable.auto_bb_neumorph_thumb, false));
        bb_list.add(new BrightnessBarModel("Blocky Thumb", R.drawable.bb_blocky_thumb, R.drawable.auto_bb_blocky_thumb, false));
        bb_list.add(new BrightnessBarModel("Comet Thumb", R.drawable.bb_comet_thumb, R.drawable.auto_bb_comet_thumb, false));
        bb_list.add(new BrightnessBarModel("Minimal Thumb", R.drawable.bb_minimal_thumb, R.drawable.auto_bb_minimal_thumb, false));
        bb_list.add(new BrightnessBarModel("Old School Thumb", R.drawable.bb_oldschool_thumb, R.drawable.auto_bb_oldschool_thumb, false));
        bb_list.add(new BrightnessBarModel("Gradient Thumb", R.drawable.bb_gradient_thumb, R.drawable.auto_bb_gradient_thumb, false));
        bb_list.add(new BrightnessBarModel("Lighty", R.drawable.bb_lighty, R.drawable.auto_bb_lighty, true));
        bb_list.add(new BrightnessBarModel("Semi Transparent", R.drawable.bb_semi_transparent, R.drawable.auto_bb_semi_transparent, false));
        bb_list.add(new BrightnessBarModel("Thin Outline", R.drawable.bb_thin_outline, R.drawable.auto_bb_thin_outline, true));
        bb_list.add(new BrightnessBarModel("Purfect", R.drawable.bb_purfect, R.drawable.auto_bb_purfect, false));

        return new BrightnessBarAdapter(requireActivity(), bb_list, loadingDialog, "BBN");
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}