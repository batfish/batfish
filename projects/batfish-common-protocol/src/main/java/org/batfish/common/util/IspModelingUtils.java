package org.batfish.common.util;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.annotations.VisibleForTesting;
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
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Util classes and functions to model ISPs and Internet for a given network */
public final class IspModelingUtils {

  static final String EXPORT_POLICY_ON_INTERNET = "exportPolicyOnInternet";
  static final String EXPORT_POLICY_ON_ISP = "exportPolicyOnIsp";
  private static final Ip FIRST_EVEN_INTERNET_IP = Ip.parse("240.1.1.2");
  static final long INTERNET_AS = 65537L;
  public static final String INTERNET_HOST_NAME = "internet";
  static final Ip INTERNET_OUT_ADDRESS = Ip.parse("240.254.254.1");
  static final String INTERNET_OUT_INTERFACE = "Internet_out_interface";
  static final int INTERNET_OUT_SUBNET = 30;
  private static final int ISP_INTERNET_SUBNET = 31;
  private static final String ISP_HOSTNAME_PREFIX = "isp";

  private IspModelingUtils() {}

  /** Contains the information required to create one ISP node */
  @ParametersAreNonnullByDefault
  static final class IspInfo {
    private @Nonnull List<InterfaceAddress> _interfaceAddresses;
    private @Nonnull List<BgpActivePeerConfig> _bgpActivePeerConfigs;

    IspInfo() {
      _interfaceAddresses = new ArrayList<>();
      _bgpActivePeerConfigs = new ArrayList<>();
    }

    IspInfo(
        List<InterfaceAddress> interfaceAddresses, List<BgpActivePeerConfig> bgpActivePeerConfigs) {
      _interfaceAddresses = interfaceAddresses;
      _bgpActivePeerConfigs = bgpActivePeerConfigs;
    }

    void addInterfaceAddress(InterfaceAddress interfaceAddress) {
      _interfaceAddresses.add(interfaceAddress);
    }

    void addBgpActivePeerConfig(BgpActivePeerConfig bgpActivePeerConfig) {
      _bgpActivePeerConfigs.add(bgpActivePeerConfig);
    }

    @Nonnull
    List<InterfaceAddress> getInterfaceAddresses() {
      return _interfaceAddresses;
    }

    @Nonnull
    List<BgpActivePeerConfig> getBgpActivePeerConfigs() {
      return _bgpActivePeerConfigs;
    }
  }

