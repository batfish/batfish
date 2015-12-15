package org.batfish.protocoldependency;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.dot.Digraph;
import org.batfish.dot.DotInput;
import org.batfish.dot.DotJob;
import org.batfish.dot.Edge;
import org.batfish.dot.Node;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.LineAction;
import org.batfish.representation.OspfArea;
import org.batfish.representation.OspfMetricType;
import org.batfish.representation.OspfProcess;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchType;
import org.batfish.representation.Prefix;
import org.batfish.representation.PrefixSpaceList;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;

/**
 * TODO: ospfe1
 */
public final class ProtocolDependencyAnalysis {

   private final Map<PolicyMapClause, Set<PrefixSpaceList>> _clausePrefixSpaceMap;

   private final Map<String, Configuration> _configurations;

   private final DependencyDatabase _dependencyDatabase;

   public ProtocolDependencyAnalysis(Map<String, Configuration> configurations) {
      _configurations = configurations;
      _dependencyDatabase = new DependencyDatabase(_configurations);
      _clausePrefixSpaceMap = new HashMap<PolicyMapClause, Set<PrefixSpaceList>>();
      initialize();
   }

   private void cleanDatabase() {
      _dependencyDatabase.clearPotentialExports();
      _dependencyDatabase.clearPotentialImports();
   }

   private void collectEdges(Set<Edge> edges, Map<String, Node> nodes,
         DependentRoute route) {
      String nodeId = route.getDotNodeId();
      String nodeLabel = getDotNodeLabel(route);
      Node node = nodes.get(nodeId);
      if (node == null) {
         node = new Node(nodeId);
         node.setLabel(nodeLabel);
         nodes.put(nodeId, node);
      }
      for (DependentRoute dependency : route.getDependencies()) {
         String dependencyNodeId = dependency.getDotNodeId();
         String dependencyNodeLabel = getDotNodeLabel(dependency);
         Node dependencyNode = nodes.get(dependencyNodeId);
         if (dependencyNode == null) {
            dependencyNode = new Node(dependencyNodeId);
            dependencyNode.setLabel(dependencyNodeLabel);
            nodes.put(dependencyNodeId, dependencyNode);
         }
         Edge edge = new Edge(node, dependencyNode);
         edges.add(edge);
      }
   }

