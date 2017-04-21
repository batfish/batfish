#!/usr/bin/env bash
#TODO: support i386

export BATFISH_SOURCED_SCRIPT="$BASH_SOURCE"
export OLD_PWD="$PWD"

architecture() {
   local MACHINE=$(uname -m)
   if [ "$MACHINE" = "x86_64" ]; then
      local ARCHITECTURE=x86_64
   elif [ "$MACHINE" = "i386" ]; then
      local ARCHITECTURE=i386
   else
      echo "Could not determine architecture from output of uname -m: $MACHINE"
      exit 1
   fi
   echo $ARCHITECTURE
}

jdk_requirement() {
   if [ "${REDHAT_VERSION}" = "7" ]; then
      echo "Requires: java-1.8.0-openjdk"
   elif [ "${REDHAT_VERSION}" = "6" ]; then
      # TODO: find good way to install java 8
      echo
   else
      echo "Unsupported RHEL version: $REDHAT_VERSION"
      exit 1
   fi
}

redhat_version() {
   sed 's/.*release \([^ ]*\).*/\1/g' /etc/redhat-release | cut -d'.' -f1
}

set_init_vars() {
   if [ "${REDHAT_VERSION}" = "7" ]; then
      INIT_DIR=/lib/systemd/system
      BATFISH_INIT_NAME=batfish.service
      COORDINATOR_INIT_NAME=coordinator.service
   elif [ "${REDHAT_VERSION}" = "6" ]; then
      INIT_DIR=/etc/init.d
      BATFISH_INIT_NAME=batfish
      COORDINATOR_INIT_NAME=coordinator
   else
      echo "Unsupported RHEL version: $REDHAT_VERSION"
      exit 1
   fi
}

set_service_path() {
   if [ "${REDHAT_VERSION}" = "7" ]; then
      SERVICE=/usr/sbin/service
      CTL=/usr/bin/systemctl
   elif [ "${REDHAT_VERSION}" = "6" ]; then
      SERVICE=/sbin/service
      CTL=/sbin/initctl
   else
      echo "Unsupported RHEL version: $REDHAT_VERSION"
      exit 1
   fi
}

reload_init_scripts() {
   if [ "${REDHAT_VERSION}" = "7" ]; then
      echo "/usr/bin/systemctl enable $BATFISH_INIT_NAME || true"
      echo "/usr/bin/systemctl enable $COORDINATOR_INIT_NAME || true"
      echo "/usr/bin/systemctl daemon-reload"
   elif [ "${REDHAT_VERSION}" = "6" ]; then
      echo "/sbin/initctl reload-configuration"
   else
      echo "Unsupported RHEL version: $REDHAT_VERSION"
      exit 1
   fi
}

