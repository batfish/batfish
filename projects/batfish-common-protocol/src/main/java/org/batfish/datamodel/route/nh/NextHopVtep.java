package org.batfish.datamodel.route.nh;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * Route next hop with a VTEP {@link Ip} address and a VNI. Traffic routed via this next hop will be
 * sent out the VXLAN edge matching the VTEP IP, then processed in the remote VRF matching the VNI.
 * Note that this class will reject invalid VTEP IP values such as {@link Ip#ZERO}, {@link Ip#MAX},
 * {@link Ip#AUTO}.
 */
@ParametersAreNonnullByDefault
public class NextHopVtep implements NextHop {

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }

  @JsonProperty(PROP_VTEP)
  public @Nonnull Ip getVtepIp() {
    return _vtepIp;
  }

  public static @Nonnull NextHopVtep of(int vni, Ip vtepIp) {
    return new NextHopVtep(vni, vtepIp);
  }

  @JsonCreator
  private static @Nonnull NextHopVtep create(
      @JsonProperty(PROP_VNI) @Nullable Integer vni, @JsonProperty(PROP_VTEP) @Nullable Ip vtep) {
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    checkArgument(vtep != null, "Missing %s", PROP_VTEP);
    return of(vni, vtep);
  }

  private NextHopVtep(int vni, Ip vtepIp) {
    checkArgument(
        !vtepIp.equals(Ip.AUTO) && !vtepIp.equals(Ip.ZERO) && !vtepIp.equals(Ip.MAX),
        "VTEP IP must be a valid concrete IP address. Received %s",
        vtepIp);
    _vni = vni;
    _vtepIp = vtepIp;
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopVtep(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NextHopVtep)) {
      return false;
    }
    NextHopVtep that = (NextHopVtep) o;
    return _vni == that._vni && _vtepIp.equals(that._vtepIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vtepIp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NextHopVtep.class)
        .add("vni", _vni)
        .add("vtepIp", _vtepIp)
        .toString();
  }

  private static final String PROP_VNI = "vni";
  private static final String PROP_VTEP = "vtep";

  private final int _vni;
  private final @Nonnull Ip _vtepIp;
}
