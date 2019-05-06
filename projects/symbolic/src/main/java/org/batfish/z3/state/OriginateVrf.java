package org.batfish.z3.state;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class OriginateVrf implements StateExpr {

  private final String _hostname;

  private final String _vrf;

  public OriginateVrf(String hostname, String vrf) {
    _hostname = hostname;
    _vrf = vrf;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitOriginateVrf(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrf() {
    return _vrf;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OriginateVrf)) {
      return false;
    }
    OriginateVrf that = (OriginateVrf) o;
    return _hostname.equals(that._hostname) && _vrf.equals(that._vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vrf);
  }
}
