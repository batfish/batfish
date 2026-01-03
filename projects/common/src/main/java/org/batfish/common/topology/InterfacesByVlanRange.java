package org.batfish.common.topology;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Internal implementation detail of Layer2 topology computation within one node, performed in
 * {@link TopologyUtil}. Contains an unmodifiable mapping of VLAN ranges to a set of interfaces
 * which allow those VLANs. VLAN ranges are determined based on a global breakdown of
 * uniquely-treated VLANs (see {@link NodeInterfacePairsByVlanRange}).
 */
@ParametersAreNonnullByDefault
class InterfacesByVlanRange {

  /**
   * Constructs an {@link InterfacesByVlanRange} with the given mapping of VLAN ranges to interface
   * names in that VLAN range. The keys in {@code ranges} must be non-empty, canonical, and
   * non-overlapping.
   */
  public InterfacesByVlanRange(Map<Range<Integer>, Set<String>> ranges) {
    Range<Integer> invalidRange =
        ranges.keySet().stream()
            .filter(InterfacesByVlanRange::isInvalidRange)
            .findAny()
            .orElse(null);
    checkArgument(
        invalidRange == null, "Range %s cannot be used in InterfacesByVlanRange", invalidRange);
    checkArgument(
        rangesDoNotOverlap(ranges.keySet()), "Ranges in InterfacesByVlanRange cannot overlap");
    _ranges = ImmutableMap.copyOf(ranges);
  }

  @VisibleForTesting
  static boolean isInvalidRange(Range<Integer> range) {
    return range.isEmpty() || !range.equals(range.canonical(DiscreteDomain.integers()));
  }

  /**
   * Checks that the given ranges do not overlap. Assumes they are valid canonical ranges (see
   * {@link #isInvalidRange(Range)}).
   */
  @VisibleForTesting
  static boolean rangesDoNotOverlap(Set<Range<Integer>> ranges) {
    List<Range<Integer>> rangeList =
        ranges.stream()
            .sorted(Comparator.comparing(Range::lowerEndpoint))
            .collect(ImmutableList.toImmutableList());
    int lastUpper = 0;
    for (Range<Integer> range : rangeList) {
      // Check for overlap. Canonical ranges have a closed lower bound and an open upper bound, so
      // this range's lower bound must be lower than the last range's upper bound for it to overlap.
      if (range.lowerEndpoint() < lastUpper) {
        return false;
      }
      lastUpper = range.upperEndpoint();
    }
    return true;
  }

  /** Return the mapping of all ranges to sets of interfaces as an unmodifiable map */
  public @Nonnull Map<Range<Integer>, Set<String>> getMap() {
    return _ranges;
  }

  /** Return the set of interfaces for a given VLAN, or an empty set. */
  public @Nonnull Set<String> get(int vlan) {
    return Optional.ofNullable(getRange(vlan)).map(_ranges::get).orElse(ImmutableSet.of());
  }

  /** Return the range matching given VLAN, or {@code null} if no match is present. */
  public @Nullable Range<Integer> getRange(int vlan) {
    return _ranges.keySet().stream().filter(r -> r.contains(vlan)).findFirst().orElse(null);
  }

  // Private implementation

  private final Map<Range<Integer>, Set<String>> _ranges;
}
