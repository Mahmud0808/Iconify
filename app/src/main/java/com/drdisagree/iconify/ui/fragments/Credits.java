package com.drdisagree.iconify.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
        credits_list.add(new InfoModel(requireContext(), "Icons8.com", Iconify.getAppContextLocale().getResources().getString(R.string.info_icons8_desc), "https://icons8.com/", R.drawable.ic_link));
        credits_list.add(new InfoModel(requireContext(), "iconsax.io", Iconify.getAppContextLocale().getResources().getString(R.string.info_iconsax_desc), "http://iconsax.io/", R.drawable.ic_link));
        credits_list.add(new InfoModel(requireContext(), "Siavash", Iconify.getAppContextLocale().getResources().getString(R.string.info_xposed_desc), "https://t.me/siavash7999", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Jai", Iconify.getAppContextLocale().getResources().getString(R.string.info_shell_desc), "https://t.me/Jai_08", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "1perialf", Iconify.getAppContextLocale().getResources().getString(R.string.info_rro_desc), "https://t.me/Rodolphe06", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "modestCat", Iconify.getAppContextLocale().getResources().getString(R.string.info_rro_desc), "https://t.me/ModestCat03", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Sanely Insane", Iconify.getAppContextLocale().getResources().getString(R.string.info_tester_desc), "https://t.me/sanely_insane", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "Jaguar", Iconify.getAppContextLocale().getResources().getString(R.string.info_tester_desc), "https://t.me/Jaguar0066", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "hani & TeamFiles", Iconify.getAppContextLocale().getResources().getString(R.string.info_betterqs_desc), "https://github.com/itsHanibee", R.drawable.ic_user));
        credits_list.add(new InfoModel(requireContext(), "AAGaming", Iconify.getAppContextLocale().getResources().getString(R.string.info_binaries_desc), "https://aagaming.me", R.drawable.ic_user));

        return new InfoAdapter(requireContext(), credits_list);
    }

    private InfoAdapter initContributorsList() {
        ArrayList<InfoModel> contributors_list = new ArrayList<>();

        contributors_list.add(new InfoModel(getResources().getString(R.string.section_title_contributors)));
        contributors_list.add(new InfoModel(requireContext(), "Azure-Helper", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/Azure-Helper", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "HiFIi", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/HiFIi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "IzzySoft", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/IzzySoft", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Blays", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/B1ays", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Libra420T", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc_2), "https://t.me/Libra420T", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "mohamedamrnady", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/mohamedamrnady", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "H1mJT", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/H1mJT", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "KaeruShi", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/KaeruShi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Displax", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/Displax", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "DHD2280", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/DHD2280", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "armv7a", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/armv7a", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "Jvr", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://github.com/Jvr2022", R.drawable.ic_user));
        contributors_list.add(new InfoModel(requireContext(), "ꃅꈤꀸꋪꀘ", Iconify.getAppContextLocale().getResources().getString(R.string.info_contributor_desc), "https://t.me/therealhndrk", R.drawable.ic_user));

        return new InfoAdapter(requireContext(), contributors_list);
    }

    private InfoAdapter initTranslatorsList() {
        ArrayList<InfoModel> translators_list = new ArrayList<>();

        translators_list.add(new InfoModel(getResources().getString(R.string.section_title_translators)));
        translators_list.add(new InfoModel(requireContext(), "MRX7014", Iconify.getAppContextLocale().getResources().getString(R.string.ar_translation), "https://github.com/mrx7014", R.drawable.flag_sa));
        translators_list.add(new InfoModel(requireContext(), "Mohamed Bahaa", Iconify.getAppContextLocale().getResources().getString(R.string.ar_translation), "https://github.com/muhammadbahaa2001", R.drawable.flag_sa));
        translators_list.add(new InfoModel(requireContext(), "MXC48", Iconify.getAppContextLocale().getResources().getString(R.string.fr_translation), "https://github.com/MXC48", R.drawable.flag_fr));
        translators_list.add(new InfoModel(requireContext(), "KaeruShi", Iconify.getAppContextLocale().getResources().getString(R.string.id_translation), "https://github.com/KaeruShi", R.drawable.flag_id));
        translators_list.add(new InfoModel(requireContext(), "Danilo Belmonte", Iconify.getAppContextLocale().getResources().getString(R.string.it_translation), "https://crowdin.com/profile/steve.burnside", R.drawable.flag_it));
        translators_list.add(new InfoModel(requireContext(), "Faceless1999", Iconify.getAppContextLocale().getResources().getString(R.string.fa_translation), "https://github.com/Faceless1999", R.drawable.flag_ir));
        translators_list.add(new InfoModel(requireContext(), "igor", Iconify.getAppContextLocale().getResources().getString(R.string.pt_br_translation), "https://github.com/igormiguell", R.drawable.flag_br));
        translators_list.add(new InfoModel(requireContext(), "ElTifo", Iconify.getAppContextLocale().getResources().getString(R.string.pt_translation), "https://github.com/ElTifo", R.drawable.flag_pt));
        translators_list.add(new InfoModel(requireContext(), "Blays", Iconify.getAppContextLocale().getResources().getString(R.string.ru_translation), "https://github.com/B1ays", R.drawable.flag_ru));
        translators_list.add(new InfoModel(requireContext(), "Cccc_", Iconify.getAppContextLocale().getResources().getString(R.string.zh_cn_translation), "https://github.com/Cccc-owo", R.drawable.flag_cn));
        translators_list.add(new InfoModel(requireContext(), "Zhang chunyu", Iconify.getAppContextLocale().getResources().getString(R.string.zh_tw_translation), "https://crowdin.com/profile/gyah4", R.drawable.flag_cn));
        translators_list.add(new InfoModel(requireContext(), "luckkmaxx", Iconify.getAppContextLocale().getResources().getString(R.string.es_translation), "https://github.com/luckkmaxx", R.drawable.flag_es));
        translators_list.add(new InfoModel(requireContext(), "Serhat Demir", Iconify.getAppContextLocale().getResources().getString(R.string.tr_translation), "https://github.com/serhat-demir", R.drawable.flag_tr));
        translators_list.add(new InfoModel(requireContext(), "Emre", Iconify.getAppContextLocale().getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/khapnols", R.drawable.flag_tr));
        translators_list.add(new InfoModel(requireContext(), "WINZORT", Iconify.getAppContextLocale().getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/linuxthegoat", R.drawable.flag_tr));
        translators_list.add(new InfoModel(requireContext(), "Đức Trọng", Iconify.getAppContextLocale().getResources().getString(R.string.vi_translation), "https://t.me/viettel1211", R.drawable.flag_vn));
        translators_list.add(new InfoModel(requireContext(), "SK00RUPA", Iconify.getAppContextLocale().getResources().getString(R.string.pl_translation), "https://github.com/SK00RUPA", R.drawable.flag_pl));

        return new InfoAdapter(requireContext(), translators_list);
    }
}