write_init_scripts() {
   if [ "${REDHAT_VERSION}" = "7" ]; then
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
ExecStart=/bin/bash -c '/usr/bin/java -DbatfishQuestionPluginDir=$PLUGIN_DIR -jar $BATFISH_JAR -logfile $BATFISH_LOG -servicemode -register true &>> $BATFISH_JAVA_LOG'
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
ExecStart=/bin/bash -c '/usr/bin/java -Done-jar.class.path=\$(cat $COORDINATOR_CLASSPATH) -jar $COORDINATOR_JAR -logfile $COORDINATOR_LOG -containerslocation $BATFISH_HOME &>> $COORDINATOR_JAVA_LOG'
WorkingDirectory=$BATFISH_HOME
PIDFile=$BATFISH_RUN_DIR/coordinator.pid
Restart=always
EOF
   elif [ "${REDHAT_VERSION}" = "6" ]; then
      cat > $BATFISH_INIT_P <<EOF
#!/bin/bash
#
# batfish Batfish worker
#
# chkconfig: 2345 20 80
# description: Batfish worker is the worker process component of the Batfish network configuration analysis tool

# Source function library.
. /etc/init.d/functions

RETVAL=0

start() {
   echo -n "Starting batfish: "
   if [ -f $BATFISH_PID_FILE ]; then
      echo "Already running with pid: \$(cat $BATFISH_PID_FILE)"
      return 1
   fi
   if [ -f $BATFISH_LOCK ]; then
      echo "Missing pid file, but lock file present: $BATFISH_LOCK"
      return 1
   fi
   su -c "bash -c '/usr/bin/java -DbatfishQuestionPluginDir=$PLUGIN_DIR -jar $BATFISH_JAR -logfile $BATFISH_LOG -servicemode -register true &>> $BATFISH_JAVA_LOG & echo \\\$! > $BATFISH_PID_FILE'" batfish
   touch $BATFISH_LOCK
   echo "Sucess"
}

stop() {
   echo -n "Shutting down batfish: "
   RETVAL=0
   if [ -f $BATFISH_PID_FILE ]; then
      PID=\$(cat $BATFISH_PID_FILE)
      kill -9 \$PID
      RETVAL=\$?
      rm -f $BATFISH_PID_FILE
   fi
   [ \$RETVAL -eq 0 ] && rm -f $BATFISH_LOCK && echo "Success"
   return \$RETVAL
}

case "\$1" in
    start)
      start
   ;;
    stop)
      stop
   ;;
    status)
      status
   ;;
    restart)
      stop
      start
   ;;
    *)
      echo "Usage: batfish {start|stop|status|restart]"
      exit 1
   ;;
esac
exit \$?
EOF

   cat > $COORDINATOR_INIT_P <<EOF
#!/bin/bash
#
# coordinator Batfish coordinator
#
# chkconfig: 2345 20 80
# description: Batfish coordinator is the manangement component of the Batfish network configuration analysis tool

# Source function library.
. /etc/init.d/functions

RETVAL=0

start() {
   echo -n "Starting coordinator: "
   if [ -f $COORDINATOR_PID_FILE ]; then
      echo "Already running with pid: \$(cat $COORDINATOR_PID_FILE)" >&2
      return 1
   fi
   if [ -f $COORDINATOR_LOCK ]; then
      echo "Missing pid file, but lock file present: $COORDINATOR_LOCK" >&2
      return 1
   fi
   su -c "bash -c '/usr/bin/java -Done-jar.class.path=\$(cat $COORDINATOR_CLASSPATH) -jar $COORDINATOR_JAR -logfile $COORDINATOR_LOG -containerslocation $BATFISH_HOME &>> $COORDINATOR_JAVA_LOG & echo \\\$! > $COORDINATOR_PID_FILE'" batfish
   touch $COORDINATOR_LOCK
   echo "Success"
}

stop() {
   echo -n "Shutting down coordinator: "
   RETVAL=0
   if [ -f $COORDINATOR_PID_FILE ]; then
      PID=\$(cat $COORDINATOR_PID_FILE)
      kill -9 \$PID
      RETVAL=\$?
      rm -f $COORDINATOR_PID_FILE
   fi
   [ \$RETVAL -eq 0 ] && rm -f $COORDINATOR_LOCK && echo "Success"
   return \$RETVAL
}

case "\$1" in
    start)
      start
   ;;
    stop)
      stop
   ;;
    status)
      status
   ;;
    restart)
      stop
      start
   ;;
    *)
      echo "Usage: coordinator {start|stop|status|restart]"
      exit 1
   ;;
