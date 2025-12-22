FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre AS runner
WORKDIR /app
COPY --from=builder /app/build/libs/YomiageKt.jar .
ENTRYPOINT [ "java", "-jar", "YomiageKt.jar"]
