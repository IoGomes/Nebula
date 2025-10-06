package Mercury.Android.Mercury_Model.UseCases;

import Mercury.Android.Mercury_Model.Services.AlertDialog;
import Mercury.Android.Mercury_Model.Services.Network_Checker;
import Mercury.Android.Mercury_Model.Services.Service_Permission;

public class UseCase_Voice_Call {

    private boolean isEnabled;
    private final Service_Permission servicePermission;
    private final Network_Checker networkChecker;

    // Construtor
    public UseCase_Voice_Call(Service_Permission servicePermission, Network_Checker networkChecker) {
        this.servicePermission = servicePermission;
        this.networkChecker = networkChecker;
        this.isEnabled = false;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return hasAllPermissionsAndAccess();
    }


    public boolean hasAllPermissionsAndAccess() {
        if (!servicePermission.hasRecordAudioPermission()) {
            AlertDialog.addMesage("Sem permissão para acessar a câmera");
            return false;
        }

        if (!servicePermission.hasWifiPermission()) {
            AlertDialog.addMesage("Sem permissão para acessar Wi-Fi");
            return false;
        }

        if (!networkChecker.hasWifiAccess()) {
            AlertDialog.addMesage("Sem conexão com a internet");
            return false;
        }

        return true;
    }
}
