#!/bin/bash
# macOS GUI 启动器：双击运行 Spire Duel（libGDX 图形界面）。
# 需要已安装 JDK 17+（系统已安装 Temurin 17）。
cd "$(dirname "$0")/pvp-game" || exit 1
exec ./run-gui.sh
