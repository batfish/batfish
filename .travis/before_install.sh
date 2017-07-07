#!/usr/bin/env bash

### install java 8
echo "debconf shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
echo "debconf shared/accepted-oracle-license-v1-1 seen true" | sudo debconf-set-selections
sudo apt update
sudo -E apt-get -yq --no-install-suggests --no-install-recommends --force-yes install oracle-java8-installer || exit 1

### install maven 3.3.9
MAVEN_VERSION=3.3.9
MAVEN_DIR=apache-maven-${MAVEN_VERSION}
MAVEN_TARBALL=${MAVEN_DIR}-bin.tar.gz
MAVEN_URL=http://www-eu.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/$MAVEN_TARBALL

cd /opt
sudo wget $MAVEN_URL || exit 1
sudo tar -xf $MAVEN_TARBALL || exit 1
sudo ln -s $MAVEN_DIR maven
ls -al
sudo tee /etc/profile.d/maven.sh <<EOF
export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=\${M2_HOME}/bin:\${PATH}
EOF
cat /etc/profile.d/maven.sh || exit 1

