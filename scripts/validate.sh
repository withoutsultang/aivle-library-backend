#!/bin/bash
set -e

curl -fsS http://localhost:8080/actuator/health > /dev/null