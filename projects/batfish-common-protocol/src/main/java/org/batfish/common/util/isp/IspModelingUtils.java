package org.batfish.common.util.isp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.BgpSessionProperties.SessionType.EBGP_SINGLEHOP;
import static org.batfish.datamodel.BgpSessionProperties.getSessionType;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.specifier.Location.interfaceLinkLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BgpPeerInfo;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspAttachment;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspNodeInfo;
import org.batfish.datamodel.isp_configuration.IspNodeInfo.Role;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.specifier.LocationInfo;

/** Util classes and functions to model ISPs and Internet for a given network */
@ParametersAreNonnullByDefault
public final class IspModelingUtils {
  static final Prefix INTERNET_OUT_SUBNET = Prefix.parse("240.254.254.0/30");

  static final LocationInfo INTERNET_OUT_INTERFACE_LINK_LOCATION_INFO =
      new LocationInfo(
          // use as a source
          true,
          // pick any source IP (excluding snapshot owned IPs)
          UniverseIpSpace.INSTANCE,
          // pretend there's a neighbor that responds to ARP, so we get EXITS_NETWORK instead of
          // NEIGHBOR_UNREACHABLE for traffic routed to the internet
          INTERNET_OUT_SUBNET.getLastHostIp().toIpSpace());

  static final String EXPORT_POLICY_ON_INTERNET = "exportPolicyOnInternet";
  static final String EXPORT_POLICY_ON_ISP_TO_CUSTOMERS = "exportPolicyOnIspToCustomers";
  static final String EXPORT_POLICY_ON_ISP_TO_INTERNET = "exportPolicyOnIspToInternet";
  static final String EXPORT_POLICY_FOR_ISP_PEERING = "exportPolicyForIspPeering";
  static final long INTERNET_AS = 65537L;
  public static final String INTERNET_HOST_NAME = "internet";
  static final Ip INTERNET_OUT_ADDRESS = INTERNET_OUT_SUBNET.getFirstHostIp();
  public static final String INTERNET_OUT_INTERFACE = "out";
  static final Ip LINK_LOCAL_IP = Ip.parse("169.254.0.1");
  static final LinkLocalAddress LINK_LOCAL_ADDRESS = LinkLocalAddress.of(LINK_LOCAL_IP);
  static final String ISP_TO_INTERNET_INTERFACE_NAME = "To-Internet";

  // null routing private address space at the internet prevents "INSUFFICIENT_INFO" for networks
  // that use this space internally
  public static final Set<Prefix> INTERNET_NULL_ROUTED_PREFIXES =
      ImmutableSet.of(
          Prefix.parse("10.0.0.0/8"),
          Prefix.parse("172.16.0.0/12"),
          Prefix.parse("192.168.0.0/16"));

  /** Use this cost to install static routes on ISP nodes for prefixes originated to the Internet */
  static final int HIGH_ADMINISTRATIVE_COST = AbstractRoute.MAX_ADMIN_DISTANCE;

  /** Returns the hostname that will be used for the ISP model with the given ASN. */
  public static String getDefaultIspNodeName(long asn) {
    return String.format("isp_%s", asn);
  }

  public static String internetToIspInterfaceName(String ispHostname) {
    return "To-" + ispHostname;
  }

  public static String ispToSnapshotInterfaceName(
      String snapshotHostname, String snapshotInterfaceName) {
    return "To-" + snapshotHostname + '-' + snapshotInterfaceName;
  }

  @VisibleForTesting
  static String ispPeeringInterfaceName(String otherIsp) {
    return "To-" + otherIsp;
  }

  @VisibleForTesting
  static BgpProcess makeBgpProcess(Ip routerId, Vrf vrf) {
    return BgpProcess.builder()
        .setRouterId(routerId)
        .setVrf(vrf)
        .setEbgpAdminCost(20)
        .setIbgpAdminCost(200)
        .setLocalAdminCost(200)
        .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .build();
  }

  public static class ModeledNodes {

    private final @Nonnull Map<String, Configuration> _configurations;

    private final @Nonnull Set<Layer1Edge> _layer1Edgesdges;

    public ModeledNodes() {
      _configurations = new HashMap<>();
      _layer1Edgesdges = new HashSet<>();
    }

    public void addConfiguration(Configuration configuration) {
      _configurations.put(configuration.getHostname(), configuration);
    }

    public void addLayer1Edge(Layer1Edge edge) {
      _layer1Edgesdges.add(edge);
    }

    public @Nonnull Map<String, Configuration> getConfigurations() {
      return ImmutableMap.copyOf(_configurations);
    }

    public @Nonnull Set<Layer1Edge> getLayer1Edges() {
      return ImmutableSet.copyOf(_layer1Edgesdges);
    }
  }

  private IspModelingUtils() {}

  /**
   * Creates and returns internet and ISP nodes for a {@link Map} of {@link Configuration}s
   *
   * @param configurations {@link Configuration}s for the given network
   * @param ispConfigurations A list of {@link IspConfiguration} objects to initialize the ISPs
   * @param logger {@link BatfishLogger} to log warnings and errors
   * @param warnings {@link Warnings} containing all the warnings logged during the ISP modeling
   * @return {@link Map} of {@link Configuration}s for the ISPs and Internet
   */
  public static ModeledNodes getInternetAndIspNodes(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull List<IspConfiguration> ispConfigurations,
      @Nonnull BatfishLogger logger,
      @Nonnull Warnings warnings) {

    Map<Long, IspModel> ispModels =
        combineIspConfigurations(configurations, ispConfigurations, warnings);

    List<String> conflicts = ispNameConflicts(configurations, ispModels);

    if (!conflicts.isEmpty()) {
      conflicts.forEach(warnings::redFlag);
      return new ModeledNodes();
    }

    Set<IspPeering> ispPeerings = combineIspPeerings(ispConfigurations, ispModels, warnings);

    return createInternetAndIspNodes(ispModels, ispPeerings, logger);
  }

  /**
   * Checks if the ISP names conflicts with a node name in the configurations or with another ISP
   * name. Returns messages that explain the conflicts.
   */
  @VisibleForTesting
  static List<String> ispNameConflicts(
      Map<String, Configuration> configurations, Map<Long, IspModel> asnToIspInfo) {
    ImmutableList.Builder<String> conflicts = ImmutableList.builder();

    asnToIspInfo.forEach(
        (asn, ispInfo) -> {
          String hostname = ispInfo.getHostname();
          if (configurations.containsKey(hostname)) {
            conflicts.add(
                String.format(
                    "ISP hostname %s for ASN %d conflicts with a node name in the snapshot",
                    hostname, asn));
          }
        });
    return conflicts.build();
  }

