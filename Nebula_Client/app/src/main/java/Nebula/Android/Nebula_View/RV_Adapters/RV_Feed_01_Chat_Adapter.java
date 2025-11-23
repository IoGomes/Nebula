package Nebula.Android.Nebula_View.RV_Adapters;

import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
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

import Nebula.Android.Nebula_Data.LocalDb.DatabaseHelper;
import Nebula.Android.Nebula_Data.Repository.Repo_Chat;
import Nebula.Android.Nebula_Model.Entitys.Entity_Pv_Chat;
import Nebula.Android.Nebula_Model.Services.Svc_Search_Query;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Profile_Image;
import Nebula.Android.R;

/// @author Ítalo Oliveira Gomes
@SuppressWarnings("SpellCheckingInspection")
public class RV_Feed_01_Chat_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RV_Feed_01_Chat_Adapter";
    private final List<Entity_Pv_Chat> displayedChats;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_CHAT = 2;
    private static final int VIEW_TYPE_FOOTER = 3;

    private int selectedCategoryId = R.id.all_category;
    private String currentSearchQuery = null;

    private final Set<Integer> selectedPositions = new HashSet<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ==================== Variáveis para atualização sem flick ====================
    private CategoryViewHolder cachedCategoryViewHolder;
    private int cachedUnreadCount = 0;
    private int cachedFavoriteCount = 0;
    private int cachedGroupCount = 0;

    public RV_Feed_01_Chat_Adapter(List<Entity_Pv_Chat> chats) {
        this.displayedChats = new ArrayList<>();
        if (chats != null) {
            this.displayedChats.addAll(chats);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_HEADER;
        else if (position == 1) return VIEW_TYPE_CATEGORY;
        else if (position == getItemCount() - 1) return VIEW_TYPE_FOOTER;
        else return VIEW_TYPE_CHAT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder(
                        inflater.inflate(R.layout.rv_08_header_search, parent, false));

            case VIEW_TYPE_CATEGORY:
                return new CategoryViewHolder(
                        inflater.inflate(R.layout.rv_09_header_category, parent, false),
                        this);

            case VIEW_TYPE_CHAT:
            default:
                return new ChatViewHolder(
                        inflater.inflate(R.layout.rv_01_item_chat, parent, false));

            case VIEW_TYPE_FOOTER:
                return new FooterViewHolder(
                        inflater.inflate(R.layout.rv_01_footer_info, parent, false));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {

            case VIEW_TYPE_HEADER:
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                setupSearchEditText(headerHolder.searchEditText);
                break;

            case VIEW_TYPE_CHAT:
                ChatViewHolder chatHolder = (ChatViewHolder) holder;
                int chatPosition = position - 2;

                Entity_Pv_Chat chatSession = displayedChats.get(chatPosition);

                setupChatItem(chatHolder, chatSession, chatPosition, position);
                break;

            case VIEW_TYPE_CATEGORY:
                CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
                cachedCategoryViewHolder = categoryViewHolder; // ← Salva referência
                setupCategory(categoryViewHolder);
                break;

            case VIEW_TYPE_FOOTER:
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                setupFooter(footerHolder);
                break;

            default:
                break;
        }
    }

    private void setupFooter(FooterViewHolder footerHolder) {
        footerHolder.textView.setText(Html.fromHtml(
                "Suas mensagens pessoais <font color='#EC407A'>não</font> são protegidas com <font color='#EC407A'>criptografia de ponta a ponta</font>"
        ));
    }

    private void setupCategory(CategoryViewHolder categoryViewHolder) {
        recalculateCounts();

        categoryViewHolder.notRead.setText("Não Lidas " + cachedUnreadCount);
        categoryViewHolder.allCategory.setText("Todas " + displayedChats.size());
        categoryViewHolder.favorite.setText("Favoritadas " + cachedFavoriteCount);
        categoryViewHolder.groups.setText("Grupos " + cachedGroupCount);
    }


    private void recalculateCounts() {
        cachedUnreadCount = 0;
        cachedFavoriteCount = 0;
        cachedGroupCount = 0;

        for (Entity_Pv_Chat chat : displayedChats) {
            if (chat.hasUnread()) cachedUnreadCount++;
            if (chat.isFavorite()) cachedFavoriteCount++;
        }
    }


    public void updateCategoryCounts() {
        if (cachedCategoryViewHolder != null) {
            recalculateCounts();

            cachedCategoryViewHolder.notRead.setText("Não Lidas " + cachedUnreadCount);
            cachedCategoryViewHolder.allCategory.setText("Todas " + displayedChats.size());
            cachedCategoryViewHolder.favorite.setText("Favoritadas " + cachedFavoriteCount);
            cachedCategoryViewHolder.groups.setText("Grupos " + cachedGroupCount);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof CategoryViewHolder) {
            cachedCategoryViewHolder = null;
        }
    }

    private void setupChatItem(ChatViewHolder chatHolder, Entity_Pv_Chat chatSession, int chatPosition, int adapterPosition) {

        bindChatData(chatHolder, chatSession);

        chatHolder.itemView.setBackgroundResource(
                selectedPositions.contains(adapterPosition) ? R.drawable.bg_selected_chat : 0
        );

        chatHolder.unreadIcon.setVisibility(chatSession.hasUnread() ? View.VISIBLE : GONE);
        chatHolder.favorite.setVisibility(chatSession.isFavorite() ? View.VISIBLE : GONE);

        setupChatClickListeners(chatHolder, chatSession, chatPosition, adapterPosition);
    }

    private void setupChatClickListeners(ChatViewHolder chatHolder, Entity_Pv_Chat chatSession, int chatPosition, int adapterPosition) {

        chatHolder.itemView.setOnLongClickListener(v -> {

            int pos = adapterPosition;
            if (pos == RecyclerView.NO_POSITION) return true;

            toggleSelection(pos, v);
            return true;
        });

        chatHolder.itemView.setOnClickListener(v -> {
            if (!selectedPositions.isEmpty()) {
                toggleSelection(adapterPosition, v);
            } else openChat(v.getContext(), chatHolder, chatSession, chatPosition);
        });

        chatHolder.profileImage.setOnClickListener(v ->
                new Dialog_Feed_Profile_Image(chatHolder.itemView.getContext(), chatSession.getChatWith(), chatSession.getChatSessionId(), chatSession.getChatWithNumber()).show());
    }

    private void toggleSelection(int position, View itemView) {

        if (selectedPositions.contains(position)) {

            selectedPositions.remove(position);
            itemView.setBackgroundResource(0);

            if (selectedPositions.size() < 1) {
                TypedValue outValue = new TypedValue();
                itemView.getContext().getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackground, outValue, true
                );
                itemView.setForeground(ContextCompat.getDrawable(
                        itemView.getContext(),
                        outValue.resourceId
                ));
            }

        } else {
            selectedPositions.add(position);
            itemView.setBackgroundResource(R.drawable.bg_selected_chat);
            itemView.setForeground(null);
        }

        if (itemView.getContext() instanceof Activity_02_Feed) {
            Activity_02_Feed feed = (Activity_02_Feed) itemView.getContext();
            if (selectedPositions.isEmpty()) {
                feed.hideOptionsBar();
            } else {
                feed.showOptionsBarFragment01();
            }
        }
    }


    private void openChat(Context context, ChatViewHolder chatHolder, Entity_Pv_Chat chatSession, int position) {

        chatSession.setHasUnread(false);
        new DatabaseHelper(context).updateChat(chatSession);
        chatHolder.unreadIcon.setVisibility(GONE);

        updateCategoryCounts();

        Activity_02_Feed feed = (Activity_02_Feed) context;
        feed.updateUnreadCount();
        Intent intent = new Intent(context, Activity_03_Chat.class);
        putExtraIntents(chatSession, position, intent);
        context.startActivity(intent);
    }

    public void putExtraIntents(Entity_Pv_Chat chatSession, int position, Intent intent) {
        String chatId = chatSession.getChatSessionId();
        String chatWithNumber = chatSession.getChatWithNumber();
        intent.putExtra("CHAT_POSITION", position);
        intent.putExtra("CHAT_ID", chatId);
        intent.putExtra("ContactNumber", "12345");
        intent.putExtra("ChatWith", chatSession.getChatWith());
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    private void bindChatData(ChatViewHolder holder, Entity_Pv_Chat chatSession) {

        String lastMessage = chatSession.getLastMessage();
        String chatWith = chatSession.getChatWith();

        if (currentSearchQuery == null || currentSearchQuery.trim().isEmpty()) {
            holder.lastText.setText(lastMessage);
            holder.contactName.setText(chatWith);
            return;
        }

        String highlightedMessage = Svc_Search_Query.highlightTextHtml(lastMessage, currentSearchQuery.trim(), "#EC407A");
        String highlightedName = Svc_Search_Query.highlightTextHtml(chatWith, currentSearchQuery.trim(), "#EC407A");

        holder.lastText.setText(Html.fromHtml(highlightedMessage, Html.FROM_HTML_MODE_LEGACY));
        holder.contactName.setText(Html.fromHtml(highlightedName, Html.FROM_HTML_MODE_LEGACY));

        if (chatSession.getChatDate() != null && !chatSession.getChatDate().isEmpty()) {
            List<Date> dates = chatSession.getChatDate();
            Date lastDate = dates.get(dates.size() - 1);
            holder.textDate.setText(dateFormat.format(lastDate));
        } else holder.textDate.setText("12:49");

    }

    private void setupSearchEditText(EditText searchEditText) {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void applyFilters() {
        final String query = currentSearchQuery;
        final int categoryId = selectedCategoryId;

        executor.execute(() -> {
            List<Entity_Pv_Chat> results = Svc_Search_Query.searchWithCategory(
                    query, categoryId, R.id.all_category, R.id.not_read, R.id.favorite
            );

            mainHandler.post(() -> {
                displayedChats.clear();
                displayedChats.addAll(results);

                notifyDataSetChanged();
            });
        });
    }


    public void removeSelected(Context context) {
        mainHandler.post(() -> {

            List<Integer> sortedPositions = new ArrayList<>(selectedPositions);
            Collections.sort(sortedPositions, Collections.reverseOrder());

            for (int adapterPosition : sortedPositions) {

                int chatPosition = adapterPosition - 2;

                if (chatPosition < 0 || chatPosition >= displayedChats.size()) {
                    continue;
                }

                Entity_Pv_Chat chat = displayedChats.get(chatPosition);

                displayedChats.remove(chatPosition);

                Repo_Chat.removeChat(chat, adapterPosition);
            }

            selectedPositions.clear();
            updateCategoryCounts();

            Activity_02_Feed feed = (Activity_02_Feed) context;
            feed.updateUnreadCount();
        });
    }


    public void toggleFavoriteForSelected() {
        if (selectedPositions.isEmpty()) {
            return;
        }

        for (Integer adapterPosition : selectedPositions) {

            int chatPosition = adapterPosition - 2;

            if (chatPosition >= 0 && chatPosition < displayedChats.size()) {
                Entity_Pv_Chat chatSession = displayedChats.get(chatPosition);

                boolean newFavoriteState = !chatSession.isFavorite();
                chatSession.setFavorite(newFavoriteState);

                notifyItemChanged(adapterPosition);
            }
        }

        updateCategoryCounts();
    }

    @Override
    public int getItemCount() {
        return displayedChats.size() + 3;
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            searchEditText = itemView.findViewById(R.id.searchGlass);
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final Button allCategory, favorite, groups, notRead;

        public CategoryViewHolder(@NonNull View itemView, RV_Feed_01_Chat_Adapter adapter) {
            super(itemView);
            allCategory = itemView.findViewById(R.id.all_category);
            notRead = itemView.findViewById(R.id.not_read);
            favorite = itemView.findViewById(R.id.favorite);
            groups = itemView.findViewById(R.id.groups);

            Button[] buttons = new Button[]{allCategory, notRead, favorite, groups};
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
            for (Button btn : buttons) {
                boolean selecionado = btn.getId() == selectedId;
                btn.setBackground(ContextCompat.getDrawable(itemView.getContext(), selecionado ? R.drawable.bg_selected_highlight : R.drawable.bg_category));
                btn.setTextColor(selecionado ? 0xFFF7B2CA : 0xFF808080);
            }
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView lastText, textDate, unreadIcon, contactName;
        ImageButton profileImage;
        View favorite, selected;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            lastText = itemView.findViewById(R.id.lastText);
            textDate = itemView.findViewById(R.id.textDate);
            profileImage = itemView.findViewById(R.id.imageButton);
            unreadIcon = itemView.findViewById(R.id.notification);
            contactName = itemView.findViewById(R.id.contactName);
            favorite = itemView.findViewById(R.id.favorite);
            selected = itemView.findViewById(R.id.selected);
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