import express from "express";
import http from "http";
import userRoutes from "./routes/user.routes.js";
import { Server } from "socket.io";

import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = 3000;

const server = http.createServer(app);

app.use(express.json());

// ------------------------------------------------
// 🔥 VITE.JS e Rotas
// ------------------------------------------------

const clientDistPath = path.join(__dirname, '../../web/dist');

// Static
app.use(express.static(clientDistPath));

// Suas rotas API
app.use('/api/user', userRoutes);

app.use((req, res) => {
  res.sendFile(path.join(clientDistPath, 'index.html'));
});

// ------------------------------------------------
// 🔥 SOCKET.IO 
// ------------------------------------------------

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"],
  },
});

const connectedSockets = [];
const offers = [];
const userSocketMap = {};

io.on("connection", (socket) => {
  console.log(`Um usuário conectado: ${socket.id}`);

  const userName = socket.handshake.query.userName;
  const userId = socket.handshake.query.userId;

  connectedSockets.push({ socketId: socket.id, userId, userName });
  userSocketMap[userId] = socket.id;

  console.log(`Usuário conectado: ${userName} (${userId}) - Socket: ${socket.id}`);
  console.log(connectedSockets);

  socket.on("check-user-online", (targetUserId) => {
        const targetSocketId = userSocketMap[targetUserId];
        if (targetSocketId) {
            socket.emit("user-is-online", { isOnline: true, targetUserId });
        } else {
            socket.emit("user-is-online", { isOnline: false, targetUserId });
        }
    });

  // 2. Processar Oferta (Call Request)
    socket.on("newOffer", (data) => {
        const { targetUserId, sdp, type } = data;
        const targetSocketId = userSocketMap[targetUserId];

        if (targetSocketId) {
            // Envia APENAS para o destinatário
            io.to(targetSocketId).emit("offerResponse", {
                sdp,
                type,
                offererUserId: userId, // Quem está ligando
                offererUserName: userName
            });
        } else {
            console.log("Usuário alvo desconectou antes da oferta.");
        }
    });

    // 3. Processar Resposta (Answer)
    socket.on("newAnswer", (data) => {
        const { targetUserId, sdp, type } = data;
        const targetSocketId = userSocketMap[targetUserId];

        if (targetSocketId) {
            io.to(targetSocketId).emit("answerResponse", {
                sdp,
                type,
                answererUserId: userId // Quem atendeu
            });
        }
    });

    // 4. ICE Candidates
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

    socket.on("disconnect", () => {
        // Limpeza básica
        const index = connectedSockets.findIndex(s => s.socketId === socket.id);
        if (index !== -1) connectedSockets.splice(index, 1);
        if (userId) delete userSocketMap[userId];
        console.log(`Usuário desconectado: ${userId}`);
    });
});

server.listen(port, () => {
  console.log(`Servidor rodando: http://localhost:${port}`);
});
