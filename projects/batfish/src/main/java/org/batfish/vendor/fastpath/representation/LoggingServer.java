package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration for a remote logging (syslog) host, set via {@code logging host
 * {hostaddress|hostname} [addresstype] [port] [severitylevel]}.
 */
public final class LoggingServer implements Serializable {

  /** The address type of a {@link LoggingServer}, from the {@code addresstype} argument. */
  public enum AddressType {
    DNS,
    IPV4,
    IPV6,
  }

  public LoggingServer(String host) {
    _host = host;
  }

  /** The configured host: an IP address or a hostname. */
  public @Nonnull String getHost() {
    return _host;
  }

  public @Nullable AddressType getAddressType() {
    return _addressType;
  }

  public void setAddressType(@Nullable AddressType addressType) {
    _addressType = addressType;
  }

  /** The destination UDP port (1-65535), or {@code null} if the default (514) is used. */
  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  /** The minimum severity to log (0-7), or {@code null} if the default is used. */
  public @Nullable Integer getSeverityLevel() {
    return _severityLevel;
  }

  public void setSeverityLevel(@Nullable Integer severityLevel) {
    _severityLevel = severityLevel;
  }

  private final @Nonnull String _host;
  private @Nullable AddressType _addressType;
  private @Nullable Integer _port;
  private @Nullable Integer _severityLevel;
}
