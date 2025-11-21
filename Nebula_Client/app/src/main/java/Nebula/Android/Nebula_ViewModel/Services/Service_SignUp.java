package Nebula.Android.Nebula_ViewModel.Services;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Service_SignUp {

    public static String sendUserData(String username, String password, String email, String phoneNumber) throws Exception {

        Map<String, String> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("password", password);
        userData.put("email", email);
        userData.put("phone_number", phoneNumber);

        Gson gson = new Gson();
        String json = gson.toJson(userData);

        URL url = new URL("https://youlanda-undependable-compressingly.ngrok-free.dev/api/user/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
            os.flush();
        }

        int status = conn.getResponseCode();
        System.out.println("Status HTTP: " + status);

        Scanner scanner = new Scanner(conn.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        conn.disconnect();

        return response.toString();
    }
}
