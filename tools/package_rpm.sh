#!/usr/bin/env bash
export BATFISH_SOURCED_SCRIPT="$BASH_SOURCE"
export OLD_PWD="$PWD"
package() {
   local BATFISH_TOOLS_PATH="$(readlink -f $(dirname $BATFISH_SOURCED_SCRIPT))"
   local BATFISH_PATH="$(readlink -f ${BATFISH_TOOLS_PATH}/..)"
   local VERSION_FILE=$BATFISH_PATH/projects/batfish-common-protocol/src/org/batfish/common/Version.java
   local VERSION=$(grep 'private static final String VERSION' $VERSION_FILE | sed -e 's/^[^"]*"\([^"]*\)".*$/\1/g')
   local RELEASE=1
   local PACKAGE_NAME="batfish-${VERSION}"
   local BATFISH_Z3_RHEL_INSTALLER=${BATFISH_TOOLS_PATH}/install_z3_rhel_x86_64.sh
   local WORKING=$(mktemp -d)
   local RBASE=$WORKING/rpmbuild
   local PBASE=$RBASE/$PACKAGE_NAME
   local USR=/usr
   local USR_P=${PBASE}${USR}
   local DATA_DIR=/usr/share/batfish
   local DATA_DIR_P=${PBASE}${DATA_DIR}
   local CONF_DIR=/etc/batfish
   local CONF_DIR_P=${PBASE}${CONF_DIR}
   local DOC_DIR=/usr/share/doc/batfish
   local DOC_DIR_P=${PBASE}${DOC_DIR}
   local INIT_DIR=/etc/init.d
   local INIT_DIR_P=${PBASE}${INIT_DIR}
   local BATFISH_INIT=${INIT_DIR}/batfish
   local BATFISH_INIT_P=${PBASE}${BATFISH_INIT}
   local BATFISH_JAR_NAME=batfish.jar
   local BATFISH_JAR_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_JAR_NAME
   local BATFISH_JAR=${DATA_DIR}/$BATFISH_JAR_NAME
   local BATFISH_JAR_P=${PBASE}${BATFISH_JAR}
   local BATFISH_PROPERTIES_NAME=batfish.properties
   local BATFISH_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish/out/$BATFISH_PROPERTIES_NAME
   local BATFISH_PROPERTIES_LINK=${DATA_DIR}/$BATFISH_PROPERTIES_NAME
   local BATFISH_PROPERTIES_LINK_P=${PBASE}${BATFISH_PROPERTIES_LINK}
   local BATFISH_PROPERTIES=${CONF_DIR}/$BATFISH_PROPERTIES_NAME
   local BATFISH_PROPERTIES_P=${PBASE}${BATFISH_PROPERTIES}
   local CLIENT_JAR_NAME=batfish-client.jar
   local CLIENT_JAR_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_JAR_NAME
   local CLIENT_JAR=${DATA_DIR}/$CLIENT_JAR_NAME
   local CLIENT_JAR_P=${PBASE}${CLIENT_JAR}
   local CLIENT_PROPERTIES_NAME=client.properties
   local CLIENT_PROPERTIES_SRC=$BATFISH_PATH/projects/batfish-client/out/$CLIENT_PROPERTIES_NAME
   local CLIENT_PROPERTIES_LINK=${DATA_DIR}/$CLIENT_PROPERTIES_NAME
   local CLIENT_PROPERTIES_LINK_P=${PBASE}${CLIENT_PROPERTIES_LINK}
   local CLIENT_PROPERTIES=${CONF_DIR}/$CLIENT_PROPERTIES_NAME
   local CLIENT_PROPERTIES_P=${PBASE}${CLIENT_PROPERTIES}
   local COORDINATOR_INIT=${INIT_DIR}/coordinator
   local COORDINATOR_INIT_P=${PBASE}${COORDINATOR_INIT}
   local COORDINATOR_JAR_NAME=coordinator.jar
   local COORDINATOR_JAR_SRC=$BATFISH_PATH/projects/coordinator/out/${COORDINATOR_JAR_NAME}
   local COORDINATOR_JAR=${DATA_DIR}/$COORDINATOR_JAR_NAME
   local COORDINATOR_JAR_P=${PBASE}$COORDINATOR_JAR
   local COORDINATOR_PROPERTIES_NAME=coordinator.properties
   local COORDINATOR_PROPERTIES_SRC=${BATFISH_PATH}/projects/coordinator/out/${COORDINATOR_PROPERTIES_NAME}
   local COORDINATOR_PROPERTIES_LINK=${DATA_DIR}/${COORDINATOR_PROPERTIES_NAME}
   local COORDINATOR_PROPERTIES_LINK_P=${PBASE}${COORDINATOR_PROPERTIES_LINK}
   local COORDINATOR_PROPERTIES=${CONF_DIR}/${COORDINATOR_PROPERTIES_NAME}
   local COORDINATOR_PROPERTIES_P=${PBASE}${COORDINATOR_PROPERTIES}
   local COORDINATOR_KEYSTORE_NAME=selfsigned.jks
   local COORDINATOR_KEYSTORE_SRC=${BATFISH_PATH}/projects/coordinator/out/${COORDINATOR_KEYSTORE_NAME}
   local COORDINATOR_KEYSTORE_LINK=${DATA_DIR}/${COORDINATOR_KEYSTORE_NAME}
   local COORDINATOR_KEYSTORE_LINK_P=${PBASE}${COORDINATOR_KEYSTORE_LINK}
   local COORDINATOR_KEYSTORE=${CONF_DIR}/${COORDINATOR_KEYSTORE_NAME}
   local COORDINATOR_KEYSTORE_P=${PBASE}${COORDINATOR_KEYSTORE}
   local PLUGIN_DIR=${DATA_DIR}/plugins
   local PLUGIN_DIR_P=${PBASE}${PLUGIN_DIR}
   local QUESTION_JAR_NAME=question.jar
   local QUESTION_JAR_SRC=${BATFISH_PATH}/projects/question/out/${QUESTION_JAR_NAME}
   local QUESTION_JAR=${PLUGIN_DIR}/${QUESTION_JAR_NAME}
   local QUESTION_JAR_P=${PBASE}${QUESTION_JAR}
   local COPYRIGHT_NAME=copyright
   local COPYRIGHT=${DOC_DIR}/${COPYRIGHT_NAME}
   local COPYRIGHT_P=${PBASE}${COPYRIGHT}
   local BATFISH_HOME=/var/batfish                                                                                                         
   local BATFISH_LOG_DIR=/var/log/batfish
   local BATFISH_RUN_DIR=/var/run/batfish
   local BATFISH_LOG_NAME=batfish.log
   local BATFISH_LOG=$BATFISH_LOG_DIR/$BATFISH_LOG_NAME
   local COORDINATOR_LOG_NAME=coordinator.log
   local COORDINATOR_LOG_DST=$BATFISH_LOG_DIR/$COORDINATOR_LOG_NAME
   local BATFISH_USER=batfish
   local SPEC_FILE_NAME=batfish.spec
   local SPEC_FILE=$RBASE/SPECS/$SPEC_FILE_NAME
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

   cat > $SPEC_FILE <<EOF
%define        __spec_install_post %{nil}                                                                                                  
%define          debug_package %{nil}
%define        __os_install_post %{_dbpath}/brp-compress
Name:    batfish
Version: $VERSION
Release: $RELEASE
Summary: RHEL binary package for batfish

Group:   Applications/Engineering
License: Apache2.0
URL:     https://github.com/intentionet/batfish
Source0: %{name}-%{version}.tar.gz
BuildArch: x86_64

BuildRoot: %{_tmppath}/%name}-%{version}-%{release}-root
Requires(pre): /usr/sbin/useradd, /usr/sbin/groupadd, /usr/bin/getent, /bin/mkdir, /bin/chown, /bin/chmod
Requires(post): /bin/mkdir, /bin/chown, /bin/chmod, /sbin/service, /sbin/initctl
Requires(preun): /sbin/service, /bin/true

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
/sbin/initctl reload-configuration
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
/sbin/service coordinator restart
/sbin/service batfish restart

%preun
/sbin/service batfish stop || /bin/true
/sbin/service coordinator stop || /bin/true

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
prog="batfish"
LOCKFILE=/var/lock/subsys/\$prog
PIDFILE=$BATFISH_RUN_DIR/\${prog}.pid

start() {
   echo -n "Starting \$prog: "
   if [ -f \$PIDFILE ]; then
      echo "Already running with pid: \$(cat \$PIDFILE)"
      return 1
   fi
   if [ -f \$LOCKFILE ]; then
      echo "Missing pid file, but lock file present: \$LOCKFILE"
      return 1
   fi
   su -c "bash -c '/usr/bin/java -DbatfishQuestionPluginDir=$PLUGIN_DIR -jar $BATFISH_JAR -logfile $BATFISH_LOG_DIR/\${prog}.log -servicemode -register true >& /dev/null & echo \\\$! > \$PIDFILE'" batfish
   touch \$LOCKFILE
   echo "Sucess"
}

stop() {
   echo -n "Shutting down \$prog: "
   RETVAL=0
   if [ -f \$PIDFILE ]; then
      PID=\$(cat \$PIDFILE)
      kill -9 \$PID
      RETVAL=\$?
      rm -f \$PIDFILE
   fi
   [ \$RETVAL -eq 0 ] && rm -f \$LOCKFILE && echo "Success"
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
      echo "Usage: \$prog {start|stop|status|restart]"
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
prog="coordinator"
LOCKFILE=/var/lock/subsys/\$prog
PIDFILE=$BATFISH_RUN_DIR/\${prog}.pid

start() {
   echo -n "Starting \$prog: "
   if [ -f \$PIDFILE ]; then
      echo "Already running with pid: \$(cat \$PIDFILE)" >&2
      return 1
   fi
   if [ -f \$LOCKFILE ]; then
      echo "Missing pid file, but lock file present: \$LOCKFILE" >&2
      return 1
   fi
   su -c "bash -c '/usr/bin/java -jar $COORDINATOR_JAR -loglevel debug -logfile $BATFISH_LOG_DIR/\${prog}.log -servicehost localhost -containerslocation /var/batfish >& /dev/null & echo \\\$! > \$PIDFILE'" batfish
   touch \$LOCKFILE
   echo "Success"
}

stop() {
   echo -n "Shutting down \$prog: "
   RETVAL=0
   if [ -f \$PIDFILE ]; then
      PID=\$(cat \$PIDFILE)
      kill -9 \$PID
      RETVAL=\$?
      rm -f \$PIDFILE
   fi
   [ \$RETVAL -eq 0 ] && rm -f \$LOCKFILE && echo "Success"
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
      echo "Usage: \$prog {start|stop|status|restart]"
      exit 1
   ;;
esac
exit \$?
EOF
   echo "Building and installing z3 in $USR_P"
   $BATFISH_Z3_RHEL_INSTALLER $USR_P
   cd $RBASE
   fakeroot tar -cpzvf ${PACKAGE_NAME}.tar.gz ${PACKAGE_NAME}/ && cp ${PACKAGE_NAME}.tar.gz SOURCES/ && rpmbuild --define "_topdir $RBASE" -ba SPECS/$SPEC_FILE_NAME
   [ $? -ne 0 ] && return 1
   RPM_SRC=$(find RPMS -type f)
   RPM_NAME=$(basename $RPM_SRC)
   cp $RPM_SRC ${OLD_PWD}/
   cd $OLD_PWD
   rm -rf $WORKING
   rpm --addsign $RPM_NAME || return 1
}
package

