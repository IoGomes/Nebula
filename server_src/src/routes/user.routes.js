import { Router } from "express";
import { registerUser, refreshTokenAPI,loginUser } from "../api/user/auth.api.js";


const router = Router();

// POST /api/user/register
router.post("/register", registerUser);

// // POST /api/user/login
router.post("/login", loginUser);

router.post("/refresh", refreshTokenAPI);

export default router;
