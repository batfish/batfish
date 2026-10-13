# NOCFlow's per-tenant Batfish engine.
#
# Build the jar first, from the repository root:
#   bazel build //projects/engine:engine_main_deploy.jar
#   docker build -f projects/engine/Dockerfile -t nocflow/batfish-engine bazel-bin/projects/engine
FROM eclipse-temurin:25-jre

RUN useradd --system --uid 10001 engine && mkdir -p /var/lib/engine && chown engine /var/lib/engine
USER engine

COPY engine_main_deploy.jar /opt/engine/engine.jar

ENV ENGINE_PORT=9800 \
    ENGINE_STORAGE=/var/lib/engine \
    ENGINE_MAX_DATAPLANES=4
EXPOSE 9800

# Data planes are memory-bound; size the heap per tenant with JAVA_TOOL_OPTIONS (e.g. -Xmx4g).
ENTRYPOINT ["java", "-jar", "/opt/engine/engine.jar"]
