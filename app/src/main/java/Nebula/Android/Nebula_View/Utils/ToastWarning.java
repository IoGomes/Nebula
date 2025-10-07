package Nebula.Android.Nebula_View.Utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import Nebula.Android.R;

public class ToastWarning {
    private Context context;

    public ToastWarning(Context context) {
        this.context = context;
    }

    private void showToast(int type, String text){

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.tst_01_login_credentials, null);

        int widthInDp = 360;
        float scale = context.getResources().getDisplayMetrics().density;
        int widthInPx = (int) (widthInDp * scale);
        layout.setMinimumWidth(widthInPx);

        TextView textView = layout.findViewById(R.id.toast_text);
        if (textView != null) {
            textView.setText(text);
        }

        switch(type) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }

        Toast toast = new Toast(context);
        toast.setDuration(type == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 40);


        TranslateAnimation slideDown = new TranslateAnimation(
                0, 0,
                -200, 0
        );
        slideDown.setDuration(500);
        layout.startAnimation(slideDown);

        toast.show();
    }

    public void showInfo(String text) {
        showToast(0, text);
    }

    public void showWarning(String text) {
        showToast(1, text);
    }

    public void showError(String text) {
        showToast(2, text);
    }
}
