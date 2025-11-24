package Nebula.Android.Nebula_Model.Entitys;

import Nebula.Android.Nebula_Model.Services.Svc_Alert_Dialog;

@SuppressWarnings("SpellCheckingInspection")
public class Entity_User {

    private String userId;
    private String userName;
    private String userEmail;
    private String userTelefoneNumber;
    private String userPassword;
    private String confirmUserPassword;
    private Boolean userEnabled;
    private Boolean isTermsAndConditionsAceppted;
    private Boolean isPrivacyPoliticAccepted;


    public Entity_User(
            String userName,
            String userEmail,
            String userPassword,
            String confirmUserPassword,
            Boolean isTermsAndConditionsAceppted,
            Boolean isPrivacyPoliticAccepted) {

        setUserName(userName);
        setUserEmail(userEmail);
        setUserTelefoneNumber(userTelefoneNumber);
        setUserPassword(userPassword);
        setConfirmUserPassword(confirmUserPassword);
        setTermsAndConditionsAceppted(isTermsAndConditionsAceppted);
        setPrivacyPoliticAccepted(isPrivacyPoliticAccepted);
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName.isEmpty()) {
            Svc_Alert_Dialog.addMesage("Campo de Usuario está Vazio");
            setUserEnabled(false);
            return;
        }
        if(userName.length()>200 || userName.length()<3){
            Svc_Alert_Dialog.addMesage(" com Número de caracteres invalido");
            setUserEnabled(false);
            return;
        }
        this.userName = userName;
        setUserEnabled(true);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        if (userEmail.isEmpty()) {
            Svc_Alert_Dialog.addMesage("Campo de Email vazio");
            setUserEnabled(false);
            return;
        }
        if(userEmail.length()>200 || userEmail.length()<3){
            Svc_Alert_Dialog.addMesage(" com Número de caracteres invalido");
            setUserEnabled(false);
            return;
        }
        this.userEmail = userEmail;
        setUserEnabled(true);
    }

    public String getUserTelefoneNumber() {
        return userTelefoneNumber;
    }

    public void setUserTelefoneNumber(String userTelefoneNumber) {
        this.userTelefoneNumber = userTelefoneNumber;
    }

    public boolean isUserEnabled() {
        return userEnabled;
    }

    public void setUserEnabled(boolean userEnabled) {
        this.userEnabled = userEnabled;
    }

    public boolean isTermsAndConditionsAceppted() {
        return isTermsAndConditionsAceppted;
    }

    public void setTermsAndConditionsAceppted(boolean termsAndConditionsAceppted) {
        if(!termsAndConditionsAceppted){
            setUserEnabled(false);
            Svc_Alert_Dialog.addMesage("É necessario aceitar os termos e condições");
            return;
        }
        isTermsAndConditionsAceppted = termsAndConditionsAceppted;
    }

    public boolean isPrivacyPoliticAccepted() {
        return isPrivacyPoliticAccepted;
    }

    public void setPrivacyPoliticAccepted(boolean privacyPoliticAccepted) {
        if(!privacyPoliticAccepted){
            setUserEnabled(false);
            Svc_Alert_Dialog.addMesage("É necessario aceitar os termos e condições");
            return;
        }
        isPrivacyPoliticAccepted = privacyPoliticAccepted;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {

        if(userPassword.isEmpty()){
            Svc_Alert_Dialog.addMesage("Campo de Senha está vazia");
            return;
        }
        this.userPassword = userPassword;
    }

    public String getConfirmUserPassword() {
        return confirmUserPassword;
    }

    public void setConfirmUserPassword(String confirmUserPassword) {
        this.confirmUserPassword = confirmUserPassword;
    }
}
