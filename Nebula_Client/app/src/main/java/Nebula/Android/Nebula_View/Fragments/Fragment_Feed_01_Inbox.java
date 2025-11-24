package Nebula.Android.Nebula_View.Fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Choose_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.databinding.Frg03FeedBinding;

/// @author Ítalo Oliveira Gomes
public class Fragment_Feed_01_Inbox extends Fragment {

    private Frg03FeedBinding bind;
    private RV_Feed_01_Chat_Adapter adapter;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RV_Feed_01_Chat_Adapter getAdapter() {
        return adapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        bind = Frg03FeedBinding.inflate(inflater, container, false);

        // Recria o executor para cada instância da view
        executor = Executors.newSingleThreadExecutor();

        // Configurações leves primeiro
        setupClickListeners();
        setupEmptyMessage();

        // Configuração do RecyclerView com otimizações
        setupRecyclerView();

        // Carrega dados em background
        loadDataAsync();

        return bind.getRoot();
    }

    private void setupClickListeners() {
        bind.recyclerView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Activity_03_Chat.class)));

        bind.startChat.setOnClickListener(v ->
                new Dialog_Feed_Choose_Contact(requireContext(), Repo_Contact.getContacts()).show());
    }

    private void setupEmptyMessage() {
        bind.nullMessage.setText(Html.fromHtml(
                "No <font color='#FFFFFF'><b>Open Chats</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to start a conversation.",
                Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupRecyclerView() {
        // Otimizações do RecyclerView
        bind.recyclerView.setHasFixedSize(true);
        bind.recyclerView.setItemViewCacheSize(20);
        bind.recyclerView.setDrawingCacheEnabled(true);
        bind.recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setInitialPrefetchItemCount(10);
        bind.recyclerView.setLayoutManager(layoutManager);
    }

    private void loadDataAsync() {
        executor.execute(() -> {
            // Carrega dados em background
            boolean hasChats = !Repo_Chat.getChats().isEmpty();

            mainHandler.post(() -> {
                if (isAdded() && bind != null) {
                    // Configura adapter
                    adapter = new RV_Feed_01_Chat_Adapter(Repo_Chat.getChats());
                    bind.recyclerView.setAdapter(adapter);
                    Repo_Chat.setFeedAdapter(adapter);

                    // Configura listener
                    Repo_Chat.setOnChatsChangedListener(this::updateEmptyState);

                    // Atualiza UI
                    updateEmptyState(hasChats);
                }
            });
        });
    }

    public void updateEmptyState() {
        updateEmptyState(!Repo_Chat.getChats().isEmpty());
    }

    private void updateEmptyState(boolean hasChats) {
        if (!isAdded() || bind == null) return;

        if (hasChats) {
            bind.nullMessage.setVisibility(GONE);
            bind.recyclerView.setVisibility(VISIBLE);
            bind.stars.cancelAnimation();
            bind.stars.setVisibility(GONE);
        } else {
            bind.recyclerView.setVisibility(GONE);
            bind.nullMessage.setVisibility(VISIBLE);
            bind.stars.setVisibility(VISIBLE);
            bind.stars.playAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Limpa listener
        Repo_Chat.setOnChatsChangedListener(null);

        if (bind != null && bind.stars != null) {
            bind.stars.cancelAnimation();
        }

        // Encerra executor de forma segura
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        bind = null;
        executor = null;
    }
}