package org.batfish.representation.cisco_xr;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/** OSPF distribute-list */
public final class DistributeList implements Serializable {
  /** OSPF distribute-list can be route-policy (in) or ACL (in | out). */
  public enum DistributeListFilterType {
    ACCESS_LIST,
    ROUTE_POLICY,
  }

  @Nonnull private final DistributeListFilterType _filterType;
  @Nonnull private final String _filterName;

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
    } else if (!(o instanceof DistributeList)) {
      return false;
    }
    DistributeList that = (DistributeList) o;
    return _filterType == that._filterType && _filterName.equals(that._filterName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filterType, _filterName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("filterName", _filterName)
        .add("filterType", _filterType)
        .toString();
  }
}
