package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A syslog server configured via {@code logging host interface_name syslog_ip [tcp[/port] |
 * udp[/port]]}.
 *
 * <p>The host is stored as its literal text so that both IPv4 and IPv6 syslog servers are
 * supported.
 */
@ParametersAreNonnullByDefault
public final class LoggingHost implements Serializable {

  private final @Nonnull String _interfaceName;
  private final @Nonnull String _host;
  private @Nullable SyslogTransportProtocol _transport;
  private @Nullable Integer _port;

  public LoggingHost(String interfaceName, String host) {
    _interfaceName = interfaceName;
    _host = host;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  public @Nullable SyslogTransportProtocol getTransport() {
    return _transport;
  }

  public void setTransport(@Nullable SyslogTransportProtocol transport) {
    _transport = transport;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }
}