   private Set<RoutingProtocol> getClausePermittedProtocols(
         Set<RoutingProtocol> permittedProtocols, PolicyMapClause clause) {
      Set<RoutingProtocol> protocols = new HashSet<RoutingProtocol>();
      boolean foundMatchProtocol = false;
      for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
         if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
            foundMatchProtocol = true;
            PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
            protocols.add(matchProtocolLine.getProtocol());
         }
      }
      if (!foundMatchProtocol) {
         protocols.addAll(Arrays.asList(RoutingProtocol.values()));
      }
      if (permittedProtocols != null) {
         protocols.retainAll(permittedProtocols);
      }
      return protocols;
   }

   public DependencyDatabase getDependencyDatabase() {
      return _dependencyDatabase;
   }

   private DotInput getDotInput() {
      Digraph graph = new Digraph();
      Set<Edge> edges = new HashSet<Edge>();
      Map<String, Node> nodes = new HashMap<String, Node>();
      Set<DependentRoute> routes = _dependencyDatabase.getDependentRoutes();
      int i = 0;
      for (DependentRoute route : routes) {
         route.setDotNodeId("node" + i);
         i++;
      }
      for (DependentRoute route : routes) {
         collectEdges(edges, nodes, route);
      }
      graph.getEdges().addAll(edges);
      graph.getNodes().addAll(nodes.values());
      return graph;
   }

   private String getDotNodeLabel(DependentRoute route) {
      return route.getNode() + ":" + route.getProtocol().toString() + ":"
            + route.getPrefix();
   }

   private Set<PotentialExport> getPermittedExports(
         RoutingProtocol exportProtocol,
         Set<RoutingProtocol> permittedProtocols, String node,
         Set<PolicyMap> policies) {
      Set<PotentialExport> permittedExports = new HashSet<PotentialExport>();
      for (PolicyMap policy : policies) {
         Set<DependentRoute> contributingRoutes = getPermittedRoutes(node,
               policy, permittedProtocols);
         for (DependentRoute contributingRoute : contributingRoutes) {
            Prefix prefix = contributingRoute.getPrefix();
            PotentialExport potentialExport = new PotentialExport(node, prefix,
                  exportProtocol, contributingRoute);
            permittedExports.add(potentialExport);
         }
      }
      return permittedExports;
   }

   private Set<PotentialExport> getPermittedExports(
         Set<PotentialExport> originationExports,
         Set<PolicyMap> exportPolicies, Set<RoutingProtocol> permittedProtocols) {
      Set<PotentialExport> permittedExports = new LinkedHashSet<PotentialExport>();
      for (PotentialExport originationExport : originationExports) {
         Prefix prefix = originationExport.getPrefix();
         RoutingProtocol protocol = originationExport.getProtocol();
         for (PolicyMap exportPolicy : exportPolicies) {
            if (policyPermits(exportPolicy, prefix, protocol,
                  permittedProtocols)) {
               permittedExports.add(originationExport);
            }
         }
      }
      return permittedExports;
   }

   private Set<DependentRoute> getPermittedRoutes(String node,
         PolicyMap policy, Set<RoutingProtocol> permittedProtocols) {
      Set<DependentRoute> contributingRoutes = new LinkedHashSet<DependentRoute>();
      Set<DependentRoute> dependentRoutes = _dependencyDatabase
            .getDependentRoutes(node);
      for (DependentRoute dependentRoute : dependentRoutes) {
         Prefix prefix = dependentRoute.getPrefix();
         RoutingProtocol protocol = dependentRoute.getProtocol();
         if (policyPermits(policy, prefix, protocol, permittedProtocols)) {
            contributingRoutes.add(dependentRoute);
         }
      }
      return contributingRoutes;
   }

   private void initBgpRoutes() {
      initRoutes(RoutingProtocol.BGP);
      initRoutes(RoutingProtocol.IBGP);
   }

   private void initConnectedRoutes() {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         for (Interface iface : c.getInterfaces().values()) {
            Prefix interfacePrefix = iface.getPrefix();
            if (interfacePrefix != null) {
               Prefix prefix = interfacePrefix.getNetworkPrefix();
               RoutingProtocol protocol = RoutingProtocol.CONNECTED;
               DependentRoute dependentRoute = new DependentRoute(node, prefix,
                     protocol);
               _dependencyDatabase.addDependentRoute(dependentRoute);
            }
         }
      }
   }

   private void initFixedPointRoutes() {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         Set<StaticRoute> staticRoutesToCheck = new LinkedHashSet<StaticRoute>();
         Set<GeneratedRoute> generatedRoutesToCheck = new LinkedHashSet<GeneratedRoute>();
         String node = e.getKey();
         Configuration c = e.getValue();
         staticRoutesToCheck.addAll(c.getStaticRoutes());
         generatedRoutesToCheck.addAll(c.getGeneratedRoutes());
         boolean workDone;
         do {
            workDone = false;

            // static routes
            Set<StaticRoute> failedStaticRoutes = new LinkedHashSet<StaticRoute>();
            for (StaticRoute staticRoute : staticRoutesToCheck) {
               RoutingProtocol protocol = RoutingProtocol.STATIC;
               if (staticRoute.getNextHopInterface() == null) {
                  Ip nextHopIp = staticRoute.getNextHopIp();
                  Set<DependentRoute> longestPrefixMatches = _dependencyDatabase
                        .getLongestPrefixMatch(node, nextHopIp);
                  if (!longestPrefixMatches.isEmpty()) {
                     workDone = true;
                     Prefix prefix = staticRoute.getPrefix();
                     DependentRoute dependentRoute = new DependentRoute(node,
                           prefix, protocol);
                     dependentRoute.getDependencies().addAll(
                           longestPrefixMatches);
                     _dependencyDatabase.addDependentRoute(dependentRoute);
                  }
                  else {
                     failedStaticRoutes.add(staticRoute);
                  }
               }
            }
            staticRoutesToCheck = failedStaticRoutes;

            // generated routes
            Set<GeneratedRoute> failedGeneratedRoutes = new LinkedHashSet<GeneratedRoute>();
            for (GeneratedRoute gr : generatedRoutesToCheck) {
               RoutingProtocol protocol = RoutingProtocol.AGGREGATE;
               Prefix prefix = gr.getPrefix();
               Set<DependentRoute> contributingRoutes = new LinkedHashSet<DependentRoute>();
               for (PolicyMap grPolicy : gr.getGenerationPolicies()) {
                  Set<DependentRoute> policyContributingRoutes = getPermittedRoutes(
                        node, grPolicy, null);
                  contributingRoutes.addAll(policyContributingRoutes);
               }
               if (contributingRoutes.isEmpty()) {
                  failedGeneratedRoutes.add(gr);
               }
               else {
                  workDone = true;
                  DependentRoute generatedRoute = new DependentRoute(node,
                        prefix, protocol);
                  generatedRoute.getDependencies().addAll(contributingRoutes);
                  _dependencyDatabase.addDependentRoute(generatedRoute);
               }
            }
            generatedRoutesToCheck = failedGeneratedRoutes;

         } while (workDone);
      }
   }

   public void initialize() {
      initPolicyPrefixSpaces();

      // One and done
      initConnectedRoutes();
      initStaticInterfaceRoutes();
      initPotentialIgpExports();
      initPotentialIgpImports();
      initIgpRoutes();
      cleanDatabase();

      // First iteration
      initFixedPointRoutes();
      initPotentialOspfExternalExports();
      initPotentialOspfExternalImports();
      initPotentialEbgpOriginationExports();
      initPotentialEbgpImports();

      initOspfExternalRoutes();

      initPotentialIbgpOriginationExports();
      initPotentialIbgpImports();
      initBgpRoutes();

      cleanDatabase();

      // second iteration
      initFixedPointRoutes();
      initPotentialOspfExternalExports();
      initPotentialOspfExternalImports();
      initPotentialEbgpOriginationExports();
      initPotentialEbgpRecursiveExports();
      initPotentialEbgpImports();

      initOspfExternalRoutes();

      initPotentialIbgpOriginationExports();
      initPotentialIbgpRecursiveExports();
      initPotentialIbgpImports();
      initBgpRoutes();

      // cleanDatabase();

      initFixedPointRoutes();

      removeCycles();

      _dependencyDatabase.calculateDependencies();
   }

   private void initIgpRoutes() {
      initOspfInternalRoutes();
   }

   private void initOspfExternalRoutes() {
      initRoutes(RoutingProtocol.OSPF_E1);
      initRoutes(RoutingProtocol.OSPF_E2);
   }

   private void initOspfInternalRoutes() {
      initRoutes(RoutingProtocol.OSPF);
      initRoutes(RoutingProtocol.OSPF_IA);
   }

   private void initPolicyPrefixSpaces() {
      for (Configuration c : _configurations.values()) {
         for (PolicyMap p : c.getPolicyMaps().values()) {
            for (PolicyMapClause clause : p.getClauses()) {
               boolean matchRouteFilter = false;
               Set<PrefixSpaceList> prefixSpaceLists = new LinkedHashSet<PrefixSpaceList>();
               _clausePrefixSpaceMap.put(clause, prefixSpaceLists);
               for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                  if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                     if (matchRouteFilter) {
                        throw new BatfishException(
                              "Do not support multiple match route filters at this time");
                     }
                     matchRouteFilter = true;
                     PolicyMapMatchRouteFilterListLine matchRfLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                     for (RouteFilterList rf : matchRfLine.getLists()) {
                        PrefixSpaceList psl = PrefixSpaceList
                              .fromRouteFilter(rf);
                        prefixSpaceLists.add(psl);
                     }
                  }
               }
            }
         }
      }
   }

   private void initPotentialEbgpImports() {
      RoutingProtocol protocol = RoutingProtocol.BGP;
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getBgpProcess() != null) {
            boolean ebgp = false;
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               if (!neighbor.getLocalAs().equals(neighbor.getRemoteAs())) {
                  ebgp = true;
                  break;
               }
            }
            if (!ebgp) {
               continue;
            }
            for (PotentialExport potentialExport : _dependencyDatabase
                  .getPotentialExports(protocol)) {
               if (!node.equals(potentialExport.getNode())) {
                  Prefix prefix = potentialExport.getPrefix();
                  PotentialImport potentialImport = new PotentialImport(node,
                        prefix, protocol, potentialExport);
                  _dependencyDatabase.addPotentialImport(potentialImport);
               }
            }
         }
      }
   }

   private void initPotentialEbgpOriginationExports() {
      RoutingProtocol exportProtocol = RoutingProtocol.BGP;
      Set<RoutingProtocol> permittedProtocols = new HashSet<RoutingProtocol>();
      permittedProtocols.addAll(Arrays.asList(RoutingProtocol.values()));
      permittedProtocols.remove(RoutingProtocol.BGP);
      permittedProtocols.remove(RoutingProtocol.IBGP);
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         if (c.getBgpProcess() != null) {
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               boolean ebgp = !neighbor.getLocalAs().equals(
                     neighbor.getRemoteAs());
               if (!ebgp) {
                  continue;
               }
               Set<PolicyMap> originationPolicies = neighbor
                     .getOriginationPolicies();
               Set<PolicyMap> exportPolicies = neighbor.getOutboundPolicyMaps();
               if (!originationPolicies.isEmpty() && exportPolicies.isEmpty()) {
                  Set<PotentialExport> originationExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        originationPolicies);
                  for (PotentialExport originationExport : originationExports) {
                     _dependencyDatabase.addPotentialExport(originationExport);
                  }
               }
               else if (originationPolicies.isEmpty()
                     && !exportPolicies.isEmpty()) {
                  Set<PotentialExport> exportExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        exportPolicies);
                  for (PotentialExport exportExport : exportExports) {
                     _dependencyDatabase.addPotentialExport(exportExport);
                  }
               }
               else if (!originationPolicies.isEmpty()
                     && !exportPolicies.isEmpty()) {
                  Set<PotentialExport> originationExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        originationPolicies);
                  Set<PotentialExport> exportExports = getPermittedExports(
                        originationExports, exportPolicies, permittedProtocols);
                  for (PotentialExport exportExport : exportExports) {
                     _dependencyDatabase.addPotentialExport(exportExport);
                  }

               }
               else {
                  throw new BatfishException("todo");
               }
            }
         }
      }
   }

   private void initPotentialEbgpRecursiveExports() {
      RoutingProtocol exportProtocol = RoutingProtocol.BGP;
      Set<RoutingProtocol> permittedProtocols = new HashSet<RoutingProtocol>();
      permittedProtocols.add(RoutingProtocol.BGP);
      permittedProtocols.add(RoutingProtocol.IBGP);
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         if (c.getBgpProcess() != null) {
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               boolean ebgp = !neighbor.getLocalAs().equals(
                     neighbor.getRemoteAs());
               if (!ebgp) {
                  continue;
               }
               Set<PolicyMap> exportPolicies = neighbor.getOutboundPolicyMaps();
               Set<DependentRoute> dependentRoutes = new LinkedHashSet<DependentRoute>();
               for (PolicyMap exportPolicy : exportPolicies) {
                  dependentRoutes.addAll(getPermittedRoutes(node, exportPolicy,
                        permittedProtocols));
               }
               if (exportPolicies.isEmpty()) {
                  for (RoutingProtocol protocol : permittedProtocols) {
                     dependentRoutes.addAll(_dependencyDatabase
                           .getDependentRoutes(node, protocol));
                  }
               }
               for (DependentRoute dependentRoute : dependentRoutes) {
                  Prefix prefix = dependentRoute.getPrefix();
                  PotentialExport potentialExport = new PotentialExport(node,
                        prefix, exportProtocol, dependentRoute);
                  _dependencyDatabase.addPotentialExport(potentialExport);
               }
            }
         }
      }
   }

   private void initPotentialIbgpImports() {
      RoutingProtocol protocol = RoutingProtocol.IBGP;
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getBgpProcess() != null) {
            boolean ibgp = false;
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               if (neighbor.getLocalAs().equals(neighbor.getRemoteAs())) {
                  ibgp = true;
                  break;
               }
            }
            if (!ibgp) {
               continue;
            }
            for (PotentialExport potentialExport : _dependencyDatabase
                  .getPotentialExports(protocol)) {
               if (!node.equals(potentialExport.getNode())) {
                  Prefix prefix = potentialExport.getPrefix();
                  PotentialImport potentialImport = new PotentialImport(node,
                        prefix, protocol, potentialExport);
                  _dependencyDatabase.addPotentialImport(potentialImport);
               }
            }
         }
      }
   }

   private void initPotentialIbgpOriginationExports() {
      RoutingProtocol exportProtocol = RoutingProtocol.IBGP;
      Set<RoutingProtocol> permittedProtocols = new HashSet<RoutingProtocol>();
      permittedProtocols.addAll(Arrays.asList(RoutingProtocol.values()));
      permittedProtocols.remove(RoutingProtocol.BGP);
      permittedProtocols.remove(RoutingProtocol.IBGP);
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         if (c.getBgpProcess() != null) {
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               boolean ibgp = neighbor.getLocalAs().equals(
                     neighbor.getRemoteAs());
               if (!ibgp) {
                  continue;
               }
               Set<PolicyMap> originationPolicies = neighbor
                     .getOriginationPolicies();
               Set<PolicyMap> exportPolicies = neighbor.getOutboundPolicyMaps();
               if (!originationPolicies.isEmpty() && exportPolicies.isEmpty()) {
                  Set<PotentialExport> originationExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        originationPolicies);
                  for (PotentialExport originationExport : originationExports) {
                     _dependencyDatabase.addPotentialExport(originationExport);
                  }
               }
               else if (originationPolicies.isEmpty()
                     && !exportPolicies.isEmpty()) {
                  Set<PotentialExport> exportExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        exportPolicies);
                  for (PotentialExport exportExport : exportExports) {
                     _dependencyDatabase.addPotentialExport(exportExport);
                  }
               }
               else if (!originationPolicies.isEmpty()
                     && !exportPolicies.isEmpty()) {
                  Set<PotentialExport> originationExports = getPermittedExports(
                        exportProtocol, permittedProtocols, node,
                        originationPolicies);
                  Set<PotentialExport> exportExports = getPermittedExports(
                        originationExports, exportPolicies, permittedProtocols);
                  for (PotentialExport exportExport : exportExports) {
                     _dependencyDatabase.addPotentialExport(exportExport);
                  }

               }
               else {
                  throw new BatfishException("todo");
               }
            }
         }
      }
   }

   private void initPotentialIbgpRecursiveExports() {
      RoutingProtocol exportProtocol = RoutingProtocol.IBGP;
      Set<RoutingProtocol> permittedProtocols = new HashSet<RoutingProtocol>();
      permittedProtocols.add(RoutingProtocol.BGP);
      permittedProtocols.add(RoutingProtocol.IBGP);
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         if (c.getBgpProcess() != null) {
            for (BgpNeighbor neighbor : c.getBgpProcess().getNeighbors()
                  .values()) {
               boolean ibgp = neighbor.getLocalAs().equals(
                     neighbor.getRemoteAs());
               if (!ibgp) {
                  continue;
               }
               Set<PolicyMap> exportPolicies = neighbor.getOutboundPolicyMaps();
               Set<DependentRoute> dependentRoutes = new LinkedHashSet<DependentRoute>();
               for (PolicyMap exportPolicy : exportPolicies) {
                  dependentRoutes.addAll(getPermittedRoutes(node, exportPolicy,
                        permittedProtocols));
               }
               if (exportPolicies.isEmpty()) {
                  for (RoutingProtocol protocol : permittedProtocols) {
                     Set<DependentRoute> protocolDependentRoutes = _dependencyDatabase
                           .getDependentRoutes(node, protocol);
                     dependentRoutes.addAll(protocolDependentRoutes);
                  }
               }
               for (DependentRoute dependentRoute : dependentRoutes) {
                  Prefix prefix = dependentRoute.getPrefix();
                  PotentialExport potentialExport = new PotentialExport(node,
                        prefix, exportProtocol, dependentRoute);
                  _dependencyDatabase.addPotentialExport(potentialExport);
               }
            }
         }
      }
   }

   private void initPotentialIgpExports() {
      initPotentialOspfInternalExports();
   }

   private void initPotentialIgpImports() {
      initPotentialOspfInternalImports();
   }

   private void initPotentialOspfExternalExports() {
      Set<RoutingProtocol> permittedProtocols = new HashSet<RoutingProtocol>();
      permittedProtocols.addAll(Arrays.asList(RoutingProtocol.values()));
      permittedProtocols.remove(RoutingProtocol.OSPF);
      permittedProtocols.remove(RoutingProtocol.OSPF_E1);
      permittedProtocols.remove(RoutingProtocol.OSPF_E2);
      permittedProtocols.remove(RoutingProtocol.OSPF_IA);
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         OspfProcess proc = c.getOspfProcess();
         if (c.getOspfProcess() != null) {
            for (PolicyMap outboundPolicy : proc.getOutboundPolicyMaps()) {
               OspfMetricType metricType = proc.getPolicyMetricTypes().get(
                     outboundPolicy);
               RoutingProtocol protocol = metricType.toRoutingProtocol();
               Set<DependentRoute> dependentRoutes = getPermittedRoutes(node,
                     outboundPolicy, permittedProtocols);
               for (DependentRoute dependentRoute : dependentRoutes) {
                  Prefix prefix = dependentRoute.getPrefix();
                  OspfExternalPotentialExport potentialExport = new OspfExternalPotentialExport(
                        node, prefix, protocol, dependentRoute);
                  _dependencyDatabase.addPotentialExport(potentialExport);
               }
            }
         }
      }
   }

   private void initPotentialOspfExternalImports() {
      initPotentialOspfExternalImports(RoutingProtocol.OSPF_E1);
      initPotentialOspfExternalImports(RoutingProtocol.OSPF_E2);
   }

   private void initPotentialOspfExternalImports(RoutingProtocol protocol) {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getOspfProcess() != null) {
            for (PotentialExport potentialExport : _dependencyDatabase
                  .getPotentialExports(protocol)) {
               if (!node.equals(potentialExport.getNode())) {
                  Prefix prefix = potentialExport.getPrefix();
                  PotentialImport potentialImport = new PotentialImport(node,
                        prefix, protocol, potentialExport);
                  _dependencyDatabase.addPotentialImport(potentialImport);
               }
            }
         }
      }
   }

   private void initPotentialOspfInterAreaExports() {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getOspfProcess() != null) {
            if (c.getOspfProcess().getAreas().size() > 1) {
               Set<Long> areas = c.getOspfProcess().getAreas().keySet();
               for (OspfArea area : c.getOspfProcess().getAreas().values()) {
                  for (Interface iface : area.getInterfaces()) {
                     Prefix prefix = iface.getPrefix().getNetworkPrefix();
                     DependentRoute dependentRoute = _dependencyDatabase
                           .getConnectedRoute(node, prefix);
                     PotentialExport potentialExport = new OspfInterAreaPotentialExport(
                           node, prefix, dependentRoute, areas);
                     _dependencyDatabase.addPotentialExport(potentialExport);
                  }
               }
            }
         }
      }
   }

   private void initPotentialOspfInterAreaImports() {
      RoutingProtocol protocol = RoutingProtocol.OSPF_IA;
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getOspfProcess() != null) {
            Set<Long> areas = c.getOspfProcess().getAreas().keySet();
            for (PotentialExport potentialExport : _dependencyDatabase
                  .getPotentialExports(protocol)) {
               OspfInterAreaPotentialExport ospfInterAreaPotentialExport = (OspfInterAreaPotentialExport) potentialExport;
               Set<Long> commonAreas = new HashSet<Long>();
               commonAreas
                     .addAll(ospfInterAreaPotentialExport.getAreaNumbers());
               commonAreas.retainAll(areas);
               if (!node.equals(potentialExport.getNode())
                     && !commonAreas.isEmpty()) {
                  Prefix prefix = potentialExport.getPrefix();
                  PotentialImport potentialImport = new PotentialImport(node,
                        prefix, protocol, potentialExport);
                  _dependencyDatabase.addPotentialImport(potentialImport);
               }
            }
         }
      }
   }

   private void initPotentialOspfInternalExports() {
      initPotentialOspfIntraAreaExports();
      initPotentialOspfInterAreaExports();
   }

   private void initPotentialOspfInternalImports() {
      initPotentialOspfIntraAreaImports();
      initPotentialOspfInterAreaImports();
   }

   private void initPotentialOspfIntraAreaExports() {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getOspfProcess() != null) {
            for (Entry<Long, OspfArea> e2 : c.getOspfProcess().getAreas()
                  .entrySet()) {
               long areaNum = e2.getKey();
               OspfArea area = e2.getValue();
               for (Interface iface : area.getInterfaces()) {
                  Prefix prefix = iface.getPrefix().getNetworkPrefix();
                  DependentRoute dependentRoute = _dependencyDatabase
                        .getConnectedRoute(node, prefix);
                  PotentialExport potentialExport = new OspfIntraAreaPotentialExport(
                        node, prefix, dependentRoute, areaNum);
                  _dependencyDatabase.addPotentialExport(potentialExport);
               }
            }
         }
      }
   }

   private void initPotentialOspfIntraAreaImports() {
      RoutingProtocol protocol = RoutingProtocol.OSPF;
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         if (c.getOspfProcess() != null) {
            Set<Long> areas = c.getOspfProcess().getAreas().keySet();
            for (PotentialExport potentialExport : _dependencyDatabase
                  .getPotentialExports(protocol)) {
               OspfIntraAreaPotentialExport ospfIntraAreaPotentialExport = (OspfIntraAreaPotentialExport) potentialExport;
               if (!node.equals(potentialExport.getNode())
                     && areas.contains(ospfIntraAreaPotentialExport
                           .getAreaNum())) {
                  Prefix prefix = potentialExport.getPrefix();
                  PotentialImport potentialImport = new PotentialImport(node,
                        prefix, protocol, potentialExport);
                  _dependencyDatabase.addPotentialImport(potentialImport);
               }
            }
         }
      }
   }

   private void initRoutes(RoutingProtocol protocol) {
      Set<PotentialImport> potentialImports = _dependencyDatabase
            .getPotentialImports(protocol);
      for (PotentialImport potentialImport : potentialImports) {
         String node = potentialImport.getNode();
         Prefix prefix = potentialImport.getPrefix();
         DependentRoute dependentRoute = new DependentRoute(node, prefix,
               protocol);
         dependentRoute.getDependencies().add(
               potentialImport.getPotentialExport().getDependency());
         _dependencyDatabase.addDependentRoute(dependentRoute);
      }
   }

   private void initStaticInterfaceRoutes() {
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String node = e.getKey();
         Configuration c = e.getValue();
         for (StaticRoute staticRoute : c.getStaticRoutes()) {
            if (staticRoute.getNextHopInterface() != null) {
               Prefix prefix = staticRoute.getPrefix();
               RoutingProtocol protocol = RoutingProtocol.STATIC;
               DependentRoute dependentRoute = new DependentRoute(node, prefix,
                     protocol);
               _dependencyDatabase.addDependentRoute(dependentRoute);
            }
         }
      }
   }

   private boolean policyPermits(PolicyMap policy, Prefix prefix,
         RoutingProtocol protocol, Set<RoutingProtocol> permittedProtocols) {
      for (PolicyMapClause clause : policy.getClauses()) {
         boolean clauseAcceptsPrefix = false;
         Set<RoutingProtocol> protocols = getClausePermittedProtocols(
               permittedProtocols, clause);
         Set<PrefixSpaceList> clausePrefixSpaceLists = _clausePrefixSpaceMap
               .get(clause);
         if (protocols.contains(protocol)) {
            clauseAcceptsPrefix = clausePrefixSpaceLists.isEmpty();
            for (PrefixSpaceList prefixSpaceList : clausePrefixSpaceLists) {
               LineAction pslAction = prefixSpaceList.getAction(prefix);
               if (pslAction == LineAction.ACCEPT) {
                  clauseAcceptsPrefix = true;
                  break;
               }
            }
            if (clauseAcceptsPrefix) {
               if (clause.getAction() == PolicyMapAction.PERMIT) {
                  return true;
               }
               else {
                  return false;
               }
            }
         }
      }
      return false;
   }

   public void printDependencies(BatfishLogger logger) {
      StringBuilder sb = new StringBuilder();
      Map<RoutingProtocol, Map<RoutingProtocol, Set<Integer>>> protocolDependencies = _dependencyDatabase
            .getCompactProtocolDependencies();
      for (Entry<RoutingProtocol, Map<RoutingProtocol, Set<Integer>>> e : protocolDependencies
            .entrySet()) {
         RoutingProtocol dependentProtocol = e.getKey();
         sb.append(dependentProtocol.toString() + ": {");
         Map<RoutingProtocol, Set<Integer>> compactDependencies = e.getValue();
         RoutingProtocol[] dependencyProtocols = compactDependencies.keySet()
               .toArray(new RoutingProtocol[] {});
         for (int i = 0; i < dependencyProtocols.length; i++) {
            RoutingProtocol dependencyProtocol = dependencyProtocols[i];
            Set<Integer> indirectionLevels = compactDependencies
                  .get(dependencyProtocol);
            sb.append(dependencyProtocol.toString() + ":"
                  + indirectionLevels.toString());
            if (i < dependencyProtocols.length - 1) {
               sb.append(", ");
            }
         }
         sb.append("}\n");
      }
      logger.output(sb.toString());
   }

   private void removeCycles() {
      _dependencyDatabase.removeCycles();
   }

   public void writeGraph(String protocolDependencyGraphInputPath,
         String protocolDependencyGraphOutputPath, BatfishLogger logger) {
      DotJob dotJob = new DotJob(logger);
      DotInput dotInput = getDotInput();
      dotJob.setInput(dotInput);
      dotJob.writeSvg(new File(protocolDependencyGraphInputPath), new File(
            protocolDependencyGraphOutputPath));
   }

}
