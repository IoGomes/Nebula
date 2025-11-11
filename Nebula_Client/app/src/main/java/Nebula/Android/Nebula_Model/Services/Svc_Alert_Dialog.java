package Nebula.Android.Nebula_Model.Services;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class Svc_Alert_Dialog {

    private static List<String> alertMesages = new ArrayList<>();

    public static String getAlertMesages() {
        return String.join("", alertMesages);
    }

    public static void addMesage(String mesage) {
        alertMesages.add(mesage.replaceFirst("", "\n• "));
    }

    public static void clearListMesage() {
        alertMesages.clear();
    }

    public static void setAlertMesages(List<String> alertMesages) {
        Svc_Alert_Dialog.alertMesages = alertMesages;
    }
}
