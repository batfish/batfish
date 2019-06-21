package org.batfish.bddreachability;

import com.google.common.collect.Streams;
import io.vavr.Function4;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropNoRoute;
import org.batfish.symbolic.state.NodeDropNullRoute;
import org.batfish.symbolic.state.StateExpr;

/** Module used to generate forwarding {@link Edge}s with configurable pre/postStates */
@ParametersAreNonnullByDefault
public final class BDDFibGenerator {

  /*
   * node -> vrf -> edge -> set of packets that vrf will forward out that edge successfully,
   * including that the neighbor will respond to ARP.
   */
  private final Map<String, Map<String, Map<org.batfish.datamodel.Edge, BDD>>> _arpTrueEdgeBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _deliveredToSubnetBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _exitsNetworkBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _insufficientInfoBDDs;

  /*
   * node --> vrf --> interface --> set of packets that get routed out the interface but do not
   * reach the neighbor, or exits network, or delivered to subnet
   * This includes neighbor unreachable, exits network, and delivered to subnet
   */
  private final Map<String, Map<String, Map<String, BDD>>> _neighborUnreachableBDDs;

  // node --> vrf --> nextVrf --> set of packets vrf delegates to nextVrf
  private final Map<String, Map<String, Map<String, BDD>>> _nextVrfBDDs;

  // node --> vrf --> set of packets null-routed by the vrf
  private final Map<String, Map<String, BDD>> _nullRoutedBDDs;

  // node --> vrf --> set of packets routable by the vrf
  private final Map<String, Map<String, BDD>> _routableBDDs;

  // node --> vrf --> set of packets accepted by the vrf
  private final Map<String, Map<String, BDD>> _vrfAcceptBDDs;

  public BDDFibGenerator(
      Map<String, Map<String, Map<org.batfish.datamodel.Edge, BDD>>> arpTrueEdgeBDDs,
      Map<String, Map<String, Map<String, BDD>>> neighborUnreachableBDDs,
      Map<String, Map<String, Map<String, BDD>>> deliveredToSubnetBDDs,
      Map<String, Map<String, Map<String, BDD>>> exitsNetworkBDDs,
      Map<String, Map<String, Map<String, BDD>>> insufficientInfoBDDs,
      Map<String, Map<String, BDD>> vrfAcceptBDDs,
      Map<String, Map<String, BDD>> routableBDDs,
      Map<String, Map<String, Map<String, BDD>>> nextVrfBDDs,
      Map<String, Map<String, BDD>> nullRoutedBDDs) {
    _arpTrueEdgeBDDs = arpTrueEdgeBDDs;
    _neighborUnreachableBDDs = neighborUnreachableBDDs;
    _deliveredToSubnetBDDs = deliveredToSubnetBDDs;
    _exitsNetworkBDDs = exitsNetworkBDDs;
    _insufficientInfoBDDs = insufficientInfoBDDs;
    _vrfAcceptBDDs = vrfAcceptBDDs;
    _routableBDDs = routableBDDs;
    _nextVrfBDDs = nextVrfBDDs;
    _nullRoutedBDDs = nullRoutedBDDs;
  }

  public @Nonnull Stream<Edge> generateForwardingEdges(
      Predicate<String> includedNode,
      BiFunction<String, String, StateExpr> postInVrf,
      Function4<String, String, String, String, StateExpr> preOutEdge,
      BiFunction<String, String, StateExpr> preOutVrf,
      BiFunction<String, String, StateExpr> preOutInterfaceDeliveredToSubnet,
      BiFunction<String, String, StateExpr> preOutInterfaceExitsNetwork,
      BiFunction<String, String, StateExpr> preOutInterfaceInsufficientInfo,
      BiFunction<String, String, StateExpr> preOutInterfaceNeighborUnreachable) {
    return Streams.concat(
        generateRules_PostInVrf_NodeAccept(includedNode, postInVrf),
        generateRules_PostInVrf_NodeDropNoRoute(includedNode, postInVrf),
        generateRules_PostInVrf_PostInVrf(includedNode, postInVrf),
        generateRules_PostInVrf_PreOutVrf(includedNode, postInVrf, preOutVrf),
        generateRules_PreOutVrf_NodeDropNullRoute(includedNode, preOutVrf),
        generateRules_PreOutVrf_PreOutEdge(includedNode, preOutVrf, preOutEdge),
        generateRules_PreOutVrf_PreOutInterfaceDisposition(
            includedNode,
            preOutVrf,
            preOutInterfaceDeliveredToSubnet,
            preOutInterfaceExitsNetwork,
            preOutInterfaceInsufficientInfo,
            preOutInterfaceNeighborUnreachable));
  }

