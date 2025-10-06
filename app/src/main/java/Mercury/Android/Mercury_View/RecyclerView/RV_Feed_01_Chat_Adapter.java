package Mercury.Android.Mercury_View.RecyclerView;

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
import java.util.List;
import java.util.Locale;

import Mercury.Android.Mercury_Model.Entitys.Entity_02_Chat_Session;
import Mercury.Android.Mercury_View.Activities.Activity_03_Chat;
import Mercury.Android.Mercury_View.Dialogs.Dialog_Feed_01_Profile_Image;
import Mercury.Android.Mercury_View.Dialogs.Dialog_Feed_03_Long_Hold_Options;
import Mercury.Android.R;

public class RV_Feed_01_Chat_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Entity_02_Chat_Session> chats;
    private List<Entity_02_Chat_Session> filteredChats;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CATEGORY = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private int selectedPosition = -1;

    private int selectedCategoryId = R.id.all_category;

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
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_05_search_layout, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_header_category, parent, false);
            return new SecondViewHolder(view, this);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_01_chat, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            setupSearchEditText(headerHolder.searchEditText);

        } else if (holder instanceof SecondViewHolder) {

        } else if (holder instanceof MessageViewHolder) {
            MessageViewHolder messageHolder = (MessageViewHolder) holder;
            int chatPosition = position - 2;

            if (chatPosition >= 0 && chatPosition < filteredChats.size()) {
                Entity_02_Chat_Session chatSession = filteredChats.get(chatPosition);
                bindChatData(messageHolder, chatSession);
            }

            if (chatPosition == selectedPosition) {
                messageHolder.itemView.setBackgroundResource(R.drawable.shape_chat_selected);
            } else {
                messageHolder.itemView.setBackground(null);
            }

            messageHolder.itemView.setOnLongClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = chatPosition;

                if (previousSelected != -1) notifyItemChanged(previousSelected + 2);
                notifyItemChanged(selectedPosition + 2);

                new Dialog_Feed_03_Long_Hold_Options(v.getContext()) {{
                    setOnDismissListener(d -> {
                        int temp = selectedPosition;
                        selectedPosition = -1;
                        notifyItemChanged(temp + 2);
                    });
                }}.show();

                return true;
            });

            messageHolder.itemView.setOnClickListener(v ->
                    v.getContext().startActivity(new Intent(v.getContext(), Activity_03_Chat.class))
            );

            messageHolder.profileImage.setOnClickListener(v ->
                    new Dialog_Feed_01_Profile_Image(v.getContext()).show()
            );
        }
    }

    private void bindChatData(MessageViewHolder holder, Entity_02_Chat_Session chatSession) {
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
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterChats(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }
    }

    private void filterChats(String query) {
        filteredChats.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredChats.addAll(chats);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Entity_02_Chat_Session chat : chats) {
                String lastMessage = chat.getLastMessage();
                if (lastMessage != null && lastMessage.toLowerCase().contains(lowerCaseQuery)) {
                    filteredChats.add(chat);
                }
            }
        }

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredChats.size() + 2;
    }

    private void selecionarCategoria(Button button) {
        selectedCategoryId = button.getId();

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView lastText, textDate;
        ImageButton profileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            lastText = itemView.findViewById(R.id.lastText);
            textDate = itemView.findViewById(R.id.textDate);
            profileImage = itemView.findViewById(R.id.imageButton);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText searchEditText;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            searchEditText = itemView.findViewById(R.id.searchGlass);
        }
    }

    static class SecondViewHolder extends RecyclerView.ViewHolder {

        private final Button allCategory, notRead, favorite, groups;

        public SecondViewHolder(@NonNull View itemView, RV_Feed_01_Chat_Adapter adapter) {
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
