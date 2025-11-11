package Nebula.Android.Nebula_View.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;
import Nebula.Android.Nebula_Model.Repository.Repo_Chat;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_06_Choose_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_01_Chat_Adapter;
import Nebula.Android.databinding.Frg03FeedBinding;

public class Fragment_Feed_01_Inbox extends Fragment {

    Frg03FeedBinding bind;
    private RV_Feed_01_Chat_Adapter adapter;
    private List<Entity_06_Contact> contacts;

    @Nullable
    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bind = Frg03FeedBinding.inflate(inflater, container, false);

        contacts = new ArrayList<>();

        if (Repo_Chat.getChats().isEmpty()) {
            List<String> users2 = Arrays.asList("userC", "userD");
            List<Date> date = Arrays.asList(new Date());

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat1",
                    users2,
                    date,
                    "Olá! Bem-vindo(a) ao chat do Nebula 🚀",
                    "Nebula Dev Team"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat2",
                    users2,
                    date,
                    "Versão 1.3 está no ar! 🧩",
                    "TDE 2025.2"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat3",
                    users2,
                    date,
                    "Fala pessoal, alguém conseguiu compilar o módulo novo no Android 15?",
                    "Lucas"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat4",
                    users2,
                    date,
                    "Aqui funcionou depois que limpei o cache do Gradle 👀",
                    "Marina"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat5",
                    users2,
                    date,
                    "Boa! Achei que fosse bug do SDK de novo 😂",
                    "Rafa"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat6",
                    users2,
                    date,
                    "Atualizem o plugin do Kotlin — reduziu metade dos warnings aqui.",
                    "João"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat7",
                    users2,
                    date,
                    "Alguém viu o novo dashboard no Figma? O design ficou bem fluido!",
                    "Ana"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat8",
                    users2,
                    date,
                    "Sim, lembra bastante o visual do Fleet. Minimalista e bonito.",
                    "Bruno"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat9",
                    users2,
                    date,
                    "Esse tema mais escuro combina demais com o Nebula 🌌",
                    "Marina"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat10",
                    users2,
                    date,
                    "Dev noturno agradece 😎",
                    "Lucas"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat11",
                    users2,
                    date,
                    "Vou subir o patch pro branch `feature/chat-refactor`. Façam pull antes de mexer!",
                    "Rafa"
            ));

            Repo_Chat.getChats().add(new Entity_02_Chat_Session(
                    "chat12",
                    users2,
                    date,
                    "Perfeito. Depois disso reviso o merge da API de mensagens.",
                    "João"
            ));



        }

        bind.nullMessage.setText(Html.fromHtml("No <font color='#FFFFFF'><b>Open Chats</b></font> → Tap <font color='#FFFFFF'><b>+</b></font> to start a conversation.", Html.FROM_HTML_MODE_LEGACY));

        if (Repo_Chat.getChats() == null || Repo_Chat.getChats().isEmpty()) {
            bind.nullMessage.setVisibility(View.VISIBLE);
            bind.recyclerView.setVisibility(View.GONE);
        } else {
            bind.nullMessage.setVisibility(View.GONE);
            bind.recyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new RV_Feed_01_Chat_Adapter(Repo_Chat.getChats());
        Repo_Chat.setFeedAdapter(adapter);
        bind.recyclerView.setAdapter(adapter);
        bind.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bind.recyclerView.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), Activity_03_Chat.class)));

        bind.startChat.setOnClickListener(v ->
                new Dialog_Feed_06_Choose_Contact(requireContext(), contacts).show());

        return bind.getRoot();

    }

    public RV_Feed_01_Chat_Adapter getAdapter() {
        return adapter;
    }

}