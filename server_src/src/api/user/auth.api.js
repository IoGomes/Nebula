import { createUser, findByUserId, findUserByEmail } from "../../services/user.services.js";
import bcrypt from "bcrypt";

import {
    generateAccessToken,
    generateRefreshToken
} from "../../lib/jwt.js";

import {
    saveRefreshToken,
    refreshAccessToken
} from "../../services/auth.services.js";


export const registerUser = async (req, res) => {
    try {
        const { username, password, email, phone_number } = req.body;

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = await createUser({
            username,
            password: hashedPassword,
            email,
            phone_number,
        });

        if (newUser.success) {
            res.status(201).json(newUser);
        } else {
            res.status(409).json(newUser)
        }
    } catch (err) {
        console.error(err);
        res.status(500).json();
    }
};

export const loginUser = async (req, res) => {
    const { email, password } = req.body;

    const response = await findUserByEmail(email);
    if (!response.success) return res.status(400).json(response);

    const user = response.user

    const match = await bcrypt.compare(password, user.password);
    if (!match) return res.status(401).json({ error: "Senha incorreta" });

    // const accessToken = generateAccessToken(user);
    // const refreshToken = generateRefreshToken(user);

    // await saveRefreshToken(user.user_id, refreshToken);

    return res.json({
        success: true,
        userId: user.user_id
        // accessToken,
        // refreshToken
    });
};


export const refreshTokenAPI = async (req, res) => {
    try {
        const { refreshToken } = req.body;

        if (!refreshToken)
            return res.status(400).json({ error: "Refresh token faltando" });

        const newAccess = await refreshAccessToken(refreshToken);

        res.json({
            accessToken: newAccess
        });
    } catch (err) {
        res.status(401).json({ error: err.message });
    }
};

export const getUserById = async (req, res) => {
    try {
        const id = Number(req.query.id);
        console.log(id)
        if (!id) {
            return res.status(400).json({
                error: "O parâmetro 'id' é obrigatório. Exemplo: ?id=4"
            });
        }
        

        const userData = await findByUserId(id);

        if (userData.success) {
            res.status(200).json({userData})
        } else {
            res.status(401).json({userData})

        }
    } catch (err) {
        res.status(500).json()
    }

};