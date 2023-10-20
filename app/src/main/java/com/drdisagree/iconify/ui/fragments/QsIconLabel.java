package com.drdisagree.iconify.ui.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentQsIconLabelBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.google.android.material.slider.Slider;

import java.util.Objects;

public class QsIconLabel extends BaseFragment {

    private static String selectedVariant;
    private FragmentQsIconLabelBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsIconLabelBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Header
        ViewHelper.setHeader(requireContext(), getParentFragmentManager(), binding.header.toolbar, R.string.activity_title_qs_icon_label);

        // Text Size
        final int[] finalTextSize = {14};

        if (!Prefs.getString(FABRICATED_QS_TEXT_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) == 14)
                binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            else
                binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp");
            finalTextSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE));
            binding.textSize.setValue(finalTextSize[0]);
        } else
            binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + " 14sp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));

        // Reset button
        binding.resetTextSize.setVisibility(finalTextSize[0] == 14 ? View.INVISIBLE : View.VISIBLE);
        binding.resetTextSize.setOnLongClickListener(v -> {
            finalTextSize[0] = 14;
            binding.textSize.setValue(finalTextSize[0]);
            binding.resetTextSize.setVisibility(View.INVISIBLE);
            binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalTextSize[0] + "sp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));
            FabricatedUtil.disableOverlay(FABRICATED_QS_TEXT_SIZE);
            return true;
        });

        binding.textSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalTextSize[0] = (int) slider.getValue();
                if (finalTextSize[0] == 14) {
                    binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalTextSize[0] + "sp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
                    binding.resetTextSize.setVisibility(View.INVISIBLE);
                } else {
                    binding.textSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalTextSize[0] + "sp");
                    binding.resetTextSize.setVisibility(View.VISIBLE);
                }
                Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_TEXT_SIZE, "dimen", "qs_tile_text_size", finalTextSize[0] + "sp");
                Toast.makeText(Iconify.getAppContext(), finalTextSize[0] + "sp " + Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Icon Size
        final int[] finalIconSize = {20};

        if (!Prefs.getString(FABRICATED_QS_ICON_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) == 20) {
                binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            } else {
                binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp");
            }
            finalIconSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE));
            binding.iconSize.setValue(finalIconSize[0]);
        } else
            binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + " 20dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));

        // Reset button
        binding.resetIconSize.setVisibility(finalIconSize[0] == 20 ? View.INVISIBLE : View.VISIBLE);
        binding.resetIconSize.setOnLongClickListener(v -> {
            finalIconSize[0] = 20;
            binding.iconSize.setValue(finalIconSize[0]);
            binding.resetIconSize.setVisibility(View.INVISIBLE);
            binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalIconSize[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));
            FabricatedUtil.disableOverlay(FABRICATED_QS_ICON_SIZE);
            return true;
        });

        binding.iconSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalIconSize[0] = (int) slider.getValue();
                if (finalIconSize[0] == 20) {
                    binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalIconSize[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
                    binding.resetIconSize.setVisibility(View.INVISIBLE);
                } else {
                    binding.iconSizeOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalIconSize[0] + "dp");
                    binding.resetIconSize.setVisibility(View.VISIBLE);
                }
                Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_ICON_SIZE, "dimen", "qs_icon_size", finalIconSize[0] + "dp");
                Toast.makeText(Iconify.getAppContext(), finalIconSize[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Hide text size if hide label is enabled
        if (Prefs.getBoolean("IconifyComponentQSHL.overlay")) {
            binding.textSizeContainer.setVisibility(View.GONE);
        }

        // QS Text Color
        if (isNormalVariantActive()) {
            binding.toggleButtonTextColor.check(R.id.textColorNormal);
        } else if (isPixelVariantActive()) {
            binding.toggleButtonTextColor.check(R.id.textColorPixel);
        }

        selectedVariant = binding.toggleButtonTextColor.getCheckedButtonId() == R.id.textColorNormal ? QS_TEXT_COLOR_VARIANT_NORMAL : QS_TEXT_COLOR_VARIANT_PIXEL;

        binding.toggleButtonTextColor.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
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
                    binding.labelFixtextcolor.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.labelWhiteContainer.setOnClickListener(v -> binding.labelWhite.toggle());

        binding.labelWhiteV2.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST2.overlay")));
        binding.labelWhiteV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);
                    binding.labelFixtextcolor.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.labelWhiteV2Container.setOnClickListener(v -> binding.labelWhiteV2.toggle());

        binding.labelSystemInverse.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST3.overlay")));
        binding.labelSystemInverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);
                    binding.labelFixtextcolor.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.labelSystemInverseContainer.setOnClickListener(v -> binding.labelSystemInverse.toggle());

        binding.labelSystemInverseV2.setChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST4.overlay")));
        binding.labelSystemInverseV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);
                    binding.labelFixtextcolor.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.labelSystemInverseV2Container.setOnClickListener(v -> binding.labelSystemInverseV2.toggle());

        binding.labelFixtextcolor.setChecked(Prefs.getBoolean("IconifyComponentQST5.overlay"));
        binding.labelFixtextcolor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setChecked(false);
                    binding.labelWhiteV2.setChecked(false);
                    binding.labelSystemInverse.setChecked(false);
                    binding.labelSystemInverseV2.setChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST5.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST5.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });
        binding.labelFixtextcolorContainer.setOnClickListener(v -> binding.labelFixtextcolor.toggle());

        // Hide Label
        binding.hideLabel.setChecked(Prefs.getBoolean("IconifyComponentQSHL.overlay"));
        binding.hideLabel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentQSHL.overlay");

                binding.textSizeContainer.setVisibility(View.GONE);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSHL.overlay");

                binding.textSizeContainer.setVisibility(View.VISIBLE);
            }
        });
        binding.hideLabelContainer.setOnClickListener(v -> binding.hideLabel.toggle());

        // Move Icon
        final int[] finalMoveIcon = {16};

        if (!Prefs.getString(FABRICATED_QS_MOVE_ICON).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) == 16)
                binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            else
                binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp");
            finalMoveIcon[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON));
            binding.moveIcon.setValue(finalMoveIcon[0]);
        } else
            binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + " 16dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));

        // Reset button
        binding.resetMoveIcon.setVisibility(finalMoveIcon[0] == 16 ? View.INVISIBLE : View.VISIBLE);
        binding.resetMoveIcon.setOnLongClickListener(v -> {
            finalMoveIcon[0] = 16;
            binding.moveIcon.setValue(finalMoveIcon[0]);
            binding.resetMoveIcon.setVisibility(View.INVISIBLE);
            binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalMoveIcon[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
            Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));
            FabricatedUtil.disableOverlay(FABRICATED_QS_MOVE_ICON);
            return true;
        });

        binding.moveIcon.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalMoveIcon[0] = (int) slider.getValue();
                if (finalMoveIcon[0] == 16) {
                    binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalMoveIcon[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.opt_default));
                    binding.resetMoveIcon.setVisibility(View.INVISIBLE);
                } else {
                    binding.moveIconOutput.setText(getResources().getString(R.string.opt_selected) + ' ' + finalMoveIcon[0] + "dp");
                    binding.resetMoveIcon.setVisibility(View.VISIBLE);
                }
                Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_MOVE_ICON, "dimen", "qs_tile_start_padding", finalMoveIcon[0] + "dp");
                Toast.makeText(Iconify.getAppContext(), finalMoveIcon[0] + "dp " + Iconify.getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private String[] replaceVariant(String... args) {
        String[] newArgs = new String[args.length];

        for (int i = 0; i < args.length; i++) {
            if (args[i].contains("QST5")) continue;
            newArgs[i] = args[i].replace("QST", Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_NORMAL) ? "QSNT" : "QSPT");
        }

        return newArgs;
    }

    private String replaceVariant(String arg) {
        return arg.contains("QST5") ? arg : arg.replace("QST", Objects.equals(selectedVariant, QS_TEXT_COLOR_VARIANT_NORMAL) ? "QSNT" : "QSPT");
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