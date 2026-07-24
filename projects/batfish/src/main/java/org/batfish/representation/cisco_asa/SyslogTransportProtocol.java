package org.batfish.representation.cisco_asa;

/** The transport protocol used to send syslog messages to a syslog server. */
public enum SyslogTransportProtocol {
  TCP(1470),
  UDP(514);

  private final int _defaultPort;

  SyslogTransportProtocol(int defaultPort) {
    _defaultPort = defaultPort;
  }

  public int getDefaultPort() {
    return _defaultPort;
  }
}
