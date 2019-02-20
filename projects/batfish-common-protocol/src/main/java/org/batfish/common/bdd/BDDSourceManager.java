package org.batfish.common.bdd;

import static org.batfish.common.util.CommonUtil.toImmutableMap;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.referencedSources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;

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
 * org.batfish.datamodel.acl.MatchSrcInterface} or {@link OriginatingFromDevice} expression.
 */
public final class BDDSourceManager {
  private static final String VAR_NAME = "PacketSource";

  /**
   * A source (an interface or the device itself) is "active but unreferenced" if it is active but
   * not reference by {@link MatchSrcInterface} or {@link OriginatingFromDevice}. For efficiency,
   * the finite domain doesn't track each active but unreferenced source. Instead, it tracks a
   * single representative value that represents all of them.
   *
   * <p>{@code null} when there are no active but unreferenced sources.
   */
  private final @Nullable String _activeButUnreferencedRepresentative;

  private final Set<String> _activeButUnreferenced;
  private final BDD _falseBDD;
  private final BDDFiniteDomain<String> _finiteDomain;
  private final BDD _trueBDD;

  private BDDSourceManager(
      BDDFiniteDomain<String> finiteDomain, Set<String> activeButUnreferenced) {
    _activeButUnreferenced = ImmutableSet.copyOf(activeButUnreferenced);
    _activeButUnreferencedRepresentative =
        _activeButUnreferenced.stream().sorted().findFirst().orElse(null);
    _falseBDD = finiteDomain.getIsValidConstraint().getFactory().zero();
    _trueBDD = finiteDomain.getIsValidConstraint().getFactory().one();
    _finiteDomain = finiteDomain;
  }

  /** Allocates a variable for the sources and initializes SANE. */
  private static BDDSourceManager forSourcesInternal(
      BDDPacket pkt, Set<String> activeAndReferenced, Set<String> activeButNotReferenced) {
    return new BDDSourceManager(
        new BDDFiniteDomain<>(
            pkt, VAR_NAME, valuesToTrack(activeAndReferenced, activeButNotReferenced)),
        activeButNotReferenced);
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
                referencedSources(config.getIpAccessLists(), acl).stream()
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

    Map<String, Set<String>> valuesToTrack =
        toImmutableMap(
            activeAndReferenced,
            Entry::getKey,
            entry -> {
              String hostname = entry.getKey();
              Set<String> hostActiveAndReferenced = entry.getValue();
              Set<String> hostActiveButNotReferenced = activeButNotReferenced.get(hostname);
              return valuesToTrack(hostActiveAndReferenced, hostActiveButNotReferenced);
            });

    Map<String, BDDFiniteDomain<String>> finiteDomains =
        BDDFiniteDomain.domainsWithSharedVariable(pkt, VAR_NAME, valuesToTrack);

    return toImmutableMap(
        finiteDomains,
        Entry::getKey,
        entry ->
            new BDDSourceManager(entry.getValue(), activeButNotReferenced.get(entry.getKey())));
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
   * at the router). So for N srcInterfaces, we need N+1 distinct values (0 through N): one for each
   * interface, and one for "none of them". We use the last value (N) for "none of them".
   */
  private static Set<String> valuesToTrack(
      Set<String> activeAndReferenced, Set<String> activeButUnreferenced) {

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    builder.addAll(activeAndReferenced);

    if (!activeButUnreferenced.isEmpty()) {
      builder.add(activeButUnreferenced.stream().min(Comparator.naturalOrder()).get());
    }

    return builder.build();
  }

  public BDD getOriginatingFromDeviceBDD() {
    return getSourceBDD(SOURCE_ORIGINATING_FROM_DEVICE);
  }

  private BDD getSourceBDD(String source) {
    if (isTrivial()) {
      return _trueBDD;
    }
    String key =
        _activeButUnreferenced.contains(source) ? _activeButUnreferencedRepresentative : source;
    BDD bdd = _finiteDomain.getValueBdds().getOrDefault(key, _falseBDD);
    return bdd;
  }

  public BDD getSourceInterfaceBDD(String iface) {
    return getSourceBDD(iface);
  }

  public Map<String, BDD> getSourceBDDs() {
    ImmutableMap.Builder<String, BDD> builder = ImmutableMap.builder();
    builder.putAll(_finiteDomain.getValueBdds());

    if (_activeButUnreferencedRepresentative != null) {
      BDD representativeBdd =
          _finiteDomain.getConstraintForValue(_activeButUnreferencedRepresentative);
      _activeButUnreferenced.stream()
          .filter(src -> !src.equals(_activeButUnreferencedRepresentative))
          .forEach(src -> builder.put(src, representativeBdd));
    }

    return builder.build();
  }

  /**
   * @param bdd An assignment (i.e. only 1 possible value for each variable mentioned).
   * @return The interface of identified by the assigned value, or none if the device itself is the
   *     source.
   */
  public Optional<String> getSourceFromAssignment(BDD bdd) {
    if (_finiteDomain.isEmpty() && _activeButUnreferencedRepresentative == null) {
      // No sources
      return Optional.empty();
    }
    String value =
        _finiteDomain.isEmpty()
            ? _activeButUnreferencedRepresentative
            : _finiteDomain.getValueFromAssignment(bdd);
    return value.equals(SOURCE_ORIGINATING_FROM_DEVICE) ? Optional.empty() : Optional.of(value);
  }

  /**
   * @return true when there is nothing to track. This can happen when no {@link IpAccessList ACL}
   *     on the node uses {@link org.batfish.datamodel.acl.MatchSrcInterface} or {@link
   *     OriginatingFromDevice}.
   */
  public boolean isTrivial() {
    return _finiteDomain.isEmpty();
  }

  /**
   * @return A constraint that the source variable is assigned one of the valid values for this
   *     device.
   */
  public BDD isValidValue() {
    return _finiteDomain.getIsValidConstraint();
  }

  /** Existentially quantify the source variable. */
  public BDD existsSource(BDD bdd) {
    return _finiteDomain.existsValue(bdd);
  }

  /**
   * Test if a {@link BDD} includes the constraint that the source variable has a valid value (or a
   * stronger one that implies it).
   */
  public boolean hasIsValidConstraint(BDD bdd) {
    return bdd.and(_finiteDomain.getIsValidConstraint()).equals(bdd);
  }

  /** Test if a {@link BDD} includes a constraint on the source variable. */
  public boolean hasSourceConstraint(BDD bdd) {
    return !bdd.equals(_finiteDomain.existsValue(bdd));
  }

  /**
   * Return whether each source is tracked separately (i.e. each has its own value in the domain).
   */
  public boolean allSourcesTracked() {
    return _activeButUnreferencedRepresentative == null;
  }
}
