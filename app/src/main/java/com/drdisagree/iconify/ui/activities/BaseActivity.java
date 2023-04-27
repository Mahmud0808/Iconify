package com.drdisagree.iconify.ui.activities;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.utils.helpers.LocaleHelper;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }
}
