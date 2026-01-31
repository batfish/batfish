package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public final class OspfArea implements Serializable {

  public static final int DEFAULT_DEFAULT_COST = 1;

  public OspfArea(long id) {
    _id = id;
    _defaultCost = DEFAULT_DEFAULT_COST;
    _ranges = new HashMap<>();
  }

  public @Nullable OspfAreaAuthentication getAuthentication() {
    return _authentication;
  }

  public void setAuthentication(@Nullable OspfAreaAuthentication authentication) {
    _authentication = authentication;
  }

  public int getDefaultCost() {
    return _defaultCost;
  }

  public void setDefaultCost(int defaultCost) {
    _defaultCost = defaultCost;
  }

  public @Nullable String getFilterListIn() {
    return _filterListIn;
  }

  public void setFilterListIn(@Nullable String filterListIn) {
    _filterListIn = filterListIn;
  }

  public @Nullable String getFilterListOut() {
    return _filterListOut;
  }

  public void setFilterListOut(@Nullable String filterListOut) {
    _filterListOut = filterListOut;
  }

  public Map<Prefix, OspfAreaRange> getRanges() {
    return _ranges;
  }

  public @Nullable OspfAreaTypeSettings getTypeSettings() {
    return _typeSettings;
  }

  public void setTypeSettings(@Nullable OspfAreaTypeSettings typeSettings) {
    _typeSettings = typeSettings;
  }

  public long getId() {
    return _id;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable OspfAreaAuthentication _authentication;
  private int _defaultCost;
  private @Nullable String _filterListIn;
  private @Nullable String _filterListOut;
  private final long _id;
  private final @Nonnull Map<Prefix, OspfAreaRange> _ranges;
  private @Nullable OspfAreaTypeSettings _typeSettings;
}
