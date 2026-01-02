package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.GenericTrackMethodVisitor;
import org.batfish.datamodel.tracking.PriorityEvaluator;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethodEvaluatorProvider;

/** Base implementation for {@link IpOwners}. */
@ParametersAreNonnullByDefault
public abstract class IpOwnersBaseImpl implements IpOwners {

  /** Details of HSRP or VRRP elections. */
  public static class ElectionDetails {

    /** Map: interface -> VRRP/HSRP group -> actual priority after tracks are applied */
    public @Nonnull Map<NodeInterfacePair, Map<Integer, Integer>> getActualPriorities() {
      return _actualPriorities;
    }

    /**
     * Map: interface -> VRRP/HSRP group -> successful track method name -> (track method, track
     * action)
     */
    public @Nonnull Map<
            NodeInterfacePair, Map<Integer, Map<String, Entry<TrackMethod, TrackAction>>>>
        getSuccessfulTracks() {
      return _successfulTracks;
    }

    /** Map: interface -> VRRP/HSRP group -> election winner */
    public @Nonnull Map<NodeInterfacePair, Map<Integer, NodeInterfacePair>> getWinnerByCandidate() {
      return _winnerByCandidate;
    }

    /** Map: interface -> VRRP/HSRP group -> election candidates */
    public @Nonnull Map<NodeInterfacePair, Map<Integer, Set<NodeInterfacePair>>>
        getCandidatesByCandidate() {
      return _candidatesByCandidate;
    }

    /**
     * Map: interface -> VRRP/HSRP group -> failed track method name -> (track method, track action)
     */
    public @Nonnull Map<
            NodeInterfacePair, Map<Integer, Map<String, Entry<TrackMethod, TrackAction>>>>
        getFailedTracks() {
      return _failedTracks;
    }

    private ElectionDetails() {
      _actualPriorities = new HashMap<>();
      _successfulTracks = new HashMap<>();
      _failedTracks = new HashMap<>();
      _candidatesByCandidate = new HashMap<>();
      _winnerByCandidate = new HashMap<>();
    }

    private final @Nonnull Map<NodeInterfacePair, Map<Integer, Integer>> _actualPriorities;
    private final @Nonnull Map<
            NodeInterfacePair, Map<Integer, Map<String, Entry<TrackMethod, TrackAction>>>>
        _successfulTracks;
    private final @Nonnull Map<
            NodeInterfacePair, Map<Integer, Map<String, Entry<TrackMethod, TrackAction>>>>
        _failedTracks;
    private final @Nonnull Map<NodeInterfacePair, Map<Integer, Set<NodeInterfacePair>>>
        _candidatesByCandidate;
    private final @Nonnull Map<NodeInterfacePair, Map<Integer, NodeInterfacePair>>
        _winnerByCandidate;
  }

