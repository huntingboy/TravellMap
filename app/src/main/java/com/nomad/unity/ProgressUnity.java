package com.nomad.unity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by nomad on 18-4-10.
 */

public class ProgressUnity {
    private static ProgressDialog progressDialog;
    public ProgressUnity(Context context) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
        }
    }

    public void showProgressDiaglog() {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在执行~~");
        progressDialog.show();

    }
    public void dissmissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;  //此句话一定要有  不然到处都是is activity running错误
        }
    }
}
