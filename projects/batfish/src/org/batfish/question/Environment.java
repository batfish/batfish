package org.batfish.question;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpAdvertisement;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.RouteSet;
import org.batfish.datamodel.questions.QuestionParameters;
import org.batfish.datamodel.questions.VariableType;
import org.batfish.protocoldependency.DependencyDatabase;
import org.batfish.protocoldependency.DependentRoute;
import org.batfish.protocoldependency.PotentialExport;
import org.batfish.protocoldependency.ProtocolDependencyAnalysis;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Interface;
import org.batfish.representation.IpsecVpn;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PrefixSpace;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.StaticRoute;

public class Environment {

   private int[] _assertionCount;

   private boolean[] _assertions;

   private BgpAdvertisement _bgpAdvertisement;

   private Map<String, BgpAdvertisement> _bgpAdvertisements;

   private Map<String, Set<BgpAdvertisement>> _bgpAdvertisementSets;

   private boolean[] _bgpAdvertisementsInitialized;

   private BgpNeighbor _bgpNeighbor;

   private Map<String, BgpNeighbor> _bgpNeighbors;

   private Map<String, Set<BgpNeighbor>> _bgpNeighborSets;

   private boolean[] _bgpOriginationSpaceInitialized;

   private Map<String, Boolean> _booleans;

   private Map<String, Configuration> _configurations;

   private Set<RoutingProtocol> _currentProtocols;

   private int[] _failedAssertionCount;

   private GeneratedRoute _generatedRoute;

   private Map<String, GeneratedRoute> _generatedRoutes;

   private Map<String, Set<GeneratedRoute>> _generatedRouteSets;

   private AdvertisementSet _globalBgpAdvertisements;

   private RouteSet _globalRoutes;

   private Integer _integer;

   private Map<String, Integer> _integers;

   private Map<String, Set<Integer>> _integerSets;

   private Interface _interface;

   private Map<String, Interface> _interfaces;

   private Map<String, Set<Interface>> _interfaceSets;

   private Ip _ip;

   private Map<String, Ip> _ips;

   private IpsecVpn _ipsecVpn;

   private Map<String, IpsecVpn> _ipsecVpns;

   private Map<String, Set<IpsecVpn>> _ipsecVpnSets;

   private Map<String, Set<Ip>> _ipSets;

   private Map<String, QMap> _maps;

   private PolicyMapMatchProtocolLine _matchProtocolLine;

   private PolicyMapMatchRouteFilterListLine _matchRouteFilterLine;

   private Configuration _node;

   private Map<String, Configuration> _nodes;

   private Map<String, Set<Configuration>> _nodeSets;

   private PolicyMap _policyMap;

   private PolicyMapClause _policyMapClause;

   private Map<String, PolicyMapClause> _policyMapClauses;

   private Map<String, Set<PolicyMapClause>> _policyMapClauseSets;

   private Map<String, PolicyMap> _policyMaps;

   private Map<String, Set<PolicyMap>> _policyMapSets;

   private Prefix _prefix;

   private Map<String, Prefix> _prefixes;

   private Map<String, Set<Prefix>> _prefixSets;

   private PrefixSpace _prefixSpace;

   private Map<String, PrefixSpace> _prefixSpaces;

   private Map<String, Set<PrefixSpace>> _prefixSpaceSets;

   private RoutingProtocol _protocol;

   private ProtocolDependencyAnalysis[] _protocolDependencyAnalysis;

   private Map<String, RoutingProtocol> _protocols;

   private QMap _query;

   private BgpAdvertisement _receivedEbgpAdvertisement;

   private BgpAdvertisement _receivedIbgpAdvertisement;

   private BgpNeighbor _remoteBgpNeighbor;

   private boolean[] _remoteBgpNeighborsInitialized;

   private IpsecVpn _remoteIpsecVpn;

   private boolean[] _remoteIpsecVpnsInitialized;

   private PrecomputedRoute _route;

   private RouteFilterList _routeFilter;

