#!/usr/bin/env bash
export BATFISH_SOURCED_SCRIPT="$BASH_SOURCE"
export OLD_PWD="$PWD"

architecture() {
	local MACHINE=$(uname -m)
	if [ "$MACHINE" = "x86_64" ]; then
		local ARCHITECTURE=amd64
	elif [ "$MACHINE" = "i386" ]; then
		local ARCHITECTURE=i386
	else
		echo "Could not determine architecture from output of uname -m: $MACHINE"
		exit 1
	fi
	echo $ARCHITECTURE
}

ubuntu_version() {
	head -n1 /etc/issue | cut -f2 -d' ' | cut -f1,2 -d'.'
}

install_z3() {
   if [ "${UBUNTU_VERSION}" = "16.04" ]; then
      echo "Building and installing z3 in $USR_P"
      $BATFISH_TOOLS_PATH/install_z3_ubuntu_16.04.sh $USR_P
   elif [ "${UBUNTU_VERSION}" = "14.04" ]; then
      echo "Installing z3 in $USR_P"
      $BATFISH_TOOLS_PATH/install_z3_ubuntu_14.04.sh $USR_P
   else
      echo "Unsupported Ubuntu version: $UBUNTU_VERSION"
		exit 1
   fi
}

set_init_vars() {
   if [ "${UBUNTU_VERSION}" = "16.04" ]; then
      INIT_DIR=/lib/systemd/system
      BATFISH_INIT_NAME=batfish.service
      COORDINATOR_INIT_NAME=coordinator.service
   elif [ "${UBUNTU_VERSION}" = "14.04" ]; then
      INIT_DIR=/etc/init
      BATFISH_INIT_NAME=batfish.conf
      COORDINATOR_INIT_NAME=coordinator.conf
   else
      echo "Unsupported Ubuntu version: $UBUNTU_VERSION"
		exit 1
   fi
}

reload_init_scripts() {
   if [ "${UBUNTU_VERSION}" = "16.04" ]; then
		echo "systemctl enable $BATFISH_INIT_NAME || true"
		echo "systemctl enable $COORDINATOR_INIT_NAME || true"
		echo "systemctl daemon-reload"
   elif [ "${UBUNTU_VERSION}" = "14.04" ]; then
		echo "initctl reload-configuration"
   else
      echo "Unsupported Ubuntu version: $UBUNTU_VERSION"
		exit 1
   fi
}

write_init_scripts() {
   if [ "${UBUNTU_VERSION}" = "16.04" ]; then
      cat > $BATFISH_INIT_P <<EOF
# Batfish systemd service file

[Unit]
Description=Batfish Service
After=network.target auditd.service

[Install]
WantedBy=multi-user.target

[Service]
User=$BATFISH_USER
Group=$BATFISH_USER
ExecStart=/usr/bin/java -jar $BATFISH_JAR -logfile $BATFISH_LOG -servicemode -register true
PIDFile=$BATFISH_RUN_DIR/batfish.pid
Restart=always
EOF
      cat > $COORDINATOR_INIT_P <<EOF
# Coordinator systemd service file

[Unit]
Description=Coordinator Service
After=network.target auditd.service

[Install]
WantedBy=multi-user.target

[Service]
User=$BATFISH_USER
Group=$BATFISH_USER
ExecStart=/usr/bin/java -jar $COORDINATOR_JAR -logfile $COORDINATOR_LOG -servicehost localhost -containerslocation $BATFISH_HOME
WorkingDirectory=$BATFISH_HOME
PIDFile=$BATFISH_RUN_DIR/coordinator.pid
Restart=always
EOF
   elif [ "${UBUNTU_VERSION}" = "14.04" ]; then
      cat > $BATFISH_INIT_P <<EOF
author "Ari Fogel"
description "start and stop batfish service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

respawn

exec su -c "/usr/bin/java -DbatfishQuestionPluginDir=$PLUGIN_DIR -jar $BATFISH_JAR -logfile $BATFISH_LOG -servicemode -register true" $BATFISH_USER
EOF

   cat > $COORDINATOR_INIT_P <<EOF
author "Ari Fogel"
description "start and stop batfish coordinator service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

respawn

exec su -c "/usr/bin/java -jar $COORDINATOR_JAR -loglevel debug -logfile $COORDINATOR_LOG -servicehost localhost -containerslocation $BATFISH_HOME" $BATFISH_USER
EOF
   echo $BATFISH_INIT >> $CONFFILES_FILE
   echo $COORDINATOR_INIT >> $CONFFILES_FILE
   else
      echo "Unsupported Ubuntu version: $UBUNTU_VERSION"
		exit 1
   fi

}