  @VisibleForTesting
  static Map<Long, IspModel> combineIspConfigurations(
      Map<String, Configuration> configurations,
      List<IspConfiguration> ispConfigurations,
      Warnings warnings) {
    Map<Long, List<SnapshotConnection>> remotes =
        combineBorderInterfaces(configurations, ispConfigurations, warnings);

    ImmutableMap.Builder<Long, IspModel> ispModelsBuilder = ImmutableMap.builder();
    for (long asn : remotes.keySet()) {
      List<IspNodeInfo> ispNodeInfos =
          ispConfigurations.stream()
              .flatMap(ispConfig -> ispConfig.getIspNodeInfos().stream())
              .filter(ispNodeInfo -> ispNodeInfo.getAsn() == asn)
              .collect(Collectors.toList());
      ispModelsBuilder.put(asn, toIspModel(asn, remotes.get(asn), ispNodeInfos));
    }
    // TODO: Warn about ISP ASNs that appear in IspNodeInfo but are pulled from configs

    return ispModelsBuilder.build();
  }

  /**
   * Returns {@link IspPeering} objects across all ISP configuration objects, after de-duplication
   * and removing invalid ASNs.
   */
  @VisibleForTesting
  static Set<IspPeering> combineIspPeerings(
      List<IspConfiguration> ispConfigurations, Map<Long, IspModel> ispModels, Warnings warnings) {
    ImmutableSet.Builder<IspPeering> ispPeerings = ImmutableSet.builder();
    ispConfigurations.stream()
        .flatMap(ispConfig -> ispConfig.getIspPeeringInfos().stream())
        .forEach(
            peeringInfo -> {
              if (!ispModels.containsKey(peeringInfo.getPeer1().getAsn())) {
                warnings.redFlagf(
                    "ISP Modeling: Could not find ISP with ASN %s, specified for ISP peering",
                    peeringInfo.getPeer1().getAsn());
                return;
              }
              if (!ispModels.containsKey(peeringInfo.getPeer2().getAsn())) {
                warnings.redFlagf(
                    "ISP Modeling: Could not find ISP with ASN %s, specified for ISP peering",
                    peeringInfo.getPeer2().getAsn());
                return;
              }
              ispPeerings.add(IspPeering.create(peeringInfo));
            });

    return ispPeerings.build();
  }

