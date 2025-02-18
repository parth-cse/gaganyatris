package com.gaganyatris.gaganyatri.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.gaganyatris.gaganyatri.R;

public class LoadingDialog {
    private Dialog dialog;
    private TextView messageTextView;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);

        // Initialize the TextView
        messageTextView = dialog.findViewById(R.id.loading_message);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // Method to update the message
    public void setMessage(String message) {
        if (messageTextView != null) {
            messageTextView.setText(message);
        }
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
