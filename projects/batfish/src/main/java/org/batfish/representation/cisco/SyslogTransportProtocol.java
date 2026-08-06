package org.batfish.representation.cisco;

/** The transport protocol used to send syslog messages to a syslog server. */
public enum SyslogTransportProtocol {
  // Cisco IOS defaults: UDP syslog uses port 514, TCP syslog uses port 601.
  TCP(601),
  UDP(514);

  private final int _defaultPort;

  SyslogTransportProtocol(int defaultPort) {
    _defaultPort = defaultPort;
  }

  public int getDefaultPort() {
    return _defaultPort;
  }
}
