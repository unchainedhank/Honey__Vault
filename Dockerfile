#FROM openjdk:8-jdk-alpine
FROM openjdk:11-jre

# 安装Python和依赖项

COPY target /app/
ENV TZ=Asia/Shanghai


ENTRYPOINT ["nohup","java", "-jar", "./app/HoneyVault-0.0.1-SNAPSHOT.jar","-Dfile.encoding=utf-8"]
#cd#ENTRYPOINT ["java", "-jar", "./app/RiskPredict-0.0.1-SNAPSHOT.jar","--server.port=8082"]
