import jwt from "jsonwebtoken";
import dotenv from "dotenv";
dotenv.config();

const ACCESS_SECRET = process.env.JWT_SECRET;
const REFRESH_SECRET = process.env.JWT_REFRESH_SECRET;

// Gera JWT de 8h
export const generateAccessToken = (user) => {
  return jwt.sign(
    { user_id: user.user_id },
    ACCESS_SECRET,
    { expiresIn: "8h" }
  );
};

// Gera Refresh Token válido por 7 dias
export const generateRefreshToken = (user) => {
  return jwt.sign(
    { user_id: user.user_id },
    REFRESH_SECRET,
    { expiresIn: "7d" }
  );
};

export const verifyAccessToken = (token) => {
  return jwt.verify(token, ACCESS_SECRET);
};

export const verifyRefreshToken = (token) => {
  return jwt.verify(token, REFRESH_SECRET);
};
