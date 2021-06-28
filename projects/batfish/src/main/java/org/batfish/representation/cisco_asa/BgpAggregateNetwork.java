package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Objects;

public abstract class BgpAggregateNetwork implements Serializable {

  private boolean _asSet;

  private String _attributeMap;

  private boolean _summaryOnly;

  @Override
  public abstract boolean equals(Object o);

  protected final boolean baseEquals(BgpAggregateNetwork that) {
    return _asSet == that._asSet
        && Objects.equals(_attributeMap, that._attributeMap)
        && _summaryOnly == that._summaryOnly;
  }

  public boolean getAsSet() {
    return _asSet;
  }

  public String getAttributeMap() {
    return _attributeMap;
  }

  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  @Override
  public abstract int hashCode();

  protected final int baseHashcode() {
    return Objects.hash(_asSet, _attributeMap, _summaryOnly);
  }

  public void setAsSet(boolean asSet) {
    _asSet = asSet;
  }

  public void setAttributeMap(String attributeMap) {
    _attributeMap = attributeMap;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
