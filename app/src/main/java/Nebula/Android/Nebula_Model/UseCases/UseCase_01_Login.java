package Nebula.Android.Nebula_Model.UseCases;

public class UseCase_01_Login {

    private String userEmail;
    private String userPassword;

    private Boolean loginEnabled;

    public UseCase_01_Login(String userEmail, String userPassword){
        setUserEmail(userEmail);
        setUserPassword(userPassword);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public Boolean isLoginEnabled() {
        return loginEnabled;
    }

    public void setLoginEnabled(Boolean loginEnabled) {
        loginEnabled = loginEnabled;
    }
}
