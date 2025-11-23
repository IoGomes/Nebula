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

    public static Repo_Contact initialize(Context context) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
            dbHelper.copyDatabaseIfNeeded();
            loadContactsFromDatabase();
        }
        return null;
    }

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
    }

    public static List<Entity_Contact> getContacts() {
        return contacts;
    }

    public static RV_Feed_02_Contact_Adapter getFeedAdapter() {
        return feedAdapter;
    }

    public static void setFeedAdapter(RV_Feed_02_Contact_Adapter adapter) {
        feedAdapter = adapter;
    }

    public static void addContact(Entity_Contact contact) {
        if (contact != null) {

            if (dbHelper != null) {
                dbHelper.insertContact(contact);
            }

            if (feedAdapter != null) {
                feedAdapter.notifyItemInserted(contacts.size()+1);
                notifyContactsChanged();
            }
        }
    }

    public static void addContactFromDatabase(Entity_Contact contact) {
        if (contact != null) {
            contacts.add(contact);
            if (feedAdapter != null) {

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

    public static void removeContactAt(int position) {
        if (position >= 0 && position < contacts.size()) {
            Entity_Contact contact = contacts.get(position);
            removeContact(contact, position);
        }
    }

    private static void notifyContactsChanged() {
        if (contactsChangedListener != null) {
            contactsChangedListener.onContactsChanged();
        }
    }
}