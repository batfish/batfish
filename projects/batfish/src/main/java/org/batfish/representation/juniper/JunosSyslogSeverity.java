package org.batfish.representation.juniper;

/** Syslog severity levels that can be configured under {@code system syslog host}. */
public enum JunosSyslogSeverity {
  ANY,
  NONE,
  EMERGENCY,
  ALERT,
  CRITICAL,
  ERROR,
  WARNING,
  NOTICE,
  INFO
}
