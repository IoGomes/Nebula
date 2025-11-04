package Nebula.Android.Nebula_Model.Services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthAPI_Interface {

    @POST("/register")
    Call<Auth_response> register(@Body LoginRegisterRequest request);

    @POST("/login")
    Call<Auth_response> login(@Body LoginRegisterRequest request);

}
