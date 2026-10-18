#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ ! -f target/spire-hotseat.jar ]; then sh ./mvnw -B package; fi
if [ "$(uname -s)" = "Darwin" ]; then
  exec java -XstartOnFirstThread -jar target/spire-hotseat.jar "$@"
fi
exec java -jar target/spire-hotseat.jar "$@"
