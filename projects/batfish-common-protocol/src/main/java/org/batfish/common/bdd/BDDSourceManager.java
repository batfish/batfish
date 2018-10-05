package org.batfish.common.bdd;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.bdd.BDDUtils.isAssignment;
import static org.batfish.common.util.CommonUtil.toImmutableMap;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.sf.javabdd.BDD;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;

/**
 * Manages BDD variables to track a packet's source: which interface it entered the node through, if
 * any. If it didn't enter through any node, it originated from the device itself.
 *
 * <p>To minize to the number of BDD variables used, we consider which sources are active and which
 * are referenced by ACLs on the node. If a source is active and referenced, then we assign it a
 * unique identifier. All inactive interface sources are assigned the same identifier. We allocate
 * enough BDD variables to distinguish between the unique identifiers.
 *
 * <p>We need these identifiers in two contexts: to record the source of a packet as it enters a
 * node, and to check the source of a packet when an ACL includes a {@link
 * org.batfish.datamodel.acl.MatchSrcInterface} or {@link
 * org.batfish.datamodel.acl.OriginatingFromDevice} expression.
 */
public final class BDDSourceManager {
  private static final String VAR_NAME = "PacketSource";

  private final BDD _falseBDD;

  private final BDD _isValidValue;

  private final Map<String, BDD> _sourceBDDs;

  private final BDD _sourceVarBits;

  private BDDSourceManager(BDD isValidValue, Map<String, BDD> sourceBDDs, BDD sourceVarBits) {
    _isValidValue = isValidValue;
    _sourceBDDs = ImmutableMap.copyOf(sourceBDDs);
    _sourceVarBits = sourceVarBits;
    _falseBDD = isValidValue.getFactory().zero();
  }

  private static BDDSourceManager forNoReferencedSources(
      BDDPacket pkt, Set<String> activeButNotReferenced) {
    return new BDDSourceManager(
        pkt.getFactory().one(),
        toImmutableMap(activeButNotReferenced, Function.identity(), src -> pkt.getFactory().one()),
        pkt.getFactory().zero());
  }

  /** Allocates a variable for the sources and initializes SANE. */
  private static BDDSourceManager forSourcesInternal(
      BDDPacket pkt, Set<String> activeAndReferenced, Set<String> activeButNotReferenced) {
    if (activeAndReferenced.isEmpty()) {
      return forNoReferencedSources(pkt, activeButNotReferenced);
    }

    int bitsRequired =
        LongMath.log2(
            valuesRequired(activeAndReferenced, activeButNotReferenced), RoundingMode.CEILING);
    BDDInteger sourceVar = pkt.allocateBDDInteger(VAR_NAME, bitsRequired, false);

    return forSourcesInternal(pkt, sourceVar, activeAndReferenced, activeButNotReferenced);
  }

  private static BDDSourceManager forSourcesInternal(
      BDDPacket pkt,
      BDDInteger sourceVar,
      Set<String> activeAndReferenced,
      Set<String> activeButNotReferenced) {

    Map<String, BDD> sourceBDDs =
        computeMatchSourceBDDs(sourceVar, activeAndReferenced, activeButNotReferenced);

    BDD isSane = sourceVar.leq(valuesRequired(activeAndReferenced, activeButNotReferenced) - 1);

    BDD sourceVarBits =
        Arrays.stream(sourceVar.getBitvec()).reduce(pkt.getFactory().one(), BDD::and);

    return new BDDSourceManager(isSane, sourceBDDs, sourceVarBits);
  }

  /**
   * Create a {@link BDDSourceManager} that tracks the specified interfaces as sources, plus the
   * device itself.
   */
  public static BDDSourceManager forInterfaces(BDDPacket pkt, Set<String> interfaces) {
    Set<String> sources =
        ImmutableSet.<String>builder()
            .addAll(interfaces)
            .add(SOURCE_ORIGINATING_FROM_DEVICE)
            .build();
    return forSourcesInternal(pkt, sources, ImmutableSet.of());
  }

  public static BDDSourceManager forIpAccessList(
      BDDPacket pkt, Configuration config, IpAccessList acl) {
    return forIpAccessList(
        pkt,
        Sets.union(ImmutableSet.of(SOURCE_ORIGINATING_FROM_DEVICE), config.activeInterfaces()),
        config.getIpAccessLists(),
        acl);
  }

  public static BDDSourceManager forSources(
      BDDPacket pkt, Set<String> activeSources, Set<String> referencedSources) {
    return forSourcesInternal(
        pkt,
        Sets.intersection(activeSources, referencedSources),
        Sets.difference(activeSources, referencedSources));
  }

