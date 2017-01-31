package org.batfish.bdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.batfish.common.plugin.DataPlanePlugin;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteBuilder;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.BdpAnswerElement;
import org.batfish.datamodel.collections.AdvertisementSet;
import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.IbgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.collections.RouteSet;

public class BdpDataPlanePlugin extends DataPlanePlugin {

   private final Map<BdpDataPlane, Map<Flow, Set<FlowTrace>>> _flowTraces;

   public BdpDataPlanePlugin() {
      _flowTraces = new HashMap<>();
   }

   private void collectFlowTraces(BdpDataPlane dp, String currentNodeName,
         Set<Edge> visitedEdges, List<FlowTraceHop> hopsSoFar,
         Set<FlowTrace> flowTraces, Flow flow) {
      Ip dstIp = flow.getDstIp();
      Set<String> ipOwners = dp._ipOwners.get(dstIp);
      if (ipOwners != null && ipOwners.contains(currentNodeName)) {
         FlowTrace trace = new FlowTrace(FlowDisposition.ACCEPTED, hopsSoFar,
               FlowDisposition.ACCEPTED.toString());
         flowTraces.add(trace);
      }
      else {
         Node currentNode = dp._nodes.get(currentNodeName);
         String vrfName;
         if (hopsSoFar.isEmpty()) {
            vrfName = flow.getIngressVrf();
         }
         else {
            FlowTraceHop lastHop = hopsSoFar.get(hopsSoFar.size() - 1);
            String receivingInterface = lastHop.getEdge().getInt2();
            vrfName = currentNode._c.getInterfaces().get(receivingInterface)
                  .getVrf().getName();
         }
         VirtualRouter currentVirtualRouter = currentNode._virtualRouters
               .get(vrfName);
         Map<AbstractRoute, Set<String>> nextHopInterfacesByRoute = currentVirtualRouter._fib
               .getNextHopInterfacesByRoute(dstIp);
         Map<String, Set<AbstractRoute>> nextHopInterfacesWithRoutes = currentVirtualRouter._fib
               .getNextHopInterfaces(dstIp);
         if (!nextHopInterfacesWithRoutes.isEmpty()) {
            for (String nextHopInterfaceName : nextHopInterfacesWithRoutes
                  .keySet()) {
               // SortedSet<String> routesForThisNextHopInterface = new
               // TreeSet<>(
               // nextHopInterfacesWithRoutes.get(nextHopInterfaceName)
               // .stream().map(ar -> ar.toString())
               // .collect(Collectors.toSet()));
               SortedSet<String> routesForThisNextHopInterface = new TreeSet<>();
               boolean nextHopIpRoute = false;
               for (Entry<AbstractRoute, Set<String>> e : nextHopInterfacesByRoute
                     .entrySet()) {
                  AbstractRoute routeCandidate = e.getKey();
                  Set<String> routeCandidateNextHopInterfaces = e.getValue();
                  if (routeCandidateNextHopInterfaces
                        .contains(nextHopInterfaceName)) {
                     Ip nextHopIp = routeCandidate.getNextHopIp();
                     if (nextHopIp != null && !nextHopIp
                           .equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
                        nextHopIpRoute = true;
                     }
                     routesForThisNextHopInterface
                           .add(routeCandidate.toString());
                  }
               }
               NodeInterfacePair nextHopInterface = new NodeInterfacePair(
                     currentNodeName, nextHopInterfaceName);
               if (nextHopInterfaceName.equals(Interface.NULL_INTERFACE_NAME)) {
                  List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
                  Edge newEdge = new Edge(nextHopInterface,
                        new NodeInterfacePair(Configuration.NODE_NONE_NAME,
                              Interface.NULL_INTERFACE_NAME));
                  FlowTraceHop newHop = new FlowTraceHop(newEdge,
                        routesForThisNextHopInterface);
                  newHops.add(newHop);
                  FlowTrace nullRouteTrace = new FlowTrace(
                        FlowDisposition.NULL_ROUTED, newHops,
                        FlowDisposition.NULL_ROUTED.toString());
                  flowTraces.add(nullRouteTrace);
               }
               else if (dp._flowSinks.contains(nextHopInterface)) {
                  List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
                  Edge newEdge = new Edge(nextHopInterface,
                        new NodeInterfacePair(Configuration.NODE_NONE_NAME,
                              Interface.FLOW_SINK_TERMINATION_NAME));
                  FlowTraceHop newHop = new FlowTraceHop(newEdge,
                        routesForThisNextHopInterface);
                  newHops.add(newHop);
                  FlowTrace flowSinkTrace = new FlowTrace(
                        FlowDisposition.ACCEPTED, newHops,
                        FlowDisposition.ACCEPTED.toString());
                  flowTraces.add(flowSinkTrace);
               }
               else {
                  EdgeSet edges = dp._topology.getInterfaceEdges()
                        .get(nextHopInterface);
                  if (edges != null) {
                     int unreachableNeighbors = 0;
                     int potentialNeighbors = 0;
                     for (Edge edge : edges) {
                        if (!edge.getNode1().equals(currentNodeName)) {
                           continue;
                        }
                        potentialNeighbors++;
                        List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
                        Set<Edge> newVisitedEdges = new LinkedHashSet<>(
                              visitedEdges);
                        FlowTraceHop newHop = new FlowTraceHop(edge,
                              routesForThisNextHopInterface);
                        newVisitedEdges.add(edge);
                        newHops.add(newHop);
                        /*
                         * Check to see whether neighbor would refrain from
                         * sending ARP reply (NEIGHBOR_UNREACHABLE)
                         *
                         * This occurs if:
                         *
                         * - Using interface-only route
                         *
                         * AND
                         *
                         * - Neighbor does not own dstIp
                         *
                         * AND EITHER
                         *
                         * -- Neighbor not using proxy-arp
                         *
                         * - OR
                         *
                         * -- Subnet of neighbor's receiving-interface contains
                         * dstIp
                         */
                        if (!nextHopIpRoute) {
                           // using interface-only route
                           String node2 = edge.getNode2();
                           if (ipOwners == null || !ipOwners.contains(node2)) {
                              // neighbor does not own dstIp
                              String int2Name = edge.getInt2();
                              Interface int2 = dp._nodes.get(node2)._c
                                    .getInterfaces().get(int2Name);
                              boolean neighborUnreachable = false;
                              Boolean proxyArp = int2.getProxyArp();
                              if (proxyArp == null || !proxyArp) {
                                 // TODO: proxyArp probably shouldn't be null
                                 neighborUnreachable = true;
                              }
                              else {
                                 for (Prefix prefix : int2.getAllPrefixes()) {
                                    if (prefix.getNetworkPrefix()
                                          .contains(dstIp)) {
                                       neighborUnreachable = true;
                                       break;
                                    }
                                 }
                              }
                              if (neighborUnreachable) {
                                 unreachableNeighbors++;
                                 continue;
                              }
                           }
                        }

                        if (visitedEdges.contains(edge)) {
                           FlowTrace trace = new FlowTrace(FlowDisposition.LOOP,
                                 newHops, FlowDisposition.LOOP.toString());
                           flowTraces.add(trace);
                           potentialNeighbors--;
                           continue;
                        }
                        String nextNodeName = edge.getNode2();
                        // now check output filter and input filter
                        IpAccessList outFilter = dp._nodes
                              .get(currentNodeName)._c.getInterfaces()
                                    .get(nextHopInterfaceName)
                                    .getOutgoingFilter();
                        if (outFilter != null) {
                           FlowDisposition disposition = FlowDisposition.DENIED_OUT;
                           boolean denied = flowTraceDeniedHelper(flowTraces,
                                 flow, newHops, outFilter, disposition);
                           if (denied) {
                              potentialNeighbors--;
                              continue;
                           }
                        }
                        IpAccessList inFilter = dp._nodes.get(nextNodeName)._c
                              .getInterfaces().get(edge.getInt2())
                              .getIncomingFilter();
                        if (inFilter != null) {
                           FlowDisposition disposition = FlowDisposition.DENIED_IN;
                           boolean denied = flowTraceDeniedHelper(flowTraces,
                                 flow, newHops, inFilter, disposition);
                           if (denied) {
                              potentialNeighbors--;
                              continue;
                           }
                        }
                        // recurse
                        collectFlowTraces(dp, nextNodeName, newVisitedEdges,
                              newHops, flowTraces, flow);
                     }
                     if (unreachableNeighbors > 0
                           && unreachableNeighbors == potentialNeighbors) {
                        FlowTrace trace = neighborUnreachableTrace(hopsSoFar,
                              nextHopInterface, routesForThisNextHopInterface);
                        flowTraces.add(trace);
                        continue;
                     }
                  }
                  else {
                     // Should only get here for delta environment where
                     // non-flow-sink interface from base has no edges in delta
                     FlowTrace trace = neighborUnreachableTrace(hopsSoFar,
                           nextHopInterface, routesForThisNextHopInterface);
                     flowTraces.add(trace);
                  }
               }
            }
         }
         else {
            FlowTrace trace = new FlowTrace(FlowDisposition.NO_ROUTE, hopsSoFar,
                  FlowDisposition.NO_ROUTE.toString());
            flowTraces.add(trace);
         }
      }
   }

