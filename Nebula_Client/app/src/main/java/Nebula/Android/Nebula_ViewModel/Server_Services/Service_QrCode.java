package Nebula.Android.Nebula_ViewModel.Server_Services;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Service_QrCode{
    public static String sendUserData(int userId) throws Exception {

        Log.e("Service_QrCode", "Enviando userId = " + userId);

        URL url = new URL("https://nebula.app.br/api/user/get-by-id?id=" + userId);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(false);

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

