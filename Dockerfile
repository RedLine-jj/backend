FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ./build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]