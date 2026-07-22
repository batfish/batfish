package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for a logging-server. */
public final class LoggingServer implements Serializable {

  public LoggingServer(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  public @Nullable Integer getSeverityLevel() {
    return _severityLevel;
  }

  public void setSeverityLevel(@Nullable Integer level) {
    _severityLevel = level;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  public @Nullable String getFacility() {
    return _facility;
  }

  public void setFacility(@Nullable String facility) {
    _facility = facility;
  }

  public @Nullable String getUseVrf() {
    return _useVrf;
  }

  public void setUseVrf(@Nullable String useVrf) {
    _useVrf = useVrf;
  }

  /** Whether the {@code secure} option is configured for this logging server. */
  public boolean getSecure() {
    return _secure;
  }

  public void setSecure(boolean secure) {
    _secure = secure;
  }

  private final @Nonnull String _host;
  private @Nullable Integer _severityLevel;
  private @Nullable Integer _port;
  private @Nullable String _facility;
  private @Nullable String _useVrf;
  private boolean _secure;
}
