package org.batfish.minesweeper.abstraction;

import java.util.Objects;
import javax.annotation.Nullable;

class EdgePolicyToRole {

  private Integer _abstractId;

  private EdgePolicy _policy;

  private int _hcode = 0;

  EdgePolicyToRole(Integer abstractId, @Nullable EdgePolicy policy) {
    _abstractId = abstractId;
    _policy = policy;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EdgePolicyToRole)) {
      return false;
    }
    EdgePolicyToRole other = (EdgePolicyToRole) o;
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

  Integer getAbstractId() {
    return _abstractId;
  }

  EdgePolicy getPolicy() {
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
