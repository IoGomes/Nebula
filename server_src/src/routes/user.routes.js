import { Router } from "express";
import { registerUser, refreshTokenAPI,loginUser, getUserById } from "../api/user/auth.api.js";


const router = Router();

// POST /api/user/register
router.post("/register", registerUser);

// // POST /api/user/login
router.post("/login", loginUser);

router.post("/refresh", refreshTokenAPI);

router.get("/get-by-id", getUserById)

export default router;
