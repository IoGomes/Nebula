package Nebula.Android.Nebula_ViewModel.Server_Services;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Service_SignIn {
    public static String sendUserData(String email, String password) throws Exception {

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", password);

        Gson gson = new Gson();
        String json = gson.toJson(userData);

        URL url = new URL("https://nebula.app.br/api/user/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
            os.flush();
        }

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
