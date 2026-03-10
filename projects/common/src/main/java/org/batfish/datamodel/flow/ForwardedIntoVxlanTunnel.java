package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A flow being forwarded into a VXLAN tunnel. */
public final class ForwardedIntoVxlanTunnel implements ForwardingDetail {

  public static @Nonnull ForwardedIntoVxlanTunnel of(int vni, Ip vtep) {
    return new ForwardedIntoVxlanTunnel(vni, vtep);
  }

  private ForwardedIntoVxlanTunnel(int vni, Ip vtep) {
    _vni = vni;
    _vtep = vtep;
  }

  @JsonCreator
  private static @Nonnull ForwardedIntoVxlanTunnel create(
      @JsonProperty(PROP_VNI) @Nullable Integer vni, @JsonProperty(PROP_VTEP) @Nullable Ip vtep) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vtep != null, "Missing %s", PROP_VTEP);
    return of(vni, vtep);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ForwardedIntoVxlanTunnel)) {
      return false;
    }
    ForwardedIntoVxlanTunnel that = (ForwardedIntoVxlanTunnel) o;
    return _vni == that._vni && _vtep.equals(that._vtep);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vtep);
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  @JsonProperty(PROP_VTEP)
  public @Nonnull Ip getVtep() {
    return _vtep;
  }

  private static final String PROP_VNI = "vni";
  private static final String PROP_VTEP = "vtep";

  private final int _vni;
  private final @Nonnull Ip _vtep;
}
