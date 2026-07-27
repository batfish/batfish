package org.batfish.representation.cisco_asa;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A single DNS server entry within a {@link DnsServerGroup}, corresponding to one address on a
 * {@code name-server <ip> [...] [interface]} line. Holds the server IP and the optional {@code
 * nameif} interface through which the ASA reaches the server.
 */
@ParametersAreNonnullByDefault
public final class NameServer implements Serializable {

  public NameServer(String ip, @Nullable String sourceInterface) {
    _ip = ip;
    _sourceInterface = sourceInterface;
  }

  public @Nonnull String getIp() {
    return _ip;
  }

  public @Nullable String getSourceInterface() {
    return _sourceInterface;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameServer)) {
      return false;
    }
    NameServer that = (NameServer) o;
    return _ip.equals(that._ip) && Objects.equals(_sourceInterface, that._sourceInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _sourceInterface);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("ip", _ip)
        .add("sourceInterface", _sourceInterface)
        .toString();
  }

  private final @Nonnull String _ip;
  private final @Nullable String _sourceInterface;
}
