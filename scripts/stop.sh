#!/bin/bash
set -e

PID=$(lsof -ti:8080 || true)
if [ -n "$PID" ]; then
  kill -15 "$PID"
  sleep 5
fi