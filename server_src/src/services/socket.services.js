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

        // 2. Offer
        socket.on("newOffer", (data) => {
            const { targetUserId, sdp, type, offererUserName, offererUserId } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("offerResponse", {
                    sdp, type, offererUserId, offererUserName
                });
            }
        });

        // 3. Answer
        socket.on("newAnswer", (data) => {
            const { targetUserId, sdp, type, answererUserName } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("answerResponse", {
                    sdp, type, answererUserId: userId, answererUserName
                });
            }
        });

        // 4. ICE Candidates
        socket.on("sendIceCandidate", (data) => {
            const { targetUserId, candidate } = data;
            const targetSocketId = userSocketMap[targetUserId];

            if (targetSocketId) {
                io.to(targetSocketId).emit("receivedIceCandidate", {
                    candidate, senderUserId: userId
                });
            }
        });
        
        // 5. Media State Change (Camera/Mic Toggle) - NOVO
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
            const index = connectedSockets.findIndex(s => s.socketId === socket.id);
            if (index !== -1) {
                connectedSockets.splice(index, 1);
            }
            if (userId) delete userSocketMap[userId];
        });
    });
};