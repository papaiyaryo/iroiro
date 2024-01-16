echo off
start "server" cmd.exe /k "java UmiServer3 2 50 50"
start "player" cmd.exe /k  "java Robot2 127.0.0.1 ryo"
start "bot" cmd.exe /k  "java Robot2 127.0.0.1 bot"