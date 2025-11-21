package Nebula.Android.Nebula_View.Activities;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Nebula.Android.Nebula_Data.LocalDb.DatabaseHelper;
import Nebula.Android.Nebula_Data.Repository.Repo_Archived_Chats;
import Nebula.Android.Nebula_Data.Repository.Repo_Calls_History;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Confirm_Chat_Delection;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_01_Inbox;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_02_Contacts;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_03_Calls;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_04_Archived;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_03_Calls_Adapter;
import Nebula.Android.R;
import Nebula.Android.databinding.Act02FeedBinding;

/// @author Ítalo Oliveira Gomes

/// Activity principal do Feed
@SuppressWarnings("SpellCheckingInspection")
public class Activity_02_Feed extends AppCompatActivity {

    private static final String TAG = "Activity_02_Feed";

    /// Declaração do ViewBinding
    private Act02FeedBinding bind;

    /// Declaração dos Fragments
    private final Fragment fragment01 = new Fragment_Feed_01_Inbox();
    private final Fragment fragment02 = new Fragment_Feed_02_Contacts();
    private final Fragment fragment03 = new Fragment_Feed_03_Calls();
    private final Fragment fragment04 = new Fragment_Feed_04_Archived();

    /// Declarações uteis para performance do código
    private ImageButton lastClickedButton = null;

    /// Handler e Runnable para Delay
    private Runnable hideNotifyRunnable;
    Handler handler = new Handler(Looper.getMainLooper());
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        TimingUtils.start("onCreate");

        super.onCreate(savedInstanceBundle);

        /// Configura Elementos iniciais da UI
        setupUI();

        /// Define o Fragment Inicial
        bind = Act02FeedBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        /// Configura o Fragment Inicial
        replaceFragment(fragment01);

        this.deleteDatabase("NebulaLocalDB.db");
        dbHelper.copyDatabaseIfNeeded();

        /// Inicializa os Repositórios
        Repo_Chat.initialize(this);

