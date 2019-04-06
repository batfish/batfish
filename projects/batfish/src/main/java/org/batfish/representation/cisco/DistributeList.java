package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DistributeList implements Serializable {
  public enum DistributeListFilterType {
    ACCESS_LIST,
    PREFIX_LIST,
    ROUTE_MAP
  }

  private static final long serialVersionUID = 1L;

  @Nonnull private DistributeListFilterType _DistributeList_filterType;

  @Nonnull private String _filterName;

  public DistributeList(
      @Nonnull String filterName, @Nonnull DistributeListFilterType distributeListFilterType) {
    _filterName = filterName;
    _DistributeList_filterType = distributeListFilterType;
  }

  @Nonnull
  public DistributeListFilterType getFilterType() {
    return _DistributeList_filterType;
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
    return _DistributeList_filterType == that._DistributeList_filterType
        && Objects.equals(_filterName, that._filterName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_DistributeList_filterType, _filterName);
  }
}
