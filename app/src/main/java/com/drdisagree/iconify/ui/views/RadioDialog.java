package com.drdisagree.iconify.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.BaseActivity;

public class RadioDialog extends BaseActivity {

    Context context;
    Dialog dialog;
    int selectedIndex, dialogId;
    private RadioDialogListener listener;

    public RadioDialog(Context context, int dialogId, int selectedIndex) {
        this.context = context;
        this.dialogId = dialogId;
        this.selectedIndex = selectedIndex;
    }

    public void setRadioDialogListener(RadioDialogListener listener) {
        this.listener = listener;
    }

    public void show(int title, int items, TextView output) {
        if (dialog != null) dialog.dismiss();

        String[] options = context.getResources().getStringArray(items);

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.view_radio_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        dialog.setCanceledOnTouchOutside(true);

        TextView text = dialog.findViewById(R.id.title);
        text.setText(context.getResources().getText(title));

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group);

        for (int i = 0; i < options.length; i++) {
            LayoutInflater inflater = LayoutInflater.from(context);
            RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.view_radio_button, radioGroup, false);

            radioButton.setText(options[i]);
            radioButton.setId(i);
            radioGroup.addView(radioButton);
        }

        ((RadioButton) radioGroup.getChildAt(selectedIndex)).setChecked(true);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedIndex = checkedId;
            dialog.hide();
            output.setText(((RadioButton) radioGroup.getChildAt(checkedId)).getText());

            if (listener != null) {
                listener.onItemSelected(dialogId, selectedIndex);
            }
        });

        dialog.create();
        dialog.show();

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void hide() {
        if ((dialog != null) && dialog.isShowing()) dialog.dismiss();
    }

    public void dismiss() {
        if (dialog != null) dialog.dismiss();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && !isFinishing()) {
            dialog.dismiss();
        }
    }

    public interface RadioDialogListener {
        void onItemSelected(int dialogId, int selectedIndex);
    }
}
