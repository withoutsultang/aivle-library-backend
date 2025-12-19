#!/bin/bash
set -e

cd /opt/app

JAR=$(ls -1 *.jar | head -n 1)

nohup java -jar "$JAR" > /opt/app/app.log 2>&1 &