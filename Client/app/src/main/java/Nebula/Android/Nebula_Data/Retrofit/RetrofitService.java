package Nebula.Android.Nebula_Data.Retrofit;

import java.util.List;

import Nebula.Android.Nebula_Model.Entitys.Entity_02_Chat_Session;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RetrofitService {

    //Base Url
    @GET("users")
    Call<List<Entity_02_Chat_Session>> groupList();

}
