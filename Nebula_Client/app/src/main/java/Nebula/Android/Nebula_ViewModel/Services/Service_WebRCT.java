package Nebula.Android.Nebula_ViewModel.Services;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class Service_WebRCT implements SdpObserver {

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        // Sobrescreva quando necessário
    }

    @Override
    public void onSetSuccess() {
        // Sobrescreva quando necessário
    }

    @Override
    public void onCreateFailure(String error) {
        System.out.println("SDP Create Failed: " + error);
    }

    @Override
    public void onSetFailure(String error) {
        System.out.println("SDP Set Failed: " + error);
    }
}

