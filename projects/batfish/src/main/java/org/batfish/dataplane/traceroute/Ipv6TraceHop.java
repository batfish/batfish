package org.batfish.dataplane.traceroute;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip6;

/** One hop in an IPv6 forwarding trace. */
@ParametersAreNonnullByDefault
public final class Ipv6TraceHop implements Serializable {

  public static Ipv6TraceHop forwarding(
      String node,
      String vrf,
      String outgoingInterface,
      @Nullable Ip6 ndTarget) {
    return new Ipv6TraceHop(
        node,
        vrf,
        outgoingInterface,
        ndTarget);
  }

  public static Ipv6TraceHop terminal(
      String node,
      String vrf) {
    return new Ipv6TraceHop(
        node,
        vrf,
        null,
        null);
  }

  private Ipv6TraceHop(
      String node,
      String vrf,
      @Nullable String outgoingInterface,
      @Nullable Ip6 ndTarget) {
    _node = node;
    _vrf = vrf;
    _outgoingInterface = outgoingInterface;
    _ndTarget = ndTarget;
  }

  public @Nonnull String getNode() {
    return _node;
  }

  public @Nonnull String getVrf() {
    return _vrf;
  }

  public @Nonnull Optional<String>
      getOutgoingInterface() {
    return Optional.ofNullable(
        _outgoingInterface);
  }

  /**
   * IPv6 address for which Neighbor Discovery is performed on this hop.
   *
   * <p>For a routed next hop this is the neighbor address. For an on-link
   * destination this is the destination itself. An empty value represents a
   * point-to-point neighbor whose generated link-local address is not
   * explicitly modeled.
   */
  public @Nonnull Optional<Ip6> getNdTarget() {
    return Optional.ofNullable(_ndTarget);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ipv6TraceHop)) {
      return false;
    }

    Ipv6TraceHop rhs =
        (Ipv6TraceHop) o;

    return _node.equals(rhs._node)
        && _vrf.equals(rhs._vrf)
        && Objects.equals(
            _outgoingInterface,
            rhs._outgoingInterface)
        && Objects.equals(
            _ndTarget,
            rhs._ndTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _node,
        _vrf,
        _outgoingInterface,
        _ndTarget);
  }

  @Override
  public String toString() {
    return "Ipv6TraceHop<"
        + _node
        + ",vrf:"
        + _vrf
        + ",iface:"
        + _outgoingInterface
        + ",nd:"
        + _ndTarget
        + ">";
  }

  private final @Nullable Ip6 _ndTarget;
  private final @Nonnull String _node;
  private final @Nullable String _outgoingInterface;
  private final @Nonnull String _vrf;
}