  /**
   * Compute IP owners based on information in configurations, layer-3 adjencies, and a provider for
   * methods to evaluate track methods affecting HSRP/VRRP priorities.
   *
   * <p>{@link L3Adjacencies} are needed to compute partitions for HSRP/VRRP elections.
   *
   * <p>{@link TrackMethodEvaluatorProvider} should provide a an evaluator for a configuration that
   * is capable of evaluating tracks via information consistent with the provided {@link
   * L3Adjacencies}.
   *
   * <p>A pre-dataplane evaluator provider should be paired with initial l3 adjacencies, while a
   * dataplane-based evaluator should be paired with dataplane-based l3 adjacencies.
   */
  protected IpOwnersBaseImpl(
      Map<String, Configuration> configurations,
      L3Adjacencies l3Adjacencies,
      TrackMethodEvaluatorProvider trackMethodEvaluatorProvider,
      boolean recordElections) {
    if (recordElections) {
      _hsrpElectionDetails = new ElectionDetails();
      _vrrpElectionDetails = new ElectionDetails();
    } else {
      _hsrpElectionDetails = null;
      _vrrpElectionDetails = null;
    }

    /* Mapping from a hostname to a set of all (including inactive) interfaces that node owns */
    Map<String, Set<Interface>> allInterfaces =
        ImmutableMap.copyOf(computeNodeInterfaces(configurations));

    {
      _allDeviceOwnedIps =
          ImmutableMap.copyOf(
              computeIpInterfaceOwners(
                  allInterfaces,
                  false,
                  l3Adjacencies,
                  NetworkConfigurations.of(configurations),
                  trackMethodEvaluatorProvider,
                  null,
                  null));
      _activeDeviceOwnedIps =
          ImmutableMap.copyOf(
              computeIpInterfaceOwners(
                  allInterfaces,
                  true,
                  l3Adjacencies,
                  NetworkConfigurations.of(configurations),
                  trackMethodEvaluatorProvider,
                  _hsrpElectionDetails,
                  _vrrpElectionDetails));
    }

    {
      Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
          computeIpIfaceOwners(allInterfaces, _activeDeviceOwnedIps);
      _ipVrfOwners = computeIpVrfOwners(ipIfaceOwners);
      _hostToVrfToInterfaceToIpSpace = computeIfaceOwnedIpSpaces(ipIfaceOwners);
    }

    {
      _hostToInterfaceToIpSpace = computeInterfaceOwnedIpSpaces(_hostToVrfToInterfaceToIpSpace);
      _allInterfaceHostIps = computeInterfaceHostSubnetIps(configurations, false);
    }
  }

  @Override
  public @Nullable ElectionDetails getHsrpElectionDetails() {
    return _hsrpElectionDetails;
  }

  @Override
  public @Nullable ElectionDetails getVrrpElectionDetails() {
    return _vrrpElectionDetails;
  }

  @Override
  public final @Nonnull Map<Ip, Map<String, Set<String>>> getActiveDeviceOwnedIps() {
    return _activeDeviceOwnedIps;
  }

  @Override
  public final @Nonnull Map<Ip, Map<String, Set<String>>> getAllDeviceOwnedIps() {
    return _allDeviceOwnedIps;
  }

  @Override
  public final @Nonnull Map<String, Map<String, IpSpace>> getAllInterfaceHostIps() {
    return _allInterfaceHostIps;
  }

  @Override
  public final @Nonnull Map<String, Map<String, IpSpace>> getInterfaceOwnedIpSpaces() {
    return _hostToInterfaceToIpSpace;
  }

  @Override
  public final @Nonnull Map<Ip, Map<String, Set<String>>> getIpVrfOwners() {
    return _ipVrfOwners;
  }

  @Override
  public final @Nonnull Map<String, Map<String, Map<String, IpSpace>>> getVrfIfaceOwnedIpSpaces() {
    return _hostToVrfToInterfaceToIpSpace;
  }

  @Override
  public final @Nonnull Map<Ip, Set<String>> getNodeOwners(boolean excludeInactive) {
    return computeNodeOwners(excludeInactive ? _activeDeviceOwnedIps : _allDeviceOwnedIps);
  }

