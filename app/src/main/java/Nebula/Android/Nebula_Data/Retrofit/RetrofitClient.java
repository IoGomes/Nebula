package Nebula.Android.Nebula_Data.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

    public class RetrofitClient {
        private static final String BASE_URL = "https://jsonplaceholder.typicode.com/";
        private static Retrofit retrofit;

        public static Retrofit getInstance() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }

        public static RetrofitService getApiService() {
            return getInstance().create(RetrofitService.class);
        }
    }