  private @Nonnull Stream<Edge> generateRules_PostInVrf_NodeAccept(
      Predicate<String> includedNode, BiFunction<String, String, StateExpr> postInVrf) {
    return _vrfAcceptBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().entrySet().stream()
                    .map(
                        vrfEntry -> {
                          String node = nodeEntry.getKey();
                          String vrf = vrfEntry.getKey();
                          BDD acceptBDD = vrfEntry.getValue();
                          return new Edge(
                              postInVrf.apply(node, vrf), new NodeAccept(node), acceptBDD);
                        }));
  }

  private @Nonnull Stream<Edge> generateRules_PostInVrf_NodeDropNoRoute(
      Predicate<String> includedNode, BiFunction<String, String, StateExpr> postInVrf) {
    return _vrfAcceptBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().entrySet().stream()
                    .map(
                        vrfEntry -> {
                          String node = nodeEntry.getKey();
                          String vrf = vrfEntry.getKey();
                          BDD acceptBDD = vrfEntry.getValue();
                          BDD routableBDD = _routableBDDs.get(node).get(vrf);
                          return new Edge(
                              postInVrf.apply(node, vrf),
                              new NodeDropNoRoute(node),
                              acceptBDD.nor(routableBDD));
                        }));
  }

  /**
   * Generate edges from vrf to nextVrf
   */
  private @Nonnull Stream<Edge> generateRules_PostInVrf_PostInVrf(
      Predicate<String> includedNode, BiFunction<String, String, StateExpr> postInVrf) {
    return _nextVrfBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry -> {
              String node = nodeEntry.getKey();
              return nodeEntry.getValue().entrySet().stream()
                  .flatMap(
                      vrfEntry -> {
                        String vrf = vrfEntry.getKey();
                        return vrfEntry.getValue().entrySet().stream()
                            .map(
                                nextVrfEntry -> {
                                  String nextVrf = nextVrfEntry.getKey();
                                  BDD nextVrfBDD = nextVrfEntry.getValue();
                                  return new Edge(
                                      postInVrf.apply(node, vrf),
                                      postInVrf.apply(node, nextVrf),
                                      nextVrfBDD);
                                });
                      });
            });
  }

  private @Nonnull Stream<Edge> generateRules_PostInVrf_PreOutVrf(
      Predicate<String> includedNode,
      BiFunction<String, String, StateExpr> postInVrf,
      BiFunction<String, String, StateExpr> preOutVrf) {
    return _vrfAcceptBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().entrySet().stream()
                    .map(
                        vrfEntry -> {
                          String node = nodeEntry.getKey();
                          String vrf = vrfEntry.getKey();
                          BDD acceptBDD = vrfEntry.getValue();
                          BDD routableBDD = _routableBDDs.get(node).get(vrf);
                          return new Edge(
                              postInVrf.apply(node, vrf),
                              preOutVrf.apply(node, vrf),
                              routableBDD.diff(acceptBDD));
                        }));
  }

  private @Nonnull Stream<Edge> generateRules_PreOutVrf_NodeDropNullRoute(
      Predicate<String> includedNode, BiFunction<String, String, StateExpr> preOutVrf) {
    return _nullRoutedBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().entrySet().stream()
                    .map(
                        vrfEntry -> {
                          String node = nodeEntry.getKey();
                          String vrf = vrfEntry.getKey();
                          BDD nullRoutedBDD = vrfEntry.getValue();
                          return new Edge(
                              preOutVrf.apply(node, vrf),
                              new NodeDropNullRoute(node),
                              nullRoutedBDD);
                        }));
  }

  private @Nonnull Stream<Edge> generateRules_PreOutVrf_PreOutEdge(
      Predicate<String> includedNode,
      BiFunction<String, String, StateExpr> preOutVrf,
      Function4<String, String, String, String, StateExpr> preOutEdge) {
    return _arpTrueEdgeBDDs.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry ->
                nodeEntry.getValue().entrySet().stream()
                    .flatMap(
                        vrfEntry ->
                            vrfEntry.getValue().entrySet().stream()
                                .map(
                                    edgeEntry -> {
                                      org.batfish.datamodel.Edge edge = edgeEntry.getKey();
                                      BDD arpTrue = edgeEntry.getValue();

                                      String node1 = edge.getNode1();
                                      String iface1 = edge.getInt1();
                                      String vrf1 = vrfEntry.getKey();
                                      String node2 = edge.getNode2();
                                      String iface2 = edge.getInt2();

                                      return new Edge(
                                          preOutVrf.apply(node1, vrf1),
                                          preOutEdge.apply(node1, iface1, node2, iface2),
                                          arpTrue);
                                    })));
  }

  private @Nonnull Stream<Edge> generateRules_PreOutVrf_PreOutInterfaceDisposition(
      Predicate<String> includedNode,
      BiFunction<String, String, StateExpr> preOutVrf,
      BiFunction<String, String, StateExpr> preOutInterfaceDeliveredToSubnet,
      BiFunction<String, String, StateExpr> preOutInterfaceExitsNetwork,
      BiFunction<String, String, StateExpr> preOutInterfaceInsufficientInfo,
      BiFunction<String, String, StateExpr> preOutInterfaceNeighborUnreachable) {
    return Streams.concat(
        generateRules_PreOutVrf_PreOutInterfaceDisposition(
            includedNode, _deliveredToSubnetBDDs, preOutVrf, preOutInterfaceDeliveredToSubnet),
        generateRules_PreOutVrf_PreOutInterfaceDisposition(
            includedNode, _exitsNetworkBDDs, preOutVrf, preOutInterfaceExitsNetwork),
        generateRules_PreOutVrf_PreOutInterfaceDisposition(
            includedNode, _insufficientInfoBDDs, preOutVrf, preOutInterfaceInsufficientInfo),
        generateRules_PreOutVrf_PreOutInterfaceDisposition(
            includedNode, _neighborUnreachableBDDs, preOutVrf, preOutInterfaceNeighborUnreachable));
  }

  private static @Nonnull Stream<Edge> generateRules_PreOutVrf_PreOutInterfaceDisposition(
      Predicate<String> includedNode,
      Map<String, Map<String, Map<String, BDD>>> dispositionBddMap,
      BiFunction<String, String, StateExpr> preOutVrfConstructor,
      BiFunction<String, String, StateExpr> preOutInterfaceDispositionConstructor) {
    return dispositionBddMap.entrySet().stream()
        .filter(byNodeEntry -> includedNode.test(byNodeEntry.getKey()))
        .flatMap(
            nodeEntry -> {
              String hostname = nodeEntry.getKey();
              return nodeEntry.getValue().entrySet().stream()
                  .flatMap(
                      vrfEntry -> {
                        String vrfName = vrfEntry.getKey();
                        StateExpr preState = preOutVrfConstructor.apply(hostname, vrfName);
                        return vrfEntry.getValue().entrySet().stream()
                            .filter(e -> !e.getValue().isZero())
                            .map(
                                ifaceEntry -> {
                                  String ifaceName = ifaceEntry.getKey();
                                  BDD bdd = ifaceEntry.getValue();
                                  return new Edge(
                                      preState,
                                      preOutInterfaceDispositionConstructor.apply(
                                          hostname, ifaceName),
                                      bdd);
                                });
                      });
            });
  }
}
