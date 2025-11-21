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

io.on("connection", (socket) => {
  console.log(`Um usuário conectado: ${socket.id}`);

  const userName = socket.handshake.query.userName;
  const userId = socket.handshake.query.userId;

  connectedSockets.push({
    socketId: socket.id,
    userId,
    userName,
  });

  console.log(connectedSockets);

  socket.on("sendMessage", (data) => {
    console.log("Mensagem recebida:", data);
    io.emit("newMessage", data);
  });

  socket.on("disconnect", () => {
    console.log(`Usuário desconectado: ${socket.id}`);
  });
});

// ------------------------------------------------
// SERVIDOR EXPRESS
// ------------------------------------------------
server.listen(port, () => {
  console.log(`Servidor rodando: http://localhost:${port}`);
});
