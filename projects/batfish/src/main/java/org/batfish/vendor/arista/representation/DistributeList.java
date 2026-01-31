package org.batfish.vendor.arista.representation;

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

  private @Nonnull DistributeListFilterType _filterType;
  private @Nonnull String _filterName;

  public DistributeList(
      @Nonnull String filterName, @Nonnull DistributeListFilterType distributeListFilterType) {
    _filterName = filterName;
    _filterType = distributeListFilterType;
  }

  public @Nonnull DistributeListFilterType getFilterType() {
    return _filterType;
  }

  public @Nonnull String getFilterName() {
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
