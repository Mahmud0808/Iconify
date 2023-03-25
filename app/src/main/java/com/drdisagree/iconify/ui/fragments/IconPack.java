package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.References.FRAGMENT_COLOREDBATTERY;
import static com.drdisagree.iconify.common.References.FRAGMENT_MEDIAICONS;
import static com.drdisagree.iconify.common.References.FRAGMENT_SETTINGSICONS;

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
import com.drdisagree.iconify.ui.adapters.IconPackAdapter;
import com.drdisagree.iconify.ui.adapters.MenuFragmentAdapter;
import com.drdisagree.iconify.ui.adapters.ViewAdapter;
import com.drdisagree.iconify.ui.models.IconPackModel;
import com.drdisagree.iconify.ui.models.MenuFragmentModel;
import com.drdisagree.iconify.ui.utils.FragmentHelper;
import com.drdisagree.iconify.ui.views.LoadingDialog;

import java.util.ArrayList;

public class IconPack extends Fragment {

    LoadingDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon_pack, container, false);

        // Header
        FragmentHelper.initHeader((AppCompatActivity) requireActivity(), view, R.string.activity_title_icon_pack, getParentFragmentManager());

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireActivity());

        // RecyclerView
        RecyclerView listView = view.findViewById(R.id.icon_pack_container);
        listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        ConcatAdapter adapter = new ConcatAdapter(initActivityItems(), new ViewAdapter(requireActivity(), R.layout.view_divider), initIconPackItems());
        listView.setAdapter(adapter);
        listView.setHasFixedSize(true);

        return view;
    }

    private MenuFragmentAdapter initActivityItems() {
        ArrayList<MenuFragmentModel> iconpack_activity_list = new ArrayList<>();

        iconpack_activity_list.add(new MenuFragmentModel(new ColoredBattery(), FRAGMENT_COLOREDBATTERY, getResources().getString(R.string.activity_title_colored_battery), getResources().getString(R.string.activity_desc_colored_battery), R.drawable.ic_colored_battery));
        iconpack_activity_list.add(new MenuFragmentModel(new MediaIcons(), FRAGMENT_MEDIAICONS, getResources().getString(R.string.activity_title_media_icons), getResources().getString(R.string.activity_desc_media_icons), R.drawable.ic_media_player_icon));
        iconpack_activity_list.add(new MenuFragmentModel(new SettingsIcons(), FRAGMENT_SETTINGSICONS, getResources().getString(R.string.activity_title_settings_icons), getResources().getString(R.string.activity_desc_settings_icons), R.drawable.ic_settings_icon_pack));

        return new MenuFragmentAdapter(requireActivity(), iconpack_activity_list, getParentFragmentManager());
    }

    private IconPackAdapter initIconPackItems() {
        ArrayList<IconPackModel> iconpack_list = new ArrayList<>();

        iconpack_list.add(new IconPackModel("Aurora", "Dual tone linear icon pack", R.drawable.preview_aurora_wifi, R.drawable.preview_aurora_signal, R.drawable.preview_aurora_airplane, R.drawable.preview_aurora_location));
        iconpack_list.add(new IconPackModel("Gradicon", "Gradient shaded filled icon pack", R.drawable.preview_gradicon_wifi, R.drawable.preview_gradicon_signal, R.drawable.preview_gradicon_airplane, R.drawable.preview_gradicon_location));
        iconpack_list.add(new IconPackModel("Lorn", "Thick linear icon pack", R.drawable.preview_lorn_wifi, R.drawable.preview_lorn_signal, R.drawable.preview_lorn_airplane, R.drawable.preview_lorn_location));
        iconpack_list.add(new IconPackModel("Plumpy", "Dual tone filled icon pack", R.drawable.preview_plumpy_wifi, R.drawable.preview_plumpy_signal, R.drawable.preview_plumpy_airplane, R.drawable.preview_plumpy_location));
        iconpack_list.add(new IconPackModel("Acherus", "Acherus sub icon pack", R.drawable.preview_acherus_wifi, R.drawable.preview_acherus_signal, R.drawable.preview_acherus_airplane, R.drawable.preview_acherus_location));
        iconpack_list.add(new IconPackModel("Circular", "Thin line icon pack", R.drawable.preview_circular_wifi, R.drawable.preview_circular_signal, R.drawable.preview_circular_airplane, R.drawable.preview_circular_location));
        iconpack_list.add(new IconPackModel("Filled", "Dual tone filled icon pack", R.drawable.preview_filled_wifi, R.drawable.preview_filled_signal, R.drawable.preview_filled_airplane, R.drawable.preview_filled_location));
        iconpack_list.add(new IconPackModel("Kai", "Thin line icon pack", R.drawable.preview_kai_wifi, R.drawable.preview_kai_signal, R.drawable.preview_kai_airplane, R.drawable.preview_kai_location));
        iconpack_list.add(new IconPackModel("OOS", "Oxygen OS icon pack", R.drawable.preview_oos_wifi, R.drawable.preview_oos_signal, R.drawable.preview_oos_airplane, R.drawable.preview_oos_location));
        iconpack_list.add(new IconPackModel("Outline", "Thin outline icon pack", R.drawable.preview_outline_wifi, R.drawable.preview_outline_signal, R.drawable.preview_outline_airplane, R.drawable.preview_outline_location));
        iconpack_list.add(new IconPackModel("PUI", "Thick dualtone icon pack", R.drawable.preview_pui_wifi, R.drawable.preview_pui_signal, R.drawable.preview_pui_airplane, R.drawable.preview_pui_location));
        iconpack_list.add(new IconPackModel("Rounded", "Rounded corner icon pack", R.drawable.preview_rounded_wifi, R.drawable.preview_rounded_signal, R.drawable.preview_rounded_airplane, R.drawable.preview_rounded_location));
        iconpack_list.add(new IconPackModel("Sam", "Filled icon pack", R.drawable.preview_sam_wifi, R.drawable.preview_sam_signal, R.drawable.preview_sam_airplane, R.drawable.preview_sam_location));
        iconpack_list.add(new IconPackModel("Victor", "Edgy icon pack", R.drawable.preview_victor_wifi, R.drawable.preview_victor_signal, R.drawable.preview_victor_airplane, R.drawable.preview_victor_location));

        return new IconPackAdapter(requireActivity(), iconpack_list, loadingDialog);
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}