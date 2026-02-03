FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:25-jre AS runner
WORKDIR /app
COPY --from=builder /app/build/libs/YomiageKt.jar .
ENTRYPOINT [ "java", "-jar", "YomiageKt.jar"]