esac
exit \$?
EOF
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
   SECONDARY_VERSION=$(echo $BATFISH_VERSION | cut -d'.' -f1,2)
   ARCHITECTURE=$(architecture)
   REDHAT_VERSION=$(redhat_version)
   RELEASE=1
   PACKAGE_NAME="batfish-${SECONDARY_VERSION}"
   TARBALL_NAME="${PACKAGE_NAME}-${BATFISH_VERSION}.tar.gz"
   FINAL_RPM_NAME=${PACKAGE_NAME}-${BATFISH_VERSION}-${RELEASE}.el${REDHAT_VERSION}.${ARCHITECTURE}.rpm
   BATFISH_Z3_RHEL_INSTALLER=${BATFISH_TOOLS_PATH}/install_z3_rhel_x86_64.sh
   WORKING=$(mktemp -d)
   RBASE=${WORKING}/rpmbuild
   PBASE=${RBASE}/${PACKAGE_NAME}-${BATFISH_VERSION}
   USR=/usr
   USR_P=${PBASE}${USR}
   DATA_DIR=/usr/share/batfish
   DATA_DIR_P=${PBASE}${DATA_DIR}
   CONF_DIR=/etc/batfish
   CONF_DIR_P=${PBASE}${CONF_DIR}
   DOC_DIR=/usr/share/doc/batfish
   DOC_DIR_P=${PBASE}${DOC_DIR}
   set_init_vars
   set_service_path
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
   COORDINATOR_CLASSPATH_NAME=coordinator.classpath
   COORDINATOR_CLASSPATH=${CONF_DIR}/${COORDINATOR_CLASSPATH_NAME}
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
   LOCK_DIR=/var/lock/subsys
   BATFISH_RUN_DIR=/var/run/batfish
   BATFISH_LOG_NAME=batfish.log
   BATFISH_LOG=${BATFISH_LOG_DIR}/${BATFISH_LOG_NAME}
   BATFISH_JAVA_LOG_NAME=batfish-java.log
   BATFISH_JAVA_LOG=${BATFISH_LOG_DIR}/${BATFISH_JAVA_LOG_NAME}
   BATFISH_LOCK_NAME=batfish.lock
   BATFISH_LOCK=${LOCK_DIR}/${BATFISH_LOCK_NAME}
   BATFISH_PID_FILE_NAME=batfish.pid
   BATFISH_PID_FILE=${BATFISH_RUN_DIR}/${BATFISH_PID_FILE_NAME}
   COORDINATOR_LOG_NAME=coordinator.log
   COORDINATOR_LOG=${BATFISH_LOG_DIR}/${COORDINATOR_LOG_NAME}
   COORDINATOR_JAVA_LOG_NAME=coordinator-java.log
   COORDINATOR_JAVA_LOG=${BATFISH_LOG_DIR}/${COORDINATOR_JAVA_LOG_NAME}
   COORDINATOR_LOCK_NAME=coordinator.lock
   COORDINATOR_LOCK=${LOCK_DIR}/${COORDINATOR_LOCK_NAME}
   COORDINATOR_PID_FILE_NAME=coordinator.pid
   COORDINATOR_PID_FILE=${BATFISH_RUN_DIR}/${COORDINATOR_PID_FILE_NAME}
   BATFISH_USER=batfish
   SPEC_FILE_NAME=batfish.spec
   SPEC_FILE=$RBASE/SPECS/$SPEC_FILE_NAME
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
   mkdir -p $RBASE/BUILD
   mkdir -p $RBASE/BUILDROOT
   mkdir -p $RBASE/RPMS
   mkdir -p $RBASE/SPECS
   mkdir -p $RBASE/SOURCES
   mkdir -p $RBASE/SRPMS
   mkdir -p $RBASE/tmp
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

   cat > $SPEC_FILE <<EOF
%define        __spec_install_post %{nil}                                                                                                  
%define          debug_package %{nil}
%define        __os_install_post %{_dbpath}/brp-compress
Name: ${PACKAGE_NAME}
Version: ${BATFISH_VERSION}
Release: ${RELEASE}
Provides: batfish
Obsoletes: batfish
Conflicts: batfish
Summary: RHEL binary package for batfish

Group:   Applications/Engineering
License: Apache2.0
URL:     https://github.com/batfish/batfish
Source0: %{name}-%{version}.tar.gz
BuildArch: ${ARCHITECTURE}

