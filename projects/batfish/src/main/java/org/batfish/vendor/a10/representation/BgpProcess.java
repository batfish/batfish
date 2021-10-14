package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Data model class representing BGP configuration. */
public class BgpProcess implements Serializable {
  @Nullable
  public Long getDefaultLocalPreference() {
    return _defaultLocalPreference;
  }

  public void setDefaultLocalPreference(long defaultLocalPreference) {
    _defaultLocalPreference = defaultLocalPreference;
  }

  @Nonnull
  public Map<BgpNeighborId, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  /**
   * Get the {@link BgpNeighbor} given by the specified {@link BgpNeighborId}, creating a new
   * neighbor if it doesn't already exist.
   */
  @Nullable
  public BgpNeighbor getNeighbor(BgpNeighborId id) {
    return _neighbors.get(id);
  }

  /**
   * Get the {@link BgpNeighbor} given by the specified {@link BgpNeighborId}, creating a new
   * neighbor if it doesn't already exist.
   */
  @Nonnull
  public BgpNeighbor getOrCreateNeighbor(BgpNeighborId id) {
    BgpNeighbor neighbor = _neighbors.get(id);
    if (neighbor == null) {
      neighbor = new BgpNeighbor(id);
      _neighbors =
          ImmutableMap.<BgpNeighborId, BgpNeighbor>builder()
              .putAll(_neighbors)
              .put(neighbor.getId(), neighbor)
              .build();
    }
    return neighbor;
  }

  public long getNumber() {
    return _number;
  }

  @Nullable
  public Integer getMaximumPaths() {
    return _maximumPaths;
  }

  public void setMaximumPaths(Integer maximumPaths) {
    _maximumPaths = maximumPaths;
  }

  public boolean isRedistributeConnected() {
    return _redistributeConnected;
  }

  public void setRedistributeConnected(boolean redistributeConnected) {
    _redistributeConnected = redistributeConnected;
  }

  public boolean isRedistributeFloatingIp() {
    return _redistributeFloatingIp;
  }

  public void setRedistributeFloatingIp(boolean redistributeFloatingIp) {
    _redistributeFloatingIp = redistributeFloatingIp;
  }

  public boolean isRedistributeIpNat() {
    return _redistributeIpNat;
  }

  public void setRedistributeIpNat(boolean redistributeIpNat) {
    _redistributeIpNat = redistributeIpNat;
  }

  public boolean isRedistributeVipOnlyFlagged() {
    return _redistributeVipOnlyFlagged;
  }

  public void setRedistributeVipOnlyFlagged(boolean redistributeVipOnlyFlagged) {
    _redistributeVipOnlyFlagged = redistributeVipOnlyFlagged;
  }

  public boolean isRedistributeVipOnlyNotFlagged() {
    return _redistributeVipOnlyNotFlagged;
  }

  public void setRedistributeVipOnlyNotFlagged(boolean redistributeVipOnlyNotFlagged) {
    _redistributeVipOnlyNotFlagged = redistributeVipOnlyNotFlagged;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public BgpProcess(long number) {
    _number = number;
    _neighbors = ImmutableMap.of();
  }

  @Nullable private Long _defaultLocalPreference;
  @Nonnull private Map<BgpNeighborId, BgpNeighbor> _neighbors;
  private final long _number;
  @Nullable private Integer _maximumPaths;
  private boolean _redistributeConnected;
  private boolean _redistributeFloatingIp;
  private boolean _redistributeIpNat;
  private boolean _redistributeVipOnlyFlagged;
  private boolean _redistributeVipOnlyNotFlagged;
  @Nullable private Ip _routerId;
}
