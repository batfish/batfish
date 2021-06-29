package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class BgpAggregateNetwork implements Serializable {

  private @Nullable String _advertiseMap;
  private boolean _asSet;
  private @Nullable String _attributeMap;
  private boolean _summaryOnly;
  private @Nullable String _suppressMap;

  public boolean getAsSet() {
    return _asSet;
  }

  public @Nullable String getAdvertiseMap() {
    return _advertiseMap;
  }

  public void setAdvertiseMap(@Nullable String advertiseMap) {
    _advertiseMap = advertiseMap;
  }

  public void setAsSet(boolean asSet) {
    _asSet = asSet;
  }

  public @Nullable String getAttributeMap() {
    return _attributeMap;
  }

  public void setAttributeMap(@Nullable String attributeMap) {
    _attributeMap = attributeMap;
  }

  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }

  public @Nullable String getSuppressMap() {
    return _suppressMap;
  }

  public void setSuppressMap(@Nullable String suppressMap) {
    _suppressMap = suppressMap;
  }

  @Override
  public abstract boolean equals(@Nullable Object o);

  protected final boolean baseEquals(BgpAggregateNetwork that) {
    return Objects.equals(_advertiseMap, that._advertiseMap)
        && _asSet == that._asSet
        && Objects.equals(_attributeMap, that._attributeMap)
        && _summaryOnly == that._summaryOnly
        && Objects.equals(_suppressMap, that._suppressMap);
  }

  protected final int baseHashcode() {
    return Objects.hash(_advertiseMap, _asSet, _attributeMap, _summaryOnly, _suppressMap);
  }

  @Override
  public abstract int hashCode();
}
