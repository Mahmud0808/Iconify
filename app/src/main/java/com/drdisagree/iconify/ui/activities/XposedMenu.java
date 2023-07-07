package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.References.FRAGMENT_XPOSEDMENU;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.databinding.ActivityXposedMenuBinding;
import com.drdisagree.iconify.ui.utils.FragmentHelper;

import java.util.Objects;

public class XposedMenu extends BaseActivity {

    ActivityXposedMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityXposedMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            replaceFragment(new com.drdisagree.iconify.ui.fragments.XposedMenu(), FRAGMENT_XPOSEDMENU);
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        fragmentTransaction.replace(R.id.main_fragment, fragment, tag);
        if (Objects.equals(tag, FRAGMENT_XPOSEDMENU))
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        else {
            fragmentManager.popBackStack(null, 0);
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (Objects.equals(FragmentHelper.getTopFragment(getSupportFragmentManager()), FRAGMENT_XPOSEDMENU)) {
            finish();
            System.exit(0);
        }
        super.onBackPressed();
    }
}