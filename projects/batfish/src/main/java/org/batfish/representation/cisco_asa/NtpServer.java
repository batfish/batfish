package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An NTP server configured via {@code ntp server}. */
@ParametersAreNonnullByDefault
public final class NtpServer implements Serializable {

  private final @Nonnull String _host;
  private @Nullable Long _key;

  public NtpServer(String host) {
    _host = host;
  }

  public @Nonnull String getHost() {
    return _host;
  }

  /** The key number referenced by this server's {@code key} statement, if any. */
  public @Nullable Long getKey() {
    return _key;
  }

  public void setKey(@Nullable Long key) {
    _key = key;
  }
}
