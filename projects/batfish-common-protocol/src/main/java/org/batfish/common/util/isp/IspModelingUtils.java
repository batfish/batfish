package org.batfish.common.util.isp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.specifier.Location.interfaceLinkLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
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
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BgpPeerInfo;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspAttachment;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspNodeInfo;
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
  static final int HIGH_ADMINISTRATIVE_COST = 32767; // maximum possible

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
  static BgpProcess makeBgpProcess(Ip routerId, Vrf vrf) {
    return BgpProcess.builder()
        .setRouterId(routerId)
        .setVrf(vrf)
        .setEbgpAdminCost(20)
        .setIbgpAdminCost(200)
        .setLocalAdminCost(200)
        .build();
  }

  public static class ModeledNodes {

    @Nonnull private final Map<String, Configuration> _configurations;

    @Nonnull private final Set<Layer1Edge> _layer1Edgesdges;

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

    @Nonnull
    public Map<String, Configuration> getConfigurations() {
      return ImmutableMap.copyOf(_configurations);
    }

    @Nonnull
    public Set<Layer1Edge> getLayer1Edges() {
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

    return createInternetAndIspNodes(ispModels, new NetworkFactory(), logger);
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
    boolean internetConnection =
        ispNodeInfos.stream().map(IspNodeInfo::getInternetConnection).findFirst().orElse(true);
    IspTrafficFiltering filtering =
        ispNodeInfos.stream()
            .map(IspNodeInfo::getIspTrafficFiltering)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(IspTrafficFiltering.blockReservedAddressesAtInternet());

    // Merge the sets of additional announcements to internet is merging their prefixes
    Set<Prefix> additionalPrefixes =
        ispNodeInfos.stream()
            .flatMap(i -> i.getAdditionalAnnouncements().stream().map(IspAnnouncement::getPrefix))
            .collect(ImmutableSet.toImmutableSet());

    return IspModel.builder()
        .setAsn(asn)
        .setName(ispName)
        .setInternetConnection(internetConnection)
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
      warnings.redFlag(
          String.format(
              "ISP Modeling: Non-existent border node %s", nodeInterfacePair.getHostname()));
      return ImmutableList.of();
    }
    Interface snapshotIface =
        snapshotHost.getAllInterfaces().values().stream()
            .filter(i -> i.getName().equalsIgnoreCase(nodeInterfacePair.getInterface()))
            .findFirst()
            .orElse(null);
    if (snapshotIface == null) {
      warnings.redFlag(
          String.format("ISP Modeling: Non-existent border interface %s", nodeInterfacePair));
      return ImmutableList.of();
    }

    // TODO: Enforce interface type constraint here

    Set<Ip> localConcreteIps =
        snapshotIface.getAllConcreteAddresses().stream()
            .map(ConcreteInterfaceAddress::getIp)
            .collect(ImmutableSet.toImmutableSet());

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
                        bgpPeerConfig, localConcreteIps, remoteIps, remoteAsns))
            .collect(Collectors.toList());

    if (validBgpPeers.isEmpty()) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: No valid eBGP configurations for border interface %s",
              nodeInterfacePair));
      return ImmutableList.of();
    }

    Set<Long> asns =
        validBgpPeers.stream()
            .map(peer -> peer.getRemoteAsns().least())
            .collect(ImmutableSet.toImmutableSet());

    if (asns.size() > 1) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: Skipping border interface %s because it connects to multiple ASNs",
              nodeInterfacePair));
      return ImmutableList.of();
    }

    return validBgpPeers.stream()
        .map(peer -> makeSnapshotConnection(peer, snapshotIface))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Converts the {@code bgpPeerConfig} and snapshot iface information into a {@link
   * SnapshotConnection} object.
   */
  private static SnapshotConnection makeSnapshotConnection(
      BgpPeerConfig bgpPeerConfig, Interface snapshotIface) {
    String snapshotHostname = snapshotIface.getOwner().getHostname();
    String ispIfaceName = ispToSnapshotInterfaceName(snapshotHostname, snapshotIface.getName());
    Layer1Node snapshotL1node = new Layer1Node(snapshotHostname, snapshotIface.getName());

    if (bgpPeerConfig instanceof BgpUnnumberedPeerConfig) {
      IspInterface ispInterface =
          new IspInterface(ispIfaceName, LINK_LOCAL_ADDRESS, snapshotL1node, null);
      return new SnapshotConnection(
          snapshotHostname,
          ImmutableList.of(ispInterface),
          IspBgpUnnumberedPeer.create((BgpUnnumberedPeerConfig) bgpPeerConfig, ispIfaceName));
    }

    if (bgpPeerConfig instanceof BgpActivePeerConfig) {
      Ip peerAddress = ((BgpActivePeerConfig) bgpPeerConfig).getPeerAddress();
      checkArgument(peerAddress != null, "Peer address should not be null");
      ConcreteInterfaceAddress ifaceAddress =
          snapshotIface.getAllConcreteAddresses().stream()
              .filter(addr -> Objects.equals(addr.getIp(), bgpPeerConfig.getLocalIp()))
              .findFirst()
              .get();
      InterfaceAddress ispInterfaceAddress =
          ConcreteInterfaceAddress.create(peerAddress, ifaceAddress.getNetworkBits());
      IspInterface ispInterface =
          new IspInterface(ispIfaceName, ispInterfaceAddress, snapshotL1node, null);
      return new SnapshotConnection(
          snapshotHostname,
          ImmutableList.of(ispInterface),
          IspBgpActivePeer.create((BgpActivePeerConfig) bgpPeerConfig));
    }
    throw new IllegalArgumentException("makeRemote called with illegal BgpPeerConfig type");
  }

  /**
   * Given a {@link BgpPeerInfo} object, return the corresponding {@link SnapshotConnection} object.
   * Nothing is returned if the interface established BGP sessions with multiple ISPs.
   */
  @VisibleForTesting
  static Optional<SnapshotConnection> getSnapshotConnectionForBgpPeerInfo(
      BgpPeerInfo bgpPeerInfo,
      Set<Ip> remoteIps,
      LongSpace remoteAsns,
      Map<String, Configuration> configurations,
      Warnings warnings) {
    Configuration snapshotBgpHost = configurations.get(bgpPeerInfo.getHostname());
    if (snapshotBgpHost == null) {
      warnings.redFlag(
          String.format("ISP Modeling: Non-existent border node %s", bgpPeerInfo.getHostname()));
      return Optional.empty();
    }
    String bgpPeerVrf =
        bgpPeerInfo.getVrf() == null
            ? snapshotBgpHost.getDefaultVrf().getName()
            : bgpPeerInfo.getVrf();
    Optional<BgpActivePeerConfig> snapshotBgpPeerOpt =
        snapshotBgpHost.getVrfs().values().stream()
            // Assume that case-insensitive matching is good enough, though some vendors have
            // case-sensitive names per lab testing
            .filter(vrf -> vrf.getName().equalsIgnoreCase(bgpPeerVrf))
            .flatMap(vrf -> vrf.getBgpProcess().getActiveNeighbors().values().stream())
            .filter(peer -> bgpPeerInfo.getPeerAddress().equals(peer.getPeerAddress()))
            .findFirst();
    if (!snapshotBgpPeerOpt.isPresent()) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: No BGP neighbor %s found on node %s in %s vrf",
              bgpPeerInfo.getPeerAddress(), bgpPeerInfo.getHostname(), bgpPeerVrf));
      return Optional.empty();
    }
    BgpActivePeerConfig snapshotBgpPeer = snapshotBgpPeerOpt.get();
    if (!isValidBgpPeerConfigForBgpPeerInfo(snapshotBgpPeer, remoteIps, remoteAsns)) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: BGP neighbor %s on node %s is invalid.",
              bgpPeerInfo.getPeerAddress(), bgpPeerInfo.getHostname()));
      return Optional.empty();
    }
    if (bgpPeerInfo.getIspAttachment() == null) {
      return Optional.of(
          new SnapshotConnection(
              snapshotBgpHost.getHostname(),
              ImmutableList.of(),
              IspBgpActivePeer.create(snapshotBgpPeer)));
    }

    IspAttachment ispAttachment = bgpPeerInfo.getIspAttachment();
    String attachmentHostname =
        ispAttachment.getHostname() == null
            ? snapshotBgpHost.getHostname()
            : ispAttachment.getHostname();
    Configuration attachmentHost = configurations.get(attachmentHostname);
    if (attachmentHost == null) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: Non-existent ISP attachment node %s", ispAttachment.getHostname()));
      return Optional.empty();
    }
    Interface attachmentIface =
        snapshotBgpHost.getAllInterfaces().values().stream()
            .filter(i -> i.getName().equalsIgnoreCase(ispAttachment.getInterface()))
            .findFirst()
            .orElse(null);
    if (attachmentIface == null) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: Non-existent attachment interface %s on node %s",
              ispAttachment.getInterface(), attachmentHostname));
      return Optional.empty();
    }

    // TODO: Enforce interface type constraint here

    Optional<ConcreteInterfaceAddress> snapshotBgpIfaceAddress =
        snapshotBgpHost.getActiveInterfaces().values().stream()
            .filter(iface -> iface.getVrfName().equalsIgnoreCase(bgpPeerVrf))
            // prefer active interfaces
            .sorted(Comparator.comparing(iface -> !iface.getActive()))
            .flatMap(iface -> iface.getAllConcreteAddresses().stream())
            .filter(iface -> Objects.equals(iface.getIp(), snapshotBgpPeer.getLocalIp()))
            // for determinism
            .sorted()
            .findFirst();
    if (!snapshotBgpIfaceAddress.isPresent()) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: No active interface with local IP %s found for neighbor %s on node %s",
              snapshotBgpPeer.getLocalIp(),
              bgpPeerInfo.getPeerAddress(),
              snapshotBgpHost.getHostname()));
      return Optional.empty();
    }

    return Optional.of(
        makeSnapshotConnection(
            snapshotBgpPeer,
            snapshotBgpIfaceAddress.get(),
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
  private static SnapshotConnection makeSnapshotConnection(
      BgpActivePeerConfig snapshotBgpPeer,
      ConcreteInterfaceAddress snapshotBgpIfaceAddress,
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
        ConcreteInterfaceAddress.create(peerAddress, snapshotBgpIfaceAddress.getNetworkBits());
    IspInterface ispInterface =
        new IspInterface(ispIfaceName, ispInterfaceAddress, snapshotL1node, vlanTag);
    return new SnapshotConnection(
        snapshotAttachmentHostname,
        ImmutableList.of(ispInterface),
        IspBgpActivePeer.create(snapshotBgpPeer));
  }

  private static ModeledNodes createInternetAndIspNodes(
      Map<Long, IspModel> asnToIspModel, NetworkFactory nf, BatfishLogger logger) {
    ModeledNodes modeledNodes = new ModeledNodes();

    asnToIspModel
        .values()
        .forEach(
            ispModel ->
                createIspNode(ispModel, nf, logger)
                    .ifPresent(
                        ispConfiguration -> {
                          Set<Layer1Edge> layer1Edges =
                              connectIspToSnapshot(ispModel, ispConfiguration, nf, logger);

                          modeledNodes.addConfiguration(ispConfiguration);
                          layer1Edges.forEach(modeledNodes::addLayer1Edge);
                        }));

    boolean needInternet =
        asnToIspModel.values().stream().anyMatch(IspModel::getInternetConnection);

    // not proceeding if no ISPs were created or internet is not needed
    if (modeledNodes.getConfigurations().isEmpty() || !needInternet) {
      return modeledNodes;
    }

    Configuration internet = createInternetNode(nf);
    modeledNodes.addConfiguration(internet);

    modeledNodes.getConfigurations().values().stream()
        .filter(c -> c != internet)
        .forEach(
            c -> {
              long ispAsn = getAsnOfIspNode(c);
              if (asnToIspModel.get(ispAsn).getInternetConnection()) {
                Set<Layer1Edge> layer1Edges =
                    connectIspToInternet(ispAsn, asnToIspModel.get(ispAsn), c, internet, nf);
                layer1Edges.forEach(modeledNodes::addLayer1Edge);
              }
            });

    return modeledNodes;
  }

  /** Creates the modeled Internet node and inserts it into {@code modeledNodes} */
  @VisibleForTesting
  static Configuration createInternetNode(NetworkFactory nf) {
    Configuration internetConfiguration =
        nf.configurationBuilder()
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
      Configuration internetConfiguration,
      NetworkFactory nf) {

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
    nf.interfaceBuilder()
        .setOwner(ispConfiguration)
        .setVrf(ispConfiguration.getDefaultVrf())
        .setName(ISP_TO_INTERNET_INTERFACE_NAME)
        .setAddress(LINK_LOCAL_ADDRESS)
        .setIncomingFilter(fromInternet)
        .setOutgoingFilter(toInternet)
        .setType(InterfaceType.PHYSICAL)
        .build();

    Interface internetIface =
        nf.interfaceBuilder()
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
  static Optional<Configuration> createIspNode(
      IspModel ispModel, NetworkFactory nf, BatfishLogger logger) {
    if (ispModel.getSnapshotConnections().isEmpty()) {
      logger.warnf("ISP information for ASN '%s' is not correct", ispModel.getAsn());
      return Optional.empty();
    }

    Configuration ispConfiguration =
        nf.configurationBuilder()
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
      IspModel ispModel, Configuration ispConfiguration, NetworkFactory nf, BatfishLogger logger) {

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
                            nf.interfaceBuilder()
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
                  ispConfiguration.getDefaultVrf().getBgpProcess());
            });

    return layer1Edges.build();
  }

  /**
   * Gets the local AS of a given ISP node {@link Configuration}. Since Local AS of all eBGP peers
   * on this node will be same, returning the Local AS of the any eBGP peer will suffice.
   */
  @VisibleForTesting
  @Nonnull
  static Long getAsnOfIspNode(Configuration ispConfiguration) {
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

  @VisibleForTesting
  static boolean isValidBgpPeerForBorderInterfaceInfo(
      BgpPeerConfig bgpPeerConfig,
      Set<Ip> validLocalIps,
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

      // limit to peers with statically determined local IP -- that is how we know that the
      // session is indeed tied to the interface
      return Objects.nonNull(bgpPeerConfig.getLocalIp())
          && validLocalIps.contains(bgpPeerConfig.getLocalIp())
          && Objects.nonNull(activePeerConfig.getPeerAddress()) // peer address is defined
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

  @VisibleForTesting
  static boolean isValidBgpPeerConfigForBgpPeerInfo(
      BgpActivePeerConfig bgpPeerConfig, Set<Ip> allowedRemoteIps, LongSpace allowedRemoteAsns) {
    return Objects.nonNull(bgpPeerConfig.getLocalAs()) // local AS is defined
        && Objects.nonNull(
            bgpPeerConfig.getLocalIp()) // local IP is defined -- used as peer address on ISP
        && !bgpPeerConfig
            .getRemoteAsns()
            .equals(LongSpace.of(bgpPeerConfig.getLocalAs())) // not iBGP
        && !allowedRemoteAsns
            .intersection(bgpPeerConfig.getRemoteAsns())
            .isEmpty() // remote AS is defined and allowed
        && Objects.nonNull(bgpPeerConfig.getPeerAddress()) // peer address is defined
        && (allowedRemoteIps.isEmpty()
            || allowedRemoteIps.contains(
                bgpPeerConfig.getPeerAddress())); // peer address is allowed
  }

  /**
   * Computes the mirror of the {@code remotePeerConfig} and adds it to {@code bgpProcess}. Also
   * sets the export policy meant for the ISP.
   */
  @VisibleForTesting
  static void addBgpPeerToIsp(IspBgpPeer ispBgpPeer, BgpProcess bgpProcess) {
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
                .build())
        .setBgpProcess(bgpProcess)
        .build();
  }
}
