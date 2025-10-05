package Mercury.Android.Mercury_View.Utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import Mercury.Android.R;

public class ToastWarning {
    private Context context;

    public ToastWarning(Context context) {
        this.context = context;
    }

    private void showToast(int type, String text){
        // Inflar o layout customizado
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast_01_login_credentials, null);

        // Converter 350dp para pixels
        int widthInDp = 360;
        float scale = context.getResources().getDisplayMetrics().density;
        int widthInPx = (int) (widthInDp * scale);
        layout.setMinimumWidth(widthInPx);

        // Encontrar o TextView no layout e definir o texto
        TextView textView = layout.findViewById(R.id.toast_text);
        if (textView != null) {
            textView.setText(text);
        }

        // Configurar a aparência baseada no tipo (opcional)
        switch(type) {
            case 0: // Info
                break;
            case 1: // Warning
                // layout.setBackgroundResource(R.drawable.toast_warning_bg);
                break;
            case 2: // Error
                // layout.setBackgroundResource(R.drawable.toast_error_bg);
                break;
        }

        // Criar o toast
        Toast toast = new Toast(context);
        toast.setDuration(type == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 40);

        // Adicionar animação de slide down
        TranslateAnimation slideDown = new TranslateAnimation(
                0, 0,     // fromXDelta, toXDelta
                -200, 0   // fromYDelta (acima da tela), toYDelta
        );
        slideDown.setDuration(500); // 0.5s
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
