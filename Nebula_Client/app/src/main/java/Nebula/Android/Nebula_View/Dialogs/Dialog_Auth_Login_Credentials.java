package Nebula.Android.Nebula_View.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Services.Svc_Alert_Dialog;
import Nebula.Android.R;

public class Dialog_Auth_Login_Credentials extends Dialog {

        public Dialog_Auth_Login_Credentials(@NonNull Context context) {
            super(context);

            setContentView(R.layout.dlg_01_login_credentials);

            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(); params.copyFrom(getWindow().getAttributes()); params.width = 200; params.height = WindowManager.LayoutParams.WRAP_CONTENT; // altura automática getWindow().setAttributes(params); }

            init(context);
            setOnCancelListener(d -> {
                Svc_Alert_Dialog.clearListMesage();
            });
        }

    private void init(Context context) {
        ListView listView = findViewById(R.id.list_credential);

        List<String> items = new ArrayList<>();
        String alertMesagesString = Svc_Alert_Dialog.getAlertMesages();

        if (!alertMesagesString.isEmpty()) {
            String[] splitMessages = alertMesagesString.split("\n• ");
            for (String msg : splitMessages) {
                if (!msg.trim().isEmpty()) {
                    items.add("• " + msg.trim());
                }
            }
        }
    }

}





