package org.batfish.representation.cisco;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class OspfProcess extends ComparableStructure<String> {

  private static final long DEFAULT_DEFAULT_INFORMATION_METRIC = 1;

  private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;

  /** bits per second */
  public static final double DEFAULT_REFERENCE_BANDWIDTH = 1E8;

  private static final long serialVersionUID = 1L;

  private long _defaultInformationMetric;

  private OspfMetricType _defaultInformationMetricType;

  private boolean _defaultInformationOriginate;

  private boolean _defaultInformationOriginateAlways;

  private String _defaultInformationOriginateMap;

  private Integer _defaultInformationOriginateMapLine;

  private Set<String> _interfaceBlacklist;

  private Set<String> _interfaceWhitelist;

  private Set<OspfNetwork> _networks;

  private Map<Integer, Boolean> _nssas;

  private boolean _passiveInterfaceDefault;

  private Map<RoutingProtocol, OspfRedistributionPolicy> _redistributionPolicies;

  private double _referenceBandwidth;

  private Ip _routerId;

  private Map<Long, Map<Prefix, Boolean>> _summaries;

  private Set<OspfWildcardNetwork> _wildcardNetworks;

  public OspfProcess(String name) {
    super(name);
    _referenceBandwidth = DEFAULT_REFERENCE_BANDWIDTH;
    _networks = new TreeSet<>();
    _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
    _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
    _nssas = new HashMap<>();
    _interfaceBlacklist = new HashSet<>();
    _interfaceWhitelist = new HashSet<>();
    _wildcardNetworks = new TreeSet<>();
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
    _summaries = new TreeMap<>();
  }

  public void computeNetworks(Collection<Interface> interfaces) {
    for (Interface i : interfaces) {
      Prefix intPrefix = i.getPrefix();
      if (intPrefix == null) {
        continue;
      }
      for (OspfWildcardNetwork wn : _wildcardNetworks) {
        // first we check if the interface ip address matches the ospf
        // network when the wildcard is ORed to both
        long wildcardLong = wn.getWildcard().asLong();
        long ospfNetworkLong = wn.getNetworkAddress().asLong();
        long intIpLong = intPrefix.getAddress().asLong();
        long wildcardedOspfNetworkLong = ospfNetworkLong | wildcardLong;
        long wildcardedIntIpLong = intIpLong | wildcardLong;
        if (wildcardedOspfNetworkLong == wildcardedIntIpLong) {
          // since we have a match, we add the INTERFACE network, ignoring
          // the wildcard stuff from before
          Prefix newOspfNetwork =
              new Prefix(intPrefix.getNetworkAddress(), intPrefix.getPrefixLength());
          _networks.add(new OspfNetwork(newOspfNetwork, wn.getArea()));
          break;
        }
      }
    }
  }

  public Set<String> getActiveInterfaceList() {
    return _interfaceWhitelist;
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

  public Integer getDefaultInformationOriginateMapLine() {
    return _defaultInformationOriginateMapLine;
  }

  public Set<OspfNetwork> getNetworks() {
    return _networks;
  }

  public Map<Integer, Boolean> getNssas() {
    return _nssas;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public Set<String> getPassiveInterfaceList() {
    return _interfaceBlacklist;
  }

  public Map<RoutingProtocol, OspfRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public Map<Long, Map<Prefix, Boolean>> getSummaries() {
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

  public void setDefaultInformationOriginateMapLine(Integer defaultInformationOriginateMapLine) {
    _defaultInformationOriginateMapLine = defaultInformationOriginateMapLine;
  }

  public void setPassiveInterfaceDefault(boolean b) {
    _passiveInterfaceDefault = b;
  }

  public void setReferenceBandwidth(double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }
}
