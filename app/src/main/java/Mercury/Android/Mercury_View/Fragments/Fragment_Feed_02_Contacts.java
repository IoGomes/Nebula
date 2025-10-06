package Mercury.Android.Mercury_View.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Mercury.Android.Mercury_Model.Entitys.Entity_06_Contact;
import Mercury.Android.Mercury_View.RecyclerView.RV_Feed_02_Contact_Adapter;
import Mercury.Android.R;

public class Fragment_Feed_02_Contacts extends Fragment {

    private RecyclerView recyclerContacts;
    private RV_Feed_02_Contact_Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_04_contact_list, container, false);

        recyclerContacts = view.findViewById(R.id.recyclerView);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Entity_06_Contact> contacts = new ArrayList<>();
        contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));
        contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));contacts.add(new Entity_06_Contact("123456789", "Alice"));
        contacts.add(new Entity_06_Contact("987654321", "Bob"));
        contacts.add(new Entity_06_Contact("555888777", "Carla"));

        adapter = new RV_Feed_02_Contact_Adapter(contacts);
        recyclerContacts.setAdapter(adapter);

        return view;
    }
}
