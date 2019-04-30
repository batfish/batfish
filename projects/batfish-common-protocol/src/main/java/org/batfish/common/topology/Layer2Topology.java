package org.batfish.common.topology;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.jgrapht.alg.util.UnionFind;

/** Tracks which interfaces are in the same layer 2 broadcast domain. */
@ParametersAreNonnullByDefault
public final class Layer2Topology {

  public static final Layer2Topology EMPTY = new Layer2Topology(new UnionFind<>(ImmutableSet.of()));

  private final UnionFind<Layer2Node> _unionFind;

  private Layer2Topology(UnionFind<Layer2Node> unionFind) {
    _unionFind = unionFind;
  }

  public static @Nonnull Layer2Topology fromDomains(Collection<Set<Layer2Node>> domains) {
    UnionFind<Layer2Node> unionFind =
        new UnionFind<>(
            domains.stream().flatMap(Set::stream).collect(ImmutableSet.toImmutableSet()));
    domains.forEach(
        domain -> {
          if (domain.isEmpty()) {
            return;
          }
          Iterator<Layer2Node> it = domain.iterator();
          Layer2Node node = it.next();
          while (it.hasNext()) {
            unionFind.union(node, it.next());
          }
        });
    return new Layer2Topology(unionFind);
  }

  public static @Nonnull Layer2Topology fromEdges(Set<Layer2Edge> edges) {
    UnionFind<Layer2Node> unionFind =
        new UnionFind<>(
            edges.stream()
                .flatMap(e -> Stream.of(e.getNode1(), e.getNode2()))
                .collect(ImmutableSet.toImmutableSet()));
    edges.forEach(e -> unionFind.union(e.getNode1(), e.getNode2()));
    return new Layer2Topology(unionFind);
  }

  /**
   * Return the representative of the broadcast domain of {@code layer2Node}, or {@link
   * Optional#empty} if not represented in the layer-2 topology.
   */
  public @Nonnull Optional<Layer2Node> getBroadcastDomainRepresentative(Layer2Node layer2Node) {
    try {
      return Optional.of(_unionFind.find(layer2Node));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
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
    try {
      return _unionFind.inSameSet(n1, n2);
    } catch (IllegalArgumentException e) {
      // one or both elements missing
      return false;
    }
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
