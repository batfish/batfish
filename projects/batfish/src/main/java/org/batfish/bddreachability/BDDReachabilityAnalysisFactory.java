package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.batfish.bddreachability.BidirectionalReachabilityReturnPassInstrumentation.instrumentReturnPassEdges;
import static org.batfish.bddreachability.SessionInstrumentation.sessionInstrumentation;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.batfish.bddreachability.transition.Transitions.addLastHopConstraint;
import static org.batfish.bddreachability.transition.Transitions.addNoLastHopConstraint;
import static org.batfish.bddreachability.transition.Transitions.addOriginatingFromDeviceConstraint;
import static org.batfish.bddreachability.transition.Transitions.addSourceInterfaceConstraint;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.eraseAndSet;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.batfish.bddreachability.transition.Transitions.removeLastHopConstraint;
import static org.batfish.bddreachability.transition.Transitions.removeSourceConstraint;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.transformation.TransformationUtil.visitTransformationSteps;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.bdd.MemoizedIpAccessListToBdd;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.IngressInterfaceVrf;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.VrfExprVisitor;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStepVisitor;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.LocationVisitor;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.Accept;
import org.batfish.symbolic.state.DeliveredToSubnet;
import org.batfish.symbolic.state.DropAclIn;
import org.batfish.symbolic.state.DropAclOut;
import org.batfish.symbolic.state.DropNoRoute;
import org.batfish.symbolic.state.DropNullRoute;
import org.batfish.symbolic.state.ExitsNetwork;
import org.batfish.symbolic.state.InsufficientInfo;
import org.batfish.symbolic.state.NeighborUnreachable;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropAclIn;
import org.batfish.symbolic.state.NodeDropAclOut;
import org.batfish.symbolic.state.NodeDropNoRoute;
import org.batfish.symbolic.state.NodeDropNullRoute;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInInterface;
import org.batfish.symbolic.state.PostInVrf;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.PreOutEdge;
import org.batfish.symbolic.state.PreOutEdgePostNat;
import org.batfish.symbolic.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.PreOutInterfaceExitsNetwork;
import org.batfish.symbolic.state.PreOutInterfaceInsufficientInfo;
import org.batfish.symbolic.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.PreOutVrf;
import org.batfish.symbolic.state.Query;
import org.batfish.symbolic.state.StateExpr;
import org.batfish.symbolic.state.VrfAccept;

/**
 * Constructs a the reachability graph for {@link BDDReachabilityAnalysis}. The public API is very
 * simple: it provides two methods for constructing {@link BDDReachabilityAnalysis}, depending on
 * whether or not you have a destination Ip constraint.
 *
 * <p>The core of the implementation is the {@code generateEdges()} method and its many helpers,
 * which generate the {@link StateExpr nodes} and {@link Edge edges} of the reachability graph. Each
 * node represents a step of the routing process within some network device or between devices. The
 * edges represent the flow of traffic between these steps. Each edge is labeled with a {@link BDD}
 * that represents the set of packets that can traverse that edge. If the edge represents a source
 * NAT, the edge will be labeled with the NAT rules (match conditions and set of pool IPs).
 *
 * <p>To support {@link org.batfish.datamodel.acl.MatchSrcInterface} and {@link
 * org.batfish.datamodel.acl.OriginatingFromDevice} {@link
 * org.batfish.datamodel.acl.AclLineMatchExpr ACL expressions}, we maintain the invariant that
 * whenever a packet is inside a node, it has a valid source (according to the BDDSourceManager of
 * that node). For forward edges this is established by constraining to a single source. For
 * backward edges it's established using {@link BDDSourceManager#isValidValue}. When we exit the
 * node (e.g. forward into another node or a disposition state, or backward into another node or an
 * origination state), we erase the contraint on source by existential quantification.
 */
@ParametersAreNonnullByDefault
public final class BDDReachabilityAnalysisFactory {
  // node name --> acl name --> set of packets denied by the acl.
  private final Map<String, Map<String, Supplier<BDD>>> _aclDenyBDDs;

  // node name --> acl name --> set of packets permitted by the acl.
  private final Map<String, Map<String, Supplier<BDD>>> _aclPermitBDDs;

  /*
   * node -> vrf -> edge -> set of packets that vrf will forward out that edge successfully,
   * including that the neighbor will respond to ARP.
   */
  private final Map<String, Map<String, Map<org.batfish.datamodel.Edge, BDD>>> _arpTrueEdgeBDDs;

  /*
   * Symbolic variables corresponding to the different packet header fields. We use these to
   * generate new BDD constraints on those fields. Each constraint can be understood as the set
   * of packet headers for which the constraint is satisfied.
   */
  private final BDDPacket _bddPacket;

  @VisibleForTesting final @Nonnull BDDFibGenerator _bddFibGenerator;

  private final Map<String, BDDSourceManager> _bddSourceManagers;

  // Needed when initializing sessions.
  private final @Nullable LastHopOutgoingInterfaceManager _lastHopMgr;

  // node --> iface --> bdd nats
  private Map<String, Map<String, Transition>> _bddIncomingTransformations;
  private final Map<String, Map<String, Transition>> _bddOutgoingTransformations;

  private final Map<String, Configuration> _configs;

  // only use this for IpSpaces that have no references
  private final IpSpaceToBDD _dstIpSpaceToBDD;
  private final IpSpaceToBDD _srcIpSpaceToBDD;

  private final boolean _ignoreFilters;

  /*
   * node --> vrf --> interface --> set of packets that get routed out the interface but do not
   * reach the neighbor, or exits network, or delivered to subnet
   * This includes neighbor unreachable, exits network, and delivered to subnet
   */
  private final Map<String, Map<String, Map<String, BDD>>> _neighborUnreachableBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _deliveredToSubnetBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _exitsNetworkBDDs;

  private final Map<String, Map<String, Map<String, BDD>>> _insufficientInfoBDDs;

  private final BDD _one;

  private final BDD _requiredTransitNodeBDD;

  // node --> vrf --> set of packets null-routed by the vrf
  private final @Nonnull Map<String, Map<String, BDD>> _nullRoutedBDDs;

  // node --> vrf --> set of packets routable by the vrf
  private final Map<String, Map<String, BDD>> _routableBDDs;

  // conjunction of the BDD vars encoding source and dest IPs/Ports. Used for existential
  // quantification in source and destination NAT.
  private final BDD _dstIpVars;
  private final BDD _sourceIpVars;
  private final BDD _dstPortVars;
  private final BDD _sourcePortVars;

  // Pre-computed conversions from packet policies to BDDs
  private final Map<String, Map<String, PacketPolicyToBdd>> _convertedPacketPolicies;

  private final Set<org.batfish.datamodel.Edge> _topologyEdges;

  // ranges of IPs in all transformations in the network, per IP address field.
  private final Map<IpField, BDD> _transformationIpRanges;

  // ranges of ports in all transformations in the network, per port field.
  private final Map<PortField, BDD> _transformationPortRanges;

  /** node --&gt; vrf --&gt; interface --&gt; set of packets accepted by the interface */
  private final Map<String, Map<String, Map<String, BDD>>> _ifaceAcceptBDDs;

  // node --> vrf --> nextVrf --> set of packets vrf delegates to nextVrf
  private final Map<String, Map<String, Map<String, BDD>>> _nextVrfBDDs;

  // node --> interface --> vrf
  private final Map<String, Map<String, String>> _interfacesToVrfsMap;

  private BDD _zero;

