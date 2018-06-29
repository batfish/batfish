#!/usr/bin/env bash
export OLD_PWD="$PWD"

set -e

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
   echo ${ARCHITECTURE}
}

install_z3() {
   if [ "${UBUNTU_VERSION}" = "14.04" -o \
        "${UBUNTU_VERSION}" = "16.04" ]; then
      echo "Building and installing z3 in ${USR_P}"
      "${BATFISH_TOOLS_PATH}/install_z3_ubuntu.sh" "${USR_P}"
   else
      echo "Unsupported Ubuntu version: ${UBUNTU_VERSION}"
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
   set -e
   if [ "${UBUNTU_VERSION}" = "16.04" ]; then
      cat > ${BATFISH_INIT_P} <<EOF
# Batfish systemd service file

[Unit]
Description=Batfish Service
After=network.target auditd.service

[Install]
WantedBy=multi-user.target

[Service]
EnvironmentFile=${BATFISH_ENVFILE}
User=${BATFISH_USER}
Group=${BATFISH_USER}
ExecStart=/bin/bash -c '/usr/bin/java -DbatfishBatfishPropertiesPath=${BATFISH_PROPERTIES} \${BATFISH_JAVA_ARGS} -cp ${ALLINONE_JAR} ${BATFISH_MAIN_CLASS} -runmode workservice -register true &>> ${BATFISH_JAVA_LOG}'
PIDFile=${BATFISH_RUN_DIR}/batfish.pid
Restart=always
EOF
      cat > ${COORDINATOR_INIT_P} <<EOF
# Coordinator systemd service file

[Unit]
Description=Coordinator Service
After=network.target auditd.service

[Install]
WantedBy=multi-user.target

[Service]
EnvironmentFile=${COORDINATOR_ENVFILE}
User=${BATFISH_USER}
Group=${BATFISH_USER}
ExecStart=/bin/bash -c '/usr/bin/java -DbatfishCoordinatorPropertiesPath=${COORDINATOR_PROPERTIES} \${COORDINATOR_JAVA_ARGS} -cp \$(cat ${COORDINATOR_CLASSPATH}):${ALLINONE_JAR} ${COORDINATOR_MAIN_CLASS} -logfile ${COORDINATOR_LOG} -containerslocation ${BATFISH_HOME} &>> ${COORDINATOR_JAVA_LOG}'
WorkingDirectory=${BATFISH_HOME}
PIDFile=${BATFISH_RUN_DIR}/coordinator.pid
Restart=always
EOF
   elif [ "${UBUNTU_VERSION}" = "14.04" ]; then
      cat > ${BATFISH_INIT_P} <<EOF
author "Ari Fogel"
description "start and stop batfish service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

respawn

exec su -c "/bin/bash -c . ${BATFISH_ENVFILE} && '/usr/bin/java -DbatfishBatfishPropertiesPath=${BATFISH_PROPERTIES} \${BATFISH_JAVA_ARGS} -cp ${ALLINONE_JAR} ${BATFISH_MAIN_CLASS} -runmode workservice -register true &>> ${BATFISH_JAVA_LOG}'" ${BATFISH_USER}
EOF

   cat > ${COORDINATOR_INIT_P} <<EOF
author "Ari Fogel"
description "start and stop batfish coordinator service for Ubuntu (upstart)"
version "1.0"

start on started networking
stop on runlevel [!2345]

respawn

exec su -c "/bin/bash -c '. ${COORDINATOR_ENVFILE} && /usr/bin/java -DbatfishCoordinatorPropertiesPath=${COORDINATOR_PROPERTIES} \${COORDINATOR_JAVA_ARGS} -cp \$(cat ${COORDINATOR_CLASSPATH}):${ALLINONE_JAR} ${COORDINATOR_MAIN_CLASS} -logfile ${COORDINATOR_LOG} -containerslocation ${BATFISH_HOME} &>> ${COORDINATOR_JAVA_LOG}'" ${BATFISH_USER}
EOF
   echo ${BATFISH_INIT} >> ${CONFFILES_FILE}
   echo ${COORDINATOR_INIT} >> ${CONFFILES_FILE}
   else
      echo "Unsupported Ubuntu version: $UBUNTU_VERSION"
      exit 1
   fi

}

