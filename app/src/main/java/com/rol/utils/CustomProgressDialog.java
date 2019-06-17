package com.rol.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import com.rol.R;

public class CustomProgressDialog extends Dialog {
    Context context;
    ListView list;
    String message;

    public CustomProgressDialog(Context mContext, String message) {
        super(mContext);
        context = mContext;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.progress_dialog);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }
}