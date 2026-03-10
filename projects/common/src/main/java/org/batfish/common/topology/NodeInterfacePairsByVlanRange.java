package org.batfish.common.topology;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Internal implementation detail of Layer2 topology computation, performed in {@link TopologyUtil}.
 * Keeps track of a mapping between VLAN ranges to a set of {@link NodeInterfacePair} which allow
 * those VLANs.
 *
 * <p>This implementation does not care about switchport modes, only overlapping VLANs.
 */
@ParametersAreNonnullByDefault
class NodeInterfacePairsByVlanRange {

  /** Create a new mapping of VLAN ranges to NodeInterfacePairs */
  static NodeInterfacePairsByVlanRange create() {
    return new NodeInterfacePairsByVlanRange();
  }

  /** See {@link #add(Range, Collection) } */
  public void add(int vlan, NodeInterfacePair ni) {
    add(Range.singleton(vlan), ni);
  }

  /** See {@link #add(Range, Collection)} */
  public void add(Range<Integer> vlans, NodeInterfacePair ni) {
    add(vlans, ImmutableSet.of(ni));
  }

  /**
   * Add a new range with the given collection of node-interface pairs. If the given range
   * intersects with existing ranges, new ranges will be created, with the intersection(s) being
   * mapped to the union of existing and provided node-interface pairs.
   */
  public void add(Range<Integer> vlans, Collection<NodeInterfacePair> nis) {
    Range<Integer> canonical = vlans.canonical(DiscreteDomain.integers());
    if (canonical.isEmpty() || nis.isEmpty()) {
      return;
    }
    _ranges.merge(
        canonical,
        ImmutableSet.copyOf(nis),
        (a, b) -> ImmutableSet.<NodeInterfacePair>builder().addAll(a).addAll(b).build());
  }

  /** Return the mapping of all ranges to sets of node-interface pairs as an unmodifiable map */
  public @Nonnull Map<Range<Integer>, Set<NodeInterfacePair>> asMap() {
    return _ranges.asMapOfRanges();
  }

  /**
   * Splits the current VLAN range to {@link NodeInterfacePair} mapping into a map of hostname to
   * {@link InterfacesByVlanRange}. Uses the current set of VLAN ranges without any per-node
   * modifications; all keys in each mapping of VLAN ranges to interfaces (in {@link
   * InterfacesByVlanRange}) are also present in {@link #asMap()}.
   */
  public @Nonnull Map<String, InterfacesByVlanRange> splitByNode() {
    // hostname -> VLAN range -> interfaces on hostname in VLAN range
    Map<String, Map<Range<Integer>, ImmutableSet.Builder<String>>> byNode = new HashMap<>();
    asMap()
        .forEach(
            (vlanRange, nis) -> {
              for (NodeInterfacePair ni : nis) {
                byNode
                    .computeIfAbsent(ni.getHostname(), k -> new HashMap<>())
                    .computeIfAbsent(vlanRange, k -> ImmutableSet.builder())
                    .add(ni.getInterface());
              }
            });
    return toImmutableMap(
        byNode,
        Map.Entry::getKey,
        e ->
            new InterfacesByVlanRange(
                toImmutableMap(e.getValue(), Map.Entry::getKey, e2 -> e2.getValue().build())));
  }

  private final RangeMap<Integer, Set<NodeInterfacePair>> _ranges;

  private NodeInterfacePairsByVlanRange() {
    _ranges = TreeRangeMap.create();
  }
}
