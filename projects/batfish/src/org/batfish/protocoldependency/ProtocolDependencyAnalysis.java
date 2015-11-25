package org.batfish.protocoldependency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.LineAction;
import org.batfish.representation.OspfArea;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchType;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;
import org.batfish.util.SubRange;

/**
 * TODO: ospfe1
 */
public class ProtocolDependencyAnalysis {

   private Map<String, Configuration> _configurations;

   private final DependencyDatabase _dependencyDatabase;

   @SuppressWarnings("unused")
   private BatfishLogger _logger;

   @SuppressWarnings("unused")
   private Settings _settings;

   public ProtocolDependencyAnalysis(Map<String, Configuration> configurations,
         Settings settings, BatfishLogger logger) {
      _configurations = configurations;
      _settings = settings;
      _logger = logger;
      _dependencyDatabase = new DependencyDatabase(_configurations);
   }

   private void cleanDatabase() {
      _dependencyDatabase.clearPotentialExports();
      _dependencyDatabase.clearPotentialImports();
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
            Prefix prefix = iface.getPrefix();
            if (prefix != null) {
               RoutingProtocol protocol = RoutingProtocol.CONNECTED;
               DependentRoute dependentRoute = new DependentRoute(node, prefix,
                     protocol);
               _dependencyDatabase.addDependentRoute(dependentRoute);
            }
         }
      }
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
               for (PolicyMap origination : originationPolicies) {
                  for (PolicyMapClause clause : origination.getClauses()) {
                     if (clause.getAction() == PolicyMapAction.PERMIT) {
                        Set<RoutingProtocol> protocols = new HashSet<RoutingProtocol>();
                        Set<PrefixRange> prefixRanges = new HashSet<PrefixRange>();
                        boolean foundMatchProtocol = false;
                        boolean foundMatchRouteFilter = false;
                        for (PolicyMapMatchLine matchLine : clause
                              .getMatchLines()) {
                           if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
                              foundMatchProtocol = true;
                              PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
                              protocols.add(matchProtocolLine.getProtocol());
                           }
                        }
                        if (!foundMatchProtocol) {
                           protocols.addAll(Arrays.asList(RoutingProtocol
                                 .values()));
                           protocols.remove(RoutingProtocol.BGP);
                           protocols.remove(RoutingProtocol.IBGP);
                        }
                        for (PolicyMapMatchLine matchLine : clause
                              .getMatchLines()) {
                           if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                              foundMatchRouteFilter = true;
                              PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                              for (RouteFilterList list : matchRouteFilterLine
                                    .getLists()) {
                                 for (RouteFilterLine line : list.getLines()) {
                                    Prefix prefix = line.getPrefix();
                                    SubRange lengthRange = line
                                          .getLengthRange();
                                    if (line.getAction() == LineAction.ACCEPT) {
                                       prefixRanges.add(new PrefixRange(prefix,
                                             lengthRange));
                                    }
                                 }
                              }
                           }
                        }
                        if (!foundMatchRouteFilter) {
                           prefixRanges.add(new PrefixRange(Prefix.ZERO,
                                 new SubRange(0, 32)));
                        }
                        Set<DependentRoute> dependentRoutes = _dependencyDatabase
                              .getDependentRoutes(node);
                        for (DependentRoute dependentRoute : dependentRoutes) {
                           Prefix prefix = dependentRoute.getPrefix();
                           if (protocols.contains(dependentRoute.getProtocol())) {
                              for (PrefixRange prefixRange : prefixRanges) {
                                 if (prefixRange.includesPrefix(prefix)) {
                                    PotentialExport potentialExport = new EbgpPotentialExport(
                                          node, prefix, dependentRoute);
                                    _dependencyDatabase
                                          .addPotentialExport(potentialExport);
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
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
               for (PolicyMap origination : originationPolicies) {
                  for (PolicyMapClause clause : origination.getClauses()) {
                     if (clause.getAction() == PolicyMapAction.PERMIT) {
                        Set<RoutingProtocol> protocols = new HashSet<RoutingProtocol>();
                        Set<PrefixRange> prefixRanges = new HashSet<PrefixRange>();
                        boolean foundMatchProtocol = false;
                        boolean foundMatchRouteFilter = false;
                        for (PolicyMapMatchLine matchLine : clause
                              .getMatchLines()) {
                           if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
                              foundMatchProtocol = true;
                              PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
                              protocols.add(matchProtocolLine.getProtocol());
                           }
                        }
                        if (!foundMatchProtocol) {
                           protocols.addAll(Arrays.asList(RoutingProtocol
                                 .values()));
                           protocols.remove(RoutingProtocol.BGP);
                           protocols.remove(RoutingProtocol.IBGP);
                        }
                        for (PolicyMapMatchLine matchLine : clause
                              .getMatchLines()) {
                           if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                              foundMatchRouteFilter = true;
                              PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                              for (RouteFilterList list : matchRouteFilterLine
                                    .getLists()) {
                                 for (RouteFilterLine line : list.getLines()) {
                                    Prefix prefix = line.getPrefix();
                                    SubRange lengthRange = line
                                          .getLengthRange();
                                    if (line.getAction() == LineAction.ACCEPT) {
                                       prefixRanges.add(new PrefixRange(prefix,
                                             lengthRange));
                                    }
                                 }
                              }
                           }
                        }
                        if (!foundMatchRouteFilter) {
                           prefixRanges.add(new PrefixRange(Prefix.ZERO,
                                 new SubRange(0, 32)));
                        }
                        Set<DependentRoute> dependentRoutes = _dependencyDatabase
                              .getDependentRoutes(node);
                        for (DependentRoute dependentRoute : dependentRoutes) {
                           Prefix prefix = dependentRoute.getPrefix();
                           if (protocols.contains(dependentRoute.getProtocol())) {
                              for (PrefixRange prefixRange : prefixRanges) {
                                 if (prefixRange.includesPrefix(prefix)) {
                                    PotentialExport potentialExport = new IbgpPotentialExport(
                                          node, prefix, dependentRoute);
                                    _dependencyDatabase
                                          .addPotentialExport(potentialExport);
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
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
      for (Configuration c : _configurations.values()) {
         String node = c.getHostname();
         if (c.getOspfProcess() != null) {
            for (PolicyMap outboundPolicy : c.getOspfProcess()
                  .getOutboundPolicyMaps()) {
               for (PolicyMapClause clause : outboundPolicy.getClauses()) {
                  if (clause.getAction() == PolicyMapAction.PERMIT) {
                     Set<RoutingProtocol> protocols = new HashSet<RoutingProtocol>();
                     Set<PrefixRange> prefixRanges = new HashSet<PrefixRange>();
                     boolean foundMatchProtocol = false;
                     boolean foundMatchRouteFilter = false;
                     for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                        if (matchLine.getType() == PolicyMapMatchType.PROTOCOL) {
                           foundMatchProtocol = true;
                           PolicyMapMatchProtocolLine matchProtocolLine = (PolicyMapMatchProtocolLine) matchLine;
                           protocols.add(matchProtocolLine.getProtocol());
                        }
                     }
                     if (!foundMatchProtocol) {
                        protocols
                              .addAll(Arrays.asList(RoutingProtocol.values()));
                        protocols.remove(RoutingProtocol.OSPF);
                        protocols.remove(RoutingProtocol.OSPF_E1);
                        protocols.remove(RoutingProtocol.OSPF_E2);
                        protocols.remove(RoutingProtocol.OSPF_IA);
                     }
                     for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                        if (matchLine.getType() == PolicyMapMatchType.ROUTE_FILTER_LIST) {
                           foundMatchRouteFilter = true;
                           PolicyMapMatchRouteFilterListLine matchRouteFilterLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                           for (RouteFilterList list : matchRouteFilterLine
                                 .getLists()) {
                              for (RouteFilterLine line : list.getLines()) {
                                 Prefix prefix = line.getPrefix();
                                 SubRange lengthRange = line.getLengthRange();
                                 if (line.getAction() == LineAction.ACCEPT) {
                                    prefixRanges.add(new PrefixRange(prefix,
                                          lengthRange));
                                 }
                              }
                           }
                        }
                     }
                     if (!foundMatchRouteFilter) {
                        prefixRanges.add(new PrefixRange(Prefix.ZERO,
                              new SubRange(0, 32)));
                     }
                     Set<DependentRoute> dependentRoutes = _dependencyDatabase
                           .getDependentRoutes(node);
                     for (DependentRoute dependentRoute : dependentRoutes) {
                        Prefix prefix = dependentRoute.getPrefix();
                        if (protocols.contains(dependentRoute.getProtocol())) {
                           for (PrefixRange prefixRange : prefixRanges) {
                              if (prefixRange.includesPrefix(prefix)) {
                                 OspfE2PotentialExport potentialExport = new OspfE2PotentialExport(
                                       node, prefix, dependentRoute);
                                 _dependencyDatabase
                                       .addPotentialExport(potentialExport);
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void initPotentialOspfExternalImports() {
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
                     Prefix prefix = iface.getPrefix();
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
                  Prefix prefix = iface.getPrefix();
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

   private void initStaticNextHopRoutes() {
      RoutingProtocol protocol = RoutingProtocol.STATIC;
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         Set<StaticRoute> routesToCheck = new LinkedHashSet<StaticRoute>();
         String node = e.getKey();
         Configuration c = e.getValue();
         routesToCheck.addAll(c.getStaticRoutes());
         boolean workDone;
         do {
            workDone = false;
            Set<StaticRoute> failedRoutes = new LinkedHashSet<StaticRoute>();
            for (StaticRoute staticRoute : routesToCheck) {
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
                     failedRoutes.add(staticRoute);
                  }
               }
            }
            routesToCheck = failedRoutes;
         } while (workDone);
      }
   }

   public void run() {
      // One and done
      initConnectedRoutes();
      initStaticInterfaceRoutes();
      initPotentialIgpExports();
      initPotentialIgpImports();
      initIgpRoutes();
      cleanDatabase();

      // First iteration
      initStaticNextHopRoutes();
      initPotentialOspfExternalExports();
      initPotentialOspfExternalImports();
      initPotentialEbgpOriginationExports();
      initPotentialEbgpImports();

      initOspfExternalRoutes();

      initPotentialIbgpOriginationExports();
      initPotentialIbgpImports();
      initBgpRoutes();

      // second iteration
      // resolveImports();
      assert Boolean.TRUE;
   }

}
