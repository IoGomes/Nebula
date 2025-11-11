package Nebula.Android.Nebula_Model.UseCases;

import Nebula.Android.Nebula_Model.Services.Svc_Alert_Dialog;
import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;

public class UseCase_Video_Call {

    private boolean isEnabled;
    private final Svc_Permission servicePermission;
    private final Svc_Network_Checker networkChecker;

    // Construtor
    public UseCase_Video_Call(Svc_Permission servicePermission, Svc_Network_Checker networkChecker) {
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
            Svc_Alert_Dialog.addMesage("Sem permissão para acessar a câmera");
            return false;
        }

        if (!servicePermission.hasWifiPermission()) {
            Svc_Alert_Dialog.addMesage("Sem permissão para acessar Wi-Fi");
            return false;
        }

        if (!networkChecker.hasWifiAccess()) {
            Svc_Alert_Dialog.addMesage("Sem conexão com a internet");
            return false;
        }

        return true;
    }
}