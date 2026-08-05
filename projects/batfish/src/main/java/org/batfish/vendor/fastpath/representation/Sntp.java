package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Aggregates a FastPath device's SNTP ({@code sntp}) configuration, keeping the individual settings
 * off of {@link FastpathConfiguration}.
 */
public final class Sntp implements Serializable {

  /** SNTP client operating mode, from {@code sntp client mode [broadcast | unicast]}. */
  public enum ClientMode {
    BROADCAST,
    UNICAST,
  }

  public Sntp() {
    _servers = new LinkedHashSet<>();
  }

  /** Configured SNTP servers (IP addresses or hostnames). */
  public @Nonnull Set<String> getServers() {
    return _servers;
  }

  public void addServer(String server) {
    _servers.add(server);
  }

  /** The SNTP client operating mode ({@code sntp client mode}), or {@code null} if not set. */
  public @Nullable ClientMode getClientMode() {
    return _clientMode;
  }

  public void setClientMode(@Nullable ClientMode clientMode) {
    _clientMode = clientMode;
  }

  /**
   * The SNTP client source port ({@code sntp client port}, 1-65535), or {@code null} if the default
   * (0, OS-assigned) is used.
   */
  public @Nullable Integer getClientPort() {
    return _clientPort;
  }

  public void setClientPort(@Nullable Integer clientPort) {
    _clientPort = clientPort;
  }

  /** The SNTP client source interface ({@code sntp source-interface}), or {@code null}. */
  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(@Nullable String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  private final @Nonnull Set<String> _servers;
  private @Nullable ClientMode _clientMode;
  private @Nullable Integer _clientPort;
  private @Nullable String _sourceInterface;
}
