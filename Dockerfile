# --- build stage: JAR 빌드 (러너에서만, EC2 부담 없음) ---
FROM gradle:8.14-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle bootJar --no-daemon

# --- run stage: JRE만, 가벼움 ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
EXPOSE 8080
# 설정(application.properties)·업로드는 이미지에 안 넣음 → EC2 볼륨으로 주입
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
