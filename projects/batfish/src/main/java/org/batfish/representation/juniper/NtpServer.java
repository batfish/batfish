package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An NTP server configured under {@code system ntp server}. */
@ParametersAreNonnullByDefault
public final class NtpServer implements Serializable {

  private final @Nonnull String _host;
  private @Nullable Integer _key;

  public NtpServer(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  /** The key-number referenced by this server's {@code key} statement, if any. */
  public @Nullable Integer getKey() {
    return _key;
  }

  public void setKey(@Nullable Integer key) {
    _key = key;
  }

  /**
   * Whether this server has NTP authentication fully configured: it references a key, that key is
   * defined via {@code authentication-key}, and it is listed in {@code trusted-key}.
   */
  public boolean isAuthenticated(Set<Integer> definedKeys, Set<Integer> trustedKeys) {
    return _key != null && definedKeys.contains(_key) && trustedKeys.contains(_key);
  }
}
