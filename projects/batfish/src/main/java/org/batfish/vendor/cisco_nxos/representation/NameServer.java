package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Name server configuration. Contains ipv4 or ipv6 address of the nameserver, and what VRF to use
 * for fallback name resolution.
 */
@ParametersAreNonnullByDefault
public final class NameServer implements Serializable {

  public NameServer(String name, @Nullable String useVrf) {
    _ip = name;
    _useVrf = useVrf;
  }

  public String getName() {
    return _ip;
  }

  /** VRF to use for fallback name resolution */
  public @Nullable String getUseVrf() {
    return _useVrf;
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
    return _ip.equals(that._ip) && Objects.equals(_useVrf, that._useVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _useVrf);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("ip", _ip).add("useVrf", _useVrf).toString();
  }

  private final @Nonnull String _ip;
  private final @Nullable String _useVrf;
}
