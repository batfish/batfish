package org.batfish.representation.cisco;

import java.io.Serializable;

public abstract class BgpAggregateNetwork implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _asSet;

  private String _attributeMap;

  private boolean _summaryOnly;

  @Override
  public abstract boolean equals(Object o);

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
