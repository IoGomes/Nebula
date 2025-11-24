// src/public/video-call/script.js

// --- 1. CONFIGURAÇÃO E PARÂMETROS URL ---
const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('userName') || "Usuário Web";
const userId = urlParams.get('userId') || Math.floor(Math.random() * 100000).toString();
const initialTargetId = urlParams.get('targetId');

console.log(`[System] Iniciando como: ${userName} (${userId})`);

// --- 2. CONFIGURAÇÃO SOCKET.IO ---
const socket = io('/', {
    query: { userName, userId },
    transports: ['websocket', 'polling'], // Força websocket para estabilidade
    reconnection: true
});

socket.on('connect', () => {
    console.log("✅ [Socket] Conectado! ID:", socket.id);
});

socket.on('connect_error', (err) => {
    console.error("❌ [Socket] Erro de conexão:", err);
});

// --- 3. VARIÁVEIS GLOBAIS ---
let localStream = null;
let peerConnection = null;
let candidateQueue = [];
let connectedPeerId = null;

// Elementos do DOM
const localVideoEl = document.getElementById('local-video');
const remoteVideoEl = document.getElementById('remote-video');
const callBtn = document.getElementById('call');
const hangupBtn = document.getElementById('hangup');
const waitingEl = document.getElementById('waiting');
const userNameDisplay = document.getElementById('user-name');
const toggleCameraBtn = document.getElementById('toggle-camera'); 
const toggleMicBtn = document.getElementById('toggle-mic');
const remoteAvatar = document.getElementById('remote-avatar');
const remoteMicStatus = document.getElementById('remote-mic-status');

if (userNameDisplay) userNameDisplay.innerText = "Eu: " + userName;

// Configuração STUN (Google)
const peerConfiguration = {
    iceServers: [
        { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] }
    ]
};

// --- 4. TELA DE INICIALIZAÇÃO (CORREÇÃO DO BLOQUEIO DE MÍDIA) ---
// Criamos uma sobreposição para forçar a interação do usuário
function showStartScreen() {
    const overlay = document.createElement('div');
    overlay.id = 'start-overlay';
    overlay.style.cssText = `
        position: fixed; top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0,0,0,0.9); z-index: 10000;
        display: flex; flex-direction: column; align-items: center; justify-content: center;
        color: white; font-family: sans-serif;
    `;
    
    overlay.innerHTML = `
        <h2 style="margin-bottom: 20px;">Permissão Necessária</h2>
        <p style="margin-bottom: 30px; text-align: center; max-width: 80%;">
            Para realizar a chamada, precisamos acessar sua câmera e microfone.
        </p>
        <button id="btn-start-system" style="
            padding: 15px 30px; font-size: 18px; background: #28a745; 
            color: white; border: none; border-radius: 50px; cursor: pointer;
            box-shadow: 0 4px 15px rgba(40, 167, 69, 0.4);
        ">INICIAR SISTEMA</button>
    `;
    
    document.body.appendChild(overlay);

    document.getElementById('btn-start-system').addEventListener('click', async () => {
        try {
            await initMedia(); // Tenta pegar a câmera
            overlay.remove();  // Se der certo, remove a tela preta
        } catch (e) {
            console.error(e);
        }
    });
}

// --- 5. LÓGICA DE MÍDIA ---
const initMedia = async () => {
    console.log("[Media] Solicitando permissão de câmera/mic...");
    
    // Verificação de Segurança SSL
    if (location.protocol !== 'https:' && location.hostname !== 'localhost' && location.hostname !== '127.0.0.1') {
        alert("ERRO CRÍTICO: Câmera requer HTTPS. Você está usando HTTP inseguro.");
        throw new Error("HTTPS Required");
    }

    try {
        const stream = await navigator.mediaDevices.getUserMedia({video: true, audio: true});
        console.log("[Media] Permissão concedida!");
        
        localStream = stream;
        localVideoEl.srcObject = stream;
        
        // Corrige bug de vídeo preto em alguns iPhones/Androids
        localVideoEl.onloadedmetadata = () => {
            localVideoEl.play();
        };

    } catch (err) {
        console.error("[Media] Erro:", err);
        alert("Erro ao acessar câmera: " + err.name + ". Verifique se o dispositivo não está bloqueado.");
        throw err;
    }
};

