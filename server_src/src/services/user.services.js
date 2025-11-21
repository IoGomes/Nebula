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

export const findByUserId = async (userId) => {
    try {
        const user = await prisma.user.findUnique({
            where:{
                user_id: userId
            },
            select: {
                user_id: true,
                phone_number: true,
                username: true
            }
        })

        console.log(user)
        if (user === null) {
            return {success: false, reason: 'Não existe usuário cadastrado com esse ID fornecido'}
        }
        return {success: true, user}
    } catch (err) {
        console.log(err)
        return {success: false, reason: 'Erro não identificado no servidor'}
    }
}