package Nebula.Android.Nebula_Model.UseCases;

import Nebula.Android.Nebula_Model.Entitys.Entity_01_User;
import Nebula.Android.Nebula_Model.Services.AlertDialog;
import Nebula.Android.Nebula_Model.Services.Network_Checker;
import Nebula.Android.Nebula_Model.Services.Service_Permission;

@SuppressWarnings("SpellCheckingInspection")
public class UseCase_02_Register {

    private Boolean isUseCaseEnabled = false;
    private final Entity_01_User user;
    private final Service_Permission servicePermission;
    private final Network_Checker networkChecker;

    public UseCase_02_Register(String userName,
                               String userEmail,
                               String userTelefoneNumber,
                               String userPassword,
                               String confirmUserPassword,
                               Boolean isTermsAndConditionsAccepted,
                               Boolean isPrivacyPoliticAccepted,
                               Network_Checker networkChecker,
                               Service_Permission servicePermission) {


        this.user = new Entity_01_User(
                userName,
                userEmail,
                userTelefoneNumber,
                userPassword,
                confirmUserPassword,
                isTermsAndConditionsAccepted,
                isPrivacyPoliticAccepted
        );
        this.networkChecker = networkChecker;
        this.servicePermission = servicePermission;
    }

    public Boolean isEnabled() {
        if (!hasAllPermissionsAndAccess()) {
            return false;
        }
        if (!user.isUserEnabled()){
            return false;
        }
        return true;
    }

    public boolean hasAllPermissionsAndAccess() {

        if (!servicePermission.hasWifiPermission()) {
            AlertDialog.addMesage("Sem permissão para acessar wifi");
            return false;
        }

        if (!networkChecker.hasWifiAccess()) {
            AlertDialog.addMesage("Sem conexão com a internet");
            return false;
        }

        return true;
    }
}
