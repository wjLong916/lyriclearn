# 前端启动脚本：npm run dev（http://localhost:5173）
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot\..\frontend
if (-not (Test-Path node_modules)) { npm install --registry=https://registry.npmmirror.com }
npm run dev