  /**
   * Creates and returns internet and ISP nodes for a {@link Map} of {@link Configuration}s
   *
   * @param configurations {@link Configuration}s for the given network
   * @param ispConfiguration {@link IspConfiguration} required to initialize the ISPs
   * @param logger {@link BatfishLogger} to log warnings and errors
   * @param warnings {@link Warnings} containing all the warnings logged during the ISP modeling
   * @return {@link Map} of {@link Configuration}s for the ISPs and Internet
   */
  public static Map<String, Configuration> getInternetAndIspNodes(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull IspConfiguration ispConfiguration,
      @Nonnull BatfishLogger logger,
      @Nonnull Warnings warnings) {

    NetworkFactory nf = new NetworkFactory();
    Map<String, Set<String>> interfaceSetByNodes =
        ispConfiguration.getBorderInterfaces().stream()
            .map(BorderInterfaceInfo::getBorderInterface)
            .collect(
                Collectors.groupingBy(
                    nodeInterfacePair -> nodeInterfacePair.getHostname().toLowerCase(),
                    Collectors.mapping(NodeInterfacePair::getInterface, Collectors.toSet())));

    Map<Long, IspInfo> asnToIspInfos = new HashMap<>();

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
          asnToIspInfos,
          warnings);
    }

    Map<String, Configuration> ispConfigurations =
        asnToIspInfos.entrySet().stream()
            .map(
                asnIspInfo ->
                    getIspConfigurationNode(asnIspInfo.getKey(), asnIspInfo.getValue(), nf, logger))
            .filter(Objects::nonNull)
            .collect(ImmutableMap.toImmutableMap(Configuration::getHostname, Function.identity()));
    // not proceeding if no ISPs were created
    if (ispConfigurations.isEmpty()) {
      return ispConfigurations;
    }

    Configuration internet = createInternetNode(nf);
    connectIspsToInternet(internet, ispConfigurations, nf);

    return ImmutableMap.<String, Configuration>builder()
        .putAll(ispConfigurations)
        .put(internet.getHostname(), internet)
        .build();
  }

  @VisibleForTesting
  static Configuration createInternetNode(NetworkFactory nf) {

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration internetConfiguration =
        cb.setHostname(INTERNET_HOST_NAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    internetConfiguration.setDeviceType(DeviceType.INTERNET);
    Vrf defaultVrf =
        nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(internetConfiguration).build();
    nf.interfaceBuilder()
        .setName(INTERNET_OUT_INTERFACE)
        .setOwner(internetConfiguration)
        .setVrf(defaultVrf)
        .setAddress(new InterfaceAddress(INTERNET_OUT_ADDRESS, INTERNET_OUT_SUBNET))
        .build();

    internetConfiguration
        .getDefaultVrf()
        .setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopInterface(INTERNET_OUT_INTERFACE)
                    .setAdministrativeCost(1)
                    .build()));

    BgpProcess bgpProcess =
        nf.bgpProcessBuilder()
            .setRouterId(INTERNET_OUT_ADDRESS)
            .setVrf(defaultVrf)
            .setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS)
            .build();
    bgpProcess.setMultipathEbgp(true);

    internetConfiguration.setRoutingPolicies(
        ImmutableSortedMap.of(
            EXPORT_POLICY_ON_INTERNET, getRoutingPolicyForInternet(internetConfiguration, nf)));
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
          .setAddress(new InterfaceAddress(internetInterfaceIp, ISP_INTERNET_SUBNET))
          .build();
      nf.interfaceBuilder()
          .setOwner(ispConfiguration)
          .setVrf(ispConfiguration.getDefaultVrf())
          .setAddress(new InterfaceAddress(ispInterfaceIp, ISP_INTERNET_SUBNET))
          .build();

      BgpActivePeerConfig.builder()
          .setPeerAddress(internetInterfaceIp)
          .setRemoteAs(INTERNET_AS)
          .setLocalIp(ispInterfaceIp)
          .setLocalAs(ispAs)
          .setBgpProcess(ispConfiguration.getDefaultVrf().getBgpProcess())
          .setExportPolicy(EXPORT_POLICY_ON_ISP)
          .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.instance())
          .build();

      BgpActivePeerConfig.builder()
          .setPeerAddress(ispInterfaceIp)
          .setRemoteAs(ispAs)
          .setLocalIp(internetInterfaceIp)
          .setLocalAs(INTERNET_AS)
          .setBgpProcess(internet.getDefaultVrf().getBgpProcess())
          .setExportPolicy(EXPORT_POLICY_ON_INTERNET)
          .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.instance())
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
      Map<Long, IspInfo> allIspInfos,
      @Nonnull Warnings warnings) {

    // collecting InterfaceAddresses for interfaces
    Map<String, Interface> lowerCasedInterfaces =
        configuration.getAllInterfaces().entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Entry::getValue));
    Map<Ip, InterfaceAddress> ipToInterfaceAddresses =
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
            .flatMap(iface -> iface.getAllAddresses().stream())
            .collect(ImmutableMap.toImmutableMap(InterfaceAddress::getIp, Function.identity()));

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
      IspInfo ispInfo =
          allIspInfos.computeIfAbsent(
              bgpActivePeerConfig.getRemoteAsns().least(), k -> new IspInfo());
      // merging ISP's interface addresses and eBGP confs from the current configuration
      ispInfo.addInterfaceAddress(
          new InterfaceAddress(
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
      Long asn, IspInfo ispInfo, NetworkFactory nf, BatfishLogger logger) {
    if (ispInfo.getBgpActivePeerConfigs().isEmpty()
        || ispInfo.getInterfaceAddresses().isEmpty()
        || ispInfo.getInterfaceAddresses().size() != ispInfo.getBgpActivePeerConfigs().size()) {
      logger.warnf("ISP information for ASN '%s' is not correct", asn);
      return null;
    }

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration ispConfiguration =
        cb.setHostname(String.format("%s_%s", ISP_HOSTNAME_PREFIX, asn))
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    ispConfiguration.setDeviceType(DeviceType.ISP);
    ispConfiguration.setRoutingPolicies(
        ImmutableSortedMap.of(EXPORT_POLICY_ON_ISP, getRoutingPolicyForIsp(ispConfiguration, nf)));
    Vrf defaultVrf = nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(ispConfiguration).build();

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
                    .map(InterfaceAddress::getIp)
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
                    .setExportPolicy(bgpActivePeerConfig.getExportPolicy())
                    .setBgpProcess(bgpProcess)
                    .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.instance())
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

  /** Creates a routing policy to advertise all static routes configured */
  @VisibleForTesting
  static RoutingPolicy getRoutingPolicyForInternet(Configuration internet, NetworkFactory nf) {
    PrefixSpace prefixSpace = new PrefixSpace();
    prefixSpace.addPrefix(Prefix.ZERO);
    return nf.routingPolicyBuilder()
        .setName(EXPORT_POLICY_ON_INTERNET)
        .setOwner(internet)
        .setStatements(
            Collections.singletonList(
                new If(
                    new Conjunction(
                        ImmutableList.of(
                            new MatchProtocol(RoutingProtocol.STATIC),
                            new MatchPrefixSet(
                                DestinationNetwork.instance(),
                                new ExplicitPrefixSet(prefixSpace)))),
                    ImmutableList.of(
                        new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)),
                        Statements.ExitAccept.toStaticStatement()))))
        .build();
  }

  /** Creates a routing policy to export all BGP routes */
  @VisibleForTesting
  static RoutingPolicy getRoutingPolicyForIsp(Configuration isp, NetworkFactory nf) {
    return nf.routingPolicyBuilder()
        .setName(EXPORT_POLICY_ON_ISP)
        .setOwner(isp)
        .setStatements(
            Collections.singletonList(
                new If(
                    new Conjunction(ImmutableList.of(new MatchProtocol(RoutingProtocol.BGP))),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))))
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
        .setRemoteAs(bgpActivePeerConfig.getLocalAs())
        .setLocalIp(bgpActivePeerConfig.getPeerAddress())
        .setLocalAs(bgpActivePeerConfig.getRemoteAsns().least())
        .setExportPolicy(EXPORT_POLICY_ON_ISP)
        .build();
  }
}
