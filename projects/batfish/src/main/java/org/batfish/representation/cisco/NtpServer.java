package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An NTP server configured via {@code ntp server}. */
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

  /** The key number referenced by this server's {@code key} statement, if any. */
  public @Nullable Integer getKey() {
    return _key;
  }

  public void setKey(@Nullable Integer key) {
    _key = key;
  }
}
