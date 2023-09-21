package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Data model class representing BGP configuration. */
public class BgpProcess implements Serializable {

  public long getAsn() {
    return _asn;
  }

  public @Nullable Long getDefaultLocalPreference() {
    return _defaultLocalPreference;
  }

  public void setDefaultLocalPreference(long defaultLocalPreference) {
    _defaultLocalPreference = defaultLocalPreference;
  }

  public @Nonnull Map<BgpNeighborId, BgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  /** Get the {@link BgpNeighbor} given by the specified {@link BgpNeighborId}. */
  public @Nullable BgpNeighbor getNeighbor(BgpNeighborId id) {
    return _neighbors.get(id);
  }

  /**
   * Get the {@link BgpNeighbor} given by the specified {@link BgpNeighborId}, creating a new
   * neighbor if it doesn't already exist.
   */
  public @Nonnull BgpNeighbor getOrCreateNeighbor(BgpNeighborId id) {
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

  public @Nullable Integer getMaximumPaths() {
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

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public BgpProcess(long asn) {
    _asn = asn;
    _neighbors = ImmutableMap.of();
  }

  private final long _asn;
  private @Nullable Long _defaultLocalPreference;
  private @Nonnull Map<BgpNeighborId, BgpNeighbor> _neighbors;
  private @Nullable Integer _maximumPaths;
  private boolean _redistributeConnected;
  private boolean _redistributeFloatingIp;
  private boolean _redistributeIpNat;
  private boolean _redistributeVipOnlyFlagged;
  private boolean _redistributeVipOnlyNotFlagged;
  private @Nullable Ip _routerId;
}
