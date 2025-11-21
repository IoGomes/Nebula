package Nebula.Android.Nebula_View.RV_Adapters;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_Data.Repository.Repo_Archived_Chats;
import Nebula.Android.Nebula_Model.Services.Svc_Search_Query;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Profile_Image;
import Nebula.Android.R;

///@author Ítalo Oliveira Gomes
public class RV_Feed_04_Archived_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Entity_Pv_Chat> archivedChats;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private static final int VIEW_TYPE_FOOTER = 3;

    private int selectedCategoryId = R.id.all_category;
    private String currentSearchQuery = "";

    private final Set<Integer> selectedPositions = new HashSet<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public RV_Feed_04_Archived_Adapter(List<Entity_Pv_Chat> archivedChats) {
        this.archivedChats = archivedChats;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_HEADER;
        else if (position == 1) return VIEW_TYPE_CATEGORY;
        else if (position == getItemCount() - 1) return VIEW_TYPE_FOOTER;
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
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = inflater.inflate(R.layout.rv_01_footer_info, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.rv_01_item_chat, parent, false);
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            setupSearchEditText(headerHolder.searchEditText);

        } else if (holder instanceof CategoryViewHolder) {

        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;

            // Configuração do footer baseado no estado dos dados
            if (Repo_Archived_Chats.getArchivedChats().isEmpty()) {
                footerHolder.footerView.setVisibility(View.GONE);
            } else {
                footerHolder.footerView.setVisibility(View.VISIBLE);
                footerHolder.textView.setText(Html.fromHtml(
                        "Archived chats – <font color='#EC407A'>Long press to select and unarchive</font>",
                        Html.FROM_HTML_MODE_LEGACY
                ));
            }

        } else if (holder instanceof ChatViewHolder) {
            ChatViewHolder chatHolder = (ChatViewHolder) holder;
            int chatPosition = position - 2;

            if (chatPosition >= 0 && chatPosition < Repo_Archived_Chats.getArchivedChats().size()) {

                Entity_Pv_Chat chatSession = Repo_Archived_Chats.getArchivedChats().get(chatPosition);

                bindChatData(chatHolder, chatSession);

                if (selectedPositions.contains(chatPosition)) {
                    chatHolder.itemView.setBackgroundResource(R.drawable.bg_selected_chat);
                } else {
                    chatHolder.itemView.setBackgroundResource(0);
                }

                chatHolder.unreadIcon.setVisibility(chatSession.hasUnread() ? View.VISIBLE : View.GONE);
                chatHolder.itemView.setOnLongClickListener(v -> {

                    final int finalPosition = chatPosition;

                    if (selectedPositions.contains(finalPosition)) {
                        selectedPositions.remove(finalPosition);
                        v.setBackgroundResource(0);
                    } else {
                        selectedPositions.add(finalPosition);
                        v.setBackgroundResource(R.drawable.bg_selected_chat);
                    }

                    if (v.getContext() instanceof Activity_02_Feed) {
                        ((Activity_02_Feed) v.getContext()).showOptionsBarFragment04();
                    }
                    notifyItemChanged(position);
                    return true;
                });

                chatHolder.itemView.setOnClickListener(v -> {
                    chatSession.setHasUnread(false);
                    chatHolder.unreadIcon.setVisibility(View.GONE);

                    Intent intent = new Intent(v.getContext(), Activity_03_Chat.class);
                    intent.putExtra("CHAT_POSITION", chatPosition);
                    intent.putExtra("CHAT_ID", chatSession.getChatSessionId());
                    intent.putExtra("ChatWith", chatSession.getChatWith());
                    intent.putExtra("IS_ARCHIVED", true);
                    v.getContext().startActivity(intent);
                });

                chatHolder.profileImage.setOnClickListener(v -> {
                    new Dialog_Feed_Profile_Image(
                            chatHolder.itemView.getContext(),
                            chatSession.getChatWith()
                    ).show();
                });
            }
        }
    }

    private void bindChatData(ChatViewHolder holder, Entity_Pv_Chat chatSession) {
        String lastMessage = chatSession.getLastMessage();

        if (currentSearchQuery != null && !currentSearchQuery.trim().isEmpty()) {
            String highlightedMessage = Svc_Search_Query.highlightTextHtml(
                    lastMessage != null ? lastMessage : "No messages",
                    currentSearchQuery,
                    "#EC407A"
            );
            holder.lastText.setText(Html.fromHtml(highlightedMessage, Html.FROM_HTML_MODE_LEGACY));

            String highlightedName = Svc_Search_Query.highlightTextHtml(
                    chatSession.getChatWith(),
                    currentSearchQuery,
                    "#EC407A"
            );
            holder.contactName.setText(Html.fromHtml(highlightedName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.lastText.setText(lastMessage != null ? lastMessage : "No messages");
            holder.contactName.setText(chatSession.getChatWith());
        }

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
        // Captura valores atuais
        final String query = currentSearchQuery;
        final int categoryId = selectedCategoryId;

        // Executa busca e ordenação em background
        executor.execute(() -> {
            // Usa o SVC_Search_Query para fazer a pesquisa com filtros de categoria
            List<Entity_Pv_Chat> results = Svc_Search_Query.searchWithCategory(
                    query,
                    categoryId,
                    R.id.all_category,
                    R.id.not_read,
                    R.id.favorite
            );

            sortResults(results);

            mainHandler.post(() -> {
                Repo_Archived_Chats.getArchivedChats().clear();
                Repo_Archived_Chats.getArchivedChats().addAll(results);
                notifyDataSetChanged();
            });
        });
    }

    private void sortResults(List<Entity_Pv_Chat> chats) {
        for (int i = 0; i < chats.size() - 1; i++) {
            for (int j = 0; j < chats.size() - i - 1; j++) {
                Entity_Pv_Chat current = chats.get(j);
                Entity_Pv_Chat next = chats.get(j + 1);

                // Prioridade 1: Mensagens não lidas
                if (!current.hasUnread() && next.hasUnread()) {
                    swap(chats, j, j + 1);
                }
                // Prioridade 2: Se ambos tiverem o mesmo status, o mais recente vai primeiro
                else if (current.hasUnread() == next.hasUnread() &&
                        getLastMessageTime(current) < getLastMessageTime(next)) {
                    swap(chats, j, j + 1);
                }
            }
        }
    }

    private void swap(List<Entity_Pv_Chat> chats, int i, int j) {
        Entity_Pv_Chat temp = chats.get(i);
        chats.set(i, chats.get(j));
        chats.set(j, temp);
    }

    private long getLastMessageTime(Entity_Pv_Chat chat) {
        if (chat.getChatDate() != null && !chat.getChatDate().isEmpty()) {
            List<Date> dates = chat.getChatDate();
            return dates.get(dates.size() - 1).getTime();
        }
        return 0;
    }

    public void removeSelected() {

        List<Integer> sorted = new ArrayList<>(selectedPositions);
        Collections.sort(sorted, Collections.reverseOrder());

        for (int pos : sorted) {
            if (pos >= 0 && pos < Repo_Archived_Chats.getArchivedChats().size()) {
                Repo_Archived_Chats.getArchivedChats().remove(pos);
            }
        }

        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void unarchiveSelected() {
        List<Integer> sorted = new ArrayList<>(selectedPositions);
        Collections.sort(sorted, Collections.reverseOrder());

        for (int pos : sorted) {
            if (pos >= 0 && pos < Repo_Archived_Chats.getArchivedChats().size()) {
                Repo_Archived_Chats.unarchiveChatAt(pos);
            }
        }

        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Repo_Archived_Chats.getArchivedChats().size() + 3;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView lastText, textDate, unreadIcon, contactName;
        ImageButton profileImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            lastText = itemView.findViewById(R.id.lastText);
            textDate = itemView.findViewById(R.id.textDate);
            profileImage = itemView.findViewById(R.id.imageButton);
            unreadIcon = itemView.findViewById(R.id.notification);
            contactName = itemView.findViewById(R.id.contactName);
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

        public CategoryViewHolder(@NonNull View itemView, RV_Feed_04_Archived_Adapter adapter) {
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

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout footerView;
        TextView textView;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            footerView = itemView.findViewById(R.id.footer);
            textView = itemView.findViewById(R.id.criptografia);
        }
    }
}