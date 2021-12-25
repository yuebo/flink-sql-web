#!/usr/bin/env bash
rm -f *.jar
mvn clean install -f ../pom.xml -Dmaven.test.skip=true
cp ../target/flink-sql-web.jar app.jar
docker build . -t flink-sql-web:0.1
docker run --name -d flink-sql-web -p 8222:8222 flink-sql-web:0.1