// --- 6. CORE WEBRTC ---

const createPeerConnection = () => {
    if (peerConnection) {
        peerConnection.close();
    }
    
    console.log("[WebRTC] Criando nova conexão Peer...");
    peerConnection = new RTCPeerConnection(peerConfiguration);

    // Adiciona tracks locais
    if (localStream) {
        localStream.getTracks().forEach(track => peerConnection.addTrack(track, localStream));
    }

    // Recebe tracks remotos
    peerConnection.ontrack = (event) => {
        console.log("[WebRTC] Stream remoto recebido!");
        waitingEl.style.display = 'none';
        remoteVideoEl.srcObject = event.streams[0];
        
        // Reset de UI
        if(remoteAvatar) remoteAvatar.style.display = 'none';
        if(remoteMicStatus) remoteMicStatus.style.display = 'none';
    };

    // ICE Candidates
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

    // Monitoramento de Estado
    peerConnection.onconnectionstatechange = () => {
        console.log("[WebRTC] Estado da conexão:", peerConnection.connectionState);
        if (['disconnected', 'failed', 'closed'].includes(peerConnection.connectionState)) {
            stopCallTimer();
        }
    };
};

// --- 7. FLUXO DE CHAMADA E NOTIFICAÇÃO ANDROID ---

const initiateCall = () => {
    if (!initialTargetId) return alert("Erro: ID de destino não encontrado na URL.");
    
    callBtn.disabled = true;
    waitingEl.innerText = "Localizando usuário...";
    waitingEl.style.display = 'block';
    
    console.log(`[Call] Buscando usuário alvo: ${initialTargetId}`);
    socket.emit("check-user-online", initialTargetId);
};

// Callback de verificação
socket.on('user-is-online', (data) => {
    console.log("[Call] Status do usuário:", data);
    if (data.isOnline) {
        performOffer();
    } else {
        alert("O usuário destino está offline.");
        resetUI();
    }
});

// Enviar Oferta (Com Payload para Android)
const performOffer = async () => {
    connectedPeerId = initialTargetId;
    createPeerConnection();

    try {
        const offer = await peerConnection.createOffer();
        await peerConnection.setLocalDescription(offer);

        console.log("[Call] Enviando oferta e notificação...");

        socket.emit('newOffer', {
            targetUserId: connectedPeerId,
            
            // Dados WebRTC
            sdp: offer.sdp,
            type: offer.type,
            
            // DADOS ESPECÍFICOS PARA O ANDROID APP
            offererUserName: userName,       
            offererUserId: userId,           
            notificationType: 'incoming_call', // Gatilho do Java
            callType: 'video',               
            timestamp: Date.now()
        });
        
        waitingEl.innerText = "Chamando...";
        hangupBtn.disabled = false;
    } catch (err) {
        console.error("[Call] Erro ao criar oferta:", err);
        resetUI();
    }
};

// --- 8. RECEBIMENTO DE CHAMADA (Browser) ---

socket.on('offerResponse', async (offerObj) => {
    console.log("[Call] Recebendo chamada de:", offerObj.offererUserName);
    
    connectedPeerId = offerObj.offererUserId;
    createPeerConnection();
    
    waitingEl.innerText = `Recebendo chamada de ${offerObj.offererUserName}...`;
    waitingEl.style.display = 'block';

    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(offerObj));
        
        // Aceita automaticamente (no browser) - No Android você tem a tela de aceite
        const answer = await peerConnection.createAnswer();
        await peerConnection.setLocalDescription(answer);

        console.log("[Call] Enviando resposta...");
        socket.emit('newAnswer', {
            targetUserId: connectedPeerId,
            sdp: answer.sdp,
            type: answer.type,
            answererUserName: userName
        });
        
        waitingEl.style.display = 'none';
        hangupBtn.disabled = false;
        callBtn.disabled = true;
        
        // Inicia cronômetro (função do outro arquivo)
        if(typeof startCallTimer === 'function') startCallTimer();
        processCandidateQueue();
        
        // Sincroniza estado inicial de mídia
        syncMediaState();

    } catch (err) {
        console.error("[Call] Erro ao atender:", err);
    }
});

