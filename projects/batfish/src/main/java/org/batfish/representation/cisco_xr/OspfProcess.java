package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfMetricType;

public class OspfProcess implements Serializable {

  private static final long DEFAULT_DEFAULT_INFORMATION_METRIC = 1L;

  private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;

  // Although not clearly documented; from GNS3 emulation and CiscoXr forum
  // (https://community.cisco_xr.com/t5/switching/ospf-cost-calculation/td-p/2917356)
  public static final int DEFAULT_LOOPBACK_OSPF_COST = 1;

  public static final long DEFAULT_MAX_METRIC_EXTERNAL_LSA = 0xFF0000L;

  public static final long DEFAULT_MAX_METRIC_SUMMARY_LSA = 0xFF0000L;

  // https://www.cisco.com/c/en/us/td/docs/ios_xr_sw/iosxr_r3-7/routing/command/reference/rr37ospf.html
  public static final double DEFAULT_OSPF_REFERENCE_BANDWIDTH = 100E6D;

  public static final long MAX_METRIC_ROUTER_LSA = 0xFFFFL;

  private long _defaultInformationMetric;

  private OspfMetricType _defaultInformationMetricType;

  private boolean _defaultInformationOriginate;

  private boolean _defaultInformationOriginateAlways;

  private String _defaultInformationOriginateMap;

  private Long _defaultMetric;

  @Nullable private OspfNetworkType _defaultNetworkType;

  @Nullable private DistributeList _inboundGlobalDistributeList;

  private Long _maxMetricExternalLsa;

  private boolean _maxMetricIncludeStub;

  private boolean _maxMetricRouterLsa;

  private Long _maxMetricSummaryLsa;

  private final String _name;

  private Set<OspfNetwork> _networks;

  private Set<String> _nonDefaultInterfaces;

  private Map<Long, NssaSettings> _nssas;

  @Nullable private DistributeList _outboundGlobalDistributeList;

  private Map<Long, StubSettings> _stubs;

  private boolean _passiveInterfaceDefault;

  private Set<String> _passiveInterfaces;

  private Map<RoutingProtocol, OspfRedistributionPolicy> _redistributionPolicies;

  private double _referenceBandwidth;

  private @Nullable Boolean _rfc1583Compatible;

  private Ip _routerId;

  private Map<Long, Map<Prefix, OspfAreaSummary>> _summaries;

  private Set<OspfWildcardNetwork> _wildcardNetworks;

  public long getDefaultMetric(RoutingProtocol protocol) {
    if (_defaultMetric != null) {
      return _defaultMetric;
    }

    // https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/7039-1.html
    // "the cost allocated to the external route is 20 (the default is 1 for BGP)."
    if (protocol == RoutingProtocol.BGP) {
      return 1;
    }
    return 20;
  }

  public OspfProcess(String name) {
    _name = name;
    _referenceBandwidth = DEFAULT_OSPF_REFERENCE_BANDWIDTH;
    _networks = new TreeSet<>();
    _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
    _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
    _nonDefaultInterfaces = new HashSet<>();
    _nssas = new HashMap<>();
    _passiveInterfaces = new HashSet<>();
    _stubs = new HashMap<>();
    _wildcardNetworks = new TreeSet<>();
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
    _summaries = new TreeMap<>();
  }

