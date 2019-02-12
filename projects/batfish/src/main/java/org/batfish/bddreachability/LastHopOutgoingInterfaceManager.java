package org.batfish.bddreachability;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.batfish.common.bdd.BDDFiniteDomain.domainsWithSharedVariable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDFiniteDomain;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Tracks {@link BDD} constraints to identify the previous hop and its outgoing interface. This is
 * required for implementing sessions, since they don't forward using the FIB. Instead, return
 * traffic is forwarded to the node/interface the forward traffic was received from. In real
 * devices, the MAC address of the previous hop node/interface is stored in the session. We model
 * that behavior using the node/interface names.
 *
 * <p>We use a separate {@link BDDFiniteDomain} for each (receiving node, ingress interface), since
 * that makes the domains smaller (thus fewer BDD variables required; more efficient) and we already
 * track ingress interface.
 */
public final class LastHopOutgoingInterfaceManager {
  private static final String VAR_NAME = "LAST_HOP_OUTGOING_IFACE";

  // (Receiving node, ingress interface) --> [(Sending node, egress interface)]
  private final Map<NodeInterfacePair, BDDFiniteDomain<NodeInterfacePair>> _finiteDomains;

  private final BDD _trueBdd;

  public LastHopOutgoingInterfaceManager(
      BDDPacket pkt, Map<String, Configuration> configs, Topology topology) {
    _finiteDomains = computeFiniteDomains(pkt, configs, topology);
    _trueBdd = pkt.getFactory().one();
  }

  private static Map<NodeInterfacePair, BDDFiniteDomain<NodeInterfacePair>> computeFiniteDomains(
      BDDPacket pkt, Map<String, Configuration> configs, Topology topology) {
    // Nodes that can have sessions (and thus need to track last hop outgoing interface.
    Set<String> sessionNodes =
        configs.values().stream()
            .filter(
                config ->
                    config.getAllInterfaces().values().stream()
                        .filter(Interface::getActive)
                        .anyMatch(iface -> iface.getFirewallSessionInterfaceInfo() != null))
            .map(Configuration::getHostname)
            .collect(ImmutableSet.toImmutableSet());

    Map<NodeInterfacePair, Set<NodeInterfacePair>> lastHops =
        topology.getEdges().stream()
            .filter(edge -> sessionNodes.contains(edge.getNode2()))
            .collect(groupingBy(Edge::getHead, mapping(Edge::getTail, Collectors.toSet())));

    return lastHops.isEmpty()
        ? ImmutableMap.of()
        : domainsWithSharedVariable(pkt, VAR_NAME, lastHops);
  }

  public BDD getLastHopOutgoingInterfaceBdd(
      String sendingNode, String sendingIface, String receivingNode, String receivingIface) {
    NodeInterfacePair key = new NodeInterfacePair(receivingNode, receivingIface);
    BDDFiniteDomain<NodeInterfacePair> fd = _finiteDomains.get(key);
    if (fd == null) {
      // receivingNode doesn't track last-hop outgoing interfaces.
      return _trueBdd;
    }
    NodeInterfacePair val = new NodeInterfacePair(sendingNode, sendingIface);
    return fd.getConstraintForValue(val);
  }

  public Optional<NodeInterfacePair> getLastHopOutgoingInterfaceFromAssignment(
      String receivingNode, String receivingIface, BDD bdd) {
    NodeInterfacePair key = new NodeInterfacePair(receivingNode, receivingIface);
    BDDFiniteDomain<NodeInterfacePair> fd = _finiteDomains.get(key);
    return fd == null ? Optional.empty() : Optional.of(fd.getValueFromAssignment(bdd));
  }

  @VisibleForTesting
  Map<NodeInterfacePair, BDDFiniteDomain<NodeInterfacePair>> getFiniteDomains() {
    return _finiteDomains;
  }
}
