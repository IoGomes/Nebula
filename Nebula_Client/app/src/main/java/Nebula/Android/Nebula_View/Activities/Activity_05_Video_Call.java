package Nebula.Android.Nebula_View.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import Nebula.Android.Nebula_ViewModel.Services.Service_WebRCT;
import Nebula.Android.R;
import io.socket.client.Socket;

public class Activity_05_Video_Call extends AppCompatActivity {

    private static final String TAG = "CallActivity";

    public static Socket sharedSocket;
    public static String sharedUserName;

    private Socket socket;
    private String userName;

    // ==== WebRTC ====
    private SurfaceViewRenderer localView;
    private SurfaceViewRenderer remoteView;

    private EglBase eglBase;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;

    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;

    private MediaStream localStream;

    private boolean didIOffer = false;

    private final List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_05_video_call);

        if (sharedSocket == null) {
            throw new RuntimeException("ERRO: sharedSocket é nulo! Você esqueceu de passar o socket para CallActivity.");
        }

        socket = sharedSocket;
        userName = sharedUserName;

        setupWebRTC();
        setupIceServers();
        setupViews();
        setupWebRTCListeners();

        startLocalStream();
    }

    private void setupWebRTC() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions
                        .builder(this)
                        .createInitializationOptions()
        );

        eglBase = EglBase.create();

        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(
                        eglBase.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .createPeerConnectionFactory();
    }

    private void setupIceServers() {
        iceServers.add(
                PeerConnection.IceServer
                        .builder("stun:stun.l.google.com:19302")
                        .createIceServer()
        );
    }

    private void setupViews() {
        localView = findViewById(R.id.front_preview);
        remoteView = findViewById(R.id.back_preview);

        initRenderer(localView);
        initRenderer(remoteView);
    }

    private void initRenderer(SurfaceViewRenderer view) {
        view.init(eglBase.getEglBaseContext(), null);
        view.setMirror(true);
    }


    private void startLocalStream() {
        VideoCapturer capturer = createCameraCapturer();

        VideoSource videoSource = factory.createVideoSource(capturer.isScreencast());
        capturer.initialize(null, this, videoSource.getCapturerObserver());
        capturer.startCapture(720, 1280, 30);

        localVideoTrack = factory.createVideoTrack("localVideoTrack", videoSource);
        localAudioTrack = factory.createAudioTrack("localAudioTrack",
                factory.createAudioSource(new MediaConstraints()));

        localStream = factory.createLocalMediaStream("localStream");
        localStream.addTrack(localVideoTrack);
        localStream.addTrack(localAudioTrack);

        localVideoTrack.addSink(localView);
    }

    private VideoCapturer createCameraCapturer() {
        Camera1Enumerator enumerator = new Camera1Enumerator(false);

        for (String deviceName : enumerator.getDeviceNames()) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer cap = enumerator.createCapturer(deviceName, null);
                if (cap != null) return cap;
            }
        }
        return null;
    }


    private void createPeerConnection() {
        PeerConnection.RTCConfiguration config = new PeerConnection.RTCConfiguration(iceServers);

        peerConnection = factory.createPeerConnection(config, new PeerConnection.Observer() {

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {

            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

            }

            @Override
            public void onIceCandidate(IceCandidate ice) {
                sendIceCandidate(ice);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

            }

            @Override
            public void onAddStream(MediaStream stream) {
                runOnUiThread(() -> {
                    if (!stream.videoTracks.isEmpty()) {
                        stream.videoTracks.get(0).addSink(remoteView);
                    }
                });
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {

            }

            @Override
            public void onRenegotiationNeeded() {

            }
        });

        peerConnection.addStream(localStream);
    }


    public void startCall(View v) {
        didIOffer = true;

        createPeerConnection();

        MediaConstraints mc = new MediaConstraints();
        peerConnection.createOffer(new Service_WebRCT() {
                    @Override
                    public void onCreateSuccess(SessionDescription sdp) {
                        peerConnection.setLocalDescription(new Service_WebRCT(), sdp);
                        sendOffer(sdp);
                    }
                }, mc);
    }

    private void sendOffer(SessionDescription offer) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", offer.type.canonicalForm());
            obj.put("sdp", offer.description);
            obj.put("userName", userName);

            socket.emit("newOffer", obj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setRemoteAnswer(JSONObject answerJson) {
        try {
            SessionDescription answer =
                    new SessionDescription(SessionDescription.Type.ANSWER,
                            answerJson.getString("sdp"));

            peerConnection.setRemoteDescription(new Service_WebRCT(), answer);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendIceCandidate(IceCandidate ice) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("sdpMid", ice.sdpMid);
            obj.put("sdpMLineIndex", ice.sdpMLineIndex);
            obj.put("candidate", ice.sdp);

            obj.put("didIOffer", didIOffer);
            obj.put("iceUserName", userName);

            socket.emit("sendIceCandidateToSignalingServer", obj);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRemoteIceCandidate(JSONObject json) {
        try {
            IceCandidate ice = new IceCandidate(
                    json.getString("sdpMid"),
                    json.getInt("sdpMLineIndex"),
                    json.getString("candidate")
            );
            peerConnection.addIceCandidate(ice);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupWebRTCListeners() {

        socket.on("answerResponse", args -> {
            JSONObject obj = (JSONObject) args[0];
            runOnUiThread(() -> setRemoteAnswer(obj));
        });

        socket.on("receivedIceCandidateFromServer", args -> {
            JSONObject obj = (JSONObject) args[0];
            runOnUiThread(() -> addRemoteIceCandidate(obj));
        });

        socket.on("offerResponse", args -> {
            JSONObject offerJson = (JSONObject) args[0];
            runOnUiThread(() -> handleIncomingOffer(offerJson));
        });
    }

    private void handleIncomingOffer(JSONObject offerJson) {
        try {
            didIOffer = false;

            createPeerConnection();

            SessionDescription offer = new SessionDescription(
                    SessionDescription.Type.OFFER,
                    offerJson.getString("sdp")
            );

            peerConnection.setRemoteDescription(new Service_WebRCT(), offer);

            peerConnection.createAnswer(new Service_WebRCT() {
                            @Override
                            public void onCreateSuccess(SessionDescription answer) {
                                peerConnection.setLocalDescription(new Service_WebRCT(), answer);

                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("answer", answer.description);
                                    obj.put("offererUserName", offerJson.getString("offererUserName"));

                                    socket.emit("answerToOffer", obj);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new MediaConstraints());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.off("answerResponse");
        socket.off("receivedIceCandidateFromServer");
        socket.off("offerResponse");

        localView.release();
        remoteView.release();
        eglBase.release();
    }
}