package() {
   BATFISH_TOOLS_PATH="$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT))"
   SCRIPT_NAME="$(basename $BATFISH_SOURCED_SCRIPT)"
   BATFISH_PATH="$(readlink -f ${BATFISH_TOOLS_PATH}/..)"
   VERSION_FILE=$BATFISH_PATH/projects/batfish-common-protocol/src/org/batfish/common/Version.java
   BATFISH_VERSION=$(grep 'private static final String VERSION' $VERSION_FILE | sed -e 's/^[^"]*"\([^"]*\)".*$/\1/g')
	ARCHITECTURE=$(architecture)
	UBUNTU_VERSION=$(ubuntu_version)
	VERSION="${BATFISH_VERSION}-ubuntu${UBUNTU_VERSION}"
   TARGET="${OLD_PWD}/batfish_${VERSION}_${ARCHITECTURE}.deb"
   WORKING=$(mktemp -d -t ${SCRIPT_NAME}.XXXXXXX)
   PACKAGE_NAME="batfish-${VERSION}"
   DPKG_DIR=$WORKING/$PACKAGE_NAME
   PBASE=$DPKG_DIR/debian
   USR=/usr
   USR_P=${PBASE}${USR}
   Z3=$USR/bin/z3
   Z3_P=${PBASE}${Z3}
   DATA_DIR=/usr/share/batfish
   DATA_DIR_P=${PBASE}${DATA_DIR}
   CONF_DIR=/etc/batfish
   CONF_DIR_P=${PBASE}${CONF_DIR}
   DOC_DIR=/usr/share/doc/batfish
   DOC_DIR_P=${PBASE}${DOC_DIR}
   set_init_vars
   INIT_DIR_P=${PBASE}${INIT_DIR}
   BATFISH_INIT=${INIT_DIR}/${BATFISH_INIT_NAME}
   BATFISH_INIT_P=${PBASE}${BATFISH_INIT}
   BATFISH_JAR_NAME=batfish.jar
   BATFISH_JAR_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_JAR_NAME
   BATFISH_JAR=${DATA_DIR}/$BATFISH_JAR_NAME
   BATFISH_JAR_P=${PBASE}${BATFISH_JAR}
   BATFISH_PROPERTIES_NAME=batfish.properties
   BATFISH_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_PROPERTIES_NAME
   BATFISH_PROPERTIES_LINK=${DATA_DIR}/$BATFISH_PROPERTIES_NAME
   BATFISH_PROPERTIES_LINK_P=${PBASE}${BATFISH_PROPERTIES_LINK}
   BATFISH_PROPERTIES=${CONF_DIR}/$BATFISH_PROPERTIES_NAME
   BATFISH_PROPERTIES_P=${PBASE}${BATFISH_PROPERTIES}
   CLIENT_JAR_NAME=batfish-client.jar
   CLIENT_JAR_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_JAR_NAME
   CLIENT_JAR=${DATA_DIR}/$CLIENT_JAR_NAME
   CLIENT_JAR_P=${PBASE}${CLIENT_JAR}
   CLIENT_PROPERTIES_NAME=client.properties
   CLIENT_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_PROPERTIES_NAME
   CLIENT_PROPERTIES_LINK=${DATA_DIR}/$CLIENT_PROPERTIES_NAME
   CLIENT_PROPERTIES_LINK_P=${PBASE}${CLIENT_PROPERTIES_LINK}
   CLIENT_PROPERTIES=${CONF_DIR}/$CLIENT_PROPERTIES_NAME
   CLIENT_PROPERTIES_P=${PBASE}${CLIENT_PROPERTIES}
   COORDINATOR_INIT=${INIT_DIR}/${COORDINATOR_INIT_NAME}
   COORDINATOR_INIT_P=${PBASE}${COORDINATOR_INIT}
   COORDINATOR_JAR_NAME=coordinator.jar
   COORDINATOR_JAR_SRC=$BATFISH_PATH/projects/coordinator/out/${COORDINATOR_JAR_NAME}
   COORDINATOR_JAR=${DATA_DIR}/$COORDINATOR_JAR_NAME
   COORDINATOR_JAR_P=${PBASE}$COORDINATOR_JAR
   COORDINATOR_PROPERTIES_NAME=coordinator.properties
   COORDINATOR_PROPERTIES_SRC=${BATFISH_PATH}/projects/coordinator/out/${COORDINATOR_PROPERTIES_NAME}
   COORDINATOR_PROPERTIES_LINK=${DATA_DIR}/${COORDINATOR_PROPERTIES_NAME}
   COORDINATOR_PROPERTIES_LINK_P=${PBASE}${COORDINATOR_PROPERTIES_LINK}
   COORDINATOR_PROPERTIES=${CONF_DIR}/${COORDINATOR_PROPERTIES_NAME}
   COORDINATOR_PROPERTIES_P=${PBASE}${COORDINATOR_PROPERTIES}
   COORDINATOR_KEYSTORE_NAME=selfsigned.jks
   COORDINATOR_KEYSTORE_SRC=${BATFISH_PATH}/projects/coordinator/out/${COORDINATOR_KEYSTORE_NAME}
   COORDINATOR_KEYSTORE_LINK=${DATA_DIR}/${COORDINATOR_KEYSTORE_NAME}
   COORDINATOR_KEYSTORE_LINK_P=${PBASE}${COORDINATOR_KEYSTORE_LINK}
   COORDINATOR_KEYSTORE=${CONF_DIR}/${COORDINATOR_KEYSTORE_NAME}
   COORDINATOR_KEYSTORE_P=${PBASE}${COORDINATOR_KEYSTORE}
   PLUGIN_DIR=${DATA_DIR}/plugins
   PLUGIN_DIR_P=${PBASE}${PLUGIN_DIR}
   QUESTION_JAR_NAME=question.jar
   QUESTION_JAR_SRC=${BATFISH_PATH}/projects/question/out/${QUESTION_JAR_NAME}
   QUESTION_JAR=${PLUGIN_DIR}/${QUESTION_JAR_NAME}
   QUESTION_JAR_P=${PBASE}${QUESTION_JAR}
   COPYRIGHT_NAME=copyright
   COPYRIGHT=${DOC_DIR}/${COPYRIGHT_NAME}
   COPYRIGHT_P=${PBASE}${COPYRIGHT}
   BATFISH_HOME=/var/batfish
   BATFISH_LOG_DIR=/var/log/batfish
   BATFISH_RUN_DIR=/var/run/batfish
   BATFISH_LOG_NAME=batfish.log
   BATFISH_LOG=$BATFISH_LOG_DIR/$BATFISH_LOG_NAME
   COORDINATOR_LOG_NAME=coordinator.log
   COORDINATOR_LOG=$BATFISH_LOG_DIR/$COORDINATOR_LOG_NAME
   BATFISH_USER=batfish
   CHANGELOG_NAME=changelog.Debian.gz
   CHANGELOG=${DOC_DIR}/${CHANGELOG_NAME}
   CHANGELOG_P=${PBASE}${CHANGELOG}
   DEBIAN_DIR=${PBASE}/DEBIAN
   CONTROL_FILE=$DEBIAN_DIR/control
   POSTINST_FILE=$DEBIAN_DIR/postinst
   PRERM_FILE=$DEBIAN_DIR/prerm
   CONFFILES_FILE=$DEBIAN_DIR/conffiles
   BATFISH_USER=batfish
   DEB_OUTPUT=debian.deb
   set -x
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
   if [ ! -f "$QUESTION_JAR_SRC" ]; then
      echo "Missing $QUESTION_JAR_SRC" >&2
      return 1
   fi
   mkdir -p $DEBIAN_DIR
   mkdir -p $CONF_DIR_P
   mkdir -p $DATA_DIR_P
   mkdir -p $DOC_DIR_P
   mkdir -p $INIT_DIR_P
   mkdir -p $PLUGIN_DIR_P
   cp $BATFISH_JAR_SRC $BATFISH_JAR_P
   cp $BATFISH_PROPERTIES_SRC $BATFISH_PROPERTIES_P
   ln -s $BATFISH_PROPERTIES $BATFISH_PROPERTIES_LINK_P
   cp $CLIENT_JAR_SRC $CLIENT_JAR_P
   cp $CLIENT_PROPERTIES_SRC $CLIENT_PROPERTIES_P
   ln -s $CLIENT_PROPERTIES $CLIENT_PROPERTIES_LINK_P
   cp $COORDINATOR_JAR_SRC $COORDINATOR_JAR_P
   cp $COORDINATOR_PROPERTIES_SRC $COORDINATOR_PROPERTIES_P
   ln -s $COORDINATOR_PROPERTIES $COORDINATOR_PROPERTIES_LINK_P
   cp $COORDINATOR_KEYSTORE_SRC $COORDINATOR_KEYSTORE_P
   ln -s $COORDINATOR_KEYSTORE $COORDINATOR_KEYSTORE_LINK_P
   cp $QUESTION_JAR_SRC $QUESTION_JAR_P

   write_init_scripts

   cat > $CONTROL_FILE <<EOF
Package: batfish
Version: $VERSION
Section: web
Priority: optional
Architecture: $ARCHITECTURE
Maintainer: Ari Fogel <ari@intentionet.com>
Description: network configuration analysis tool
 Batfish is a network configuration analysis tool developed 
 jointly by researchers at University of California, Los Angeles;
 University of Southern California; and Microsoft Research. 
 Though its individual modules have various applications,
 its primary purpose is to detect bugs in network configurations.

EOF

   gzip --best > $CHANGELOG_P <<EOF
batfish (0.2.0-ubuntu${UBUNTU_VERSION}) trusty; urgency=low

  * Second Release: second release

 -- Ari Fogel <ari@intentionet.com>  Mon, 09 Jan 2017 15:36:00 -0700

batfish (0.1.0-ubuntu${UBUNTU_VERSION}) trusty; urgency=low

  * Initial Release: initial release

 -- Ari Fogel <ari@intentionet.com>  Thu, 07 Apr 2016 17:49:41 -0700

EOF

   cat > $COPYRIGHT_P <<EOF
Name: batfish
Maintainer: Ari Fogel <ari@intentionet.com>
Source: https://github.com/arifogel/batfish
Copyright: 2013-2017 Ari Fogel <ari@intentionet.com>
License: Apache-2.0
 Internal components may have different licenses. Their license
 files may be extracted from the included jar files.
 On Debian systems, the full text of the Apache 2.0
 License can be found in the file
 '/usr/share/common-licenses/Apache-2.0'.
EOF

   cat > $POSTINST_FILE <<EOF
#!/bin/bash

set -e

getent group $BATFISH_USER > /dev/null || groupadd -r $BATFISH_USER
getent passwd $BATFISH_USER > /dev/null || useradd -r -d $BATFISH_HOME -s /bin/bash -g $BATFISH_USER $BATFISH_USER
mkdir -p $BATFISH_HOME
chown $BATFISH_USER:$BATFISH_USER $BATFISH_HOME
chmod 0750 $BATFISH_HOME

$(reload_init_scripts)

chown root:$BATFISH_USER $CONF_DIR
chmod 0770 $CONF_DIR
chown root:$BATFISH_USER $BATFISH_PROPERTIES
chmod 0660 $BATFISH_PROPERTIES
chown root:$BATFISH_USER $CLIENT_PROPERTIES
chmod 0660 $CLIENT_PROPERTIES
chown root:$BATFISH_USER $COORDINATOR_PROPERTIES
chmod 0660 $COORDINATOR_PROPERTIES
chown root:batfish $COORDINATOR_KEYSTORE
chmod 0660 $COORDINATOR_KEYSTORE
mkdir -p $BATFISH_LOG_DIR
chown batfish:batfish $BATFISH_LOG_DIR
chmod 0770 $BATFISH_LOG_DIR
mkdir -p $BATFISH_RUN_DIR
chown batfish:batfish $BATFISH_RUN_DIR
chmod 0755 $BATFISH_RUN_DIR
chmod 0644 $BATFISH_INIT
chmod 0644 $COORDINATOR_INIT
service coordinator restart
service batfish restart
exit 0
EOF

   cat > $PRERM_FILE <<EOF
#!/bin/bash

set -e

service coordinator stop
service batfish stop
exit 0
EOF

   echo $BATFISH_PROPERTIES >> $CONFFILES_FILE
   echo $CLIENT_PROPERTIES >> $CONFFILES_FILE
   echo $COORDINATOR_PROPERTIES >> $CONFFILES_FILE
   echo $COORDINATOR_KEYSTORE >> $CONFFILES_FILE
   install_z3
   find $PBASE -type d -exec chmod 0755 {} \;
   find $PBASE -type f -exec chmod 0644 {} \;
   chmod 0755 $POSTINST_FILE $PRERM_FILE $Z3_P
   cd $DPKG_DIR || return 1
   fakeroot dpkg --build debian || return 1
   cp $DEB_OUTPUT $TARGET || return 1
   cd $OLD_PWD
   rm -rf $WORKING
}
package

