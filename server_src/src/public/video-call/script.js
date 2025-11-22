// src/public/video-call/script.js

// --- 1. CONFIGURAÇÃO E VARIÁVEIS ---
const urlParams = new URLSearchParams(window.location.search);
const userName = urlParams.get('userName') || "Usuario" + Math.floor(Math.random() * 100);
const userId = urlParams.get('userId') || Math.floor(Math.random() * 100000).toString();
// Quem eu quero chamar (pode vir vazio se eu for apenas receber)
const initialTargetId = urlParams.get('targetId');

// Variável dinâmica para saber com quem estamos falando realmente (seja chamando ou atendendo)
let connectedPeerId = null;

// Conecta ao Socket
const socket = io('/', {
    query: { userName, userId }
});

let localStream;
let peerConnection;
let candidateQueue = []; // Fila para guardar candidatos que chegam fora de ordem

const localVideoEl = document.getElementById('local-video');
const remoteVideoEl = document.getElementById('remote-video');
const callBtn = document.getElementById('call');
const hangupBtn = document.getElementById('hangup');

// --- BOTÕES DE MÍDIA ---
const toggleCameraBtn = document.getElementById('toggle-camera'); 
const toggleMicBtn = document.getElementById('toggle-mic'); // NOVO: Botão de Mic

const waitingEl = document.getElementById('waiting');
const userNameDisplay = document.getElementById('user-name');

userNameDisplay.innerText = "Eu: " + userName;

const peerConfiguration = {
    iceServers: [
        { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] }
    ]
};

// --- 2. INICIALIZAÇÃO DA MÍDIA ---
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

// --- FUNÇÃO: Alternar Câmera (Liga/Desliga) ---
const toggleCamera = () => {
    if (localStream) {
        const videoTracks = localStream.getVideoTracks();
        if (videoTracks && videoTracks.length > 0) {
            const videoTrack = videoTracks[0];
            videoTrack.enabled = !videoTrack.enabled;
            
            if (toggleCameraBtn) {
                const isCameraOn = videoTrack.enabled;
                toggleCameraBtn.innerText = isCameraOn ? "Desativar Câmera" : "Ativar Câmera";
                toggleCameraBtn.classList.toggle('btn-secondary', isCameraOn);
                toggleCameraBtn.classList.toggle('btn-danger', !isCameraOn);
            }
        }
    }
};

// --- FUNÇÃO NOVA: Alternar Microfone (Mute/Unmute) ---
const toggleMic = () => {
    if (localStream) {
        const audioTracks = localStream.getAudioTracks(); // Pega faixas de áudio
        if (audioTracks && audioTracks.length > 0) {
            const audioTrack = audioTracks[0];
            // Inverte o estado (Mudo / Ouvindo)
            audioTrack.enabled = !audioTrack.enabled;
            
            // Feedback visual
            if (toggleMicBtn) {
                const isMicOn = audioTrack.enabled;
                toggleMicBtn.innerText = isMicOn ? "Desativar Microfone" : "Ativar Microfone";
                
                toggleMicBtn.classList.toggle('btn-secondary', isMicOn);
                toggleMicBtn.classList.toggle('btn-danger', !isMicOn); // Vermelho se estiver mudo
            }
        }
    }
};

// --- 3. LÓGICA WEBRTC ---

const createPeerConnection = () => {
    // Se já existir, fecha a anterior para evitar conflitos
    if (peerConnection) peerConnection.close();

    peerConnection = new RTCPeerConnection(peerConfiguration);

    // Adiciona tracks locais
    if (localStream) {
        localStream.getTracks().forEach(track => {
            peerConnection.addTrack(track, localStream);
        });
    }

    // Recebe vídeo remoto
    peerConnection.ontrack = (event) => {
        console.log("Vídeo remoto recebido!");
        waitingEl.style.display = 'none';
        remoteVideoEl.srcObject = event.streams[0];
    };

    // Envia candidatos ICE locais
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
        if (peerConnection.connectionState === 'disconnected' || 
            peerConnection.connectionState === 'failed' || 
            peerConnection.connectionState === 'closed') {
            console.log("Conexão encerrada remotamente.");
        }
    };
}

// Inicia a tentativa de chamada
const initiateCall = () => {
    if(!initialTargetId) return alert("Nenhum ID de destino na URL.");
    
    callBtn.disabled = true;
    waitingEl.innerText = "Buscando usuário...";
    waitingEl.style.display = 'block';
    
    socket.emit("check-user-online", initialTargetId);
};

// Executa a oferta real após confirmação
const performOffer = async () => {
    connectedPeerId = initialTargetId; // Definimos o alvo
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
    resetUI();
};

const resetUI = () => {
    waitingEl.style.display = 'none';
    callBtn.disabled = false;
    hangupBtn.disabled = true;
    remoteVideoEl.srcObject = null;
    connectedPeerId = null;
    candidateQueue = [];
};

// Processa fila de candidatos que chegaram cedo demais
const processCandidateQueue = async () => {
    if (peerConnection && peerConnection.remoteDescription && candidateQueue.length > 0) {
        console.log(`Processando ${candidateQueue.length} candidatos ICE da fila...`);
        for (const candidate of candidateQueue) {
            try {
                await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
            } catch (e) {
                console.error("Erro processando ICE da fila:", e);
            }
        }
        candidateQueue = [];
    }
};

// --- 4. SOCKET LISTENERS ---

socket.on('connect', () => console.log("Socket Conectado:", socket.id));

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
        
        processCandidateQueue();
        
    } catch (err) {
        console.error("Erro ao atender chamada:", err);
    }
});

socket.on('answerResponse', async (answerObj) => {
    console.log("Chamada atendida!");
    waitingEl.style.display = 'none';
    
    try {
        await peerConnection.setRemoteDescription(new RTCSessionDescription(answerObj));
        processCandidateQueue();
    } catch (err) {
        console.error("Erro no setRemoteDescription (Answer):", err);
    }
});

socket.on('receivedIceCandidate', async (iceObj) => {
    const candidate = iceObj.candidate || iceObj; 
    
    if (peerConnection && peerConnection.remoteDescription) {
        try {
            await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
        } catch (e) {
            console.error("Erro ao adicionar ICE:", e);
        }
    } else {
        console.log("ICE recebido antes da hora. Enfileirando.");
        candidateQueue.push(candidate);
    }
});

// --- 5. EVENTOS UI ---
callBtn.addEventListener('click', initiateCall);
hangupBtn.addEventListener('click', hangupCall);

if (toggleCameraBtn) {
    toggleCameraBtn.addEventListener('click', toggleCamera);
}

// Listener para o novo botão de Mic
if (toggleMicBtn) {
    toggleMicBtn.addEventListener('click', toggleMic);
}