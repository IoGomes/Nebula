// src/public/video-call/script.js

// 1. Configuração Inicial e Captura de Parâmetros
const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('userName') || "Usuário Web";
const userId = urlParams.get('userId') || Math.floor(Math.random() * 100000).toString();
const initialTargetId = urlParams.get('targetId');

let connectedPeerId = null;

// Conexão Socket.IO (Forçando websocket para melhor performance)
const socket = io('/', {
    query: { userName, userId },
    transports: ['websocket', 'polling']
});

// Variáveis de Estado WebRTC
let localStream;
let peerConnection;
let candidateQueue = [];

// Elementos da DOM
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

// Elementos de Estado Remoto (Avatar/Mic)
const remoteAvatar = document.getElementById('remote-avatar');
const remoteMicStatus = document.getElementById('remote-mic-status');

// Inicialização de UI
if(userNameDisplay) userNameDisplay.innerText = "Eu: " + userName;

// Configuração STUN (Essencial para conexões externas/4G)
const peerConfiguration = {
    iceServers: [
        { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] }
    ]
};

// 2. Inicialização de Mídia (Câmera/Mic)
const initMedia = async () => {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
        localStream = stream;
        localVideoEl.srcObject = stream;
    } catch (err) {
        console.error("Erro ao acessar mídia:", err);
        alert("Erro: Verifique permissões de câmera e microfone.");
    }
}
initMedia();

// 3. Lógica de Chamada e Notificação

// Botão Ligar Clicado
const initiateCall = () => {
    if (!initialTargetId) return alert("Erro: ID de destino não encontrado na URL.");
    
    callBtn.disabled = true;
    waitingEl.innerText = "Verificando disponibilidade...";
    waitingEl.style.display = 'block';
    
    // Primeiro verifica se o usuário está conectado no Socket
    socket.emit("check-user-online", initialTargetId);
};

// Resposta da verificação de status
socket.on('user-is-online', (data) => {
    if (data.isOnline) {
        // Se online, inicia o processo WebRTC e Notificação
        performOffer();
    } else {
        alert("O usuário destino está offline.");
        resetUI();
    }
});

// CRIA A OFERTA E ENVIA DADOS PARA O ANDROID
const performOffer = async () => {
    connectedPeerId = initialTargetId;
    createPeerConnection();

    try {
        const offer = await peerConnection.createOffer();
        await peerConnection.setLocalDescription(offer);

        console.log("Enviando sinalização com payload de notificação...");

        // --- PONTO CRÍTICO: PAYLOAD PARA O ANDROID ---
        socket.emit('newOffer', {
            targetUserId: connectedPeerId, // Quem recebe
            
            // Dados WebRTC
            sdp: offer.sdp,
            type: offer.type,
            
            // Dados de Notificação (Para o Java interpretar)
            offererUserName: userName,       // Nome para exibir na notificação
            offererUserId: userId,           // ID para callback
            notificationType: 'incoming_call', // TAG para o Android saber que é chamada
            callType: 'video',               // Ícone de vídeo
            timestamp: Date.now()
        });
        
        waitingEl.innerText = "Chamando...";
        hangupBtn.disabled = false;
    } catch (err) {
        console.error("Erro ao criar oferta:", err);
        resetUI();
    }
};

// 4. Funções Core WebRTC

const createPeerConnection = () => {
    if (peerConnection) peerConnection.close();
    peerConnection = new RTCPeerConnection(peerConfiguration);

    // Adiciona faixas locais
    if (localStream) {
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));
    }

    // Recebe vídeo remoto
    peerConnection.ontrack = (event) => {
        waitingEl.style.display = 'none';
        remoteVideoEl.srcObject = event.streams[0];
        // Reseta placeholders visuais
        if(remoteAvatar) remoteAvatar.style.display = 'none';
        if(remoteMicStatus) remoteMicStatus.style.display = 'none';
    };

    // Gerencia candidatos ICE (Network paths)
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
    
    // Monitora estado da conexão
    peerConnection.onconnectionstatechange = () => {
        if (['disconnected', 'failed', 'closed'].includes(peerConnection.connectionState)) {
            console.log("Conexão caiu ou foi encerrada.");
            stopCallTimer(); // Função do timer (video-call-info.js)
        }
    };
}

const hangupCall = () => {
    if (peerConnection) {
        peerConnection.close();
        peerConnection = null;
    }
    stopCallTimer();
    resetUI();
    // Opcional: Emitir evento para cancelar notificação no Android se não atendida
    // socket.emit('callCancelled', { targetUserId: connectedPeerId });
};

const resetUI = () => {
    waitingEl.style.display = 'none';
    callBtn.disabled = false;
    hangupBtn.disabled = true;
    remoteVideoEl.srcObject = null;
    connectedPeerId = null;
    candidateQueue = [];
    if(remoteAvatar) remoteAvatar.style.display = 'none';
    if(remoteMicStatus) remoteMicStatus.style.display = 'none';
};

