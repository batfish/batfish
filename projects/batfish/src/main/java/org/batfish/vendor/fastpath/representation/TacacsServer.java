package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A single TACACS+ server host, from {@code tacacs-server host <host>} and its host-config submode
 * options ({@code port}/{@code priority}/{@code timeout}/{@code key}).
 */
public final class TacacsServer implements Serializable {

  private final @Nonnull String _host;
  private @Nullable Integer _port;
  private @Nullable Integer _priority;
  private @Nullable Integer _timeout;
  private boolean _keyEncrypted;

  public TacacsServer(String host) {
    _host = host;
  }

  /** The server IP address or hostname. */
  public @Nonnull String getHost() {
    return _host;
  }

  public @Nullable Integer getPort() {
    return _port;
  }

  public void setPort(@Nullable Integer port) {
    _port = port;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  public @Nullable Integer getTimeout() {
    return _timeout;
  }

  public void setTimeout(@Nullable Integer timeout) {
    _timeout = timeout;
  }

  public boolean getKeyEncrypted() {
    return _keyEncrypted;
  }

  public void setKeyEncrypted(boolean keyEncrypted) {
    _keyEncrypted = keyEncrypted;
  }
}
