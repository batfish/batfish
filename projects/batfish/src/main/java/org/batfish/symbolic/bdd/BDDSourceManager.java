package org.batfish.symbolic.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.DEVICE_IS_THE_SOURCE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;
import static org.batfish.symbolic.bdd.BDDUtils.isAssignment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.List;
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

  private BDD _isSane;

  private final Map<String, BDD> _sourceBDDs;

  /**
   * Create a {@link BDDSourceManager} for a specified set of possible sources. To include the
   * device as a source, use {@link
   * org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists#DEVICE_IS_THE_SOURCE}.
   */
  @VisibleForTesting
  BDDSourceManager(BDDPacket pkt, List<String> sources) {
    if (sources.size() == 0) {
      _isSane = pkt.getFactory().one();
      _sourceBDDs = ImmutableMap.of();
    } else {
      int bitsRequired = LongMath.log2(sources.size(), RoundingMode.CEILING);
      BDDInteger var = pkt.allocateBDDInteger(VAR_NAME, bitsRequired, false);
      _isSane = var.leq(sources.size());
      _sourceBDDs = computeMatchSourceBDDs(var, sources);
    }
  }

  /**
   * Create a {@link BDDSourceManager} that tracks the specified interfaces as sources, plus the
   * device itself.
   */
  public static BDDSourceManager forInterfaces(BDDPacket pkt, List<String> interfaces) {
    return new BDDSourceManager(
        pkt, ImmutableList.<String>builder().addAll(interfaces).add(DEVICE_IS_THE_SOURCE).build());
  }

  /**
   * Create a {@link BDDSourceManager} for a specified {@link IpAccessList}. To minimize the number
   * of {@link BDD} bits used, it will only track interfaces referenced by the ACL.
   */
  public static BDDSourceManager forIpAccessList(
      BDDPacket pkt, Configuration config, IpAccessList acl) {
    List<String> interfaces = sourcesForIpAccessList(config, acl);
    return new BDDSourceManager(pkt, interfaces);
  }

  @VisibleForTesting
  static List<String> sourcesForIpAccessList(Configuration config, IpAccessList acl) {
    Set<String> referencedSources = referencedSources(acl);
    if (referencedSources.isEmpty()) {
      return ImmutableList.of();
    }

    Set<String> allSources =
        ImmutableSet.<String>builder()
            .add(DEVICE_IS_THE_SOURCE)
            .addAll(config.getInterfaces().keySet())
            .build();
    Set<String> unReferencedInterfaces = Sets.difference(allSources, referencedSources);
    /*
     * If only some interfaces are referenced, we only need to track the referenced ones, plus one
     * more (as a representative of the set of unreferenced interfaces). This is in case the
     * BDD is true only when the source is not the device itself or any referenced interface.
     */
    return unReferencedInterfaces.isEmpty()
        ? ImmutableList.copyOf(allSources)
        : ImmutableList.<String>builder()
            .addAll(referencedSources)
            .add(unReferencedInterfaces.iterator().next())
            .build();
  }

  /**
   * A packet can enter a router from at most 1 interface, possibly none (if the packet originated
   * at the router). So for N srcInterfaces, we need N+1 distinct values: one for each interface,
   * and one for "none of them". We use 0 for "none of them".
   */
  private static Map<String, BDD> computeMatchSourceBDDs(
      BDDInteger srcInterfaceVar, List<String> srcInterfaces) {
    ImmutableMap.Builder<String, BDD> matchSrcInterfaceBDDs = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        srcInterfaces,
        (idx, iface) -> matchSrcInterfaceBDDs.put(iface, srcInterfaceVar.value(idx + 1)));
    return matchSrcInterfaceBDDs.build();
  }

  public BDD getOriginatingFromDeviceBDD() {
    return getSourceBDD(DEVICE_IS_THE_SOURCE);
  }

  public BDD getSourceInterfaceBDD(String iface) {
    return getSourceBDD(iface);
  }

  private BDD getSourceBDD(String iface) {
    checkArgument(_sourceBDDs.containsKey(iface), "Missing BDD for source: " + iface);
    return _sourceBDDs.get(iface);
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
    return source.equals(DEVICE_IS_THE_SOURCE) ? Optional.empty() : Optional.of(source);
  }

  /**
   * @return A sanity constraint that the packet source must be a valid value (originating from
   *     device, or one of the source interfaces).
   */
  public BDD isSane() {
    return _isSane;
  }
}
