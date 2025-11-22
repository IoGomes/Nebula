// src/services/socket.service.js

const connectedSockets = [];
const userSocketMap = {};

export const initializeSocketIO = (io) => {
    io.on("connection", (socket) => {
        console.log(`Novo socket conectado: ${socket.id}`);

        // Pega dados da query string (io('/', { query: { userId, userName } }))
        const userName = socket.handshake.query.userName;
        const userId = socket.handshake.query.userId;

        // Registrar usuário no mapa
        if (userId) {
            // Remove conexão antiga se existir para evitar duplicidade
            const existingIndex = connectedSockets.findIndex(s => s.userId === userId);
            if (existingIndex !== -1) {
                connectedSockets.splice(existingIndex, 1);
            }

            connectedSockets.push({ socketId: socket.id, userId, userName });
            userSocketMap[userId] = socket.id;
            console.log(`Usuário registrado: ${userName} (${userId})`);
        }

        // 1. Verificar se usuário está online
        socket.on("check-user-online", (targetUserId) => {
            const targetSocketId = userSocketMap[targetUserId];
            // Retorna true se achou o socket ID no mapa
            socket.emit("user-is-online", { 
                isOnline: !!targetSocketId, 
                targetUserId 
            });
        });

        // 2. Processar Oferta (Call Request)
        socket.on("newOffer", (data) => {
            const { targetUserId, sdp, type, offererUserName, offererUserId } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("offerResponse", {
                    sdp,
                    type,
                    offererUserId,
                    offererUserName
                });
            } else {
                console.log(`Usuário alvo ${targetUserId} não encontrado para oferta.`);
            }
        });

        // 3. Processar Resposta (Answer)
        socket.on("newAnswer", (data) => {
            const { targetUserId, sdp, type, answererUserName } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("answerResponse", {
                    sdp,
                    type,
                    answererUserId: userId,
                    answererUserName
                });
            }
        });

        // 4. ICE Candidates (Troca de caminhos de rede)
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

        // 5. Desconexão
        socket.on("disconnect", () => {
            const index = connectedSockets.findIndex(s => s.socketId === socket.id);
            if (index !== -1) {
                console.log(`Usuário desconectado: ${connectedSockets[index].userId}`);
                connectedSockets.splice(index, 1);
            }
            if (userId) delete userSocketMap[userId];
        });
    });
};