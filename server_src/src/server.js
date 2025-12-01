import express from "express";
import http from "http";
import userRoutes from "./routes/user.routes.js";
import { Server } from "socket.io";
import path from "path";
import { fileURLToPath } from "url";
import { initializeSocketIO } from "./services/socket.services.js";

// 🔥 PM2 METRICS: Importação
import pm2 from '@pm2/io';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = 3000;

// ------------------------------------------------
// 🔥 PM2 METRICS: Definição das Métricas
// ------------------------------------------------

// 1. Contador de Chamadas API
const apiRequestCounter = pm2.counter({
  name: 'API HTTP Req',
  id: 'app/api/requests'
});

// 2. Métrica de Tempo Online (Customizada)
const startTime = Date.now();
pm2.metric({
  name: 'Uptime (Horas)',
  value: () => ((Date.now() - startTime) / 1000 / 60 / 60).toFixed(2)
});

const server = http.createServer(app);

app.use(express.json());

// ------------------------------------------------
// 🔥 PM2 METRICS: Middleware para contar requests
// ------------------------------------------------
// Colocamos isso ANTES das rotas de API para interceptar tudo
app.use('/api', (req, res, next) => {
  apiRequestCounter.inc(); // Incrementa o contador
  next(); // Passa para a próxima rota
});


// Caminhos
const clientDistPath = path.join(__dirname, '../../web/dist');
const videoCallPath = path.join(__dirname, 'public/video-call'); 

// ------------------------------------------------
// 📹 ROTA DE VÍDEO CHAMADA
// ------------------------------------------------
app.use('/video-call', express.static(videoCallPath));

app.get('/video-call', (req, res) => {
  res.sendFile(path.join(videoCallPath, 'index.html'));
});

// ------------------------------------------------
// 🔥 VITE.JS e Rotas da Aplicação Principal
// ------------------------------------------------
app.use(express.static(clientDistPath));

// Suas rotas API
app.use('/api/user', userRoutes);

// Catch-all SPA
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

// ------------------------------------------------
// 🔥 PM2 METRICS: Monitorar Usuários Socket
// ------------------------------------------------
// Criamos uma métrica que consulta o Socket.io automaticamente
pm2.metric({
  name: 'Usuários Socket Ativos',
  value: () => io.engine.clientsCount // Pega a contagem real do engine do Socket.io
});

// Inicializa o serviço de Socket separado
initializeSocketIO(io);

server.listen(port, () => {
  console.log(`Servidor rodando: http://localhost:${port}`);
  console.log(`Video Call disponível em: http://localhost:${port}/video-call`);
});