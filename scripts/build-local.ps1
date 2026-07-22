$ErrorActionPreference = "Stop"

$jdkHome = "C:\Program Files\Java\jdk-25"
if (-not (Test-Path -Path $jdkHome)) {
    throw "JDK not found: $jdkHome"
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
javac -version
mvn -v
mvn clean package -DskipTests
