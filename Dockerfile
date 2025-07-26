# This is the demo Dockerfile for the generated template project, please change accordingly before building image from it.
# Run the following command to build image: docker build -f ./Dockerfile --build-arg APP_FILE=demo-0.0.1-SNAPSHOT.jar -t demo:latest .
FROM openjdk:17-jdk-slim
# 在容器中创建一个目录来存放应用程序
RUN mkdir /app
WORKDIR /app
COPY bootstarp/target/authService.jar /app/authService.jar
# 容器启动时运行的命令，启动 Spring Boot 应用程序
CMD ["java", "-jar", "authService.jar"]
