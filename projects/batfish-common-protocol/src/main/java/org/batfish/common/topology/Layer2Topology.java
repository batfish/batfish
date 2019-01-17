package org.batfish.common.topology;

import static com.google.common.base.Predicates.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Tracks which interfaces are in the same layer 2 broadcast domain. */
@ParametersAreNonnullByDefault
public final class Layer2Topology {

  private final Map<Layer2Node, Layer2Node> _ifaceToRepresentative;

  public Layer2Topology(@Nonnull Collection<Set<Layer2Node>> domains) {
    _ifaceToRepresentative =
        domains.stream()
            .filter(not(Set::isEmpty))
            .flatMap(
                domain -> {
                  Layer2Node representative = domain.stream().min(Layer2Node::compareTo).get();
                  return domain.stream().map(member -> Maps.immutableEntry(member, representative));
                })
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  /** Convert a layer3 interface to a layer2 node. */
  private static Layer2Node layer2Node(String hostName, String iface) {
    return new Layer2Node(hostName, iface, null);
  }

  /** Return whether the two interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(Layer2Node n1, Layer2Node n2) {
    Layer2Node r1 = _ifaceToRepresentative.get(n1);
    return r1 != null && r1.equals(_ifaceToRepresentative.get(n2));
  }

  /** Return whether the two interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    return inSameBroadcastDomain(
        layer2Node(i1.getHostname(), i1.getInterface()),
        layer2Node(i2.getHostname(), i2.getInterface()));
  }

  /** Return whether the two interfaces are in the same broadcast domain. */
  public boolean inSameBroadcastDomain(String host1, String iface1, String host2, String iface2) {
    return inSameBroadcastDomain(layer2Node(host1, iface1), layer2Node(host2, iface2));
  }
}
