package org.batfish.common.util;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.specifier.Location.interfaceLinkLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspNodeInfo;
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
  private static final Ip FIRST_EVEN_INTERNET_IP = Ip.parse("240.1.1.2");
  static final long INTERNET_AS = 65537L;
  public static final String INTERNET_HOST_NAME = "internet";
  static final Ip INTERNET_OUT_ADDRESS = INTERNET_OUT_SUBNET.getFirstHostIp();
  static final String INTERNET_OUT_INTERFACE = "Internet_out_interface";
  private static final int ISP_INTERNET_SUBNET = 31;
  // null routing private address space at the internet prevents "INSUFFICIENT_INFO" for networks
  // that use this space internally
  public static final Set<Prefix> INTERNET_NULL_ROUTED_PREFIXES =
      ImmutableSet.of(
          Prefix.parse("10.0.0.0/8"),
          Prefix.parse("172.16.0.0/12"),
          Prefix.parse("192.168.0.0/16"));

  /** Use this cost to install static routes on ISP nodes for prefixes originated to the Internet */
  static final int HIGH_ADMINISTRATIVE_COST = 32767; // maximum possible

  public static String getDefaultIspNodeName(Long asn) {
    return String.format("%s_%s", "isp", asn);
  }

  private IspModelingUtils() {}

  /** Contains the information required to create one ISP node */
  @ParametersAreNonnullByDefault
  static final class IspInfo {
    private long _asn;
    private @Nonnull List<ConcreteInterfaceAddress> _interfaceAddresses;
    private @Nonnull List<BgpActivePeerConfig> _bgpActivePeerConfigs;
    private @Nonnull String _name;
    private @Nonnull Set<Prefix> _additionalPrefixesToInternet;

    IspInfo(long asn, String name) {
      this(asn, new ArrayList<>(), new ArrayList<>(), name, ImmutableSet.of());
    }

    IspInfo(long asn, String name, Set<Prefix> additionalPrefixesToInternet) {
      this(asn, new ArrayList<>(), new ArrayList<>(), name, additionalPrefixesToInternet);
    }

    IspInfo(
        long asn,
        List<ConcreteInterfaceAddress> interfaceAddresses,
        List<BgpActivePeerConfig> bgpActivePeerConfigs,
        String name) {
      this(asn, interfaceAddresses, bgpActivePeerConfigs, name, ImmutableSet.of());
    }

    IspInfo(
        long asn,
        List<ConcreteInterfaceAddress> interfaceAddresses,
        List<BgpActivePeerConfig> bgpActivePeerConfigs,
        String name,
        Set<Prefix> additionalPrefixesToInternet) {
      _asn = asn;
      _interfaceAddresses = interfaceAddresses;
      _bgpActivePeerConfigs = bgpActivePeerConfigs;
      _name = name;
      _additionalPrefixesToInternet = ImmutableSet.copyOf(additionalPrefixesToInternet);
    }

    void addInterfaceAddress(ConcreteInterfaceAddress interfaceAddress) {
      _interfaceAddresses.add(interfaceAddress);
    }

    void addBgpActivePeerConfig(BgpActivePeerConfig bgpActivePeerConfig) {
      _bgpActivePeerConfigs.add(bgpActivePeerConfig);
    }

    @Nonnull
    List<ConcreteInterfaceAddress> getInterfaceAddresses() {
      return _interfaceAddresses;
    }

    @Nonnull
    List<BgpActivePeerConfig> getBgpActivePeerConfigs() {
      return _bgpActivePeerConfigs;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("_interfaceAddresses", _interfaceAddresses)
          .add("_bgpActivePeerConfigs", _bgpActivePeerConfigs)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof IspInfo)) {
        return false;
      }
      IspInfo ispInfo = (IspInfo) o;
      return _asn == ispInfo._asn
          && Objects.equals(_interfaceAddresses, ispInfo._interfaceAddresses)
          && Objects.equals(_bgpActivePeerConfigs, ispInfo._bgpActivePeerConfigs)
          && _name.equals(ispInfo._name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_asn, _interfaceAddresses, _bgpActivePeerConfigs, _name);
    }

    public long getAsn() {
      return _asn;
    }

    public void setAsn(long asn) {
      _asn = asn;
    }

    @Nonnull
    public String getName() {
      return _name;
    }

    /**
     * Returns the prefixes that the ISP should announce to the Internet over the BGP connection
     * (beyond just passing along what it hears from other connected nodes)
     */
    @Nonnull
    public Set<Prefix> getAdditionalPrefixesToInternet() {
      return _additionalPrefixesToInternet;
    }
  }

  /**
   * Creates and returns internet and ISP nodes for a {@link Map} of {@link Configuration}s
   *
   * @param configurations {@link Configuration}s for the given network
   * @param ispConfigurations A list of {@link IspConfiguration} objects to initialize the ISPs
   * @param logger {@link BatfishLogger} to log warnings and errors
   * @param warnings {@link Warnings} containing all the warnings logged during the ISP modeling
   * @return {@link Map} of {@link Configuration}s for the ISPs and Internet
   */
  public static Map<String, Configuration> getInternetAndIspNodes(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull List<IspConfiguration> ispConfigurations,
      @Nonnull BatfishLogger logger,
      @Nonnull Warnings warnings) {

    NetworkFactory nf = new NetworkFactory();

    Map<Long, IspInfo> asnToIspInfos =
        combineIspConfigurations(configurations, ispConfigurations, warnings);

    List<String> conflicts = ispNameConflicts(configurations, asnToIspInfos);

    if (!conflicts.isEmpty()) {
      conflicts.forEach(warnings::redFlag);
      return ImmutableMap.of();
    }

    return createInternetAndIspNodes(asnToIspInfos, nf, logger);
  }

  /**
   * Checks if the ISP names conflicts with a node name in the configurations or with another ISP
   * name. Returns messages that explain the conflicts
   */
  @VisibleForTesting
  static List<String> ispNameConflicts(
      Map<String, Configuration> configurations, Map<Long, IspInfo> asnToIspInfo) {
    ImmutableList.Builder<String> conflicts = ImmutableList.builder();

    asnToIspInfo.forEach(
        (asn, ispInfo) -> {
          String ispName = ispInfo.getName();
          if (configurations.containsKey(ispName)) {
            conflicts.add(
                String.format(
                    "ISP name %s for ASN %d conflicts with a node name in the snapshot",
                    ispName, asn));
          }
          asnToIspInfo.values().stream()
              .filter(info -> info.getAsn() > asn && info.getName().equals(ispName))
              .forEach(
                  info ->
                      conflicts.add(
                          String.format(
                              "ISP name %s for ASN %d conflicts with that for ASN %d",
                              ispName, asn, info.getAsn())));
        });
    return conflicts.build();
  }

  @VisibleForTesting
  static Map<Long, IspInfo> combineIspConfigurations(
      Map<String, Configuration> configurations,
      List<IspConfiguration> ispConfigurations,
      Warnings warnings) {
    Map<Long, IspInfo> asnToIspInfos = new HashMap<>();

    for (IspConfiguration ispConfiguration : ispConfigurations) {
      Map<String, Set<String>> interfaceSetByNodes =
          ispConfiguration.getBorderInterfaces().stream()
              .map(BorderInterfaceInfo::getBorderInterface)
              .collect(
                  Collectors.groupingBy(
                      nodeInterfacePair -> nodeInterfacePair.getHostname().toLowerCase(),
                      Collectors.mapping(NodeInterfacePair::getInterface, Collectors.toSet())));

      for (Entry<String, Set<String>> nodeAndInterfaces : interfaceSetByNodes.entrySet()) {
        Configuration configuration = configurations.get(nodeAndInterfaces.getKey());
        if (configuration == null) {
          warnings.redFlag(
              String.format(
                  "ISP Modeling: Non-existent border node %s specified in ISP configuration",
                  nodeAndInterfaces.getKey()));
          continue;
        }
        populateIspInfos(
            configuration,
            nodeAndInterfaces.getValue(),
            ispConfiguration.getfilter().getOnlyRemoteIps(),
            ispConfiguration.getfilter().getOnlyRemoteAsns(),
            ispConfiguration.getIspNodeInfos(),
            asnToIspInfos,
            warnings);
      }
    }

    return asnToIspInfos;
  }

  private static Map<String, Configuration> createInternetAndIspNodes(
      Map<Long, IspInfo> asnToIspInfos, NetworkFactory nf, BatfishLogger logger) {
    Map<String, Configuration> ispConfigurations =
        asnToIspInfos.entrySet().stream()
            .map(asnIspInfo -> getIspConfigurationNode(asnIspInfo.getValue(), nf, logger))
            .filter(Objects::nonNull)
            .collect(ImmutableMap.toImmutableMap(Configuration::getHostname, Function.identity()));
    // not proceeding if no ISPs were created
    if (ispConfigurations.isEmpty()) {
      return ispConfigurations;
    }

    Configuration internet = createInternetNode();
    connectIspsToInternet(internet, ispConfigurations, nf);

    return ImmutableMap.<String, Configuration>builder()
        .putAll(ispConfigurations)
        .put(internet.getHostname(), internet)
        .build();
  }

  @VisibleForTesting
  static Configuration createInternetNode() {
    Configuration.Builder cb = Configuration.builder();
    Configuration internetConfiguration =
        cb.setHostname(INTERNET_HOST_NAME)
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

    BgpProcess bgpProcess =
        BgpProcess.builder()
            .setRouterId(INTERNET_OUT_ADDRESS)
            .setVrf(defaultVrf)
            .setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS)
            .build();
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
   * Adds connection between internet and each ISP by creating interface pairs (in /31 subnet) on
   * both with connected edges. Also adds eBGP peers on both Internet and all the ISPs to peer with
   * each other using the created Interface pairs.
   */
  private static void connectIspsToInternet(
      Configuration internet, Map<String, Configuration> ispConfigurations, NetworkFactory nf) {
    Ip internetInterfaceIp = FIRST_EVEN_INTERNET_IP;
    for (Configuration ispConfiguration : ispConfigurations.values()) {
      long ispAs = getAsnOfIspNode(ispConfiguration);
      Ip ispInterfaceIp = Ip.create(internetInterfaceIp.asLong() + 1);
      nf.interfaceBuilder()
          .setOwner(internet)
          .setVrf(internet.getDefaultVrf())
          .setAddress(ConcreteInterfaceAddress.create(internetInterfaceIp, ISP_INTERNET_SUBNET))
          .build();
      nf.interfaceBuilder()
          .setOwner(ispConfiguration)
          .setVrf(ispConfiguration.getDefaultVrf())
          .setAddress(ConcreteInterfaceAddress.create(ispInterfaceIp, ISP_INTERNET_SUBNET))
          .build();

      BgpActivePeerConfig.builder()
          .setPeerAddress(internetInterfaceIp)
          .setRemoteAs(INTERNET_AS)
          .setLocalIp(ispInterfaceIp)
          .setLocalAs(ispAs)
          .setBgpProcess(ispConfiguration.getDefaultVrf().getBgpProcess())
          .setIpv4UnicastAddressFamily(
              Ipv4UnicastAddressFamily.builder()
                  .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_INTERNET)
                  .build())
          .build();

      BgpActivePeerConfig.builder()
          .setPeerAddress(ispInterfaceIp)
          .setRemoteAs(ispAs)
          .setLocalIp(internetInterfaceIp)
          .setLocalAs(INTERNET_AS)
          .setBgpProcess(internet.getDefaultVrf().getBgpProcess())
          .setIpv4UnicastAddressFamily(
              Ipv4UnicastAddressFamily.builder().setExportPolicy(EXPORT_POLICY_ON_INTERNET).build())
          .build();

      internetInterfaceIp = Ip.create(internetInterfaceIp.asLong() + 2);
    }
  }

  /**
   * Extracts the ISP information from a given {@link Configuration} and merges it to a given map of
   * ASNs to {@link IspInfo}s
   *
   * @param configuration {@link Configuration} owning given interfaces
   * @param interfaces {@link List} of interfaces on this node having eBGP sessions with the ISP
   * @param remoteIps Expected {@link Ip}s of the ISPs (optional)
   * @param remoteAsnsList Expected ASNs of the ISP nodes (optional)
   * @param allIspInfos {@link Map} containing existing ASNs and corresponding {@link IspInfo}s to
   *     which ISPs extracted from this {@link Configuration} will be merged
   * @param warnings {@link Warnings} for ISP and Internet modeling
   */
  @VisibleForTesting
  static void populateIspInfos(
      @Nonnull Configuration configuration,
      @Nonnull Set<String> interfaces,
      @Nonnull List<Ip> remoteIps,
      @Nonnull List<Long> remoteAsnsList,
      @Nonnull List<IspNodeInfo> ispNodeInfos,
      Map<Long, IspInfo> allIspInfos,
      @Nonnull Warnings warnings) {

    // collecting InterfaceAddresses for interfaces
    Map<String, Interface> lowerCasedInterfaces =
        configuration.getAllInterfaces().entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Entry::getValue));
    Map<Ip, ConcreteInterfaceAddress> ipToInterfaceAddresses =
        interfaces.stream()
            .map(
                ifaceName -> {
                  Interface iface = lowerCasedInterfaces.get(ifaceName.toLowerCase());
                  if (iface == null) {
                    warnings.redFlag(
                        String.format(
                            "ISP Modeling: Cannot find interface %s on node %s",
                            ifaceName, configuration.getHostname()));
                  }
                  return iface;
                })
            .filter(Objects::nonNull)
            .flatMap(iface -> iface.getAllConcreteAddresses().stream())
            .collect(
                ImmutableMap.toImmutableMap(ConcreteInterfaceAddress::getIp, Function.identity()));

    Set<Ip> remoteIpsSet = ImmutableSet.copyOf(remoteIps);
    LongSpace remoteAsns =
        remoteAsnsList.isEmpty()
            ? ALL_AS_NUMBERS
            : LongSpace.builder().includingAll(remoteAsnsList).build();

    List<BgpActivePeerConfig> validBgpActivePeerConfigs =
        configuration.getVrfs().values().stream()
            .map(Vrf::getBgpProcess)
            .filter(Objects::nonNull)
            .flatMap(bgpProcess -> bgpProcess.getActiveNeighbors().values().stream())
            .filter(
                bgpActivePeerConfig ->
                    isValidBgpPeerConfig(
                        bgpActivePeerConfig,
                        ipToInterfaceAddresses.keySet(),
                        remoteIpsSet,
                        remoteAsns))
            .collect(Collectors.toList());

    if (validBgpActivePeerConfigs.isEmpty()) {
      warnings.redFlag(
          String.format(
              "ISP Modeling: Cannot find any valid eBGP configurations for provided interfaces on node %s",
              configuration.getHostname()));
    }
    for (BgpActivePeerConfig bgpActivePeerConfig : validBgpActivePeerConfigs) {
      Long asn = bgpActivePeerConfig.getRemoteAsns().least();
      // Pick the first name we find; else, use default
      String ispName =
          ispNodeInfos.stream()
              .filter(i -> i.getAsn() == asn)
              .map(IspNodeInfo::getName)
              .findFirst()
              .orElse(getDefaultIspNodeName(asn));
      // Merge the sets of additional announcements to internet is merging their prefixes
      Set<Prefix> additionalPrefixes =
          ispNodeInfos.stream()
              .filter(i -> i.getAsn() == asn)
              .flatMap(i -> i.getAdditionalAnnouncements().stream().map(IspAnnouncement::getPrefix))
              .collect(ImmutableSet.toImmutableSet());
      IspInfo ispInfo =
          allIspInfos.computeIfAbsent(asn, k -> new IspInfo(asn, ispName, additionalPrefixes));
      // merging ISP's interface addresses and eBGP confs from the current configuration
      ispInfo.addInterfaceAddress(
          ConcreteInterfaceAddress.create(
              bgpActivePeerConfig.getPeerAddress(),
              ipToInterfaceAddresses.get(bgpActivePeerConfig.getLocalIp()).getNetworkBits()));
      ispInfo.addBgpActivePeerConfig(getBgpPeerOnIsp(bgpActivePeerConfig));
    }
  }

  /**
   * Creates and returns the {@link Configuration} for the ISP node given an ASN and {@link IspInfo}
   */
  @VisibleForTesting
  @Nullable
  static Configuration getIspConfigurationNode(
      IspInfo ispInfo, NetworkFactory nf, BatfishLogger logger) {
    if (ispInfo.getBgpActivePeerConfigs().isEmpty()
        || ispInfo.getInterfaceAddresses().isEmpty()
        || ispInfo.getInterfaceAddresses().size() != ispInfo.getBgpActivePeerConfigs().size()) {
      logger.warnf("ISP information for ASN '%s' is not correct", ispInfo.getAsn());
      return null;
    }

    Configuration ispConfiguration =
        Configuration.builder()
            .setHostname(ispInfo.getName())
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDeviceModel(DeviceModel.BATFISH_ISP)
            .build();
    ispConfiguration.setDeviceType(DeviceType.ISP);
    Vrf defaultVrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(ispConfiguration).build();

    // add a static route for each additional prefix announced to the internet
    ispConfiguration
        .getDefaultVrf()
        .setStaticRoutes(
            ImmutableSortedSet.copyOf(
                ispInfo.getAdditionalPrefixesToInternet().stream()
                    .map(
                        prefix ->
                            StaticRoute.builder()
                                .setNetwork(prefix)
                                .setNextHopInterface(NULL_INTERFACE_NAME)
                                .setAdministrativeCost(HIGH_ADMINISTRATIVE_COST)
                                .build())
                    .collect(ImmutableSet.toImmutableSet())));

    PrefixSpace prefixSpace = new PrefixSpace();
    ispInfo.getAdditionalPrefixesToInternet().forEach(prefixSpace::addPrefix);

    ispConfiguration.setRoutingPolicies(
        ImmutableSortedMap.of(
            EXPORT_POLICY_ON_ISP_TO_CUSTOMERS,
            installRoutingPolicyForIspToCustomers(ispConfiguration),
            EXPORT_POLICY_ON_ISP_TO_INTERNET,
            installRoutingPolicyForIspToInternet(ispConfiguration, prefixSpace)));

    ispInfo
        .getInterfaceAddresses()
        .forEach(
            interfaceAddress ->
                nf.interfaceBuilder()
                    .setOwner(ispConfiguration)
                    .setVrf(defaultVrf)
                    .setAddress(interfaceAddress)
                    .build());

    // using the lowest IP among the InterfaceAddresses as the router ID
    BgpProcess bgpProcess =
        nf.bgpProcessBuilder()
            .setRouterId(
                ispInfo.getInterfaceAddresses().stream()
                    .map(ConcreteInterfaceAddress::getIp)
                    .min(Ip::compareTo)
                    .orElse(null))
            .setVrf(ispConfiguration.getDefaultVrf())
            .setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS)
            .build();
    bgpProcess.setMultipathEbgp(true);

    ispInfo
        .getBgpActivePeerConfigs()
        .forEach(
            bgpActivePeerConfig ->
                BgpActivePeerConfig.builder()
                    .setLocalIp(bgpActivePeerConfig.getLocalIp())
                    .setLocalAs(bgpActivePeerConfig.getLocalAs())
                    .setPeerAddress(bgpActivePeerConfig.getPeerAddress())
                    .setRemoteAsns(bgpActivePeerConfig.getRemoteAsns())
                    .setBgpProcess(bgpProcess)
                    .setIpv4UnicastAddressFamily(
                        Ipv4UnicastAddressFamily.builder()
                            .setExportPolicy(
                                bgpActivePeerConfig.getIpv4UnicastAddressFamily().getExportPolicy())
                            .build())
                    .build());

    return ispConfiguration;
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
    checkState(
        !ispConfiguration.getDefaultVrf().getBgpProcess().getActiveNeighbors().isEmpty(),
        "ISP should have greater than 0 BGP peers");
    Long localAs =
        ispConfiguration
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .values()
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
  static boolean isValidBgpPeerConfig(
      @Nonnull BgpActivePeerConfig bgpActivePeerConfig,
      @Nonnull Set<Ip> localIps,
      @Nonnull Set<Ip> remoteIps,
      @Nonnull LongSpace remoteAsns) {
    return Objects.nonNull(bgpActivePeerConfig.getLocalIp())
        && Objects.nonNull(bgpActivePeerConfig.getLocalAs())
        && Objects.nonNull(bgpActivePeerConfig.getPeerAddress())
        && !bgpActivePeerConfig
            .getRemoteAsns()
            .equals(LongSpace.of(bgpActivePeerConfig.getLocalAs()))
        && localIps.contains(bgpActivePeerConfig.getLocalIp())
        && (remoteIps.isEmpty() || remoteIps.contains(bgpActivePeerConfig.getPeerAddress()))
        && !remoteAsns.intersection(bgpActivePeerConfig.getRemoteAsns()).isEmpty();
  }

  /**
   * Returns the {@link BgpActivePeerConfig} to be used on ISP by flipping the local and remote AS
   * and IP for a given eBGP peer configuration. Also sets the export policy meant for the ISP
   */
  @VisibleForTesting
  static BgpActivePeerConfig getBgpPeerOnIsp(BgpActivePeerConfig bgpActivePeerConfig) {
    return BgpActivePeerConfig.builder()
        .setPeerAddress(bgpActivePeerConfig.getLocalIp())
        .setRemoteAs(
            firstNonNull(
                bgpActivePeerConfig.getConfederationAsn(), bgpActivePeerConfig.getLocalAs()))
        .setLocalIp(bgpActivePeerConfig.getPeerAddress())
        .setLocalAs(bgpActivePeerConfig.getRemoteAsns().least())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                .build())
        .build();
  }
}
