package org.batfish.question;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.protocoldependency.DependencyDatabase;
import org.batfish.protocoldependency.DependentRoute;
import org.batfish.protocoldependency.PotentialExport;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
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
import org.batfish.representation.PrefixSpace;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;

public class Environment {

   private int[] _assertionCount;

   private boolean[] _assertions;

   private BgpNeighbor _bgpNeighbor;

   private Map<String, BgpNeighbor> _bgpNeighbors;

   private boolean[] _bgpOriginationSpaceInitialized;

   private Map<String, Boolean> _booleans;

   private PolicyMapClause _clause;

   private Map<String, Configuration> _configurations;

   private int[] _failedAssertionCount;

   private GeneratedRoute _generatedRoute;

   private Map<String, Integer> _integers;

   private Map<String, Set<Integer>> _integerSets;

   private Interface _interface;

   private Map<String, Interface> _interfaces;

   private Map<String, Ip> _ips;

   private IpsecVpn _ipsecVpn;

   private Map<String, IpsecVpn> _ipsecVpns;

   private Map<String, Set<Ip>> _ipSets;

   private PolicyMapMatchProtocolLine _matchProtocolLine;

   private PolicyMapMatchRouteFilterListLine _matchRouteFilterLine;

   private Configuration _node;

   private Map<String, Configuration> _nodes;

   private PolicyMap _policyMap;

   private Map<String, PolicyMapClause> _policyMapClauses;

   private Map<String, PolicyMap> _policyMaps;

   private Map<String, Prefix> _prefixes;

   private Map<String, Set<Prefix>> _prefixSets;

   private Map<String, PrefixSpace> _prefixSpaces;

   private RoutingProtocol _protocol;

   private ProtocolDependencyAnalysis[] _protocolDependencyAnalysis;

   private Set<RoutingProtocol> _protocols;

   private IpsecVpn _remoteIpsecVpn;

   private boolean[] _remoteIpsecVpnsInitialized;

   private RouteFilterList _routeFilter;

   private RouteFilterLine _routeFilterLine;

   private Map<String, RouteFilterLine> _routeFilterLines;

   private Map<String, RouteFilterList> _routeFilters;

   private Set<RouteFilterList> _routeFilterSet;

   private Map<String, Set<RouteFilterList>> _routeFilterSets;

   private StaticRoute _staticRoute;

   private Map<String, StaticRoute> _staticRoutes;

   private Map<String, Set<String>> _stringSets;

   private boolean[] _unsafe;

   public Environment() {
      _assertionCount = new int[1];
      _assertions = new boolean[1];
      _bgpNeighbors = new HashMap<String, BgpNeighbor>();
      _bgpOriginationSpaceInitialized = new boolean[1];
      _booleans = new HashMap<String, Boolean>();
      _failedAssertionCount = new int[1];
      _integers = new HashMap<String, Integer>();
      _integerSets = new HashMap<String, Set<Integer>>();
      _interfaces = new HashMap<String, Interface>();
      _ips = new HashMap<String, Ip>();
      _ipsecVpns = new HashMap<String, IpsecVpn>();
      _ipSets = new HashMap<String, Set<Ip>>();
      _nodes = new HashMap<String, Configuration>();
      _policyMaps = new HashMap<String, PolicyMap>();
      _policyMapClauses = new HashMap<String, PolicyMapClause>();
      _prefixes = new HashMap<String, Prefix>();
      _prefixSets = new HashMap<String, Set<Prefix>>();
      _prefixSpaces = new HashMap<String, PrefixSpace>();
      _protocolDependencyAnalysis = new ProtocolDependencyAnalysis[1];
      _remoteIpsecVpnsInitialized = new boolean[1];
      _routeFilters = new HashMap<String, RouteFilterList>();
      _routeFilterLines = new HashMap<String, RouteFilterLine>();
      _routeFilterSets = new HashMap<String, Set<RouteFilterList>>();
      _staticRoutes = new HashMap<String, StaticRoute>();
      _stringSets = new HashMap<String, Set<String>>();
      _unsafe = new boolean[1];
   }

   public void applyParameters(QuestionParameters parameters) {
      for (Entry<String, Object> e : parameters.getStore().entrySet()) {
         String key = e.getKey();
         String var = "$" + key;
         Object value = e.getValue();
         VariableType type = parameters.getTypeBindings().get(key);
         switch (type) {
         case BOOLEAN:
            _booleans.put(var, (Boolean) value);
            break;

         case INT:
            _integers.put(var, (Integer) value);
            break;

         case ACTION:
         case BGP_NEIGHBOR:
         case INTERFACE:
         case IP:
         case IPSEC_VPN:
         case NODE:
         case POLICY_MAP:
         case POLICY_MAP_CLAUSE:
         case PREFIX:
         case PREFIX_SPACE:
         case RANGE:
         case REGEX:
         case ROUTE_FILTER:
         case ROUTE_FILTER_LINE:
         case SET_INT:
         case SET_IP:
         case SET_PREFIX:
         case SET_ROUTE_FILTER:
         case SET_STRING:
         case STATIC_ROUTE:
         case STRING:
         default:
            throw new BatfishException("Unsupported variable type: "
                  + type.toString());

         }
      }
   }

