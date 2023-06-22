package com.kdang.library.common;

import android.app.AlertDialog;
import android.content.Context;

public class BaseAlertDialog extends AlertDialog {
    protected BaseAlertDialog(Context context) {
        super(context);
    }

    protected BaseAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected BaseAlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
}