BuildRoot: %{_tmppath}/%name}-%{version}-%{release}-root
$(jdk_requirement)
Requires(pre): /usr/sbin/useradd, /usr/sbin/groupadd, /usr/bin/getent, /bin/mkdir, /bin/chown, /bin/chmod
Requires(post): /bin/mkdir, /bin/chown, /bin/chmod, ${SERVICE}, ${CTL}
Requires(preun): ${SERVICE}, /bin/true

%pre
/usr/bin/getent group $BATFISH_USER > /dev/null || /usr/sbin/groupadd -r $BATFISH_USER
/usr/bin/getent passwd $BATFISH_USER > /dev/null || /usr/sbin/useradd -r -d $BATFISH_HOME -s /bin/bash -g $BATFISH_USER $BATFISH_USER
/bin/mkdir -p $BATFISH_HOME
/bin/chown $BATFISH_USER:$BATFISH_USER $BATFISH_HOME
/bin/chmod 0750 $BATFISH_HOME

%description
%{summary}

%prep
%setup -q

%build
# Empty section.

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}

# in builddir
cp -a * %{buildroot}


%clean
rm -rf %{buildroot}

%post
$(reload_init_scripts)
/bin/chown root:$BATFISH_USER $CONF_DIR
/bin/chmod 0770 $CONF_DIR
/bin/chown root:$BATFISH_USER $BATFISH_PROPERTIES
/bin/chmod 0660 $BATFISH_PROPERTIES
/bin/chown root:$BATFISH_USER $CLIENT_PROPERTIES
/bin/chmod 0660 $CLIENT_PROPERTIES
/bin/chown root:$BATFISH_USER $COORDINATOR_PROPERTIES
/bin/chmod 0660 $COORDINATOR_PROPERTIES
/bin/chown root:batfish $COORDINATOR_KEYSTORE
/bin/chmod 0660 $COORDINATOR_KEYSTORE
/bin/mkdir -p $BATFISH_LOG_DIR
/bin/chown batfish:batfish $BATFISH_LOG_DIR
/bin/chmod 0770 $BATFISH_LOG_DIR
/bin/mkdir -p $BATFISH_RUN_DIR
/bin/chown batfish:batfish $BATFISH_RUN_DIR
/bin/chmod 0755 $BATFISH_RUN_DIR
/bin/chmod 0755 $BATFISH_INIT
/bin/chmod 0755 $COORDINATOR_INIT
${SERVICE} coordinator restart
${SERVICE} batfish restart

%preun
${SERVICE} batfish stop || /bin/true
${SERVICE} coordinator stop || /bin/true

%postun
/bin/rmdir /var/run/batfish

%files
%defattr(-,root,root,-)
%config(noreplace) $BATFISH_PROPERTIES
%config(noreplace) $CLIENT_PROPERTIES
%config(noreplace) $COORDINATOR_PROPERTIES
%config(noreplace) $COORDINATOR_KEYSTORE
%config $BATFISH_INIT
%config $COORDINATOR_INIT
$DATA_DIR/*
$DOC_DIR/*
$USR/bin/*
$USR/lib64/*
$USR/include/*

%changelog
* Mon Jan 9 2017  Ari Fogel <ari@intentionet.com> 0.2.0-1
- Second Build
* Tue Dec 6 2016  Ari Fogel <ari@intentionet.com> 0.1.0-1
- First Build
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

   echo "Building and installing z3 in $USR_P"
   $BATFISH_Z3_RHEL_INSTALLER $USR_P
   cd $RBASE
   fakeroot tar -cpzvf ${TARBALL_NAME} ${PACKAGE_NAME}-${BATFISH_VERSION}/ && cp ${TARBALL_NAME} SOURCES/ && rpmbuild --define "_topdir $RBASE" -ba SPECS/$SPEC_FILE_NAME
   [ $? -ne 0 ] && return 1
   RPM_SRC=$(find RPMS -type f)
   cp $RPM_SRC ${OLD_PWD}/${FINAL_RPM_NAME}
   cd $OLD_PWD
   rm -rf $WORKING
   rpm --addsign ${FINAL_RPM_NAME} || return 1
}
package

