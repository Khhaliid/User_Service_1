# Stage 1: Build
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

# Kopiera pom och src
COPY pom.xml ./
COPY src ./src

# Dummy secret för build
ENV JWT_SECRET=dummysecret
ENV JWT_EXPIRATION=3600000
ENV BACKEND_PORT=8080
ENV MASTER_KEY=some_secure_key

# Bygg jar
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Uppdatera paket och installera curl (valfritt)
RUN apk update && apk upgrade --no-cache && apk add curl

# Sätt fallback JWT_SECRET och BACKEND_PORT
ENV JWT_SECRET=dummysecret
ENV JWT_EXPIRATION=3600000
ENV BACKEND_PORT=8080
ENV MASTER_KEY=some_secure_key

# Kopiera jar från build
COPY --from=build /app/target/*.jar app.jar

# Skapa icke-root-användare
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Exponera port
EXPOSE 8080

# Starta applikationen
CMD ["java", "-jar", "/app/app.jar"]