   private RouteFilterLine _routeFilterLine;

   private Map<String, RouteFilterLine> _routeFilterLines;

   private Map<String, Set<RouteFilterLine>> _routeFilterLineSets;

   private Map<String, RouteFilterList> _routeFilters;

   private Set<RouteFilterList> _routeFilterSet;

   private Map<String, Set<RouteFilterList>> _routeFilterSets;

   private Map<String, PrecomputedRoute> _routes;

   private Map<String, Set<PrecomputedRoute>> _routeSets;

   private boolean[] _routesInitialized;

   private BgpAdvertisement _sentEbgpAdvertisement;

   private BgpAdvertisement _sentIbgpAdvertisement;

   private StaticRoute _staticRoute;

   private Map<String, StaticRoute> _staticRoutes;

   private Map<String, Set<StaticRoute>> _staticRouteSets;

   private String _string;

   private Map<String, String> _strings;

   private Map<String, Set<String>> _stringSets;

   private boolean[] _unsafe;

   public Environment() {
      _assertionCount = new int[1];
      _assertions = new boolean[1];
      _bgpAdvertisements = new HashMap<String, BgpAdvertisement>();
      _bgpAdvertisementsInitialized = new boolean[1];
      _bgpAdvertisementSets = new HashMap<String, Set<BgpAdvertisement>>();
      _bgpNeighbors = new HashMap<String, BgpNeighbor>();
      _bgpNeighborSets = new HashMap<String, Set<BgpNeighbor>>();
      _bgpOriginationSpaceInitialized = new boolean[1];
      _booleans = new HashMap<String, Boolean>();
      _failedAssertionCount = new int[1];
      _generatedRoutes = new HashMap<String, GeneratedRoute>();
      _generatedRouteSets = new HashMap<String, Set<GeneratedRoute>>();
      _integers = new HashMap<String, Integer>();
      _integerSets = new HashMap<String, Set<Integer>>();
      _interfaces = new HashMap<String, Interface>();
      _interfaceSets = new HashMap<String, Set<Interface>>();
      _ips = new HashMap<String, Ip>();
      _ipsecVpns = new HashMap<String, IpsecVpn>();
      _ipSets = new HashMap<String, Set<Ip>>();
      _ipsecVpnSets = new HashMap<String, Set<IpsecVpn>>();
      _maps = new HashMap<String, QMap>();
      _nodes = new HashMap<String, Configuration>();
      _nodeSets = new HashMap<String, Set<Configuration>>();
      _policyMaps = new HashMap<String, PolicyMap>();
      _policyMapClauses = new HashMap<String, PolicyMapClause>();
      _policyMapSets = new HashMap<String, Set<PolicyMap>>();
      _policyMapClauseSets = new HashMap<String, Set<PolicyMapClause>>();
      _prefixes = new HashMap<String, Prefix>();
      _prefixSets = new HashMap<String, Set<Prefix>>();
      _prefixSpaces = new HashMap<String, PrefixSpace>();
      _prefixSpaceSets = new HashMap<String, Set<PrefixSpace>>();
      _protocolDependencyAnalysis = new ProtocolDependencyAnalysis[1];
      _protocols = new HashMap<String, RoutingProtocol>();
      _query = new QMap();
      _remoteBgpNeighborsInitialized = new boolean[1];
      _remoteIpsecVpnsInitialized = new boolean[1];
      _routes = new HashMap<String, PrecomputedRoute>();
      _routesInitialized = new boolean[1];
      _routeSets = new HashMap<String, Set<PrecomputedRoute>>();
      _routeFilters = new HashMap<String, RouteFilterList>();
      _routeFilterLines = new HashMap<String, RouteFilterLine>();
      _routeFilterSets = new HashMap<String, Set<RouteFilterList>>();
      _routeFilterLineSets = new HashMap<String, Set<RouteFilterLine>>();
      _staticRoutes = new HashMap<String, StaticRoute>();
      _staticRouteSets = new HashMap<String, Set<StaticRoute>>();
      _strings = new HashMap<String, String>();
      _stringSets = new HashMap<String, Set<String>>();
      _unsafe = new boolean[1];
   }

