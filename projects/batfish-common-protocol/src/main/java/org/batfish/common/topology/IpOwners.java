package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.HsrpPriorityEvaluator;
import org.batfish.datamodel.tracking.PredicateTrackMethodEvaluator;
import org.batfish.datamodel.tracking.TrackMethod;

/** A utility class for working with IPs owned by network devices. */
public final class IpOwners {

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  private final Map<Ip, Map<String, Set<String>>> _allDeviceOwnedIps;

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  private final Map<Ip, Map<String, Set<String>>> _activeDeviceOwnedIps;

  /** Mapping from hostname to interface name to IpSpace owned by that interface */
  private final Map<String, Map<String, IpSpace>> _hostToInterfaceToIpSpace;

  /** Mapping from hostname to VRF name to interface name to IpSpace owned by that interface */
  private final Map<String, Map<String, Map<String, IpSpace>>> _hostToVrfToInterfaceToIpSpace;

  /**
   * Mapping from hostname to interface name to host IP subnet.
   *
   * @see Prefix#toHostIpSpace()
   */
  private final Map<String, Map<String, IpSpace>> _allInterfaceHostIps;

  /** Mapping from an IP to hostname to set of VRFs that own that IP. */
  private final Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

  public IpOwners(Map<String, Configuration> configurations, L3Adjacencies l3Adjacencies) {
    /* Mapping from a hostname to a set of all (including inactive) interfaces that node owns */
    Map<String, Set<Interface>> allInterfaces =
        ImmutableMap.copyOf(computeNodeInterfaces(configurations));

    {
      _allDeviceOwnedIps =
          ImmutableMap.copyOf(computeIpInterfaceOwners(allInterfaces, false, l3Adjacencies));
      _activeDeviceOwnedIps =
          ImmutableMap.copyOf(computeIpInterfaceOwners(allInterfaces, true, l3Adjacencies));
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

  /**
   * Computes a map of hostname -&gt; interface name -&gt; {@link IpSpace} from a map of hostname
   * -&gt; vrf name -&gt; interface name -&gt; {@link IpSpace}.
   */
  private static Map<String, Map<String, IpSpace>> computeInterfaceOwnedIpSpaces(
      Map<String, Map<String, Map<String, IpSpace>>> ipOwners) {
    return toImmutableMap(
        ipOwners,
        Entry::getKey, /* host */
        hostEntry ->
            hostEntry.getValue().values().stream() /* Skip VRF keys */
                .flatMap(ifaceMap -> ifaceMap.entrySet().stream())
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeInterfaceHostSubnetIps(
      Map<String, Configuration> configs, boolean excludeInactive) {
    Span span = GlobalTracer.get().buildSpan("IpOwners.computeInterfaceHostSubnetIps").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning
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
    } finally {
      span.finish();
    }
  }

  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * to (hostname -&gt; interface name -&gt; Ip).
   */
  public Map<String, Map<String, Set<Ip>>> getInterfaceOwners(boolean excludeInactive) {
    return computeInterfaceOwners(excludeInactive ? _activeDeviceOwnedIps : _allDeviceOwnedIps);
  }

  @VisibleForTesting
  static Map<String, Map<String, Set<Ip>>> computeInterfaceOwners(
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

  /**
   * Returns a mapping of IP addresses to a set of hostnames that "own" this IP (e.g., as a network
   * interface address)
   *
   * @param excludeInactive Whether to exclude inactive interfaces
   * @return A map of {@link Ip}s to a set of hostnames that own this IP
   */
  public Map<Ip, Set<String>> getNodeOwners(boolean excludeInactive) {
    return computeNodeOwners(excludeInactive ? _activeDeviceOwnedIps : _allDeviceOwnedIps);
  }

  @VisibleForTesting
  static Map<Ip, Set<String>> computeNodeOwners(Map<Ip, Map<String, Set<String>>> deviceOwnedIps) {
    Span span = GlobalTracer.get().buildSpan("TopologyUtil.computeNodeOwners").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      return toImmutableMap(
          deviceOwnedIps,
          Entry::getKey, /* Ip */
          ipInterfaceOwnersEntry ->
              /* project away interfaces */
              ipInterfaceOwnersEntry.getValue().keySet());
    } finally {
      span.finish();
    }
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
  static Map<Ip, Map<String, Set<String>>> computeIpInterfaceOwners(
      Map<String, Set<Interface>> allInterfaces,
      boolean excludeInactive,
      L3Adjacencies l3Adjacencies) {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    // vrid -> interface -> ips owned by interface if it wins election
    Map<Integer, Map<Interface, Set<Ip>>> vrrpGroups = new HashMap<>();
    Table<Ip, Integer, Set<Interface>> hsrpGroups = HashBasedTable.create();
    allInterfaces.forEach(
        (hostname, interfaces) ->
            interfaces.forEach(
                i -> {
                  if ((!i.getActive() || i.getBlacklisted()) && excludeInactive) {
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
    processVrrpGroups(ipOwners, vrrpGroups, l3Adjacencies);
    processHsrpGroups(ipOwners, hsrpGroups);

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

  /** extract HSRP info from a given interface and add it to the {@code hsrpGroups} table */
  @VisibleForTesting
  static void extractHsrp(Table<Ip, Integer, Set<Interface>> hsrpGroups, Interface i) {
    // collect hsrp info
    i.getHsrpGroups()
        .values()
        .forEach(
            g -> {
              Set<Ip> ips = g.getIps();
              int groupNum = g.getGroupNumber();
              if (ips.isEmpty()) {
                return;
              }
              ips.forEach(
                  ip -> {
                    Set<Interface> candidates = hsrpGroups.get(ip, groupNum);
                    if (candidates == null) {
                      candidates = Collections.newSetFromMap(new IdentityHashMap<>());
                      hsrpGroups.put(ip, groupNum, candidates);
                    }
                    if (i.getConcreteAddress() != null) {
                      // Only interfaces that have IP addresses are considered valid.
                      candidates.add(i);
                    }
                  });
            });
  }

  /**
   * Take {@code hsrpGroups} table, run master interface selection process, and add that
   * IP/interface pair to ip owners
   */
  @VisibleForTesting
  static void processHsrpGroups(
      Map<Ip, Map<String, Set<String>>> ipOwners, Table<Ip, Integer, Set<Interface>> hsrpGroups) {
    hsrpGroups
        .cellSet()
        .forEach(
            cell -> {
              Ip ip = cell.getRowKey();
              assert ip != null;
              Integer groupNum = cell.getColumnKey();
              assert groupNum != null;
              Set<Interface> candidates = cell.getValue();
              assert candidates != null;
              if (candidates.isEmpty()) {
                // No interfaces can actually be the master for this group.
                return;
              }
              /*
               * Compare priorities first. If tied, break tie based on highest interface IP.
               */
              Interface hsrpMaster =
                  Collections.max(
                      candidates,
                      Comparator.comparingInt(
                              (Interface i) ->
                                  computeHsrpPriority(i, i.getHsrpGroups().get(groupNum)))
                          .thenComparing(i -> i.getConcreteAddress().getIp()));
              ipOwners
                  .computeIfAbsent(ip, k -> new HashMap<>())
                  .computeIfAbsent(hsrpMaster.getOwner().getHostname(), k -> new HashSet<>())
                  .add(hsrpMaster.getName());
            });
  }

  /** Compute HSRP priority for a given HSRP group and the interface it is associated with. */
  static int computeHsrpPriority(@Nonnull Interface iface, @Nonnull HsrpGroup group) {
    Configuration c = iface.getOwner();
    Map<String, TrackMethod> trackMethods = c.getTrackingGroups();
    PredicateTrackMethodEvaluator trackMethodEvaluator = new PredicateTrackMethodEvaluator(c);
    HsrpPriorityEvaluator hsrpEvaluator = new HsrpPriorityEvaluator(group.getPriority());
    group
        .getTrackActions()
        .forEach(
            (trackName, action) -> {
              TrackMethod trackMethod = trackMethods.get(trackName);
              if (trackMethod != null && trackMethod.accept(trackMethodEvaluator)) {
                action.accept(hsrpEvaluator);
              }
            });
    return hsrpEvaluator.getPriority();
  }

  /** extract VRRP info from a given interface and add it to the {@code vrrpGroups} table */
  @VisibleForTesting
  static void extractVrrp(Map<Integer, Map<Interface, Set<Ip>>> vrrpGroups, Interface i) {
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
                 * Invalid VRRP configuration. The VRRP has no virtual IP addresses set, so should
                 * not participate in  VRRP election. This interface could never win the election,
                 * so is not a candidate.
                 */
                return;
              }
              Map<Interface, Set<Ip>> candidates = vrrpGroups.get(vrid);
              if (candidates == null) {
                candidates = new IdentityHashMap<>();
                vrrpGroups.put(vrid, candidates);
              }
              candidates.put(i, i.getVrrpGroups().get(vrid).getVirtualAddresses());
            });
  }

  /**
   * Take {@code vrrpGroups} table, run master interface selection process, and add that
   * IP/interface pair to ip owners
   */
  static void processVrrpGroups(
      Map<Ip, Map<String, Set<String>>> ipOwners,
      Map<Integer, Map<Interface, Set<Ip>>> vrrpGroups,
      L3Adjacencies l3Adjacencies) {
    vrrpGroups.forEach(
        (vrid, ipSpaceByCandidate) -> {
          assert vrid != null;
          Set<Interface> candidates = ipSpaceByCandidate.keySet();

          Set<Set<Interface>> candidatePartitions =
              partitionVrrpCandidates(candidates, l3Adjacencies);

          candidatePartitions.forEach(
              cp -> {
                /*
                 * Compare priorities first, then highest interface IP, then hostname, then interface name.
                 */
                Interface vrrpMaster =
                    Collections.max(
                        cp,
                        Comparator.comparingInt(
                                (Interface o) -> o.getVrrpGroups().get(vrid).getPriority())
                            .thenComparing(o -> o.getConcreteAddress().getIp())
                            .thenComparing(o -> NodeInterfacePair.of(o)));
                ipSpaceByCandidate
                    .get(vrrpMaster)
                    .forEach(
                        ip ->
                            ipOwners
                                .computeIfAbsent(ip, k -> new HashMap<>())
                                .computeIfAbsent(
                                    vrrpMaster.getOwner().getHostname(), k -> new HashSet<>())
                                .add(vrrpMaster.getName()));
              });
        });
  }

  /**
   * Partitions the input set of VRRP candidates into subsets where all candidates are in the same
   * broadcast domain. This disambiguates VRRP groups that have the same IP and group ID
   */
  @VisibleForTesting
  static Set<Set<Interface>> partitionVrrpCandidates(
      Set<Interface> candidates, L3Adjacencies l3Adjacencies) {
    Map<NodeInterfacePair, Set<Interface>> partitions = new HashMap<>();
    for (Interface c : candidates) {
      boolean foundRepresentative = false;
      NodeInterfacePair cni = NodeInterfacePair.of(c);
      for (NodeInterfacePair representative : partitions.keySet()) {
        if (l3Adjacencies.inSameBroadcastDomain(representative, cni)) {
          partitions.get(representative).add(c);
          foundRepresentative = true;
          break;
        }
      }
      if (!foundRepresentative) {
        partitions.put(cni, new HashSet<>());
        partitions.get(cni).add(c);
      }
    }
    return ImmutableSet.copyOf(partitions.values());
  }

  /**
   * Compute a mapping of IP addresses to the VRFs that "own" this IP (e.g., as a network interface
   * address).
   *
   * @param ipVrfIfaceOwners A mapping of IP owners hostname -&gt; vrf name -&gt; interface names
   * @return A map of {@link Ip}s to a map of hostnames to vrfs that own the Ip.
   */
  @VisibleForTesting
  static Map<Ip, Map<String, Set<String>>> computeIpVrfOwners(
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
  static Map<Ip, Map<String, Map<String, Set<String>>>> computeIpIfaceOwners(
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

  /**
   * Invert a mapping from Ip to interface owners (Ip -&gt; host name -&gt; VRF name -&gt; interface
   * names) and combine all IPs owned by each interface into an IpSpace.
   */
  private static Map<String, Map<String, Map<String, IpSpace>>> computeIfaceOwnedIpSpaces(
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
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  public Map<Ip, Map<String, Set<String>>> getActiveDeviceOwnedIps() {
    return _activeDeviceOwnedIps;
  }

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  public Map<Ip, Map<String, Set<String>>> getAllDeviceOwnedIps() {
    return _allDeviceOwnedIps;
  }

  /**
   * Returns a mapping from hostname to interface name to the host {@link IpSpace} of that
   * interface, including inactive interfaces.
   *
   * @see Prefix#toHostIpSpace()
   */
  public Map<String, Map<String, IpSpace>> getAllInterfaceHostIps() {
    return _allInterfaceHostIps;
  }

  /**
   * Returns a mapping from hostname to interface name to IpSpace owned by that interface, for
   * active interfaces only
   */
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIpSpaces() {
    return _hostToInterfaceToIpSpace;
  }

  /** Returns a mapping from IP to hostname to set of VRFs that own that IP. */
  public Map<Ip, Map<String, Set<String>>> getIpVrfOwners() {
    return _ipVrfOwners;
  }

  /**
   * Returns a mapping from hostname to vrf name to interface name to a space of IPs owned by that
   * interface. Only considers interface IPs. Considers <em>only active</em> interfaces.
   */
  public Map<String, Map<String, Map<String, IpSpace>>> getVrfIfaceOwnedIpSpaces() {
    return _hostToVrfToInterfaceToIpSpace;
  }
}
