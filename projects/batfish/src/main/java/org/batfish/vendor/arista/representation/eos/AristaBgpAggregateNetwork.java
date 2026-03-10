package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Aggregate network configuration */
public final class AristaBgpAggregateNetwork implements Serializable {
  private @Nullable Boolean _advertiseOnly;
  private @Nullable Boolean _asSet;
  private @Nullable String _attributeMap;
  private @Nullable String _matchMap;
  private @Nullable Boolean _summaryOnly;

  public AristaBgpAggregateNetwork() {}

  public @Nullable Boolean getAdvertiseOnly() {
    return _advertiseOnly;
  }

  public void setAdvertiseOnly(@Nullable Boolean advertiseOnly) {
    _advertiseOnly = advertiseOnly;
  }

  public @Nullable Boolean getAsSet() {
    return _asSet;
  }

  public void setAsSet(@Nullable Boolean asSet) {
    _asSet = asSet;
  }

  public @Nullable String getAttributeMap() {
    return _attributeMap;
  }

  public void setAttributeMap(@Nullable String attributeMap) {
    _attributeMap = attributeMap;
  }

  public @Nullable String getMatchMap() {
    return _matchMap;
  }

  public void setMatchMap(@Nullable String matchMap) {
    _matchMap = matchMap;
  }

  public @Nullable Boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public boolean getSummaryOnlyEffective() {
    return _summaryOnly != null ? _summaryOnly : false;
  }

  public void setSummaryOnly(@Nullable Boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
