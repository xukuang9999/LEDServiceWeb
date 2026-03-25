FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY zhglxt-web/target/zhglxt.war /app/zhglxt.war

ENV ZHGLXT_PROFILE=/tmp/runtime-userfiles

ENTRYPOINT ["sh", "-c", "mkdir -p \"${ZHGLXT_PROFILE}/upload\" && exec java ${JAVA_OPTS} -jar /app/zhglxt.war --server.port=${PORT:-8080} --zhglxt.profile=${ZHGLXT_PROFILE}"]
