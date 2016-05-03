#!/usr/bin/env bash
export BATFISH_SOURCED_SCRIPT="$BASH_SOURCE"
export OLD_PWD="$PWD"
package() {
   local TARGET="${OLD_PWD}/batfish.deb"
   local WORKING=$(mktemp -d)
   local BATFISH_DST_DIR=/usr/share/batfish
   local CONF_DST_DIR=/etc/batfish
   local DOC_DST_DIR=/usr/share/doc/batfish
   local INIT_DST_DIR=/etc/init
   local BATFISH_TOOLS_PATH="$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT))"
   local BATFISH_PATH="$(readlink -f ${BATFISH_TOOLS_PATH}/..)"
   local BATFISH_SERVICE_NAME=batfish
   local BATFISH_INIT=${BATFISH_SERVICE_NAME}.conf
   local BATFISH_INIT_DST=$INIT_DST_DIR/$BATFISH_INIT
   local BATFISH_JAR=batfish.jar
   local BATFISH_JAR_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_JAR
   local BATFISH_JAR_DST=$BATFISH_DST_DIR/$BATFISH_JAR
   local BATFISH_PROPERTIES=batfish.properties
   local BATFISH_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_PROPERTIES
   local BATFISH_PROPERTIES_LINK=$BATFISH_DST_DIR/$BATFISH_PROPERTIES
   local BATFISH_PROPERTIES_DST=$CONF_DST_DIR/$BATFISH_PROPERTIES
   local CLIENT_JAR=batfish-client.jar
   local CLIENT_JAR_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_JAR
   local CLIENT_JAR_DST=$BATFISH_DST_DIR/$CLIENT_JAR
   local CLIENT_PROPERTIES=client.properties
   local CLIENT_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_PROPERTIES
   local CLIENT_PROPERTIES_LINK=$BATFISH_DST_DIR/$CLIENT_PROPERTIES
   local CLIENT_PROPERTIES_DST=$CONF_DST_DIR/$CLIENT_PROPERTIES
   local COORDINATOR_SERVICE_NAME=coordinator
   local COORDINATOR_INIT=${COORDINATOR_SERVICE_NAME}.conf
   local COORDINATOR_INIT_DST=$INIT_DST_DIR/$COORDINATOR_INIT
   local COORDINATOR_JAR=coordinator.jar
   local COORDINATOR_JAR_SRC=$BATFISH_PATH/projects/coordinator/out/$COORDINATOR_JAR
   local COORDINATOR_JAR_DST=$BATFISH_DST_DIR/$COORDINATOR_JAR
   local COORDINATOR_PROPERTIES=coordinator.properties
   local COORDINATOR_PROPERTIES_SRC=$BATFISH_PATH/projects/coordinator/out/$COORDINATOR_PROPERTIES
   local COORDINATOR_PROPERTIES_LINK=$BATFISH_DST_DIR/$COORDINATOR_PROPERTIES
   local COORDINATOR_PROPERTIES_DST=$CONF_DST_DIR/$COORDINATOR_PROPERTIES
   local COORDINATOR_KEYSTORE=selfsigned.jks
   local COORDINATOR_KEYSTORE_SRC=$BATFISH_PATH/projects/coordinator/out/$COORDINATOR_KEYSTORE
   local COORDINATOR_KEYSTORE_LINK=$BATFISH_DST_DIR/$COORDINATOR_KEYSTORE
   local COORDINATOR_KEYSTORE_DST=$CONF_DST_DIR/$COORDINATOR_KEYSTORE
   local CHANGELOG=changelog.gz
   local CHANGELOG_DST=$DOC_DST_DIR/$CHANGELOG
   local COPYRIGHT=copyright
   local COPYRIGHT_DST=$DOC_DST_DIR/$COPYRIGHT
   local BATFISH_HOME=/var/batfish
   local BATFISH_LOG_DIR=/var/log/batfish
   local BATFISH_LOG=batfish.log
   local BATFISH_LOG_DST=$BATFISH_LOG_DIR/$BATFISH_LOG
   local COORDINATOR_LOG=coordinator.log
   local COORDINATOR_LOG_DST=$BATFISH_LOG_DIR/$COORDINATOR_LOG
   local BASE=$WORKING/debian
   local DEBIAN_DIR=$BASE/DEBIAN
   local CONTROL_FILE=$DEBIAN_DIR/control
   local POSTINST_FILE=$DEBIAN_DIR/postinst
   local CONFFILES_FILE=$DEBIAN_DIR/conffiles
   local BATFISH_USER=batfish
   local DEB_OUTPUT=debian.deb
   if [ ! -f "$BATFISH_JAR_SRC" ]; then
      echo "Missing $BATFISH_JAR_SRC" >&2
      return 1
   fi
   if [ ! -f "$COORDINATOR_JAR_SRC" ]; then
      echo "Missing $COORDINATOR_JAR_SRC" >&2
      return 1
   fi
   if [ ! -f "$CLIENT_JAR_SRC" ]; then
      echo "Missing $CLIENT_JAR_SRC" >&2
      return 1
   fi
   mkdir -p $DEBIAN_DIR
   cat > $CONTROL_FILE <<EOF
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
   mkdir -p $BASE/$BATFISH_DST_DIR
   mkdir -p $BASE/$CONF_DST_DIR
   mkdir -p $BASE/$DOC_DST_DIR
   gzip --best > $BASE/$CHANGELOG_DST <<EOF
batfish (0.1debug) trusty; urgency=low

  * Initial Release: initial release

 -- Ari Fogel <ari@intentionet.com>  Thu, 07 Apr 2016 17:49:41 -0700

EOF
   cat > $BASE/$COPYRIGHT_DST <<EOF
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
   cp $BATFISH_JAR_SRC $BASE/$BATFISH_JAR_DST
   cp $BATFISH_PROPERTIES_SRC $BASE/$BATFISH_PROPERTIES_DST
   ln -s $BATFISH_PROPERTIES_DST $BASE/$BATFISH_PROPERTIES_LINK
   cp $CLIENT_JAR_SRC $BASE/$CLIENT_JAR_DST
   cp $CLIENT_PROPERTIES_SRC $BASE/$CLIENT_PROPERTIES_DST
   ln -s $CLIENT_PROPERTIES_DST $BASE/$CLIENT_PROPERTIES_LINK
   cp $COORDINATOR_JAR_SRC $BASE/$COORDINATOR_JAR_DST
   cp $COORDINATOR_PROPERTIES_SRC $BASE/$COORDINATOR_PROPERTIES_DST
   ln -s $COORDINATOR_PROPERTIES_DST $BASE/$COORDINATOR_PROPERTIES_LINK
   cp $COORDINATOR_KEYSTORE_SRC $BASE/$COORDINATOR_KEYSTORE_DST
   ln -s $COORDINATOR_KEYSTORE_DST $BASE/$COORDINATOR_KEYSTORE_LINK
   mkdir -p $BASE/$INIT_DST_DIR
   cat > $BASE/$BATFISH_INIT_DST <<EOF
author "Ari Fogel"
description "start and stop batfish service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

env APPUSER="$BATFISH_USER"
env APPDIR="/usr/bin"
env APPBIN="java"
env APPARGS="-jar $BATFISH_JAR_DST -logfile $BATFISH_LOG_DST -servicemode -register true"

respawn

script
  exec su - \$APPUSER -c "\$APPDIR/\$APPBIN \$APPARGS"
end script
EOF
   cat > $BASE/$COORDINATOR_INIT_DST <<EOF
author "Ari Fogel"
description "start and stop batfish coordinator service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

env APPUSER="$BATFISH_USER"
env APPDIR="/usr/bin"
env APPBIN="java"
env APPARGS="-jar $COORDINATOR_JAR_DST -loglevel debug -logfile $COORDINATOR_LOG_DST -servicehost localhost -containerslocation $BATFISH_HOME"

respawn

script
  exec su - \$APPUSER -c "\$APPDIR/\$APPBIN \$APPARGS"
end script
EOF
   cat > $POSTINST_FILE <<EOF
#!/bin/bash

set -e

initctl reload-configuration

if grep '^$BATFISH_USER:' /etc/group > /dev/null 2>&1; then
   echo "Not creating group '$BATFISH_USER' since it already exists"
else
   addgroup --system $BATFISH_USER
fi
if id -u $BATFISH_USER > /dev/null 2>&1; then
   echo "Not creating user '$BATFISH_USER' since it already exists"
else
   useradd -rs /bin/bash -d $BATFISH_HOME -g $BATFISH_USER $BATFISH_USER
fi
mkdir -p $BATFISH_HOME
chown $BATFISH_USER:$BATFISH_USER $BATFISH_HOME
chmod 0750 $BATFISH_HOME
chown $BATFISH_USER:$BATFISH_USER $CONF_DST_DIR
chown $BATFISH_USER:$BATFISH_USER $BATFISH_PROPERTIES_DST
chmod 0664 $BATFISH_PROPERTIES_DST
chown $BATFISH_USER:$BATFISH_USER $CLIENT_PROPERTIES_DST
chmod 0660 $CLIENT_PROPERTIES_DST
chown $BATFISH_USER:$BATFISH_USER $COORDINATOR_PROPERTIES_DST
chmod 0660 $COORDINATOR_PROPERTIES_DST
chown $BATFISH_USER:$BATFISH_USER $COORDINATOR_KEYSTORE_DST
chmod 0660 $COORDINATOR_KEYSTORE_DST
mkdir -p $BATFISH_LOG_DIR
chown $BATFISH_USER:$BATFISH_USER $BATFISH_LOG_DIR
chmod 0750 $BATFISH_LOG_DIR
service $COORDINATOR_SERVICE_NAME restart
service $BATFISH_SERVICE_NAME restart
exit 0
EOF
   echo $BATFISH_INIT_DST > $CONFFILES_FILE
   echo $COORDINATOR_INIT_DST >> $CONFFILES_FILE
   echo $BATFISH_PROPERTIES_DST >> $CONFFILES_FILE
   echo $CLIENT_PROPERTIES_DST >> $CONFFILES_FILE
   echo $COORDINATOR_PROPERTIES_DST >> $CONFFILES_FILE
   echo $COORDINATOR_KEYSTORE_DST >> $CONFFILES_FILE
   find $BASE -type d -exec chmod 0755 {} \;
   find $BASE -type f -exec chmod 0644 {} \;
   chmod 0755 $POSTINST_FILE
   cd $WORKING
   fakeroot dpkg --build debian
   cp $DEB_OUTPUT $TARGET
   cd $OLD_PWD
   rm -rf $WORKING
}
package
