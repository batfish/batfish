package org.batfish.question;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpsecVpn;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;

public class Environment {

   private int[] _assertionCount;

   private boolean[] _assertions;

   private BgpNeighbor _bgpNeighbor;

   private PolicyMapClause _clause;

   private Map<String, Configuration> _configurations;

   private int[] _failedAssertionCount;

   private GeneratedRoute _generatedRoute;

   private Map<String, Integer> _integers;

   private Map<String, Set<Integer>> _integerSets;

   private Interface _interface;

   private IpsecVpn _ipsecVpn;

   private Map<String, Set<Ip>> _ipSets;

   private PolicyMapMatchProtocolLine _matchProtocolLine;

   private PolicyMapMatchRouteFilterListLine _matchRouteFilterLine;

   private Configuration _node;

   private PolicyMap _policyMap;

   private Map<String, Set<Prefix>> _prefixSets;

   private RoutingProtocol _protocol;

   private Set<RoutingProtocol> _protocols;

   private boolean[] _remoteIpsecVpnsInitialized;

   private RouteFilterList _routeFilter;

   private RouteFilterLine _routeFilterLine;

   private Set<RouteFilterList> _routeFilterSet;

   private Map<String, Set<RouteFilterList>> _routeFilterSets;

   private StaticRoute _staticRoute;

   private Map<String, Set<String>> _stringSets;

   private boolean[] _unsafe;

   public Environment() {
      _assertionCount = new int[1];
      _assertions = new boolean[1];
      _failedAssertionCount = new int[1];
      _integers = new HashMap<String, Integer>();
      _integerSets = new HashMap<String, Set<Integer>>();
      _ipSets = new HashMap<String, Set<Ip>>();
      _prefixSets = new HashMap<String, Set<Prefix>>();
      _remoteIpsecVpnsInitialized = new boolean[1];
      _routeFilterSets = new HashMap<String, Set<RouteFilterList>>();
      _stringSets = new HashMap<String, Set<String>>();
      _unsafe = new boolean[1];
   }

   public Environment copy() {
      Environment copy = new Environment();
      copy._assertionCount = _assertionCount;
      copy._assertions = _assertions;
      copy._clause = _clause;
      copy._configurations = _configurations;
      copy._failedAssertionCount = _failedAssertionCount;
      copy._generatedRoute = _generatedRoute;
      copy._matchProtocolLine = _matchProtocolLine;
      copy._matchRouteFilterLine = _matchRouteFilterLine;
      copy._node = _node;
      copy._integers = _integers;
      copy._integerSets = _integerSets;
      copy._interface = _interface;
      copy._ipSets = _ipSets;
      copy._policyMap = _policyMap;
      copy._prefixSets = _prefixSets;
      copy._protocol = _protocol;
      copy._protocols = _protocols;
      copy._remoteIpsecVpnsInitialized = _remoteIpsecVpnsInitialized;
      copy._routeFilter = _routeFilter;
      copy._routeFilterLine = _routeFilterLine;
      copy._routeFilterSet = _routeFilterSet;
      copy._routeFilterSets = _routeFilterSets;
      copy._staticRoute = _staticRoute;
      copy._stringSets = _stringSets;
      copy._unsafe = _unsafe;
      return copy;
   }

   public boolean getAssertions() {
      return _assertions[0];
   }

   public BgpNeighbor getBgpNeighbor() {
      return _bgpNeighbor;
   }

   public PolicyMapClause getClause() {
      return _clause;
   }

   public int getFailedAssertions() {
      return _failedAssertionCount[0];
   }

   public GeneratedRoute getGeneratedRoute() {
      return _generatedRoute;
   }

   public Map<String, Integer> getIntegers() {
      return _integers;
   }

   public Map<String, Set<Integer>> getIntegerSets() {
      return _integerSets;
   }

   public Interface getInterface() {
      return _interface;
   }

   public IpsecVpn getIpsecVpn() {
      return _ipsecVpn;
   }

   public Map<String, Set<Ip>> getIpSets() {
      return _ipSets;
   }

   public PolicyMapMatchProtocolLine getMatchProtocolLine() {
      return _matchProtocolLine;
   }

   public Configuration getNode() {
      return _node;
   }

   public Set<Configuration> getNodes() {
      Set<Configuration> nodes = new TreeSet<Configuration>();
      nodes.addAll(_configurations.values());
      return nodes;
   }

   public PolicyMap getPolicyMap() {
      return _policyMap;
   }