   @SuppressWarnings("unchecked")
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

         case SET_STRING:
            Set<String> strSet = Collections.checkedSet((Set<String>) value,
                  String.class);
            _stringSets.put(var, strSet);
            break;

         case ACTION:
         case BGP_ADVERTISEMENT:
         case BGP_NEIGHBOR:
         case GENERATED_ROUTE:
         case INTERFACE:
         case IP:
         case IPSEC_VPN:
         case MAP:
         case NAMED_STRUCT_TYPE:
         case NODE:
         case NEIGHBOR_TYPE:
         case NODE_TYPE:            
         case POLICY_MAP:
         case POLICY_MAP_CLAUSE:
         case PREFIX:
         case PREFIX_SPACE:
         case PROTOCOL:
         case RANGE:
         case REGEX:
         case ROUTE:
         case ROUTE_FILTER:
         case ROUTE_FILTER_LINE:
         case SET_BGP_ADVERTISEMENT:
         case SET_BGP_NEIGHBOR:
         case SET_INT:
         case SET_INTERFACE:
         case SET_IP:
         case SET_IPSEC_VPN:
         case SET_NODE:
         case SET_POLICY_MAP:
         case SET_POLICY_MAP_CLAUSE:
         case SET_PREFIX:
         case SET_PREFIX_SPACE:
         case SET_ROUTE:
         case SET_ROUTE_FILTER:
         case SET_ROUTE_FILTER_LINE:
         case SET_STATIC_ROUTE:
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
      copy._bgpAdvertisement = _bgpAdvertisement;
      copy._bgpAdvertisements = _bgpAdvertisements;
      copy._bgpAdvertisementsInitialized = _bgpAdvertisementsInitialized;
      copy._bgpAdvertisementSets = _bgpAdvertisementSets;
      copy._bgpNeighbors = _bgpNeighbors;
      copy._bgpNeighborSets = _bgpNeighborSets;
      copy._bgpOriginationSpaceInitialized = _bgpOriginationSpaceInitialized;
      copy._booleans = _booleans;
      copy._configurations = _configurations;
      copy._failedAssertionCount = _failedAssertionCount;
      copy._generatedRoute = _generatedRoute;
      copy._generatedRoutes = _generatedRoutes;
      copy._generatedRouteSets = _generatedRouteSets;
      copy._globalBgpAdvertisements = _globalBgpAdvertisements;
      copy._globalRoutes = _globalRoutes;
      copy._integer = _integer;
      copy._integers = _integers;
      copy._integerSets = _integerSets;
      copy._interface = _interface;
      copy._interfaces = _interfaces;
      copy._interfaceSets = _interfaceSets;
      copy._ip = _ip;
      copy._ips = _ips;
      copy._ipSets = _ipSets;
      copy._ipsecVpn = _ipsecVpn;
      copy._ipsecVpns = _ipsecVpns;
      copy._ipsecVpnSets = _ipsecVpnSets;
      copy._maps = _maps;
      copy._matchProtocolLine = _matchProtocolLine;
      copy._matchRouteFilterLine = _matchRouteFilterLine;
      copy._node = _node;
      copy._nodes = _nodes;
      copy._nodeSets = _nodeSets;
      copy._policyMap = _policyMap;
      copy._policyMaps = _policyMaps;
      copy._policyMapSets = _policyMapSets;
      copy._policyMapClause = _policyMapClause;
      copy._policyMapClauses = _policyMapClauses;
      copy._policyMapClauseSets = _policyMapClauseSets;
      copy._prefix = _prefix;
      copy._prefixes = _prefixes;
      copy._prefixSets = _prefixSets;
      copy._prefixSpace = _prefixSpace;
      copy._prefixSpaces = _prefixSpaces;
      copy._prefixSpaceSets = _prefixSpaceSets;
      copy._protocol = _protocol;
      copy._currentProtocols = _currentProtocols;
      copy._protocolDependencyAnalysis = _protocolDependencyAnalysis;
      copy._protocols = _protocols;
      copy._query = _query;
      copy._receivedEbgpAdvertisement = _receivedEbgpAdvertisement;
      copy._receivedIbgpAdvertisement = _receivedIbgpAdvertisement;
      copy._remoteBgpNeighbor = _remoteBgpNeighbor;
      copy._remoteBgpNeighborsInitialized = _remoteBgpNeighborsInitialized;
      copy._remoteIpsecVpn = _remoteIpsecVpn;
      copy._remoteIpsecVpnsInitialized = _remoteIpsecVpnsInitialized;
      copy._route = _route;
      copy._routes = _routes;
      copy._routesInitialized = _routesInitialized;
      copy._routeSets = _routeSets;
      copy._routeFilter = _routeFilter;
      copy._routeFilters = _routeFilters;
      copy._routeFilterSet = _routeFilterSet;
      copy._routeFilterSets = _routeFilterSets;
      copy._routeFilterLine = _routeFilterLine;
      copy._routeFilterLines = _routeFilterLines;
      copy._routeFilterLineSets = _routeFilterLineSets;
      copy._sentEbgpAdvertisement = _sentEbgpAdvertisement;
      copy._sentIbgpAdvertisement = _sentIbgpAdvertisement;
      copy._staticRoute = _staticRoute;
      copy._staticRoutes = _staticRoutes;
      copy._staticRouteSets = _staticRouteSets;
      copy._string = _string;
      copy._strings = _strings;
      copy._stringSets = _stringSets;
      copy._unsafe = _unsafe;
      return copy;
   }

   public boolean getAssertions() {
      return _assertions[0];
   }

   public Map<String, BgpAdvertisement> getBgpAdvertisements() {
      return _bgpAdvertisements;
   }

   public Map<String, Set<BgpAdvertisement>> getBgpAdvertisementSets() {
      return _bgpAdvertisementSets;
   }

   public BgpNeighbor getBgpNeighbor() {
      return _bgpNeighbor;
   }

   public Map<String, BgpNeighbor> getBgpNeighbors() {
      return _bgpNeighbors;
   }

   public Map<String, Set<BgpNeighbor>> getBgpNeighborSets() {
      return _bgpNeighborSets;
   }

   public Map<String, Boolean> getBooleans() {
      return _booleans;
   }

   public Map<String, Configuration> getConfigurations() {
      return _configurations;
   }

   public Set<RoutingProtocol> getCurrentProtocols() {
      return _currentProtocols;
   }

   public int getFailedAssertions() {
      return _failedAssertionCount[0];
   }

   public GeneratedRoute getGeneratedRoute() {
      return _generatedRoute;
   }

   public Map<String, GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public Map<String, Set<GeneratedRoute>> getGeneratedRouteSets() {
      return _generatedRouteSets;
   }

   public Integer getInteger() {
      return _integer;
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

   public Map<String, Set<Interface>> getInterfaceSets() {
      return _interfaceSets;
   }

   public Ip getIp() {
      return _ip;
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

   public Map<String, Set<IpsecVpn>> getIpsecVpnSets() {
      return _ipsecVpnSets;
   }

   public Map<String, Set<Ip>> getIpSets() {
      return _ipSets;
   }

   public Map<String, QMap> getMaps() {
      return _maps;
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

   public Map<String, Set<Configuration>> getNodeSets() {
      return _nodeSets;
   }

   public PolicyMap getPolicyMap() {
      return _policyMap;
   }

   public PolicyMapClause getPolicyMapClause() {
      return _policyMapClause;
   }

   public Map<String, PolicyMapClause> getPolicyMapClauses() {
      return _policyMapClauses;
   }

   public Map<String, Set<PolicyMapClause>> getPolicyMapClauseSets() {
      return _policyMapClauseSets;
   }

   public Map<String, PolicyMap> getPolicyMaps() {
      return _policyMaps;
   }

   public Map<String, Set<PolicyMap>> getPolicyMapSets() {
      return _policyMapSets;
   }

   public Prefix getPrefix() {
      return _prefix;
   }

   public Map<String, Prefix> getPrefixes() {
      return _prefixes;
   }

   public Map<String, Set<Prefix>> getPrefixSets() {
      return _prefixSets;
   }

   public PrefixSpace getPrefixSpace() {
      return _prefixSpace;
   }

   public Map<String, PrefixSpace> getPrefixSpaces() {
      return _prefixSpaces;
   }

   public Map<String, Set<PrefixSpace>> getPrefixSpaceSets() {
      return _prefixSpaceSets;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public Map<String, RoutingProtocol> getProtocols() {
      return _protocols;
   }

   public QMap getQuery() {
      return _query;
   }

   public BgpAdvertisement getReceivedEbgpAdvertisement() {
      return _receivedEbgpAdvertisement;
   }

   public BgpAdvertisement getReceivedIbgpAdvertisement() {
      return _receivedIbgpAdvertisement;
   }

   public BgpNeighbor getRemoteBgpNeighbor() {
      return _remoteBgpNeighbor;
   }

   public IpsecVpn getRemoteIpsecVpn() {
      return _remoteIpsecVpn;
   }

   public PrecomputedRoute getRoute() {
      return _route;
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

   public Map<String, Set<RouteFilterLine>> getRouteFilterLineSets() {
      return _routeFilterLineSets;
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

   public Map<String, PrecomputedRoute> getRoutes() {
      return _routes;
   }

   public Map<String, Set<PrecomputedRoute>> getRouteSets() {
      return _routeSets;
   }

   public BgpAdvertisement getSentEbgpAdvertisement() {
      return _sentEbgpAdvertisement;
   }

   public BgpAdvertisement getSentIbgpAdvertisement() {
      return _sentIbgpAdvertisement;
   }

   public StaticRoute getStaticRoute() {
      return _staticRoute;
   }

   public Map<String, StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public Map<String, Set<StaticRoute>> getStaticRouteSets() {
      return _staticRouteSets;
   }

   public String getString() {
      return _string;
   }

   public Map<String, String> getStrings() {
      return _strings;
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

   public void initBgpAdvertisements() {
      if (_bgpAdvertisementsInitialized[0]) {
         return;
      }
      _bgpAdvertisementsInitialized[0] = true;
      for (Configuration node : _configurations.values()) {
         node.initBgpAdvertisements();
      }
      for (BgpAdvertisement bgpAdvertisement : _globalBgpAdvertisements) {
         BgpAdvertisementType type = BgpAdvertisementType
               .fromNxtnetTypeName(bgpAdvertisement.getType());
         switch (type) {
         case EBGP_ORIGINATED: {
            String originationNodeName = bgpAdvertisement.getSrcNode();
            Configuration originationNode = _configurations
                  .get(originationNodeName);
            if (originationNode != null) {
               originationNode.getBgpAdvertisements().add(bgpAdvertisement);
               originationNode.getOriginatedAdvertisements().add(
                     bgpAdvertisement);
               originationNode.getOriginatedEbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            else {
               throw new BatfishException(
                     "Originated bgp advertisement refers to missing node: \""
                           + originationNodeName + "\"");
            }
            break;
         }

         case IBGP_ORIGINATED: {
            String originationNodeName = bgpAdvertisement.getSrcNode();
            Configuration originationNode = _configurations
                  .get(originationNodeName);
            if (originationNode != null) {
               originationNode.getBgpAdvertisements().add(bgpAdvertisement);
               originationNode.getOriginatedAdvertisements().add(
                     bgpAdvertisement);
               originationNode.getOriginatedIbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            else {
               throw new BatfishException(
                     "Originated bgp advertisement refers to missing node: \""
                           + originationNodeName + "\"");
            }
            break;
         }

         case EBGP_RECEIVED: {
            String recevingNodeName = bgpAdvertisement.getDstNode();
            Configuration receivingNode = _configurations.get(recevingNodeName);
            if (receivingNode != null) {
               receivingNode.getBgpAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedEbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            break;
         }

         case IBGP_RECEIVED: {
            String recevingNodeName = bgpAdvertisement.getDstNode();
            Configuration receivingNode = _configurations.get(recevingNodeName);
            if (receivingNode != null) {
               receivingNode.getBgpAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedAdvertisements().add(bgpAdvertisement);
               receivingNode.getReceivedIbgpAdvertisements().add(
                     bgpAdvertisement);
            }
            break;
         }

         case EBGP_SENT: {
            String sendingNodeName = bgpAdvertisement.getSrcNode();
            Configuration sendingNode = _configurations.get(sendingNodeName);
            if (sendingNode != null) {
               sendingNode.getBgpAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentEbgpAdvertisements().add(bgpAdvertisement);
            }
            break;
         }

         case IBGP_SENT: {
            String sendingNodeName = bgpAdvertisement.getSrcNode();
            Configuration sendingNode = _configurations.get(sendingNodeName);
            if (sendingNode != null) {
               sendingNode.getBgpAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentAdvertisements().add(bgpAdvertisement);
               sendingNode.getSentIbgpAdvertisements().add(bgpAdvertisement);
            }
            break;
         }

         default:
            throw new BatfishException("Invalid bgp advertisement type");
         }
      }
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

   public void initRemoteBgpNeighbors() {
      if (!_remoteBgpNeighborsInitialized[0]) {
         Map<BgpNeighbor, Ip> remoteAddresses = new HashMap<BgpNeighbor, Ip>();
         Map<Ip, Set<BgpNeighbor>> localAddresses = new HashMap<Ip, Set<BgpNeighbor>>();
         for (Configuration node : _configurations.values()) {
            String hostname = node.getHostname();
            BgpProcess proc = node.getBgpProcess();
            if (proc != null) {
               for (BgpNeighbor bgpNeighbor : proc.getNeighbors().values()) {
                  bgpNeighbor.initCandidateRemoteBgpNeighbors();
                  if (bgpNeighbor.getPrefix().getPrefixLength() < 32) {
                     throw new BatfishException(
                           hostname
                                 + ": Do not support dynamic bgp sessions at this time: "
                                 + bgpNeighbor.getPrefix());
                  }
                  Ip remoteAddress = bgpNeighbor.getAddress();
                  if (remoteAddress == null) {
                     throw new BatfishException(
                           hostname
                                 + ": Could not determine remote address of bgp neighbor: "
                                 + bgpNeighbor);
                  }
                  Ip localAddress = bgpNeighbor.getLocalIp();
                  if (localAddress == null) {
                     continue;
                  }
                  remoteAddresses.put(bgpNeighbor, remoteAddress);
                  Set<BgpNeighbor> localAddressOwners = localAddresses
                        .get(localAddress);
                  if (localAddressOwners == null) {
                     localAddressOwners = new HashSet<BgpNeighbor>();
                     localAddresses.put(localAddress, localAddressOwners);
                  }
                  localAddressOwners.add(bgpNeighbor);
               }
            }
         }
         for (Entry<BgpNeighbor, Ip> e : remoteAddresses.entrySet()) {
            BgpNeighbor bgpNeighbor = e.getKey();
            Ip remoteAddress = e.getValue();
            Ip localAddress = bgpNeighbor.getLocalIp();
            Set<BgpNeighbor> remoteBgpNeighborCandidates = localAddresses
                  .get(remoteAddress);
            if (remoteBgpNeighborCandidates != null) {
               for (BgpNeighbor remoteBgpNeighborCandidate : remoteBgpNeighborCandidates) {
                  Ip reciprocalRemoteIp = remoteBgpNeighborCandidate
                        .getAddress();
                  if (localAddress.equals(reciprocalRemoteIp)) {
                     bgpNeighbor.getCandidateRemoteBgpNeighbors().add(
                           remoteBgpNeighborCandidate);
                     bgpNeighbor
                           .setRemoteBgpNeighbor(remoteBgpNeighborCandidate);
                  }
               }
            }
         }
         _remoteBgpNeighborsInitialized[0] = true;
      }
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
               Ip remoteIpsecVpnLocalAddress = remoteIpsecVpnCandidate
                     .getGateway().getLocalAddress();
               if (remoteIpsecVpnLocalAddress != null
                     && !remoteIpsecVpnLocalAddress.equals(remoteAddress)) {
                  continue;
               }
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

   public void initRoutes() {
      if (_routesInitialized[0]) {
         return;
      }
      _routesInitialized[0] = true;
      for (Configuration node : _configurations.values()) {
         node.initRoutes();
      }
      for (PrecomputedRoute route : _globalRoutes) {
         String nodeName = route.getNode();
         Configuration node = _configurations.get(nodeName);
         if (node != null) {
            node.getRoutes().add(route);
         }
         else {
            throw new BatfishException(
                  "Precomputed route refers to missing node: \"" + nodeName
                        + "\"");
         }
      }
   }

   public void setAssertions(boolean b) {
      _assertions[0] = b;
   }

   public void setBgpAdvertisement(BgpAdvertisement bgpAdvertisement) {
      _bgpAdvertisement = bgpAdvertisement;
   }

   public void setBgpNeighbor(BgpNeighbor bgpNeighbor) {
      _bgpNeighbor = bgpNeighbor;
   }

   public void setConfigurations(Map<String, Configuration> configurations) {
      _configurations = configurations;
   }

   public void setGeneratedRoute(GeneratedRoute generatedRoute) {
      _generatedRoute = generatedRoute;
   }

   public void setGlobalBgpAdvertisements(AdvertisementSet bgpAdvertisements) {
      _globalBgpAdvertisements = bgpAdvertisements;
   }

   public void setGlobalRoutes(RouteSet routes) {
      _globalRoutes = routes;
   }

   public void setInteger(Integer i) {
      _integer = i;
   }

   public void setInterface(Interface iface) {
      _interface = iface;
   }

   public void setIp(Ip ip) {
      _ip = ip;
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

   public void setPolicyMapClause(PolicyMapClause policyMapClause) {
      _policyMapClause = policyMapClause;
   }

   public void setPrefix(Prefix prefix) {
      _prefix = prefix;
   }

   public void setPrefixSpace(PrefixSpace prefixSpace) {
      _prefixSpace = prefixSpace;
   }

   public void setProtocolSet(Set<RoutingProtocol> currentProtocols) {
      _currentProtocols = currentProtocols;
   }

   public void setReceivedEbgpAdvertisement(BgpAdvertisement t) {
      _receivedEbgpAdvertisement = t;
   }

   public void setReceivedIbgpAdvertisement(BgpAdvertisement t) {
      _receivedIbgpAdvertisement = t;
   }

   public void setRemoteBgpNeighbor(BgpNeighbor remoteBgpNeighbor) {
      _remoteBgpNeighbor = remoteBgpNeighbor;
   }

   public void setRemoteIpsecVpn(IpsecVpn remoteIpsecVpn) {
      _remoteIpsecVpn = remoteIpsecVpn;
   }

   public void setRoute(PrecomputedRoute route) {
      _route = route;
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

   public void setSentEbgpAdvertisement(BgpAdvertisement t) {
      _sentEbgpAdvertisement = t;
   }

   public void setSentIbgpAdvertisement(BgpAdvertisement t) {
      _sentIbgpAdvertisement = t;
   }

   public void setStaticRoute(StaticRoute staticRoute) {
      _staticRoute = staticRoute;
   }

   public void setString(String string) {
      _string = string;
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
