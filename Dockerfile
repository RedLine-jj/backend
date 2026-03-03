FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY *.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]