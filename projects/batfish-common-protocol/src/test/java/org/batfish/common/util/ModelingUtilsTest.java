package org.batfish.common.util;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceType;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.ModelingUtils.IspInfo;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
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
import org.junit.Test;

/** Tests for {@link ModelingUtils} */
public class ModelingUtilsTest {

  @Test
  public void testReverseLocalAndRemote() {
    BgpActivePeerConfig bgpActivePeerConfig =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();

    BgpActivePeerConfig reversedPeer = ModelingUtils.getBgpPeerOnIsp(bgpActivePeerConfig);
    assertThat(reversedPeer.getPeerAddress(), equalTo(Ip.parse("2.2.2.2")));
    assertThat(reversedPeer.getLocalIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(reversedPeer, allOf(hasLocalAs(1L), hasRemoteAs(2L)));
  }

  @Test
  public void testIsValidBgpPeer() {
    Set<Ip> validLocalIps = ImmutableSet.of(Ip.parse("3.3.3.3"));
    BgpActivePeerConfig invalidPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    BgpActivePeerConfig validPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("3.3.3.3"))
            .setLocalAs(2L)
            .build();

    assertFalse(
        ModelingUtils.isValidBgpPeerConfig(
            invalidPeer, validLocalIps, ImmutableSet.of(), ImmutableSet.of()));
    assertTrue(
        ModelingUtils.isValidBgpPeerConfig(
            validPeer, validLocalIps, ImmutableSet.of(), ImmutableSet.of()));
  }

  @Test
  public void testGetIspConfigurationNode() {
    InterfaceAddress interfaceAddress = new InterfaceAddress(Ip.parse("2.2.2.2"), 30);
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    IspInfo ispInfo = new IspInfo(ImmutableList.of(interfaceAddress), ImmutableList.of(peer));

    Configuration ispConfiguration =
        ModelingUtils.getIspConfigurationNode(
            2L, ispInfo, new NetworkFactory(), new BatfishLogger("output", false));

    assertThat(
        ispConfiguration,
        allOf(
            hasHostname("Isp_2"),
            hasDeviceType(equalTo(DeviceType.ISP)),
            hasInterface(
                "~Interface_0~", hasAllAddresses(equalTo(ImmutableSet.of(interfaceAddress)))),
            hasVrf(
                DEFAULT_VRF_NAME,
                hasBgpProcess(
                    allOf(
                        hasMultipathEbgp(true),
                        hasActiveNeighbor(
                            Prefix.parse("1.1.1.1/32"),
                            allOf(hasRemoteAs(1L), hasLocalAs(2L))))))));

    assertThat(
        ispConfiguration
            .getVrfs()
            .get(DEFAULT_VRF_NAME)
            .getBgpProcess()
            .getActiveNeighbors()
            .values()
            .iterator()
            .next(),
        equalTo(peer));
    assertThat(ispConfiguration.getRoutingPolicies(), hasKey(ModelingUtils.EXPORT_POLICY_ON_ISP));
  }

  @Test
  public void testGetIspConfigurationNodeInvalid() {
    InterfaceAddress interfaceAddress = new InterfaceAddress(Ip.parse("2.2.2.2"), 30);
    InterfaceAddress interfaceAddress2 = new InterfaceAddress(Ip.parse("3.3.3.3"), 30);
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    IspInfo ispInfo =
        new IspInfo(ImmutableList.of(interfaceAddress, interfaceAddress2), ImmutableList.of(peer));
    BatfishLogger logger = new BatfishLogger("debug", false);
    Configuration ispConfiguration =
        ModelingUtils.getIspConfigurationNode(2L, ispInfo, new NetworkFactory(), logger);

    assertThat(ispConfiguration, nullValue());

    assertThat(logger.getHistory(), hasSize(1));
    assertThat(
        logger.getHistory().toString(300), equalTo("ISP information for ASN '2' is not correct"));
  }

