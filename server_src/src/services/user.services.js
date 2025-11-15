import prisma from "../lib/prisma.js";

export const createUser = async (data) => {
    try {
        const registerUser = await prisma.user.create({
            data: data
        })

        return {success: true, registerUser}
    } catch (err) {

        if (err.code === 'P2002') {
            return {success: false, reason: 'Email ou telefone já registrado'}
        }

        console.log(err)
        return {success: false, reason: 'Erro não identificado no servidor'}
    }
}

export const findUserByEmail = async (email) => {
    try {
        const user = await prisma.user.findUnique({
            where: {
                email: email
            }
        })
        // console.log(user)
        return {success: true, user}
    } catch (err) {
        console.log(err)
        return {success: false, reason: 'Erro não identificado no servidor'}
    }
}