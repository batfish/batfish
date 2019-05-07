package org.batfish.symbolic.state;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PreInInterface implements StateExpr {

  private final String _hostname;

  private final String _iface;

  public PreInInterface(String hostname, String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreInInterface(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getIface() {
    return _iface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PreInInterface)) {
      return false;
    }
    PreInInterface that = (PreInInterface) o;
    return _hostname.equals(that._hostname) && _iface.equals(that._iface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _iface);
  }
}
