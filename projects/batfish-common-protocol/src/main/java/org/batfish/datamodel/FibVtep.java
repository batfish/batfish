package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * A {@link FibAction} indicating traffic should be sent out the VXLAN edge matching a given VTEP IP
 * and VNI, then processed in the remote node's VRF matching the VNI.
 */
public class FibVtep implements FibAction {
  public int getVni() {
    return _vni;
  }

  public @Nonnull Ip getVtepIp() {
    return _vtepIp;
  }

  public FibVtep(int vni, Ip vtepIp) {
    checkArgument(
        !vtepIp.equals(Ip.AUTO) && !vtepIp.equals(Ip.ZERO) && !vtepIp.equals(Ip.MAX),
        "VTEP IP must be a valid concrete IP address. Received %s",
        vtepIp);
    _vni = vni;
    _vtepIp = vtepIp;
  }

  @Override
  public <T> T accept(FibActionVisitor<T> visitor) {
    return visitor.visitFibVtep(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof FibVtep)) {
      return false;
    }
    FibVtep that = (FibVtep) o;
    return _vni == that._vni && _vtepIp.equals(that._vtepIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_vni, _vtepIp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FibVtep.class)
        .add("vni", _vni)
        .add("vtepIp", _vtepIp)
        .toString();
  }

  private final int _vni;
  private final @Nonnull Ip _vtepIp;
}
