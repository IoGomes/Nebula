import prisma from "./lib/prisma.js";

const test = async () => {
  const users = await prisma.user.findMany();
  console.log(users);
};

test();