  /**
   * Initialize a {@link BDDSourceManager} for each {@link Configuration} in a network. A single
   * variable is shared by all of them.
   */
  public static Map<String, BDDSourceManager> forNetwork(
      BDDPacket pkt, Map<String, Configuration> configs) {
    Map<String, Set<String>> activeSources =
        toImmutableMap(
            configs.entrySet(),
            Entry::getKey,
            entry ->
                ImmutableSet.<String>builder()
                    .add(SOURCE_ORIGINATING_FROM_DEVICE)
                    .addAll(entry.getValue().activeInterfaces())
                    .build());

    Map<String, Set<String>> referencedSources =
        toImmutableMap(
            configs.entrySet(),
            Entry::getKey,
            entry -> {
              Configuration config = entry.getValue();
              Set<String> active = activeSources.get(config.getHostname());
              Set<String> sources = new HashSet<>();
              for (IpAccessList acl : config.getIpAccessLists().values()) {
                referencedSources(config.getIpAccessLists(), acl)
                    .stream()
                    .filter(active::contains)
                    .forEach(sources::add);
              }
              return sources;
            });

    Map<String, Set<String>> activeAndReferenced =
        toImmutableMap(
            activeSources,
            Entry::getKey,
            activeEntry ->
                Sets.intersection(
                    activeEntry.getValue(), referencedSources.get(activeEntry.getKey())));

    Map<String, Set<String>> activeButNotReferenced =
        toImmutableMap(
            activeSources,
            Entry::getKey,
            activeEntry ->
                Sets.difference(
                    activeEntry.getValue(), referencedSources.get(activeEntry.getKey())));

    // Number of values needed to track sources in any node
    int valuesNeeded =
        activeAndReferenced
            .keySet()
            .stream()
            .mapToInt(
                node ->
                    valuesRequired(activeAndReferenced.get(node), activeButNotReferenced.get(node)))
            .max()
            .orElse(0);

    if (valuesNeeded == 0) {
      return toImmutableMap(
          configs.keySet(),
          Function.identity(),
          hostname -> forNoReferencedSources(pkt, activeButNotReferenced.get(hostname)));
    }

    int bits = LongMath.log2(valuesNeeded, RoundingMode.CEILING);
    BDDInteger sourceVar = pkt.allocateBDDInteger(VAR_NAME, bits, false);

    return toImmutableMap(
        configs.keySet(),
        Function.identity(),
        hostname ->
            forSourcesInternal(
                pkt,
                sourceVar,
                activeAndReferenced.get(hostname),
                activeButNotReferenced.get(hostname)));
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

  /*
   * If there are activeButUnreference sources, we need to allocate an extra value that is shared
   * by all of them.
   */
  private static int valuesRequired(
      Set<String> activeAndReferenced, Set<String> activeButUnreferenced) {
    return activeButUnreferenced.isEmpty()
        ? activeAndReferenced.size()
        : activeAndReferenced.size() + 1;
  }

  /**
   * A packet can enter a router from at most 1 interface, possibly none (if the packet originated
   * at the router). So for N srcInterfaces, we need N+1 distinct values (0 through N): one for each
   * interface, and one for "none of them". We use the last value (N) for "none of them".
   */
  private static Map<String, BDD> computeMatchSourceBDDs(
      BDDInteger sourceVar, Set<String> activeAndReferenced, Set<String> activeButUnreferenced) {
    int bitsRequired =
        LongMath.log2(
            valuesRequired(activeAndReferenced, activeButUnreferenced), RoundingMode.CEILING);
    checkArgument(
        bitsRequired <= sourceVar.getBitvec().length,
        "sourceVar not big enough to track active and referenced sources");

    ImmutableMap.Builder<String, BDD> matchSrcBDDs = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        activeAndReferenced, (idx, src) -> matchSrcBDDs.put(src, sourceVar.value(idx)));

    // Get the next unused identifier. This is used for all activeButUnreferenced sources
    int unreferencedSourceValue = activeAndReferenced.size();
    activeButUnreferenced.forEach(
        src -> matchSrcBDDs.put(src, sourceVar.value(unreferencedSourceValue)));

    return matchSrcBDDs.build();
  }

  public BDD getOriginatingFromDeviceBDD() {
    return _sourceBDDs.getOrDefault(SOURCE_ORIGINATING_FROM_DEVICE, _falseBDD);
  }

  public BDD getSourceInterfaceBDD(String iface) {
    return _sourceBDDs.getOrDefault(iface, _falseBDD);
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
    checkArgument(bdd.imp(_isValidValue).isOne());

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
   * @return A constraint that the source variable is assigned one of the valid values for this
   *     device.
   */
  public BDD isValidValue() {
    return _isValidValue;
  }

  /** Existentially quantify the source variable. */
  public BDD existsSource(BDD bdd) {
    return bdd.exist(_sourceVarBits);
  }
}
