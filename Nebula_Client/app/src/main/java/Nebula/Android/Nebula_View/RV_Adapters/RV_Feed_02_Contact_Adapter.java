package Nebula.Android.Nebula_View.RV_Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Nebula.Android.Nebula_Data.Repository.Repo_Contact;
import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.Nebula_Model.Services.Svc_Search_Query;
import Nebula.Android.Nebula_View.Activities.Activity_02_Feed;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_Profile_Image;
import Nebula.Android.R;

/// @author Ítalo Oliveira Gomes
public class RV_Feed_02_Contact_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RV_Feed_02_Contact_Adapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TITLE = 1;
    private static final int TYPE_ITEM = 2;

    private List<Entity_Contact> contactList;
    private List<Entity_Contact> fullList;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private String currentSearchQuery = "";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public enum Adapter_Mode {
        MODE_A,
        /// Modo Adapter Recycler
        MODE_B /// Modo Adapter Recycler Apartir do FeedFragment
    }

    private Adapter_Mode rvFeed02ContactMode;

    public RV_Feed_02_Contact_Adapter(List<Entity_Contact> contactList, Adapter_Mode mode) {
        this.contactList = contactList;
        this.fullList = new ArrayList<>(contactList);
        this.rvFeed02ContactMode = mode;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) return TYPE_TITLE;
        return TYPE_ITEM;
    }

    private void bindContactData(ItemViewHolder holder, Entity_Contact contacts) {

        String name = contacts.getContactName() != null ? contacts.getContactName() : "Sem nome";
        String number = contacts.getContactNumber() != null ? contacts.getContactNumber() : "Sem número";

        if (currentSearchQuery == null || currentSearchQuery.trim().isEmpty()) {
            holder.tvName.setText(name);
            holder.tvNumber.setText(number);
            return;
        }

        String query = currentSearchQuery.trim();

        String highlightedName = Svc_Search_Query.highlightTextHtml(
                name,
                query,
                "#EC407A"
        );

        // Destacar número
        String highlightedNumber = Svc_Search_Query.highlightTextHtml(
                number,
                query,
                "#EC407A"
        );

        holder.tvName.setText(Html.fromHtml(highlightedName, Html.FROM_HTML_MODE_LEGACY));
        holder.tvNumber.setText(Html.fromHtml(highlightedNumber, Html.FROM_HTML_MODE_LEGACY));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.rv_08_header_search, parent, false);
            return new HeaderViewHolder(view, this);
        } else if (viewType == TYPE_TITLE) {
            View view = inflater.inflate(R.layout.rv_07_header_contact_title, parent, false);
            return new TitleViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.rv_06_item_contact, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            header.bind();
            if (rvFeed02ContactMode == Adapter_Mode.MODE_A) {
                header.etSearch.setBackground(
                        ContextCompat.getDrawable(header.itemView.getContext(),
                                R.drawable.bg_main_searchfield)
                );
            } else {
                header.etSearch.setBackground(
                        ContextCompat.getDrawable(holder.itemView.getContext(),
                                R.drawable.bg_searchfield)
                );
            }

        } else if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleHolder = (TitleViewHolder) holder;
            titleHolder.bind(contactList.size());

        } else if (holder instanceof ItemViewHolder) {

            int index = position - 2;
            Entity_Contact contact = contactList.get(index);

            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            bindContactData(itemHolder, contact);

            if (selectedPositions.contains(position)) {
                itemHolder.itemView.setBackgroundResource(R.drawable.bg_selected_chat);
            } else {
                itemHolder.itemView.setBackgroundResource(0);
            }

            if (rvFeed02ContactMode == Adapter_Mode.MODE_B) {
                itemHolder.itemView.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, Activity_03_Chat.class);
                    intent.putExtra("CHAT_POSITION", index);
                    intent.putExtra("ChatWith", contact.getContactName());
                    intent.putExtra("ContactNumber", contact.getContactNumber());
                    context.startActivity(intent);
                });
            }

            if (rvFeed02ContactMode == Adapter_Mode.MODE_A) {
                itemHolder.profile.setOnClickListener(v ->
                        new Dialog_Feed_Profile_Image(v.getContext(), contact.getContactName()).show()
                );
            }

            if (rvFeed02ContactMode == Adapter_Mode.MODE_A) {
                itemHolder.itemView.setOnLongClickListener(v -> {

                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition == RecyclerView.NO_POSITION) return true;

                    if (selectedPositions.contains(currentPosition)) {
                        selectedPositions.remove(currentPosition);
                        v.setBackgroundResource(0);
                    } else {
                        selectedPositions.add(currentPosition);
                        v.setBackgroundResource(R.drawable.bg_selected_chat);
                    }

                    if (v.getContext() instanceof Activity_02_Feed) {
                        Activity_02_Feed feed = (Activity_02_Feed) v.getContext();

                        if (selectedPositions.isEmpty()) feed.hideOptionsBar();
                        else feed.showOptionsBarFragment02();

                    }
                    return true;
                });
            }
        }
    }

    private void toggleSelection(int position, View view) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(position);
            view.setBackgroundResource(0);
        } else {
            selectedPositions.add(position);
            view.setBackgroundResource(R.drawable.bg_selected_chat);
        }

        if (view.getContext() instanceof Activity_02_Feed) {

            Activity_02_Feed feed = (Activity_02_Feed) view.getContext();

            if (selectedPositions.isEmpty()) feed.hideOptionsBar();
            else feed.showOptionsBarFragment03();
        }
    }

    public void removeSelected(Context context) {
        mainHandler.post(() -> {

            // Converter e ordenar em ordem reversa
            List<Integer> sortedPositions = new ArrayList<>(selectedPositions);
            Collections.sort(sortedPositions, Collections.reverseOrder());

            Log.d(TAG, "Removendo posições: " + sortedPositions);

            for (int adapterPosition : sortedPositions) {

                int contactIndex = adapterPosition - 2;

                if (contactIndex < 0 || contactIndex >= contactList.size()) {
                    Log.w(TAG, "Índice inválido: " + contactIndex);
                    continue;
                }

                // Obter o contato
                Entity_Contact contact = contactList.get(contactIndex);

                Repo_Contact.removeContact(contact, adapterPosition);

                // ⭐ Remover das listas locais DEPOIS
                contactList.remove(contact);
                fullList.remove(contact);
            }

            // Limpar seleções
            selectedPositions.clear();

            // Atualizar UI
            if (context instanceof Activity_02_Feed) {
                Activity_02_Feed feed = (Activity_02_Feed) context;
                feed.hideOptionsBar();
            }
        });
    }


    @Override
    public int getItemCount() {
        int count = contactList.size() + 2;
        return count;
    }

    public Set<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void filter(String query) {
        currentSearchQuery = query;
        Log.d(TAG, "🔍 Filtrando por: '" + query + "'");

        if (query.isEmpty()) {
            contactList = new ArrayList<>(fullList);
        } else {
            List<Entity_Contact> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase().trim();

            for (Entity_Contact contact : fullList) {
                if (contact.getContactName().toLowerCase().contains(lowerCaseQuery) ||
                        contact.getContactNumber().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(contact);
                }
            }
            contactList = filteredList;
        }

        notifyDataSetChanged();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText etSearch;
        RV_Feed_02_Contact_Adapter adapter;

        public HeaderViewHolder(@NonNull View itemView, RV_Feed_02_Contact_Adapter adapter) {
            super(itemView);
            this.adapter = adapter;
            etSearch = itemView.findViewById(R.id.searchGlass);
        }

        public void bind() {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.header);
        }

        public void bind(int contactCount) {
            tvTitle.setText("Lista de Contatos (" + contactCount + ")");
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;
        ImageButton profile;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.contactName);
            tvNumber = itemView.findViewById(R.id.number);
            profile = itemView.findViewById(R.id.profile_photo);
        }
    }
}