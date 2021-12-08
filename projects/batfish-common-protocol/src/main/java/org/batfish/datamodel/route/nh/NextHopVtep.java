package org.batfish.datamodel.route.nh;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
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
  public int getVni() {
    return _vni;
  }

  public @Nonnull Ip getVtepIp() {
    return _vtepIp;
  }

  public NextHopVtep(int vni, Ip vtepIp) {
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
  public boolean equals(Object o) {
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

  private final int _vni;
  private final @Nonnull Ip _vtepIp;
}