// Processa fila de candidatos ICE que chegaram antes da resposta SDP
const processCandidateQueue = async () => {
    if (peerConnection && peerConnection.remoteDescription && candidateQueue.length > 0) {
        for (const candidate of candidateQueue) {
            try { await peerConnection.addIceCandidate(new RTCIceCandidate(candidate)); } catch (e) {}
        }
        candidateQueue = [];
    }
};

// 5. Listeners do Socket (Recepção de Eventos)

socket.on('connect', () => console.log("Socket Conectado ID:", socket.id));

// Recebimento de Chamada (Lado Browser)
socket.on('offerResponse', async (offerObj) => {
    console.log("Recebendo chamada de:", offerObj.offererUserName);
    connectedPeerId = offerObj.offererUserId;
    
    createPeerConnection();
    waitingEl.innerText = `Recebendo chamada de ${offerObj.offererUserName}...`;
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
        
        // Sincroniza estado inicial de mídia (Envia estado atual para quem ligou)
        if (localStream) {
            const isCam = localStream.getVideoTracks()[0]?.enabled ?? true;
            const isMic = localStream.getAudioTracks()[0]?.enabled ?? true;
            emitMediaState(isCam, isMic);
        }
    } catch (err) {
        console.error("Erro ao processar oferta recebida:", err);
    }
});

// Resposta de Atendimento (Lado Browser que iniciou)
socket.on('answerResponse', async (answerObj) => {
    console.log("Chamada atendida por:", answerObj.answererUserName);
    waitingEl.style.display = 'none';
    startCallTimer();
    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(answerObj));
        processCandidateQueue();
        
        // Envia estado inicial de mídia
        if (localStream) {
            const isCam = localStream.getVideoTracks()[0]?.enabled ?? true;
            const isMic = localStream.getAudioTracks()[0]?.enabled ?? true;
            emitMediaState(isCam, isMic);
        }
    } catch (err) { console.error("Erro ao processar resposta:", err); }
});

// Recebimento de ICE Candidates
socket.on('receivedIceCandidate', async (iceObj) => {
    const candidate = iceObj.candidate || iceObj; 
    if (peerConnection && peerConnection.remoteDescription) {
        try { await peerConnection.addIceCandidate(new RTCIceCandidate(candidate)); } catch (e) {}
    } else {
        candidateQueue.push(candidate);
    }
});

// Recebimento de Mudança de Mídia (Câmera/Mic Remoto)
socket.on('mediaStateChange', (data) => {
    // Câmera Remota
    if (remoteAvatar) {
        remoteAvatar.style.display = (data.camera === false) ? 'flex' : 'none';
        remoteVideoEl.style.opacity = (data.camera === false) ? '0' : '1';
    }
    // Microfone Remoto
    if (remoteMicStatus) {
        remoteMicStatus.style.display = (data.mic === false) ? 'flex' : 'none';
    }
});

// 6. Helpers de UI e Mídia

const emitMediaState = (isCameraOn, isMicOn) => {
    const target = connectedPeerId || initialTargetId;
    if (target) {
        socket.emit('mediaStateChange', { targetUserId: target, camera: isCameraOn, mic: isMicOn });
    }
};

const toggleCamera = () => {
    if (localStream) {
        const track = localStream.getVideoTracks()[0];
        if(track) {
            track.enabled = !track.enabled;
            // Atualiza Ícones
            if(cameraOnIcon) cameraOnIcon.style.display = track.enabled ? 'block' : 'none';
            if(cameraOffIcon) cameraOffIcon.style.display = track.enabled ? 'none' : 'block';
            if(toggleCameraBtn) toggleCameraBtn.classList.toggle('disabled', !track.enabled);
            // Emite estado
            emitMediaState(track.enabled, localStream.getAudioTracks()[0]?.enabled ?? true);
        }
    }
};

const toggleMic = () => {
    if (localStream) {
        const track = localStream.getAudioTracks()[0];
        if(track) {
            track.enabled = !track.enabled;
            // Atualiza Ícones
            if(micOnIcon) micOnIcon.style.display = track.enabled ? 'block' : 'none';
            if(micOffIcon) micOffIcon.style.display = track.enabled ? 'none' : 'block';
            if(toggleMicBtn) toggleMicBtn.classList.toggle('disabled', !track.enabled);
            // Emite estado
            emitMediaState(localStream.getVideoTracks()[0]?.enabled ?? true, track.enabled);
        }
    }
};

// Event Listeners
if(callBtn) callBtn.addEventListener('click', initiateCall);
if(hangupBtn) hangupBtn.addEventListener('click', hangupCall);
if(toggleCameraBtn) toggleCameraBtn.addEventListener('click', toggleCamera);
if(toggleMicBtn) toggleMicBtn.addEventListener('click', toggleMic);