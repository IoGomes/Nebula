package Nebula.Android.Nebula_ViewModel.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_06_Contact;

public class ViewModel_Contacts extends ViewModel {

    private MutableLiveData<List<Entity_06_Contact>> contacts = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Entity_06_Contact>> getContacts() {
        return contacts;
    }

    public void addContact(Entity_06_Contact contact) {
        List<Entity_06_Contact> list = contacts.getValue();
        if (list != null) {
            list.add(contact);
            contacts.setValue(list);
        }
    }
}