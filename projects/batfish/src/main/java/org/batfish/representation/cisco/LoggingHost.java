package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A syslog server configured via {@code logging host <host> [transport tcp|udp] [port num]}.
 *
 * <p>The host is stored as its literal text so that both IPv4 and IPv6 syslog servers (and
 * hostnames) are supported.
 */
@ParametersAreNonnullByDefault
public final class LoggingHost implements Serializable {

  private final @Nonnull String _host;
  private @Nullable SyslogTransportProtocol _transport;
  private @Nullable Integer _port;

  public LoggingHost(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  /**
   * Returns the transport protocol used to reach this syslog server. When no transport is
   * explicitly configured, IOS defaults to {@link SyslogTransportProtocol#UDP}.
   */
  public @Nonnull SyslogTransportProtocol getTransport() {
    return _transport == null ? SyslogTransportProtocol.UDP : _transport;
  }

  public void setTransport(@Nullable SyslogTransportProtocol transport) {
    _transport = transport;
  }

  /**
   * Returns the destination port used to reach this syslog server. When no port is explicitly
   * configured, it defaults to the {@link #getTransport() effective transport}'s default port.
   */
  public int getPort() {
    return _port == null ? getTransport().getDefaultPort() : _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }
}