  @Override
  public final @Nonnull Map<String, Map<String, Set<Ip>>> getInterfaceOwners(
      boolean excludeInactive) {
    return computeInterfaceOwners(excludeInactive ? _activeDeviceOwnedIps : _allDeviceOwnedIps);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof IpOwnersBaseImpl)) {
      return false;
    }
    IpOwnersBaseImpl that = (IpOwnersBaseImpl) o;
    return _allDeviceOwnedIps.equals(that._allDeviceOwnedIps)
        && _activeDeviceOwnedIps.equals(that._activeDeviceOwnedIps)
        && _hostToInterfaceToIpSpace.equals(that._hostToInterfaceToIpSpace)
        && _hostToVrfToInterfaceToIpSpace.equals(that._hostToVrfToInterfaceToIpSpace)
        && _allInterfaceHostIps.equals(that._allInterfaceHostIps)
        && _ipVrfOwners.equals(that._ipVrfOwners);
  }

  @Override
  public int hashCode() {
    // no real use case for avoiding collisions
    return 0;
  }

  @VisibleForTesting
  static @Nonnull Map<String, Map<String, IpSpace>> computeInterfaceHostSubnetIps(
      Map<String, Configuration> configs, boolean excludeInactive) {
    return toImmutableMap(
        configs,
        Entry::getKey, /* hostname */
        nodeEntry ->
            toImmutableMap(
                excludeInactive
                    ? nodeEntry.getValue().getActiveInterfaces()
                    : nodeEntry.getValue().getAllInterfaces(),
                Entry::getKey, /* interface */
                ifaceEntry ->
                    firstNonNull(
                        AclIpSpace.union(
                            ifaceEntry.getValue().getAllConcreteAddresses().stream()
                                .map(ConcreteInterfaceAddress::getPrefix)
                                .map(Prefix::toHostIpSpace)
                                .collect(ImmutableList.toImmutableList())),
                        EmptyIpSpace.INSTANCE)));
  }

  @VisibleForTesting
  static @Nonnull Map<String, Map<String, Set<Ip>>> computeInterfaceOwners(
      Map<Ip, Map<String, Set<String>>> deviceOwnedIps) {
    Map<String, Map<String, Set<Ip>>> ownedIps = new HashMap<>();

    deviceOwnedIps.forEach(
        (ip, owners) ->
            owners.forEach(
                (host, ifaces) ->
                    ifaces.forEach(
                        iface ->
                            ownedIps
                                .computeIfAbsent(host, k -> new HashMap<>())
                                .computeIfAbsent(iface, k -> new HashSet<>())
                                .add(ip))));

    // freeze
    return toImmutableMap(
        ownedIps,
        Entry::getKey, /* host */
        hostEntry ->
            toImmutableMap(
                hostEntry.getValue(),
                Entry::getKey, /* interface */
                ifaceEntry -> ImmutableSet.copyOf(ifaceEntry.getValue())));
  }

  @VisibleForTesting
  static @Nonnull Map<Ip, Set<String>> computeNodeOwners(
      Map<Ip, Map<String, Set<String>>> deviceOwnedIps) {
    return toImmutableMap(
        deviceOwnedIps,
        Entry::getKey, /* Ip */
        ipInterfaceOwnersEntry ->
            /* project away interfaces */
            ipInterfaceOwnersEntry.getValue().keySet());
  }

  /**
   * Compute a mapping from IP address to the interfaces that "own" that IP (e.g., as a network
   * interface address).
   *
   * <p>Takes into account VRRP configuration.
   *
   * @param allInterfaces A mapping of interfaces: hostname -&gt; set of {@link Interface}
   * @param excludeInactive whether to ignore inactive interfaces
   * @param l3Adjacencies L3Adjacencies (used to disambiguate VRRP ownership among multiple domains
   *     that use the same virtual IP)
   * @return A map from {@link Ip}s to hostname to set of interface names that own that IP.
   */
  @VisibleForTesting
  static @Nonnull Map<Ip, Map<String, Set<String>>> computeIpInterfaceOwners(
      Map<String, Set<Interface>> allInterfaces,
      boolean excludeInactive,
      L3Adjacencies l3Adjacencies,
      NetworkConfigurations nc,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails hsrpElectionDetails,
      @Nullable ElectionDetails vrrpElectionDetails) {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    // vrid -> sync interface -> interface to own IPs if sync interface wins election -> IPs
    Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> vrrpGroups = new HashMap<>();
    // group -> interface -> IPs to own if interface wins election
    Map<Integer, Map<NodeInterfacePair, Set<Ip>>> hsrpGroups = new HashMap<>();
    allInterfaces.forEach(
        (hostname, interfaces) ->
            interfaces.forEach(
                i -> {
                  if (!i.getActive() && excludeInactive) {
                    return;
                  }
                  extractVrrp(vrrpGroups, i);
                  extractHsrp(hsrpGroups, i);
                  // collect prefixes
                  i.getAllConcreteAddresses().stream()
                      .map(ConcreteInterfaceAddress::getIp)
                      .forEach(
                          ip ->
                              ipOwners
                                  .computeIfAbsent(ip, k -> new HashMap<>())
                                  .computeIfAbsent(hostname, k -> new HashSet<>())
                                  .add(i.getName()));
                }));
    processVrrpGroups(ipOwners, vrrpGroups, l3Adjacencies, nc, provider, vrrpElectionDetails);
    processHsrpGroups(ipOwners, hsrpGroups, l3Adjacencies, nc, provider, hsrpElectionDetails);

    // freeze
    return toImmutableMap(
        ipOwners,
        Entry::getKey,
        ipOwnersEntry ->
            toImmutableMap(
                ipOwnersEntry.getValue(),
                Entry::getKey, // hostname
                hostIpOwnersEntry -> ImmutableSet.copyOf(hostIpOwnersEntry.getValue())));
  }

  /**
   * Extract HSRP info from a given interface and add it to the {@code hsrpGroups}.
   *
   * @param hsrpGroups Output map: groupid -> interface -> IPs to own if interface wins election
   */
  @VisibleForTesting
  static void extractHsrp(Map<Integer, Map<NodeInterfacePair, Set<Ip>>> hsrpGroups, Interface i) {
    // Inactive interfaces could never win a HSRP election
    if (!i.getActive()) {
      return;
    }
    // collect hsrp info
    i.getHsrpGroups()
        .forEach(
            (groupNum, hsrpGroup) -> {
              Set<Ip> ips = hsrpGroup.getVirtualAddresses();
              /*
               * Invalid HSRP configuration. The HSRP group has no source IP address that
               * would be used for VRRP election. This interface could never win the
               * election, so is not a candidate.
               */
              if (hsrpGroup.getSourceAddress() == null) {
                /*
                 * Invalid VRRP configuration. The VRRP has no source IP address that
                 * would be used for VRRP election. This interface could never win the
                 * election, so is not a candidate.
                 */
                return;
              }
              if (ips.isEmpty()) {
                /*
                 * Invalid HSRP configuration. The HSRP group has no virtual IP addresses set, so
                 * should not participate in HSRP election. This interface could never win the
                 * election, so is not a candidate.
                 *
                 * TODO: Technically according to
                 *       https://datatracker.ietf.org/doc/html/rfc2281#section-5.1 the virtual IP
                 *       (there is only one primary IP communicatd via an HSRP packet) may be
                 *       learned from the active router via a HELLO message, but we do not support
                 *       this mode of operation.
                 */
                return;
              }
              ips.forEach(
                  ip -> {
                    hsrpGroups
                        .computeIfAbsent(groupNum, k -> new HashMap<>())
                        .put(NodeInterfacePair.of(i), hsrpGroup.getVirtualAddresses());
                  });
            });
  }

  /**
   * Take {@code hsrpGroups} table, run master interface selection process, and add that
   * IP/interface pair to ip owners
   */
  @VisibleForTesting
  static void processHsrpGroups(
      Map<Ip, Map<String, Set<String>>> ipOwners,
      Map<Integer, Map<NodeInterfacePair, Set<Ip>>> hsrpGroups,
      L3Adjacencies l3Adjacencies,
      NetworkConfigurations nc,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails electionDetails) {
    hsrpGroups.forEach(
        (groupNum, ipSpaceByCandidate) -> {
          assert groupNum != null;
          Set<NodeInterfacePair> candidates = ipSpaceByCandidate.keySet();

          List<List<NodeInterfacePair>> candidatePartitions =
              partitionCandidates(candidates, l3Adjacencies);

          candidatePartitions.forEach(
              cp -> {
                List<Interface> partitionInterfaces =
                    cp.stream()
                        .map(nip -> nc.getInterface(nip.getHostname(), nip.getInterface()).get())
                        .collect(ImmutableList.toImmutableList());
                if (partitionInterfaces.size() != 2) {
                  LOGGER.warn(
                      "HSRP group {} election is not among 2 devices, which is rare: {}",
                      groupNum,
                      partitionInterfaces);
                }
                /*
                 * Compare priorities first, then highest interface IP, then hostname, then interface name.
                 */
                Map<NodeInterfacePair, Integer> priorities =
                    partitionInterfaces.stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                NodeInterfacePair::of,
                                i ->
                                    computeHsrpPriority(
                                        i,
                                        i.getHsrpGroups().get(groupNum),
                                        groupNum,
                                        provider,
                                        electionDetails)));
                NodeInterfacePair hsrpMaster =
                    NodeInterfacePair.of(
                        Collections.max(
                            partitionInterfaces,
                            Comparator.comparingInt(
                                    (Interface o) -> priorities.get(NodeInterfacePair.of(o)))
                                .thenComparing(o -> o.getConcreteAddress().getIp())
                                .thenComparing(o -> NodeInterfacePair.of(o))));
                if (electionDetails != null) {
                  cp.forEach(
                      ni -> {
                        electionDetails
                            ._winnerByCandidate
                            .computeIfAbsent(ni, n -> new HashMap<>())
                            .put(groupNum, hsrpMaster);
                        electionDetails
                            ._candidatesByCandidate
                            .computeIfAbsent(ni, n -> new HashMap<>())
                            .put(groupNum, ImmutableSet.copyOf(cp));
                      });
                }
                LOGGER.debug(
                    "{} elected HSRP master for groupNum {} among candidates {}",
                    hsrpMaster,
                    groupNum,
                    partitionInterfaces);
                ipSpaceByCandidate
                    .get(hsrpMaster)
                    .forEach(
                        ip ->
                            ipOwners
                                .computeIfAbsent(ip, k -> new HashMap<>())
                                .computeIfAbsent(hsrpMaster.getHostname(), k -> new HashSet<>())
                                .add(hsrpMaster.getInterface()));
              });
        });
  }

  /** Compute priority for a given HSRP group and the interface it is associated with. */
  @VisibleForTesting
  static int computeHsrpPriority(
      Interface iface,
      HsrpGroup group,
      int groupNum,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails electionDetails) {
    return computePriority(
        iface, group.getPriority(), groupNum, group.getTrackActions(), provider, electionDetails);
  }

  /** Compute priority for a given VRRP group and the interface it is associated with. */
  @VisibleForTesting
  static int computeVrrpPriority(
      Interface iface,
      VrrpGroup group,
      int vrid,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails electionDetails) {
    return computePriority(
        iface, group.getPriority(), vrid, group.getTrackActions(), provider, electionDetails);
  }

  /**
   * Extract VRRP info from a given interface and add it to the {@code vrrpGroups}.
   *
   * @param vrrpGroups Output map: vrid -> sync interface -> interface to own IPs if sync interface
   *     wins election -> IPs
   */
  @VisibleForTesting
  static void extractVrrp(
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> vrrpGroups, Interface i) {
    // Inactive interfaces could never win a VRRP election
    if (!i.getActive()) {
      return;
    }
    i.getVrrpGroups()
        .forEach(
            (vrid, vrrpGroup) -> {
              if (vrrpGroup.getSourceAddress() == null) {
                /*
                 * Invalid VRRP configuration. The VRRP has no source IP address that
                 * would be used for VRRP election. This interface could never win the
                 * election, so is not a candidate.
                 */
                return;
              }
              if (vrrpGroup.getVirtualAddresses().isEmpty()) {
                /*
                 * Invalid VRRP configuration. The VRRP group has no virtual IP addresses set, so
                 * should not participate in VRRP election. This interface could never win the
                 * election, so is not a candidate.
                 */
                return;
              }
              // sync interface -> interface to receive IPs -> IPs
              vrrpGroups
                  .computeIfAbsent(vrid, k -> new HashMap<>())
                  .put(NodeInterfacePair.of(i), vrrpGroup.getVirtualAddresses());
            });
  }

  /**
   * Take {@code vrrpGroups} table, run master interface selection process, and add that
   * IP/interface pair to ip owners
   */
  @VisibleForTesting
  static void processVrrpGroups(
      Map<Ip, Map<String, Set<String>>> ipOwners,
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> vrrpGroups,
      L3Adjacencies l3Adjacencies,
      NetworkConfigurations nc,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails electionDetails) {
    vrrpGroups.forEach(
        (vrid, ipSpaceByCandidate) -> {
          assert vrid != null;
          Set<NodeInterfacePair> candidates = ipSpaceByCandidate.keySet();

          List<List<NodeInterfacePair>> candidatePartitions =
              partitionCandidates(candidates, l3Adjacencies);

          candidatePartitions.forEach(
              cp -> {
                List<Interface> partitionInterfaces =
                    cp.stream()
                        .map(nip -> nc.getInterface(nip.getHostname(), nip.getInterface()).get())
                        .collect(ImmutableList.toImmutableList());
                if (partitionInterfaces.size() != 2) {
                  LOGGER.warn(
                      "VRRP vrid {} election is not among 2 devices, which is rare: {}",
                      vrid,
                      partitionInterfaces);
                }
                /*
                 * Compare priorities first, then highest interface IP, then hostname, then interface name.
                 */
                Map<NodeInterfacePair, Integer> priorities =
                    partitionInterfaces.stream()
                        .collect(
                            ImmutableMap.toImmutableMap(
                                NodeInterfacePair::of,
                                i ->
                                    computeVrrpPriority(
                                        i,
                                        i.getVrrpGroups().get(vrid),
                                        vrid,
                                        provider,
                                        electionDetails)));
                NodeInterfacePair vrrpMaster =
                    NodeInterfacePair.of(
                        Collections.max(
                            partitionInterfaces,
                            Comparator.comparingInt(
                                    (Interface o) -> priorities.get(NodeInterfacePair.of(o)))
                                .thenComparing(o -> o.getConcreteAddress().getIp())
                                .thenComparing(o -> NodeInterfacePair.of(o))));
                if (electionDetails != null) {
                  cp.forEach(
                      ni -> {
                        electionDetails
                            ._winnerByCandidate
                            .computeIfAbsent(ni, n -> new HashMap<>())
                            .put(vrid, vrrpMaster);
                        electionDetails
                            ._candidatesByCandidate
                            .computeIfAbsent(ni, n -> new HashMap<>())
                            .put(vrid, ImmutableSet.copyOf(cp));
                      });
                }
                LOGGER.debug(
                    "{} elected VRRP master for vrid {} among candidates {}",
                    vrrpMaster,
                    vrid,
                    partitionInterfaces);
                ipSpaceByCandidate
                    .get(vrrpMaster)
                    .forEach(
                        (receivingInterface, ips) ->
                            ips.forEach(
                                ip ->
                                    ipOwners
                                        .computeIfAbsent(ip, k -> new HashMap<>())
                                        .computeIfAbsent(
                                            vrrpMaster.getHostname(), k -> new HashSet<>())
                                        .add(receivingInterface)));
              });
        });
  }

  /**
   * Partitions the input set of HSRP/VRRP candidates into subsets where all candidates are in the
   * same broadcast domain. This disambiguates VRRP groups that have the same IP and group ID
   */
  @VisibleForTesting
  static @Nonnull List<List<NodeInterfacePair>> partitionCandidates(
      Set<NodeInterfacePair> candidates, L3Adjacencies l3Adjacencies) {
    Map<NodeInterfacePair, List<NodeInterfacePair>> partitions = new HashMap<>();
    for (NodeInterfacePair cni : candidates) {
      boolean foundRepresentative = false;
      for (NodeInterfacePair representative : partitions.keySet()) {
        if (l3Adjacencies.inSameBroadcastDomain(representative, cni)) {
          partitions.get(representative).add(cni);
          foundRepresentative = true;
          break;
        }
      }
      if (!foundRepresentative) {
        partitions.put(cni, new LinkedList<>());
        partitions.get(cni).add(cni);
      }
    }
    return ImmutableList.copyOf(partitions.values());
  }

  /**
   * Compute a mapping of IP addresses to the VRFs that "own" this IP (e.g., as a network interface
   * address).
   *
   * @param ipVrfIfaceOwners A mapping of IP owners hostname -&gt; vrf name -&gt; interface names
   * @return A map of {@link Ip}s to a map of hostnames to vrfs that own the Ip.
   */
  @VisibleForTesting
  static @Nonnull Map<Ip, Map<String, Set<String>>> computeIpVrfOwners(
      Map<Ip, Map<String, Map<String, Set<String>>>> ipVrfIfaceOwners) {
    return toImmutableMap(
        ipVrfIfaceOwners,
        Entry::getKey, /* Ip */
        ipEntry ->
            toImmutableMap(
                ipEntry.getValue(),
                Entry::getKey, /* Hostname */
                nodeEntry -> ImmutableSet.copyOf(nodeEntry.getValue().keySet()))); /* VRFs */
  }

  /**
   * Compute a mapping of IP addresses to the VRFs and interfaces that "own" this IP (e.g., as a
   * network interface address).
   *
   * @param allInterfaces A mapping of enabled interfaces hostname -&gt; set of {@link Interface}
   * @param activeDeviceOwnedIps Mapping from a IP to hostname to set of interfaces that own that IP
   *     (for active interfaces only)
   * @return A map of {@link Ip}s to a map of hostnames to vrfs to interfaces that own the Ip.
   */
  @VisibleForTesting
  static @Nonnull Map<Ip, Map<String, Map<String, Set<String>>>> computeIpIfaceOwners(
      Map<String, Set<Interface>> allInterfaces,
      Map<Ip, Map<String, Set<String>>> activeDeviceOwnedIps) {

    // Helper mapping: hostname -> vrf -> interfaces
    Map<String, Map<String, Set<String>>> hostsToVrfsToIfaces = new HashMap<>();
    allInterfaces.forEach(
        (hostname, ifaces) ->
            ifaces.forEach(
                iface -> {
                  hostsToVrfsToIfaces
                      .computeIfAbsent(hostname, n -> new HashMap<>())
                      .computeIfAbsent(iface.getVrfName(), n -> new HashSet<>())
                      .add(iface.getName());
                }));

    return toImmutableMap(
        activeDeviceOwnedIps,
        Entry::getKey, /* Ip */
        ipEntry ->
            toImmutableMap(
                ipEntry.getValue(),
                Entry::getKey, /* hostname */
                nodeEntry -> {
                  String hostname = nodeEntry.getKey();
                  Set<String> ownerIfaces = nodeEntry.getValue();
                  return hostsToVrfsToIfaces.get(hostname).entrySet().stream()
                      // Filter to VRFs containing interfaces that own this IP
                      .filter(vrfEntry -> !Collections.disjoint(vrfEntry.getValue(), ownerIfaces))
                      .collect(
                          // Map each VRF to its set of interfaces that own this IP
                          ImmutableMap.toImmutableMap(
                              Entry::getKey,
                              vrfEntry -> Sets.intersection(vrfEntry.getValue(), ownerIfaces)));
                }));
  }

  private static final Logger LOGGER = LogManager.getLogger(IpOwnersBaseImpl.class);

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  private final @Nonnull Map<Ip, Map<String, Set<String>>> _allDeviceOwnedIps;

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  private final @Nonnull Map<Ip, Map<String, Set<String>>> _activeDeviceOwnedIps;

  /** Mapping from hostname to interface name to IpSpace owned by that interface */
  private final @Nonnull Map<String, Map<String, IpSpace>> _hostToInterfaceToIpSpace;

  /** Mapping from hostname to VRF name to interface name to IpSpace owned by that interface */
  private final @Nonnull Map<String, Map<String, Map<String, IpSpace>>>
      _hostToVrfToInterfaceToIpSpace;

  /**
   * Mapping from hostname to interface name to host IP subnet.
   *
   * @see Prefix#toHostIpSpace()
   */
  private final @Nonnull Map<String, Map<String, IpSpace>> _allInterfaceHostIps;

  /** Mapping from an IP to hostname to set of VRFs that own that IP. */
  private final @Nonnull Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

  private final @Nullable ElectionDetails _hsrpElectionDetails;
  private final @Nullable ElectionDetails _vrrpElectionDetails;

  private static int computePriority(
      @Nonnull Interface iface,
      int initialPriority,
      int groupOrVrid,
      @Nonnull Map<String, TrackAction> trackActions,
      TrackMethodEvaluatorProvider provider,
      @Nullable ElectionDetails electionDetails) {
    Configuration c = iface.getOwner();
    Map<String, TrackMethod> trackMethods = c.getTrackingGroups();
    GenericTrackMethodVisitor<Boolean> trackMethodEvaluator = provider.forConfiguration(c);
    PriorityEvaluator evaluator = new PriorityEvaluator(initialPriority);
    Map<String, Entry<TrackMethod, TrackAction>> successfulTracks;
    Map<String, Entry<TrackMethod, TrackAction>> failedTracks;
    NodeInterfacePair ni;
    if (electionDetails != null) {
      ni = NodeInterfacePair.of(iface);
      successfulTracks =
          electionDetails
              ._successfulTracks
              .computeIfAbsent(ni, n -> new HashMap<>())
              .computeIfAbsent(groupOrVrid, g -> new HashMap<>());
      failedTracks =
          electionDetails
              ._failedTracks
              .computeIfAbsent(ni, n -> new HashMap<>())
              .computeIfAbsent(groupOrVrid, g -> new HashMap<>());
    } else {
      ni = null;
      successfulTracks = null;
      failedTracks = null;
    }
    trackActions.forEach(
        (trackName, action) -> {
          TrackMethod trackMethod = trackMethods.get(trackName);
          assert trackMethod != null;
          if (trackMethod.accept(trackMethodEvaluator)) {
            action.accept(evaluator);
            if (electionDetails != null) {
              successfulTracks.put(trackName, immutableEntry(trackMethod, action));
            }
          } else if (electionDetails != null) {
            failedTracks.put(trackName, immutableEntry(trackMethod, action));
          }
        });
    int actualPriority = evaluator.getPriority();
    if (electionDetails != null) {
      electionDetails
          ._actualPriorities
          .computeIfAbsent(ni, n -> new HashMap<>())
          .put(groupOrVrid, actualPriority);
    }
    return actualPriority;
  }

  /**
   * Invert a mapping from Ip to interface owners (Ip -&gt; host name -&gt; VRF name -&gt; interface
   * names) and combine all IPs owned by each interface into an IpSpace.
   */
  private static @Nonnull Map<String, Map<String, Map<String, IpSpace>>> computeIfaceOwnedIpSpaces(
      Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners) {
    Map<String, Map<String, Map<String, IpWildcardSetIpSpace.Builder>>> builders = new HashMap<>();
    ipIfaceOwners.forEach(
        (ip, nodeMap) ->
            nodeMap.forEach(
                (node, vrfMap) ->
                    vrfMap.forEach(
                        (vrf, ifaces) ->
                            ifaces.forEach(
                                iface ->
                                    builders
                                        .computeIfAbsent(node, k -> new HashMap<>())
                                        .computeIfAbsent(vrf, k -> new HashMap<>())
                                        .computeIfAbsent(iface, k -> IpWildcardSetIpSpace.builder())
                                        .including(IpWildcard.create(ip))))));
    return toImmutableMap(
        builders,
        Entry::getKey, /* node */
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue(),
                Entry::getKey, /* vrf */
                vrfEntry ->
                    toImmutableMap(
                        vrfEntry.getValue(),
                        Entry::getKey, /* interface */
                        ifaceEntry -> ifaceEntry.getValue().build())));
  }

  /**
   * Computes a map of hostname -&gt; interface name -&gt; {@link IpSpace} from a map of hostname
   * -&gt; vrf name -&gt; interface name -&gt; {@link IpSpace}.
   */
  private static @Nonnull Map<String, Map<String, IpSpace>> computeInterfaceOwnedIpSpaces(
      Map<String, Map<String, Map<String, IpSpace>>> ipOwners) {
    return toImmutableMap(
        ipOwners,
        Entry::getKey, /* host */
        hostEntry ->
            hostEntry.getValue().values().stream() /* Skip VRF keys */
                .flatMap(ifaceMap -> ifaceMap.entrySet().stream())
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));
  }
}
