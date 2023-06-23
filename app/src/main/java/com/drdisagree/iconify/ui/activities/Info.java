package com.drdisagree.iconify.ui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.adapters.InfoAdapter;
import com.drdisagree.iconify.ui.models.InfoModel;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

import java.util.ArrayList;

public class Info extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_info);

        // RecyclerView
        RecyclerView container = findViewById(R.id.info_container);
        container.setLayoutManager(new LinearLayoutManager(this));
        ConcatAdapter adapter = new ConcatAdapter(initAppInfo(), initAppInfoExtended(), initCreditsList(), initContributorsList(), initTranslatorsList());
        container.setAdapter(adapter);
        container.setHasFixedSize(true);
    }

    private InfoAdapter initAppInfo() {
        ArrayList<InfoModel> app_info = new ArrayList<>();

        app_info.add(new InfoModel(R.layout.view_list_info_app));

        return new InfoAdapter(this, app_info);
    }

    private InfoAdapter initAppInfoExtended() {
        ArrayList<InfoModel> app_info = new ArrayList<>();

        app_info.add(new InfoModel(""));
        app_info.add(new InfoModel(getResources().getString(R.string.info_version_title), BuildConfig.VERSION_NAME, v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.iconify_clipboard_label_version), getResources().getString(R.string.app_name) + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_name) + ' ' + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_code) + ' ' + BuildConfig.VERSION_CODE);
            clipboard.setPrimaryClip(clip);
            Toast toast = Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_copied), Toast.LENGTH_SHORT);
            toast.show();
        }, R.drawable.ic_info));
        app_info.add(new InfoModel(this, getResources().getString(R.string.info_github_title), getResources().getString(R.string.info_github_desc), "https://github.com/Mahmud0808/Iconify", R.drawable.ic_github));
        app_info.add(new InfoModel(this, getResources().getString(R.string.info_telegram_title), getResources().getString(R.string.info_telegram_desc), "https://t.me/IconifyOfficial", R.drawable.ic_telegram));

        return new InfoAdapter(this, app_info);
    }

    private InfoAdapter initCreditsList() {
        ArrayList<InfoModel> credits_list = new ArrayList<>();

        credits_list.add(new InfoModel(getResources().getString(R.string.section_title_credits)));
        credits_list.add(new InfoModel(this, "Icons8.com", getResources().getString(R.string.info_icons8_desc), "https://icons8.com/", R.drawable.ic_link));
        credits_list.add(new InfoModel(this, "iconsax.io", getResources().getString(R.string.info_iconsax_desc), "http://iconsax.io/", R.drawable.ic_link));
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

        return new InfoAdapter(this, contributors_list);
    }

    private InfoAdapter initTranslatorsList() {
        ArrayList<InfoModel> translators_list = new ArrayList<>();

        translators_list.add(new InfoModel(getResources().getString(R.string.section_title_translators)));
        translators_list.add(new InfoModel(this, "MXC48", getResources().getString(R.string.fr_translation), "https://github.com/MXC48", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "KaeruShi", getResources().getString(R.string.id_translation), "https://github.com/KaeruShi", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Faceless1999", getResources().getString(R.string.fa_translation), "https://github.com/Faceless1999", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "ElTifo", getResources().getString(R.string.pt_translation), "https://github.com/ElTifo", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Blays", getResources().getString(R.string.ru_translation), "https://github.com/B1ays", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Cccc_", getResources().getString(R.string.zh_cn_translation), "https://github.com/Cccc-owo", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Serhat Demir", getResources().getString(R.string.tr_translation), "https://github.com/serhat-demir", R.drawable.ic_user));
        translators_list.add(new InfoModel(this, "Đức Trọng", getResources().getString(R.string.vi_translation), "https://t.me/viettel1211", R.drawable.ic_user));

        return new InfoAdapter(this, translators_list);
    }
}