   public Environment copy() {
      Environment copy = new Environment();
      copy._assertionCount = _assertionCount;
      copy._assertions = _assertions;
      copy._bgpNeighbors = _bgpNeighbors;
      copy._bgpOriginationSpaceInitialized = _bgpOriginationSpaceInitialized;
      copy._booleans = _booleans;
      copy._clause = _clause;
      copy._configurations = _configurations;
      copy._failedAssertionCount = _failedAssertionCount;
      copy._generatedRoute = _generatedRoute;
      copy._matchProtocolLine = _matchProtocolLine;
      copy._matchRouteFilterLine = _matchRouteFilterLine;
      copy._node = _node;
      copy._nodes = _nodes;
      copy._integers = _integers;
      copy._integerSets = _integerSets;
      copy._interface = _interface;
      copy._interfaces = _interfaces;
      copy._ips = _ips;
      copy._ipsecVpn = _ipsecVpn;
      copy._ipsecVpns = _ipsecVpns;
      copy._ipSets = _ipSets;
      copy._policyMap = _policyMap;
      copy._policyMaps = _policyMaps;
      copy._policyMapClauses = _policyMapClauses;
      copy._prefixes = _prefixes;
      copy._prefixSets = _prefixSets;
      copy._prefixSpaces = _prefixSpaces;
      copy._protocol = _protocol;
      copy._protocolDependencyAnalysis = _protocolDependencyAnalysis;
      copy._protocols = _protocols;
      copy._remoteIpsecVpn = _remoteIpsecVpn;
      copy._remoteIpsecVpnsInitialized = _remoteIpsecVpnsInitialized;
      copy._routeFilter = _routeFilter;
      copy._routeFilters = _routeFilters;
      copy._routeFilterLine = _routeFilterLine;
      copy._routeFilterLines = _routeFilterLines;
      copy._routeFilterSet = _routeFilterSet;
      copy._routeFilterSets = _routeFilterSets;
      copy._staticRoute = _staticRoute;
      copy._staticRoutes = _staticRoutes;
      copy._stringSets = _stringSets;
      copy._unsafe = _unsafe;
      return copy;
   }

   public Set<Configuration> getAllNodes() {
      Set<Configuration> nodes = new TreeSet<Configuration>();
      nodes.addAll(_configurations.values());
      return nodes;
   }

   public boolean getAssertions() {
      return _assertions[0];
   }

   public BgpNeighbor getBgpNeighbor() {
      return _bgpNeighbor;
   }

   public Map<String, BgpNeighbor> getBgpNeighbors() {
      return _bgpNeighbors;
   }

