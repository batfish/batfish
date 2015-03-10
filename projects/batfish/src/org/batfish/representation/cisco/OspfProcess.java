package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.Ip;
import org.batfish.representation.OspfMetricType;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class OspfProcess implements Serializable {

   private static final int DEFAULT_DEFAULT_INFORMATION_METRIC = 1;

   private static final OspfMetricType DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE = OspfMetricType.E2;

   /**
    * bits per second
    */
   private static final double DEFAULT_REFERENCE_BANDWIDTH = 1E9;

   private static final long serialVersionUID = 1L;

   private int _defaultInformationMetric;
   private OspfMetricType _defaultInformationMetricType;
   private boolean _defaultInformationOriginate;
   private boolean _defaultInformationOriginateAlways;
   private String _defaultInformationOriginateMap;
   private Set<String> _interfaceBlacklist;
   private Set<String> _interfaceWhitelist;
   private Set<OspfNetwork> _networks;
   private Map<Integer, Boolean> _nssas;
   private boolean _passiveInterfaceDefault;
   private int _pid;
   private Map<RoutingProtocol, OspfRedistributionPolicy> _redistributionPolicies;
   private double _referenceBandwidth;
   private Ip _routerId;
   private Set<OspfWildcardNetwork> _wildcardNetworks;

   public OspfProcess(int procnum) {
      _pid = procnum;
      _referenceBandwidth = DEFAULT_REFERENCE_BANDWIDTH;
      _networks = new TreeSet<OspfNetwork>();
      _defaultInformationOriginate = false;
      _defaultInformationOriginateAlways = false;
      _defaultInformationMetric = DEFAULT_DEFAULT_INFORMATION_METRIC;
      _defaultInformationMetricType = DEFAULT_DEFAULT_INFORMATION_METRIC_TYPE;
      _passiveInterfaceDefault = false;
      _nssas = new HashMap<Integer, Boolean>();
      _interfaceBlacklist = new HashSet<String>();
      _interfaceWhitelist = new HashSet<String>();
      _wildcardNetworks = new TreeSet<OspfWildcardNetwork>();
      _redistributionPolicies = new EnumMap<RoutingProtocol, OspfRedistributionPolicy>(
            RoutingProtocol.class);
   }

   public void computeNetworks(Collection<Interface> interfaces) {
      for (Interface i : interfaces) {
         String iname = i.getName();
         if (_interfaceBlacklist.contains(iname)
               || (_passiveInterfaceDefault && !_interfaceWhitelist
                     .contains(iname))) {
            continue;
         }
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
               Prefix newOspfNetwork = new Prefix(
                     intPrefix.getNetworkAddress(), intPrefix.getPrefixLength());
               _networks.add(new OspfNetwork(newOspfNetwork, wn.getArea()));
               break;
            }
         }
      }
   }

   public int getDefaultInformationMetric() {
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

   public Set<String> getInterfaceBlacklist() {
      return _interfaceBlacklist;
   }

   public Set<String> getInterfaceWhitelist() {
      return _interfaceWhitelist;
   }

   public Set<OspfNetwork> getNetworks() {
      return _networks;
   }

   public Map<Integer, Boolean> getNssas() {
      return _nssas;
   }

   public int getPid() {
      return _pid;
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
