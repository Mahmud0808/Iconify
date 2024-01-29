package com.drdisagree.iconify.ui.fragments;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Dynamic.isAtleastA14;
import static com.drdisagree.iconify.common.Preferences.QS_HIDE_LABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_NORMAL;
import static com.drdisagree.iconify.common.Preferences.QS_TEXT_COLOR_VARIANT_PIXEL;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ICON_SIZE;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_MOVE_ICON;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TEXT_SIZE;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.databinding.FragmentQsIconLabelBinding;
import com.drdisagree.iconify.ui.base.BaseFragment;
import com.drdisagree.iconify.ui.utils.ViewHelper;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceEntry;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.google.android.material.slider.Slider;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class QsIconLabel extends BaseFragment {

    private static String selectedVariant;
    private FragmentQsIconLabelBinding binding;
    public static final String QSNPT_overlay = "IconifyComponentQSNPT.overlay";
    public static final String QSNT1_overlay = "IconifyComponentQSNT1.overlay";
    public static final String QSNT2_overlay = "IconifyComponentQSNT2.overlay";
    public static final String QSNT3_overlay = "IconifyComponentQSNT3.overlay";
    public static final String QSNT4_overlay = "IconifyComponentQSNT4.overlay";
    public static final String QSPT1_overlay = "IconifyComponentQSPT1.overlay";
    public static final String QSPT2_overlay = "IconifyComponentQSPT2.overlay";
    public static final String QSPT3_overlay = "IconifyComponentQSPT3.overlay";
    public static final String QSPT4_overlay = "IconifyComponentQSPT4.overlay";

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
            finalTextSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE));
            binding.textSize.setSliderValue(finalTextSize[0]);
        }

        // Reset button
        binding.textSize.setResetClickListener(v -> {
            finalTextSize[0] = 14;
            Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size")
            );
            return true;
        });

        binding.textSize.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalTextSize[0] = (int) slider.getValue();
                Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));
                ResourceManager.buildOverlayWithResource(
                        requireContext(),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size", finalTextSize[0] + "sp")
                );
            }
        });

        // Icon Size
        final int[] finalIconSize = {20};

        if (!Prefs.getString(FABRICATED_QS_ICON_SIZE).equals(STR_NULL)) {
            finalIconSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE));
            binding.iconSize.setSliderValue(finalIconSize[0]);
        }

        // Reset button
        binding.iconSize.setResetClickListener(v -> {
            finalIconSize[0] = 20;
            Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_icon_size")
            );
            return true;
        });

        binding.iconSize.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalIconSize[0] = (int) slider.getValue();
                Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));
                ResourceManager.buildOverlayWithResource(
                        requireContext(),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_icon_size", finalIconSize[0] + "sp")
                );
            }
        });

        // Hide text size if hide label is enabled
        if (Prefs.getBoolean(QS_HIDE_LABEL_SWITCH, false)) {
            binding.textSize.setVisibility(View.GONE);
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
                    selectedVariant = QS_TEXT_COLOR_VARIANT_NORMAL;

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
                    selectedVariant = QS_TEXT_COLOR_VARIANT_PIXEL;

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

        binding.labelWhite.setSwitchChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST1.overlay")));
        binding.labelWhite.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked && isAtleastA14) {
                Toast.makeText(requireContext(), R.string.toast_use_from_xposed_menu, Toast.LENGTH_SHORT).show();
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhiteV2.setSwitchChecked(false);
                    binding.labelSystem.setSwitchChecked(false);
                    binding.labelSystemV2.setSwitchChecked(false);
                    binding.labelFixTextColor.setSwitchChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST1.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelWhiteV2.setSwitchChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST2.overlay")));
        binding.labelWhiteV2.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked && isAtleastA14) {
                Toast.makeText(requireContext(), R.string.toast_use_from_xposed_menu, Toast.LENGTH_SHORT).show();
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setSwitchChecked(false);
                    binding.labelSystem.setSwitchChecked(false);
                    binding.labelSystemV2.setSwitchChecked(false);
                    binding.labelFixTextColor.setSwitchChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST2.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelSystem.setSwitchChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST3.overlay")));
        binding.labelSystem.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked && isAtleastA14) {
                Toast.makeText(requireContext(), R.string.toast_use_from_xposed_menu, Toast.LENGTH_SHORT).show();
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setSwitchChecked(false);
                    binding.labelWhiteV2.setSwitchChecked(false);
                    binding.labelSystemV2.setSwitchChecked(false);
                    binding.labelFixTextColor.setSwitchChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST4.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST3.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelSystemV2.setSwitchChecked(Prefs.getBoolean(replaceVariant("IconifyComponentQST4.overlay")));
        binding.labelSystemV2.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked && isAtleastA14) {
                Toast.makeText(requireContext(), R.string.toast_use_from_xposed_menu, Toast.LENGTH_SHORT).show();
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setSwitchChecked(false);
                    binding.labelWhiteV2.setSwitchChecked(false);
                    binding.labelSystem.setSwitchChecked(false);
                    binding.labelFixTextColor.setSwitchChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST5.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST4.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        binding.labelFixTextColor.setSwitchChecked(Prefs.getBoolean("IconifyComponentQST5.overlay"));
        binding.labelFixTextColor.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked && isAtleastA14) {
                Toast.makeText(requireContext(), R.string.toast_use_from_xposed_menu, Toast.LENGTH_SHORT).show();
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isChecked) {
                    binding.labelWhite.setSwitchChecked(false);
                    binding.labelWhiteV2.setSwitchChecked(false);
                    binding.labelSystem.setSwitchChecked(false);
                    binding.labelSystemV2.setSwitchChecked(false);

                    OverlayUtil.disableOverlays(replaceVariant("IconifyComponentQST1.overlay", "IconifyComponentQST2.overlay", "IconifyComponentQST3.overlay", "IconifyComponentQST4.overlay"));
                    OverlayUtil.enableOverlay(replaceVariant("IconifyComponentQST5.overlay"));
                } else {
                    OverlayUtil.disableOverlay(replaceVariant("IconifyComponentQST5.overlay"));
                }
                handleCommonOverlay();
            }, SWITCH_ANIMATION_DELAY);
        });

        // Hide Label
        AtomicBoolean isHideLabelContainerClicked = new AtomicBoolean(false);
        binding.hideLabel.setSwitchChecked(Prefs.getBoolean(QS_HIDE_LABEL_SWITCH, false));
        binding.hideLabel.setSwitchChangeListener((buttonView, isChecked) -> {
            if (!SystemUtil.hasStoragePermission()) {
                isHideLabelContainerClicked.set(false);
                SystemUtil.requestStoragePermission(requireContext());
                binding.hideLabel.setSwitchChecked(!isChecked);
            } else if (buttonView.isPressed() || isHideLabelContainerClicked.get()) {
                isHideLabelContainerClicked.set(false);
                Prefs.putBoolean(QS_HIDE_LABEL_SWITCH, isChecked);

                if (isChecked) {
                    ResourceManager.buildOverlayWithResource(
                            requireContext(),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size", "0sp"),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_label_container_margin", "-120dp")
                    );

                    binding.textSize.setVisibility(View.GONE);
                } else {
                    ResourceManager.removeResourceFromOverlay(
                            requireContext(),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_text_size"),
                            new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_label_container_margin")
                    );

                    binding.textSize.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.hideLabel.setBeforeSwitchChangeListener(() -> isHideLabelContainerClicked.set(true));

        // Move Icon
        final int[] finalMoveIcon = {16};

        if (!Prefs.getString(FABRICATED_QS_MOVE_ICON).equals(STR_NULL)) {
            finalMoveIcon[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON));
            binding.moveIcon.setSliderValue(finalMoveIcon[0]);
        }

        // Reset button
        binding.moveIcon.setResetClickListener(v -> {
            finalMoveIcon[0] = 16;
            Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));
            ResourceManager.removeResourceFromOverlay(
                    requireContext(),
                    new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_start_padding")
            );
            return true;
        });

        binding.moveIcon.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                finalMoveIcon[0] = (int) slider.getValue();
                Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));
                ResourceManager.buildOverlayWithResource(
                        requireContext(),
                        new ResourceEntry(SYSTEMUI_PACKAGE, "dimen", "qs_tile_start_padding", finalMoveIcon[0] + "dp")
                );
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
        return Prefs.getBoolean(QSNT1_overlay) ||
                Prefs.getBoolean(QSNT2_overlay) ||
                Prefs.getBoolean(QSNT3_overlay) ||
                Prefs.getBoolean(QSNT4_overlay) ||
                Objects.equals(
                        Prefs.getString(QS_TEXT_COLOR_VARIANT),
                        QS_TEXT_COLOR_VARIANT_NORMAL
                );
    }

    private boolean isPixelVariantActive() {
        return Prefs.getBoolean(QSPT1_overlay) ||
                Prefs.getBoolean(QSPT2_overlay) ||
                Prefs.getBoolean(QSPT3_overlay) ||
                Prefs.getBoolean(QSPT4_overlay) ||
                Objects.equals(
                        Prefs.getString(QS_TEXT_COLOR_VARIANT),
                        QS_TEXT_COLOR_VARIANT_PIXEL
                );
    }

    private void handleCommonOverlay() {
        OverlayUtil.changeOverlayState(
                QSNPT_overlay,
                Prefs.getBoolean(QSNT1_overlay) ||
                        Prefs.getBoolean(QSNT2_overlay) ||
                        Prefs.getBoolean(QSNT3_overlay) ||
                        Prefs.getBoolean(QSNT4_overlay) ||
                        Prefs.getBoolean(QSPT1_overlay) ||
                        Prefs.getBoolean(QSPT2_overlay) ||
                        Prefs.getBoolean(QSPT3_overlay) ||
                        Prefs.getBoolean(QSPT4_overlay));
    }
}