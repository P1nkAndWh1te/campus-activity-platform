$ErrorActionPreference = "Stop"

java -version
javac -version
mvn -v

mvn clean package -DskipTests
