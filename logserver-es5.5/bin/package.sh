#! /bin/sh

mvn clean package dependency:copy-dependencies -DoutputDirectory=target/libs -DskipTests