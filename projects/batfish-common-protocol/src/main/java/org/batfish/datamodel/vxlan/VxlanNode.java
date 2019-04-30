package org.batfish.datamodel.vxlan;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A VXLAN endpoint. */
@ParametersAreNonnullByDefault
public final class VxlanNode {

  public static final class Builder {

    private @Nullable String _hostname;
    private @Nullable Integer _vni;

    private Builder() {}

    public @Nonnull VxlanNode build() {
      checkArgument(_hostname != null, "Missing %s", "hostname");
      checkArgument(_vni != null, "Missing %s", "vni");
      return new VxlanNode(_hostname, _vni);
    }

    public @Nonnull Builder setHostname(String hostname) {
      _hostname = hostname;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final String _hostname;
  private final int _vni;

  public VxlanNode(String hostname, int vni) {
    _hostname = hostname;
    _vni = vni;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VxlanNode)) {
      return false;
    }
    VxlanNode rhs = (VxlanNode) obj;
    return _hostname.equals(rhs._hostname) && _vni == rhs._vni;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vni);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add("hostname", _hostname).add("vni", _vni).toString();
  }

  /** Hostname of the endpoint. */
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /** VNI number of the endpoint. */
  public int getVni() {
    return _vni;
  }
}