  public BDDReachabilityAnalysisFactory(
      BDDPacket packet,
      Map<String, Configuration> configs,
      ForwardingAnalysis forwardingAnalysis,
      IpsRoutedOutInterfacesFactory ipsRoutedOutInterfacesFactory,
      boolean ignoreFilters,
      boolean initializeSessions) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("Construct BDDReachabilityAnalysisFactory").startActive()) {
      assert span != null; // avoid unused warning
      _bddPacket = packet;
      _one = packet.getFactory().one();
      _zero = packet.getFactory().zero();
      _ignoreFilters = ignoreFilters;
      _topologyEdges =
          forwardingAnalysis.getArpTrueEdge().values().stream()
              .flatMap(m -> m.values().stream())
              .flatMap(m -> m.keySet().stream())
              .collect(ImmutableSet.toImmutableSet());
      _lastHopMgr =
          initializeSessions
              ? new LastHopOutgoingInterfaceManager(packet, configs, _topologyEdges)
              : null;
      _requiredTransitNodeBDD = _bddPacket.allocateBDDBit("requiredTransitNodes");
      _bddSourceManagers = BDDSourceManager.forNetwork(_bddPacket, configs, initializeSessions);
      _configs = configs;
      _dstIpSpaceToBDD = _bddPacket.getDstIpSpaceToBDD();
      _srcIpSpaceToBDD = _bddPacket.getSrcIpSpaceToBDD();

      _aclPermitBDDs = computeAclBDDs(_bddPacket, _bddSourceManagers, configs);
      _aclDenyBDDs = computeAclDenyBDDs(_aclPermitBDDs);

      _bddIncomingTransformations = computeBDDIncomingTransformations();
      _bddOutgoingTransformations = computeBDDOutgoingTransformations();

      _arpTrueEdgeBDDs = computeArpTrueEdgeBDDs(forwardingAnalysis, _dstIpSpaceToBDD);
      _neighborUnreachableBDDs =
          computeDispositionBDDs(forwardingAnalysis.getNeighborUnreachable(), _dstIpSpaceToBDD);
      _deliveredToSubnetBDDs =
          computeDispositionBDDs(forwardingAnalysis.getDeliveredToSubnet(), _dstIpSpaceToBDD);
      _exitsNetworkBDDs =
          computeDispositionBDDs(forwardingAnalysis.getExitsNetwork(), _dstIpSpaceToBDD);
      _insufficientInfoBDDs =
          computeDispositionBDDs(forwardingAnalysis.getInsufficientInfo(), _dstIpSpaceToBDD);
      _nullRoutedBDDs = computeNullRoutedBDDs(forwardingAnalysis, _dstIpSpaceToBDD);
      _routableBDDs = computeRoutableBDDs(forwardingAnalysis, _dstIpSpaceToBDD);
      _ifaceAcceptBDDs =
          computeIfaceAcceptBDDs(configs, forwardingAnalysis.getAcceptsIps(), _dstIpSpaceToBDD);
      _nextVrfBDDs = computeNextVrfBDDs(forwardingAnalysis.getNextVrfIps(), _dstIpSpaceToBDD);
      _interfacesToVrfsMap = computeInterfacesToVrfsMap(configs);

      _convertedPacketPolicies = convertPacketPolicies(configs, ipsRoutedOutInterfacesFactory);

      _dstIpVars = Arrays.stream(_bddPacket.getDstIp().getBitvec()).reduce(_one, BDD::and);
      _sourceIpVars = Arrays.stream(_bddPacket.getSrcIp().getBitvec()).reduce(_one, BDD::and);
      _dstPortVars = Arrays.stream(_bddPacket.getDstPort().getBitvec()).reduce(_one, BDD::and);
      _sourcePortVars = Arrays.stream(_bddPacket.getSrcPort().getBitvec()).reduce(_one, BDD::and);

      RangeComputer rangeComputer = computeTransformationRanges();
      _transformationPortRanges = rangeComputer.getPortRanges();
      _transformationIpRanges = rangeComputer.getIpRanges();

      _bddFibGenerator =
          new BDDFibGenerator(
              _arpTrueEdgeBDDs,
              _neighborUnreachableBDDs,
              _deliveredToSubnetBDDs,
              _exitsNetworkBDDs,
              _insufficientInfoBDDs,
              _ifaceAcceptBDDs,
              _routableBDDs,
              _nextVrfBDDs,
              _nullRoutedBDDs,
              _bddPacket.getFactory()::orAll);
    }
  }

  /**
   * Lazily compute the ACL BDDs, since we may only need some of them (depending on ignoreFilters,
   * forbidden transit nodes, etc). When ignoreFilters is enabled, we still need the ACLs used in
   * NATs. This is simpler than trying to precompute which ACLs we actually need.
   */
  private static Map<String, Map<String, Supplier<BDD>>> computeAclBDDs(
      BDDPacket bddPacket,
      Map<String, BDDSourceManager> bddSourceManagers,
      Map<String, Configuration> configs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeAclBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configs,
          Entry::getKey,
          nodeEntry -> {
            Configuration config = nodeEntry.getValue();
            IpAccessListToBdd aclToBdd =
                ipAccessListToBdd(bddPacket, bddSourceManagers.get(config.getHostname()), config);
            return toImmutableMap(
                config.getIpAccessLists(),
                Entry::getKey,
                aclEntry -> Suppliers.memoize(() -> aclToBdd.toBdd(aclEntry.getValue())));
          });
    }
  }

  Map<String, Map<String, Supplier<BDD>>> getAclPermitBdds() {
    return _aclPermitBDDs;
  }

  IpAccessListToBdd ipAccessListToBdd(Configuration config) {
    return ipAccessListToBdd(_bddPacket, _bddSourceManagers.get(config.getHostname()), config);
  }

  private static IpAccessListToBdd ipAccessListToBdd(
      BDDPacket bddPacket, BDDSourceManager srcMgr, Configuration config) {
    return new IpAccessListToBddImpl(
        bddPacket, srcMgr, config.getIpAccessLists(), config.getIpSpaces());
  }

  private static Map<String, Map<String, Supplier<BDD>>> computeAclDenyBDDs(
      Map<String, Map<String, Supplier<BDD>>> aclBDDs) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeAclDenyBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          aclBDDs,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  aclEntry -> Suppliers.memoize(() -> aclEntry.getValue().get().not())));
    }
  }

  private TransformationToTransition initTransformationToTransformation(Configuration node) {
    return new TransformationToTransition(
        _bddPacket,
        new IpAccessListToBddImpl(
            _bddPacket,
            _bddSourceManagers.get(node.getHostname()),
            new HeaderSpaceToBDD(_bddPacket, node.getIpSpaces()),
            node.getIpAccessLists()));
  }

  private Map<String, Map<String, Transition>> computeBDDIncomingTransformations() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeBDDIncomingTransformations")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _configs,
          Entry::getKey, /* node */
          nodeEntry -> {
            Configuration node = nodeEntry.getValue();
            TransformationToTransition toTransition = initTransformationToTransformation(node);
            return toImmutableMap(
                node.getActiveInterfaces(),
                Entry::getKey, /* iface */
                ifaceEntry ->
                    toTransition.toTransition(ifaceEntry.getValue().getIncomingTransformation()));
          });
    }
  }

  private Map<String, Map<String, Transition>> computeBDDOutgoingTransformations() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeBDDOutgoingTransformations")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          _configs,
          Entry::getKey, /* node */
          nodeEntry -> {
            Configuration node = nodeEntry.getValue();
            TransformationToTransition toTransition = initTransformationToTransformation(node);
            return toImmutableMap(
                nodeEntry.getValue().getActiveInterfaces(),
                Entry::getKey, /* iface */
                ifaceEntry ->
                    toTransition.toTransition(ifaceEntry.getValue().getOutgoingTransformation()));
          });
    }
  }

  private static @Nonnull Map<String, Map<String, BDD>> computeNullRoutedBDDs(
      ForwardingAnalysis forwardingAnalysis, IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeNullRoutedBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          forwardingAnalysis.getNullRoutedIps(),
          Entry::getKey /* hostname */,
          nullRoutedIpsByNodeVrfEntry ->
              toImmutableMap(
                  nullRoutedIpsByNodeVrfEntry.getValue(),
                  Entry::getKey /* vrf */,
                  nullRoutedIpsByVrfEntry ->
                      nullRoutedIpsByVrfEntry.getValue().accept(ipSpaceToBDD)));
    }
  }

  private static Map<String, Map<String, BDD>> computeRoutableBDDs(
      ForwardingAnalysis forwardingAnalysis, IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeRoutableBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          forwardingAnalysis.getRoutableIps(),
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry -> vrfEntry.getValue().accept(ipSpaceToBDD)));
    }
  }

  /** For all configs/interfaces that have PBR policy defined, convert the packet policy to BDDs */
  private Map<String, Map<String, PacketPolicyToBdd>> convertPacketPolicies(
      Map<String, Configuration> configs,
      IpsRoutedOutInterfacesFactory ipsRoutedOutInterfacesFactory) {

    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.convertPacketPolicies")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configs,
          Entry::getKey,
          configEntry ->
              configEntry.getValue().getVrfs().values().stream()
                  .flatMap(
                      vrf -> {
                        IpsRoutedOutInterfaces ipsRoutedOutInterfaces =
                            ipsRoutedOutInterfacesFactory.getIpsRoutedOutInterfaces(
                                configEntry.getKey(), vrf.getName());
                        return configEntry.getValue().getActiveInterfaces(vrf.getName()).values()
                            .stream()
                            .filter(iface -> iface.getRoutingPolicyName() != null)
                            .map(
                                iface ->
                                    Maps.immutableEntry(
                                        iface.getName(),
                                        PacketPolicyToBdd.evaluate(
                                            configEntry
                                                .getValue()
                                                .getPacketPolicies()
                                                .get(iface.getRoutingPolicyName()),
                                            ipAccessListToBdd(configEntry.getValue()),
                                            ipsRoutedOutInterfaces)));
                      })
                  .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));
    }
  }

  IpSpaceToBDD getIpSpaceToBDD() {
    return _dstIpSpaceToBDD;
  }

  Map<String, Map<String, Map<String, BDD>>> getIfaceAcceptBDDs() {
    return _ifaceAcceptBDDs;
  }

  BDD getRequiredTransitNodeBDD() {
    return _requiredTransitNodeBDD;
  }

  private static Map<String, Map<String, Map<org.batfish.datamodel.Edge, BDD>>>
      computeArpTrueEdgeBDDs(ForwardingAnalysis forwardingAnalysis, IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeArpTrueEdgeBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          forwardingAnalysis.getArpTrueEdge(),
          Entry::getKey, // node
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey, // vrf
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue(),
                          Entry::getKey,
                          edgeEntry -> edgeEntry.getValue().accept(ipSpaceToBDD))));
    }
  }

  private static Map<String, Map<String, Map<String, BDD>>> computeDispositionBDDs(
      Map<String, Map<String, Map<String, IpSpace>>> ipSpaceMap, IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeDispositionBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          ipSpaceMap,
          Entry::getKey,
          nodeEntry ->
              toImmutableMap(
                  nodeEntry.getValue(),
                  Entry::getKey,
                  vrfEntry ->
                      toImmutableMap(
                          vrfEntry.getValue(),
                          Entry::getKey,
                          ifaceEntry -> ifaceEntry.getValue().accept(ipSpaceToBDD))));
    }
  }

  private Stream<Edge> generateRootEdges(Map<StateExpr, BDD> rootBdds) {
    return Streams.concat(
        generateRootEdges_OriginateInterfaceLink_PreInInterface(rootBdds),
        generateRootEdges_OriginateVrf_PostInVrf(rootBdds),
        generateRootEdges_OriginateInterface_PostInVrf(rootBdds));
  }

  private static Stream<Edge> generateQueryEdges(Set<FlowDisposition> actions) {
    return actions.stream()
        .map(
            action -> {
              switch (action) {
                case ACCEPTED:
                  return new Edge(Accept.INSTANCE, Query.INSTANCE);
                case DENIED_IN:
                  return new Edge(DropAclIn.INSTANCE, Query.INSTANCE);
                case DENIED_OUT:
                  return new Edge(DropAclOut.INSTANCE, Query.INSTANCE);
                case LOOP:
                  throw new BatfishException("FlowDisposition LOOP is unsupported");
                case NEIGHBOR_UNREACHABLE:
                  return new Edge(NeighborUnreachable.INSTANCE, Query.INSTANCE);
                case DELIVERED_TO_SUBNET:
                  return new Edge(DeliveredToSubnet.INSTANCE, Query.INSTANCE);
                case EXITS_NETWORK:
                  return new Edge(ExitsNetwork.INSTANCE, Query.INSTANCE);
                case INSUFFICIENT_INFO:
                  return new Edge(InsufficientInfo.INSTANCE, Query.INSTANCE);
                case NO_ROUTE:
                  return new Edge(DropNoRoute.INSTANCE, Query.INSTANCE);
                case NULL_ROUTED:
                  return new Edge(DropNullRoute.INSTANCE, Query.INSTANCE);
                default:
                  throw new BatfishException("Unknown FlowDisposition " + action.toString());
              }
            });
  }

  private Stream<Edge> generateRootEdges_OriginateInterfaceLink_PreInInterface(
      Map<StateExpr, BDD> rootBdds) {
    return rootBdds.entrySet().stream()
        .filter(entry -> entry.getKey() instanceof OriginateInterfaceLink)
        .map(
            entry -> {
              OriginateInterfaceLink originateInterfaceLink =
                  (OriginateInterfaceLink) entry.getKey();
              String hostname = originateInterfaceLink.getHostname();
              String iface = originateInterfaceLink.getInterface();
              PreInInterface preInInterface = new PreInInterface(hostname, iface);

              BDD rootBdd = entry.getValue();

              return new Edge(
                  originateInterfaceLink,
                  preInInterface,
                  compose(
                      constraint(rootBdd),
                      addNoLastHopConstraint(_lastHopMgr, hostname, iface),
                      addSourceInterfaceConstraint(_bddSourceManagers.get(hostname), iface)));
            });
  }

  private Stream<Edge> generateRootEdges_OriginateVrf_PostInVrf(Map<StateExpr, BDD> rootBdds) {
    return rootBdds.entrySet().stream()
        .filter(entry -> entry.getKey() instanceof OriginateVrf)
        .map(
            entry -> {
              OriginateVrf originateVrf = (OriginateVrf) entry.getKey();
              String hostname = originateVrf.getHostname();
              String vrf = originateVrf.getVrf();
              PostInVrf postInVrf = new PostInVrf(hostname, vrf);
              BDD rootBdd = entry.getValue();
              return new Edge(
                  originateVrf,
                  postInVrf,
                  compose(
                      addOriginatingFromDeviceConstraint(_bddSourceManagers.get(hostname)),
                      constraint(rootBdd)));
            });
  }

  private Stream<Edge> generateRootEdges_OriginateInterface_PostInVrf(
      Map<StateExpr, BDD> rootBdds) {
    return rootBdds.entrySet().stream()
        .filter(e -> e.getKey() instanceof OriginateInterface)
        .map(
            e -> {
              OriginateInterface state = (OriginateInterface) e.getKey();
              String vrf = _interfacesToVrfsMap.get(state.getHostname()).get(state.getInterface());
              PostInVrf postInVrf = new PostInVrf(state.getHostname(), vrf);
              return new Edge(
                  state,
                  postInVrf,
                  compose(
                      // To transition this edge, flow must originate from
                      // device and have a src IP owned by this interface
                      addOriginatingFromDeviceConstraint(
                          _bddSourceManagers.get(state.getHostname())),
                      constraint(e.getValue())));
            });
  }

  /** Generate edges to each disposition. Depends on final nodes. */
  private Stream<Edge> generateDispositionEdges(Set<String> finalNodes) {
    return Streams.concat(
        generateRules_NodeAccept_Accept(finalNodes),
        generateRules_NodeDropAclIn_DropAclIn(finalNodes),
        generateRules_NodeDropAclOut_DropAclOut(finalNodes),
        generateRules_NodeDropNoRoute_DropNoRoute(finalNodes),
        generateRules_NodeDropNullRoute_DropNullRoute(finalNodes),
        generateRules_NodeInterfaceDeliveredToSubnet_DeliveredToSubnet(finalNodes),
        generateRules_NodeInterfaceExitsNetwork_ExitsNetwork(finalNodes),
        generateRules_NodeInterfaceInsufficientInfo_InsufficientInfo(finalNodes),
        generateRules_NodeInterfaceNeighborUnreachable_NeighborUnreachable(finalNodes));
  }

  /*
   * These edges do not depend on the query. Compute them separately so that we can later cache them
   * across queries if we want to.
   */
  private Stream<Edge> generateEdges() {
    return Streams.concat(
        generateRules_PreInInterface_NodeDropAclIn(),
        generateRules_PreInInterface_NodeDropAclIn_PBR(),
        generateRules_PreInInterface_PostInInterface(),
        generateRules_PreInInterface_PostInVrf_PBR(),
        generateRules_PostInInterface_NodeDropAclIn(),
        generateRules_PostInInterface_PostInVrf(),
        generateRules_PreOutEdge_NodeDropAclOut(),
        generateRules_PreOutEdge_PreOutEdgePostNat(),
        generateRules_PreOutEdgePostNat_NodeDropAclOut(),
        generateRules_PreOutEdgePostNat_PreInInterface(),
        generateRules_PreOutInterfaceDisposition_NodeInterfaceDisposition(),
        generateRules_PreOutInterfaceDisposition_NodeDropAclOut(),
        generateRules_VrfAccept_NodeAccept(),
        generateFibRules());
  }

  private @Nonnull Stream<Edge> generateFibRules() {
    return _bddFibGenerator.generateForwardingEdges(
        alwaysTrue(),
        PostInVrf::new,
        PreOutEdge::new,
        PreOutVrf::new,
        PreOutInterfaceDeliveredToSubnet::new,
        PreOutInterfaceExitsNetwork::new,
        PreOutInterfaceInsufficientInfo::new,
        PreOutInterfaceNeighborUnreachable::new);
  }

  private Stream<Edge> generateRules_NodeAccept_Accept(Set<String> finalNodes) {
    return finalNodes.stream().map(node -> new Edge(new NodeAccept(node), Accept.INSTANCE));
  }

  private static Stream<Edge> generateRules_NodeDropAclIn_DropAclIn(Set<String> finalNodes) {
    return finalNodes.stream().map(node -> new Edge(new NodeDropAclIn(node), DropAclIn.INSTANCE));
  }

  private static Stream<Edge> generateRules_NodeDropAclOut_DropAclOut(Set<String> finalNodes) {
    return finalNodes.stream().map(node -> new Edge(new NodeDropAclOut(node), DropAclOut.INSTANCE));
  }

  private Stream<Edge> generateRules_NodeDropNoRoute_DropNoRoute(Set<String> finalNodes) {
    return finalNodes.stream()
        /* In differential context, nodes can be added or removed. This can lead to a finalNode
         * that doesn't exist in _configs.
         */
        .filter(_configs::containsKey)
        .map(
            node ->
                new Edge(
                    new NodeDropNoRoute(node),
                    DropNoRoute.INSTANCE,
                    compose(
                        removeSourceConstraint(_bddSourceManagers.get(node)),
                        removeLastHopConstraint(_lastHopMgr, node))));
  }

  private static Stream<Edge> generateRules_NodeDropNullRoute_DropNullRoute(
      Set<String> finalNodes) {
    return finalNodes.stream()
        .map(node -> new Edge(new NodeDropNullRoute(node), DropNullRoute.INSTANCE));
  }

  private Stream<Edge> generateRules_NodeInterfaceDisposition_Disposition(
      BiFunction<String, String, StateExpr> nodeInterfaceDispositionConstructor,
      StateExpr dispositionNode,
      Set<String> finalNodes) {
    return finalNodes.stream()
        .map(_configs::get)
        .filter(Objects::nonNull) // remove finalNodes that don't exist on this network
        .flatMap(Configuration::activeInterfaces)
        .map(
            iface -> {
              String node = iface.getOwner().getHostname();
              String ifaceName = iface.getName();
              return new Edge(
                  nodeInterfaceDispositionConstructor.apply(node, ifaceName),
                  dispositionNode,
                  compose(
                      removeSourceConstraint(_bddSourceManagers.get(node)),
                      removeLastHopConstraint(_lastHopMgr, node)));
            });
  }

  private Stream<Edge> generateRules_NodeInterfaceNeighborUnreachable_NeighborUnreachable(
      Set<String> finalNodes) {
    return generateRules_NodeInterfaceDisposition_Disposition(
        NodeInterfaceNeighborUnreachable::new, NeighborUnreachable.INSTANCE, finalNodes);
  }

  private Stream<Edge> generateRules_NodeInterfaceDeliveredToSubnet_DeliveredToSubnet(
      Set<String> finalNodes) {
    return generateRules_NodeInterfaceDisposition_Disposition(
        NodeInterfaceDeliveredToSubnet::new, DeliveredToSubnet.INSTANCE, finalNodes);
  }

  private Stream<Edge> generateRules_NodeInterfaceExitsNetwork_ExitsNetwork(
      Set<String> finalNodes) {
    return generateRules_NodeInterfaceDisposition_Disposition(
        NodeInterfaceExitsNetwork::new, ExitsNetwork.INSTANCE, finalNodes);
  }

  private Stream<Edge> generateRules_NodeInterfaceInsufficientInfo_InsufficientInfo(
      Set<String> finalNodes) {
    return generateRules_NodeInterfaceDisposition_Disposition(
        NodeInterfaceInsufficientInfo::new, InsufficientInfo.INSTANCE, finalNodes);
  }

  private Stream<Edge> generateRules_PostInInterface_NodeDropAclIn() {
    return getInterfaces()
        .filter(iface -> iface.getPostTransformationIncomingFilter() != null)
        .map(
            i -> {
              String acl = i.getPostTransformationIncomingFilterName();
              String node = i.getOwner().getHostname();
              String iface = i.getName();

              BDD aclDenyBDD = ignorableAclDenyBDD(node, acl);
              return new Edge(
                  new PostInInterface(node, iface),
                  new NodeDropAclIn(node),
                  compose(
                      constraint(aclDenyBDD),
                      removeSourceConstraint(_bddSourceManagers.get(node)),
                      removeLastHopConstraint(_lastHopMgr, node)));
            });
  }

  private Stream<Edge> generateRules_PostInInterface_PostInVrf() {
    return getInterfaces()
        .map(
            iface -> {
              String aclName = iface.getPostTransformationIncomingFilterName();
              String nodeName = iface.getOwner().getHostname();
              String vrfName = iface.getVrfName();
              String ifaceName = iface.getName();

              PostInInterface preState = new PostInInterface(nodeName, ifaceName);
              PostInVrf postState = new PostInVrf(nodeName, vrfName);

              BDD inAclBDD = ignorableAclPermitBDD(nodeName, aclName);
              return new Edge(preState, postState, constraint(inAclBDD));
            });
  }

  private Stream<Edge> generateRules_PreInInterface_NodeDropAclIn() {
    return getInterfaces()
        // Policy-based routing rules are generated elsewhere
        .filter(iface -> iface.getRoutingPolicyName() == null && iface.getIncomingFilter() != null)
        .map(
            i -> {
              String acl = i.getIncomingFilterName();
              String node = i.getOwner().getHostname();
              String iface = i.getName();

              BDD aclDenyBDD = ignorableAclDenyBDD(node, acl);
              return new Edge(
                  new PreInInterface(node, iface),
                  new NodeDropAclIn(node),
                  compose(
                      constraint(aclDenyBDD),
                      removeSourceConstraint(_bddSourceManagers.get(node)),
                      removeLastHopConstraint(_lastHopMgr, node)));
            });
  }

  private Stream<Edge> generateRules_PreInInterface_NodeDropAclIn_PBR() {
    return getInterfaces()
        .filter(iface -> iface.getRoutingPolicyName() != null)
        .map(
            i -> {
              String node = i.getOwner().getHostname();
              String iface = i.getName();
              Transition denyTransition = _convertedPacketPolicies.get(node).get(iface).getToDrop();

              return new Edge(
                  new PreInInterface(node, iface),
                  new NodeDropAclIn(node),
                  compose(
                      denyTransition,
                      removeSourceConstraint(_bddSourceManagers.get(node)),
                      removeLastHopConstraint(_lastHopMgr, node)));
            });
  }

  private Stream<Edge> generateRules_PreInInterface_PostInVrf_PBR() {
    return getInterfaces()
        .filter(iface -> iface.getRoutingPolicyName() != null)
        .flatMap(
            iface -> {
              String nodeName = iface.getOwner().getHostname();
              String ifaceName = iface.getName();
              Map<FibLookup, Transition> transitionByFibLookup =
                  _convertedPacketPolicies.get(nodeName).get(ifaceName).getFibLookups();

              PreInInterface preState = new PreInInterface(nodeName, ifaceName);
              return transitionByFibLookup.entrySet().stream()
                  .map(
                      transitionByFibLookupEntry ->
                          new Edge(
                              preState,
                              new PostInVrf(
                                  nodeName,
                                  transitionByFibLookupEntry
                                      .getKey()
                                      .getVrfExpr()
                                      .accept(
                                          new VrfExprVisitor<String>() {
                                            @Override
                                            public String visitLiteralVrfName(LiteralVrfName expr) {
                                              return expr.getVrfName();
                                            }

                                            @Override
                                            public String visitIngressInterfaceVrf(
                                                IngressInterfaceVrf expr) {
                                              return iface.getVrfName();
                                            }
                                          })),
                              transitionByFibLookupEntry.getValue()));
            });
  }

  private BDD aclDenyBDD(String node, @Nullable String acl) {
    return acl == null ? _zero : _aclDenyBDDs.get(node).get(acl).get();
  }

  private BDD aclPermitBDD(String node, @Nullable String acl) {
    return acl == null ? _one : _aclPermitBDDs.get(node).get(acl).get();
  }

  private BDD ignorableAclDenyBDD(String node, @Nullable String acl) {
    return _ignoreFilters ? _zero : aclDenyBDD(node, acl);
  }

  private BDD ignorableAclPermitBDD(String node, @Nullable String acl) {
    return _ignoreFilters ? _one : aclPermitBDD(node, acl);
  }

  private Stream<Edge> generateRules_PreInInterface_PostInInterface() {
    return getInterfaces()
        // Policy-based routing edges handled elsewhere
        .filter(iface -> iface.getRoutingPolicyName() == null)
        .map(
            iface -> {
              String aclName = iface.getIncomingFilterName();
              String nodeName = iface.getOwner().getHostname();
              String ifaceName = iface.getName();

              PreInInterface preState = new PreInInterface(nodeName, ifaceName);
              PostInInterface postState = new PostInInterface(nodeName, ifaceName);

              BDD inAclBDD = ignorableAclPermitBDD(nodeName, aclName);

              Transition transition =
                  compose(
                      constraint(inAclBDD),
                      _bddIncomingTransformations.get(nodeName).get(ifaceName));
              return new Edge(preState, postState, transition);
            });
  }

  private Stream<Edge> generateRules_PreOutEdge_NodeDropAclOut() {
    if (_ignoreFilters) {
      return Stream.of();
    }
    return _topologyEdges.stream()
        .flatMap(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();

              Interface i1 = _configs.get(node1).getAllInterfaces().get(iface1);
              assert i1.getActive();
              String preNatAcl = i1.getPreTransformationOutgoingFilterName();

              BDD denyPreNat = ignorableAclDenyBDD(node1, preNatAcl);
              if (denyPreNat.equals(_zero)) {
                return Stream.of();
              }
              return Stream.of(
                  new Edge(
                      new PreOutEdge(node1, iface1, node2, iface2),
                      new NodeDropAclOut(node1),
                      compose(
                          constraint(denyPreNat),
                          removeSourceConstraint(_bddSourceManagers.get(node1)),
                          removeLastHopConstraint(_lastHopMgr, node1))));
            });
  }

  private Stream<Edge> generateRules_PreOutEdge_PreOutEdgePostNat() {
    return _topologyEdges.stream()
        .flatMap(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();

              Interface i1 = _configs.get(node1).getAllInterfaces().get(iface1);
              assert i1.getActive();
              String preNatAcl = i1.getPreTransformationOutgoingFilterName();

              BDD aclPermit = ignorableAclPermitBDD(node1, preNatAcl);
              if (aclPermit.equals(_zero)) {
                return Stream.of();
              }
              PreOutEdge preState = new PreOutEdge(node1, iface1, node2, iface2);
              PreOutEdgePostNat postState = new PreOutEdgePostNat(node1, iface1, node2, iface2);
              return Stream.of(
                  new Edge(
                      preState,
                      postState,
                      compose(
                          constraint(aclPermit),
                          _bddOutgoingTransformations.get(node1).get(iface1))));
            });
  }

  private Stream<Edge> generateRules_PreOutEdgePostNat_NodeDropAclOut() {
    if (_ignoreFilters) {
      return Stream.of();
    }
    return _topologyEdges.stream()
        .flatMap(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();

              Interface i1 = _configs.get(node1).getAllInterfaces().get(iface1);
              assert i1.getActive();
              String aclName = i1.getOutgoingFilterName();

              if (aclName == null) {
                return Stream.of();
              }

              BDD aclDenyBDD = ignorableAclDenyBDD(node1, aclName);
              return Stream.of(
                  new Edge(
                      new PreOutEdgePostNat(node1, iface1, node2, iface2),
                      new NodeDropAclOut(node1),
                      compose(
                          constraint(aclDenyBDD),
                          removeSourceConstraint(_bddSourceManagers.get(node1)),
                          removeLastHopConstraint(_lastHopMgr, node1))));
            });
  }

  private Stream<Edge> generateRules_PreOutEdgePostNat_PreInInterface() {
    return _topologyEdges.stream()
        .map(
            edge -> {
              String node1 = edge.getNode1();
              String iface1 = edge.getInt1();
              String node2 = edge.getNode2();
              String iface2 = edge.getInt2();

              Interface i1 = _configs.get(node1).getAllInterfaces().get(iface1);
              assert i1.getActive();
              BDD aclPermitBDD = ignorableAclPermitBDD(node1, i1.getOutgoingFilterName());
              assert aclPermitBDD != null;

              return new Edge(
                  new PreOutEdgePostNat(node1, iface1, node2, iface2),
                  new PreInInterface(node2, iface2),
                  compose(
                      constraint(aclPermitBDD),
                      removeLastHopConstraint(_lastHopMgr, node1),
                      removeSourceConstraint(_bddSourceManagers.get(node1)),
                      addSourceInterfaceConstraint(_bddSourceManagers.get(node2), iface2),
                      addLastHopConstraint(_lastHopMgr, node1, iface1, node2, iface2)));
            });
  }

  private Stream<Edge> generateRules_PreOutInterfaceDisposition_NodeDropAclOut() {
    if (_ignoreFilters) {
      return Stream.of();
    }

    return _configs.entrySet().stream()
        .flatMap(
            nodeEntry -> {
              String node = nodeEntry.getKey();
              return nodeEntry.getValue().getVrfs().entrySet().stream()
                  .flatMap(
                      vrfEntry -> {
                        StateExpr postState = new NodeDropAclOut(node);
                        return nodeEntry.getValue().getActiveInterfaces(vrfEntry.getKey()).values()
                            .stream()
                            .filter(iface -> iface.getOutgoingFilterName() != null)
                            .flatMap(
                                iface -> {
                                  String ifaceName = iface.getName();
                                  BDD denyPreAclBDD =
                                      ignorableAclDenyBDD(
                                          node, iface.getPreTransformationOutgoingFilterName());
                                  BDD permitPreAclBDD =
                                      ignorableAclPermitBDD(
                                          node, iface.getPreTransformationOutgoingFilterName());
                                  BDD denyPostAclBDD =
                                      ignorableAclDenyBDD(node, iface.getOutgoingFilterName());
                                  Transition transformation =
                                      _bddOutgoingTransformations.get(node).get(ifaceName);

                                  Transition transition =
                                      compose(
                                          or(
                                              constraint(denyPreAclBDD),
                                              compose(
                                                  constraint(permitPreAclBDD),
                                                  transformation,
                                                  constraint(denyPostAclBDD))),
                                          removeSourceConstraint(_bddSourceManagers.get(node)),
                                          removeLastHopConstraint(_lastHopMgr, node));

                                  return Stream.of(
                                          new PreOutInterfaceDeliveredToSubnet(node, ifaceName),
                                          new PreOutInterfaceExitsNetwork(node, ifaceName),
                                          new PreOutInterfaceInsufficientInfo(node, ifaceName),
                                          new PreOutInterfaceNeighborUnreachable(node, ifaceName))
                                      .map(preState -> new Edge(preState, postState, transition));
                                });
                      });
            });
  }

  @Nonnull
  private Stream<Edge> generateRules_VrfAccept_NodeAccept() {
    return _ifaceAcceptBDDs.entrySet().stream()
        .flatMap(
            nodeEntry -> {
              String hostname = nodeEntry.getKey();
              return nodeEntry.getValue().keySet().stream() // vrf names
                  .map(
                      vrf ->
                          new Edge(
                              new VrfAccept(hostname, vrf),
                              new NodeAccept(hostname),
                              compose(
                                  removeSourceConstraint(_bddSourceManagers.get(hostname)),
                                  removeLastHopConstraint(_lastHopMgr, hostname))));
            });
  }

  @Nonnull
  private static Stream<Edge> getPreOutVrfToDispositionStateEdges(
      Map<String, Map<String, Map<String, BDD>>> dispositionBddMap,
      BiFunction<String, String, StateExpr> stateConstructor) {
    return dispositionBddMap.entrySet().stream()
        .flatMap(
            nodeEntry -> {
              String hostname = nodeEntry.getKey();
              return nodeEntry.getValue().entrySet().stream()
                  .flatMap(
                      vrfEntry -> {
                        String vrfName = vrfEntry.getKey();
                        StateExpr preState = new PreOutVrf(hostname, vrfName);
                        return vrfEntry.getValue().entrySet().stream()
                            .filter(e -> !e.getValue().isZero())
                            .map(
                                ifaceEntry -> {
                                  String ifaceName = ifaceEntry.getKey();
                                  BDD bdd = ifaceEntry.getValue();
                                  return new Edge(
                                      preState, stateConstructor.apply(hostname, ifaceName), bdd);
                                });
                      });
            });
  }

  private Stream<Edge> generateRules_PreOutInterfaceDisposition_NodeInterfaceDisposition() {
    return getInterfaces()
        .flatMap(
            iface -> {
              String node = iface.getOwner().getHostname();
              String ifaceName = iface.getName();
              BDD permitBeforeNatBDD =
                  ignorableAclPermitBDD(node, iface.getPreTransformationOutgoingFilterName());
              BDD permitAfterNatBDD = ignorableAclPermitBDD(node, iface.getOutgoingFilterName());
              Transition outgoingTransformation =
                  _bddOutgoingTransformations.get(node).get(ifaceName);

              if (permitBeforeNatBDD.isZero() || permitAfterNatBDD.isZero()) {
                return Stream.of();
              }

              /* 1. pre-transformation filter
               * 2. outgoing transformation
               * 3. post-transformation filter
               */
              Transition transition =
                  compose(
                      constraint(permitBeforeNatBDD),
                      outgoingTransformation,
                      constraint(permitAfterNatBDD));
              if (transition == ZERO) {
                return Stream.of();
              }

              return Stream.of(
                  new Edge(
                      new PreOutInterfaceDeliveredToSubnet(node, ifaceName),
                      new NodeInterfaceDeliveredToSubnet(node, ifaceName),
                      transition),
                  new Edge(
                      new PreOutInterfaceExitsNetwork(node, ifaceName),
                      new NodeInterfaceExitsNetwork(node, ifaceName),
                      transition),
                  new Edge(
                      new PreOutInterfaceInsufficientInfo(node, ifaceName),
                      new NodeInterfaceInsufficientInfo(node, ifaceName),
                      transition),
                  new Edge(
                      new PreOutInterfaceNeighborUnreachable(node, ifaceName),
                      new NodeInterfaceNeighborUnreachable(node, ifaceName),
                      transition));
            });
  }

  public BDDReachabilityAnalysis bddReachabilityAnalysis(IpSpaceAssignment srcIpSpaceAssignment) {
    return bddReachabilityAnalysis(
        srcIpSpaceAssignment,
        matchDst(UniverseIpSpace.INSTANCE),
        ImmutableSet.of(),
        ImmutableSet.of(),
        _configs.keySet(),
        ImmutableSet.of(FlowDisposition.ACCEPTED));
  }

  public BDDLoopDetectionAnalysis bddLoopDetectionAnalysis(IpSpaceAssignment srcIpSpaceAssignment) {
    Map<StateExpr, BDD> ingressLocationStates = rootConstraints(srcIpSpaceAssignment, _one);
    Stream<Edge> edges = Stream.concat(generateEdges(), generateRootEdges(ingressLocationStates));
    return new BDDLoopDetectionAnalysis(_bddPacket, edges, ingressLocationStates.keySet());
  }

  /**
   * Given a set of parameters finds a {@link Map} of {@link IngressLocation}s to {@link BDD}s while
   * including the results for {@link FlowDisposition#LOOP} if required
   *
   * @param srcIpSpaceAssignment An assignment of active source locations to the corresponding
   *     source {@link IpSpace}.
   * @param initialHeaderSpace The initial headerspace (i.e. before any packet transformations).
   * @param forbiddenTransitNodes A set of hostnames that must not be transited.
   * @param requiredTransitNodes A set of hostnames of which one must be transited.
   * @param finalNodes Find flows that stop at one of these nodes.
   * @param actions Find flows for which at least one trace has one of these actions.
   * @return {@link Map} of {@link IngressLocation}s to {@link BDD}s
   */
  public Map<IngressLocation, BDD> getAllBDDs(
      IpSpaceAssignment srcIpSpaceAssignment,
      AclLineMatchExpr initialHeaderSpace,
      Set<String> forbiddenTransitNodes,
      Set<String> requiredTransitNodes,
      Set<String> finalNodes,
      Set<FlowDisposition> actions) {
    checkArgument(!actions.isEmpty(), "No actions");

    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("BDDReachabilityAnalysisFactory.getAllBDDs").startActive()) {
      assert span != null; // avoid unused warning

      Set<FlowDisposition> nonLoopActions = new HashSet<>(actions);
      boolean loopIncluded = nonLoopActions.remove(LOOP);

      if (nonLoopActions.isEmpty()) {
        // since actions is not empty, loopIncluded must be true. Thus just detect loops
        return bddLoopDetectionAnalysis(srcIpSpaceAssignment).detectLoops();
      } else if (!loopIncluded) {
        // only reachability, no loop detection
        return bddReachabilityAnalysis(
                srcIpSpaceAssignment,
                initialHeaderSpace,
                forbiddenTransitNodes,
                requiredTransitNodes,
                finalNodes,
                nonLoopActions)
            .getIngressLocationReachableBDDs();
      } else {
        // both reachability and loop detection
        return bddReachabilityAndLoopDetectionAnalysis(
                srcIpSpaceAssignment,
                initialHeaderSpace,
                forbiddenTransitNodes,
                requiredTransitNodes,
                finalNodes,
                nonLoopActions)
            .getIngressLocationBdds();
      }
    }
  }

  private BDDReachabilityAndLoopDetectionAnalysis bddReachabilityAndLoopDetectionAnalysis(
      IpSpaceAssignment srcIpSpaceAssignment,
      AclLineMatchExpr initialHeaderSpace,
      Set<String> forbiddenTransitNodes,
      Set<String> requiredTransitNodes,
      Set<String> finalNodes,
      Set<FlowDisposition> actions) {
    BDD initialHeaderSpaceBdd = computeInitialHeaderSpaceBdd(initialHeaderSpace);
    BDD finalHeaderSpaceBdd = computeFinalHeaderSpaceBdd(initialHeaderSpaceBdd);
    Map<StateExpr, BDD> roots = rootConstraints(srcIpSpaceAssignment, initialHeaderSpaceBdd);

    List<Edge> sharedEdges =
        Stream.concat(generateEdges(), generateRootEdges(roots)).collect(Collectors.toList());

    Stream<Edge> reachabilityEdges =
        Streams.concat(
            sharedEdges.stream(),
            generateDispositionEdges(finalNodes),
            generateQueryEdges(actions));
    reachabilityEdges = instrumentForbiddenTransitNodes(forbiddenTransitNodes, reachabilityEdges);
    reachabilityEdges = instrumentRequiredTransitNodes(requiredTransitNodes, reachabilityEdges);

    BDDLoopDetectionAnalysis loopDetectionAnalysis =
        new BDDLoopDetectionAnalysis(_bddPacket, sharedEdges.stream(), roots.keySet());
    BDDReachabilityAnalysis reachabilityAnalysis =
        new BDDReachabilityAnalysis(
            _bddPacket, roots.keySet(), reachabilityEdges, finalHeaderSpaceBdd);

    return new BDDReachabilityAndLoopDetectionAnalysis(reachabilityAnalysis, loopDetectionAnalysis);
  }

  /**
   * Create a {@link BDDReachabilityAnalysis} with the specified parameters.
   *
   * @param srcIpSpaceAssignment An assignment of active source locations to the corresponding
   *     source {@link IpSpace}.
   * @param initialHeaderSpace The initial headerspace (i.e. before any packet transformations).
   * @param forbiddenTransitNodes A set of hostnames that must not be transited.
   * @param requiredTransitNodes A set of hostnames of which one must be transited.
   * @param finalNodes Find flows that stop at one of these nodes.
   * @param actions Find flows for which at least one trace has one of these actions.
   */
  @VisibleForTesting
  public BDDReachabilityAnalysis bddReachabilityAnalysis(
      IpSpaceAssignment srcIpSpaceAssignment,
      AclLineMatchExpr initialHeaderSpace,
      Set<String> forbiddenTransitNodes,
      Set<String> requiredTransitNodes,
      Set<String> finalNodes,
      Set<FlowDisposition> actions) {
    checkArgument(!finalNodes.isEmpty(), "final nodes cannot be empty");
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.bddReachabilityAnalysis")
            .startActive()) {
      assert span != null; // avoid unused warning
      BDD initialHeaderSpaceBdd = computeInitialHeaderSpaceBdd(initialHeaderSpace);
      BDD finalHeaderSpaceBdd = computeFinalHeaderSpaceBdd(initialHeaderSpaceBdd);

      Map<StateExpr, BDD> roots = rootConstraints(srcIpSpaceAssignment, initialHeaderSpaceBdd);

      Stream<Edge> edgeStream =
          Streams.concat(
              generateEdges(),
              generateRootEdges(roots),
              generateDispositionEdges(finalNodes),
              generateQueryEdges(actions));
      edgeStream = instrumentForbiddenTransitNodes(forbiddenTransitNodes, edgeStream);
      edgeStream = instrumentRequiredTransitNodes(requiredTransitNodes, edgeStream);

      return new BDDReachabilityAnalysis(
          _bddPacket, roots.keySet(), edgeStream, finalHeaderSpaceBdd);
    }
  }

  private BDD computeInitialHeaderSpaceBdd(AclLineMatchExpr initialHeaderSpace) {
    IpAccessListToBdd ipAccessListToBdd =
        new MemoizedIpAccessListToBdd(
            _bddPacket, BDDSourceManager.empty(_bddPacket), ImmutableMap.of(), ImmutableMap.of());
    return ipAccessListToBdd.toBdd(initialHeaderSpace);
  }

  /**
   * Compute the space of possible final headers, under the assumption that any NAT rule may be
   * applied.
   */
  @VisibleForTesting
  BDD computeFinalHeaderSpaceBdd(BDD initialHeaderSpaceBdd) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeFinalHeaderSpaceBdd")
            .startActive()) {
      assert span != null; // avoid unused warning
      BDD finalHeaderSpace = initialHeaderSpaceBdd;

      BDD noDstIp = finalHeaderSpace.exist(_dstIpVars);
      if (!noDstIp.equals(finalHeaderSpace)) {
        // there's a constraint on dst Ip, so include nat pool Ips
        BDD dstTransformationRange = _transformationIpRanges.get(IpField.DESTINATION);
        if (dstTransformationRange != null) {
          // dst IP is either the initial one, or one of that NAT pool IPs.
          finalHeaderSpace = finalHeaderSpace.or(noDstIp.and(dstTransformationRange));
        }
      }

      BDD noSrcIp = finalHeaderSpace.exist(_sourceIpVars);
      if (!noSrcIp.equals(finalHeaderSpace)) {
        // there's a constraint on source Ip, so include nat pool Ips
        BDD srcNatPoolIps = _transformationIpRanges.getOrDefault(IpField.SOURCE, _zero);
        if (!srcNatPoolIps.isZero()) {
          /*
           * In this case, since source IPs usually don't play a huge role in routing, we could just
           * existentially quantify away the constraint. There's a performance trade-off: tighter
           * constraints prune more paths, but are more expensive to operate on.
           */
          finalHeaderSpace = finalHeaderSpace.or(noSrcIp.and(srcNatPoolIps));
        }
      }

      BDD noDstPort = finalHeaderSpace.exist(_dstPortVars);
      if (!noDstPort.equals(finalHeaderSpace)) {
        BDD dstTransformationRange = _transformationPortRanges.get(PortField.DESTINATION);
        if (dstTransformationRange != null) {
          finalHeaderSpace = finalHeaderSpace.or(noDstPort.and(dstTransformationRange));
        }
      }

      BDD noSrcPort = finalHeaderSpace.exist(_sourcePortVars);
      if (!noSrcPort.equals(finalHeaderSpace)) {
        BDD srcNatPool = _transformationPortRanges.getOrDefault(PortField.SOURCE, _zero);
        if (!srcNatPool.isZero()) {
          finalHeaderSpace = finalHeaderSpace.or(noSrcPort.and(srcNatPool));
        }
      }

      return finalHeaderSpace;
    }
  }

  /**
   * Create a {@link BDDReachabilityAnalysis} with the given {@link BDDFirewallSessionTraceInfo
   * sessions}.
   *
   * @param returnPassOrigBdds Sets of packets at different locations to originate packets from in
   *     the return pass graph.
   * @param returnPassSuccessBdds Sets of packets at different locations in the return pass graph
   *     that are to be considered successful (and not failure). See {@link
   *     BidirectionalReachabilityReturnPassInstrumentation}.
   */
  BDDReachabilityAnalysis bddReachabilityAnalysis(
      Map<StateExpr, BDD> returnPassOrigBdds,
      Map<StateExpr, BDD> returnPassSuccessBdds,
      Map<String, List<BDDFirewallSessionTraceInfo>> initializedSessions,
      Set<String> forbiddenTransitNodes,
      Set<String> requiredTransitNodes,
      Set<FlowDisposition> dispositions) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.bddReachabilityAnalysis")
            .startActive()) {
      assert span != null; // avoid unused warning

      /* We will use the return pass reachability graph a bit differently than usual: to find flows
       * that successfully return to the origination point of the forward flow, we'll look at states
       * like NodeInterfaceDeliveredToSubnet, rather than adding edges all the way to Query. Also,
       * to find return flows that fail, we add the usual query edges for failure dispositions, and
       * then search backward from Query to the origination points, which are the termination points
       * of the forward pass, and then in the forward reachability graph we'll propagate failing flows
       * from the termination points back to the forward pass origination points.
       *
       * This backward search would work for successful flows as well, but requires an additional
       * graph traversal (two backward searches instead of one forward search). The advantage is that
       * it would work in cases where the forward and return flow don't match (if that's possible).
       */
      Stream<Edge> returnPassEdges =
          Streams.concat(
              instrumentReturnPassEdges(
                  returnPassSuccessBdds,
                  sessionInstrumentation(
                      _bddPacket,
                      _configs,
                      _bddSourceManagers,
                      _lastHopMgr,
                      _aclPermitBDDs,
                      Stream.concat(generateEdges(), generateDispositionEdges(_configs.keySet())),
                      initializedSessions,
                      _bddFibGenerator)),
              generateRootEdges(returnPassOrigBdds),
              generateQueryEdges(dispositions));
      returnPassEdges = instrumentForbiddenTransitNodes(forbiddenTransitNodes, returnPassEdges);
      returnPassEdges = instrumentRequiredTransitNodes(requiredTransitNodes, returnPassEdges);

      return new BDDReachabilityAnalysis(
          _bddPacket, returnPassOrigBdds.keySet(), returnPassEdges, _one);
    }
  }

  class RangeComputer implements TransformationStepVisitor<Void> {
    private Map<IpField, BDD> _ipRanges;
    private Map<PortField, BDD> _portRanges;

    public RangeComputer() {
      _ipRanges = new HashMap<>();
      _portRanges = new HashMap<>();
    }

    public Map<IpField, BDD> getIpRanges() {
      return ImmutableMap.copyOf(_ipRanges);
    }

    public Map<PortField, BDD> getPortRanges() {
      return ImmutableMap.copyOf(_portRanges);
    }

    private IpSpaceToBDD getIpSpaceToBDD(IpField ipField) {
      switch (ipField) {
        case DESTINATION:
          return _dstIpSpaceToBDD;
        case SOURCE:
          return _srcIpSpaceToBDD;
        default:
          throw new IllegalArgumentException("Unknown IpField " + ipField);
      }
    }

    private BDDInteger getPortVar(PortField portField) {
      switch (portField) {
        case DESTINATION:
          return _bddPacket.getDstPort();
        case SOURCE:
          return _bddPacket.getSrcPort();
        default:
          throw new IllegalArgumentException("Unknown PortField " + portField);
      }
    }

    @Override
    public Void visitAssignIpAddressFromPool(AssignIpAddressFromPool assignIpAddressFromPool) {
      IpField ipField = assignIpAddressFromPool.getIpField();
      BDDInteger var = getIpSpaceToBDD(ipField).getBDDInteger();
      BDD bdd =
          assignIpAddressFromPool.getIpRanges().asRanges().stream()
              .map(
                  range ->
                      var.range(range.lowerEndpoint().asLong(), range.upperEndpoint().asLong()))
              .reduce(var.getFactory().zero(), BDD::or);
      _ipRanges.merge(ipField, bdd, BDD::or);
      return null;
    }

    @Override
    public Void visitNoop(Noop noop) {
      return null;
    }

    @Override
    public Void visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet) {
      IpField ipField = shiftIpAddressIntoSubnet.getIpField();
      BDD bdd = getIpSpaceToBDD(ipField).toBDD(shiftIpAddressIntoSubnet.getSubnet());
      _ipRanges.merge(ipField, bdd, BDD::or);
      return null;
    }

    @Override
    public Void visitAssignPortFromPool(AssignPortFromPool assignPortFromPool) {
      PortField portField = assignPortFromPool.getPortField();
      BDDInteger var = getPortVar(portField);
      BDD bdd = var.range(assignPortFromPool.getPoolStart(), assignPortFromPool.getPoolEnd());
      _portRanges.merge(portField, bdd, BDD::or);
      return null;
    }

    @Override
    public Void visitApplyAll(ApplyAll applyAll) {
      applyAll.getSteps().forEach(step -> step.accept(this));
      return null;
    }

    @Override
    public Void visitApplyAny(ApplyAny applyAny) {
      applyAny.getSteps().forEach(step -> step.accept(this));
      return null;
    }
  }

  private RangeComputer computeTransformationRanges() {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeTransformationRanges")
            .startActive()) {
      assert span != null; // avoid unused warning
      RangeComputer rangeComputer = new RangeComputer();
      _configs
          .values()
          .forEach(
              configuration ->
                  configuration
                      .activeInterfaces()
                      .forEach(
                          iface -> {
                            visitTransformationSteps(
                                iface.getIncomingTransformation(), rangeComputer);
                            visitTransformationSteps(
                                iface.getOutgoingTransformation(), rangeComputer);
                          }));
      return rangeComputer;
    }
  }

  Map<StateExpr, BDD> rootConstraints(
      IpSpaceAssignment srcIpSpaceAssignment, BDD initialHeaderSpaceBdd) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.rootConstraints")
            .startActive()) {
      assert span != null; // avoid unused warning
      LocationVisitor<Optional<StateExpr>> locationToStateExpr =
          new LocationToOriginationStateExpr(_configs);
      IpSpaceToBDD srcIpSpaceToBDD = _bddPacket.getSrcIpSpaceToBDD();

      // convert Locations to StateExprs, and merge srcIp constraints
      Map<StateExpr, BDD> rootConstraints = new HashMap<>();
      for (IpSpaceAssignment.Entry entry : srcIpSpaceAssignment.getEntries()) {
        BDD srcIpSpaceBDD = entry.getIpSpace().accept(srcIpSpaceToBDD);
        entry.getLocations().stream()
            .map(locationToStateExpr::visit)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(root -> rootConstraints.merge(root, srcIpSpaceBDD, BDD::or));
      }

      // add the global initial HeaderSpace and remove unsat entries
      Map<StateExpr, BDD> finalRootConstraints =
          rootConstraints.entrySet().stream()
              .map(
                  entry ->
                      Maps.immutableEntry(
                          entry.getKey(), entry.getValue().and(initialHeaderSpaceBdd)))
              .filter(entry -> !entry.getValue().isZero())
              .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));

      // make sure there is at least one possible source
      checkArgument(
          !finalRootConstraints.isEmpty(),
          "No sources are compatible with the headerspace constraint");

      return finalRootConstraints;
    }
  }

  /** Creates mapping of hostname -&gt; interface name -&gt; vrf name for active interfaces */
  private static Map<String, Map<String, String>> computeInterfacesToVrfsMap(
      Map<String, Configuration> configs) {
    return toImmutableMap(
        configs,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue().getActiveInterfaces().values(),
                Interface::getName,
                Interface::getVrfName));
  }

  private static Map<String, Map<String, Map<String, BDD>>> computeIfaceAcceptBDDs(
      Map<String, Configuration> configs,
      Map<String, Map<String, Map<String, IpSpace>>> acceptIps, // hostname -> vrf -> iface -> ips
      IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeIfaceAcceptBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      // Iterate over configs because we want all node/vrf/iface keys to be present in the map
      return toImmutableMap(
          configs,
          Entry::getKey, /* hostname */
          nodeEntry -> {
            String hostname = nodeEntry.getKey();
            Configuration c = nodeEntry.getValue();
            Map<String, Map<String, IpSpace>> nodeAcceptIps =
                acceptIps.getOrDefault(hostname, ImmutableMap.of());
            return toImmutableMap(
                c.getVrfs().keySet(),
                Function.identity(), /* vrf */
                vrf -> {
                  Map<String, IpSpace> vrfAcceptIps =
                      nodeAcceptIps.getOrDefault(vrf, ImmutableMap.of());
                  // Create entry for every interface in the current VRF
                  return c.getAllInterfaces().values().stream()
                      .filter(iface -> iface.getVrfName().equals(vrf))
                      .map(Interface::getName)
                      .collect(
                          ImmutableMap.toImmutableMap(
                              Function.identity(), /* interface */
                              ifaceName ->
                                  vrfAcceptIps
                                      .getOrDefault(ifaceName, EmptyIpSpace.INSTANCE)
                                      .accept(ipSpaceToBDD)));
                });
          });
    }
  }

  private Map<String, Map<String, Map<String, BDD>>> computeNextVrfBDDs(
      Map<String, Map<String, Map<String, IpSpace>>> nextVrfIpsByNodeVrf,
      IpSpaceToBDD ipSpaceToBDD) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.computeNextVrfBDDs")
            .startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          nextVrfIpsByNodeVrf,
          Entry::getKey /* node */,
          nextVrfIpsByNodeVrfEntry ->
              toImmutableMap(
                  nextVrfIpsByNodeVrfEntry.getValue() /* nextVrfIpsByVrf */,
                  Entry::getKey /* vrf */,
                  nextVrfIpsByVrfEntry ->
                      toImmutableMap(
                          nextVrfIpsByVrfEntry.getValue() /* nextVrfIpsByNextVrf */,
                          Entry::getKey,
                          nextVrfIpsByNextVrfEntry ->
                              nextVrfIpsByNextVrfEntry.getValue().accept(ipSpaceToBDD))));
    }
  }

  /**
   * Adapt an edge to set the bit indicating that one of the nodes required to be transited has now
   * been transited.
   *
   * <p>Going forward, we erase the previous value of the bit as we enter the edge, then set it to 1
   * as we exit. Going backward, we just erase the bit, since the requirement has been satisfied.
   */
  @VisibleForTesting
  private Edge adaptEdgeSetTransitedBit(Edge edge) {
    return new Edge(
        edge.getPreState(),
        edge.getPostState(),
        compose(
            edge.getTransition(), eraseAndSet(_requiredTransitNodeBDD, _requiredTransitNodeBDD)));
  }

  /**
   * Adapt an edge, applying an additional constraint after traversing the edge (in the forward
   * direction).
   */
  private static Edge andThen(Edge edge, BDD constraint) {
    return new Edge(
        edge.getPreState(),
        edge.getPostState(),
        compose(edge.getTransition(), constraint(constraint)));
  }

  /**
   * Instrumentation to forbid certain nodes from being transited. Simply removes the edges from the
   * graph at which one of those nodes would become transited.
   */
  private Stream<Edge> instrumentForbiddenTransitNodes(
      Set<String> forbiddenTransitNodes, Stream<Edge> edgeStream) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.instrumentForbiddenTransitNodes")
            .startActive()) {
      assert span != null; // avoid unused warning
      if (forbiddenTransitNodes.isEmpty()) {
        return edgeStream;
      }

      // remove any edges at which a forbidden node becomes transited.
      return edgeStream.filter(
          edge ->
              !(edge.getPreState() instanceof PreOutEdgePostNat
                  && edge.getPostState() instanceof PreInInterface
                  && forbiddenTransitNodes.contains(
                      ((PreOutEdgePostNat) edge.getPreState()).getSrcNode())));
    }
  }

  /**
   * Instrumentation to require that one of a set of nodes is transited. We use a single bit of
   * state in the BDDs to track this. The bit is initialized to 0 at the origination points, and
   * constrained to be 1 at the Query state. When one of the specified nodes becomes transited (i.e.
   * the flow leaves that node and enters another) we set the bit to 1. All other edges in the graph
   * propagate the current value of that bit unchanged.
   */
  private Stream<Edge> instrumentRequiredTransitNodes(
      Set<String> requiredTransitNodes, Stream<Edge> edgeStream) {
    try (ActiveSpan span =
        GlobalTracer.get()
            .buildSpan("BDDReachabilityAnalysisFactory.instrumentRequiredTransitNodes")
            .startActive()) {
      assert span != null; // avoid unused warning
      if (requiredTransitNodes.isEmpty()) {
        return edgeStream;
      }

      BDD transited = _requiredTransitNodeBDD;
      BDD notTransited = _requiredTransitNodeBDD.not();

      return edgeStream.map(
          edge -> {
            if (edge.getPreState() instanceof PreOutEdgePostNat
                && edge.getPostState() instanceof PreInInterface) {
              String hostname = ((PreOutEdgePostNat) edge.getPreState()).getSrcNode();
              return requiredTransitNodes.contains(hostname)
                  ? adaptEdgeSetTransitedBit(edge)
                  : edge;
            } else if (edge.getPreState() instanceof OriginateVrf
                || edge.getPreState() instanceof OriginateInterface
                || edge.getPreState() instanceof OriginateInterfaceLink) {
              return andThen(edge, notTransited);
            } else if (edge.getPostState() instanceof Query) {
              return andThen(edge, transited);
            } else {
              return edge;
            }
          });
    }
  }

  public Map<String, BDDSourceManager> getBDDSourceManagers() {
    return _bddSourceManagers;
  }

  public @Nullable LastHopOutgoingInterfaceManager getLastHopManager() {
    return _lastHopMgr;
  }

  private @Nonnull Stream<Interface> getInterfaces() {
    return _configs.values().stream().flatMap(Configuration::activeInterfaces);
  }
}
