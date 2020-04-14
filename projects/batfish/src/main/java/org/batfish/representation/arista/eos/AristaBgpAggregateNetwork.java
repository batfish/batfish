package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Aggregate network configuration */
public final class AristaBgpAggregateNetwork implements Serializable {
  @Nullable private Boolean _advertiseOnly;
  @Nullable private Boolean _asSet;
  @Nullable private String _attributeMap;
  @Nullable private String _matchMap;
  @Nullable private Boolean _summaryOnly;

  public AristaBgpAggregateNetwork() {}

  @Nullable
  public Boolean getAdvertiseOnly() {
    return _advertiseOnly;
  }

  public void setAdvertiseOnly(@Nullable Boolean advertiseOnly) {
    _advertiseOnly = advertiseOnly;
  }

  @Nullable
  public Boolean getAsSet() {
    return _asSet;
  }

  public void setAsSet(@Nullable Boolean asSet) {
    _asSet = asSet;
  }

  @Nullable
  public String getAttributeMap() {
    return _attributeMap;
  }

  public void setAttributeMap(@Nullable String attributeMap) {
    _attributeMap = attributeMap;
  }

  @Nullable
  public String getMatchMap() {
    return _matchMap;
  }

  public void setMatchMap(@Nullable String matchMap) {
    _matchMap = matchMap;
  }

  @Nullable
  public Boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(@Nullable Boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }
}
