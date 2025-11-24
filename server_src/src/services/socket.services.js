// src/services/socket.service.js

const connectedSockets = [];
const userSocketMap = {};

export const initializeSocketIO = (io) => {
    io.on("connection", (socket) => {
        console.log(`Novo socket conectado: ${socket.id}`);

        const userName = socket.handshake.query.userName;
        const userId = socket.handshake.query.userId;

        if (userId) {
            const existingIndex = connectedSockets.findIndex(s => s.userId === userId);
            if (existingIndex !== -1) {
                connectedSockets.splice(existingIndex, 1);
            }

            connectedSockets.push({ socketId: socket.id, userId, userName });
            userSocketMap[userId] = socket.id;
            console.log(`Usuário registrado: ${userName} (${userId})`);
        }

        // 1. User Online Check
        socket.on("check-user-online", (targetUserId) => {
            const targetSocketId = userSocketMap[targetUserId];
            socket.emit("user-is-online", {
                isOnline: !!targetSocketId,
                targetUserId
            });
        });

        // 2. Offer (PASS-THROUGH PARA NOTIFICAÇÃO ANDROID)
        socket.on("newOffer", (data) => {
            const { targetUserId, ...offerPayload } = data;

            // --- INICIO DO DEBUG ---
            console.log(`\n--- 🔍 TENTATIVA DE CHAMADA ---`);
            console.log(`1. Remetente (ID): ${socket.handshake.query.userId}`);
            console.log(`2. Destinatário (Alvo): ${targetUserId}`);
            console.log(`3. Chaves disponíveis no Mapa:`, Object.keys(userSocketMap)); // Mostra quem está online

            const targetSocketId = userSocketMap[targetUserId];
            console.log(`4. Resultado da Busca: ${targetSocketId ? "✅ ENCONTRADO (" + targetSocketId + ")" : "❌ NÃO ENCONTRADO"}`);
            // --- FIM DO DEBUG ---

            if (targetSocketId) {
                io.to(targetSocketId).emit("offerResponse", offerPayload);
            } else {
                // Opcional: Avisar quem ligou que falhou
                socket.emit("call-failed", { reason: "User not found or offline" });
            }
        });

        // 3. Answer
        socket.on("newAnswer", (data) => {
            const { targetUserId, ...answerPayload } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                // Repassa a resposta WebRTC (SDP)
                io.to(targetSocketId).emit("answerResponse", {
                    ...answerPayload,
                    answererUserId: userId
                });
            }
        });

        // 4. ICE Candidates (Troca de rotas de rede)
        socket.on("sendIceCandidate", (data) => {
            const { targetUserId, candidate } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("receivedIceCandidate", {
                    candidate,
                    senderUserId: userId
                });
            }
        });

        // 5. Media State Change (Sincronia de Câmera/Mic)
        socket.on("mediaStateChange", (data) => {
            const { targetUserId, camera, mic } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("mediaStateChange", {
                    camera,
                    mic,
                    senderUserId: userId
                });
            }
        });

        // 6. Disconnect
        socket.on("disconnect", () => {
            console.log(`Socket desconectado: ${socket.id}`);

            const index = connectedSockets.findIndex(s => s.socketId === socket.id);
            if (index !== -1) {
                connectedSockets.splice(index, 1);
            }

            // Só remove do mapa global se o socket desconectado for o atual registrado para aquele user
            // (Evita bugs se o usuário reconectar rapidamente antes do evento de disconnect disparar)
            if (userId && userSocketMap[userId] === socket.id) {
                delete userSocketMap[userId];
            }
        });
    });
};