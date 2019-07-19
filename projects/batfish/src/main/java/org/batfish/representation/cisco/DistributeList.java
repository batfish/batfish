package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Represents Cisco specific distribute-lists */
public final class DistributeList implements Serializable {
  /** Different types of filter used in a distribute-list */
  public enum DistributeListFilterType {
    ACCESS_LIST,
    PREFIX_LIST,
    ROUTE_MAP
  }

  @Nonnull private DistributeListFilterType _filterType;
  @Nonnull private String _filterName;

  public DistributeList(
      @Nonnull String filterName, @Nonnull DistributeListFilterType distributeListFilterType) {
    _filterName = filterName;
    _filterType = distributeListFilterType;
  }

  @Nonnull
  public DistributeListFilterType getFilterType() {
    return _filterType;
  }

  @Nonnull
  public String getFilterName() {
    return _filterName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DistributeList that = (DistributeList) o;
    return _filterType == that._filterType && Objects.equals(_filterName, that._filterName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filterType, _filterName);
  }
}
