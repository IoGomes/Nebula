package Nebula.Android.Nebula_View.RV_Adapters;

// Removido import estático - será acessado via Context

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_04_Incoming_Call;
import Nebula.Android.R;

public class RV_Feed_01_Chat_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Entity_02_Chat_Session> chats;
    private List<Entity_02_Chat_Session> filteredChats;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private int selectedCategoryId = R.id.all_category;
    private String currentSearchQuery = "";

    // controle de seleção
    private final Set<Integer> selectedPositions = new HashSet<>();
    private boolean isSelectionMode = false; // Modo seleção ativado?

    public RV_Feed_01_Chat_Adapter(List<Entity_02_Chat_Session> chats) {
        this.chats = chats;
        this.filteredChats = new ArrayList<>(chats);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_HEADER;
        else if (position == 1) return VIEW_TYPE_CATEGORY;
        else return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.rv_08_header_search, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_CATEGORY) {
            View view = inflater.inflate(R.layout.rv_09_header_category, parent, false);
            return new CategoryViewHolder(view, this);
        } else {
            View view = inflater.inflate(R.layout.rv_01_chat, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            setupSearchEditText(headerHolder.searchEditText);

        } else if (holder instanceof CategoryViewHolder) {
            // nada aqui por enquanto

        } else if (holder instanceof ChatViewHolder) {
            ChatViewHolder chatHolder = (ChatViewHolder) holder;
            int chatPosition = position - 2;

            if (chatPosition >= 0 && chatPosition < filteredChats.size()) {
                Entity_02_Chat_Session chatSession = filteredChats.get(chatPosition);

                bindChatData(chatHolder, chatSession);

                // Define aparência de acordo com seleção
                if (selectedPositions.contains(chatPosition)) {
                    chatHolder.itemView.setBackgroundResource(R.drawable.bg_selected_chat);
                } else {
                    // IMPORTANTE: Resetar para o background padrão quando não estiver selecionado
                    chatHolder.itemView.setBackgroundResource(0); // ou R.drawable.bg_chat_default se você tiver um drawable padrão
                }

                chatHolder.unreadIcon.setVisibility(chatSession.hasUnread() ? View.VISIBLE : View.GONE);

                // Clique longo -> seleciona/deseleciona
                chatHolder.itemView.setOnLongClickListener(v -> {
                    if (selectedPositions.contains(chatPosition)) {
                        selectedPositions.remove(chatPosition);
                        // Resetar o background ao desselecionar
                        v.setBackgroundResource(0);
                    } else {
                        selectedPositions.add(chatPosition);
                        v.setBackgroundResource(R.drawable.bg_selected_chat);
                    }


                    if (v.getContext() instanceof Activity_02_Feed) {
                        ((Activity_02_Feed) v.getContext()).showOptionsBar();
                    }
                    notifyItemChanged(position);
                    return true;
                });


                chatHolder.itemView.setOnClickListener(v -> {
                    chatSession.setHasUnread(false);
                    chatHolder.unreadIcon.setVisibility(View.GONE);
                    v.getContext().startActivity(new Intent(v.getContext(), Activity_03_Chat.class));
                });


                chatHolder.profileImage.setOnClickListener(v ->
                        new Dialog_Feed_04_Incoming_Call(v.getContext()).show()
                );
            }
        }
    }

    public void clearSelection() {
        selectedPositions.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    private void bindChatData(ChatViewHolder holder, Entity_02_Chat_Session chatSession) {
        String lastMessage = chatSession.getLastMessage();
        holder.lastText.setText(lastMessage != null ? lastMessage : "No messages");

        if (chatSession.getChatDate() != null && !chatSession.getChatDate().isEmpty()) {
            List<Date> dates = chatSession.getChatDate();
            Date lastDate = dates.get(dates.size() - 1);
            holder.textDate.setText(dateFormat.format(lastDate));
        } else {
            holder.textDate.setText("--:--");
        }
    }

    private void setupSearchEditText(EditText searchEditText) {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void applyFilters() {
        filteredChats.clear();

        for (Entity_02_Chat_Session chat : chats) {
            boolean matchesCategory = false;
            boolean matchesSearch = false;

            if (selectedCategoryId == R.id.all_category) {
                matchesCategory = true;
            } else if (selectedCategoryId == R.id.not_read) {
                matchesCategory = chat.hasUnread();
            } else if (selectedCategoryId == R.id.favorite) {
                matchesCategory = chat.isFavorite();
            }

            if (currentSearchQuery == null || currentSearchQuery.trim().isEmpty()) {
                matchesSearch = true;
            } else {
                String lowerCaseQuery = currentSearchQuery.toLowerCase().trim();
                String lastMessage = chat.getLastMessage();
                matchesSearch = lastMessage != null && lastMessage.toLowerCase().contains(lowerCaseQuery);
            }

            if (matchesCategory && matchesSearch) {
                filteredChats.add(chat);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredChats.size() + 2;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView lastText, textDate, unreadIcon;
        ImageButton profileImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            lastText = itemView.findViewById(R.id.lastText);
            textDate = itemView.findViewById(R.id.textDate);
            profileImage = itemView.findViewById(R.id.imageButton);
            unreadIcon = itemView.findViewById(R.id.notification);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            searchEditText = itemView.findViewById(R.id.searchGlass);
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final Button allCategory, notRead;

        public CategoryViewHolder(@NonNull View itemView, RV_Feed_01_Chat_Adapter adapter) {
            super(itemView);
            allCategory = itemView.findViewById(R.id.all_category);
            notRead = itemView.findViewById(R.id.not_read);

            Button[] buttons = new Button[]{allCategory, notRead};
            atualizarAparencia(buttons, adapter.selectedCategoryId);

            for (Button button : buttons) {
                button.setOnClickListener(v -> {
                    adapter.selectedCategoryId = button.getId();
                    atualizarAparencia(buttons, adapter.selectedCategoryId);
                    adapter.applyFilters();
                });
            }
        }

        private void atualizarAparencia(Button[] buttons, int selectedId) {
            for (Button button : buttons) {
                if (button.getId() == selectedId) {
                    button.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_selected_highlight));
                    button.setTextColor(0xFFF7B2CA);
                } else {
                    button.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.bg_category));
                    button.setTextColor(0xFF808080);
                }
            }
        }
    }
}