   @Override
   public Answer computeDataPlane(boolean differentialContext) {
      Answer answer = new Answer();
      BdpDataPlane dp = new BdpDataPlane();
      BdpAnswerElement ae = new BdpAnswerElement();
      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Topology topology = _batfish.computeTopology(configurations);
      _batfish.resetTimer();
      _logger.info("\n*** COMPUTING DATA PLANE ***\n");
      Map<Ip, Set<String>> ipOwners = _batfish.computeIpOwners(configurations);
      dp.initIpOwners(configurations, ipOwners);
      _batfish.initRemoteBgpNeighbors(configurations, dp._ipOwners);
      Map<String, Node> nodes = new TreeMap<>();
      configurations.values()
            .forEach(c -> nodes.put(c.getHostname(), new Node(c, nodes)));
      AdvertisementSet externalAdverts = _batfish
            .processExternalBgpAnnouncements(configurations);
      computeFixedPoint(nodes, topology, dp, externalAdverts, ae);
      computeFibs(nodes);
      dp.setNodes(nodes);
      dp.setTopology(topology);
      dp.setFlowSinks(_batfish.computeFlowSinks(configurations,
            differentialContext, topology));
      _batfish.writeDataPlane(dp);
      _batfish.printElapsedTime();
      answer.addAnswerElement(ae);
      return answer;
   }

