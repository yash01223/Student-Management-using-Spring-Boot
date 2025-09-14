FROM openjdk:22-jdk
ADD ./target/Student-Management-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]