#!/usr/bin/env bash
./build.sh
java -jar target/mywebapp-1.0-SNAPSHOT-fat.jar --redeploy="**/*.groovy" --onRedeploy="./build.sh"
