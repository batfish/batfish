package org.batfish.vendor.rtx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * One gateway of an {@code ip route} command. A command with several {@code gateway} clauses
 * produces one of these per clause.
 */
public final class RtxStaticRoute implements Serializable {

  private final @Nonnull Prefix _network;
  private final @Nullable Ip _nextHopIp;
  private final @Nullable String _nextHopInterface;

  private RtxStaticRoute(Prefix network, @Nullable Ip nextHopIp, @Nullable String nextHopInterface) {
    _network = network;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
  }

  public static RtxStaticRoute ofIp(Prefix network, Ip nextHopIp) {
    return new RtxStaticRoute(network, nextHopIp, null);
  }

  public static RtxStaticRoute ofInterface(Prefix network, String nextHopInterface) {
    return new RtxStaticRoute(network, null, nextHopInterface);
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
}
