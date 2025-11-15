import { verifyAccessToken } from "../lib/jwt.js";

export const requireAuth = (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(" ")[1];

    if (!token) return res.status(401).json({ error: "Token não informado" });

    const decoded = verifyAccessToken(token);

    req.user = decoded; 
    next();
  } catch (err) {
    return res.status(401).json({ error: "Token inválido ou expirado" });
  }
};
