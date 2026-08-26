package org.batfish.vendor.fastpath.representation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Aggregates a FastPath device's TACACS+ ({@code tacacs-server}) configuration */
public final class Tacacs implements Serializable {

  private final @Nonnull Map<String, TacacsServer> _servers;
  private @Nullable String _sourceInterface;
  private @Nullable Integer _timeout;
  private @Nullable Boolean _keyEncrypted;

  public Tacacs() {
    _servers = new LinkedHashMap<>();
  }

  public @Nonnull Map<String, TacacsServer> getServers() {
    return _servers;
  }

  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  public void setSourceInterface(String sourceInterface) {
    _sourceInterface = sourceInterface;
  }

  public @Nullable Integer getTimeout() {
    return _timeout;
  }

  public void setTimeout(@Nullable Integer timeout) {
    _timeout = timeout;
  }

  /** Whether the global {@code key} is encrypted, or {@code null} if no {@code key} was set. */
  public @Nullable Boolean getKeyEncrypted() {
    return _keyEncrypted;
  }

  public void setKeyEncrypted(@Nullable Boolean keyEncrypted) {
    _keyEncrypted = keyEncrypted;
  }
}
