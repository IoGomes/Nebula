package Nebula.Android.Nebula_View.RV_Adapters;

import static Nebula.Android.Nebula_ViewModel.Controllers.Controller_Contact.removeContactFromRepo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

    private TitleViewHolder cachedTitleViewHolder;

    public enum Adapter_Mode {
        MODE_A, /// Modo Adapter Recycler
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

        String name = contacts.getContactName();
        String number = contacts.getContactNumber();

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

        String highlightedNumber = Svc_Search_Query.highlightTextHtml(
                number,
                query,
                "#EC407A"
        );

        new Thread(() -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                holder.tvName.setText(Html.fromHtml(highlightedName, Html.FROM_HTML_MODE_LEGACY));
                holder.tvNumber.setText(Html.fromHtml(highlightedNumber, Html.FROM_HTML_MODE_LEGACY));
            });
        }).start();
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
            } else {
                header.etSearch.setBackground(
                        ContextCompat.getDrawable(holder.itemView.getContext(),
                                R.drawable.bg_searchfield)
                );
            }

        } else if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleHolder = (TitleViewHolder) holder;
            cachedTitleViewHolder = titleHolder; // ← Salva referência
            titleHolder.bind(contactList.size());

        } else if (holder instanceof ItemViewHolder) {

            int index = position - 2;

            if (index < 0 || index >= contactList.size()) {
                return;
            }

            Entity_Contact contact = contactList.get(index);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            bindContactData(itemHolder, contact);

            itemHolder.itemView.setBackgroundResource(
                    selectedPositions.contains(index) ? R.drawable.bg_selected_chat : 0
            );


            if (rvFeed02ContactMode == Adapter_Mode.MODE_A) {
                itemHolder.profile.setOnClickListener(v ->
                        new Dialog_Feed_Profile_Image(v.getContext(),
                                contact.getContactId(),
                                contact.getContactNumber(),
                                contact.getContactName(),
                                contact.getContactNumber()).show()
                );
            }

            if (rvFeed02ContactMode == Adapter_Mode.MODE_A) {
                itemHolder.itemView.setOnLongClickListener(v -> {
                    itemHolder.profile.setClickable(false);
                    toggleSelection(index, v);
                    notifyItemChanged(position);
                    return true;
                });
            }

            itemHolder.itemView.setOnClickListener(v -> {
                if (rvFeed02ContactMode == Adapter_Mode.MODE_A && !selectedPositions.isEmpty()) {
                    toggleSelection(index, v);
                }
                else
                {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, Activity_03_Chat.class);
                    intent.putExtra("RECEIVER_ID", contact.getContactId());
                    intent.putExtra("RECEIVER_NAME", contact.getContactName());
                    intent.putExtra("RECEIVER_NUMBER", contact.getContactNumber());
                    context.startActivity(intent);
                }
            });
        }
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
            new Thread(() -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    itemView.setBackgroundResource(R.drawable.bg_selected_chat);
                    itemView.setForeground(null);
                });
            }).start();
        }

        if (itemView.getContext() instanceof Activity_02_Feed) {
            Activity_02_Feed feed = (Activity_02_Feed) itemView.getContext();

            new Thread(() -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (selectedPositions.isEmpty()) feed.hideOptionsBar();
                    else feed.showOptionsBarFragment02();
                });
            }).start();
        }
    }

    public void removeSelected(Context context) {
        mainHandler.post(() -> {

            List<Integer> sortedPositions = new ArrayList<>(selectedPositions);
            Collections.sort(sortedPositions, Collections.reverseOrder());

            sortedPositions.forEach(pos -> {
                int idx = pos - 2;
                if (idx >= 0 && idx < contactList.size()) {
                    Entity_Contact contact = contactList.get(idx);
                    removeContactFromRepo(contact, pos, context);
                    contactList.remove(contact);
                    fullList.remove(contact);
                }
            });

            selectedPositions.clear();
            updateContactCount();
        });
    }

    public void updateContactCount() {
        if (cachedTitleViewHolder != null) {
            cachedTitleViewHolder.tvTitle.setText("Lista de Contatos (" + contactList.size() + ")");
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof TitleViewHolder) {
            cachedTitleViewHolder = null;
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size() + 2;
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
        updateContactCount();
    }

    public List<Entity_Contact> getContacts() {
        return new ArrayList<>(contactList);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText etSearch;
        RV_Feed_02_Contact_Adapter adapter;

        public HeaderViewHolder(@NonNull View itemView, RV_Feed_02_Contact_Adapter adapter) {
            super(itemView);
            this.adapter = adapter;
            etSearch = itemView.findViewById(R.id.searchGlass);
            setupSearch();
        }

        private void setupSearch() {
            etSearch.setOnTouchListener((v, event) -> {
                final int DRAWABLE_RIGHT = 2;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable drawableEnd = etSearch.getCompoundDrawables()[DRAWABLE_RIGHT];

                    if (drawableEnd != null && etSearch.getText().length() > 0) {
                        int drawableWidth = drawableEnd.getIntrinsicWidth();
                        int touchAreaWidth = drawableWidth + etSearch.getPaddingEnd();

                        if (event.getX() >= (etSearch.getWidth() - touchAreaWidth)) {
                            etSearch.setText("");
                            etSearch.clearFocus();

                            InputMethodManager imm = (InputMethodManager) v.getContext()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            }

                            return true;
                        }
                    }
                }
                return false;
            });

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filter(s.toString());
                    }
                    updateClearButton(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            updateClearButton("");
        }

        private void updateClearButton(CharSequence text) {
            boolean hasText = text != null && text.length() > 0;
            Drawable searchIcon = etSearch.getContext().getDrawable(R.drawable.ic_search);
            Drawable clearIcon = hasText ? etSearch.getContext().getDrawable(R.drawable.ic_close) : null;

            etSearch.setCompoundDrawablesWithIntrinsicBounds(
                    searchIcon,
                    null,
                    clearIcon,
                    null
            );
        }

        public void bind() {
            // Método bind vazio - adicione lógica se necessário
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