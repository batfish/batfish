package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;

public final class VrfForwardingBehavior implements Serializable {
  private final @Nonnull Map<Edge, IpSpace> _arpTrueEdge;
  private final @Nonnull Map<String, InterfaceForwardingBehavior> _interfaceForwardingBehavior;
  private final @Nonnull Map<String, IpSpace> _nextVrf;
  private final @Nonnull IpSpace _nullRoutedIps;
  private final @Nonnull IpSpace _routableIps;

  public VrfForwardingBehavior(
      @Nonnull Map<Edge, IpSpace> arpTrueEdge,
      @Nonnull Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior,
      @Nonnull Map<String, IpSpace> nextVrf,
      @Nonnull IpSpace nullRoutedIps,
      @Nonnull IpSpace routableIps) {
    _arpTrueEdge = ImmutableMap.copyOf(arpTrueEdge);
    _interfaceForwardingBehavior = ImmutableMap.copyOf(interfaceForwardingBehavior);
    _nextVrf = ImmutableMap.copyOf(nextVrf);
    _nullRoutedIps = nullRoutedIps;
    _routableIps = routableIps;
  }

  /**
   * For each edge, dst IPs for which the vrf will forward out the source of the edge and receive an
   * ARP reply from the target of the edge.
   */
  public @Nonnull Map<Edge, IpSpace> getArpTrueEdge() {
    return _arpTrueEdge;
  }

  public @Nonnull Map<String, InterfaceForwardingBehavior> getInterfaceForwardingBehavior() {
    return _interfaceForwardingBehavior;
  }

  /** Destination IPs for which this VRF delegates to another VRF. */
  public @Nonnull Map<String, IpSpace> getNextVrfIps() {
    return _nextVrf;
  }

  /**
   * A null-routed IP is a destination IP for which there is a longest-prefix-match route that
   * discards the packet rather than forwarding it out some interface.
   */
  public IpSpace getNullRoutedIps() {
    return _nullRoutedIps;
  }

  /** A routable IP is a destination IP for which there is a longest-prefix-match route. */
  public IpSpace getRoutableIps() {
    return _routableIps;
  }

  public static VrfForwardingBehavior withInterfaceForwardingBehavior(
      Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior) {
    return builder().setInterfaceForwardingBehavior(interfaceForwardingBehavior).build();
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nonnull Map<Edge, IpSpace> _arpTrueEdge = ImmutableMap.of();
    private @Nonnull Map<String, InterfaceForwardingBehavior> _interfaceForwardingBehavior =
        ImmutableMap.of();
    private @Nonnull Map<String, IpSpace> _nextVrf = ImmutableMap.of();
    private @Nonnull IpSpace _nullRoutedIps = EmptyIpSpace.INSTANCE;
    private @Nonnull IpSpace _routableIps = EmptyIpSpace.INSTANCE;

    public Builder setArpTrueEdge(@Nonnull Map<Edge, IpSpace> arpTrueEdge) {
      _arpTrueEdge = arpTrueEdge;
      return this;
    }

    public Builder setInterfaceForwardingBehavior(
        @Nonnull Map<String, InterfaceForwardingBehavior> interfaceForwardingBehavior) {
      _interfaceForwardingBehavior = interfaceForwardingBehavior;
      return this;
    }

    public Builder setNextVrf(@Nonnull Map<String, IpSpace> nextVrf) {
      _nextVrf = nextVrf;
      return this;
    }

    public Builder setNullRoutedIps(@Nonnull IpSpace nullRoutedIps) {
      _nullRoutedIps = nullRoutedIps;
      return this;
    }

    public Builder setRoutableIps(@Nonnull IpSpace routableIps) {
      _routableIps = routableIps;
      return this;
    }

    public VrfForwardingBehavior build() {
      return new VrfForwardingBehavior(
          _arpTrueEdge, _interfaceForwardingBehavior, _nextVrf, _nullRoutedIps, _routableIps);
    }
  }
}
