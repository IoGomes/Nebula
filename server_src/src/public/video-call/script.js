// src/public/video-call/script.js

/**
 * MÓDULO DE VÍDEO CHAMADA
 * Integração com Android via Socket.IO e WebRTC
 */

// --- 1. CONFIGURAÇÃO INICIAL ---
const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('userName') || "Usuário Web";
const userId = urlParams.get('userId') || Math.floor(Math.random() * 100000).toString();
const initialTargetId = urlParams.get('targetId');

// --- 2. ELEMENTOS UI ---
const startOverlay = document.getElementById('start-overlay');
const startBtn = document.getElementById('btn-start-system');
const localVideoEl = document.getElementById('local-video');
const remoteVideoEl = document.getElementById('remote-video');
const callBtn = document.getElementById('call');
const hangupBtn = document.getElementById('hangup');
const waitingEl = document.getElementById('waiting');
const timerEl = document.getElementById('timer');
const userDisplayEl = document.getElementById('user-display');
const remoteAvatar = document.getElementById('remote-avatar');
const toggleCameraBtn = document.getElementById('toggle-camera'); 
const toggleMicBtn = document.getElementById('toggle-mic');

if(userDisplayEl) userDisplayEl.innerText = userName;

// --- 3. VARIÁVEIS DE ESTADO ---
let localStream = null;
let peerConnection = null;
let candidateQueue = [];
let connectedPeerId = null;
let callTimerInterval = null;
let callStartTime = null;

// Socket IO
const socket = io('/', {
    query: { userName, userId },
    transports: ['websocket', 'polling']
});

socket.on('connect', () => console.log("✅ Socket Conectado:", socket.id));

// Configuração WebRTC (Google STUN)
const peerConfiguration = {
    iceServers: [
        { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] }
    ]
};

// --- 4. INICIALIZAÇÃO DO SISTEMA (Ação do Botão) ---

startBtn.addEventListener('click', async () => {
    // Feedback visual
    startBtn.innerText = "Iniciando...";
    startBtn.disabled = true;

    try {
        await initMedia(); // Pede permissão da câmera
        
        // Se deu certo, esconde o overlay
        startOverlay.style.opacity = '0';
        setTimeout(() => startOverlay.style.display = 'none', 500); // Animação
        
        console.log("Sistema iniciado com sucesso.");
    } catch (err) {
        startBtn.innerText = "Erro: Permissão Negada";
        startBtn.style.background = "red";
        console.error(err);
    }
});

const initMedia = async () => {
    // Checagem de segurança
    if (location.protocol !== 'https:' && location.hostname !== 'localhost') {
        alert("Atenção: A câmera exige HTTPS.");
    }

    try {
        const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
        localStream = stream;
        localVideoEl.srcObject = stream;
        
        // Garante que o vídeo toque (bug fix safari)
        localVideoEl.onloadedmetadata = () => localVideoEl.play();

    } catch (err) {
        throw new Error("Falha ao acessar mídia: " + err.name);
    }
};

// --- 5. LÓGICA DE CHAMADA (Caller) ---

const initiateCall = () => {
    if (!initialTargetId) return alert("Erro: URL sem ID de destino.");
    
    toggleCallButtonState(true); // Desabilita botão ligar
    waitingEl.innerText = "Buscando usuário...";
    waitingEl.style.display = 'block';
    
    socket.emit("check-user-online", initialTargetId);
};

socket.on('user-is-online', (data) => {
    if (data.isOnline) {
        performOffer();
    } else {
        alert("Usuário offline.");
        resetUI();
    }
});

const performOffer = async () => {
    connectedPeerId = initialTargetId;
    createPeerConnection();

    try {
        const offer = await peerConnection.createOffer();
        await peerConnection.setLocalDescription(offer);

        console.log("Enviando notificação ao Android...");

        socket.emit('newOffer', {
            targetUserId: connectedPeerId,
            sdp: offer.sdp,
            type: offer.type,
            
            // Payload para Android/Java
            offererUserName: userName,       
            offererUserId: userId,           
            notificationType: 'incoming_call', 
            callType: 'video',               
            timestamp: Date.now()
        });
        
        waitingEl.innerText = "Chamando...";
        enableHangup(true);

    } catch (err) {
        console.error("Erro na oferta:", err);
        resetUI();
    }
};

// --- 6. RECEBIMENTO DE CHAMADA (Callee) ---

socket.on('offerResponse', async (offerObj) => {
    console.log("Recebendo chamada de:", offerObj.offererUserName);
    
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
        enableHangup(true);
        startTimer();
        processCandidateQueue();
        syncMediaState();

    } catch (err) {
        console.error("Erro ao atender:", err);
    }
});

socket.on('answerResponse', async (answerObj) => {
    waitingEl.style.display = 'none';
    startTimer();
    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(answerObj));
        processCandidateQueue();
        syncMediaState();
    } catch (e) { console.error(e); }
});

// --- 7. WEBRTC CORE ---

