package org.batfish.common.topology;

import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
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

  /** Mapping from hostname to interface name to IpSpace owned by that interface */
  private final Map<String, Map<String, IpSpace>> _hostToInterfaceToIpSpace;

  /** Mapping from hostname to VRF name to interface name to IpSpace owned by that interface */
  private final Map<String, Map<String, Map<String, IpSpace>>> _hostToVrfToInterfaceToIpSpace;

  /** Mapping from an IP to hostname to set of VRFs that own that IP. */
  private final Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

  public IpOwners(Map<String, Configuration> configurations) {
    /* Mapping from a hostname to a set of all (including inactive) interfaces that node owns */
    Map<String, Set<Interface>> allInterfaces =
        ImmutableMap.copyOf(computeNodeInterfaces(configurations));

    /* Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces only) */
    Map<Ip, Map<String, Set<String>>> activeDeviceOwnedIps;
    {
      _allDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(allInterfaces, false));
      activeDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(allInterfaces, true));
    }

    {
      Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
          computeIpIfaceOwners(allInterfaces, activeDeviceOwnedIps);
      _ipVrfOwners = computeIpVrfOwners(ipIfaceOwners);
      _hostToVrfToInterfaceToIpSpace = computeIfaceOwnedIpSpaces(ipIfaceOwners);
    }

    {
      _hostToInterfaceToIpSpace = computeInterfaceOwnedIpSpaces(_hostToVrfToInterfaceToIpSpace);
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

  /**
   * Compute the {@link Ip}s owned by each interface. hostname -&gt; interface name -&gt; {@link
   * Ip}s.
   */
  public static Map<String, Map<String, Set<Ip>>> computeInterfaceOwnedIps(
      Map<String, Configuration> configurations, boolean excludeInactive) {
    // TODO: cleanup callers, make this private
    return computeInterfaceOwnedIps(
        computeIpInterfaceOwners(computeNodeInterfaces(configurations), excludeInactive));
  }

  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * to (hostname -&gt; interface name -&gt; Ip).
   */
  private static Map<String, Map<String, Set<Ip>>> computeInterfaceOwnedIps(
      Map<Ip, Map<String, Set<String>>> ipInterfaceOwners) {
    Map<String, Map<String, Set<Ip>>> ownedIps = new HashMap<>();

    ipInterfaceOwners.forEach(
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
   * Compute a mapping of IP addresses to a set of hostnames that "own" this IP (e.g., as a network
   * interface address)
   *
   * @param configurations map of configurations keyed by hostname
   * @param excludeInactive Whether to exclude inactive interfaces
   * @return A map of {@link Ip}s to a set of hostnames that own this IP
   */
  public static Map<Ip, Set<String>> computeIpNodeOwners(
      Map<String, Configuration> configurations, boolean excludeInactive) {
    Span span =
        GlobalTracer.get()
            .buildSpan("TopologyUtil.computeIpNodeOwners excludeInactive=" + excludeInactive)
            .start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      return toImmutableMap(
          computeIpInterfaceOwners(computeNodeInterfaces(configurations), excludeInactive),
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
   * @return A map from {@link Ip}s to hostname to set of interface names that own that IP.
   */
  @VisibleForTesting
  static Map<Ip, Map<String, Set<String>>> computeIpInterfaceOwners(
      Map<String, Set<Interface>> allInterfaces, boolean excludeInactive) {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Table<ConcreteInterfaceAddress, Integer, Set<Interface>> vrrpGroups = HashBasedTable.create();
    Table<Ip, Integer, Set<Interface>> hsrpGroups = HashBasedTable.create();
    allInterfaces.forEach(
        (hostname, interfaces) ->
            interfaces.forEach(
                i -> {
                  if ((!i.getActive() || i.getBlacklisted()) && excludeInactive) {
                    return;
                  }
                  // collect vrrp info
                  i.getVrrpGroups()
                      .forEach(
                          (groupNum, vrrpGroup) -> {
                            ConcreteInterfaceAddress address = vrrpGroup.getVirtualAddress();
                            if (address == null) {
                              /*
                               * Invalid VRRP configuration. The VRRP has no source IP address that
                               * would be used for VRRP election. This interface could never win the
                               * election, so is not a candidate.
                               */
                              return;
                            }
                            Set<Interface> candidates = vrrpGroups.get(address, groupNum);
                            if (candidates == null) {
                              candidates = Collections.newSetFromMap(new IdentityHashMap<>());
                              vrrpGroups.put(address, groupNum, candidates);
                            }
                            candidates.add(i);
                          });
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
    vrrpGroups
        .cellSet()
        .forEach(
            cell -> {
              ConcreteInterfaceAddress address = cell.getRowKey();
              assert address != null;
              Integer groupNum = cell.getColumnKey();
              assert groupNum != null;
              Set<Interface> candidates = cell.getValue();
              assert candidates != null;
              /*
               * Compare priorities first. If tied, break tie based on highest interface IP.
               */
              Interface vrrpMaster =
                  Collections.max(
                      candidates,
                      Comparator.comparingInt(
                              (Interface o) -> o.getVrrpGroups().get(groupNum).getPriority())
                          .thenComparing(o -> o.getConcreteAddress().getIp()));
              ipOwners
                  .computeIfAbsent(address.getIp(), k -> new HashMap<>())
                  .computeIfAbsent(vrrpMaster.getOwner().getHostname(), k -> new HashSet<>())
                  .add(vrrpMaster.getName());
            });
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
    Map<String, Map<String, Map<String, AclIpSpace.Builder>>> builders = new HashMap<>();
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
                                        .computeIfAbsent(iface, k -> AclIpSpace.builder())
                                        .thenPermitting(ip.toIpSpace())))));
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
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  public Map<Ip, Map<String, Set<String>>> getAllDeviceOwnedIps() {
    return _allDeviceOwnedIps;
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
