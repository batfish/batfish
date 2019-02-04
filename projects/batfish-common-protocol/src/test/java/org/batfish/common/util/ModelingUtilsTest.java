package org.batfish.common.util;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import org.batfish.common.util.ModelingUtils.IspInfo;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.collections.NodeInterfacePair;
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

    BgpActivePeerConfig reversedPeer =
        ModelingUtils.reverseLocalAndRemote(bgpActivePeerConfig, "routing_policy");
    assertThat(reversedPeer.getPeerAddress(), equalTo(Ip.parse("2.2.2.2")));
    assertThat(reversedPeer.getLocalIp(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(reversedPeer, allOf(hasLocalAs(1L), hasRemoteAs(2L)));
    assertThat(reversedPeer.getExportPolicy(), equalTo("routing_policy"));
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

    assertFalse(ModelingUtils.isValidBgpPeerConfig(invalidPeer, validLocalIps, null, null));
    assertTrue(ModelingUtils.isValidBgpPeerConfig(validPeer, validLocalIps, null, null));
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

    Configuration ispConfiguration = ModelingUtils.getIspConfigurationNode(2L, ispInfo);

    assertThat(
        ispConfiguration,
        allOf(
            hasHostname("Isp_2"),
            hasInterface(
                "~Interface_0~", hasAllAddresses(equalTo(ImmutableSet.of(interfaceAddress)))),
            hasVrf(
                DEFAULT_VRF_NAME,
                hasBgpProcess(
                    hasActiveNeighbor(
                        Prefix.parse("1.1.1.1/32"), allOf(hasRemoteAs(1L), hasLocalAs(2L)))))));

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
  }

  @Test
  public void testPopulateIspInfos() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder();
    Configuration configuration = cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
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

    Map<Long, IspInfo> inputMap = Maps.newHashMap();
    ModelingUtils.populateIspInfos(
        configuration, ImmutableSet.of("interface"), null, null, inputMap);

    assertThat(inputMap, hasKey(1L));

    IspInfo ispInfo = inputMap.get(1L);

    BgpActivePeerConfig reversedPeer =
        BgpActivePeerConfig.builder()
            .setLocalIp(Ip.parse("1.1.1.1"))
            .setLocalAs(1L)
            .setPeerAddress(Ip.parse("2.2.2.2"))
            .setRemoteAs(2L)
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
    Configuration internet = ModelingUtils.createInternetNode();
    InterfaceAddress interfaceAddress =
        new InterfaceAddress(ModelingUtils.INTERNET_OUT_ADDRESS, ModelingUtils.INTERNET_OUT_SUBNET);
    assertThat(
        internet,
        allOf(
            hasHostname(ModelingUtils.INTERNET_HOST_NAME),
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
                                    .setNetwork(Prefix.parse("0.0.0.0/0"))
                                    .setNextHopInterface(ModelingUtils.INTERNET_OUT_INTERFACE)
                                    .setAdministrativeCost(1)
                                    .build()))),
                    hasBgpProcess(hasRouterId(ModelingUtils.INTERNET_OUT_ADDRESS))))));

    assertThat(internet.getRoutingPolicies(), hasKey(ModelingUtils.DEFAULT_ROUTE_ROUTING_POLICY));
    assertThat(
        internet.getRoutingPolicies().get(ModelingUtils.DEFAULT_ROUTE_ROUTING_POLICY),
        equalTo(ModelingUtils.getDefaultRoutingPolicy()));
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
            ImmutableList.of(new NodeInterfacePair("conf", "interface")),
            null,
            null);

    assertThat(internetAndIsps, hasKey(ModelingUtils.INTERNET_HOST_NAME));
    Configuration internetNode = internetAndIsps.get(ModelingUtils.INTERNET_HOST_NAME);

    assertThat(
        internetNode,
        allOf(
            hasHostname(ModelingUtils.INTERNET_HOST_NAME),
            hasInterface(
                "~Interface_0~",
                hasAllAddresses(
                    equalTo(ImmutableSet.of(new InterfaceAddress(Ip.parse("240.1.1.0"), 31))))),
            hasVrf(
                DEFAULT_VRF_NAME,
                hasBgpProcess(
                    hasNeighbors(
                        equalTo(
                            ImmutableMap.of(
                                Prefix.parse("240.1.1.1/32"),
                                BgpActivePeerConfig.builder()
                                    .setPeerAddress(Ip.parse("240.1.1.1"))
                                    .setRemoteAs(1L)
                                    .setLocalIp(Ip.parse("240.1.1.0"))
                                    .setLocalAs(ModelingUtils.INTERNET_AS)
                                    .setExportPolicy(ModelingUtils.DEFAULT_ROUTE_ROUTING_POLICY)
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
                new InterfaceAddress(Ip.parse("240.1.1.1"), 31),
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
                                .setLocalAs(1L)
                                .build(),
                            Prefix.parse("240.1.1.0/32"),
                            BgpActivePeerConfig.builder()
                                .setPeerAddress(Ip.parse("240.1.1.0"))
                                .setRemoteAs(ModelingUtils.INTERNET_AS)
                                .setLocalIp(Ip.parse("240.1.1.1"))
                                .setLocalAs(1L)
                                .build()))))));
  }
}
