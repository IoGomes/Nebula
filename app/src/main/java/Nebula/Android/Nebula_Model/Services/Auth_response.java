package Nebula.Android.Nebula_Model.Services;

import com.google.gson.annotations.SerializedName;

public class Auth_response {
    public class AuthResponse {
        public boolean success;
        public String message;
        public ResponseData data;

        public class ResponseData {
            @SerializedName("userId")
            public String userId;
            public String token;
            @SerializedName("refreshToken")
            public String refreshToken;
        }
    }
    }
