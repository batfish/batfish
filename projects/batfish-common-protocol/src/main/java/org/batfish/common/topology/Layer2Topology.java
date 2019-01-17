package org.batfish.common.topology;

import static org.glassfish.jersey.internal.guava.Predicates.not;

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

  private final Map<NodeInterfacePair, NodeInterfacePair> _representative;

  public Layer2Topology(@Nonnull Collection<Set<NodeInterfacePair>> domains) {
    _representative =
        domains
            .stream()
            .filter(not(Set::isEmpty))
            .flatMap(
                domain -> {
                  NodeInterfacePair representative =
                      domain.stream().min(NodeInterfacePair::compareTo).get();
                  return domain.stream().map(member -> Maps.immutableEntry(member, representative));
                })
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    NodeInterfacePair r1 = _representative.get(i1);
    return r1 != null && r1.equals(_representative.get(i2));
  }

  public boolean inSameBroadcastDomain(String host1, String iface1, String host2, String iface2) {
    return inSameBroadcastDomain(
        new NodeInterfacePair(host1, iface1), new NodeInterfacePair(host2, iface2));
  }
}
