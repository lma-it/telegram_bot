FROM openjdk:17-jdk-slim
WORKDIR /app
RUN mkdir -p /app/tmp
COPY src/main/resources/template.docx /app/tmp/
COPY target/Telegram_Bot_for_SNP_Technology-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]