socket.on('answerResponse', async (answerObj) => {
    console.log("[Call] Chamada atendida!");
    waitingEl.style.display = 'none';
    
    if(typeof startCallTimer === 'function') startCallTimer();
    
    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(answerObj));
        processCandidateQueue();
        syncMediaState();
    } catch (err) { 
        console.error("[Call] Erro na resposta:", err); 
    }
});

socket.on('receivedIceCandidate', async (iceObj) => {
    const candidate = iceObj.candidate || iceObj; 
    if (peerConnection && peerConnection.remoteDescription) {
        try { 
            await peerConnection.addIceCandidate(new RTCIceCandidate(candidate)); 
        } catch (e) { console.error("Erro ICE:", e); }
    } else {
        candidateQueue.push(candidate);
    }
});

// --- 9. CONTROLES E HELPERS ---

const processCandidateQueue = async () => {
    if (peerConnection && peerConnection.remoteDescription && candidateQueue.length > 0) {
        for (const candidate of candidateQueue) {
            try { await peerConnection.addIceCandidate(new RTCIceCandidate(candidate)); } catch (e) {}
        }
        candidateQueue = [];
    }
};

const hangupCall = () => {
    if (peerConnection) {
        peerConnection.close();
        peerConnection = null;
    }
    if(typeof stopCallTimer === 'function') stopCallTimer();
    resetUI();
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

// Sincronia de Mídia (Câmera/Mic)
const syncMediaState = () => {
    if (localStream) {
        const isCam = localStream.getVideoTracks()[0]?.enabled ?? true;
        const isMic = localStream.getAudioTracks()[0]?.enabled ?? true;
        emitMediaState(isCam, isMic);
    }
};

const emitMediaState = (isCameraOn, isMicOn) => {
    const target = connectedPeerId || initialTargetId;
    if (target) {
        socket.emit('mediaStateChange', { targetUserId: target, camera: isCameraOn, mic: isMicOn });
    }
};

// Listeners de Mídia Remota
socket.on('mediaStateChange', (data) => {
    if (remoteAvatar) {
        remoteAvatar.style.display = (data.camera === false) ? 'flex' : 'none';
        remoteVideoEl.style.opacity = (data.camera === false) ? '0' : '1';
    }
    if (remoteMicStatus) {
        remoteMicStatus.style.display = (data.mic === false) ? 'flex' : 'none';
    }
});

// Botões de Controle Local
const toggleCamera = () => {
    if (localStream) {
        const track = localStream.getVideoTracks()[0];
        if(track) {
            track.enabled = !track.enabled;
            // Atualiza UI
            const onIcon = document.getElementById('camera-on-icon');
            const offIcon = document.getElementById('camera-off-icon');
            if(onIcon) onIcon.style.display = track.enabled ? 'block' : 'none';
            if(offIcon) offIcon.style.display = track.enabled ? 'none' : 'block';
            if(toggleCameraBtn) toggleCameraBtn.classList.toggle('disabled', !track.enabled);
            
            emitMediaState(track.enabled, localStream.getAudioTracks()[0]?.enabled ?? true);
        }
    }
};

const toggleMic = () => {
    if (localStream) {
        const track = localStream.getAudioTracks()[0];
        if(track) {
            track.enabled = !track.enabled;
            // Atualiza UI
            const onIcon = document.getElementById('mic-on-icon');
            const offIcon = document.getElementById('mic-off-icon');
            if(onIcon) onIcon.style.display = track.enabled ? 'block' : 'none';
            if(offIcon) offIcon.style.display = track.enabled ? 'none' : 'block';
            if(toggleMicBtn) toggleMicBtn.classList.toggle('disabled', !track.enabled);
            
            emitMediaState(localStream.getVideoTracks()[0]?.enabled ?? true, track.enabled);
        }
    }
};

// --- 10. INICIALIZAÇÃO DE EVENTOS ---
if(callBtn) callBtn.addEventListener('click', initiateCall);
if(hangupBtn) hangupBtn.addEventListener('click', hangupCall);
if(toggleCameraBtn) toggleCameraBtn.addEventListener('click', toggleCamera);
if(toggleMicBtn) toggleMicBtn.addEventListener('click', toggleMic);

// Chama a tela de início assim que carrega
showStartScreen();