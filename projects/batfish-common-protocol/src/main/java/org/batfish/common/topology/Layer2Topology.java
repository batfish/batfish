package org.batfish.common.topology;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.jgrapht.alg.util.UnionFind;

/** Tracks which interfaces are in the same layer 2 broadcast domain. */
@ParametersAreNonnullByDefault
public final class Layer2Topology {
  public static final Layer2Topology EMPTY = new Layer2Topology(ImmutableMap.of());

  // node -> representative
  private final Map<Layer2Node, Layer2Node> _representative;

  private Layer2Topology(Map<Layer2Node, Layer2Node> representative) {
    _representative = ImmutableMap.copyOf(representative);
  }

  public static @Nonnull Layer2Topology fromDomains(Collection<Set<Layer2Node>> domains) {
    return new Layer2Topology(
        domains.stream()
            .flatMap(
                domain -> {
                  if (domain.isEmpty()) {
                    return Stream.of();
                  }
                  Layer2Node repr = domain.stream().sorted().findFirst().get();
                  return domain.stream().map(node -> Maps.immutableEntry(node, repr));
                })
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));
  }

  public static @Nonnull Layer2Topology fromEdges(Set<Layer2Edge> edges) {
    ImmutableSet<Layer2Node> nodes =
        edges.stream()
            .flatMap(e -> Stream.of(e.getNode1(), e.getNode2()))
            .collect(ImmutableSet.toImmutableSet());
    UnionFind<Layer2Node> unionFind = new UnionFind<>(nodes);
    edges.forEach(e -> unionFind.union(e.getNode1(), e.getNode2()));

    return new Layer2Topology(
        nodes.stream().collect(ImmutableMap.toImmutableMap(Function.identity(), unionFind::find)));
  }

  /**
   * Return the representative of the broadcast domain of {@code layer2Node}, or {@link
   * Optional#empty} if not represented in the layer-2 topology.
   */
  public @Nonnull Optional<Layer2Node> getBroadcastDomainRepresentative(Layer2Node layer2Node) {
    return Optional.ofNullable(_representative.get(layer2Node));
  }

  /**
   * Return the representative of the broadcast domain of {@code nodeInterfacePair}, or {@link
   * Optional#empty} if not represented in the layer-2 topology.
   */
  public @Nonnull Optional<Layer2Node> getBroadcastDomainRepresentative(
      NodeInterfacePair nodeInterfacePair) {
    return getBroadcastDomainRepresentative(
        nodeInterfacePair.getHostname(), nodeInterfacePair.getInterface());
  }

  /**
   * Return the representative of the broadcast domain of the interface represented by {@code
   * hostname} and {@code interfaceName}, or {@link Optional#empty} if not represented in the
   * layer-2 topology.
   */
  public @Nonnull Optional<Layer2Node> getBroadcastDomainRepresentative(
      String hostname, String interfaceName) {
    return getBroadcastDomainRepresentative(new Layer2Node(hostname, interfaceName, null));
  }

  /** Convert a layer3 interface to a layer2 node. */
  private static @Nonnull Layer2Node layer2Node(String hostName, String iface) {
    return new Layer2Node(hostName, iface, null);
  }

  /** Return whether the two interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(Layer2Node n1, Layer2Node n2) {
    Layer2Node r1 = _representative.get(n1);
    return r1 != null && r1.equals(_representative.get(n2));
  }

  /** Return whether the two interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    return inSameBroadcastDomain(
        layer2Node(i1.getHostname(), i1.getInterface()),
        layer2Node(i2.getHostname(), i2.getInterface()));
  }

  /** Return whether two non-switchport interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(String host1, String iface1, String host2, String iface2) {
    return inSameBroadcastDomain(layer2Node(host1, iface1), layer2Node(host2, iface2));
  }
}
