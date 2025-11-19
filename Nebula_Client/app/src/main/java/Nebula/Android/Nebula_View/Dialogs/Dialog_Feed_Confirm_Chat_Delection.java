package Nebula.Android.Nebula_View.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import Nebula.Android.R;

public class Dialog_Feed_Confirm_Chat_Delection extends Dialog {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public Dialog_Feed_Confirm_Chat_Delection(@NonNull Context context, @NonNull OnConfirmListener listener) {
        super(context);
        init(context, listener);
    }

    private void init(Context context, OnConfirmListener listener) {
        // Infla o layout XML personalizado
        View view = LayoutInflater.from(context).inflate(R.layout.dlg_10_confirm_action, null);
        setContentView(view);

        // Configura título e mensagem
        TextView title = view.findViewById(R.id.title);
        TextView message = view.findViewById(R.id.message);


        // Configura botões
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> {
            v.postDelayed(() -> {
                listener.onConfirm();
                dismiss();

            }, 140);
        });

        // Ajustes visuais do diálogo
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        // Fundo arredondado (se estiver usando o drawable)
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
