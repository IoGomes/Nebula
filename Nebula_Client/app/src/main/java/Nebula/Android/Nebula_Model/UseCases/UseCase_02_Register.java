package Nebula.Android.Nebula_Model.UseCases;

import Nebula.Android.Nebula_Model.Entitys.Entity_User;
import Nebula.Android.Nebula_Model.Services.Svc_Alert_Dialog;
import Nebula.Android.Nebula_Model.Services.Svc_Network_Checker;
import Nebula.Android.Nebula_Model.Services.Svc_Permission;

@SuppressWarnings("SpellCheckingInspection")
public class UseCase_02_Register {

    private Boolean isUseCaseEnabled = false;
    private final Entity_User user;
    private final Svc_Permission servicePermission;
    private final Svc_Network_Checker networkChecker;

    public UseCase_02_Register(String userName,
                               String userEmail,
                               String userTelefoneNumber,
                               String userPassword,
                               String confirmUserPassword,
                               Boolean isTermsAndConditionsAccepted,
                               Boolean isPrivacyPoliticAccepted,
                               Svc_Network_Checker networkChecker,
                               Svc_Permission servicePermission) {


        this.user = new Entity_User(
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
            Svc_Alert_Dialog.addMesage("Sem permissão para acessar wifi");
            return false;
        }

        if (!networkChecker.hasWifiAccess()) {
            Svc_Alert_Dialog.addMesage("Sem conexão com a internet");
            return false;
        }

        return true;
    }
}