   public Map<String, Set<Prefix>> getPrefixSets() {
      return _prefixSets;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public Set<RoutingProtocol> getProtocols() {
      return _protocols;
   }

   public RouteFilterList getRouteFilter() {
      return _routeFilter;
   }

   public RouteFilterLine getRouteFilterLine() {
      return _routeFilterLine;
   }

   public Set<RouteFilterList> getRouteFilterSet() {
      return _routeFilterSet;
   }

   public Map<String, Set<RouteFilterList>> getRouteFilterSets() {
      return _routeFilterSets;
   }

   public StaticRoute getStaticRoute() {
      return _staticRoute;
   }

   public Map<String, Set<String>> getStringSets() {
      return _stringSets;
   }

   public int getTotalAssertions() {
      return _assertionCount[0];
   }

   public boolean getUnsafe() {
      return _unsafe[0];
   }

   public void incrementAssertionCount() {
      _assertionCount[0]++;
   }

   public void incrementFailedAssertionCount() {
      _failedAssertionCount[0]++;
   }

   public void initRemoteIpsecVpns() {
      if (_remoteIpsecVpnsInitialized[0]) {
         return;
      }
      Map<IpsecVpn, Ip> remoteAddresses = new HashMap<IpsecVpn, Ip>();
      Map<Ip, IpsecVpn> externalAddresses = new HashMap<Ip, IpsecVpn>();
      for (Configuration c : _configurations.values()) {
         for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
            Ip remoteAddress = ipsecVpn.getGateway().getAddress();
            remoteAddresses.put(ipsecVpn, remoteAddress);
            Set<Prefix> externalPrefixes =  ipsecVpn.getGateway()
                  .getExternalInterface().getAllPrefixes();
            for (Prefix externalPrefix : externalPrefixes) {
               Ip externalAddress = externalPrefix.getAddress();
               externalAddresses.put(externalAddress, ipsecVpn);
            }
         }
//            Prefix externalPrefix = ipsecVpn.getGateway()
//                  .getExternalInterface().getPrefix();
//            if (externalPrefix != null) {
//               Ip externalAddress = externalPrefix.getAddress();
//               externalAddresses.put(externalAddress, ipsecVpn);
//            }

      }
      for (Entry<IpsecVpn, Ip> e : remoteAddresses.entrySet()) {
         IpsecVpn ipsecVpn = e.getKey();
         Ip remoteAddress = e.getValue();
         IpsecVpn remoteIpsecVpn = externalAddresses.get(remoteAddress);
         ipsecVpn.setRemoteIpsecVpn(remoteIpsecVpn);
      }
      _remoteIpsecVpnsInitialized[0] = true;
   }

   public void setAssertions(boolean b) {
      _assertions[0] = b;
   }

   public void setBgpNeighbor(BgpNeighbor bgpNeighbor) {
      _bgpNeighbor = bgpNeighbor;
   }

   public void setClause(PolicyMapClause clause) {
      _clause = clause;
   }

   public void setConfigurations(Map<String, Configuration> configurations) {
      _configurations = configurations;
   }

   public void setGeneratedRoute(GeneratedRoute generatedRoute) {
      _generatedRoute = generatedRoute;
   }

   public void setInterface(Interface iface) {
      _interface = iface;
   }

   public void setIpsecVpn(IpsecVpn ipsecVpn) {
      _ipsecVpn = ipsecVpn;
   }

   public void setMatchProtocolLine(PolicyMapMatchProtocolLine matchProtocolLine) {
      _matchProtocolLine = matchProtocolLine;
   }

   public void setMatchRouteFilterLine(
         PolicyMapMatchRouteFilterListLine matchRouteFilterLine) {
      _matchRouteFilterLine = matchRouteFilterLine;
   }

   public void setNode(Configuration node) {
      _node = node;
   }

   public void setPolicyMap(PolicyMap policyMap) {
      _policyMap = policyMap;
   }

   public void setProtocolSet(Set<RoutingProtocol> protocols) {
      _protocols = protocols;
   }

   public void setRouteFilter(RouteFilterList routeFilter) {
      _routeFilter = routeFilter;
   }

   public void setRouteFilterLine(RouteFilterLine routeFilterLine) {
      _routeFilterLine = routeFilterLine;
   }

   public void setRouteFilterSet(Set<RouteFilterList> routeFilterSet) {
      _routeFilterSet = routeFilterSet;
   }

   public void setRoutingProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public void setStaticRoute(StaticRoute staticRoute) {
      _staticRoute = staticRoute;
   }

   public void setUnsafe(boolean b) {
      _unsafe[0] = b;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      if (_node != null) {
         sb.append("node:" + _node + " ");
      }
      if (_interface != null) {
         sb.append("interface:" + _interface + " ");
      }
      if (_bgpNeighbor != null) {
         sb.append("bgp_neighbor:" + _bgpNeighbor + " ");
      }
      sb.append("}");
      return sb.toString();
   }

}
