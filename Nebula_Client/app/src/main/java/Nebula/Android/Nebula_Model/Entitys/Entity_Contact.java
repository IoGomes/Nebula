package Nebula.Android.Nebula_Model.Entitys;

public class Entity_Contact {

    private String contactId;
    private String contactNumber;
    private String contactName;

    public Entity_Contact(){

    }

    public Entity_Contact(String contactNumber, String contactName) {
        this.contactNumber = contactNumber;
        this.contactName = contactName;
    }



    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}
