package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** An {@code ip route} command. */
public final class NecIxStaticRoute implements Serializable {

  /** Default {@code distance}. */
  public static final int DEFAULT_DISTANCE = 1;

  /** Default {@code metric}. */
  public static final long DEFAULT_METRIC = 1L;

  private final @Nonnull String _vrf;
  private final @Nonnull Prefix _network;
  private final @Nullable Ip _nextHopIp;
  private final @Nullable String _nextHopInterface;
  private int _distance;
  private long _metric;
  private long _tag;

  public NecIxStaticRoute(
      String vrf, Prefix network, @Nullable Ip nextHopIp, @Nullable String nextHopInterface) {
    _vrf = vrf;
    _network = network;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
    _distance = DEFAULT_DISTANCE;
    _metric = DEFAULT_METRIC;
  }

  public @Nonnull String getVrf() {
    return _vrf;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public int getDistance() {
    return _distance;
  }

  public void setDistance(int distance) {
    _distance = distance;
  }

  public long getMetric() {
    return _metric;
  }

  public void setMetric(long metric) {
    _metric = metric;
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(long tag) {
    _tag = tag;
  }
}
