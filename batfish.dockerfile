FROM ubuntu:20.04

# ASSETS is the directory containing allinone-bundle.jar (the Batfish jar)
# and questions/ directory (containing question templates to be loaded by Batfish)
ARG ASSETS

# Make /data dir available to any user, so this container can be run by any user
RUN mkdir -p /data
RUN chmod a+rw /data
COPY ${ASSETS} ./
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
ENV JAVA_LIBRARY_PATH /usr/lib
ENV HOME /data

# Base package setup
RUN apt-get update \
    && apt-get install -y openjdk-11-jre-headless \
    && apt-get upgrade -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /var/cache/oracle*

# Batfish
EXPOSE 9996-9997
CMD ["java", \
    "-XX:-UseCompressedOops", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=80", \
    "-cp", "allinone-bundle.jar", \
    "-Dlog4j.configurationFile=log4j2.yaml", \
    "org.batfish.allinone.Main", \
    "-runclient", "false", \
    "-loglevel", "warn", \
    "-coordinatorargs", "-templatedirs questions -containerslocation /data/containers"]
