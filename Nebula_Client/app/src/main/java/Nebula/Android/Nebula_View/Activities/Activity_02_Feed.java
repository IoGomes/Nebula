package Nebula.Android.Nebula_View.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_Model.Repository.Repo_Archived_Chats;
import Nebula.Android.Nebula_Model.Repository.Repo_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Confirm_Chat_Delection;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_01_Inbox;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_02_Contacts;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_03_Calls;
import Nebula.Android.Nebula_View.Fragments.Fragment_Feed_04_Archived;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.Nebula_View.Utils.NavBar_Inserts;
import Nebula.Android.R;
import Nebula.Android.databinding.Act02FeedBinding;

/// @author Ítalo Oliveira Gomes

/// Activity principal do Feed
@SuppressWarnings("SpellCheckingInspection")
public class Activity_02_Feed extends AppCompatActivity {

    private final Fragment fragment01 = new Fragment_Feed_01_Inbox();
    private final Fragment fragment02 = new Fragment_Feed_02_Contacts();
    private final Fragment fragment03 = new Fragment_Feed_03_Calls();
    private final Fragment fragment04 = new Fragment_Feed_04_Archived();
    private ImageButton ultimoImageButtonClicado = null;
    private Act02FeedBinding binding;
    Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideNotifyRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceBundle);

        binding = Act02FeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        Objects.requireNonNull(getSupportActionBar()).hide();

        replaceFragment(fragment01);

        binding.git.setOnClickListener(v ->
                startActivity(new Intent(this, Activity_06_Web.class)));

        binding.trash.setOnClickListener(v -> {
            new Dialog_Feed_Confirm_Chat_Delection(v.getContext(), () -> {
                RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();
                if (adapter != null) {
                    adapter.removeSelected();
                    int count = adapter.getSelectedPositions().size();
                    binding.notify.setText(count + (count == 1 ? " Chat Deleted" : " Chats Deleted"));
                    binding.notify.setVisibility(View.VISIBLE);
                    binding.notify.startAnimation(
                            AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slide_in_right)
                    );

                    hideNotifyRunnable = new Runnable() {
                        @Override
                        public void run() {
                            binding.notify.startAnimation(
                                    AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slide_out_right)
                            );
                            binding.notify.setVisibility(View.GONE);
                        }
                    };

                    handler.postDelayed(hideNotifyRunnable, 2000); // esconde depois de 2s
                    hideOptionsBar();
                }
            }).show();
        });

        binding.ImageButtonArchived.setOnClickListener(v -> {
            RV_Feed_01_Chat_Adapter adapter = Repo_Chat.getFeedAdapter();

            if (adapter != null) {

                List<Integer> selectedPositions = new ArrayList<>(adapter.getSelectedPositions());

                List<Entity_02_Chat_Session> toArchive = new ArrayList<>();
                for (int pos : selectedPositions) {
                    if (pos >= 0 && pos < Repo_Chat.getChats().size()) {
                        toArchive.add(Repo_Chat.getChats().get(pos));
                    }
                }

                Repo_Archived_Chats.addArchivedChat(toArchive);

                adapter.removeSelected();

                int count = adapter.getSelectedPositions().size();
                binding.notify.setText(count + (count == 1 ? " Chat Archived" : " Chats Archived"));

                binding.notify.setVisibility(View.VISIBLE);
                binding.notify.startAnimation(
                        AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slide_in_right)
                );

                hideNotifyRunnable = new Runnable() {
                    @Override
                    public void run() {
                        binding.notify.startAnimation(
                                AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slide_out_right)
                        );
                        binding.notify.setVisibility(View.GONE);
                    }
                };

                handler.postDelayed(hideNotifyRunnable, 2000);


                hideOptionsBar();
            }
        });



        binding.close.setOnClickListener(v -> {

            if (fragment01 instanceof Fragment_Feed_01_Inbox) {
                Fragment_Feed_01_Inbox inboxFragment = (Fragment_Feed_01_Inbox) fragment01;
                RV_Feed_01_Chat_Adapter adapter = inboxFragment.getAdapter();
                if (adapter != null) {
                    adapter.clearSelection();
                }
            }
            hideOptionsBar();
        });

        changeButtonBg();
    }

    public void changeButtonBg() {
        int[] botoesIds = {
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

        for (int id : botoesIds) {
            LinearLayout btn = findViewById(id);
            btn.setOnClickListener(v -> {
                int imageButtonId = layoutParaBotaoMap.get(v.getId());
                ImageButton imageButton = findViewById(imageButtonId);
                selecionarBotao(imageButton);
                hideOptionsBar();
                replaceFragment(fragmentMap.get(v.getId()));
            });
        }

        ImageButton imageButtonInbox = findViewById(layoutParaBotaoMap.get(R.id.button_inbox));
        selecionarBotao(imageButtonInbox);
        replaceFragment(fragmentMap.get(R.id.button_inbox));
    }

    private void selecionarBotao(ImageButton imageButton) {
        if (imageButton != null) {

            if (ultimoImageButtonClicado != null && ultimoImageButtonClicado != imageButton) {
                ultimoImageButtonClicado.setBackground(null);
            }
            imageButton.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_selected_highlight));
            ultimoImageButtonClicado = imageButton;
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void showOptionsBar() {
        binding.ImageButtonArchived.setVisibility(VISIBLE);
        binding.close.setVisibility(VISIBLE);
        binding.trash.setVisibility(VISIBLE);
        binding.git.setVisibility(GONE);
        binding.gitNotification.setVisibility(GONE);
        binding.notify.clearAnimation();
        binding.notify.setVisibility(View.GONE);
        handler.removeCallbacks(hideNotifyRunnable);
    }

    public void hideOptionsBar() {
        binding.ImageButtonArchived.setVisibility(GONE);
        binding.close.setVisibility(GONE);
        binding.trash.setVisibility(GONE);
        binding.git.setVisibility(VISIBLE);
    }
}