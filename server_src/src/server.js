import express from "express";
import http from "http";
import userRoutes from "./routes/user.routes.js";
import { Server } from "socket.io";

import path from "path";
import { fileURLToPath } from "url";
import { initializeSocketIO } from "./services/socket.services.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = 3000;

const server = http.createServer(app);

app.use(express.json());

// Caminhos
const clientDistPath = path.join(__dirname, '../../web/dist');
// Caminho para a pasta onde criamos os arquivos da chamada de vídeo
const videoCallPath = path.join(__dirname, 'public/video-call'); 

// ------------------------------------------------
// 📹 ROTA DE VÍDEO CHAMADA (Deve vir ANTES do estático do Vite)
// ------------------------------------------------

// 1. Serve os arquivos estáticos (JS, CSS) desta pasta na rota /video-call
app.use('/video-call', express.static(videoCallPath));

// 2. Garante que acessar /video-call entregue o index.html correto
app.get('/video-call', (req, res) => {
  res.sendFile(path.join(videoCallPath, 'index.html'));
});


// ------------------------------------------------
// 🔥 VITE.JS e Rotas da Aplicação Principal
// ------------------------------------------------

// Static do App Principal (React/Vue)
app.use(express.static(clientDistPath));

// Suas rotas API
app.use('/api/user', userRoutes);

// Catch-all para o SPA (Single Page Application)
// IMPORTANTE: Isso pega qualquer rota que não foi definida acima.
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

// Inicializa o serviço de Socket separado
initializeSocketIO(io);

server.listen(port, () => {
  console.log(`Servidor rodando: http://localhost:${port}`);
  console.log(`Video Call disponível em: http://localhost:${port}/video-call`);
});