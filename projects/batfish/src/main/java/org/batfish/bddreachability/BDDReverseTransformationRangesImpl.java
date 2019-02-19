package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.bdd.BDDUtils.swapPairing;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.PreInInterface;

/** Implemention of valid ranges (outputs) of reversed transformations (for reverse flows). */
final class BDDReverseTransformationRangesImpl implements BDDReverseTransformationRanges {
  private final Map<String, Configuration> _configs;

  // null when _ignoreFilters is true.
  private final @Nullable Map<String, Map<String, BDD>> _forwardFlowFilterBdds;
  private final Map<StateExpr, BDD> _forwardReachableSets;
  private final Map<NodeInterfacePair, BDD> _incomingCache;
  private final Map<NodeInterfacePair, BDD> _outgoingCache;

  /**
   * The StateExprs that collectively whose reachable sets (unioned together) define the range of a
   * reversed outgoing transformation.
   */
  private final Multimap<NodeInterfacePair, StateExpr> _outgoingTransformationRangeStates;

  /**
   * A pairing for converting constraints on forward flows to constraints on reverse flows. It swaps
   * all src/dst variables.
   */
  private final BDDPairing _reverseFlowPairing;

  private final BDD _one;
  private final BDD _zero;

  public BDDReverseTransformationRangesImpl(
      Map<String, Configuration> configs,
      Map<StateExpr, BDD> forwardReachableSets,
      BDDPacket bddPacket,
      @Nullable Map<String, Map<String, BDD>> forwardFlowFilterBdds) {
    _configs = configs;
    _forwardReachableSets = forwardReachableSets;
    _forwardFlowFilterBdds = forwardFlowFilterBdds;
    _incomingCache = new HashMap<>();
    _outgoingCache = new HashMap<>();
    _outgoingTransformationRangeStates =
        computeOutgoingTransformationRangeStates(forwardReachableSets.keySet());
    _reverseFlowPairing =
        swapPairing(
            bddPacket.getDstIp(), bddPacket.getSrcIp(), //
            bddPacket.getDstPort(), bddPacket.getSrcPort());
    _one = bddPacket.getFactory().one();
    _zero = bddPacket.getFactory().zero();
  }

  @Override
  public BDD reverseIncomingTransformationRange(String node, String iface) {
    return _incomingCache.computeIfAbsent(
        new NodeInterfacePair(node, iface),
        k -> computeReverseIncomingTransformationRange(node, iface));
  }

  @Override
  public BDD reverseOutgoingTransformationRange(String node, String iface) {
    return _outgoingCache.computeIfAbsent(
        new NodeInterfacePair(node, iface), this::computeReverseOutgoingTransformationRange);
  }

  private BDD computeReverseIncomingTransformationRange(String node, String iface) {
    /* TODO add an intermediate state after checking ingressAcl but before applying transformation
     * This would simplify this code, allow us to remove dependency on configs and filter BDDS.
     */
    BDD preIn = _forwardReachableSets.getOrDefault(new PreInInterface(node, iface), _zero);
    BDD inAcl = forwardIncomingFilterFlowBdd(node, iface);
    return preIn.and(inAcl).replace(_reverseFlowPairing);
  }

  private BDD forwardIncomingFilterFlowBdd(String hostname, String iface) {
    if (_forwardFlowFilterBdds == null) {
      // ignoreFilters
      return _one;
    }
    IpAccessList incomingFilter =
        _configs.get(hostname).getAllInterfaces().get(iface).getIncomingFilter();
    return incomingFilter == null
        ? _one
        : checkNotNull(_forwardFlowFilterBdds.get(hostname).get(incomingFilter.getName()));
  }

  private BDD computeReverseOutgoingTransformationRange(NodeInterfacePair iface) {
    BDD outAcl =
        forwardPreTransformationOutgoingFilterFlowBdd(iface.getHostname(), iface.getInterface());
    BDD reach =
        _outgoingTransformationRangeStates.asMap().getOrDefault(iface, ImmutableSet.of()).stream()
            .map(_forwardReachableSets::get)
            .reduce(BDD::or)
            .orElse(_zero);
    return reach.and(outAcl).replace(_reverseFlowPairing);
  }

  private BDD forwardPreTransformationOutgoingFilterFlowBdd(String hostname, String iface) {
    if (_forwardFlowFilterBdds == null) {
      // ignoreFilters
      return _one;
    }
    IpAccessList preTransformationOutgoingFilter =
        _configs.get(hostname).getAllInterfaces().get(iface).getPreTransformationOutgoingFilter();
    return preTransformationOutgoingFilter == null
        ? _one
        : checkNotNull(
            _forwardFlowFilterBdds.get(hostname).get(preTransformationOutgoingFilter.getName()));
  }

  /** TODO: this currently only includes PreOutEdge states */
  private static Multimap<NodeInterfacePair, StateExpr> computeOutgoingTransformationRangeStates(
      Set<StateExpr> states) {
    ImmutableMultimap.Builder<NodeInterfacePair, StateExpr> builder = ImmutableMultimap.builder();
    states.forEach(
        state -> {
          NodeInterfacePair outIface = state.accept(PreOutgoingTransformationNodeVisitor.INSTANCE);
          if (outIface != null) {
            builder.put(outIface, state);
          }
        });
    return builder.build();
  }
}
