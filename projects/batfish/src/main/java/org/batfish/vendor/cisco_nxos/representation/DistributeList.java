package org.batfish.vendor.cisco_nxos.representation;

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

  private final @Nonnull DistributeListFilterType _filterType;
  private final @Nonnull String _filterName;

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
