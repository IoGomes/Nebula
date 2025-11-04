package Nebula.Android.Nebula_View.Activities;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Model.Entitys.Entity_03_Message;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_01_Profile_Image;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Chat_01_Msg_Adapter;
import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Video_Call;
import Nebula.Android.Nebula_ViewModel.Controllers.Controller_Voice_Call;
import Nebula.Android.R;
import Nebula.Android.databinding.Act03ChatBinding;

/// @author Ítalo Oliveira Gomes

public class Activity_03_Chat extends AppCompatActivity {

    private RV_Chat_01_Msg_Adapter adapter;
    private List<Entity_03_Message> messageList;
    private Act03ChatBinding bind;

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceBundle);

        bind = Act03ChatBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        setupBasicUI();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        bind.getRoot().post(this::initializeHeavyComponents);
        bind.returnButton.setOnClickListener(v -> finish());
    }

    private void setupBasicUI() {

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        messageList = new ArrayList<>();
        adapter = new RV_Chat_01_Msg_Adapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        bind.rvMessage.setLayoutManager(layoutManager);
        bind.rvMessage.setAdapter(adapter);
        bind.rvMessage.setItemAnimator(null);
        bind.rvMessage.setHasFixedSize(true);
        bind.profilePhoto.setOnClickListener(v ->
            new Dialog_Feed_01_Profile_Image(v.getContext()).show());
    }

    private void initializeHeavyComponents() {

        executorService.execute(() -> {
            mainHandler.post(() -> setTheme(androidx.appcompat.R.style.Theme_AppCompat));
        });

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        setupClickListeners();
        setupTextWatcher();
        setupKeyboardListener();
    }

    private void setupClickListeners() {
        bind.videoCall.setOnClickListener(v ->
                new Controller_Video_Call(this).performVideoCall(this));

        bind.voiceCall.setOnClickListener(v ->
                new Controller_Voice_Call(this).performVoiceCall(this));

        bind.send.setOnClickListener(v -> {
            String text = bind.messageTextfield.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                bind.messageTextfield.setText("");
            }
        });
    }

    private void setupTextWatcher() {
        bind.messageTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void sendMessage(String text) {

        Entity_03_Message newMessage = new Entity_03_Message();
        newMessage.setMessage(text);
        newMessage.setDateTimeMessage(new Date());
        newMessage.setWasVisualized(false);

        messageList.add(newMessage);
        adapter.notifyItemInserted(messageList.size() - 1);
        bind.rvMessage.scrollToPosition(messageList.size() - 1);
    }


    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeypadHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = rootView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (Math.abs(keypadHeight - lastKeypadHeight) < 10) {
                    return;
                }
                lastKeypadHeight = keypadHeight;

                ConstraintLayout.LayoutParams layoutParams =
                        (ConstraintLayout.LayoutParams) bind.bottomBar.getLayoutParams();

                layoutParams.bottomMargin = keypadHeight > screenHeight * 0.15
                        ? keypadHeight
                        : 0;

                bind.bottomBar.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}