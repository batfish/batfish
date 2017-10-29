package org.batfish.symbolic.abstraction;

import java.util.Objects;
import javax.annotation.Nullable;

public class EquivalenceEdge {

  private Integer _abstractId;

  private InterfacePolicyPair _policy;

  private int _hcode = 0;

  public EquivalenceEdge(Integer abstractId, @Nullable InterfacePolicyPair policy) {
    this._abstractId = abstractId;
    this._policy = policy;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EquivalenceEdge)) {
      return false;
    }
    EquivalenceEdge other = (EquivalenceEdge) o;
    return Objects.equals(_abstractId, other._abstractId) && Objects.equals(_policy, other._policy);
  }

  @Override
  public int hashCode() {
    if (_hcode == 0) {
      int result = _abstractId != null ? _abstractId.hashCode() : 0;
      result = 31 * result + (_policy != null ? _policy.hashCode() : 0);
      _hcode = result;
    }
    return _hcode;
  }

  public Integer getAbstractId() {
    return _abstractId;
  }

  public InterfacePolicyPair getPolicy() {
    return _policy;
  }

  @Override
  public String toString() {
    return "EquivalenceEdge{"
        + "_abstractId="
        + _abstractId
        + ", _policy="
        + _policy.hashCode()
        + '}';
  }
}
