package com.drdisagree.iconify.ui.activities;

import android.os.Bundle;

import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.ActivityCreditsBinding;
import com.drdisagree.iconify.ui.adapters.InfoAdapter;
import com.drdisagree.iconify.ui.models.InfoModel;
import com.drdisagree.iconify.ui.utils.ViewHelper;

import java.util.ArrayList;

public class Credits extends BaseActivity {

    private ActivityCreditsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreditsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewHelper.setHeader(this, binding.header.toolbar, R.string.section_title_credits);

        // RecyclerView
        binding.infoContainer.setLayoutManager(new LinearLayoutManager(this));
        ConcatAdapter adapter = new ConcatAdapter(initCreditsList(), initContributorsList(), initTranslatorsList());
        binding.infoContainer.setAdapter(adapter);
        binding.infoContainer.setHasFixedSize(true);
    }

    private InfoAdapter initCreditsList() {
        ArrayList<InfoModel> credits_list = new ArrayList<>();

        credits_list.add(new InfoModel(getResources().getString(R.string.section_title_thanks)));
        credits_list.add(new InfoModel(this, "Icons8.com", getResources().getString(R.string.info_icons8_desc), "https://icons8.com/", R.drawable.ic_link));
        credits_list.add(new InfoModel(this, "iconsax.io", getResources().getString(R.string.info_iconsax_desc), "http://iconsax.io/", R.drawable.ic_link));
        credits_list.add(new InfoModel(this, "Siavash", getResources().getString(R.string.info_xposed_desc), "https://t.me/siavash7999", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "Jai", getResources().getString(R.string.info_shell_desc), "https://t.me/Jai_08", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "1perialf", getResources().getString(R.string.info_rro_desc), "https://t.me/Rodolphe06", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "modestCat", getResources().getString(R.string.info_rro_desc), "https://t.me/ModestCat03", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "Sanely Insane", getResources().getString(R.string.info_tester_desc), "https://t.me/sanely_insane", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "Jaguar", getResources().getString(R.string.info_tester_desc), "https://t.me/Jaguar0066", R.drawable.ic_user));
        credits_list.add(new InfoModel(this, "hani & TeamFiles", getResources().getString(R.string.info_betterqs_desc), "https://github.com/itsHanibee", R.drawable.ic_user));

        return new InfoAdapter(this, credits_list);
    }

    private InfoAdapter initContributorsList() {
        ArrayList<InfoModel> contributors_list = new ArrayList<>();

        contributors_list.add(new InfoModel(getResources().getString(R.string.section_title_contributors)));
        contributors_list.add(new InfoModel(this, "Azure-Helper", getResources().getString(R.string.info_contributor_desc), "https://github.com/Azure-Helper", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "HiFIi", getResources().getString(R.string.info_contributor_desc), "https://github.com/HiFIi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "IzzySoft", getResources().getString(R.string.info_contributor_desc), "https://github.com/IzzySoft", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "Blays", getResources().getString(R.string.info_contributor_desc), "https://github.com/B1ays", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "Libra420T", getResources().getString(R.string.info_contributor_desc_2), "https://t.me/Libra420T", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "mohamedamrnady", getResources().getString(R.string.info_contributor_desc), "https://github.com/mohamedamrnady", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "H1mJT", getResources().getString(R.string.info_contributor_desc), "https://github.com/H1mJT", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "KaeruShi", getResources().getString(R.string.info_contributor_desc), "https://github.com/KaeruShi", R.drawable.ic_user));
        contributors_list.add(new InfoModel(this, "Displax", getResources().getString(R.string.info_contributor_desc), "https://github.com/Displax", R.drawable.ic_user));

        return new InfoAdapter(this, contributors_list);
    }

    private InfoAdapter initTranslatorsList() {
        ArrayList<InfoModel> translators_list = new ArrayList<>();

        translators_list.add(new InfoModel(getResources().getString(R.string.section_title_translators)));
        translators_list.add(new InfoModel(this, "MRX7014", getResources().getString(R.string.ar_translation), "https://github.com/mrx7014", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Mohamed Bahaa", getResources().getString(R.string.ar_translation), "https://github.com/muhammadbahaa2001", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "MXC48", getResources().getString(R.string.fr_translation), "https://github.com/MXC48", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "KaeruShi", getResources().getString(R.string.id_translation), "https://github.com/KaeruShi", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Faceless1999", getResources().getString(R.string.fa_translation), "https://github.com/Faceless1999", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "ElTifo", getResources().getString(R.string.pt_translation), "https://github.com/ElTifo", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Blays", getResources().getString(R.string.ru_translation), "https://github.com/B1ays", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Cccc_", getResources().getString(R.string.zh_cn_translation), "https://github.com/Cccc-owo", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "luckkmaxx", getResources().getString(R.string.es_translation), "https://github.com/luckkmaxx", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Serhat Demir", getResources().getString(R.string.tr_translation), "https://github.com/serhat-demir", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Emre", getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/khapnols", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "WINZORT", getResources().getString(R.string.tr_translation), "https://crowdin.com/profile/linuxthegoat", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Đức Trọng", getResources().getString(R.string.vi_translation), "https://t.me/viettel1211", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Jakub Skorłutowski", getResources().getString(R.string.pl_translation), "https://github.com/SK00RUPA", R.drawable.ic_user));

        return new InfoAdapter(this, translators_list);
    }
}