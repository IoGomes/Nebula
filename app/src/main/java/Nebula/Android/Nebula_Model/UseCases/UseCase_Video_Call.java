package Nebula.Android.Nebula_Model.UseCases;

import Nebula.Android.Nebula_Model.Services.AlertDialog;
import Nebula.Android.Nebula_Model.Services.Network_Checker;
import Nebula.Android.Nebula_Model.Services.Service_Permission;

public class UseCase_Video_Call {

    private boolean isEnabled;
    private final Service_Permission servicePermission;
    private final Network_Checker networkChecker;

    // Construtor
    public UseCase_Video_Call(Service_Permission servicePermission, Network_Checker networkChecker) {
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
        if (!servicePermission.hasCameraPermission()) {
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