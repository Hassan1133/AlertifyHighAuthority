package com.example.alertify_main_admin.main_utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.alertify_main_admin.R;

public class LoadingDialog {

    private static Dialog dialog;

    public static void showLoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    public static void hideLoadingDialog() {
        dialog.dismiss();
    }
}
