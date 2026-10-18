#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ ! -f target/spire-hotseat.jar ]; then sh ./mvnw -B package; fi
exec java -cp 'target/spire-hotseat.jar:target/lib/*' lab.pvp.view.cui.ConsoleLauncher "$@"
