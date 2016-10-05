package org.batfish.bdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.batfish.common.BatfishException;
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
         List<FlowTraceHop> hopsSoFar, Set<FlowTrace> flowTraces, Flow flow) {
      Ip dstIp = flow.getDstIp();
      Set<String> ipOwners = dp._ipOwners.get(dstIp);
      if (ipOwners != null && ipOwners.contains(currentNodeName)) {
         FlowTrace trace = new FlowTrace(FlowDisposition.ACCEPTED, hopsSoFar,
               FlowDisposition.ACCEPTED.toString());
         flowTraces.add(trace);
      }
      else {
         Node currentNode = dp._nodes.get(currentNodeName);
         Map<AbstractRoute, Set<String>> nextHopInterfacesByRoute = currentNode._fib
               .getNextHopInterfacesByRoute(dstIp);
         Map<String, Set<AbstractRoute>> nextHopInterfacesWithRoutes = currentNode._fib
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
               nextHopInterfacesByRoute.forEach(
                     (routeCandidate, routeCandidateNextHopInterfaces) -> {
                        if (routeCandidateNextHopInterfaces
                              .contains(nextHopInterfaceName)) {
                           routesForThisNextHopInterface
                                 .add(routeCandidate.toString());
                        }
                     });
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
                     for (Edge edge : edges) {
                        if (!edge.getNode1().equals(currentNodeName)) {
                           continue;
                        }
                        List<FlowTraceHop> newHops = new ArrayList<>(hopsSoFar);
                        FlowTraceHop newHop = new FlowTraceHop(edge,
                              routesForThisNextHopInterface);
                        newHops.add(newHop);
                        String nextNodeName = edge.getNode2();
                        if (nextNodeName
                              .equals(newHops.get(0).getEdge().getNode1())) {
                           throw new BatfishException("Loop!");
                        }
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
                              continue;
                           }
                        }
                        // recurse
                        collectFlowTraces(dp, nextNodeName, newHops, flowTraces,
                              flow);
                     }
                  }
                  else {
                     throw new BatfishException(
                           "Should not be sent out non-flow sink interface with no edges: "
                                 + nextHopInterface);
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
            .forEach(c -> nodes.put(c.getHostname(), new Node(c)));
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
         n.computeFib();
      });
   }

   private void computeFixedPoint(Map<String, Node> nodes, Topology topology,
         BdpDataPlane dp, AdvertisementSet externalAdverts,
         BdpAnswerElement ae) {
      // BEGIN DONE ONCE (except main rib)
      // connected, initial static routes, ospf setup, bgp setup
      nodes.values().parallelStream().forEach(n -> {
         n.initPolicyOwners();
         n.initConnectedRib();
         n.importRib(n._independentRib, n._connectedRib);
         n.importRib(n._mainRib, n._connectedRib);
         n.initStaticRib();
         n.importRib(n._independentRib, n._staticInterfaceRib);
         n.importRib(n._mainRib, n._staticInterfaceRib);
         n.initOspfInterfaceCosts();
         n.initBaseOspfRoutes();
         n.initEbgpTopology(dp);
         n.initBaseBgpRibs(externalAdverts);
      });

      final Object routesChangedMonitor = new Object();

      // OSPF internal routes
      final boolean[] ospfInternalChanged = new boolean[] { true };
      int ospfInternalIterations = 0;
      while (ospfInternalChanged[0]) {
         ospfInternalIterations++;
         ospfInternalChanged[0] = false;
         nodes.values().parallelStream().forEach(n -> {
            if (n.propagateOspfInternalRoutes(nodes, topology)) {
               synchronized (routesChangedMonitor) {
                  ospfInternalChanged[0] = true;
               }
            }
         });
         nodes.values().parallelStream().forEach(n -> {
            n.unstageOspfInternalRoutes();
         });
      }
      nodes.values().parallelStream().forEach(n -> {
         n.importRib(n._ospfRib, n._ospfIntraAreaRib);
         n.importRib(n._ospfRib, n._ospfInterAreaRib);
         n.importRib(n._independentRib, n._ospfRib);
      });
      // END DONE ONCE

      boolean[] dependentRoutesChanged = new boolean[] { true };
      int dependentRoutesIterations = 0;
      SortedMap<Integer, Integer> bgpIterations = new TreeMap<>();
      while (dependentRoutesChanged[0]) {
         dependentRoutesIterations++;
         dependentRoutesChanged[0] = false;
         // (Re)initialization of dependent route calculation
         nodes.values().parallelStream().forEach(n -> {
            /*
             * RIBs that are read from
             */
            n._prevMainRib = n._mainRib;
            n._mainRib = new Rib();

            n._prevOspfExternalType1Rib = n._ospfExternalType1Rib;
            n._ospfExternalType1Rib = new OspfExternalType1Rib();

            n._prevOspfExternalType2Rib = n._ospfExternalType2Rib;
            n._ospfExternalType2Rib = new OspfExternalType2Rib();

            n._prevBgpRib = n._bgpRib;
            n._bgpRib = new BgpRib();

            n._prevEbgpRib = n._ebgpRib;
            n._ebgpRib = new BgpRib();
            n.importRib(n._ebgpRib, n._baseEbgpRib);

            n._prevIbgpRib = n._ibgpRib;
            n._ibgpRib = new BgpRib();
            n.importRib(n._ebgpRib, n._baseIbgpRib);

            /*
             * RIBs not read from
             */
            n._ospfRib = new OspfRib();

            /*
             * Staging RIBs
             */
            n._ebgpStagingRib = new BgpRib();
            n._ibgpStagingRib = new BgpRib();
            n._ospfExternalType1StagingRib = new OspfExternalType1Rib();
            n._ospfExternalType2StagingRib = new OspfExternalType2Rib();

            /*
             * Add routes that cannot change (does not affect below computation)
             */
            n.importRib(n._mainRib, n._independentRib);

            /*
             * Re-add independent OSPF routes to ospfRib for tie-breaking
             */
            n.importRib(n._ospfRib, n._ospfIntraAreaRib);
            n.importRib(n._ospfRib, n._ospfInterAreaRib);

         });

         // Static nextHopIp routes
         nodes.values().parallelStream().forEach(n -> {
            boolean staticChanged = true;
            while (staticChanged) {
               staticChanged = false;
               if (n.activateStaticRoutes()) {
                  staticChanged = true;
               }
            }
         });

         // Generated/aggregate routes
         nodes.values().parallelStream().forEach(n -> {
            boolean generatedChanged = true;
            n._generatedRib = new Rib();
            while (generatedChanged) {
               generatedChanged = false;
               if (n.activateGeneratedRoutes()) {
                  generatedChanged = true;
               }
            }
            n.importRib(n._mainRib, n._generatedRib);
         });

         // OSPF external routes
         // recompute exports
         nodes.values().parallelStream().forEach(n -> {
            n.initOspfExports();
         });

         // repropagate exports
         final boolean[] ospfExternalChanged = new boolean[] { true };
         while (ospfExternalChanged[0]) {
            ospfExternalChanged[0] = false;
            nodes.values().parallelStream().forEach(n -> {
               if (n.propagateOspfExternalRoutes(nodes, topology)) {
                  synchronized (routesChangedMonitor) {
                     ospfExternalChanged[0] = true;
                  }
               }
            });
            nodes.values().parallelStream().forEach(n -> {
               n.unstageOspfExternalRoutes();
            });
         }
         nodes.values().parallelStream().forEach(n -> {
            n.importRib(n._ospfRib, n._ospfExternalType1Rib);
            n.importRib(n._ospfRib, n._ospfExternalType2Rib);
            n.importRib(n._mainRib, n._ospfRib);
         });

         // BGP routes
         // first let's initialize nodes-level generated/aggregate routes
         nodes.values().parallelStream().forEach(n -> {
            n.initBgpAggregateRoutes();
         });
         final boolean[] bgpChanged = new boolean[] { true };
         int currentBgpIterations = 0;
         while (bgpChanged[0]) {
            currentBgpIterations++;
            bgpChanged[0] = false;
            nodes.values().parallelStream().forEach(n -> {
               if (n.propagateBgpRoutes(nodes, topology)) {
                  synchronized (routesChangedMonitor) {
                     bgpChanged[0] = true;
                  }
               }
            });
            nodes.values().parallelStream().forEach(n -> {
               n.unstageBgpRoutes();
               n.importRib(n._bgpRib, n._ebgpRib);
               n.importRib(n._bgpRib, n._ibgpRib);
               n.importRib(n._mainRib, n._bgpRib);
            });
         }
         bgpIterations.put(dependentRoutesIterations, currentBgpIterations);

         // Check to see if routes have changed
         nodes.values().parallelStream().forEach(n -> {
            boolean changed = false;
            if (!n._mainRib.getRoutes().equals(n._prevMainRib.getRoutes())) {
               changed = true;
            }
            if (!n._ospfExternalType1Rib.getRoutes()
                  .equals(n._prevOspfExternalType1Rib.getRoutes())) {
               changed = true;
            }
            if (!n._ospfExternalType2Rib.getRoutes()
                  .equals(n._prevOspfExternalType2Rib.getRoutes())) {
               changed = true;
            }
            if (changed) {
               synchronized (routesChangedMonitor) {
                  dependentRoutesChanged[0] = true;
               }
            }
         });
      }
      int totalRoutes = nodes.values().stream()
            .mapToInt(n -> n._mainRib.getRoutes().size()).sum();
      ae.setOspfInternalIterations(ospfInternalIterations);
      ae.setDependentRoutesIterations(dependentRoutesIterations);
      ae.setBgpIterations(bgpIterations);
      ae.setTotalRoutes(totalRoutes);
   }

   private boolean flowTraceDeniedHelper(Set<FlowTrace> flowTraces, Flow flow,
         List<FlowTraceHop> newEdges, IpAccessList filter,
         FlowDisposition disposition) {
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
         FlowTrace trace = new FlowTrace(disposition, newEdges, notes);
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
         for (AbstractRoute route : node._mainRib.getRoutes()) {
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
            Route outputRoute = rb.build();
            outputRoutes.add(outputRoute);
         }
      });
      return outputRoutes;
   }

   private BdpDataPlane loadDataPlane() {
      return (BdpDataPlane) _batfish.loadDataPlane();
   }

   @Override
   public void processFlows(Set<Flow> flows) {
      BdpDataPlane dp = loadDataPlane();
      Map<Flow, Set<FlowTrace>> flowTraces = new ConcurrentHashMap<>();
      flows.parallelStream().forEach(flow -> {
         Set<FlowTrace> currentFlowTraces = new TreeSet<>();
         flowTraces.put(flow, currentFlowTraces);
         String ingressNodeName = flow.getIngressNode();
         List<FlowTraceHop> edges = new ArrayList<>();
         collectFlowTraces(dp, ingressNodeName, edges, currentFlowTraces, flow);
      });
      _flowTraces.put(dp, new TreeMap<>(flowTraces));
   }

}
