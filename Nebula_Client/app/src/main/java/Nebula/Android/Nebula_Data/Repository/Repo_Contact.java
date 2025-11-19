package Nebula.Android.Nebula_Data.Repository;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Data.LocalDb.DatabaseHelper;
import Nebula.Android.Nebula_Model.Entitys.Entity_Contact;
import Nebula.Android.Nebula_View.RV_Adapters.RV_Feed_02_Contact_Adapter;

public class Repo_Contact {

    private static final String TAG = "Repo_Contact";
    private static List<Entity_Contact> contacts = new ArrayList<>();

    private static RV_Feed_02_Contact_Adapter feedAdapter;
    private static DatabaseHelper dbHelper;

    public interface OnContactsChangedListener {
        void onContactsChanged();
    }

    private static OnContactsChangedListener contactsChangedListener;

    public static void initialize(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
            dbHelper.copyDatabaseIfNeeded();
            loadContactsFromDatabase();
        }
    }

    /**
     * Carrega todos os contatos do banco para a memória
     */
    public static void loadContactsFromDatabase() {
        if (dbHelper == null) {
            Log.e(TAG, "DatabaseHelper não inicializado!");
            return;
        }

        List<Entity_Contact> contactsFromDb = dbHelper.getAllContacts();
        contacts.clear();

        for (Entity_Contact contact : contactsFromDb) {
            contacts.add(contact);
        }

        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
        notifyContactsChanged();

        Log.i(TAG, "✅ " + contacts.size() + " contatos carregados do banco");
    }

    public static List<Entity_Contact> getContacts() {
        return contacts;
    }

    public static void setContacts(List<Entity_Contact> contactList) {
        contacts = contactList;
        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
        notifyContactsChanged();
    }

    public static RV_Feed_02_Contact_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    public static void setFeedAdapter(RV_Feed_02_Contact_Adapter adapter) {
        feedAdapter = adapter;
    }

    /**
     * Adiciona um contato novo no banco E na memória
     */
    public static void addContact(Entity_Contact contact) {
        if (contact != null && dbHelper != null) {
            // Primeiro insere no banco
            dbHelper.insertContact(contact);

            // Depois adiciona na lista local (o insertContact já faz isso via syncContactToRepository)
            Log.i(TAG, "✅ Contato adicionado: " + contact.getContactName());
        }
    }

    /**
     * Usado apenas quando carregando do banco (não insere novamente)
     */
    public static void addContactFromDatabase(Entity_Contact contact) {
        if (contact != null) {
            contacts.add(contact);
            if (feedAdapter != null) {
                feedAdapter.notifyItemInserted(contacts.size() - 1);
            }
            notifyContactsChanged();
        }
    }


    public static void removeContact(Entity_Contact contact, int adapterPosition) {
        if (contact == null || dbHelper == null) return;

        if (contacts.indexOf(contact) != -1) {

            contacts.remove(contact);

            if (feedAdapter != null) {
                feedAdapter.notifyItemRemoved(adapterPosition);

                feedAdapter.notifyItemRangeChanged(adapterPosition,
                        feedAdapter.getItemCount());
            }

            notifyContactsChanged();

            dbHelper.deleteContact(contact.getContactId());
        }
    }

    /**
     * Remove contato por posição
     */
    public static void removeContactAt(int position) {
        if (position >= 0 && position < contacts.size()) {
            Entity_Contact contact = contacts.get(position);
            removeContact(contact, position);
        }
    }

    /**
     * Atualiza um contato no banco E na memória
     */
    public static void updateContact(Entity_Contact contact) {
        if (contact != null && dbHelper != null) {
            // Atualiza no banco
            dbHelper.updateContact(contact);

            // Atualiza na lista local (o updateContact do DB já faz isso)
            Log.i(TAG, "✏️ Contato atualizado: " + contact.getContactName());
        }
    }

    /**
     * Limpa todos os contatos (apenas da memória, não do banco)
     */
    public static void clearContacts() {
        contacts.clear();
        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
        notifyContactsChanged();
    }

    /**
     * Sincroniza um contato específico do banco para o repositório
     */
    public static void syncContactFromDatabase(String contactId) {
        if (dbHelper != null) {
            dbHelper.syncContactToRepository(contactId);
        }
    }

    private static void notifyContactsChanged() {
        if (contactsChangedListener != null) {
            contactsChangedListener.onContactsChanged();
        }
    }

    public static void setOnContactsChangedListener(OnContactsChangedListener listener) {
        contactsChangedListener = listener;
    }
}