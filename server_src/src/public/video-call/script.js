// src/public/video-call/script.js

const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('userName') || "Usuario" + Math.floor(Math.random() * 100);
const userId = urlParams.get('userId') || Math.floor(Math.random() * 100000).toString();
const initialTargetId = urlParams.get('targetId');

let connectedPeerId = null;

const socket = io('/', {
    query: { userName, userId }
});

let localStream;
let peerConnection;
let candidateQueue = [];

// UI Elements
const localVideoEl = document.getElementById('local-video');
const remoteVideoEl = document.getElementById('remote-video');
const callBtn = document.getElementById('call');
const hangupBtn = document.getElementById('hangup');
const waitingEl = document.getElementById('waiting');
const userNameDisplay = document.getElementById('user-name');

const toggleCameraBtn = document.getElementById('toggle-camera'); 
const toggleMicBtn = document.getElementById('toggle-mic');

const cameraOnIcon = document.getElementById('camera-on-icon');
const cameraOffIcon = document.getElementById('camera-off-icon');
const micOnIcon = document.getElementById('mic-on-icon');
const micOffIcon = document.getElementById('mic-off-icon');

// Remote State Elements
const remoteAvatar = document.getElementById('remote-avatar');
const remoteMicStatus = document.getElementById('remote-mic-status');

userNameDisplay.innerText = "Eu: " + userName;

const peerConfiguration = {
    iceServers: [
        { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] }
    ]
};

const initMedia = async () => {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
        localStream = stream;
        localVideoEl.srcObject = stream;
    } catch (err) {
        console.error("Erro ao acessar câmera:", err);
        alert("Erro: Câmera bloqueada ou não encontrada.");
    }
}

initMedia();

// --- HELPER: Emit Media State ---
// Envia para o servidor que mudamos o estado (Ligado/Desligado)
const emitMediaState = (isCameraOn, isMicOn) => {
    // Precisamos de um target. Se connectedPeerId for nulo, não há ninguém para avisar ainda.
    const target = connectedPeerId || initialTargetId;
    if (target) {
        socket.emit('mediaStateChange', {
            targetUserId: target,
            camera: isCameraOn,
            mic: isMicOn
        });
    }
};

// --- TOGGLE CAMERA ---
const toggleCamera = () => {
    if (localStream) {
        const videoTracks = localStream.getVideoTracks();
        if (videoTracks.length > 0) {
            const track = videoTracks[0];
            track.enabled = !track.enabled;
            
            const isCameraOn = track.enabled;
            
            // Update UI Local
            cameraOnIcon.style.display = isCameraOn ? 'block' : 'none';
            cameraOffIcon.style.display = isCameraOn ? 'none' : 'block';
            toggleCameraBtn.classList.toggle('disabled', !isCameraOn);
            
            // Check Mic State to send complete info
            const isMicOn = localStream.getAudioTracks()[0]?.enabled ?? true;
            emitMediaState(isCameraOn, isMicOn);
        }
    }
};

// --- TOGGLE MIC ---
const toggleMic = () => {
    if (localStream) {
        const audioTracks = localStream.getAudioTracks();
        if (audioTracks.length > 0) {
            const track = audioTracks[0];
            track.enabled = !track.enabled;
            
            const isMicOn = track.enabled;
            
            // Update UI Local
            micOnIcon.style.display = isMicOn ? 'block' : 'none';
            micOffIcon.style.display = isMicOn ? 'none' : 'block';
            toggleMicBtn.classList.toggle('disabled', !isMicOn);
            
            // Check Camera State to send complete info
            const isCameraOn = localStream.getVideoTracks()[0]?.enabled ?? true;
            emitMediaState(isCameraOn, isMicOn);
        }
    }
};

const createPeerConnection = () => {
    if (peerConnection) peerConnection.close();

    peerConnection = new RTCPeerConnection(peerConfiguration);

    if (localStream) {
        localStream.getTracks().forEach(track => {
            peerConnection.addTrack(track, localStream);
        });
    }

    peerConnection.ontrack = (event) => {
        console.log("Vídeo remoto recebido!");
        waitingEl.style.display = 'none';
        remoteVideoEl.srcObject = event.streams[0];
        // Reset visuals when connection starts (assume ON until told otherwise)
        remoteAvatar.style.display = 'none';
        remoteMicStatus.style.display = 'none';
    };

    peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            const target = connectedPeerId || initialTargetId;
            if (target) {
                socket.emit('sendIceCandidate', {
                    targetUserId: target,
                    candidate: event.candidate
                });
            }
        }
    };
    
    peerConnection.onconnectionstatechange = () => {
        if (['disconnected', 'failed', 'closed'].includes(peerConnection.connectionState)) {
            console.log("Conexão encerrada remotamente.");
            stopCallTimer();
        }
    };
}

