FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . /app
RUN javac -d out $(find src -name "*.java")
EXPOSE 8080
CMD ["sh", "-c", "java -cp out Main"]
