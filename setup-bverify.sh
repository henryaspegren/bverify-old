#!/bin/bash

echo '-----------------setting up b_verify-------------------------'

rm -rf server && rm -rf client && mkdir server && mkdir client

echo '-----------generating protobuf serialization code------------'
mkdir src/generated
protoc --java_out=src/generated serialization.proto 

echo '---------------setting up bitcoind path----------------------'
source set-env.sh

echo '--------------building b_verify with maven-------------------'
mvn clean install

echo '-----------------------completed-----------------------------'



