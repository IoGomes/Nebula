package Nebula.Android.Nebula_View.RV_Adapters;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;
import Nebula.Android.Nebula_View.Activities.Activity_03_Chat;
import Nebula.Android.Nebula_View.Dialogs.Dialog_Feed_01_Profile_Image;
import Nebula.Android.R;

public class RV_Feed_02_Contact_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TITLE = 1;
    private static final int TYPE_ITEM = 2;

    private List<Entity_06_Contact> contactList;
    private List<Entity_06_Contact> fullList;
    private OnContactClickListener onContactClickListener;
    private String currentSearchQuery = "";

    public interface OnContactClickListener {
        void onContactClick(Entity_06_Contact contact);
    }

    public RV_Feed_02_Contact_Adapter(List<Entity_06_Contact> contactList) {
        this.contactList = contactList;
        this.fullList = new ArrayList<>(contactList);
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.onContactClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) return TYPE_TITLE;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.rv_08_header_search, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_TITLE) {
            View view = inflater.inflate(R.layout.rv_07_header_contact_title, parent, false);
            return new TitleViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.rv_06_item_contact, parent, false);
            return new ItemViewHolder(view);
        }
    }

    public void updateData(List<Entity_06_Contact> newContacts) {
        this.contactList = newContacts;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            header.bind();
        } else if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleHolder = (TitleViewHolder) holder;
            titleHolder.bind();
        } else if (holder instanceof ItemViewHolder) {
            Entity_06_Contact contact = contactList.get(position - 2);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.tvName.setText(contact.getContactName());
            itemHolder.tvNumber.setText(contact.getContactNumber());

            itemHolder.profile.setOnClickListener(v ->
                    new Dialog_Feed_01_Profile_Image(v.getContext(), contact.getContactName()).show()
            );

            itemHolder.itemView.setOnClickListener(v ->
                v.getContext().startActivity(new Intent(v.getContext(), Activity_03_Chat.class)));
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size() + 2;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        EditText etSearch;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            etSearch = itemView.findViewById(R.id.searchGlass);
        }

        public void bind() {

        }
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.header);
        }

        public void bind() {
            tvTitle.setText("Lista de Contatos");
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