const createPeerConnection = () => {
    if (peerConnection) peerConnection.close();
    peerConnection = new RTCPeerConnection(peerConfiguration);

    if (localStream) {
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));
    }

    peerConnection.ontrack = (event) => {
        remoteVideoEl.srcObject = event.streams[0];
        waitingEl.style.display = 'none';
        remoteAvatar.style.display = 'none';
    };

    peerConnection.onicecandidate = (event) => {
        if (event.candidate && (connectedPeerId || initialTargetId)) {
            socket.emit('sendIceCandidate', {
                targetUserId: connectedPeerId || initialTargetId,
                candidate: event.candidate
            });
        }
    };
    
    peerConnection.onconnectionstatechange = () => {
        if (['disconnected', 'closed', 'failed'].includes(peerConnection.connectionState)) {
            hangupCall();
        }
    }
};

socket.on('receivedIceCandidate', async (iceObj) => {
    const candidate = iceObj.candidate || iceObj;
    if (peerConnection && peerConnection.remoteDescription) {
        try { await peerConnection.addIceCandidate(new RTCIceCandidate(candidate)); } catch (e) {}
    } else {
        candidateQueue.push(candidate);
    }
});

const processCandidateQueue = async () => {
    if (peerConnection && peerConnection.remoteDescription) {
        while(candidateQueue.length > 0) {
            try { await peerConnection.addIceCandidate(new RTCIceCandidate(candidateQueue.shift())); } catch(e){}
        }
    }
};

// --- 8. CONTROLES DE UI E MÍDIA ---

const hangupCall = () => {
    if (peerConnection) {
        peerConnection.close();
        peerConnection = null;
    }
    stopTimer();
    resetUI();
};

const resetUI = () => {
    waitingEl.style.display = 'none';
    remoteVideoEl.srcObject = null;
    remoteAvatar.style.display = 'none';
    connectedPeerId = null;
    candidateQueue = [];
    toggleCallButtonState(false);
    enableHangup(false);
};

// Helpers de Botões
const toggleCallButtonState = (disabled) => {
    callBtn.disabled = disabled;
    callBtn.style.opacity = disabled ? '0.5' : '1';
};

const enableHangup = (enabled) => {
    hangupBtn.disabled = !enabled;
    hangupBtn.style.opacity = enabled ? '1' : '0.5';
    toggleCallButtonState(enabled); // Se hangup on, call off
};

// Controle de Câmera/Mic
const toggleMedia = (type) => {
    if (!localStream) return;
    const isVideo = type === 'video';
    const tracks = isVideo ? localStream.getVideoTracks() : localStream.getAudioTracks();
    const btn = isVideo ? toggleCameraBtn : toggleMicBtn;
    
    if (tracks.length > 0) {
        tracks[0].enabled = !tracks[0].enabled;
        const isEnabled = tracks[0].enabled;
        
        // Troca classe visual
        if (isEnabled) {
            btn.classList.add('active');
            btn.classList.remove('disabled');
            btn.querySelector('.icon-on').style.display = 'block';
            btn.querySelector('.icon-off').style.display = 'none';
        } else {
            btn.classList.remove('active');
            btn.classList.add('disabled');
            btn.querySelector('.icon-on').style.display = 'none';
            btn.querySelector('.icon-off').style.display = 'block';
        }

        // Avisa o par
        const camState = localStream.getVideoTracks()[0]?.enabled ?? true;
        const micState = localStream.getAudioTracks()[0]?.enabled ?? true;
        
        const target = connectedPeerId || initialTargetId;
        if (target) {
            socket.emit('mediaStateChange', { targetUserId: target, camera: camState, mic: micState });
        }
    }
};

// Listeners de estado remoto
socket.on('mediaStateChange', (data) => {
    if (data.camera === false) {
        remoteAvatar.style.display = 'flex';
        remoteVideoEl.style.opacity = '0';
    } else {
        remoteAvatar.style.display = 'none';
        remoteVideoEl.style.opacity = '1';
    }
});

// Timer
const startTimer = () => {
    if (callTimerInterval) clearInterval(callTimerInterval);
    callStartTime = Date.now();
    callTimerInterval = setInterval(() => {
        const diff = Math.floor((Date.now() - callStartTime) / 1000);
        const m = Math.floor(diff / 60).toString().padStart(2, '0');
        const s = (diff % 60).toString().padStart(2, '0');
        if(timerEl) timerEl.innerText = `${m}:${s}`;
    }, 1000);
};

const stopTimer = () => {
    if (callTimerInterval) clearInterval(callTimerInterval);
    if(timerEl) timerEl.innerText = "00:00";
};

const syncMediaState = () => {
    // Força envio do estado atual ao conectar
    if (localStream) {
        const camState = localStream.getVideoTracks()[0]?.enabled ?? true;
        const micState = localStream.getAudioTracks()[0]?.enabled ?? true;
        const target = connectedPeerId || initialTargetId;
        if (target) socket.emit('mediaStateChange', { targetUserId: target, camera: camState, mic: micState });
    }
};

// --- 9. EVENT LISTENERS ---
callBtn.addEventListener('click', initiateCall);
hangupBtn.addEventListener('click', hangupCall);
toggleCameraBtn.addEventListener('click', () => toggleMedia('video'));
toggleMicBtn.addEventListener('click', () => toggleMedia('audio'));