package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BgpProcessDiff {

  private static final String PROP_IN_AFTER_ONLY = "inAfterOnly";

  private static final String PROP_IN_BEFORE_ONLY = "inBeforeOnly";

  private Boolean _inAfterOnly;

  private Boolean _inBeforeOnly;

  private NeighborsDiff _neighborsDiff;

  private NeighborsDiff _dynamicNeighborsDiff;

  @JsonCreator
  public BgpProcessDiff() {}

  public BgpProcessDiff(BgpProcess before, BgpProcess after) {
    if (before != null && after == null) {
      _inBeforeOnly = true;
    } else if (before == null && after != null) {
      _inAfterOnly = true;
    } else if (before != null && after != null) {
      _neighborsDiff = new NeighborsDiff(before.getActiveNeighbors(), after.getActiveNeighbors());
      if (_neighborsDiff.isEmpty()) {
        _neighborsDiff = null;
      }
      _dynamicNeighborsDiff =
          new NeighborsDiff(before.getPassiveNeighbors(), after.getPassiveNeighbors());
      if (_dynamicNeighborsDiff.isEmpty()) {
        _dynamicNeighborsDiff = null;
      }
    }
  }

  @JsonProperty(PROP_IN_AFTER_ONLY)
  public Boolean getInAfterOnly() {
    return _inAfterOnly;
  }

  @JsonProperty(PROP_IN_BEFORE_ONLY)
  public Boolean getInBeforeOnly() {
    return _inBeforeOnly;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _dynamicNeighborsDiff == null
        && _neighborsDiff == null
        && _inBeforeOnly == null
        && _inAfterOnly == null;
  }

  @JsonProperty(PROP_IN_AFTER_ONLY)
  public void setInAfterOnly(Boolean inAfterOnly) {
    _inAfterOnly = inAfterOnly;
  }

  @JsonProperty(PROP_IN_BEFORE_ONLY)
  public void setInBeforeOnly(Boolean inBeforeOnly) {
    _inBeforeOnly = inBeforeOnly;
  }
}
