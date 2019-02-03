package org.batfish.common.util;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.common.util.CommonUtil.toImmutableSortedMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Util classes and functions to model ISPs and Internet for a given network */
public class ModelingUtils {

  private static final Ip INTERNET_OUT_ADDRESS = Ip.parse("240.254.254.1");
  private static final Ip FIRST_EVEN_INTERNET_IP = Ip.parse("240.1.1.0");
  private static final int ISP_INTERNET_SUBNET = 31;
  private static final String DEFAULT_ROUTE_ROUTING_POLICY = "defaultRoutingPolicy";
  private static final int INTERNET_OUT_SUBNET = 30;
  private static final long INTERNET_AS = 1111L;

  /** Contains the information required to create one ISP node */
  @ParametersAreNonnullByDefault
  private static class IspInfo {
    private @Nonnull List<InterfaceAddress> _interfaceAddresses;
    private @Nonnull List<BgpActivePeerConfig> _bgpActivePeerConfigs;

    IspInfo(
        List<InterfaceAddress> interfaceAddresses, List<BgpActivePeerConfig> bgpActivePeerConfigs) {
      checkState(!interfaceAddresses.isEmpty(), "ISP should have non-zero interfaces");
      checkState(!bgpActivePeerConfigs.isEmpty(), "ISP should have non-zero BGP peer configs ");
      _interfaceAddresses = interfaceAddresses;
      _bgpActivePeerConfigs = bgpActivePeerConfigs;
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
   * @param interfacesConnectedToIsps {@link List} of {@link NodeInterfacePair}s connected to ISPs
   * @param asNumOfIsps {@link List} optional {@link List} of AS numbers of ISPs
   * @param ipsOfIsps {@link List} optional {@link List} of {@link Ip}s of ISPs
   * @return {@link Map} of {@link Configuration}s for the ISPs and Internet
   */
  private static Map<String, Configuration> addInternetAndIspNodes(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull List<NodeInterfacePair> interfacesConnectedToIsps,
      @Nullable List<Long> asNumOfIsps,
      @Nullable List<Ip> ipsOfIsps) {
    Map<String, Set<String>> interfaceSetByNodes =
        interfacesConnectedToIsps.stream()
            .collect(
                Collectors.groupingBy(
                    NodeInterfacePair::getHostname,
                    Collectors.mapping(NodeInterfacePair::getInterface, Collectors.toSet())));

    Map<Long, IspInfo> asnToIspInfos = new HashMap<>();

    for (Entry<String, Set<String>> nodeAndInterfaces : interfaceSetByNodes.entrySet()) {
      Configuration configuration = configurations.get(nodeAndInterfaces.getKey());
      if (configuration == null) {
        continue;
      }
      populateIspInfos(
          configuration, nodeAndInterfaces.getValue(), ipsOfIsps, asNumOfIsps, asnToIspInfos);
    }

    Map<String, Configuration> ispConfigurations =
        asnToIspInfos.entrySet().stream()
            .map(asnIspInfo -> getIspConfigurationNode(asnIspInfo.getKey(), asnIspInfo.getValue()))
            .collect(ImmutableMap.toImmutableMap(Configuration::getHostname, Function.identity()));
    Configuration internet = createInternetNode();
    connectIspsToInternet(internet, ispConfigurations, DEFAULT_ROUTE_ROUTING_POLICY);
    
    return ImmutableMap.<String, Configuration>builder()
        .putAll(ispConfigurations)
        .put(internet.getHostname(), internet)
        .build();
  }

  private static Configuration createInternetNode() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration internetConfiguration =
        cb.setHostname("internet").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Interface outInterface =
        nf.interfaceBuilder()
            .setOwner(internetConfiguration)
            .setAddress(new InterfaceAddress(INTERNET_OUT_ADDRESS, INTERNET_OUT_SUBNET))
            .build();

    internetConfiguration
        .getDefaultVrf()
        .setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(Prefix.parse("0.0.0.0./0"))
                    .setNextHopInterface(outInterface.getName())
                    .build()));

    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.setRouterId(INTERNET_OUT_ADDRESS);
    internetConfiguration.getDefaultVrf().setBgpProcess(bgpProcess);
    internetConfiguration.setRoutingPolicies(
        ImmutableSortedMap.of(DEFAULT_ROUTE_ROUTING_POLICY, getDefaultRoutingPolicy()));
    return internetConfiguration;
  }

  /** Creates a routing policy to advertise all static routes configured */
  private static RoutingPolicy getDefaultRoutingPolicy() {
    NetworkFactory nf = new NetworkFactory();
    return nf.routingPolicyBuilder()
        .setName(DEFAULT_ROUTE_ROUTING_POLICY)
        .setStatements(
            Collections.singletonList(
                new If(
                    new MatchProtocol(RoutingProtocol.STATIC),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))))
        .build();
  }

  /**
   * Adds connection between internet and each ISP by creating interface pairs (in /31 subnet) on
   * both with connected edges. Also adds EBGP peers on both Internet and all the ISPs to peer with
   * each other using the created Interface pairs.
   */
  private static void connectIspsToInternet(
      Configuration internet,
      Map<String, Configuration> ispConfigurations,
      String exportPolicyOnInternet) {
    NetworkFactory nf = new NetworkFactory();
    Ip internetInterfaceIp = FIRST_EVEN_INTERNET_IP;
    Long localAs = null;
    for (Configuration ispConfiguration : ispConfigurations.values()) {
      localAs = firstNonNull(localAs, getAsnOfIspNode(ispConfiguration));
      Ip ispInterfaceIp = Ip.create(internetInterfaceIp.asLong() + 1);
      nf.interfaceBuilder()
          .setOwner(internet)
          .setAddress(new InterfaceAddress(internetInterfaceIp, ISP_INTERNET_SUBNET))
          .build();
      nf.interfaceBuilder()
          .setOwner(ispConfiguration)
          .setAddress(new InterfaceAddress(ispInterfaceIp, ISP_INTERNET_SUBNET))
          .build();

      BgpActivePeerConfig ispToInternet =
          BgpActivePeerConfig.builder()
              .setPeerAddress(internetInterfaceIp)
              .setRemoteAs(INTERNET_AS)
              .setLocalIp(ispInterfaceIp)
              .setLocalAs(localAs)
              .build();
      ispConfiguration
          .getDefaultVrf()
          .getBgpProcess()
          .getActiveNeighbors()
          .put(Prefix.create(internetInterfaceIp, 32), ispToInternet);
      // internet EBGP peers will also have the export policy to export the static route
      internet
          .getDefaultVrf()
          .getBgpProcess()
          .getActiveNeighbors()
          .put(
              Prefix.create(ispInterfaceIp, 32),
              reverseLocalAndRemote(ispToInternet, exportPolicyOnInternet));

      internetInterfaceIp = Ip.create(internetInterfaceIp.asLong() + 2);
    }
  }

  /**
   * Gets the local AS of a given ISP node {@link Configuration}. Since Local AS of all EBGP peers
   * on this node will be same, returning the Local AS of the any EBGP peer will suffice.
   */
  @Nonnull
  private static Long getAsnOfIspNode(Configuration ispConfiguration) {
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
    checkState(Objects.nonNull(localAs), "Local AS of all EBGP peers should be set on ISP");
    return localAs;
  }

  /**
   * Extracts the ISP information from a given {@link Configuration} and merges it to a given map of
   * ASNs to {@link IspInfo}s
   *
   * @param configuration {@link Configuration} owning given interfaces
   * @param interfaces {@link List} of interfaces on this node having EBGP sessions with the ISP
   * @param remoteIps Expected {@link Ip}s of the ISPs (optional)
   * @param remoteAsns Expected ASNs of the ISP nodes (optional)
   * @param allIspInfos {@link Map} containing existing ASNs and corresponding {@link IspInfo}s to
   *     which ISPs extracted from this {@link Configuration} will be merged
   */
  private static void populateIspInfos(
      @Nonnull Configuration configuration,
      @Nonnull Set<String> interfaces,
      @Nullable List<Ip> remoteIps,
      @Nullable List<Long> remoteAsns,
      Map<Long, IspInfo> allIspInfos) {

    // collecting InterfaceAddresses for interfaces
    Map<Ip, InterfaceAddress> ipToInterfaceAddresses =
        interfaces.stream()
            .map(iface -> configuration.getAllInterfaces().get(iface))
            .filter(Objects::nonNull)
            .flatMap(iface -> iface.getAllAddresses().stream())
            .collect(ImmutableMap.toImmutableMap(InterfaceAddress::getIp, Function.identity()));

    List<BgpActivePeerConfig> validBgpActivePeerConfigs =
        configuration.getVrfs().values().stream()
            .map(Vrf::getBgpProcess)
            .flatMap(bgpProcess -> bgpProcess.getActiveNeighbors().values().stream())
            .filter(
                bgpActivePeerConfig ->
                    isValidBgpPeerConfig(
                        bgpActivePeerConfig,
                        ipToInterfaceAddresses.keySet(),
                        remoteIps,
                        remoteAsns))
            .collect(Collectors.toList());

    for (BgpActivePeerConfig bgpActivePeerConfig : validBgpActivePeerConfigs) {
      IspInfo ispInfo =
          new IspInfo(
              Lists.newArrayList(
                  new InterfaceAddress(
                      bgpActivePeerConfig.getPeerAddress(),
                      ipToInterfaceAddresses
                          .get(bgpActivePeerConfig.getLocalIp())
                          .getNetworkBits())),
              Lists.newArrayList(reverseLocalAndRemote(bgpActivePeerConfig, null)));
      allIspInfos.merge(
          bgpActivePeerConfig.getRemoteAs(),
          ispInfo,
          (ispInfoExisting, ispInfoNew) -> {
            ispInfoExisting.getBgpActivePeerConfigs().addAll(ispInfoNew.getBgpActivePeerConfigs());
            ispInfoExisting.getInterfaceAddresses().addAll(ispInfoNew.getInterfaceAddresses());
            return ispInfoExisting;
          });
    }
  }

  /**
   * Creates and returns the {@link Configuration} for the ISP node given an ASN and {@link IspInfo}
   */
  private static Configuration getIspConfigurationNode(Long asn, IspInfo ispInfo) {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration ispConfiguration =
        cb.setHostname(String.format("isp_%s", asn))
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    ispInfo
        .getInterfaceAddresses()
        .forEach(
            interfaceAddress ->
                nf.interfaceBuilder()
                    .setOwner(ispConfiguration)
                    .setAddress(interfaceAddress)
                    .build());

    // adding bgp process and peers
    BgpProcess bgpProcess = new BgpProcess();
    // using an arbitrary first Ip of an interface as router ID
    bgpProcess.setRouterId(ispInfo.getInterfaceAddresses().get(0).getIp());
    ispConfiguration.getDefaultVrf().setBgpProcess(bgpProcess);

    bgpProcess.setNeighbors(
        ispInfo.getBgpActivePeerConfigs().stream()
            .collect(
                toImmutableSortedMap(
                    bgpActivePeerConfig -> Prefix.create(bgpActivePeerConfig.getPeerAddress(), 32),
                    Function.identity())));
    return ispConfiguration;
  }

  private static boolean isValidBgpPeerConfig(
      @Nonnull BgpActivePeerConfig bgpActivePeerConfig,
      @Nonnull Set<Ip> localIps,
      @Nullable List<Ip> remoteIps,
      @Nullable List<Long> remoteAsns) {
    return Objects.nonNull(bgpActivePeerConfig.getLocalIp())
        && Objects.nonNull(bgpActivePeerConfig.getLocalAs())
        && Objects.nonNull(bgpActivePeerConfig.getPeerAddress())
        && Objects.nonNull(bgpActivePeerConfig.getRemoteAs())
        && !bgpActivePeerConfig.getLocalAs().equals(bgpActivePeerConfig.getRemoteAs())
        && localIps.contains(bgpActivePeerConfig.getLocalIp())
        && (remoteIps == null
            || Sets.newHashSet(remoteIps).contains(bgpActivePeerConfig.getPeerAddress()))
        && (remoteAsns == null
            || Sets.newHashSet(remoteAsns).contains(bgpActivePeerConfig.getRemoteAs()));
  }

  /**
   * Flips the local and remote AS and IP for a given EBGP peer configuration and optionally sets
   * the export policy to the supplied export policy name
   */
  private static BgpActivePeerConfig reverseLocalAndRemote(
      BgpActivePeerConfig bgpActivePeerConfig, @Nullable String exportPolicyName) {
    BgpActivePeerConfig.Builder bgpActivePeerBuilder =
        BgpActivePeerConfig.builder()
            .setPeerAddress(bgpActivePeerConfig.getLocalIp())
            .setRemoteAs(bgpActivePeerConfig.getLocalAs())
            .setLocalIp(bgpActivePeerConfig.getPeerAddress())
            .setLocalAs(bgpActivePeerConfig.getRemoteAs());
    if (exportPolicyName != null) {
      bgpActivePeerBuilder.setExportPolicy(exportPolicyName);
    }
    return bgpActivePeerBuilder.build();
  }
}
