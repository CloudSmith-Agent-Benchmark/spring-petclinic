#!/bin/bash

# Build the application if not already built
if [ ! -f "target/spring-petclinic-3.4.0-SNAPSHOT.jar" ]; then
    ./mvnw clean package -DskipTests
fi

# Set Java options for memory and GC
JAVA_OPTS="-Xmx512m -Xms256m"

# Set active profile to postgres as per k8s config
SPRING_PROFILES_ACTIVE=postgres

# Start the application
java $JAVA_OPTS -jar target/spring-petclinic-3.4.0-SNAPSHOT.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE