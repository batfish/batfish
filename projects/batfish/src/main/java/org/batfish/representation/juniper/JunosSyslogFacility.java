package org.batfish.representation.juniper;

/** Syslog facilities that can be configured under {@code system syslog host}. */
public enum JunosSyslogFacility {
  ANY,
  AUTHORIZATION,
  CHANGE_LOG,
  CONFLICT_LOG,
  DAEMON,
  DFC,
  EXTERNAL,
  FIREWALL,
  FTP,
  INTERACTIVE_COMMANDS,
  KERNEL,
  NTP,
  PFE,
  USER
}
