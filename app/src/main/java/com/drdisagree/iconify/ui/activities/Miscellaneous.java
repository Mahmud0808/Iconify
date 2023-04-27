package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;

import android.os.Bundle;
import android.os.Handler;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityMiscellaneousBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.OverlayUtil;

public class Miscellaneous extends BaseActivity {

    ActivityMiscellaneousBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMiscellaneousBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_miscellaneous);

        binding.enableNotchBarKiller.setChecked(Prefs.getBoolean("IconifyComponentNBK.overlay", false));
        binding.enableNotchBarKiller.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked)
                    OverlayUtil.enableOverlay("IconifyComponentNBK.overlay");
                else
                    OverlayUtil.disableOverlay("IconifyComponentNBK.overlay");
            }, SWITCH_ANIMATION_DELAY);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}