package() {
   set -e
   BATFISH_MAIN_CLASS=org.batfish.main.Driver
   COORDINATOR_MAIN_CLASS=org.batfish.coordinator.Main
   BATFISH_TOOLS_PATH="$(cd $(dirname "${BASH_SOURCE}") && pwd)"
   SCRIPT_NAME="$(basename "${BASH_SOURCE}")"
   BATFISH_PATH="$(readlink -f ${BATFISH_TOOLS_PATH}/..)"
   SECONDARY_VERSION=$(echo ${BATFISH_VERSION} | cut -d'.' -f1,2)
   ARCHITECTURE=$(architecture)
   UBUNTU_VERSION="$(lsb_release -rs)"
   VERSION="${BATFISH_VERSION}~ubuntu${UBUNTU_VERSION}"
   PACKAGE_NAME="batfish-${SECONDARY_VERSION}"
   DEB_NAME=${PACKAGE_NAME}_${VERSION}_${ARCHITECTURE}
   TARGET="${OLD_PWD}/${DEB_NAME}.deb"
   WORKING=$(mktemp -d -t ${SCRIPT_NAME}.XXXXXXX)
   DPKG_DIR=${WORKING}/${DEB_NAME}
   PBASE=${DPKG_DIR}/debian
   USR=/usr
   USR_P=${PBASE}${USR}
   Z3=${USR}/bin/z3
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
   ALLINONE_JAR_SRC_NAME=allinone-bundle-${BATFISH_VERSION}.jar
   ALLINONE_JAR_SRC=${BATFISH_PATH}/projects/allinone/target/${ALLINONE_JAR_SRC_NAME}
   ALLINONE_JAR_NAME=allinone.jar
   ALLINONE_JAR=${DATA_DIR}/${ALLINONE_JAR_NAME}
   ALLINONE_JAR_P=${PBASE}${ALLINONE_JAR}
   ALLINONE_PROPERTIES_NAME=allinone.properties
   ALLINONE_PROPERTIES_SRC=${BATFISH_PATH}/projects/allinone/target/classes/org/batfish/allinone/config/${ALLINONE_PROPERTIES_NAME}
   ALLINONE_PROPERTIES=${CONF_DIR}/${ALLINONE_PROPERTIES_NAME}
   ALLINONE_PROPERTIES_P=${PBASE}${ALLINONE_PROPERTIES}
   BATFISH_ENVFILE_NAME=batfish_env
   BATFISH_ENVFILE=${CONF_DIR}/${BATFISH_ENVFILE_NAME}
   BATFISH_ENVFILE_P=${PBASE}${BATFISH_ENVFILE}
   BATFISH_PROPERTIES_NAME=batfish.properties
   BATFISH_PROPERTIES_SRC=${BATFISH_PATH}/projects/batfish/target/classes/org/batfish/config/${BATFISH_PROPERTIES_NAME}
   BATFISH_PROPERTIES=${CONF_DIR}/${BATFISH_PROPERTIES_NAME}
   BATFISH_PROPERTIES_P=${PBASE}${BATFISH_PROPERTIES}
   CLIENT_PROPERTIES_NAME=client.properties
   CLIENT_PROPERTIES_SRC=${BATFISH_PATH}/projects/batfish-client/target/classes/org/batfish/client/config/${CLIENT_PROPERTIES_NAME}
   CLIENT_PROPERTIES=${CONF_DIR}/${CLIENT_PROPERTIES_NAME}
   CLIENT_PROPERTIES_P=${PBASE}${CLIENT_PROPERTIES}
   COORDINATOR_ENVFILE_NAME=coordinator_env
   COORDINATOR_ENVFILE=${CONF_DIR}/${COORDINATOR_ENVFILE_NAME}
   COORDINATOR_ENVFILE_P=${PBASE}${COORDINATOR_ENVFILE}
   COORDINATOR_INIT=${INIT_DIR}/${COORDINATOR_INIT_NAME}
   COORDINATOR_INIT_P=${PBASE}${COORDINATOR_INIT}
   COORDINATOR_CLASSPATH_NAME=coordinator.classpath
   COORDINATOR_CLASSPATH=${CONF_DIR}/${COORDINATOR_CLASSPATH_NAME}
   COORDINATOR_CLASSPATH_P=${PBASE}${COORDINATOR_CLASSPATH}
   COORDINATOR_PROPERTIES_NAME=coordinator.properties
   COORDINATOR_PROPERTIES_SRC=${BATFISH_PATH}/projects/coordinator/target/classes/org/batfish/coordinator/config/${COORDINATOR_PROPERTIES_NAME}
   COORDINATOR_PROPERTIES=${CONF_DIR}/${COORDINATOR_PROPERTIES_NAME}
   COORDINATOR_PROPERTIES_P=${PBASE}${COORDINATOR_PROPERTIES}
   COPYRIGHT_NAME=copyright
   COPYRIGHT=${DOC_DIR}/${COPYRIGHT_NAME}
   COPYRIGHT_P=${PBASE}${COPYRIGHT}
   BATFISH_HOME=/var/batfish
   BATFISH_LOG_DIR=/var/log/batfish
   BATFISH_RUN_DIR=/var/run/batfish
   BATFISH_JAVA_LOG_NAME=batfish-java.log
   BATFISH_JAVA_LOG=${BATFISH_LOG_DIR}/${BATFISH_JAVA_LOG_NAME}
   COORDINATOR_LOG_NAME=coordinator.log
   COORDINATOR_LOG=${BATFISH_LOG_DIR}/${COORDINATOR_LOG_NAME}
   COORDINATOR_JAVA_LOG_NAME=coordinator-java.log
   COORDINATOR_JAVA_LOG=${BATFISH_LOG_DIR}/${COORDINATOR_JAVA_LOG_NAME}
   BATFISH_USER=batfish
   CHANGELOG_NAME=changelog.Debian.gz
   CHANGELOG=${DOC_DIR}/${CHANGELOG_NAME}
   CHANGELOG_P=${PBASE}${CHANGELOG}
   DEBIAN_DIR=${PBASE}/DEBIAN
   CONTROL_FILE=${DEBIAN_DIR}/control
   POSTINST_FILE=${DEBIAN_DIR}/postinst
   PRERM_FILE=${DEBIAN_DIR}/prerm
   CONFFILES_FILE=${DEBIAN_DIR}/conffiles
   BATFISH_USER=batfish
   DEB_OUTPUT=debian.deb
   set -x
   if [ ! -f "$ALLINONE_JAR_SRC" ]; then
      echo "Missing $ALLINONE_JAR_SRC" >&2
      return 1
   fi
   mkdir -p ${DEBIAN_DIR}
   mkdir -p ${CONF_DIR_P}
   mkdir -p ${DATA_DIR_P}
   mkdir -p ${DOC_DIR_P}
   mkdir -p ${INIT_DIR_P}
   cp ${ALLINONE_JAR_SRC} ${ALLINONE_JAR_P}
   cp ${ALLINONE_PROPERTIES_SRC} ${ALLINONE_PROPERTIES_P}
   cp ${BATFISH_PROPERTIES_SRC} ${BATFISH_PROPERTIES_P}
   cp ${CLIENT_PROPERTIES_SRC} ${CLIENT_PROPERTIES_P}
   cp ${COORDINATOR_PROPERTIES_SRC} ${COORDINATOR_PROPERTIES_P}

   write_init_scripts

  cat > ${BATFISH_ENVFILE_P} <<EOF
#!/usr/bin/env bash
BATFISH_JAVA_ARGS=
EOF

  cat > ${COORDINATOR_ENVFILE_P} <<EOF
#!/usr/bin/env bash
COORDINATOR_JAVA_ARGS=
EOF

   cat > ${CONTROL_FILE} <<EOF
Package: ${PACKAGE_NAME}
Conflicts: batfish
Replaces: batfish
Provides: batfish
Breaks: z3
Version: ${VERSION}
Section: web
Priority: optional
Architecture: ${ARCHITECTURE}
Maintainer: Ari Fogel <ari@intentionet.com>
Description: network configuration analysis tool
 Batfish is a network configuration analysis tool developed
 jointly by researchers at University of California, Los Angeles;
 University of Southern California; and Microsoft Research.
 Though its individual modules have various applications,
 its primary purpose is to detect bugs in network configurations.

EOF

   gzip --best > ${CHANGELOG_P} <<EOF
batfish (0.2.0-ubuntu${UBUNTU_VERSION}) trusty; urgency=low

  * Second Release: second release

 -- Ari Fogel <ari@intentionet.com>  Mon, 09 Jan 2017 15:36:00 -0700

batfish (0.1.0-ubuntu${UBUNTU_VERSION}) trusty; urgency=low

  * Initial Release: initial release

 -- Ari Fogel <ari@intentionet.com>  Thu, 07 Apr 2016 17:49:41 -0700

EOF

   cat > ${COPYRIGHT_P} <<EOF
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

   cat > ${POSTINST_FILE} <<EOF
#!/bin/bash

set -e

getent group ${BATFISH_USER} > /dev/null || groupadd -r ${BATFISH_USER}
getent passwd ${BATFISH_USER} > /dev/null || useradd -r -d ${BATFISH_HOME} -s /bin/bash -g ${BATFISH_USER} ${BATFISH_USER}
mkdir -p ${BATFISH_HOME}
chown ${BATFISH_USER}:${BATFISH_USER} ${BATFISH_HOME}
chmod 0750 ${BATFISH_HOME}

$(reload_init_scripts)

chown root:${BATFISH_USER} ${CONF_DIR}
chmod 0770 ${CONF_DIR}
chown root:${BATFISH_USER} ${ALLINONE_PROPERTIES}
chmod 0660 ${ALLINONE_PROPERTIES}
chown root:${BATFISH_USER} ${BATFISH_ENVFILE}
chmod 0660 ${BATFISH_ENVFILE}
chown root:${BATFISH_USER} ${BATFISH_PROPERTIES}
chmod 0660 ${BATFISH_PROPERTIES}
chown root:${BATFISH_USER} ${CLIENT_PROPERTIES}
chmod 0660 ${CLIENT_PROPERTIES}
chown root:${BATFISH_USER} ${COORDINATOR_CLASSPATH}
chmod 0660 ${COORDINATOR_CLASSPATH}
chown root:${BATFISH_USER} ${COORDINATOR_ENVFILE}
chmod 0660 ${COORDINATOR_ENVFILE}
chown root:${BATFISH_USER} ${COORDINATOR_PROPERTIES}
chmod 0660 ${COORDINATOR_PROPERTIES}
mkdir -p ${BATFISH_LOG_DIR}
chown batfish:batfish ${BATFISH_LOG_DIR}
chmod 0770 ${BATFISH_LOG_DIR}
mkdir -p ${BATFISH_RUN_DIR}
chown batfish:batfish ${BATFISH_RUN_DIR}
chmod 0755 ${BATFISH_RUN_DIR}
chmod 0644 ${BATFISH_INIT}
chmod 0644 ${COORDINATOR_INIT}
service coordinator restart
service batfish restart
exit 0
EOF

   cat > ${PRERM_FILE} <<EOF
#!/bin/bash

set -e

service coordinator stop
service batfish stop
exit 0
EOF

   touch ${COORDINATOR_CLASSPATH_P}
   echo ${ALLINONE_PROPERTIES} >> ${CONFFILES_FILE}
   echo ${BATFISH_ENVFILE} >> ${CONFFILES_FILE}
   echo ${BATFISH_PROPERTIES} >> ${CONFFILES_FILE}
   echo ${CLIENT_PROPERTIES} >> ${CONFFILES_FILE}
   echo ${COORDINATOR_ENVFILE} >> ${CONFFILES_FILE}
   echo ${COORDINATOR_PROPERTIES} >> ${CONFFILES_FILE}
   install_z3
   find ${PBASE} -type d -exec chmod 0755 {} \;
   find ${PBASE} -type f -exec chmod 0644 {} \;
   chmod 0755 ${POSTINST_FILE} ${PRERM_FILE} ${Z3_P}
   cd ${DPKG_DIR} || return 1
   fakeroot dpkg --build debian || return 1
   cp ${DEB_OUTPUT} ${TARGET} || return 1
   cd ${OLD_PWD}
   rm -rf ${WORKING}
}
package
