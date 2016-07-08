package org.batfish.protocoldependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchProtocolLine;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.PolicyMapMatchType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpaceList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.GraphvizAnswerElement;
import org.batfish.graphviz.GraphvizDigraph;
import org.batfish.graphviz.GraphvizJob;
import org.batfish.graphviz.GraphvizEdge;
import org.batfish.graphviz.GraphvizInput;
import org.batfish.graphviz.GraphvizNode;
import org.batfish.graphviz.GraphvizResult;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.main.Batfish;

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

   private void collectEdges(Map<Prefix, Set<GraphvizEdge>> prefixEdges,
         Map<String, GraphvizNode> nodes, DependentRoute route) {
      Prefix fromPrefix = route.getPrefix();
      String nodeId = route.getDotNodeId();
      String nodeLabel = getDotNodeLabel(route);
      GraphvizNode node = nodes.get(nodeId);
      if (node == null) {
         node = new GraphvizNode(nodeId,
               GraphvizDigraph.getGraphName(fromPrefix));
         node.setLabel(nodeLabel);
         nodes.put(nodeId, node);
      }
      for (DependentRoute dependency : route.getDependencies()) {
         Prefix toPrefix = dependency.getPrefix();
         String dependencyNodeId = dependency.getDotNodeId();
         String dependencyNodeLabel = getDotNodeLabel(dependency);
         GraphvizNode dependencyNode = nodes.get(dependencyNodeId);
         if (dependencyNode == null) {
            dependencyNode = new GraphvizNode(dependencyNodeId,
                  GraphvizDigraph.getGraphName(toPrefix));
            dependencyNode.setLabel(dependencyNodeLabel);
            nodes.put(dependencyNodeId, dependencyNode);
         }
         GraphvizEdge edge = new GraphvizEdge(node, dependencyNode);
         Set<GraphvizEdge> fromEdges = prefixEdges.get(fromPrefix);
         if (fromEdges == null) {
            fromEdges = new LinkedHashSet<GraphvizEdge>();
            prefixEdges.put(fromPrefix, fromEdges);
         }
         Set<GraphvizEdge> toEdges = prefixEdges.get(toPrefix);
         if (toEdges == null) {
            toEdges = new LinkedHashSet<GraphvizEdge>();
            prefixEdges.put(toPrefix, toEdges);
         }
         fromEdges.add(edge);
         toEdges.add(edge);
         collectEdges(prefixEdges, nodes, dependency);
      }
   }

   private String computeMasterHtmlText(Set<Prefix> prefixes) {
      StringBuilder sb = new StringBuilder();
      sb.append("<!DOCTYPE html>\n");
      sb.append("<html>\n");
      sb.append("<head>\n");
      sb.append(" <title>Protocol dependency analysis</title>\n");
      sb.append(" <style>\n");

      sb.append("  .menu {\n");
      sb.append("   overflow:hidden;\n");
      sb.append("   padding:0;\n");
      sb.append("   margin:0;\n");
      sb.append("   height:100vh;\n");
      sb.append("   width:150px;\n");
      sb.append("   float:left;\n");
      sb.append("  }\n");

      sb.append("  .mainContent {\n");
      sb.append("   overflow:hidden;\n");
      sb.append("   padding:0;\n");
      sb.append("   margin:0;\n");
      sb.append("   height:100vh;\n");
      sb.append("  }\n");

      sb.append("  iframe {\n");
      sb.append("   width:100%;\n");
      sb.append("   height:100%;\n");
      sb.append("   padding:0;\n");
      sb.append("   margin:0;\n");
      sb.append("  }\n");

      sb.append("  body {\n");
      sb.append("   overflow:hidden;\n");
      sb.append("   margin:0;\n");
      sb.append("  }\n");

      sb.append(" </style>\n");
      sb.append("</head>\n");
      sb.append("<body>\n");
      sb.append(" <div class=\"menu\">\n");
      sb.append("  <iframe srcdoc=\"\n\n\n");

      // begin srcdoc
      sb.append("<table>\n");
      for (Prefix prefix : prefixes) {
         String url = GraphvizDigraph.getGraphName(prefix) + ".html";
         sb.append("<tr><td><a href='");
         sb.append(url);
         sb.append("' target='mainframe'>");
         sb.append(prefix.toString());
         sb.append("</a></tr></td>\n");
      }
      sb.append("</table>\n");
      sb.append("</body>\n");
      // end srcdoc

      sb.append("\">\n\n\n</iframe>\n\n\n");
      sb.append(" </div>\n");
      sb.append(" <div class=\"mainContent\">\n");
      sb.append("  <iframe id=\"mainframe\" name=\"mainframe\" srcdoc=\"\">\n");
      sb.append("  </iframe>\n");
      sb.append(" </div>\n");
      sb.append("</body>\n");
      sb.append("</html>\n");
      return sb.toString();
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

   private String getDotNodeLabel(DependentRoute route) {
      return route.getNode() + ":" + route.getProtocol().toString() + ":"
            + route.getPrefix();
   }

   private Map<Prefix, GraphvizInput> getGraphs() {
      Map<Prefix, GraphvizInput> graphs = new HashMap<Prefix, GraphvizInput>();
      Map<Prefix, Set<GraphvizEdge>> prefixEdges = new HashMap<Prefix, Set<GraphvizEdge>>();
      Map<String, GraphvizNode> allNodes = new HashMap<String, GraphvizNode>();
      Set<DependentRoute> routes = _dependencyDatabase.getDependentRoutes();
      Set<Prefix> prefixes = new LinkedHashSet<Prefix>();
      int i = 0;
      for (DependentRoute route : routes) {
         route.setDotNodeId("node" + i);
         i++;
         prefixes.add(route.getPrefix());
      }
      for (DependentRoute route : routes) {
         collectEdges(prefixEdges, allNodes, route);
      }
      for (Entry<Prefix, Set<GraphvizEdge>> e : prefixEdges.entrySet()) {
         Prefix prefix = e.getKey();
         Set<GraphvizEdge> edges = e.getValue();
         GraphvizDigraph graph = new GraphvizDigraph(
               GraphvizDigraph.getGraphName(prefix));
         graphs.put(prefix, graph);
         graph.getEdges().addAll(edges);
         Set<GraphvizNode> nodes = graph.getNodes();
         for (GraphvizEdge edge : edges) {
            GraphvizNode fromNode = edge.getFromNode();
            GraphvizNode toNode = edge.getToNode();
            nodes.add(fromNode);
            nodes.add(toNode);
         }
      }
      return graphs;
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
         RoutingProtocol protocol = originationExport.getDependency()
               .getProtocol();
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
                     outboundPolicy.getName());
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

   public void writeGraphs(Batfish batfish, BatfishLogger logger) {
      Path protocolDependencyGraphPath = batfish.getTestrigSettings()
            .getProtocolDependencyGraphPath();
      CommonUtil.createDirectories(protocolDependencyGraphPath);
      Map<Prefix, GraphvizInput> graphs = getGraphs();
      BatfishJobExecutor<GraphvizJob, GraphvizAnswerElement, GraphvizResult, Map<Path, byte[]>> executor = new BatfishJobExecutor<GraphvizJob, GraphvizAnswerElement, GraphvizResult, Map<Path, byte[]>>(
            batfish.getSettings(), logger);
      Map<Path, byte[]> output = new TreeMap<Path, byte[]>();
      List<GraphvizJob> jobs = new ArrayList<GraphvizJob>();
      for (Entry<Prefix, GraphvizInput> e : graphs.entrySet()) {
         Prefix prefix = e.getKey();
         GraphvizInput input = e.getValue();
         String graphName = GraphvizDigraph.getGraphName(prefix);
         Path graphFile = protocolDependencyGraphPath.resolve("dot").resolve(
               graphName + ".dot");
         Path svgFile = protocolDependencyGraphPath.resolve("svg").resolve(
               graphName + ".svg");
         Path htmlFile = protocolDependencyGraphPath.resolve("html").resolve(
               graphName + ".html");
         GraphvizJob job = new GraphvizJob(batfish.getSettings(), input,
               graphFile, svgFile, htmlFile, prefix);
         jobs.add(job);
      }
      // todo: do something with graphviz answer element
      executor.executeJobs(jobs, output, new GraphvizAnswerElement());
      for (Entry<Path, byte[]> e : output.entrySet()) {
         Path outputPath = e.getKey();
         byte[] outputBytes = e.getValue();
         logger.debug("Writing: \"" + outputPath + "\" ..");
         try {
            Files.write(outputPath, outputBytes);
         }
         catch (IOException ex) {
            throw new BatfishException(
                  "Failed to write graphviz output file: \"" + outputPath
                        + "\"", ex);
         }
         logger.debug("OK\n");
      }
      Path masterHtmlFile = protocolDependencyGraphPath.resolve("html")
            .resolve("index.html");
      Set<Prefix> prefixes = new TreeSet<Prefix>();
      prefixes.addAll(graphs.keySet());
      String masterHtmlText = computeMasterHtmlText(prefixes);
      logger.debug("Writing: \"" + masterHtmlFile + "\" ..");
      CommonUtil.writeFile(masterHtmlFile, masterHtmlText);
      logger.debug("OK\n");
   }

}
