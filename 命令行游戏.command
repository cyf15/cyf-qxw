#!/bin/bash
# macOS 命令行启动器：双击打开终端运行 CUI 版。
cd "$(dirname "$0")/pvp-game" || exit 1
exec ./run-cui.sh
