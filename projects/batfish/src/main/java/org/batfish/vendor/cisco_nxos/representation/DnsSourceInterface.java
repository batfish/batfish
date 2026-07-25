package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Global DNS source interface configuration, corresponding to the {@code ip dns source-interface
 * <interface> [vrf <vrf>]} command. Determines the interface used to source DNS packets when the
 * device acts as a DNS client/resolver.
 */
@ParametersAreNonnullByDefault
public final class DnsSourceInterface implements Serializable {

  public DnsSourceInterface(String iface, @Nullable String vrf) {
    _interface = iface;
    _vrf = vrf;
  }

  /** Interface used to source DNS packets. */
  public @Nonnull String getInterface() {
    return _interface;
  }

  /** VRF associated with the source interface, or {@code null} if none is configured. */
  public @Nullable String getVrf() {
    return _vrf;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DnsSourceInterface)) {
      return false;
    }
    DnsSourceInterface that = (DnsSourceInterface) o;
    return _interface.equals(that._interface) && Objects.equals(_vrf, that._vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interface, _vrf);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("interface", _interface)
        .add("vrf", _vrf)
        .toString();
  }

  private final @Nonnull String _interface;
  private final @Nullable String _vrf;
}