const initiateCall = () => {
    if(!initialTargetId) return alert("Nenhum ID de destino na URL.");
    callBtn.disabled = true;
    waitingEl.innerText = "Buscando usuário...";
    waitingEl.style.display = 'block';
    socket.emit("check-user-online", initialTargetId);
};

const performOffer = async () => {
    connectedPeerId = initialTargetId;
    createPeerConnection();

    try {
        const offer = await peerConnection.createOffer();
        await peerConnection.setLocalDescription(offer);

        socket.emit('newOffer', {
            targetUserId: connectedPeerId,
            sdp: offer.sdp,
            type: offer.type,
            offererUserName: userName,
            offererUserId: userId
        });
        
        waitingEl.innerText = "Chamando...";
        hangupBtn.disabled = false;
    } catch (err) {
        console.error("Erro ao criar oferta:", err);
        resetUI();
    }
};

const hangupCall = () => {
    if (peerConnection) {
        peerConnection.close();
        peerConnection = null;
    }
    stopCallTimer();
    resetUI();
};

const resetUI = () => {
    waitingEl.style.display = 'none';
    callBtn.disabled = false;
    hangupBtn.disabled = true;
    remoteVideoEl.srcObject = null;
    connectedPeerId = null;
    candidateQueue = [];
    remoteAvatar.style.display = 'none';
    remoteMicStatus.style.display = 'none';
};

const processCandidateQueue = async () => {
    if (peerConnection && peerConnection.remoteDescription && candidateQueue.length > 0) {
        for (const candidate of candidateQueue) {
            try {
                await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
            } catch (e) {}
        }
        candidateQueue = [];
    }
};

// --- SOCKET LISTENERS ---

socket.on('connect', () => console.log("Socket Conectado:", socket.id));

// Listen for Remote Media Changes
socket.on('mediaStateChange', (data) => {
    console.log("Estado de mídia remoto alterado:", data);
    
    // Câmera Remota
    if (data.camera === false) {
        // Se câmera desligada, mostra avatar e esconde vídeo
        remoteAvatar.style.display = 'flex';
        remoteVideoEl.style.opacity = '0';
    } else {
        remoteAvatar.style.display = 'none';
        remoteVideoEl.style.opacity = '1';
    }
    
    // Microfone Remoto
    if (data.mic === false) {
        remoteMicStatus.style.display = 'flex';
    } else {
        remoteMicStatus.style.display = 'none';
    }
});

socket.on('user-is-online', (data) => {
    if (data.isOnline) {
        performOffer();
    } else {
        alert("Usuário offline ou não encontrado.");
        resetUI();
    }
});

socket.on('offerResponse', async (offerObj) => {
    console.log("Recebendo chamada de " + offerObj.offererUserName);
    connectedPeerId = offerObj.offererUserId;
    
    createPeerConnection();
    
    waitingEl.innerText = `Chamada de ${offerObj.offererUserName}...`;
    waitingEl.style.display = 'block';

    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(offerObj));
        const answer = await peerConnection.createAnswer();
        await peerConnection.setLocalDescription(answer);

        socket.emit('newAnswer', {
            targetUserId: connectedPeerId,
            sdp: answer.sdp,
            type: answer.type,
            answererUserName: userName
        });
        
        waitingEl.style.display = 'none';
        hangupBtn.disabled = false;
        callBtn.disabled = true;
        startCallTimer();
        processCandidateQueue();
        
        // Send initial state to caller (so they know if I started muted)
        if (localStream) {
            const isCam = localStream.getVideoTracks()[0]?.enabled ?? true;
            const isMic = localStream.getAudioTracks()[0]?.enabled ?? true;
            emitMediaState(isCam, isMic);
        }
        
    } catch (err) {
        console.error("Erro ao atender:", err);
    }
});

socket.on('answerResponse', async (answerObj) => {
    console.log("Chamada atendida!");
    waitingEl.style.display = 'none';
    startCallTimer();
    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(answerObj));
        processCandidateQueue();
        
        // Send initial state to callee
        if (localStream) {
            const isCam = localStream.getVideoTracks()[0]?.enabled ?? true;
            const isMic = localStream.getAudioTracks()[0]?.enabled ?? true;
            emitMediaState(isCam, isMic);
        }
    } catch (err) {
        console.error("Erro no Answer:", err);
    }
});

socket.on('receivedIceCandidate', async (iceObj) => {
    const candidate = iceObj.candidate || iceObj; 
    if (peerConnection && peerConnection.remoteDescription) {
        try {
            await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
        } catch (e) {}
    } else {
        candidateQueue.push(candidate);
    }
});

callBtn.addEventListener('click', initiateCall);
hangupBtn.addEventListener('click', hangupCall);

if (toggleCameraBtn) toggleCameraBtn.addEventListener('click', toggleCamera);
if (toggleMicBtn) toggleMicBtn.addEventListener('click', toggleMic);