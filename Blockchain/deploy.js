const { ethers } = require("ethers");
const { Kafka } = require("kafkajs");
const fs = require("fs");
require("dotenv").config();

const kafka = new Kafka({
  clientId: "my-consumer",
  brokers: ["localhost:9092"],
});

const consumer = kafka.consumer({ groupId: "consumer-group" });
const topic = "IOTRACE";
let contract;

async function deploying() {
  const provider = new ethers.providers.JsonRpcProvider(process.env.RPC_URL);
  const wallet = new ethers.Wallet(process.env.PRIVATE_KEY, provider);
  const abi = fs.readFileSync("./Project_sol_Project.abi", "utf8");
  const binary = fs.readFileSync("./Project_sol_Project.bin", "utf8");
  const contractFactory = new ethers.ContractFactory(abi, binary, wallet);
  console.log("Deploying, please wait...");
  contract = await contractFactory.deploy();
  await contract.deployTransaction.wait(1);
  console.log(`Contract Address: ${contract.address}`);
}

async function connection() {
  await consumer.connect();
  await consumer.subscribe({ topic });
}

const run = async () => {
  await deploying();
  await connection();
  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      let mensaje = message.value.toString();
      if (mensaje.includes("|")) {
        const transactionResponseUUIDS = await contract.setTimeAndUUIDS(
          mensaje
        );
        const transactionReceipt = await transactionResponseUUIDS.wait(1);
        console.log("Transaction Appended");
      }
    },
  });
};

run().catch();
