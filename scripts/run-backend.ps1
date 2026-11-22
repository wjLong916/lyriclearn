# 后端启动脚本：mvn spring-boot:run（也可直接运行 target 下 jar）
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot\..\backend
$mvn = 'D:\Program Files\apache-maven-3.6.3\bin\mvn.cmd'
if (-not (Test-Path $mvn)) { $mvn = 'mvn' }
& $mvn spring-boot:run
