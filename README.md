# IOT-BC-TRACE

The objective of the project is to define a prototype tool to store traces of IoT data on a public platform like Ethereum.
In order to guarantee scalability and performance, the tool must also consider an additional message broker component based on Apache Kafka

Expected results are:
  -Data immutability and proof evidence over the data
  -Performance analysis when changing the rate of IoT messages produced
  -Implementation of the prototypes

TECHNICAL IMPLEMENTATION
1. Run Kafka:
  KAFKA_HOME=/opt/kafka
  $KAFKA_HOME/bin/connect-distributed.sh $KAFKA_HOME/config/connect-distributed.properties


2. Go to the simulator and run it too:
  docker-compose up -d
  docker-compose logs -f


3. Connect the MQTT connector with Kafka:
  curl -s -X POST -H 'Content-Type: application/json' http://localhost:8083/connectors -d @mqtt_connect.json


4. Run the application that filters the streamings
  mvn clean package                                 -----> COMPILE
  mvn exec:java -Dexec.mainClass=myapps.Streamings  -----> EXECUTE


5. Update to the latest version
  nvm use v(Version)


6. Go to the folder that contains the smart contract and its deployer
  node deploy.js

7. In the console the address of the contract is going to appear, with it 
    we should go to the Goerli network webpage and check its traceability.