  @Test
  public void testPopulateIspInfos() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration = cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration).build();
    nf.vrfBuilder().setName("emptyVRF").setOwner(configuration).build();
    nf.interfaceBuilder()
        .setName("interface")
        .setOwner(configuration)
        .setAddress(new InterfaceAddress(Ip.parse("2.2.2.2"), 24))
        .build();
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.getActiveNeighbors().put(Prefix.parse("1.1.1.1/32"), peer);
    configuration.getDefaultVrf().setBgpProcess(bgpProcess);

    Map<Long, IspInfo> inputMap = Maps.newHashMap();
    ModelingUtils.populateIspInfos(
        configuration,
        ImmutableSet.of("interface"),
        ImmutableList.of(),
        ImmutableList.of(),
        inputMap);

    assertThat(inputMap, hasKey(1L));

    IspInfo ispInfo = inputMap.get(1L);

    BgpActivePeerConfig reversedPeer =
        BgpActivePeerConfig.builder()
            .setLocalIp(Ip.parse("1.1.1.1"))
            .setLocalAs(1L)
            .setPeerAddress(Ip.parse("2.2.2.2"))
            .setRemoteAs(2L)
            .setExportPolicy(ModelingUtils.EXPORT_POLICY_ON_ISP)
            .build();
    assertThat(ispInfo.getBgpActivePeerConfigs(), equalTo(ImmutableList.of(reversedPeer)));
    assertThat(
        ispInfo.getInterfaceAddresses(),
        equalTo(ImmutableList.of(new InterfaceAddress(Ip.parse("1.1.1.1"), 24))));
  }

  @Test
  public void testGetAsnOfIspNode() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    Configuration ispConfiguration =
        cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(ispConfiguration).build();
    nf.interfaceBuilder()
        .setName("interface")
        .setOwner(ispConfiguration)
        .setAddress(new InterfaceAddress(Ip.parse("2.2.2.2"), 24))
        .build();
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.getActiveNeighbors().put(Prefix.parse("1.1.1.1/32"), peer);
    ispConfiguration.getDefaultVrf().setBgpProcess(bgpProcess);

    assertThat(ModelingUtils.getAsnOfIspNode(ispConfiguration), equalTo(2L));
  }

  @Test
  public void testCreateInternetNode() {
    Configuration internet = ModelingUtils.createInternetNode(new NetworkFactory());
    InterfaceAddress interfaceAddress =
        new InterfaceAddress(ModelingUtils.INTERNET_OUT_ADDRESS, ModelingUtils.INTERNET_OUT_SUBNET);
    assertThat(
        internet,
        allOf(
            hasHostname(ModelingUtils.INTERNET_HOST_NAME),
            hasDeviceType(equalTo(DeviceType.INTERNET)),
            hasInterface(
                ModelingUtils.INTERNET_OUT_INTERFACE,
                hasAllAddresses(equalTo(ImmutableSet.of(interfaceAddress)))),
            hasVrf(
                DEFAULT_VRF_NAME,
                allOf(
                    hasStaticRoutes(
                        equalTo(
                            ImmutableSortedSet.of(
                                StaticRoute.builder()
                                    .setNetwork(Prefix.ZERO)
                                    .setNextHopInterface(ModelingUtils.INTERNET_OUT_INTERFACE)
                                    .setAdministrativeCost(1)
                                    .build()))),
                    hasBgpProcess(
                        allOf(
                            hasRouterId(ModelingUtils.INTERNET_OUT_ADDRESS),
                            hasMultipathEbgp(true)))))));

    assertThat(internet.getRoutingPolicies(), hasKey(ModelingUtils.EXPORT_POLICY_ON_INTERNET));
  }

  @Test
  public void testGetInternetAndIspsCaseInsensitive() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration =
        cb.setHostname("conf").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration).build();
    nf.interfaceBuilder()
        .setName("Interface")
        .setOwner(configuration)
        .setAddress(new InterfaceAddress(Ip.parse("2.2.2.2"), 24))
        .build();
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.getActiveNeighbors().put(Prefix.parse("1.1.1.1/32"), peer);
    configuration.getDefaultVrf().setBgpProcess(bgpProcess);

    Map<String, Configuration> internetAndIsps =
        ModelingUtils.getInternetAndIspNodes(
            ImmutableMap.of(configuration.getHostname(), configuration),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(new NodeInterfacePair("CoNf", "InTeRfAcE"))),
                IspFilter.ALLOW_ALL),
            new BatfishLogger("output", false));

    // Isp and Internet nodes should be created irrespective of case used in Isp configuration
    assertThat(internetAndIsps, hasKey("Isp_1"));
    assertThat(internetAndIsps, hasKey("Internet"));
  }

  @Test
  public void testGetInternetAndIspNodes() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration =
        cb.setHostname("conf").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration).build();
    nf.interfaceBuilder()
        .setName("interface")
        .setOwner(configuration)
        .setAddress(new InterfaceAddress(Ip.parse("2.2.2.2"), 24))
        .build();
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .build();
    BgpProcess bgpProcess = new BgpProcess();
    bgpProcess.getActiveNeighbors().put(Prefix.parse("1.1.1.1/32"), peer);
    configuration.getDefaultVrf().setBgpProcess(bgpProcess);

    Map<String, Configuration> internetAndIsps =
        ModelingUtils.getInternetAndIspNodes(
            ImmutableMap.of(configuration.getHostname(), configuration),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(new NodeInterfacePair("conf", "interface"))),
                IspFilter.ALLOW_ALL),
            new BatfishLogger("output", false));

    assertThat(internetAndIsps, hasKey(ModelingUtils.INTERNET_HOST_NAME));
    Configuration internetNode = internetAndIsps.get(ModelingUtils.INTERNET_HOST_NAME);

    assertThat(
        internetNode,
        allOf(
            hasHostname(ModelingUtils.INTERNET_HOST_NAME),
            hasInterface(
                "~Interface_1~",
                hasAllAddresses(
                    equalTo(ImmutableSet.of(new InterfaceAddress(Ip.parse("240.1.1.2"), 31))))),
            hasVrf(
                DEFAULT_VRF_NAME,
                hasBgpProcess(
                    hasNeighbors(
                        equalTo(
                            ImmutableMap.of(
                                Prefix.parse("240.1.1.3/32"),
                                BgpActivePeerConfig.builder()
                                    .setPeerAddress(Ip.parse("240.1.1.3"))
                                    .setRemoteAs(1L)
                                    .setLocalIp(Ip.parse("240.1.1.2"))
                                    .setLocalAs(ModelingUtils.INTERNET_AS)
                                    .setExportPolicy(ModelingUtils.EXPORT_POLICY_ON_INTERNET)
                                    .build())))))));

    assertThat(internetAndIsps, hasKey("Isp_1"));
    Configuration ispNode = internetAndIsps.get("Isp_1");

    ImmutableSet<InterfaceAddress> interfaceAddresses =
        ispNode.getAllInterfaces().values().stream()
            .flatMap(iface -> iface.getAllAddresses().stream())
            .collect(ImmutableSet.toImmutableSet());
    assertThat(
        interfaceAddresses,
        equalTo(
            ImmutableSet.of(
                new InterfaceAddress(Ip.parse("240.1.1.3"), 31),
                new InterfaceAddress(Ip.parse("1.1.1.1"), 24))));

    assertThat(
        ispNode,
        hasVrf(
            DEFAULT_VRF_NAME,
            hasBgpProcess(
                hasNeighbors(
                    equalTo(
                        ImmutableMap.of(
                            Prefix.parse("2.2.2.2/32"),
                            BgpActivePeerConfig.builder()
                                .setPeerAddress(Ip.parse("2.2.2.2"))
                                .setRemoteAs(2L)
                                .setLocalIp(Ip.parse("1.1.1.1"))
                                .setExportPolicy(ModelingUtils.EXPORT_POLICY_ON_ISP)
                                .setLocalAs(1L)
                                .build(),
                            Prefix.parse("240.1.1.2/32"),
                            BgpActivePeerConfig.builder()
                                .setPeerAddress(Ip.parse("240.1.1.2"))
                                .setRemoteAs(ModelingUtils.INTERNET_AS)
                                .setLocalIp(Ip.parse("240.1.1.3"))
                                .setExportPolicy(ModelingUtils.EXPORT_POLICY_ON_ISP)
                                .setLocalAs(1L)
                                .build()))))));
  }

  @Test
  public void testGetRoutingPolicyForInternet() {
    NetworkFactory nf = new NetworkFactory();
    Configuration internet =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("fakeInternet")
            .build();
    RoutingPolicy internetRoutingPolicy = ModelingUtils.getRoutingPolicyForInternet(internet, nf);

    PrefixSpace prefixSpace = new PrefixSpace();
    prefixSpace.addPrefix(Prefix.ZERO);
    RoutingPolicy expectedRoutingPolicy =
        nf.routingPolicyBuilder()
            .setName(ModelingUtils.EXPORT_POLICY_ON_INTERNET)
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
    assertThat(internetRoutingPolicy, equalTo(expectedRoutingPolicy));
  }

  @Test
  public void testGetRoutingPolicyForIsp() {
    NetworkFactory nf = new NetworkFactory();
    Configuration isp =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("fakeIsp")
            .build();
    RoutingPolicy ispRoutingPolicy = ModelingUtils.getRoutingPolicyForIsp(isp, nf);

    RoutingPolicy expectedRoutingPolicy =
        nf.routingPolicyBuilder()
            .setName(ModelingUtils.EXPORT_POLICY_ON_ISP)
            .setOwner(isp)
            .setStatements(
                Collections.singletonList(
                    new If(
                        new Conjunction(ImmutableList.of(new MatchProtocol(RoutingProtocol.BGP))),
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))))
            .build();
    assertThat(ispRoutingPolicy, equalTo(expectedRoutingPolicy));
  }

  @Test
  public void testInterfaceNamesIsp() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration1 =
        cb.setHostname("conf1").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration1).build();
    nf.interfaceBuilder()
        .setName("interface1")
        .setOwner(configuration1)
        .setAddress(new InterfaceAddress(Ip.parse("1.1.1.1"), 24))
        .build();
    Vrf vrfConf1 = nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration1).build();
    BgpProcess bgpProcess1 = nf.bgpProcessBuilder().setVrf(vrfConf1).build();
    BgpActivePeerConfig.builder()
        .setBgpProcess(bgpProcess1)
        .setPeerAddress(Ip.parse("1.1.1.2"))
        .setRemoteAs(1234L)
        .setLocalIp(Ip.parse("1.1.1.1"))
        .setLocalAs(1L)
        .build();

    Configuration configuration2 =
        cb.setHostname("conf2").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration2).build();
    nf.interfaceBuilder()
        .setName("interface2")
        .setOwner(configuration2)
        .setAddress(new InterfaceAddress(Ip.parse("2.2.2.2"), 24))
        .build();
    Vrf vrfConf2 = nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration2).build();
    BgpProcess bgpProcess2 = nf.bgpProcessBuilder().setVrf(vrfConf2).build();
    BgpActivePeerConfig.builder()
        .setBgpProcess(bgpProcess2)
        .setPeerAddress(Ip.parse("2.2.2.3"))
        .setRemoteAs(1234L)
        .setLocalIp(Ip.parse("2.2.2.2"))
        .setLocalAs(1L)
        .build();

    Map<String, Configuration> internetAndIsps =
        ModelingUtils.getInternetAndIspNodes(
            ImmutableMap.of(
                configuration1.getHostname(),
                configuration1,
                configuration2.getHostname(),
                configuration2),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(new NodeInterfacePair("conf1", "interface1")),
                    new BorderInterfaceInfo(new NodeInterfacePair("conf2", "interface2"))),
                IspFilter.ALLOW_ALL),
            new BatfishLogger("output", false));

    assertThat(internetAndIsps, hasKey("Isp_1234"));

    Configuration isp = internetAndIsps.get("Isp_1234");
    // two interfaces for peering with the two configurations and one interface for peering with
    // internet
    assertThat(isp.getAllInterfaces().entrySet(), hasSize(3));
    assertThat(
        isp.getAllInterfaces().keySet(),
        equalTo(ImmutableSet.of("~Interface_0~", "~Interface_1~", "~Interface_3~")));
  }

  @Test
  public void testNoIsps() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration1 =
        cb.setHostname("conf1").setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration1).build();
    nf.interfaceBuilder()
        .setName("interface1")
        .setOwner(configuration1)
        .setAddress(new InterfaceAddress(Ip.parse("1.1.1.1"), 24))
        .build();
    Vrf vrfConf1 = nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(configuration1).build();
    BgpProcess bgpProcess1 = nf.bgpProcessBuilder().setVrf(vrfConf1).build();
    BgpActivePeerConfig.builder()
        .setBgpProcess(bgpProcess1)
        .setPeerAddress(Ip.parse("1.1.1.2"))
        .setRemoteAs(1234L)
        .setLocalIp(Ip.parse("1.1.1.1"))
        .setLocalAs(1L)
        .build();

    // passing non-existent border interfaces
    Map<String, Configuration> internetAndIsps =
        ModelingUtils.getInternetAndIspNodes(
            ImmutableMap.of(configuration1.getHostname(), configuration1),
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(new NodeInterfacePair("conf2", "interface2")),
                    new BorderInterfaceInfo(new NodeInterfacePair("conf2", "interface2"))),
                IspFilter.ALLOW_ALL),
            new BatfishLogger("output", false));

    // no ISPs and no Internet
    assertThat(internetAndIsps, anEmptyMap());
  }
}
