package Mercury.Android.Mercury_View.Activities;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import Mercury.Android.Mercury_Model.Entitys.Entity_03_Message;
import Mercury.Android.Mercury_View.RecyclerView.RV_Chat_01_Msg_Adapter;
import Mercury.Android.Mercury_View.Utils.NavBar_Inserts;
import Mercury.Android.Mercury_ViewModel.Controllers.Controller_Video_Call;
import Mercury.Android.R;
import Mercury.Android.databinding.Activity03ChatBinding;

/// @author Ítalo Oliveira Gomes

public class Activity_03_Chat extends AppCompatActivity {

    private ConstraintLayout bottomBar;
    private RV_Chat_01_Msg_Adapter adapter;
    private List<Entity_03_Message> messageList;
    private ImageButton button;
    Activity03ChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceBundle);

        binding = Activity03ChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        RecyclerView recyclerView = findViewById(R.id.rv_message);
        EditText editMessage = findViewById(R.id.messageTextfield);
        ImageButton buttonSend = findViewById(R.id.send);;

        messageList = new ArrayList<>();
        adapter = new RV_Chat_01_Msg_Adapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        bottomBar = findViewById(R.id.bottom_bar);
        setupKeyboardListener();

        binding.videoCall.setOnClickListener(v -> new Controller_Video_Call(this).performVideoCall(this));

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        buttonSend.setOnClickListener(v -> {

            String text = editMessage.getText().toString().trim();

            if (!text.isEmpty()) {

                // Aplica o syntax highlighting no texto
                String highlightedText = applySyntaxHighlight(text);

                Entity_03_Message newMessage = new Entity_03_Message();
                newMessage.setMessage(highlightedText);  // Salva o texto com HTML
                newMessage.setDateTimeMessage(new Date());
                newMessage.setWasVisualized(false);

                messageList.add(newMessage);
                adapter.notifyItemInserted(messageList.size() - 1);

                recyclerView.scrollToPosition(messageList.size() - 1);

                editMessage.setText("");
            }
        });

        ImageButton popupMenu = findViewById(R.id.menu);

        popupMenu.setOnClickListener(v -> {
            int offsetDp = 20;
            int offsetY = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, offsetDp, getResources().getDisplayMetrics());
            offsetY = -offsetY;

            PopupMenu popup = new PopupMenu(this, v, Gravity.NO_GRAVITY, 0, offsetY);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

            try {
                Field mPopup = popup.getClass().getDeclaredField("mPopup");
                mPopup.setAccessible(true);
                Object menuPopupHelper = mPopup.get(popup);
                Method setBackgroundDrawable = menuPopupHelper.getClass()
                        .getDeclaredMethod("setBackgroundDrawable", Drawable.class);
                setBackgroundDrawable.invoke(menuPopupHelper,
                        ContextCompat.getDrawable(this, R.drawable.bg_popup_menu));
            } catch (Exception e) {
                e.printStackTrace();
            }

            popup.setOnMenuItemClickListener(item -> true);
            popup.show();
        });

        ImageButton camera = findViewById(R.id.camera);
        camera.setOnClickListener(v -> {
            int offsetDp = 20;
            int offsetY = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, offsetDp, getResources().getDisplayMetrics());
            offsetY = -offsetY;

            PopupMenu camera1 = new PopupMenu(this, v, Gravity.NO_GRAVITY, 0, offsetY);
            camera1.getMenuInflater().inflate(R.menu.camera_pop_up, camera1.getMenu());

            try {
                Field mPopup = camera1.getClass().getDeclaredField("mPopup");
                mPopup.setAccessible(true);
                Object menuPopupHelper = mPopup.get(camera1);
                assert menuPopupHelper != null;
                Method setBackgroundDrawable = menuPopupHelper.getClass()
                        .getDeclaredMethod("setBackgroundDrawable", Drawable.class);
                setBackgroundDrawable.invoke(menuPopupHelper,
                        ContextCompat.getDrawable(this, R.drawable.bg_popup_menu));
            } catch (Exception e) {
                e.printStackTrace();
            }

            camera1.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        return true;
                    case 2:
                        return true;
                    default:
                        return false;
                }
            });

            camera1.show();
        });

    }

    /**
     * Aplica syntax highlighting estilo IntelliJ no código Java
     */
    private String applySyntaxHighlight(String code) {
        String result = code;

        // 1. Comentários
        result = highlightComments(result);

        // 2. Strings
        result = highlightStrings(result);

        // 3. Anotações
        result = highlightAnnotations(result);

        // 4. Palavras-chave
        result = highlightKeywords(result);

        // 5. Números
        result = highlightNumbers(result);

        // 6. Classes
        result = highlightClasses(result);

        // 7. Métodos
        result = highlightMethods(result);

        return result;
    }

    private String highlightComments(String code) {
        code = code.replaceAll("(//.*?)(?=\n|$)",
                "<font color='#808080'>$1</font>");

        code = code.replaceAll("(/\\*.*?\\*/)",
                "<font color='#808080'>$1</font>");

        return code;
    }

    private String highlightStrings(String code) {
        code = code.replaceAll("(\"(?:[^\"\\\\]|\\\\.)*\")",
                "<font color='#6A8759'>$1</font>");

        code = code.replaceAll("('(?:[^'\\\\]|\\\\.)*')",
                "<font color='#6A8759'>$1</font>");

        return code;
    }

    private String highlightAnnotations(String code) {
        code = code.replaceAll("(@\\w+)",
                "<font color='#BBB529'>$1</font>");

        return code;
    }

    private String highlightKeywords(String code) {
        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double",
                "else", "enum", "extends", "final", "finally", "float", "for",
                "goto", "if", "implements", "import", "instanceof", "int", "interface",
                "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized",
                "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };

        for (String keyword : keywords) {
            code = code.replaceAll("\\b(" + keyword + ")\\b",
                    "<font color='#CC7832'>$1</font>");
        }

        return code;
    }

    private String highlightNumbers(String code) {
        code = code.replaceAll("\\b(\\d+\\.?\\d*[fFdDlL]?)\\b",
                "<font color='#6897BB'>$1</font>");

        return code;
    }

    private String highlightClasses(String code) {
        code = code.replaceAll("\\b([A-Z]\\w*)\\b",
                "<font color='#A9B7C6'>$1</font>");

        return code;
    }

    private String highlightMethods(String code) {
        code = code.replaceAll("\\b([a-z]\\w*)(?=\\s*\\()",
                "<font color='#FFC66D'>$1</font>");

        return code;
    }

    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) bottomBar.getLayoutParams();

                if (keypadHeight > screenHeight * 0.15) {
                    layoutParams.bottomMargin = keypadHeight;
                } else {
                    layoutParams.bottomMargin = dpToPx(0);
                }

                bottomBar.setLayoutParams(layoutParams);
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}