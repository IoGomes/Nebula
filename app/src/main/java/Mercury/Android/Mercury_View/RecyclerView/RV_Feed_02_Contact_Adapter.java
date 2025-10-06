
package Mercury.Android.Mercury_View.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import Mercury.Android.Mercury_Model.Entitys.Entity_06_Contact;
import Mercury.Android.R;

public class RV_Feed_02_Contact_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Entity_06_Contact> contactList;
    private List<Entity_06_Contact> fullList; // para pesquisa

    public RV_Feed_02_Contact_Adapter(List<Entity_06_Contact> contactList) {
        this.contactList = contactList;
        this.fullList = new ArrayList<>(contactList);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.rv_05_search_layout, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.rv_06_contact, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            header.bind();
        } else {
            Entity_06_Contact contact = contactList.get(position - 1); // -1 por causa do header
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            itemHolder.tvName.setText(contact.getContactName());
            itemHolder.tvNumber.setText(contact.getContactNumber());
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size() + 1; // +1 para o header
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

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.contactName);
            tvNumber = itemView.findViewById(R.id.number);
        }
    }
}