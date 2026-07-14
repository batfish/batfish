package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A syslog host configured under {@code system syslog host <host>}.
 *
 * <p>Captures the host (IP or hostname) plus the security-relevant attributes Batfish extracts:
 * facility/severity pairs, transport protocol, destination port, and routing-instance.
 */
@ParametersAreNonnullByDefault
public class JunosSyslogHost implements Serializable {

  private final @Nonnull String _host;

  /**
   * Facility -> severity assignments for this host. A host may specify multiple facilities, each
   * with its own severity (e.g. {@code any notice}, {@code kernel error}).
   */
  private final @Nonnull Map<JunosSyslogFacility, JunosSyslogSeverity> _facilitySeverities;

  private @Nullable Integer _port;

  private @Nullable String _routingInstance;

  private @Nullable JunosSyslogTransportProtocol _transportProtocol;

  public JunosSyslogHost(String host) {
    _host = host;
    _facilitySeverities = new TreeMap<>();
  }

  public @Nonnull String getHost() {
    return _host;
  }

  public @Nonnull Map<JunosSyslogFacility, JunosSyslogSeverity> getFacilitySeverities() {
    return _facilitySeverities;
  }

  /** Assign {@code severity} to {@code facility} for this host. */
  public void setFacilitySeverity(JunosSyslogFacility facility, JunosSyslogSeverity severity) {
    _facilitySeverities.put(facility, severity);
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  public @Nullable String getRoutingInstance() {
    return _routingInstance;
  }

  public void setRoutingInstance(@Nullable String routingInstance) {
    _routingInstance = routingInstance;
  }

  public @Nullable JunosSyslogTransportProtocol getTransportProtocol() {
    return _transportProtocol;
  }

  public void setTransportProtocol(@Nullable JunosSyslogTransportProtocol transportProtocol) {
    _transportProtocol = transportProtocol;
  }
}