  /**
   * Combines the {@link BorderInterfaceInfo} and {@link BgpPeerInfo} objects across all {@code
   * ispConfigurations}, returning a map from ASN to the list of {@link SnapshotConnection}
   * connections that ASN should have.
   */
  @VisibleForTesting
  static Map<Long, List<SnapshotConnection>> combineBorderInterfaces(
      Map<String, Configuration> configurations,
      List<IspConfiguration> ispConfigurations,
      Warnings warnings) {
    Map<Long, ImmutableList.Builder<SnapshotConnection>> asnToRemotes = new HashMap<>();

    for (IspConfiguration ispConfiguration : ispConfigurations) {
      Set<Ip> allowedIspIps = ImmutableSet.copyOf(ispConfiguration.getFilter().getOnlyRemoteIps());
      List<Long> remoteAsnsList = ispConfiguration.getFilter().getOnlyRemoteAsns();
      LongSpace allowedIspAsns =
          remoteAsnsList.isEmpty()
              ? ALL_AS_NUMBERS
              : LongSpace.builder().includingAll(remoteAsnsList).build();

      for (BorderInterfaceInfo borderInterfaceInfo : ispConfiguration.getBorderInterfaces()) {
        List<SnapshotConnection> snapshotConnections =
            getSnapshotConnectionsForBorderInterface(
                borderInterfaceInfo, allowedIspIps, allowedIspAsns, configurations, warnings);

        if (snapshotConnections.isEmpty()) {
          continue;
        }

        // get the ISP ASN from the first snapshot connection -- they should all be the same
        long asn = snapshotConnections.get(0).getBgpPeer().getLocalAsn();
        asnToRemotes.computeIfAbsent(asn, k -> ImmutableList.builder()).addAll(snapshotConnections);
      }

      for (BgpPeerInfo bgpPeerInfo : ispConfiguration.getBgpPeerInfos()) {
        getSnapshotConnectionForBgpPeerInfo(
                bgpPeerInfo, allowedIspIps, allowedIspAsns, configurations, warnings)
            .ifPresent(
                sc ->
                    asnToRemotes
                        .computeIfAbsent(
                            sc.getBgpPeer().getLocalAsn(), k -> ImmutableList.builder())
                        .add(sc));
      }
    }

    return ImmutableMap.copyOf(
        asnToRemotes.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().build())));
  }

  /**
   * Puts together the model of ISP based on the list of {@link SnapshotConnection}s and {@link
   * IspNodeInfo} objects across all {@link IspConfiguration} objects.
   */
  @VisibleForTesting
  static IspModel toIspModel(
      long asn, List<SnapshotConnection> snapshotConnections, List<IspNodeInfo> ispNodeInfos) {

    // For properties that can't be merged, pick the first one.
    String ispName = ispNodeInfos.stream().map(IspNodeInfo::getName).findFirst().orElse(null);
    Role ispRole = ispNodeInfos.stream().map(IspNodeInfo::getRole).findFirst().orElse(Role.TRANSIT);
    IspTrafficFiltering filtering =
        ispNodeInfos.stream()
            .map(IspNodeInfo::getIspTrafficFiltering)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(
                ispRole == Role.TRANSIT
                    ? IspTrafficFiltering.blockReservedAddressesAtInternet()
                    : IspTrafficFiltering.none());

    // Merge the sets of additional announcements to internet is merging their prefixes
    Set<Prefix> additionalPrefixes =
        ispNodeInfos.stream()
            .flatMap(i -> i.getAdditionalAnnouncements().stream().map(IspAnnouncement::getPrefix))
            .collect(ImmutableSet.toImmutableSet());

    return IspModel.builder()
        .setAsn(asn)
        .setName(ispName)
        .setRole(ispRole)
        .setAdditionalPrefixesToInternet(additionalPrefixes)
        .setSnapshotConnections(snapshotConnections)
        .setTrafficFiltering(filtering)
        .build();
  }

  /**
   * Given a {@link BorderInterfaceInfo} object, return the {@link SnapshotConnection}s
   * corresponding to it. Nothing is returned if the interface established BGP sessions with
   * multiple ISPs.
   *
   * <p>The method will typically return one {@link SnapshotConnection} but can return multiple ones
   * when the same interface in the snapshot peers with the same ISP multiple times.
   */
  @VisibleForTesting
  static List<SnapshotConnection> getSnapshotConnectionsForBorderInterface(
      BorderInterfaceInfo borderInterface,
      Set<Ip> remoteIps,
      LongSpace remoteAsns,
      Map<String, Configuration> configurations,
      Warnings warnings) {
    NodeInterfacePair nodeInterfacePair = borderInterface.getBorderInterface();
    Configuration snapshotHost = configurations.get(nodeInterfacePair.getHostname());
    if (snapshotHost == null) {
      warnings.redFlagf(
          "ISP Modeling: Non-existent border node %s", nodeInterfacePair.getHostname());
      return ImmutableList.of();
    }
    Interface snapshotIface =
        snapshotHost.getAllInterfaces().values().stream()
            .filter(i -> i.getName().equalsIgnoreCase(nodeInterfacePair.getInterface()))
            .findFirst()
            .orElse(null);
    if (snapshotIface == null) {
      warnings.redFlagf("ISP Modeling: Non-existent border interface %s", nodeInterfacePair);
      return ImmutableList.of();
    }

    // TODO: Enforce interface type constraint here

    List<BgpPeerConfig> validBgpPeers =
        snapshotHost.getVrfs().values().stream()
            .map(Vrf::getBgpProcess)
            .filter(Objects::nonNull)
            .flatMap(
                bgpProcess ->
                    StreamSupport.stream(bgpProcess.getAllPeerConfigs().spliterator(), false))
            .filter(
                bgpPeerConfig ->
                    isValidBgpPeerForBorderInterfaceInfo(
                        bgpPeerConfig,
                        snapshotIface.getAllConcreteAddresses(),
                        remoteIps,
                        remoteAsns))
            .collect(Collectors.toList());

    if (validBgpPeers.isEmpty()) {
      warnings.redFlagf(
          "ISP Modeling: No valid eBGP configurations for border interface %s", nodeInterfacePair);
      return ImmutableList.of();
    }

    Set<Long> asns =
        validBgpPeers.stream()
            .map(peer -> peer.getRemoteAsns().least())
            .collect(ImmutableSet.toImmutableSet());

    if (asns.size() > 1) {
      warnings.redFlagf(
          "ISP Modeling: Skipping border interface %s because it connects to multiple ASNs",
          nodeInterfacePair);
      return ImmutableList.of();
    }

    return validBgpPeers.stream()
        .map(peer -> makeSnapshotConnectionForBorderInterface(peer, snapshotIface))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Converts the {@code bgpPeerConfig} and snapshot iface information into a {@link
   * SnapshotConnection} object.
   */
  private static SnapshotConnection makeSnapshotConnectionForBorderInterface(
      BgpPeerConfig bgpPeerConfig, Interface snapshotIface) {
    String snapshotHostname = snapshotIface.getOwner().getHostname();
    String ispIfaceName = ispToSnapshotInterfaceName(snapshotHostname, snapshotIface.getName());
    Layer1Node snapshotL1node = new Layer1Node(snapshotHostname, snapshotIface.getName());

    if (bgpPeerConfig instanceof BgpUnnumberedPeerConfig) {
      IspInterface ispInterface =
          new IspInterface(ispIfaceName, LINK_LOCAL_ADDRESS, snapshotL1node, null);
      return new SnapshotConnection(
          ImmutableList.of(ispInterface),
          IspBgpUnnumberedPeer.create((BgpUnnumberedPeerConfig) bgpPeerConfig, ispIfaceName));
    }

    if (bgpPeerConfig instanceof BgpActivePeerConfig) {
      Ip peerAddress = ((BgpActivePeerConfig) bgpPeerConfig).getPeerAddress();
      checkArgument(peerAddress != null, "Peer address should not be null");
      ConcreteInterfaceAddress ifaceAddress =
          getSnapshotIfaceAddressForBgpPeer(
                  (BgpActivePeerConfig) bgpPeerConfig,
                  null,
                  snapshotIface.getAllConcreteAddresses())
              .get();
      InterfaceAddress ispInterfaceAddress =
          ConcreteInterfaceAddress.create(peerAddress, ifaceAddress.getNetworkBits());
      IspInterface ispInterface =
          new IspInterface(ispIfaceName, ispInterfaceAddress, snapshotL1node, null);
      return new SnapshotConnection(
          ImmutableList.of(ispInterface),
          IspBgpActivePeer.create((BgpActivePeerConfig) bgpPeerConfig, ifaceAddress.getIp()));
    }
    throw new IllegalArgumentException("makeRemote called with illegal BgpPeerConfig type");
  }

  /**
   * Given a {@link BgpPeerInfo} object, return the corresponding {@link SnapshotConnection} object.
   *
   * <p>{@link Optional#empty()} is returned if the interface established BGP sessions with multiple
   * ISPs, if the peer has incomplete configuration, if the peer's configuration is not allowed by
   * the ISP Configuration, and other cases.
   */
  @VisibleForTesting
  static Optional<SnapshotConnection> getSnapshotConnectionForBgpPeerInfo(
      BgpPeerInfo bgpPeerInfo,
      Set<Ip> remoteIps,
      LongSpace remoteAsns,
      Map<String, Configuration> configurations,
      Warnings warnings) {
    // 0a. Resolve the device indicated by the BgpPeerInfo.
    Configuration snapshotBgpHost = configurations.get(bgpPeerInfo.getHostname());
    if (snapshotBgpHost == null) {
      warnings.redFlagf("ISP Modeling: Non-existent border node %s", bgpPeerInfo.getHostname());
      return Optional.empty();
    }

    // 0b. Resolve the user-provided (or default) VRF in which to find the peering.
    String bgpPeerVrf;
    if (bgpPeerInfo.getVrf() != null) {
      Optional<String> matchingVrf =
          snapshotBgpHost.getVrfs().values().stream()
              .map(Vrf::getName)
              .filter(name -> name.equalsIgnoreCase(bgpPeerInfo.getVrf()))
              .sorted()
              .findFirst();
      if (!matchingVrf.isPresent()) {
        warnings.redFlagf(
            "ISP Modeling: No VRF %s found on node %s",
            bgpPeerInfo.getVrf(), snapshotBgpHost.getHostname());
        return Optional.empty();
      }
      bgpPeerVrf = matchingVrf.get();
    } else {
      bgpPeerVrf = snapshotBgpHost.getDefaultVrf().getName();
    }

    // 1. Find the BgpActivePeerConfig for which we are creating an ISP neighbor.
    Optional<BgpActivePeerConfig> snapshotBgpPeerOpt =
        snapshotBgpHost
            .getVrfs()
            .get(bgpPeerVrf)
            .getBgpProcess()
            .getActiveNeighbors()
            .values()
            .stream()
            .filter(peer -> bgpPeerInfo.getPeerAddress().equals(peer.getPeerAddress()))
            .findFirst();
    if (!snapshotBgpPeerOpt.isPresent()) {
      warnings.redFlagf(
          "ISP Modeling: No BGP neighbor %s found on node %s in %s vrf",
          bgpPeerInfo.getPeerAddress(), bgpPeerInfo.getHostname(), bgpPeerVrf);
      return Optional.empty();
    }
    BgpActivePeerConfig snapshotBgpPeer = snapshotBgpPeerOpt.get();

    // 1a. And validate that peer is not malformed or incompletely configured.
    Optional<String> invalidReason =
        validateOrExplainProblemCreatingIspConfig(snapshotBgpPeer, remoteIps, remoteAsns);
    if (invalidReason.isPresent()) {
      warnings.redFlagf(
          "ISP Modeling: BGP neighbor %s on node %s is invalid: %s.",
          bgpPeerInfo.getPeerAddress(), bgpPeerInfo.getHostname(), invalidReason.get());
      return Optional.empty();
    }

    // 1c. Determine the local IP for the BGP peering, if any.
    Optional<ConcreteInterfaceAddress> localInterfaceAddressForBgpPeeringOpt =
        getSnapshotIfaceAddressForBgpPeer(
            snapshotBgpPeer,
            bgpPeerInfo.getOverrideLocalAddress(),
            snapshotBgpHost.getAllInterfaces().values().stream()
                .filter(iface -> iface.getVrfName().equalsIgnoreCase(bgpPeerVrf))
                .flatMap(iface -> iface.getAllConcreteAddresses().stream())
                .collect(ImmutableSet.toImmutableSet()));
    if (!localInterfaceAddressForBgpPeeringOpt.isPresent()) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: No compatible source interface found for neighbor %s on node %s vrf %s"
                  + " (configured local address %s, ISP overridden local address %s)",
              bgpPeerInfo.getPeerAddress(),
              snapshotBgpHost.getHostname(),
              bgpPeerVrf,
              firstNonNull(snapshotBgpPeer.getLocalIp(), "none"),
              firstNonNull(bgpPeerInfo.getOverrideLocalAddress(), "none")));
      return Optional.empty();
    }
    ConcreteInterfaceAddress localInterfaceAddressForBgpPeering =
        localInterfaceAddressForBgpPeeringOpt.get();

    // 2. Find where the ISP should attach into the network. Validate provided host and interface
    // names.
    if (bgpPeerInfo.getIspAttachment() == null) {
      return Optional.of(
          new SnapshotConnection(
              ImmutableList.of(),
              IspBgpActivePeer.create(
                  snapshotBgpPeer, localInterfaceAddressForBgpPeering.getIp())));
    }

    IspAttachment ispAttachment = bgpPeerInfo.getIspAttachment();
    String attachmentHostname =
        ispAttachment.getHostname() == null
            ? snapshotBgpHost.getHostname()
            : ispAttachment.getHostname();
    Configuration attachmentHost = configurations.get(attachmentHostname);
    if (attachmentHost == null) {
      warnings.redFlagf(
          "ISP Modeling: Non-existent ISP attachment node %s", ispAttachment.getHostname());
      return Optional.empty();
    }

    Interface attachmentIface =
        attachmentHost.getAllInterfaces().values().stream()
            .filter(i -> i.getName().equalsIgnoreCase(ispAttachment.getInterface()))
            .findFirst()
            .orElse(null);
    if (attachmentIface == null) {
      warnings.redFlagf(
          "ISP Modeling: Non-existent attachment interface %s on node %s",
          ispAttachment.getInterface(), attachmentHostname);
      return Optional.empty();
    }
    // TODO: Enforce attachmentIface interface type constraint here.
    // TODO: support aggregate/redundant interfaces.

    // One last thing that we need to figure out is the prefix length to use for the address on
    // the ISP's BGP interface (the address will snapshotBgpPeer.getPeerAddress()). Cases to
    // consider for how the attachment interface is configured:
    // 1. Is not Layer3 ==> pick length based on the interface that has the BGP peer's local IP or
    // single-hop compatible IP. Assume that the interface provides L2 connectivity to the
    // snapshot BGP interface.
    // 2. Is Layer3 and owns the local IP of the peering ==> use this address's prefix length.
    // 3. Is Layer3 but does NOT own the local IP of the peering ==> do not make the connection at
    // all for now. Additional configuration is needed to enable connectivity from the ISP to the
    // snapshot's BGP interface.

    int ispPrefixLength = localInterfaceAddressForBgpPeering.getNetworkBits();
    if (!attachmentIface.getAllAddresses().isEmpty()) {
      // case 3
      Optional<ConcreteInterfaceAddress> attachmentInterfaceAddressThatAlsoHasLocalIp =
          attachmentIface.getAllConcreteAddresses().stream()
              .filter(
                  addr -> Objects.equals(addr.getIp(), localInterfaceAddressForBgpPeering.getIp()))
              .sorted()
              .findFirst();
      if (!attachmentInterfaceAddressThatAlsoHasLocalIp.isPresent()) {
        warnings.redFlagf(
            "ISP Modeling: The attachment interface %s[%s] cannot enable the BGP peering to"
                + " neighbor %s because it is Layer3 but does not own the BGP peer's local IP"
                + " %s",
            attachmentHost,
            attachmentIface.getName(),
            snapshotBgpPeer.getPeerAddress(),
            localInterfaceAddressForBgpPeering.getIp());
        return Optional.empty();
      }
      ispPrefixLength = attachmentInterfaceAddressThatAlsoHasLocalIp.get().getNetworkBits();
    }

    return Optional.of(
        makeSnapshotConnectionForBgpPeerInfo(
            snapshotBgpPeer,
            ConcreteInterfaceAddress.create(
                localInterfaceAddressForBgpPeering.getIp(), ispPrefixLength),
            attachmentIface,
            ispAttachment.getVlanTag()));
  }

  /**
   * Infers the parameters of {@link SnapshotConnection} object needed to connect the ISP to the
   * snapshot (what interface to create, which IP address to assign, ...) from the resolve BGP peer
   * and attachment interface.
   *
   * <p>Currently the logic assumes that the physical connection will either be at the snapshot
   * interface that terminates the BGP session or the attachment interface provides L2 connectivity
   * to the BGP interface.
   */
  private static SnapshotConnection makeSnapshotConnectionForBgpPeerInfo(
      BgpActivePeerConfig snapshotBgpPeer,
      ConcreteInterfaceAddress snapshotBgpPeerInterfaceAddress,
      Interface snapshotAttachmentIface,
      @Nullable Integer vlanTag) {
    String snapshotAttachmentHostname = snapshotAttachmentIface.getOwner().getHostname();
    String ispIfaceName =
        ispToSnapshotInterfaceName(snapshotAttachmentHostname, snapshotAttachmentIface.getName());
    Layer1Node snapshotL1node =
        new Layer1Node(snapshotAttachmentHostname, snapshotAttachmentIface.getName());

    Ip peerAddress = snapshotBgpPeer.getPeerAddress();
    checkArgument(peerAddress != null, "Peer address should not be null");
    InterfaceAddress ispInterfaceAddress =
        ConcreteInterfaceAddress.create(
            peerAddress, snapshotBgpPeerInterfaceAddress.getNetworkBits());
    IspInterface ispInterface =
        new IspInterface(ispIfaceName, ispInterfaceAddress, snapshotL1node, vlanTag);
    return new SnapshotConnection(
        ImmutableList.of(ispInterface),
        IspBgpActivePeer.create(snapshotBgpPeer, snapshotBgpPeerInterfaceAddress.getIp()));
  }

  /**
   * Returns the {@link ConcreteInterfaceAddress} with matching IP among the set of interfaces
   * provided.
   *
   * <p>The implementation prefers active interfaces, followed by lower addresses.
   */
  @VisibleForTesting
  static Optional<ConcreteInterfaceAddress> inferSnapshotBgpIfaceAddress(
      Collection<Interface> interfaces, Ip interfaceIp) {
    Optional<ConcreteInterfaceAddress> lowestActiveAddress =
        interfaces.stream()
            .filter(Interface::getActive)
            .flatMap(iface -> iface.getAllConcreteAddresses().stream())
            .filter(addr -> Objects.equals(addr.getIp(), interfaceIp))
            .sorted()
            .findFirst();
    if (lowestActiveAddress.isPresent()) {
      return lowestActiveAddress;
    }
    return interfaces.stream()
        .flatMap(iface -> iface.getAllConcreteAddresses().stream())
        .filter(addr -> Objects.equals(addr.getIp(), interfaceIp))
        .sorted()
        .findFirst();
  }

  private static ModeledNodes createInternetAndIspNodes(
      Map<Long, IspModel> asnToIspModel, Set<IspPeering> ispPeerings, BatfishLogger logger) {
    ModeledNodes modeledNodes = new ModeledNodes();

    asnToIspModel
        .values()
        .forEach(
            ispModel ->
                createIspNode(ispModel, logger)
                    .ifPresent(
                        ispConfiguration -> {
                          Set<Layer1Edge> layer1Edges =
                              connectIspToSnapshot(ispModel, ispConfiguration, logger);

                          modeledNodes.addConfiguration(ispConfiguration);
                          layer1Edges.forEach(modeledNodes::addLayer1Edge);
                        }));

    // not proceeding if no ISPs were created
    if (modeledNodes.getConfigurations().isEmpty()) {
      return modeledNodes;
    }

    boolean needInternet =
        asnToIspModel.values().stream().anyMatch(model -> model.getRole() == Role.TRANSIT);

    if (needInternet) {
      Configuration internet = createInternetNode();
      modeledNodes.addConfiguration(internet);

      modeledNodes.getConfigurations().values().stream()
          .filter(c -> c != internet)
          .forEach(
              c -> {
                long ispAsn = getAsnOfIspNode(c);
                if (asnToIspModel.get(ispAsn).getRole() == Role.TRANSIT) {
                  Set<Layer1Edge> layer1Edges =
                      connectIspToInternet(ispAsn, asnToIspModel.get(ispAsn), c, internet);
                  layer1Edges.forEach(modeledNodes::addLayer1Edge);
                }
              });
    }

    ispPeerings.forEach(
        ispPeering -> {
          IspModel ispModel1 = asnToIspModel.get(ispPeering.getAsn1());
          IspModel ispModel2 = asnToIspModel.get(ispPeering.getAsn2());
          Configuration ispConfiguration1 =
              modeledNodes.getConfigurations().get(ispModel1.getHostname());
          Configuration ispConfiguration2 =
              modeledNodes.getConfigurations().get(ispModel2.getHostname());
          if (ispConfiguration1 == null || ispConfiguration2 == null) {
            return;
          }
          Set<Layer1Edge> layer1Edges =
              connectPeerIsps(
                  ispPeering, ispModel1, ispModel2, ispConfiguration1, ispConfiguration2);
          layer1Edges.forEach(modeledNodes::addLayer1Edge);
        });

    return modeledNodes;
  }

  @VisibleForTesting
  static Set<Layer1Edge> connectPeerIsps(
      IspPeering ispPeering,
      IspModel ispModel1,
      IspModel ispModel2,
      Configuration ispConfiguration1,
      Configuration ispConfiguration2) {

    Interface iface1 =
        createIspPeeringInterface(ispConfiguration1, ispConfiguration2.getHostname());
    Interface iface2 =
        createIspPeeringInterface(ispConfiguration2, ispConfiguration1.getHostname());

    createIspPeeringBgpPeer(
        ispConfiguration1, ispModel1, iface1.getName(), ispPeering.getAsn1(), ispPeering.getAsn2());
    createIspPeeringBgpPeer(
        ispConfiguration2, ispModel2, iface2.getName(), ispPeering.getAsn2(), ispPeering.getAsn1());

    Layer1Node ispL1 = new Layer1Node(ispConfiguration1.getHostname(), iface1.getName());
    Layer1Node ispL2 = new Layer1Node(ispConfiguration2.getHostname(), iface2.getName());
    return ImmutableSet.of(new Layer1Edge(ispL1, ispL2), new Layer1Edge(ispL2, ispL1));
  }

  private static void createIspPeeringBgpPeer(
      Configuration ispConfiguration,
      IspModel ispModel,
      String ifaceName,
      long localAs,
      long remoteAs) {

    if (!ispConfiguration.getRoutingPolicies().containsKey(EXPORT_POLICY_FOR_ISP_PEERING)) {
      ispConfiguration
          .getRoutingPolicies()
          .put(EXPORT_POLICY_FOR_ISP_PEERING, installRoutingPolicyForIspPeering(ispConfiguration));
    }

    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(ifaceName)
        .setLocalAs(localAs)
        .setRemoteAs(remoteAs)
        .setLocalIp(LINK_LOCAL_IP)
        .setBgpProcess(ispConfiguration.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(EXPORT_POLICY_FOR_ISP_PEERING)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(ispModel.getRole() == Role.PRIVATE_BACKBONE)
                        .setSendExtendedCommunity(ispModel.getRole() == Role.PRIVATE_BACKBONE)
                        .build())
                .build())
        .build();
  }

  private static Interface createIspPeeringInterface(
      Configuration ispConfiguration, String otherHostname) {
    return Interface.builder()
        .setOwner(ispConfiguration)
        .setName(ispPeeringInterfaceName(otherHostname))
        .setVrf(ispConfiguration.getDefaultVrf())
        .setAddress(LINK_LOCAL_ADDRESS)
        .setType(InterfaceType.PHYSICAL)
        .build();
  }

  /** Creates the modeled Internet node and inserts it into {@code modeledNodes} */
  @VisibleForTesting
  static Configuration createInternetNode() {
    Configuration internetConfiguration =
        Configuration.builder()
            .setHostname(INTERNET_HOST_NAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDeviceModel(DeviceModel.BATFISH_INTERNET)
            .build();
    internetConfiguration.setDeviceType(DeviceType.INTERNET);
    Vrf defaultVrf =
        Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(internetConfiguration).build();
    Interface internetOutInterface =
        Interface.builder()
            .setName(INTERNET_OUT_INTERFACE)
            .setOwner(internetConfiguration)
            .setVrf(defaultVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    INTERNET_OUT_ADDRESS, INTERNET_OUT_SUBNET.getPrefixLength()))
            .build();

    internetConfiguration
        .getDefaultVrf()
        .setStaticRoutes(
            new ImmutableSortedSet.Builder<StaticRoute>(naturalOrder())
                .add(
                    StaticRoute.builder()
                        .setNetwork(Prefix.ZERO)
                        .setNextHopInterface(INTERNET_OUT_INTERFACE)
                        .setAdministrativeCost(1)
                        .build())
                .addAll(
                    INTERNET_NULL_ROUTED_PREFIXES.stream()
                        .map(
                            prefix ->
                                StaticRoute.builder()
                                    .setNetwork(prefix)
                                    .setNextHopInterface(NULL_INTERFACE_NAME)
                                    .setAdministrativeCost(1)
                                    .build())
                        .collect(ImmutableSet.toImmutableSet()))
                .build());

    BgpProcess bgpProcess = makeBgpProcess(INTERNET_OUT_ADDRESS, defaultVrf);
    bgpProcess.setMultipathEbgp(true);
    bgpProcess.setMultipathEquivalentAsPathMatchMode(
        MultipathEquivalentAsPathMatchMode.PATH_LENGTH);

    internetConfiguration.setRoutingPolicies(
        ImmutableSortedMap.of(
            EXPORT_POLICY_ON_INTERNET,
            installRoutingPolicyAdvertiseStatic(
                EXPORT_POLICY_ON_INTERNET,
                internetConfiguration,
                new PrefixSpace(PrefixRange.fromPrefix(Prefix.ZERO)))));

    internetConfiguration.setLocationInfo(
        ImmutableMap.of(
            interfaceLinkLocation(internetOutInterface),
            INTERNET_OUT_INTERFACE_LINK_LOCATION_INFO));

    return internetConfiguration;
  }

  /**
   * Adds infrastructure to {@code ispConfiguration} and {@code internetConfiguration} needed to
   * connect the ISP to the Internet: interfaces, traffic filters, bgp peers. Returns the layer1
   * edges that are needed for physical connectivity.
   */
  @VisibleForTesting
  static Set<Layer1Edge> connectIspToInternet(
      long ispAsn,
      IspModel ispModel,
      Configuration ispConfiguration,
      Configuration internetConfiguration) {

    // add a static route for each additional prefix announced to the internet
    ispConfiguration
        .getDefaultVrf()
        .setStaticRoutes(
            ImmutableSortedSet.copyOf(
                ispModel.getAdditionalPrefixesToInternet().stream()
                    .map(
                        prefix ->
                            StaticRoute.builder()
                                .setNetwork(prefix)
                                .setNextHopInterface(NULL_INTERFACE_NAME)
                                .setAdministrativeCost(HIGH_ADMINISTRATIVE_COST)
                                .build())
                    .collect(ImmutableSet.toImmutableSet())));

    PrefixSpace prefixSpace = new PrefixSpace();
    ispModel.getAdditionalPrefixesToInternet().forEach(prefixSpace::addPrefix);

    ispConfiguration
        .getRoutingPolicies()
        .put(
            EXPORT_POLICY_ON_ISP_TO_INTERNET,
            installRoutingPolicyForIspToInternet(ispConfiguration, prefixSpace));

    IspTrafficFilteringPolicy fp =
        IspTrafficFilteringPolicy.createFor(ispModel.getTrafficFiltering());
    IpAccessList toInternet = fp.filterTrafficToInternet();
    if (toInternet != null) {
      ispConfiguration.getIpAccessLists().put(toInternet.getName(), toInternet);
    }
    IpAccessList fromInternet = fp.filterTrafficFromInternet();
    if (fromInternet != null) {
      ispConfiguration.getIpAccessLists().put(fromInternet.getName(), fromInternet);
    }
    // Create Internet-facing interface and apply filters.
    Interface.builder()
        .setOwner(ispConfiguration)
        .setVrf(ispConfiguration.getDefaultVrf())
        .setName(ISP_TO_INTERNET_INTERFACE_NAME)
        .setAddress(LINK_LOCAL_ADDRESS)
        .setIncomingFilter(fromInternet)
        .setOutgoingFilter(toInternet)
        .setType(InterfaceType.PHYSICAL)
        .build();

    Interface internetIface =
        Interface.builder()
            .setOwner(internetConfiguration)
            .setName(internetToIspInterfaceName(ispConfiguration.getHostname()))
            .setVrf(internetConfiguration.getDefaultVrf())
            .setAddress(LINK_LOCAL_ADDRESS)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Interface ispIface = ispConfiguration.getAllInterfaces().get(ISP_TO_INTERNET_INTERFACE_NAME);

    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(ispIface.getName())
        .setRemoteAs(INTERNET_AS)
        .setLocalAs(ispAsn)
        .setLocalIp(LINK_LOCAL_IP)
        .setBgpProcess(ispConfiguration.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_INTERNET)
                .build())
        .build();

    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(internetIface.getName())
        .setRemoteAs(ispAsn)
        .setLocalAs(INTERNET_AS)
        .setLocalIp(LINK_LOCAL_IP)
        .setBgpProcess(internetConfiguration.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_POLICY_ON_INTERNET).build())
        .build();

    Layer1Node internetL1 =
        new Layer1Node(internetConfiguration.getHostname(), internetIface.getName());
    Layer1Node ispL1 = new Layer1Node(ispConfiguration.getHostname(), ispIface.getName());
    return ImmutableSet.of(new Layer1Edge(internetL1, ispL1), new Layer1Edge(ispL1, internetL1));
  }

  /**
   * Creates the {@link Configuration} for the ISP node given an ASN and {@link IspModel} and
   * inserts the node into {@code modeledNodes}. Returns empty if no node is created.
   */
  @VisibleForTesting
  static Optional<Configuration> createIspNode(IspModel ispModel, BatfishLogger logger) {
    if (ispModel.getSnapshotConnections().isEmpty()) {
      logger.warnf("ISP information for ASN '%s' is not correct", ispModel.getAsn());
      return Optional.empty();
    }

    Configuration ispConfiguration =
        Configuration.builder()
            .setHostname(ispModel.getHostname())
            .setHumanName(ispModel.getName())
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDeviceModel(DeviceModel.BATFISH_ISP)
            .build();
    ispConfiguration.setDeviceType(DeviceType.ISP);
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(ispConfiguration).build();

    ispConfiguration
        .getRoutingPolicies()
        .put(
            EXPORT_POLICY_ON_ISP_TO_CUSTOMERS,
            installRoutingPolicyForIspToCustomers(ispConfiguration));

    // using the lowest IP among the InterfaceAddresses as the router ID, or Ip.Zero
    BgpProcess bgpProcess =
        makeBgpProcess(
            ispModel.getSnapshotConnections().stream()
                .flatMap(sc -> sc.getInterfaces().stream())
                .map(IspInterface::getAddress)
                .map(
                    addr ->
                        addr instanceof ConcreteInterfaceAddress
                            ? ((ConcreteInterfaceAddress) addr).getIp()
                            : LINK_LOCAL_IP)
                .min(Ip::compareTo)
                .orElse(Ip.ZERO),
            ispConfiguration.getDefaultVrf());
    bgpProcess.setMultipathEbgp(true);

    return Optional.of(ispConfiguration);
  }

  /**
   * Adds to {@code ispConfiguration} the infrastructure (interfaces, traffic filters, L1 edges, BGP
   * peer) needed to connect the ISP to the snapshot. Returns the resulting L1 edges.
   */
  @VisibleForTesting
  static Set<Layer1Edge> connectIspToSnapshot(
      IspModel ispModel, Configuration ispConfiguration, BatfishLogger logger) {

    // Get the network-facing traffic filtering policy for this ISP.
    IspTrafficFilteringPolicy fp =
        IspTrafficFilteringPolicy.createFor(ispModel.getTrafficFiltering());
    IpAccessList toNetwork = fp.filterTrafficToNetwork();
    if (toNetwork != null) {
      ispConfiguration.getIpAccessLists().put(toNetwork.getName(), toNetwork);
    }
    IpAccessList fromNetwork = fp.filterTrafficFromNetwork();
    if (fromNetwork != null) {
      ispConfiguration.getIpAccessLists().put(fromNetwork.getName(), fromNetwork);
    }

    ImmutableSet.Builder<Layer1Edge> layer1Edges = ImmutableSet.builder();

    ispModel
        .getSnapshotConnections()
        .forEach(
            snapshotConnection -> {
              snapshotConnection
                  .getInterfaces()
                  .forEach(
                      iface -> {
                        Interface ispInterface =
                            Interface.builder()
                                .setOwner(ispConfiguration)
                                .setName(iface.getName())
                                .setVrf(ispConfiguration.getDefaultVrf())
                                .setAddress(iface.getAddress())
                                .setIncomingFilter(fromNetwork)
                                .setOutgoingFilter(toNetwork)
                                .setType(InterfaceType.PHYSICAL)
                                .setEncapsulationVlan(iface.getVlanTag())
                                .setSwitchport(false)
                                .setSwitchportMode(SwitchportMode.NONE)
                                .build();
                        Layer1Node ispL1 =
                            new Layer1Node(ispConfiguration.getHostname(), ispInterface.getName());
                        layer1Edges.add(new Layer1Edge(ispL1, iface.getLayer1Remote()));
                        layer1Edges.add(new Layer1Edge(iface.getLayer1Remote(), ispL1));
                      });
              addBgpPeerToIsp(
                  snapshotConnection.getBgpPeer(),
                  ispConfiguration.getDefaultVrf().getBgpProcess(),
                  ispModel.getRole());
            });

    return layer1Edges.build();
  }

  /**
   * Gets the local AS of a given ISP node {@link Configuration}. Since Local AS of all eBGP peers
   * on this node will be same, returning the Local AS of the any eBGP peer will suffice.
   */
  @VisibleForTesting
  static @Nonnull Long getAsnOfIspNode(Configuration ispConfiguration) {
    checkState(
        Objects.nonNull(ispConfiguration.getDefaultVrf()), "default VRF should be present in ISP");
    checkState(
        Objects.nonNull(ispConfiguration.getDefaultVrf().getBgpProcess()),
        "default VRF should have a BGP process");
    BgpProcess bgpProcess = ispConfiguration.getDefaultVrf().getBgpProcess();
    checkState(
        !(bgpProcess.getActiveNeighbors().isEmpty()
            && bgpProcess.getInterfaceNeighbors().isEmpty()),
        "ISP should have greater than 0 BGP peers");
    Long localAs =
        ispConfiguration
            .getDefaultVrf()
            .getBgpProcess()
            .getAllPeerConfigs()
            .iterator()
            .next()
            .getLocalAs();
    checkState(Objects.nonNull(localAs), "Local AS of all eBGP peers should be set on ISP");
    return localAs;
  }

  /**
   * Installs a routing policy named {@code policyName} on {@code node} that advertises all static
   * routes to {@code prefixSpace}. Returns the created policy.
   */
  public static RoutingPolicy installRoutingPolicyAdvertiseStatic(
      String policyName, Configuration node, PrefixSpace prefixSpace) {
    return RoutingPolicy.builder()
        .setName(policyName)
        .setOwner(node)
        .setStatements(Collections.singletonList(getAdvertiseStaticStatement(prefixSpace)))
        .build();
  }

  /** Returns a routing policy statement that advertises static routes to {@code prefixSpace} */
  public static Statement getAdvertiseStaticStatement(PrefixSpace prefixSpace) {
    return new If(
        new Conjunction(
            ImmutableList.of(
                new MatchProtocol(RoutingProtocol.STATIC),
                new MatchPrefixSet(
                    DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace)))),
        ImmutableList.of(
            new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)),
            Statements.ExitAccept.toStaticStatement()));
  }

  /** Returns a routing policy statement that advertises all BGP routes */
  public static Statement getAdvertiseBgpStatement() {
    return new If(
        new MatchProtocol(RoutingProtocol.BGP),
        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()));
  }

  /** Creates a routing policy to export all BGP routes */
  @VisibleForTesting
  static RoutingPolicy installRoutingPolicyForIspToCustomers(Configuration isp) {
    return RoutingPolicy.builder()
        .setName(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
        .setOwner(isp)
        .setStatements(Collections.singletonList(getAdvertiseBgpStatement()))
        .build();
  }

  /** Creates a routing policy to export all BGP and static routes */
  @VisibleForTesting
  static RoutingPolicy installRoutingPolicyForIspToInternet(
      Configuration isp, PrefixSpace prefixSpace) {
    return RoutingPolicy.builder()
        .setName(EXPORT_POLICY_ON_ISP_TO_INTERNET)
        .setOwner(isp)
        .setStatements(
            ImmutableList.of(getAdvertiseBgpStatement(), getAdvertiseStaticStatement(prefixSpace)))
        .build();
  }

  /** Creates a routing policy to export all BGP routes */
  @VisibleForTesting
  static RoutingPolicy installRoutingPolicyForIspPeering(Configuration isp) {
    return RoutingPolicy.builder()
        .setName(EXPORT_POLICY_FOR_ISP_PEERING)
        .setOwner(isp)
        .setStatements(ImmutableList.of(getAdvertiseBgpStatement()))
        .build();
  }

  @VisibleForTesting
  static boolean isValidBgpPeerForBorderInterfaceInfo(
      BgpPeerConfig bgpPeerConfig,
      Set<ConcreteInterfaceAddress> interfaceAddresses,
      Set<Ip> allowedRemoteIps,
      LongSpace allowedRemoteAsns) {
    boolean commonCriteria =
        Objects.nonNull(bgpPeerConfig.getLocalAs()) // local AS is defined
            && !bgpPeerConfig
                .getRemoteAsns()
                .equals(LongSpace.of(bgpPeerConfig.getLocalAs())) // not iBGP
            && !allowedRemoteAsns
                .intersection(bgpPeerConfig.getRemoteAsns())
                .isEmpty(); // remote AS is defined and allowed
    if (!commonCriteria) {
      return false;
    }
    if (bgpPeerConfig instanceof BgpActivePeerConfig) {
      BgpActivePeerConfig activePeerConfig = (BgpActivePeerConfig) bgpPeerConfig;

      return Objects.nonNull(activePeerConfig.getPeerAddress()) // peer address is defined
          && getSnapshotIfaceAddressForBgpPeer(activePeerConfig, null, interfaceAddresses)
              .isPresent()
          && (allowedRemoteIps.isEmpty()
              || allowedRemoteIps.contains(
                  activePeerConfig.getPeerAddress())); // peer address is allowed
    }
    if (bgpPeerConfig instanceof BgpUnnumberedPeerConfig) {
      // peer interface is always non-null, so need to check
      return true;
    }
    // passive peers are not valid for ISP modeling
    return false;
  }

  /**
   * Returns the first (lowest in sorted order) of the given interface addresses that matches the
   * given filter, or {@link Optional#empty()} if none matches.
   */
  private static Optional<ConcreteInterfaceAddress> firstMatching(
      Set<ConcreteInterfaceAddress> addresses, Predicate<ConcreteInterfaceAddress> filter) {
    return addresses.stream().filter(filter).sorted().findFirst();
  }

  /**
   * Returns the interface address, among {@code interfaceAddresses}, that is valid for to use as
   * local IP for {@code bgpPeerConfig}. The Optional is empty when there is no such address.
   *
   * <p>The priority of the search is:
   *
   * <ol>
   *   <li>If {@code overrideLocalIp} is present, a {@link ConcreteInterfaceAddress} with address
   *       equal to that IP.
   *   <li>If {@code bgpPeerConfig} has a localIp, a {@link ConcreteInterfaceAddress} with address
   *       equal to that IP.
   *   <li>If {@code bgpPeerConfig} is a single-hop eBGP session, a {@link ConcreteInterfaceAddress}
   *       with a subnet containing its peer address.
   * </ol>
   */
  private static Optional<ConcreteInterfaceAddress> getSnapshotIfaceAddressForBgpPeer(
      BgpActivePeerConfig bgpPeerConfig,
      @Nullable Ip overrideLocalIp,
      Set<ConcreteInterfaceAddress> interfaceAddresses) {
    // Check overrideLocalIp first if present.
    if (overrideLocalIp != null) {
      return firstMatching(interfaceAddresses, addr -> addr.getIp().equals(overrideLocalIp));
    }

    // Check configured localIp next if present.
    if (bgpPeerConfig.getLocalIp() != null) {
      return firstMatching(
          interfaceAddresses, addr -> addr.getIp().equals(bgpPeerConfig.getLocalIp()));
    }

    // Check peer-address containment for single-hop eBGP sessions only next.
    if (bgpPeerConfig.getPeerAddress() != null && getSessionType(bgpPeerConfig) == EBGP_SINGLEHOP) {
      return firstMatching(
          interfaceAddresses, addr -> addr.getPrefix().containsIp(bgpPeerConfig.getPeerAddress()));
    }

    // Nothing else to try.
    return Optional.empty();
  }

  /**
   * Determines whether the ISP for the given BGP Peer is allowed by filters and has enough
   * information to generate, or explain the reason why this cannot happen.
   */
  @VisibleForTesting
  static Optional<String> validateOrExplainProblemCreatingIspConfig(
      BgpActivePeerConfig bgpPeerConfig, Set<Ip> allowedRemoteIps, LongSpace allowedRemoteAsns) {
    if (bgpPeerConfig.getLocalAs() == null) {
      return Optional.of("unable to determine local AS");
    } else if (bgpPeerConfig.getRemoteAsns().equals(LongSpace.of(bgpPeerConfig.getLocalAs()))) {
      return Optional.of("iBGP peers are not supported");
    } else if (bgpPeerConfig.getRemoteAsns().isEmpty()) {
      return Optional.of("unable to determine remote AS");
    } else if (allowedRemoteAsns.intersection(bgpPeerConfig.getRemoteAsns()).isEmpty()) {
      return Optional.of(
          String.format(
              "remote AS %s is not allowed by the filter", bgpPeerConfig.getRemoteAsns()));
    } else if (bgpPeerConfig.getPeerAddress() == null) {
      return Optional.of("remote IP is not configured");
    } else if (!allowedRemoteIps.isEmpty()
        && !allowedRemoteIps.contains(bgpPeerConfig.getPeerAddress())) {
      return Optional.of(
          String.format(
              "remote IP %s is not allowed by the filter", bgpPeerConfig.getPeerAddress()));
    }
    return Optional.empty();
  }

  /**
   * Computes the mirror of the {@code remotePeerConfig} and adds it to {@code bgpProcess}. Also
   * sets the export policy meant for the ISP.
   */
  @VisibleForTesting
  static void addBgpPeerToIsp(IspBgpPeer ispBgpPeer, BgpProcess bgpProcess, Role ispRole) {
    BgpPeerConfig.Builder<?, ?> ispPeerConfig =
        ispBgpPeer instanceof IspBgpActivePeer
            ? BgpActivePeerConfig.builder()
                .setPeerAddress(((IspBgpActivePeer) ispBgpPeer).getPeerAddress())
                .setLocalIp(((IspBgpActivePeer) ispBgpPeer).getLocalIp())
            : BgpUnnumberedPeerConfig.builder()
                .setPeerInterface(((IspBgpUnnumberedPeer) ispBgpPeer).getLocalIfacename())
                .setLocalIp(LINK_LOCAL_IP);

    ispPeerConfig
        .setRemoteAs(ispBgpPeer.getRemoteAsn())
        .setLocalAs(ispBgpPeer.getLocalAsn())
        .setEbgpMultihop(ispBgpPeer.getEbgpMultiHop())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(ispRole == Role.PRIVATE_BACKBONE)
                        .setSendExtendedCommunity(ispRole == Role.PRIVATE_BACKBONE)
                        .build())
                .build())
        .setBgpProcess(bgpProcess)
        .build();
  }
}
