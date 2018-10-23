package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;

public class MockForwardingAnalysis implements ForwardingAnalysis {

  public static class Builder {

    private Map<String, Map<String, IpSpace>> _arpReplies;

    private Map<
            String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
        _arpRequests;

    private Map<Edge, IpSpace> _arpTrueEdge;

    private Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableOrExitsNetwork;

    private Map<String, Map<String, IpSpace>> _nullRoutedIps;

    private Map<String, Map<String, IpSpace>> _routableIps;

    private Builder() {
      _arpReplies = ImmutableMap.of();
      _arpRequests = ImmutableMap.of();
      _arpTrueEdge = ImmutableMap.of();
      _neighborUnreachableOrExitsNetwork = ImmutableMap.of();
      _nullRoutedIps = ImmutableMap.of();
      _routableIps = ImmutableMap.of();
    }

    public MockForwardingAnalysis build() {
      return new MockForwardingAnalysis(this);
    }

    public Builder setArpReplies(Map<String, Map<String, IpSpace>> arpReplies) {
      _arpReplies = arpReplies;
      return this;
    }

    public Builder setArpRequests(
        Map<String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
            arpRequests) {
      _arpRequests = arpRequests;
      return this;
    }

    public Builder setArpTrueEdge(Map<Edge, IpSpace> arpTrueEdge) {
      _arpTrueEdge = arpTrueEdge;
      return this;
    }

    public Builder setNeighborUnreachableOrExitsNetwork(
        Map<String, Map<String, Map<String, IpSpace>>> neighborUnreachableOrExitsNetwork) {
      _neighborUnreachableOrExitsNetwork = neighborUnreachableOrExitsNetwork;
      return this;
    }

    public Builder setNullRoutedIps(Map<String, Map<String, IpSpace>> nullRoutedIps) {
      _nullRoutedIps = nullRoutedIps;
      return this;
    }

    public Builder setRoutableIps(Map<String, Map<String, IpSpace>> routableIps) {
      _routableIps = routableIps;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<String, Map<String, IpSpace>> _arpReplies;

  private final Map<
          String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      _arpRequests;

  private final Map<Edge, IpSpace> _arpTrueEdge;

  private final Map<String, Map<String, Map<String, IpSpace>>> _neighborUnreachableOrExitsNetwork;

  private final Map<String, Map<String, IpSpace>> _nullRoutedIps;

  private final Map<String, Map<String, IpSpace>> _routableIps;

  public MockForwardingAnalysis(Builder builder) {
    _arpReplies = ImmutableMap.copyOf(builder._arpReplies);
    _arpRequests = ImmutableMap.copyOf(builder._arpRequests);
    _arpTrueEdge = ImmutableMap.copyOf(builder._arpTrueEdge);
    _neighborUnreachableOrExitsNetwork =
        ImmutableMap.copyOf(builder._neighborUnreachableOrExitsNetwork);
    _nullRoutedIps = ImmutableMap.copyOf(builder._nullRoutedIps);
    _routableIps = ImmutableMap.copyOf(builder._routableIps);
  }

  @Override
  public Map<String, Map<String, IpSpace>> getArpReplies() {
    return _arpReplies;
  }

  public Map<String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      getArpRequests() {
    return _arpRequests;
  }

  @Override
  public Map<Edge, IpSpace> getArpTrueEdge() {
    return _arpTrueEdge;
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachableOrExitsNetwork() {
    return _neighborUnreachableOrExitsNetwork;
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable() {
    return ImmutableMap.of();
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getExitsNetwork() {
    return ImmutableMap.of();
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getDeliveredToSubnet() {
    return ImmutableMap.of();
  }

  @Override
  public Map<String, Map<String, Map<String, IpSpace>>> getInsufficientInfo() {
    return ImmutableMap.of();
  }

  @Override
  public Map<String, Map<String, IpSpace>> getNullRoutedIps() {
    return _nullRoutedIps;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getRoutableIps() {
    return _routableIps;
  }
}
