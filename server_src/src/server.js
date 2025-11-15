import express from "express";
import userRoutes from "./routes/user.routes.js";

const app = express();
const port = 3000;

app.use(express.json());


// Usar as rotas com prefixo
app.use('/api/user', userRoutes);

app.listen(port, () => {
  console.log(`Servidor rodando em http://localhost:${port}`);
});
