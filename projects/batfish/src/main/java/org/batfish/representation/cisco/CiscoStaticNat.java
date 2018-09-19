package org.batfish.representation.cisco;

import java.util.Objects;
import org.batfish.datamodel.Prefix;

public class CiscoStaticNat extends CiscoNat {
  private static final long serialVersionUID = 1L;
  private Prefix _localNetwork;
  private Prefix _globalNetwork;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CiscoStaticNat)) {
      return false;
    }
    CiscoStaticNat other = (CiscoStaticNat) o;
    return (_action == other._action)
        && Objects.equals(_localNetwork, other._localNetwork)
        && Objects.equals(_globalNetwork, other._globalNetwork);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action, _localNetwork, _globalNetwork);
  }

  public Prefix getLocalNetwork() {
    return _localNetwork;
  }

  public Prefix getGlobalNetwork() {
    return _globalNetwork;
  }

  public void setLocalNetwork(Prefix localNetwork) {
    _localNetwork = localNetwork;
  }

  public void setGlobalNetwork(Prefix globalNetwork) {
    _globalNetwork = globalNetwork;
  }
}
