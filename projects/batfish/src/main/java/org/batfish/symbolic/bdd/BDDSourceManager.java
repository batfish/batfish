package org.batfish.symbolic.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;
import static org.batfish.symbolic.bdd.BDDUtils.isAssignment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;

/**
 * Manages BDD variables to track a packet's source: which interface it entered the node through, if
 * any. If it didn't enter through any node, it originated from the device itself.
 */
public final class BDDSourceManager {
  private static final String VAR_NAME = "PacketSource";

  private final BDD _falseBDD;

  private final BDD _isSane;

  private final Map<String, BDD> _sourceBDDs;

  /**
   * Create a {@link BDDSourceManager} for a specified set of possible sources. To include the
   * device as a source, use {@link
   * org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists#SOURCE_ORIGINATING_FROM_DEVICE}.
   */
  @VisibleForTesting
  BDDSourceManager(BDDPacket pkt, Set<String> sources) {
    ImmutableMap.Builder<String, BDD> sourceBDDs = ImmutableMap.builder();

    // add sourceBDD entries for active sources and initialize _isSane constraint
    if (!sources.isEmpty()) {
      int bitsRequired = LongMath.log2(sources.size(), RoundingMode.CEILING);
      BDDInteger var = pkt.allocateBDDInteger(VAR_NAME, bitsRequired, false);
      sourceBDDs.putAll(computeMatchSourceBDDs(var, sources));
      // _isSane constrains var to be one of the values assigned to sources.
      _isSane = var.leq(sources.size() - 1);
    } else {
      // no source var, so don't need a sane constraint
      _isSane = pkt.getFactory().one();
    }

    _sourceBDDs = sourceBDDs.build();
    _falseBDD = pkt.getFactory().zero();
  }

  /**
   * Create a {@link BDDSourceManager} that tracks the specified interfaces as sources, plus the
   * device itself.
   */
  public static BDDSourceManager forInterfaces(BDDPacket pkt, Set<String> interfaces) {
    return new BDDSourceManager(
        pkt,
        ImmutableSet.<String>builder()
            .addAll(interfaces)
            .add(SOURCE_ORIGINATING_FROM_DEVICE)
            .build());
  }

  public static BDDSourceManager forIpAccessList(
      BDDPacket pkt, Configuration config, IpAccessList acl) {
    return forIpAccessList(pkt, config.activeInterfaces(), config.getIpAccessLists(), acl);
  }

  public static BDDSourceManager forSources(
      BDDPacket pkt, Set<String> activeInterfaces, Set<String> referencedSources) {
    if (referencedSources.isEmpty()) {
      return new BDDSourceManager(pkt, ImmutableSet.of());
    }

    Set<String> activeSources =
        ImmutableSet.<String>builder()
            .add(SOURCE_ORIGINATING_FROM_DEVICE)
            .addAll(activeInterfaces)
            .build();

    // discard any referenced interfaces that are inactive
    Set<String> referencedActiveSources = Sets.intersection(referencedSources, activeSources);

    /*
     * If only some interfaces are referenced, we only need to track the referenced ones, plus one
     * more (as a representative of the set of unreferenced interfaces). This is in case the
     * BDD is true only when the source is not the device itself or any referenced interface.
     */
    Set<String> unReferencedInterfaces = Sets.difference(activeSources, referencedActiveSources);
    Set<String> sources =
        unReferencedInterfaces.isEmpty()
            ? ImmutableSet.copyOf(activeSources)
            : ImmutableSet.<String>builder()
                .addAll(referencedActiveSources)
                .add(unReferencedInterfaces.iterator().next())
                .build();

    return new BDDSourceManager(pkt, sources);
  }

  /**
   * Create a {@link BDDSourceManager} for a specified {@link IpAccessList}. To minimize the number
   * of {@link BDD} bits used, it will only track interfaces referenced by the ACL.
   */
  public static BDDSourceManager forIpAccessList(
      BDDPacket pkt,
      Set<String> activeInterfaces,
      Map<String, IpAccessList> namedAcls,
      IpAccessList acl) {
    Set<String> referencedSources = referencedSources(namedAcls, acl);
    return forSources(pkt, activeInterfaces, referencedSources);
  }

  /**
   * A packet can enter a router from at most 1 interface, possibly none (if the packet originated
   * at the router). So for N srcInterfaces, we need N+1 distinct values: one for each interface,
   * and one for "none of them". We use 0 for "none of them".
   */
  private static Map<String, BDD> computeMatchSourceBDDs(
      BDDInteger srcInterfaceVar, Set<String> srcInterfaces) {
    ImmutableMap.Builder<String, BDD> matchSrcInterfaceBDDs = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        srcInterfaces,
        (idx, iface) -> matchSrcInterfaceBDDs.put(iface, srcInterfaceVar.value(idx)));
    return matchSrcInterfaceBDDs.build();
  }

  public BDD getOriginatingFromDeviceBDD() {
    return getSourceBDD(SOURCE_ORIGINATING_FROM_DEVICE);
  }

  public BDD getSourceInterfaceBDD(String iface) {
    return getSourceBDD(iface);
  }

  private BDD getSourceBDD(String source) {
    return _sourceBDDs.getOrDefault(source, _falseBDD);
  }

  @VisibleForTesting
  Map<String, BDD> getSourceBDDs() {
    return _sourceBDDs;
  }

  /**
   * @param bdd An assignment (i.e. only 1 possible value for each variable mentioned).
   * @return The interface of identified by the assigned value, or none if the device itself is the
   *     source.
   */
  public Optional<String> getSourceFromAssignment(BDD bdd) {
    checkArgument(isAssignment(bdd));
    checkArgument(bdd.imp(_isSane).isOne());

    // not tracking any sources, so we can the arbitrarily choose the device
    if (_sourceBDDs.isEmpty()) {
      return Optional.empty();
    }

    String source =
        _sourceBDDs
            .entrySet()
            .stream()
            .filter(entry -> bdd.imp(entry.getValue()).isOne())
            .map(Entry::getKey)
            .findFirst()
            .get();
    return source.equals(SOURCE_ORIGINATING_FROM_DEVICE) ? Optional.empty() : Optional.of(source);
  }

  /**
   * @return A sanity constraint that the packet source must be a valid value (originating from
   *     device, or one of the source interfaces).
   */
  public BDD isSane() {
    return _isSane;
  }
}
