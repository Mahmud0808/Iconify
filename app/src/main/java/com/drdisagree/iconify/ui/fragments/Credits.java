package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.FragmentCreditsBinding;
import com.drdisagree.iconify.ui.adapters.InfoAdapter;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.models.InfoModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;

import java.util.ArrayList;

public class Credits extends BaseFragment {

    private FragmentCreditsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreditsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.section_title_credits);

        // RecyclerView
        binding.infoContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        ConcatAdapter adapter = new ConcatAdapter(initCreditsList(), initContributorsList(), initTranslatorsList());
        binding.infoContainer.setAdapter(adapter);
        binding.infoContainer.setHasFixedSize(true);

        return view;
    }

    private InfoAdapter initCreditsList() {
        ArrayList<InfoModel> credits_list = new ArrayList<>();

        credits_list.add(new InfoModel(getResources().getString(R.string.section_title_thanks)));
        credits_list.add(new InfoModel(requireContext(), "Icons8.com", getResources().getString(R.string.info_icons8_desc), "https://icons8.com/", R.drawable.ic_link));
        credits_list.add(new InfoModel(requireContext(), "iconsax.io", getResources().getString(R.string.info_iconsax_desc), "http://iconsax.io/", R.drawable.ic_link));
        credits_list.add(new InfoModel(requireContext(), "Siavash", getResources().getString(R.string.info_xposed_desc), "https://t.me/siavash7999", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Jai", getResources().getString(R.string.info_shell_desc), "https://t.me/Jai_08", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "1perialf", getResources().getString(R.string.info_rro_desc), "https://t.me/Rodolphe06", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "modestCat", getResources().getString(R.string.info_rro_desc), "https://t.me/ModestCat03", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Sanely Insane", getResources().getString(R.string.info_tester_desc), "https://t.me/sanely_insane", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Jaguar", getResources().getString(R.string.info_tester_desc), "https://t.me/Jaguar0066", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "hani & TeamFiles", getResources().getString(R.string.info_betterqs_desc), "https://github.com/itsHanibee", R.drawable.ic_user));

        return new InfoAdapter(requireContext(), credits_list);
    }

    private InfoAdapter initContributorsList() {
        ArrayList<InfoModel> contributors_list = new ArrayList<>();

        contributors_list.add(new InfoModel(getResources().getString(R.string.section_title_contributors)));
        contributors_list.add(new InfoModel(requireContext(), "Azure-Helper", getResources().getString(R.string.info_contributor_desc), "https://github.com/Azure-Helper", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "HiFIi", getResources().getString(R.string.info_contributor_desc), "https://github.com/HiFIi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "IzzySoft", getResources().getString(R.string.info_contributor_desc), "https://github.com/IzzySoft", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Blays", getResources().getString(R.string.info_contributor_desc), "https://github.com/B1ays", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Libra420T", getResources().getString(R.string.info_contributor_desc_2), "https://t.me/Libra420T", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "mohamedamrnady", getResources().getString(R.string.info_contributor_desc), "https://github.com/mohamedamrnady", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "H1mJT", getResources().getString(R.string.info_contributor_desc), "https://github.com/H1mJT", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "KaeruShi", getResources().getString(R.string.info_contributor_desc), "https://github.com/KaeruShi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Displax", getResources().getString(R.string.info_contributor_desc), "https://github.com/Displax", R.drawable.ic_user));

        return new InfoAdapter(requireContext(), contributors_list);
    }

    private InfoAdapter initTranslatorsList() {
        ArrayList<InfoModel> translators_list = new ArrayList<>();

        translators_list.add(new InfoModel(getResources().getString(R.string.section_title_translators)));
        translators_list.add(new InfoModel(requireContext(), "MRX7014", getResources().getString(R.string.ar_translation), "https://github.com/mrx7014", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Mohamed Bahaa", getResources().getString(R.string.ar_translation), "https://github.com/muhammadbahaa2001", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "MXC48", getResources().getString(R.string.fr_translation), "https://github.com/MXC48", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "KaeruShi", getResources().getString(R.string.id_translation), "https://github.com/KaeruShi", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Faceless1999", getResources().getString(R.string.fa_translation), "https://github.com/Faceless1999", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "ElTifo", getResources().getString(R.string.pt_translation), "https://github.com/ElTifo", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Blays", getResources().getString(R.string.ru_translation), "https://github.com/B1ays", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Cccc_", getResources().getString(R.string.zh_cn_translation), "https://github.com/Cccc-owo", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "luckkmaxx", getResources().getString(R.string.es_translation), "https://github.com/luckkmaxx", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Serhat Demir", getResources().getString(R.string.tr_translation), "https://github.com/serhat-demir", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Emre", getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/khapnols", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "WINZORT", getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/linuxthegoat", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Đức Trọng", getResources().getString(R.string.vi_translation), "https://t.me/viettel1211", R.drawable.ic_user));
        translators_list.add(new InfoModel(requireContext(), "Jakub Skorłutowski", getResources().getString(R.string.pl_translation), "https://github.com/SK00RUPA", R.drawable.ic_user));

        return new InfoAdapter(requireContext(), translators_list);
    }
}