package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_NORMAL;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_PIXEL;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ICON_SIZE;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_MOVE_ICON;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TEXT_SIZE;
import static com.drdisagree.iconify.common.Resources.QSNPT_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT1_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT2_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT3_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT4_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT1_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT2_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT3_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT4_overlay;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.ActivityQsIconLabelBinding;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.Objects;

public class QsIconLabel extends BaseActivity {

    private ActivityQsIconLabelBinding binding;
    private static String selectedVariant;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQsIconLabelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Header
        ViewBindingHelpers.setHeader(this, binding.header.collapsingToolbar, binding.header.toolbar, R.string.activity_title_qs_icon_label);

        // Text Size
        final int[] finalTextSize = {14};

        if (!Prefs.getString(FABRICATED_QS_TEXT_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) == 14)
                binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp " + getResources().getString(R.string.opt_default));
            else
                binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp");
            finalTextSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE));
            binding.textSize.setProgress(finalTextSize[0]);
        } else
            binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + " 14sp " + getResources().getString(R.string.opt_default));

        binding.textSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalTextSize[0] = progress;
                if (progress == 14)
                    binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "sp " + getResources().getString(R.string.opt_default));
                else
                    binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "sp");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_TEXT_SIZE, "dimen", "qs_tile_text_size", finalTextSize[0] + "sp");

                Toast.makeText(getApplicationContext(), finalTextSize[0] + "sp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Icon Size
        final int[] finalIconSize = {20};

        if (!Prefs.getString(FABRICATED_QS_ICON_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) == 20)
                binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp " + getResources().getString(R.string.opt_default));
            else
                binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp");
            finalIconSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE));
            binding.iconSize.setProgress(finalIconSize[0]);
        } else
            binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + " 20dp " + getResources().getString(R.string.opt_default));

        binding.iconSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalIconSize[0] = progress;
                if (progress == 20)
                    binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp " + getResources().getString(R.string.opt_default));
                else
                    binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_ICON_SIZE, "dimen", "qs_icon_size", finalIconSize[0] + "dp");

                Toast.makeText(getApplicationContext(), finalIconSize[0] + "dp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Hide text size if hide label is enabled
        if (Prefs.getBoolean("IconifyComponentQSHL.overlay")) {
            binding.textSizeContainer.setVisibility(View.GONE);
            binding.textSizeDivider.setVisibility(View.GONE);
        }

        // QS Text Color
        if (isNormalVariantActive()) {
            binding.textColorNormal.setChecked(true);
        } else if (isPixelVariantActive()) {
            binding.textColorPixel.setChecked(true);
        }

        selectedVariant = binding.radioGroupTextColor.getCheckedRadioButtonId() == R.id.textColorNormal ? QS_TEXT_COLOR_VARIANT_NORMAL : QS_TEXT_COLOR_VARIANT_PIXEL;

        binding.radioGroupTextColor.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (Objects.equals(checkedId, R.id.textColorNormal)) {
                if (!Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_NORMAL)) {
                    Prefs.putString(QS_TEXT_COLOR_VARIANT, QS_TEXT_COLOR_VARIANT_NORMAL);

                    if (Prefs.getBoolean(QSPT1_overlay))
                        OverlayUtil.changeOverlayState(QSPT1_overlay, false, QSNT1_overlay, true);
                    if (Prefs.getBoolean(QSPT2_overlay))
                        OverlayUtil.changeOverlayState(QSPT2_overlay, false, QSNT2_overlay, true);
                    if (Prefs.getBoolean(QSPT3_overlay))
                        OverlayUtil.changeOverlayState(QSPT3_overlay, false, QSNT3_overlay, true);
                    if (Prefs.getBoolean(QSPT4_overlay))
                        OverlayUtil.changeOverlayState(QSPT4_overlay, false, QSNT4_overlay, true);
                    handleCommonOverlay();
                }
            } else if (Objects.equals(checkedId, R.id.textColorPixel)) {
                if (!Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_PIXEL)) {
                    Prefs.putString(QS_TEXT_COLOR_VARIANT, QS_TEXT_COLOR_VARIANT_PIXEL);

                    if (Prefs.getBoolean(QSNT1_overlay))
                        OverlayUtil.changeOverlayState(QSNT1_overlay, false, QSPT1_overlay, true);
                    if (Prefs.getBoolean(QSNT2_overlay))
                        OverlayUtil.changeOverlayState(QSNT2_overlay, false, QSPT2_overlay, true);
                    if (Prefs.getBoolean(QSNT3_overlay))
                        OverlayUtil.changeOverlayState(QSNT3_overlay, false, QSPT3_overlay, true);
                    if (Prefs.getBoolean(QSNT4_overlay))
                        OverlayUtil.changeOverlayState(QSNT4_overlay, false, QSPT4_overlay, true);
                    handleCommonOverlay();
                }
            }
        });

        binding.labelWhite.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST1.overlay")));

        binding.labelWhite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelWhiteV2.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST2.overlay")));

        binding.labelWhiteV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelSystemInverse.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST3.overlay")));

        binding.labelSystemInverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST4.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelSystemInverseV2.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST4.overlay")));

        binding.labelSystemInverseV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide Label
        binding.hideLabel.setChecked(Prefs.getBoolean("IconifyComponentQSHL.overlay"));

        binding.hideLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentQSHL.overlay");

                binding.textSizeContainer.setVisibility(View.GONE);
                binding.textSizeDivider.setVisibility(View.GONE);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSHL.overlay");

                binding.textSizeContainer.setVisibility(View.VISIBLE);
                binding.textSizeDivider.setVisibility(View.VISIBLE);
            }
        });

        // Move Icon
        final int[] finalMoveIcon = {16};

        if (!Prefs.getString(FABRICATED_QS_MOVE_ICON).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) == 16)
                binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp " + getResources().getString(R.string.opt_default));
            else
                binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp");
            finalMoveIcon[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON));
            binding.moveIcon.setProgress(finalMoveIcon[0]);
        } else
            binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + " 16dp " + getResources().getString(R.string.opt_default));

        binding.moveIcon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalMoveIcon[0] = progress;
                if (progress == 16)
                    binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp " + getResources().getString(R.string.opt_default));
                else
                    binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_MOVE_ICON, "dimen", "qs_tile_start_padding", finalMoveIcon[0] + "dp");

                Toast.makeText(getApplicationContext(), finalMoveIcon[0] + "dp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String[] replaceVariant(String... args) {
        String[] newArgs = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            newArgs[i] = args[i].replace("QST", Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_NORMAL) ? "QSNT" : "QSPT");
        }

        return newArgs;
    }

    private String replaceVariant(String arg) {
        return arg.replace("QST", Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_NORMAL) ? "QSNT" : "QSPT");
    }

    private boolean isNormalVariantActive() {
        return Prefs.getBoolean(QSNT1_overlay) || Prefs.getBoolean(QSNT2_overlay) || Prefs.getBoolean(QSNT3_overlay) || Prefs.getBoolean(QSNT4_overlay) || Objects.equals(Prefs.getString(QS_TEXT_COLOR_VARIANT), QS_TEXT_COLOR_VARIANT_NORMAL);
    }

    private boolean isPixelVariantActive() {
        return Prefs.getBoolean(QSPT1_overlay) || Prefs.getBoolean(QSPT2_overlay) || Prefs.getBoolean(QSPT3_overlay) || Prefs.getBoolean(QSPT4_overlay) || Objects.equals(Prefs.getString(QS_TEXT_COLOR_VARIANT), QS_TEXT_COLOR_VARIANT_PIXEL);
    }

    private void handleCommonOverlay() {
        OverlayUtil.changeOverlayState(QSNPT_overlay, Prefs.getBoolean(QSNT1_overlay) || Prefs.getBoolean(QSNT2_overlay) || Prefs.getBoolean(QSNT3_overlay) || Prefs.getBoolean(QSNT4_overlay) || Prefs.getBoolean(QSPT1_overlay) || Prefs.getBoolean(QSPT2_overlay) || Prefs.getBoolean(QSPT3_overlay) || Prefs.getBoolean(QSPT4_overlay));
    }
}