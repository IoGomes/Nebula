import prisma from "../lib/prisma.js";
import {
  generateAccessToken,
  generateRefreshToken,
  verifyRefreshToken
} from "../lib/jwt.js";

export const saveRefreshToken = async (user_id, token) => {
  const expires = new Date();
  expires.setDate(expires.getDate() + 7);

  return prisma.refreshToken.create({
    data: { user_id, token, expiresAt: expires }
  });
};

export const refreshAccessToken = async (token) => {
  const decoded = verifyRefreshToken(token);

  const stored = await prisma.refreshToken.findUnique({
    where: { token }
  });

  if (!stored) throw new Error("Refresh token inválido");

  if (stored.expiresAt < new Date())
    throw new Error("Refresh token expirado");

  const accessToken = generateAccessToken({ user_id: decoded.user_id });

  return accessToken;
};

export const deleteRefreshToken = async (token) => {
  return prisma.refreshToken.delete({
    where: { token }
  });
};
