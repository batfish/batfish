#!/usr/bin/env bash
export BATFISH_SOURCED_SCRIPT="$BASH_SOURCE"
export OLD_PWD="$PWD"
package() {
   local TARGET="${OLD_PWD}/batfish.deb"
   local WORKING=$(mktemp -d)
   local BATFISH_TOOLS_PATH="$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT))"
   local BATFISH_PATH="$(readlink -f ${BATFISH_TOOLS_PATH}/..)"
   local BATFISH_JAR=$BATFISH_PATH/projects/batfish/out/batfish.jar
   local BATFISH_PROPERTIES=$BATFISH_PATH/projects/batfish/out/config.properties
   local BATFISH_CLIENT_JAR=$BATFISH_PATH/projects/batfish-client/out/batfish-client.jar
   local BATFISH_CLIENT_PROPERTIES=$BATFISH_PATH/projects/batfish-client/out/config.properties
   local COORDINATOR_JAR=$BATFISH_PATH/projects/coordinator/out/coordinator.jar
   local COORDINATOR_PROPERTIES=$BATFISH_PATH/projects/coordinator/out/config.properties
   local COORDINATOR_KEYSTORE=$BATFISH_PATH/projects/coordinator/out/selfsigned.jks
   local BASE=$WORKING/debian
   if [ ! -f "$BATFISH_JAR" ]; then
      echo "Missing $BATFISH_JAR" >&2
      return 1
   fi
   if [ ! -f "$COORDINATOR_JAR" ]; then
      echo "Missing $COORDINATOR_JAR" >&2
      return 1
   fi
   if [ ! -f "$BATFISH_CLIENT_JAR" ]; then
      echo "Missing $BATFISH_CLIENT_JAR" >&2
      return 1
   fi
   mkdir -p $BASE/DEBIAN
   chmod 0755 $BASE/DEBIAN
   cat > $BASE/DEBIAN/control <<EOF
Package: batfish
Version: 0.1debug
Section: web
Priority: optional
Architecture: all
Maintainer: Ari Fogel <ari@intentionet.com>
Description: network configuration analysis tool
 Batfish is a network configuration analysis tool developed 
 jointly by researchers at University of California, Los Angeles;
 University of Southern California; and Microsoft Research. 
 Though its individual modules have various applications,
 its primary purpose is to detect bugs in network configurations.

EOF
   chmod 0644 $BASE/DEBIAN/control
   mkdir -p $BASE/usr/share/batfish/batfish
   chmod 0755 $BASE/usr/
   chmod 0755 $BASE/usr/share/
   chmod 0755 $BASE/usr/share/batfish
   chmod 0755 $BASE/usr/share/batfish/batfish
   mkdir -p $BASE/usr/share/batfish/batfish-client
   chmod 0755 $BASE/usr/share/batfish/batfish-client
   mkdir -p $BASE/usr/share/batfish/coordinator
   chmod 0755 $BASE/usr/share/batfish/coordinator
   mkdir -p $BASE/usr/share/doc/batfish
   chmod 0755 $BASE/usr/share/doc
   chmod 0755 $BASE/usr/share/doc/batfish
   mkdir -p $BASE/etc/batfish
   chmod 0750 $BASE/etc/batfish
   gzip --best > $BASE/usr/share/doc/batfish/changelog.gz <<EOF
batfish (0.1debug) trusty; urgency=low

  * Initial Release: initial release

 -- Ari Fogel <ari@intentionet.com>  Thu, 07 Apr 2016 17:49:41 -0700

EOF
   chmod 0644 $BASE/usr/share/doc/batfish/changelog.gz
   cat > $BASE/usr/share/doc/batfish/copyright <<EOF
Name: batfish
Maintainer: Ari Fogel <ari@intentionet.com>
Source: https://github.com/arifogel/batfish
Copyright: 2013-2016 Ari Fogel <ari@intentionet.com>
License: Apache-2.0
 Internal components may have different licenses. Their license
 files may be extracted from the included jar files.
 On Debian systems, the full text of the Apache 2.0
 License can be found in the file
 '/usr/share/common-licenses/Apache-2.0'.
EOF
   chmod 0644 $BASE/usr/share/doc/batfish/copyright
   cp $BATFISH_JAR $BASE/usr/share/batfish/batfish/
   chmod 0644 $BASE/usr/share/batfish/batfish/batfish.jar
   cp $BATFISH_PROPERTIES $BASE/etc/batfish/batfish.properties
   chmod 0640 $BASE/etc/batfish/batfish.properties
   ln -s /etc/batfish/batfish.properties $BASE/usr/share/batfish/batfish/config.properties
   cp $BATFISH_CLIENT_JAR $BASE/usr/share/batfish/batfish-client/
   chmod 0644 $BASE/usr/share/batfish/batfish-client/batfish-client.jar
   cp $BATFISH_CLIENT_PROPERTIES $BASE/etc/batfish/batfish-client.properties
   chmod 0640 $BASE/etc/batfish/batfish-client.properties
   ln -s /etc/batfish/batfish-client.properties $BASE/usr/share/batfish/batfish-client/config.properties
   cp $COORDINATOR_JAR $BASE/usr/share/batfish/coordinator/
   chmod 0644 $BASE/usr/share/batfish/coordinator/coordinator.jar
   cp $COORDINATOR_PROPERTIES $BASE/etc/batfish/coordinator.properties
   chmod 0640 $BASE/etc/batfish/coordinator.properties
   ln -s /etc/batfish/coordinator.properties $BASE/usr/share/batfish/coordinator/config.properties
   cp $COORDINATOR_KEYSTORE $BASE/etc/batfish/selfsigned.jks
   chmod 0640 $BASE/etc/batfish/selfsigned.jks
   ln -s /etc/batfish/selfsigned.jks $BASE/usr/share/batfish/coordinator/selfsigned.jks
   mkdir -p $BASE/etc/init
   chmod 0755 $BASE/etc
   chmod 0755 $BASE/etc/init
   cat > $BASE/etc/init/batfish.conf <<EOF
author "Ari Fogel"
description "start and stop batfish service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

env APPUSER="batfish"
env APPDIR="/usr/bin"
env APPBIN="java"
env APPARGS="-jar /usr/share/batfish/batfish/batfish.jar -logfile /var/log/batfish/batfish.log -servicemode -register true"

respawn

script
  exec su - \$APPUSER -c "\$APPDIR/\$APPBIN \$APPARGS"
end script
EOF
   chmod 0644 $BASE/etc/init/batfish.conf
   cat > $BASE/etc/init/coordinator.conf <<EOF
author "Ari Fogel"
description "start and stop batfish coordinator service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

env APPUSER="batfish"
env APPDIR="/usr/bin"
env APPBIN="java"
env APPARGS="-jar /usr/share/batfish/coordinator/coordinator.jar -loglevel debug -logfile /var/log/batfish/coordinator.log -servicehost localhost -containerslocation /var/batfish"

respawn

script
  exec su - \$APPUSER -c "\$APPDIR/\$APPBIN \$APPARGS"
end script
EOF
   chmod 0644 $BASE/etc/init/coordinator.conf
   cat > $BASE/DEBIAN/postinst <<EOF
#!/bin/bash

set -e

initctl reload-configuration

if grep '^batfish:' /etc/group > /dev/null 2>&1; then
   echo "Not creating group 'batfish' since it already exists"
else
   addgroup --system batfish
fi
if id -u batfish > /dev/null 2>&1; then
   echo "Not creating user 'batfish' since it already exists"
else
   useradd -rs /bin/bash -d /var/batfish -g batfish batfish
fi
mkdir -p /var/batfish
chown batfish:batfish /var/batfish
chmod 0750 /var/batfish
chown batfish:batfish /etc/batfish
chown batfish:batfish /etc/batfish/batfish.properties
chown batfish:batfish /etc/batfish/batfish-client.properties
chown batfish:batfish /etc/batfish/coordinator.properties
chown batfish:batfish /etc/batfish/selfsigned.jks
mkdir -p /var/log/batfish
chown batfish:batfish /var/log/batfish
chmod 0750 /var/log/batfish
service coordinator restart
service batfish restart
exit 0
EOF
   chmod 0755 $BASE/DEBIAN/postinst
   echo /etc/init/batfish.conf > $BASE/DEBIAN/conffiles
   echo /etc/init/coordinator.conf >> $BASE/DEBIAN/conffiles
   echo /etc/batfish/batfish.properties >> $BASE/DEBIAN/conffiles
   echo /etc/batfish/batfish-client.properties >> $BASE/DEBIAN/conffiles
   echo /etc/batfish/coordinator.properties >> $BASE/DEBIAN/conffiles
   echo /etc/batfish/selfsigned.jks >> $BASE/DEBIAN/conffiles
   chmod 0644 $BASE/DEBIAN/conffiles
   cd $WORKING
   fakeroot dpkg --build debian
   cp *.deb $OLD_PWD/batfish.deb
   cd $OLD_PWD
   rm -rf $WORKING
}
package
