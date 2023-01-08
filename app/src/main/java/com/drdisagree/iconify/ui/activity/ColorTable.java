package com.drdisagree.iconify.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class ColorTable extends AppCompatActivity {

    View[] colorTableAccent1, colorTableAccent2, colorTableAccent3, colorTableNeutral1, colorTableNeutral2;
    View a1_0, a1_10, a1_50, a1_100, a1_200, a1_300, a1_400, a1_500, a1_600, a1_700, a1_800, a1_900, a1_1000;
    View a2_0, a2_10, a2_50, a2_100, a2_200, a2_300, a2_400, a2_500, a2_600, a2_700, a2_800, a2_900, a2_1000;
    View a3_0, a3_10, a3_50, a3_100, a3_200, a3_300, a3_400, a3_500, a3_600, a3_700, a3_800, a3_900, a3_1000;
    View n1_0, n1_10, n1_50, n1_100, n1_200, n1_300, n1_400, n1_500, n1_600, n1_700, n1_800, n1_900, n1_1000;
    View n2_0, n2_10, n2_50, n2_100, n2_200, n2_300, n2_400, n2_500, n2_600, n2_700, n2_800, n2_900, n2_1000;

    private int[] systemAccent1, systemAccent2, systemAccent3, systemNeutral1, systemNeutral2;
    private int[] colorTableAccent1Id, colorTableAccent2Id, colorTableAccent3Id, colorTableNeutral1Id, colorTableNeutral2Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_table);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_color_table));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSystemColors();

        colorTableAccent1 = new View[]{a1_0, a1_10, a1_50, a1_100, a1_200, a1_300, a1_400, a1_500, a1_600, a1_700, a1_800, a1_900, a1_1000};
        colorTableAccent2 = new View[]{a2_0, a2_10, a2_50, a2_100, a2_200, a2_300, a2_400, a2_500, a2_600, a2_700, a2_800, a2_900, a2_1000};
        colorTableAccent3 = new View[]{a3_0, a3_10, a3_50, a3_100, a3_200, a3_300, a3_400, a3_500, a3_600, a3_700, a3_800, a3_900, a3_1000};
        colorTableNeutral1 = new View[]{n1_0, n1_10, n1_50, n1_100, n1_200, n1_300, n1_400, n1_500, n1_600, n1_700, n1_800, n1_900, n1_1000};
        colorTableNeutral2 = new View[]{n2_0, n2_10, n2_50, n2_100, n2_200, n2_300, n2_400, n2_500, n2_600, n2_700, n2_800, n2_900, n2_1000};

        colorTableAccent1Id = new int[]{R.id.system_accent1_0, R.id.system_accent1_10, R.id.system_accent1_50, R.id.system_accent1_100, R.id.system_accent1_200, R.id.system_accent1_300, R.id.system_accent1_400, R.id.system_accent1_500, R.id.system_accent1_600, R.id.system_accent1_700, R.id.system_accent1_800, R.id.system_accent1_900, R.id.system_accent1_1000};
        colorTableAccent2Id = new int[]{R.id.system_accent2_0, R.id.system_accent2_10, R.id.system_accent2_50, R.id.system_accent2_100, R.id.system_accent2_200, R.id.system_accent2_300, R.id.system_accent2_400, R.id.system_accent2_500, R.id.system_accent2_600, R.id.system_accent2_700, R.id.system_accent2_800, R.id.system_accent2_900, R.id.system_accent2_1000};
        colorTableAccent3Id = new int[]{R.id.system_accent3_0, R.id.system_accent3_10, R.id.system_accent3_50, R.id.system_accent3_100, R.id.system_accent3_200, R.id.system_accent3_300, R.id.system_accent3_400, R.id.system_accent3_500, R.id.system_accent3_600, R.id.system_accent3_700, R.id.system_accent3_800, R.id.system_accent3_900, R.id.system_accent3_1000};
        colorTableNeutral1Id = new int[]{R.id.system_neutral1_0, R.id.system_neutral1_10, R.id.system_neutral1_50, R.id.system_neutral1_100, R.id.system_neutral1_200, R.id.system_neutral1_300, R.id.system_neutral1_400, R.id.system_neutral1_500, R.id.system_neutral1_600, R.id.system_neutral1_700, R.id.system_neutral1_800, R.id.system_neutral1_900, R.id.system_neutral1_1000};
        colorTableNeutral2Id = new int[]{R.id.system_neutral2_0, R.id.system_neutral2_10, R.id.system_neutral2_50, R.id.system_neutral2_100, R.id.system_neutral2_200, R.id.system_neutral2_300, R.id.system_neutral2_400, R.id.system_neutral2_500, R.id.system_neutral2_600, R.id.system_neutral2_700, R.id.system_neutral2_800, R.id.system_neutral2_900, R.id.system_neutral2_1000};

        assignIdToView();
        assignColorToPalette();
    }

    private void getSystemColors() {
        systemAccent1 = new int[]{getResources().getColor(com.google.android.material.R.color.material_dynamic_primary100),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary99),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary95),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary90),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary80),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary70),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary60),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary50),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary40),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary30),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary10),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0)};

        systemAccent2 = new int[]{getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary100),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary99),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary95),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary90),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary80),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary70),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary60),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary50),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary40),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary30),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary20),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary10),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary0)};

        systemAccent3 = new int[]{getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary100),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary99),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary95),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary90),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary80),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary70),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary60),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary50),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary40),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary30),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary20),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary10),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary0)};

        systemNeutral1 = new int[]{getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral100),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral99),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral95),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral90),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral70),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral60),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral50),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral40),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral20),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral10),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral0)};

        systemNeutral2 = new int[]{getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant100),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant99),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant90),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant80),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant70),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant60),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant50),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant40),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant30),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant20),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant10),
                getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant0)};
    }

    private void assignIdToView() {
        for (int i = 0; i < 13; i++) {
            colorTableAccent1[i] = findViewById(colorTableAccent1Id[i]);
            colorTableAccent2[i] = findViewById(colorTableAccent2Id[i]);
            colorTableAccent3[i] = findViewById(colorTableAccent3Id[i]);
            colorTableNeutral1[i] = findViewById(colorTableNeutral1Id[i]);
            colorTableNeutral2[i] = findViewById(colorTableNeutral2Id[i]);
        }
    }

    private void assignColorToPalette() {
        for (int i = 0; i < 13; i++) {
            colorTableAccent1[i].setBackgroundColor(systemAccent1[i]);
            colorTableAccent2[i].setBackgroundColor(systemAccent2[i]);
            colorTableAccent3[i].setBackgroundColor(systemAccent3[i]);
            colorTableNeutral1[i].setBackgroundColor(systemNeutral1[i]);
            colorTableNeutral2[i].setBackgroundColor(systemNeutral2[i]);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}