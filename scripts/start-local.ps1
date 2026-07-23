$ErrorActionPreference = "Stop"

if (-not $env:MYSQL_PASSWORD) {
    throw "MYSQL_PASSWORD is required. Example: `$env:MYSQL_PASSWORD=`"your-password`""
}

if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "local-dev-jwt-secret-at-least-32-characters"
}

$jar = "target\campus-activity-1.0.0-SNAPSHOT.jar"
if (-not (Test-Path -Path $jar)) {
    throw "Jar not found: $jar. Run scripts\build-local.ps1 first."
}

java -jar $jar