   private void computeFibs(Map<String, Node> nodes) {
      nodes.values().parallelStream().forEach(n -> {
         for (VirtualRouter vr : n._virtualRouters.values()) {
            vr.computeFib();
         }
      });
   }

   private void computeFixedPoint(Map<String, Node> nodes, Topology topology,
         BdpDataPlane dp, AdvertisementSet externalAdverts,
         BdpAnswerElement ae) {
      // BEGIN DONE ONCE (except main rib)
      // connected, initial static routes, ospf setup, bgp setup
      AtomicInteger initialCompleted = _batfish.newBatch(
            "Compute initial connected and static routes, ospf setup, bgp setup",
            nodes.size());
      nodes.values().parallelStream().forEach(n -> {
         for (VirtualRouter vr : n._virtualRouters.values()) {
            vr.initConnectedRib();
            vr.importRib(vr._independentRib, vr._connectedRib);
            vr.importRib(vr._mainRib, vr._connectedRib);
            vr.initStaticRib();
            vr.importRib(vr._independentRib, vr._staticInterfaceRib);
            vr.importRib(vr._mainRib, vr._staticInterfaceRib);
            vr.initOspfInterfaceCosts();
            vr.initBaseOspfRoutes();
            vr.initEbgpTopology(dp);
            vr.initBaseBgpRibs(externalAdverts);
         }
         initialCompleted.incrementAndGet();
      });

      final Object routesChangedMonitor = new Object();

      // OSPF internal routes
      final boolean[] ospfInternalChanged = new boolean[] { true };
      int ospfInternalIterations = 0;
      while (ospfInternalChanged[0]) {
         ospfInternalIterations++;
         ospfInternalChanged[0] = false;
         AtomicInteger ospfInternalCompleted = _batfish
               .newBatch("Compute OSPF Internal routes: iteration "
                     + ospfInternalIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               if (vr.propagateOspfInternalRoutes(nodes, topology)) {
                  synchronized (routesChangedMonitor) {
                     ospfInternalChanged[0] = true;
                  }
               }
            }
            ospfInternalCompleted.incrementAndGet();
         });
         AtomicInteger ospfInternalUnstageCompleted = _batfish
               .newBatch("Unstage OSPF Internal routes: iteration "
                     + ospfInternalIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               vr.unstageOspfInternalRoutes();
            }
            ospfInternalUnstageCompleted.incrementAndGet();
         });
      }
      AtomicInteger ospfInternalImportCompleted = _batfish
            .newBatch("Import OSPF Internal routes", nodes.size());
      nodes.values().parallelStream().forEach(n -> {
         for (VirtualRouter vr : n._virtualRouters.values()) {
            vr.importRib(vr._ospfRib, vr._ospfIntraAreaRib);
            vr.importRib(vr._ospfRib, vr._ospfInterAreaRib);
            vr.importRib(vr._independentRib, vr._ospfRib);
         }
         ospfInternalImportCompleted.incrementAndGet();
      });
      // END DONE ONCE

      boolean[] dependentRoutesChanged = new boolean[] { true };
      int dependentRoutesIterations = 0;
      SortedMap<Integer, Integer> bgpIterations = new TreeMap<>();
      while (dependentRoutesChanged[0]) {
         dependentRoutesIterations++;
         dependentRoutesChanged[0] = false;
         // (Re)initialization of dependent route calculation
         AtomicInteger reinitializeDependentCompleted = _batfish
               .newBatch("Reinitialize dependent routes: iteration "
                     + dependentRoutesIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {

               /*
                * RIBs that are read from
                */
               vr._prevMainRib = vr._mainRib;
               vr._mainRib = new Rib();

               vr._prevOspfExternalType1Rib = vr._ospfExternalType1Rib;
               vr._ospfExternalType1Rib = new OspfExternalType1Rib();

               vr._prevOspfExternalType2Rib = vr._ospfExternalType2Rib;
               vr._ospfExternalType2Rib = new OspfExternalType2Rib();

               vr._prevBgpRib = vr._bgpRib;
               vr._bgpRib = new BgpRib();

               vr._prevEbgpRib = vr._ebgpRib;
               vr._ebgpRib = new BgpRib();
               vr.importRib(vr._ebgpRib, vr._baseEbgpRib);

               vr._prevIbgpRib = vr._ibgpRib;
               vr._ibgpRib = new BgpRib();
               vr.importRib(vr._ibgpRib, vr._baseIbgpRib);

               /*
                * RIBs not read from
                */
               vr._ospfRib = new OspfRib();

               /*
                * Staging RIBs
                */
               vr._ebgpStagingRib = new BgpRib();
               vr._ibgpStagingRib = new BgpRib();
               vr._ospfExternalType1StagingRib = new OspfExternalType1Rib();
               vr._ospfExternalType2StagingRib = new OspfExternalType2Rib();

               /*
                * Add routes that cannot change (does not affect below
                * computation)
                */
               vr.importRib(vr._mainRib, vr._independentRib);

               /*
                * Re-add independent OSPF routes to ospfRib for tie-breaking
                */
               vr.importRib(vr._ospfRib, vr._ospfIntraAreaRib);
               vr.importRib(vr._ospfRib, vr._ospfInterAreaRib);

            }
            reinitializeDependentCompleted.incrementAndGet();
         });

         // Static nextHopIp routes
         AtomicInteger recomputeStaticCompleted = _batfish
               .newBatch("Recompute static routes with next-hop IP: iteration "
                     + dependentRoutesIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            boolean staticChanged = true;
            while (staticChanged) {
               for (VirtualRouter vr : n._virtualRouters.values()) {
                  staticChanged = false;
                  if (vr.activateStaticRoutes()) {
                     staticChanged = true;
                  }
               }
            }
            recomputeStaticCompleted.incrementAndGet();
         });

         // Generated/aggregate routes
         AtomicInteger recomputeAggregateCompleted = _batfish
               .newBatch("Recompute aggregate/generated routes: iteration "
                     + dependentRoutesIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               boolean generatedChanged = true;
               vr._generatedRib = new Rib();
               while (generatedChanged) {
                  generatedChanged = false;
                  if (vr.activateGeneratedRoutes()) {
                     generatedChanged = true;
                  }
               }
               vr.importRib(vr._mainRib, vr._generatedRib);
            }
            recomputeAggregateCompleted.incrementAndGet();
         });

         // OSPF external routes
         // recompute exports
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               vr.initOspfExports();
            }
         });

         // repropagate exports
         final boolean[] ospfExternalChanged = new boolean[] { true };
         int ospfExternalSubIterations = 0;
         while (ospfExternalChanged[0]) {
            ospfExternalSubIterations++;
            AtomicInteger propagateOspfExternalCompleted = _batfish
                  .newBatch("Propagate OSPF external routes: iteration "
                        + dependentRoutesIterations + ", subIteration: "
                        + ospfExternalSubIterations, nodes.size());
            ospfExternalChanged[0] = false;
            nodes.values().parallelStream().forEach(n -> {
               for (VirtualRouter vr : n._virtualRouters.values()) {
                  if (vr.propagateOspfExternalRoutes(nodes, topology)) {
                     synchronized (routesChangedMonitor) {
                        ospfExternalChanged[0] = true;
                     }
                  }
               }
               propagateOspfExternalCompleted.incrementAndGet();
            });
            AtomicInteger unstageOspfExternalCompleted = _batfish
                  .newBatch("Unstage OSPF external routes: iteration "
                        + dependentRoutesIterations + ", subIteration: "
                        + ospfExternalSubIterations, nodes.size());
            nodes.values().parallelStream().forEach(n -> {
               for (VirtualRouter vr : n._virtualRouters.values()) {
                  vr.unstageOspfExternalRoutes();
               }
               unstageOspfExternalCompleted.incrementAndGet();
            });
         }
         AtomicInteger importOspfExternalCompleted = _batfish
               .newBatch("Unstage OSPF external routes: iteration "
                     + dependentRoutesIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               vr.importRib(vr._ospfRib, vr._ospfExternalType1Rib);
               vr.importRib(vr._ospfRib, vr._ospfExternalType2Rib);
               vr.importRib(vr._mainRib, vr._ospfRib);
            }
            importOspfExternalCompleted.incrementAndGet();
         });

         // BGP routes
         // first let's initialize nodes-level generated/aggregate routes
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               vr.initBgpAggregateRoutes();
            }
         });
         final boolean[] bgpChanged = new boolean[] { true };
         int currentBgpIterations = 0;
         while (bgpChanged[0]) {
            currentBgpIterations++;
            bgpChanged[0] = false;
            AtomicInteger propagateBgpCompleted = _batfish.newBatch(
                  "Propagate BGP routes: iteration " + dependentRoutesIterations
                        + ", subIteration: " + currentBgpIterations,
                  nodes.size());
            nodes.values().parallelStream().forEach(n -> {
               for (VirtualRouter vr : n._virtualRouters.values()) {
                  if (vr.propagateBgpRoutes(nodes, topology)) {
                     synchronized (routesChangedMonitor) {
                        bgpChanged[0] = true;
                     }
                  }
               }
               propagateBgpCompleted.incrementAndGet();
            });
            AtomicInteger importBgpCompleted = _batfish.newBatch(
                  "Import BGP routes: iteration " + dependentRoutesIterations
                        + ", subIteration: " + currentBgpIterations,
                  nodes.size());
            nodes.values().parallelStream().forEach(n -> {
               for (VirtualRouter vr : n._virtualRouters.values()) {
                  vr.unstageBgpRoutes();
                  vr.importRib(vr._bgpRib, vr._ebgpRib);
                  vr.importRib(vr._bgpRib, vr._ibgpRib);
                  vr.importRib(vr._mainRib, vr._bgpRib);
               }
               importBgpCompleted.incrementAndGet();
            });
         }
         bgpIterations.put(dependentRoutesIterations, currentBgpIterations);

         // Check to see if routes have changed
         AtomicInteger checkFixedPointCompleted = _batfish
               .newBatch("Check if fixed-point reached: iteration "
                     + dependentRoutesIterations, nodes.size());
         nodes.values().parallelStream().forEach(n -> {
            for (VirtualRouter vr : n._virtualRouters.values()) {
               boolean changed = false;
               if (!vr._mainRib.getRoutes()
                     .equals(vr._prevMainRib.getRoutes())) {
                  changed = true;
               }
               if (!vr._ospfExternalType1Rib.getRoutes()
                     .equals(vr._prevOspfExternalType1Rib.getRoutes())) {
                  changed = true;
               }
               if (!vr._ospfExternalType2Rib.getRoutes()
                     .equals(vr._prevOspfExternalType2Rib.getRoutes())) {
                  changed = true;
               }
               if (changed) {
                  synchronized (routesChangedMonitor) {
                     dependentRoutesChanged[0] = true;
                  }
               }
            }
            checkFixedPointCompleted.incrementAndGet();
         });
      }
      int totalRoutes = nodes.values().stream()
            .flatMap(n -> n._virtualRouters.values().stream())
            .mapToInt(vr -> vr._mainRib.getRoutes().size()).sum();
      ae.setOspfInternalIterations(ospfInternalIterations);
      ae.setDependentRoutesIterations(dependentRoutesIterations);
      ae.setBgpIterations(bgpIterations);
      ae.setTotalRoutes(totalRoutes);
   }

   private boolean flowTraceDeniedHelper(Set<FlowTrace> flowTraces, Flow flow,
         List<FlowTraceHop> newHops, IpAccessList filter,
         FlowDisposition disposition) {
      boolean out = disposition == FlowDisposition.DENIED_OUT;
      FilterResult outResult = filter.filter(flow);
      boolean denied = outResult.getAction() == LineAction.REJECT;
      if (denied) {
         String outFilterName = filter.getName();
         Integer matchLine = outResult.getMatchLine();
         String lineDesc;
         if (matchLine != null) {
            lineDesc = filter.getLines().get(matchLine).getName();
            if (lineDesc == null) {
               lineDesc = "line:" + matchLine.toString();
            }
         }
         else {
            lineDesc = "no-match";
         }
         String notes = disposition.toString() + "{" + outFilterName + "}{"
               + lineDesc + "}";
         if (out) {
            FlowTraceHop lastHop = newHops.get(newHops.size() - 1);
            newHops.remove(newHops.size() - 1);
            Edge lastEdge = lastHop.getEdge();
            Edge deniedOutEdge = new Edge(lastEdge.getFirst(),
                  new NodeInterfacePair(Configuration.NODE_NONE_NAME,
                        Interface.NULL_INTERFACE_NAME));
            FlowTraceHop deniedOutHop = new FlowTraceHop(deniedOutEdge,
                  lastHop.getRoutes());
            newHops.add(deniedOutHop);
         }
         FlowTrace trace = new FlowTrace(disposition, newHops, notes);
         flowTraces.add(trace);
      }
      return denied;
   }

   @Override
   public AdvertisementSet getAdvertisements() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public List<Flow> getHistoryFlows() {
      BdpDataPlane dp = loadDataPlane();
      List<Flow> flowList = new ArrayList<>();
      _flowTraces.get(dp).forEach((flow, flowTraces) -> {
         for (int i = 0; i < flowTraces.size(); i++) {
            flowList.add(flow);
         }
      });
      return flowList;
   }

   @Override
   public List<FlowTrace> getHistoryFlowTraces() {
      BdpDataPlane dp = loadDataPlane();
      List<FlowTrace> flowTraceList = new ArrayList<>();
      _flowTraces.get(dp).forEach((flow, flowTraces) -> {
         for (FlowTrace flowTrace : flowTraces) {
            flowTraceList.add(flowTrace);
         }
      });
      return flowTraceList;
   }

   @Override
   public IbgpTopology getIbgpNeighbors() {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   @Override
   public RouteSet getRoutes() {
      BdpDataPlane dp = loadDataPlane();
      Map<Ip, String> ipOwners = dp.getIpOwnersSimple();
      RouteSet outputRoutes = new RouteSet();
      dp.getNodes().forEach((hostname, node) -> {
         node._virtualRouters.forEach((vrName, vr) -> {
            for (AbstractRoute route : vr._mainRib.getRoutes()) {
               RouteBuilder rb = new RouteBuilder();
               rb.setNode(hostname);
               rb.setNetwork(route.getNetwork());
               if (route.getProtocol() == RoutingProtocol.CONNECTED
                     || (route.getProtocol() == RoutingProtocol.STATIC
                           && route.getNextHopIp() == null)
                     || Interface.NULL_INTERFACE_NAME
                           .equals(route.getNextHopInterface())) {
                  rb.setNextHop(Configuration.NODE_NONE_NAME);
               }
               Ip nextHopIp = route.getNextHopIp();
               if (nextHopIp != null) {
                  rb.setNextHopIp(nextHopIp);
                  String nextHop = ipOwners.get(nextHopIp);
                  if (nextHop != null) {
                     rb.setNextHop(nextHop);
                  }
               }
               String nextHopInterface = route.getNextHopInterface();
               if (nextHopInterface != null) {
                  rb.setNextHopInterface(nextHopInterface);
               }
               rb.setAdministrativeCost(route.getAdministrativeCost());
               rb.setCost(route.getMetric());
               rb.setProtocol(route.getProtocol());
               rb.setTag(route.getTag());
               rb.setVrf(vrName);
               Route outputRoute = rb.build();
               outputRoutes.add(outputRoute);
            }
         });
      });
      return outputRoutes;
   }

   private BdpDataPlane loadDataPlane() {
      return (BdpDataPlane) _batfish.loadDataPlane();
   }

   private FlowTrace neighborUnreachableTrace(List<FlowTraceHop> completedHops,
         NodeInterfacePair srcInterface, SortedSet<String> routes) {
      Edge neighborUnreachbleEdge = new Edge(srcInterface,
            new NodeInterfacePair(Configuration.NODE_NONE_NAME,
                  Interface.NULL_INTERFACE_NAME));
      FlowTraceHop neighborUnreachableHop = new FlowTraceHop(
            neighborUnreachbleEdge, routes);
      List<FlowTraceHop> newHops = new ArrayList<>(completedHops);
      newHops.add(neighborUnreachableHop);
      FlowTrace trace = new FlowTrace(
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK, newHops,
            FlowDisposition.NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK.toString());
      return trace;
   }

   @Override
   public void processFlows(Set<Flow> flows) {
      BdpDataPlane dp = loadDataPlane();
      Map<Flow, Set<FlowTrace>> flowTraces = new ConcurrentHashMap<>();
      flows.parallelStream().forEach(flow -> {
         Set<FlowTrace> currentFlowTraces = new TreeSet<>();
         flowTraces.put(flow, currentFlowTraces);
         String ingressNodeName = flow.getIngressNode();
         Set<Edge> visitedEdges = Collections.emptySet();
         List<FlowTraceHop> hops = new ArrayList<>();
         collectFlowTraces(dp, ingressNodeName, visitedEdges, hops,
               currentFlowTraces, flow);
      });
      _flowTraces.put(dp, new TreeMap<>(flowTraces));
   }

}