   public Map<String, Boolean> getBooleans() {
      return _booleans;
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

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Map<String, Ip> getIps() {
      return _ips;
   }

   public IpsecVpn getIpsecVpn() {
      return _ipsecVpn;
   }

   public Map<String, IpsecVpn> getIpsecVpns() {
      return _ipsecVpns;
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

   public Map<String, Configuration> getNodes() {
      return _nodes;
   }

   public PolicyMap getPolicyMap() {
      return _policyMap;
   }

   public Map<String, PolicyMapClause> getPolicyMapClauses() {
      return _policyMapClauses;
   }

   public Map<String, PolicyMap> getPolicyMaps() {
      return _policyMaps;
   }

   public Map<String, Prefix> getPrefixes() {
      return _prefixes;
   }

   public Map<String, Set<Prefix>> getPrefixSets() {
      return _prefixSets;
   }

   public Map<String, PrefixSpace> getPrefixSpaces() {
      return _prefixSpaces;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public Set<RoutingProtocol> getProtocols() {
      return _protocols;
   }

   public IpsecVpn getRemoteIpsecVpn() {
      return _remoteIpsecVpn;
   }

   public RouteFilterList getRouteFilter() {
      return _routeFilter;
   }

   public RouteFilterLine getRouteFilterLine() {
      return _routeFilterLine;
   }

   public Map<String, RouteFilterLine> getRouteFilterLines() {
      return _routeFilterLines;
   }

   public Map<String, RouteFilterList> getRouteFilters() {
      return _routeFilters;
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

   public Map<String, StaticRoute> getStaticRoutes() {
      return _staticRoutes;
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

   public void initBgpOriginationSpaceExplicit() {
      if (_bgpOriginationSpaceInitialized[0]) {
         return;
      }
      initProtocolDependencyAnalysis();
      DependencyDatabase database = _protocolDependencyAnalysis[0]
            .getDependencyDatabase();

      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         PrefixSpace ebgpExportSpace = new PrefixSpace();
         String name = e.getKey();
         Configuration node = e.getValue();
         BgpProcess proc = node.getBgpProcess();
         if (proc != null) {
            Set<PotentialExport> bgpExports = database.getPotentialExports(
                  name, RoutingProtocol.BGP);
            for (PotentialExport export : bgpExports) {
               DependentRoute exportSourceRoute = export.getDependency();
               if (!exportSourceRoute.dependsOn(RoutingProtocol.BGP)
                     && !exportSourceRoute.dependsOn(RoutingProtocol.IBGP)) {
                  Prefix prefix = export.getPrefix();
                  ebgpExportSpace.addPrefix(prefix);
               }
            }
            proc.setOriginationSpace(ebgpExportSpace);
         }
      }

      // for (Configuration node : _configurations.values()) {
      // BgpProcess proc = node.getBgpProcess();
      // PrefixSpace ebgpExportSpace = new PrefixSpace();
      // if (proc != null) {
      // for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
      // if (!neighbor.getRemoteAs().equals(neighbor.getLocalAs())) {
      // Set<PolicyMap> originationPolicies = neighbor.getOriginationPolicies();
      // Set<PolicyMap> exportPolicies = neighbor.getOutboundPolicyMaps();
      // PrefixSpace originationSpace = new PrefixSpace();
      // PrefixSpace exportSpace = new PrefixSpace();
      // for (PolicyMap originationPolicy : neighbor
      // .getOriginationPolicies()) {
      // PrefixSpace currentOriginationSpace = originationPolicy
      // .getPrefixSpace();
      // originationSpace.addSpace(currentOriginationSpace);
      // }
      // for (PolicyMap exportPolicy : neighbor
      // .getOutboundPolicyMaps()) {
      // PrefixSpace currentExportSpace = exportPolicy
      // .getPrefixSpace();
      // exportSpace.addSpace(currentExportSpace);
      // }
      // if (originationPolicies.isEmpty() && exportPolicies.isEmpty()) {
      // PrefixRange fullRange = new PrefixRange(Prefix.ZERO, new SubRange(0,
      // 32));
      // ebgpExportSpace.addPrefixRange(fullRange);
      // break;
      // }
      // else if (originationPolicies.isEmpty() && !exportPolicies.isEmpty()) {
      // ebgpExportSpace.addSpace(exportSpace);
      // }
      // else if (!originationPolicies.isEmpty() && exportPolicies.isEmpty()) {
      // ebgpExportSpace.addSpace(originationSpace);
      // }
      // else {
      // PrefixSpace intersectSpace =
      // originationSpace.intersection(exportSpace);
      // ebgpExportSpace.addSpace(intersectSpace);
      // }
      // }
      // }
      // proc.setOriginationSpace(ebgpExportSpace);
      // }
      // }

      _bgpOriginationSpaceInitialized[0] = true;
   }

   private void initProtocolDependencyAnalysis() {
      if (_protocolDependencyAnalysis[0] != null) {
         return;
      }
      _protocolDependencyAnalysis[0] = new ProtocolDependencyAnalysis(
            _configurations);
   }

   public void initRemoteIpsecVpns() {
      if (_remoteIpsecVpnsInitialized[0]) {
         return;
      }
      Map<IpsecVpn, Ip> remoteAddresses = new HashMap<IpsecVpn, Ip>();
      Map<Ip, Set<IpsecVpn>> externalAddresses = new HashMap<Ip, Set<IpsecVpn>>();
      for (Configuration c : _configurations.values()) {
         for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
            Ip remoteAddress = ipsecVpn.getGateway().getAddress();
            remoteAddresses.put(ipsecVpn, remoteAddress);
            Set<Prefix> externalPrefixes = ipsecVpn.getGateway()
                  .getExternalInterface().getAllPrefixes();
            for (Prefix externalPrefix : externalPrefixes) {
               Ip externalAddress = externalPrefix.getAddress();
               Set<IpsecVpn> vpnsUsingExternalAddress = externalAddresses
                     .get(externalAddress);
               if (vpnsUsingExternalAddress == null) {
                  vpnsUsingExternalAddress = new HashSet<IpsecVpn>();
                  externalAddresses.put(externalAddress,
                        vpnsUsingExternalAddress);
               }
               vpnsUsingExternalAddress.add(ipsecVpn);
            }
         }
      }
      for (Entry<IpsecVpn, Ip> e : remoteAddresses.entrySet()) {
         IpsecVpn ipsecVpn = e.getKey();
         Ip remoteAddress = e.getValue();
         ipsecVpn.initCandidateRemoteVpns();
         Set<IpsecVpn> remoteIpsecVpnCandidates = externalAddresses
               .get(remoteAddress);
         if (remoteIpsecVpnCandidates != null) {
            for (IpsecVpn remoteIpsecVpnCandidate : remoteIpsecVpnCandidates) {
               Ip reciprocalRemoteAddress = remoteAddresses
                     .get(remoteIpsecVpnCandidate);
               Set<IpsecVpn> reciprocalVpns = externalAddresses
                     .get(reciprocalRemoteAddress);
               if (reciprocalVpns != null && reciprocalVpns.contains(ipsecVpn)) {
                  ipsecVpn.setRemoteIpsecVpn(remoteIpsecVpnCandidate);
                  ipsecVpn.getCandidateRemoteIpsecVpns().add(
                        remoteIpsecVpnCandidate);
               }
            }
         }
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

   public void setRemoteIpsecVpn(IpsecVpn remoteIpsecVpn) {
      _remoteIpsecVpn = remoteIpsecVpn;
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
