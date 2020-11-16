package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents NXOS distribute-lists */
public final class DistributeList implements Serializable {
  /** Distribute-lists can use prefix-lists or route-maps */
  public enum DistributeListFilterType {
    PREFIX_LIST,
    ROUTE_MAP
  }

  @Nonnull private final DistributeListFilterType _filterType;
  @Nonnull private final String _filterName;

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
  public boolean equals(@Nullable Object o) {
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