        TimingUtils.stop("onCreate");
    }

    @Override
    public void onStart(){
        super.onStart();

        Repo_Contact.initialize(this);
        Repo_Calls_History.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /// Atrelando Botões XML a suas respectivas funções
        bind.git.setOnClickListener(v -> startGitActivity(this));
        bind.trash.setOnClickListener(v -> delete(this));
        bind.favorite.setOnClickListener(v -> favorite());
        bind.ImageButtonArchived.setOnClickListener(v -> archive());
        bind.ImageButtonUnarchived.setOnClickListener(v -> unarchive());
        bind.close.setOnClickListener(v -> close(this));

        if (dbHelper.getUnreadCount() != 0) {
            RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
            bind.countUnread.setVisibility(View.VISIBLE);
            bind.countUnread.setText(String.valueOf(dbHelper.getUnreadCount()));
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else bind.countUnread.setVisibility(View.GONE);

        changeButtonBg();
    }

    /// Methods to Load UI aspects
    private void setupUI() {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
    private void selectButton(ImageButton imageButton) {
        if (imageButton != null) {

            if (lastClickedButton != null && lastClickedButton != imageButton) {
                lastClickedButton.setBackground(null);
            }
            imageButton.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_selected_highlight));
            lastClickedButton = imageButton;
        }
    }
    private void changeButtonBg() {
        int[] buttonsIds = {
                R.id.button_inbox,
                R.id.button_contact,
                R.id.button_call,
                R.id.button_archived,
        };

        Map<Integer, Integer> layoutParaBotaoMap = new HashMap<>();
        layoutParaBotaoMap.put(R.id.button_inbox, R.id.inbox);
        layoutParaBotaoMap.put(R.id.button_contact, R.id.contact);
        layoutParaBotaoMap.put(R.id.button_call, R.id.calls);
        layoutParaBotaoMap.put(R.id.button_archived, R.id.archived);

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.button_inbox, fragment01);
        fragmentMap.put(R.id.button_contact, fragment02);
        fragmentMap.put(R.id.button_call, fragment03);
        fragmentMap.put(R.id.button_archived, fragment04);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        for (int id : buttonsIds) {
            LinearLayout btn = findViewById(id);
            btn.setOnClickListener(v -> {

                v.setEnabled(false);

                int viewId = v.getId();
                Integer imageButtonId = layoutParaBotaoMap.get(viewId);
                Fragment targetFragment = fragmentMap.get(viewId);

                if (imageButtonId == null || targetFragment == null) {
                    v.setEnabled(true);
                    return;
                }

                new Thread(() -> {

                    mainHandler.post(() -> {
                        ImageButton imageButton = findViewById(imageButtonId);
                        selectButton(imageButton);
                        hideOptionsBar();
                        replaceFragment(targetFragment);
                        mainHandler.postDelayed(() -> v.setEnabled(true), 300);
                    });
                }).start();
            });
        }

        ImageButton imageButtonInbox = findViewById(layoutParaBotaoMap.get(R.id.button_inbox));
        selectButton(imageButtonInbox);
        replaceFragment(fragmentMap.get(R.id.button_inbox));
    }

    /// Methods to Display and Hide Notify Animations after option selected
    public void notifyAnimation(int count, String singular, String plural) {

        String text = count + " " + (count == 1 ? singular : plural);
        bind.notify.setText(text);

        bind.notify.setVisibility(View.VISIBLE);
        bind.notify.startAnimation(
                AnimationUtils.loadAnimation(bind.getRoot().getContext(), R.anim.slide_in_right)
        );

        hideNotifyRunnable = () -> {
            bind.notify.startAnimation(
                    AnimationUtils.loadAnimation(bind.getRoot().getContext(), R.anim.slide_out_right)
            );
            bind.notify.setVisibility(View.GONE);
        };

        handler.postDelayed(hideNotifyRunnable, 2000);
        hideOptionsBar();
    }
    public void cancelNotifyAnimation() {
        bind.notify.clearAnimation();
        bind.notify.setVisibility(View.GONE);
        handler.removeCallbacks(hideNotifyRunnable);
    }

    /// Methods to close and clean selected items from the OptionsBar
    private void closeFromFragment01() {
        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
        adapter.clearSelection();
        hideOptionsBar();
    }
    private void closeFromFragment02() {
        RV_Feed_02_Contact_Adapter adapter = Repo_Contact.getFeedAdapter();
        adapter.clearSelection();
        hideOptionsBar();
    }
    private void closeFromFragment03() {
        RV_Feed_03_Calls_Adapter adapter = Repo_Calls_History.getFeedAdapter();
        adapter.clearSelection();
        hideOptionsBar();
    }
    private void closeFromFragment04() {
        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
        adapter.clearSelection();
        hideOptionsBar();
    }

    /// Delete Switch that guide to specific delete Methods Below
    private void delete(Context context) {
        new Dialog_Feed_Confirm_Chat_Delection(context, () -> {

            if (context instanceof FragmentActivity) {
                Fragment currentFragment = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);

                switch (currentFragment.getClass().getSimpleName()) {
                    case "Fragment_Feed_01_Inbox":
                        deleteFromFragment01();
                        break;

                    case "Fragment_Feed_02_Contacts":
                        deleteFromFragment02();
                        break;

                    case "Fragment_Feed_03_Calls":
                        deleteFromFragment03();
                        break;
                }
            }
        }).show();
    }
    private void close(Context context) {

            if (context instanceof FragmentActivity) {
                Fragment currentFragment = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragmentContainer);

                switch (currentFragment.getClass().getSimpleName()) {
                    case "Fragment_Feed_01_Inbox":
                        closeFromFragment01();
                        break;

                    case "Fragment_Feed_02_Contacts":
                        closeFromFragment02();
                        break;

                    case "Fragment_Feed_03_Calls":
                        closeFromFragment03();
                        break;
                }
            }
    }

    /// Delete implementations
    private void deleteFromFragment01() {
        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
        adapter.removeSelected(this);
        int count = adapter.getSelectedPositions().size();
        notifyAnimation(count, "Chat Deleted", "Chats Deleted");
    }
    private void deleteFromFragment02() {
        RV_Feed_02_Contact_Adapter contactAdapter = Repo_Contact.getFeedAdapter();
        contactAdapter.removeSelected(this);
        int count = contactAdapter.getSelectedPositions().size();
        notifyAnimation(count, "Contact Deleted", "Contacts Deleted");
    }
    private void deleteFromFragment03() {
        RV_Feed_03_Calls_Adapter callsAdapter = Repo_Calls_History.getFeedAdapter();
        callsAdapter.removeSelected();
        int count = callsAdapter.getSelectedPositions().size();
        notifyAnimation(count, "Call Deleted", "Calls Deleted");
    }

    /// Git redirection
    private void startGitActivity(Context context) {
        startActivity(new Intent(context, Activity_06_Git_WebHook.class));
    }

    /// Methods to display OptionsBar with fragment-specific options
    public void showOptionsBarFragment01() {
        hideDefaultOptionBar();
        bind.ImageButtonArchived.setVisibility(VISIBLE);
        bind.close.setVisibility(VISIBLE);
        bind.favorite.setVisibility(VISIBLE);
        bind.trash.setVisibility(VISIBLE);
        cancelNotifyAnimation();
    }
    public void showOptionsBarFragment02() {
        hideDefaultOptionBar();
        bind.trash.setVisibility(VISIBLE);
        bind.close.setVisibility(VISIBLE);
        cancelNotifyAnimation();
    }
    public void showOptionsBarFragment03() {
        hideDefaultOptionBar();
        bind.trash.setVisibility(VISIBLE);
        bind.close.setVisibility(VISIBLE);
        cancelNotifyAnimation();
    }
    public void showOptionsBarFragment04() {
        hideDefaultOptionBar();
        bind.ImageButtonUnarchived.setVisibility(VISIBLE);
        bind.close.setVisibility(VISIBLE);
        bind.favorite.setVisibility(INVISIBLE);
        bind.trash.setVisibility(VISIBLE);
        cancelNotifyAnimation();
    }

    /// Methods to hide Default OptionsBar
    public void hideDefaultOptionBar(){
        bind.git.setVisibility(GONE);
        bind.gitNotification.setVisibility(GONE);
    }
    public void hideOptionsBar() {
        bind.git.setVisibility(VISIBLE);

        bind.favorite.setVisibility(GONE);
        bind.ImageButtonArchived.setVisibility(GONE);
        bind.close.setVisibility(GONE);
        bind.ImageButtonUnarchived.setVisibility(GONE);
        bind.trash.setVisibility(GONE);
    }

    /// Options Implementations
    private void favorite() {
        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
        adapter.toggleFavoriteForSelected();
        int count = adapter.getSelectedPositions().size();
        notifyAnimation(count, "Chat Favorited", "Chats Favorited");

        adapter.clearSelection();
    }
    private void archive() {

        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();

        if (adapter != null) {

            List<Integer> selectedPositions = new ArrayList<>(adapter.getSelectedPositions());

            List<Entity_Pv_Chat> toArchive = new ArrayList<>();
            for (int pos : selectedPositions) {
                if (pos >= 0 && pos < Repo_Chat.getChats().size()) {
                    toArchive.add(Repo_Chat.getChats().get(pos));
                }
            }

            Repo_Archived_Chats.addArchivedChat(toArchive);

            adapter.removeSelected(this);

            int count = adapter.getSelectedPositions().size();
            notifyAnimation(count, "Chat Archived", "Chats Archived");
        }
    }
    private void unarchive() {
        RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
        if (adapter != null) {
            List<Integer> selectedPositions = new ArrayList<>(adapter.getSelectedPositions());
            List<Entity_Pv_Chat> toUnarchive = new ArrayList<>();

            for (int pos : selectedPositions) {
                if (pos >= 0 && pos < Repo_Archived_Chats.getArchivedChats().size()) {
                    toUnarchive.add(Repo_Archived_Chats.getArchivedChats().get(pos));
                }
            }

            for (Entity_Pv_Chat chat : toUnarchive) {
                Repo_Archived_Chats.removeArchivedChat(chat);
                Repo_Chat.addChat(chat);
            }

            adapter.removeSelected(this);

            int count = toUnarchive.size();
            notifyAnimation(count, "Chat Unarchived", "Chats Unarchived");
        }
    }

    /// Updates NavBar Badges
    public void updateUnreadCount() {
        int unreadCount = dbHelper.getUnreadCount();

        if (unreadCount != 0) {
            bind.countUnread.setVisibility(View.VISIBLE);
            bind.countUnread.setText(String.valueOf(unreadCount));
        } else {
            bind.countUnread.setVisibility(View.GONE);
        }
    }
}