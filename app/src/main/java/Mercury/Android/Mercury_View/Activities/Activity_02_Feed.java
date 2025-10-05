package Mercury.Android.Mercury_View.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import Mercury.Android.Mercury_View.Fragments.Fragment_Feed_01_Inbox;
import Mercury.Android.Mercury_View.Fragments.Fragment_Feed_02_Contacts;
import Mercury.Android.Mercury_View.Fragments.Fragment_Feed_03_Calls;
import Mercury.Android.Mercury_View.Fragments.Fragment_Feed_04_Archived;
import Mercury.Android.Mercury_View.Utils.NavBar_Inserts;
import Mercury.Android.R;
import Mercury.Android.databinding.Activity02FeedBinding;

/// @author Ítalo Oliveira Gomes

@SuppressWarnings("SpellCheckingInspection")
public class Activity_02_Feed extends AppCompatActivity {

    Fragment fragment01 = new Fragment_Feed_01_Inbox();
    Fragment fragment02 = new Fragment_Feed_02_Contacts();
    Fragment fragment03 = new Fragment_Feed_03_Calls();
    Fragment fragment04 = new Fragment_Feed_04_Archived();

    private Activity02FeedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {

        setTheme(androidx.appcompat.R.style.Theme_AppCompat);

        super.onCreate(savedInstanceBundle);

        binding = Activity02FeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        View rootLayout = findViewById(R.id.root);
        NavBar_Inserts.adjustPaddingForNavigationBar(rootLayout, this);

        Objects.requireNonNull(getSupportActionBar()).hide();

        replaceFragment(fragment01);

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
                replaceFragment(fragmentMap.get(v.getId()));
            });
        }

        ImageButton imageButtonInbox = findViewById(layoutParaBotaoMap.get(R.id.button_inbox));
        selecionarBotao(imageButtonInbox);
        replaceFragment(fragmentMap.get(R.id.button_inbox));
    }

    private ImageButton ultimoImageButtonClicado = null;

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
}


