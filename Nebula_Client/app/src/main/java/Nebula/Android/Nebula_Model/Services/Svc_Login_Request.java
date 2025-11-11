package Nebula.Android.Nebula_Model.Services;

public class Svc_Login_Request {

    public String nome;
    public String email;
    public String senha;
    public String telefone;
    public String key;

    public Svc_Login_Request(String email, String senha, String key) {
        this.email = email;
        this.senha = senha;
        this.key = key;
    }

    public Svc_Login_Request(String nome, String email, String senha, String telefone, String key) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.key = key;
    }


}
