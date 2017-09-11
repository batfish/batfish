package org.batfish.symbolic.abstraction;

public class InterfacePolicy {

  private Integer _ospfCost;


  public InterfacePolicy(Integer ospfCost) {
    this._ospfCost = ospfCost;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    InterfacePolicy that = (InterfacePolicy) o;

    return _ospfCost != null ? _ospfCost.equals(that._ospfCost) : that._ospfCost == null;
  }

  @Override public int hashCode() {
    return _ospfCost != null ? _ospfCost.hashCode() : 0;
  }

  @Override public String toString() {
    return _ospfCost.toString();
  }
}
