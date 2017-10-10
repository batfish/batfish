package org.batfish.symbolic.abstraction;

import java.util.Objects;

public class InterfacePolicy {

  private Integer _ospfCost;

  private BDDRecord _importPolicy;

  private BDDRecord _exportPolicy;

  public InterfacePolicy(Integer ospfCost, BDDRecord iPol, BDDRecord ePol) {
    this._ospfCost = ospfCost;
    this._importPolicy = iPol;
    this._exportPolicy = ePol;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfacePolicy)) {
      return false;
    }
    InterfacePolicy other = (InterfacePolicy) o;
    return Objects.equals(_ospfCost, other._ospfCost)
        && Objects.equals(_importPolicy, other._importPolicy)
        && Objects.equals(_exportPolicy, other._exportPolicy);
  }

  @Override public int hashCode() {
    int result = _ospfCost != null ? _ospfCost.hashCode() : 0;
    result = 31 * result + (_importPolicy != null ? _importPolicy.hashCode() : 0);
    result = 31 * result + (_exportPolicy != null ? _exportPolicy.hashCode() : 0);
    return result;
  }
}