  public void computeNetworks(Collection<Interface> interfaces) {
    for (Interface i : interfaces) {
      ConcreteInterfaceAddress address = i.getAddress();
      if (address == null) {
        continue;
      }
      for (OspfWildcardNetwork wn : _wildcardNetworks) {
        // first we check if the interface ip address matches the ospf
        // network when the wildcard is ORed to both
        long wildcardLong = wn.getWildcard().asLong();
        long ospfNetworkLong = wn.getNetworkAddress().asLong();
        long intIpLong = address.getIp().asLong();
        long wildcardedOspfNetworkLong = ospfNetworkLong | wildcardLong;
        long wildcardedIntIpLong = intIpLong | wildcardLong;
        if (wildcardedOspfNetworkLong == wildcardedIntIpLong) {
          // since we have a match, we add the INTERFACE network, ignoring
          // the wildcard stuff from before
          Prefix newOspfNetwork = address.getPrefix();
          _networks.add(new OspfNetwork(newOspfNetwork, wn.getArea()));
          break;
        }
      }
    }
  }

  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  public OspfMetricType getDefaultInformationMetricType() {
    return _defaultInformationMetricType;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public boolean getDefaultInformationOriginateAlways() {
    return _defaultInformationOriginateAlways;
  }

  public String getDefaultInformationOriginateMap() {
    return _defaultInformationOriginateMap;
  }

  public Long getDefaultMetric() {
    return _defaultMetric;
  }

  @Nullable
  public OspfNetworkType getDefaultNetworkType() {
    return _defaultNetworkType;
  }

  @Nullable
  public DistributeList getInboundGlobalDistributeList() {
    return _inboundGlobalDistributeList;
  }

  public Long getMaxMetricExternalLsa() {
    return _maxMetricExternalLsa;
  }

  public boolean getMaxMetricIncludeStub() {
    return _maxMetricIncludeStub;
  }

  public boolean getMaxMetricRouterLsa() {
    return _maxMetricRouterLsa;
  }

  public Long getMaxMetricSummaryLsa() {
    return _maxMetricSummaryLsa;
  }

  public String getName() {
    return _name;
  }

  public Set<OspfNetwork> getNetworks() {
    return _networks;
  }

  public Set<String> getNonDefaultInterfaces() {
    return _nonDefaultInterfaces;
  }

  public Map<Long, NssaSettings> getNssas() {
    return _nssas;
  }

  @Nullable
  public DistributeList getOutboundGlobalDistributeList() {
    return _outboundGlobalDistributeList;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public Set<String> getPassiveInterfaces() {
    return _passiveInterfaces;
  }

  public Map<RoutingProtocol, OspfRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public @Nullable Boolean getRfc1583Compatible() {
    return _rfc1583Compatible;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public Map<Long, StubSettings> getStubs() {
    return _stubs;
  }

  public Map<Long, Map<Prefix, OspfAreaSummary>> getSummaries() {
    return _summaries;
  }

  public Set<OspfWildcardNetwork> getWildcardNetworks() {
    return _wildcardNetworks;
  }

  public void setDefaultInformationMetric(int metric) {
    _defaultInformationMetric = metric;
  }

  public void setDefaultInformationMetricType(OspfMetricType metricType) {
    _defaultInformationMetricType = metricType;
  }

  public void setDefaultInformationOriginate(boolean b) {
    _defaultInformationOriginate = b;
  }

  public void setDefaultInformationOriginateAlways(boolean b) {
    _defaultInformationOriginateAlways = b;
  }

  public void setDefaultInformationOriginateMap(String name) {
    _defaultInformationOriginateMap = name;
  }

  public void setDefaultMetric(Long metric) {
    _defaultMetric = metric;
  }

  public void setDefaultNetworkType(@Nullable OspfNetworkType networkType) {
    _defaultNetworkType = networkType;
  }

  public void setInboundGlobalDistributeList(@Nullable DistributeList inboundGlobalDistributeList) {
    _inboundGlobalDistributeList = inboundGlobalDistributeList;
  }

  public void setMaxMetricExternalLsa(Long maxMetricExternalLsa) {
    _maxMetricExternalLsa = maxMetricExternalLsa;
  }

  public void setMaxMetricIncludeStub(boolean maxMetricIncludeStub) {
    _maxMetricIncludeStub = maxMetricIncludeStub;
  }

  public void setMaxMetricRouterLsa(boolean maxMetricRouterLsa) {
    _maxMetricRouterLsa = maxMetricRouterLsa;
  }

  public void setMaxMetricSummaryLsa(Long maxMetricSummaryLsa) {
    _maxMetricSummaryLsa = maxMetricSummaryLsa;
  }

  public void setOutboundGlobalDistributeList(
      @Nullable DistributeList outboundGlobalDistributeList) {
    _outboundGlobalDistributeList = outboundGlobalDistributeList;
  }

  public void setPassiveInterfaceDefault(boolean b) {
    _passiveInterfaceDefault = b;
  }

  public void setReferenceBandwidth(double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setRfc1583Compatible(@Nullable Boolean rfc1583Compatible) {
    _rfc1583Compatible = rfc1583Compatible;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }
}
