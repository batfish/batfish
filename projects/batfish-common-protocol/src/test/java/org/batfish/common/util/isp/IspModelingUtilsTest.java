package org.batfish.common.util.isp;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlag;
import static org.batfish.common.util.isp.BlockReservedAddressesAtInternet.FROM_INTERNET_ACL_NAME;
import static org.batfish.common.util.isp.BlockReservedAddressesAtInternet.TO_INTERNET_ACL_NAME;
import static org.batfish.common.util.isp.IspModelingUtils.EXPORT_POLICY_ON_ISP_TO_CUSTOMERS;
import static org.batfish.common.util.isp.IspModelingUtils.EXPORT_POLICY_ON_ISP_TO_INTERNET;
import static org.batfish.common.util.isp.IspModelingUtils.HIGH_ADMINISTRATIVE_COST;
import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_HOST_NAME;
import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_NULL_ROUTED_PREFIXES;
import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_OUT_INTERFACE;
import static org.batfish.common.util.isp.IspModelingUtils.INTERNET_OUT_INTERFACE_LINK_LOCATION_INFO;
import static org.batfish.common.util.isp.IspModelingUtils.ISP_TO_INTERNET_INTERFACE_NAME;
import static org.batfish.common.util.isp.IspModelingUtils.LINK_LOCAL_ADDRESS;
import static org.batfish.common.util.isp.IspModelingUtils.LINK_LOCAL_IP;
import static org.batfish.common.util.isp.IspModelingUtils.addBgpPeerToIsp;
import static org.batfish.common.util.isp.IspModelingUtils.createInternetNode;
import static org.batfish.common.util.isp.IspModelingUtils.createIspNode;
import static org.batfish.common.util.isp.IspModelingUtils.getAdvertiseBgpStatement;
import static org.batfish.common.util.isp.IspModelingUtils.getAdvertiseStaticStatement;
import static org.batfish.common.util.isp.IspModelingUtils.getAsnOfIspNode;
import static org.batfish.common.util.isp.IspModelingUtils.getDefaultIspNodeName;
import static org.batfish.common.util.isp.IspModelingUtils.getInternetAndIspNodes;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyForIspToCustomers;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyForIspToInternet;
import static org.batfish.common.util.isp.IspModelingUtils.internetToIspInterfaceName;
import static org.batfish.common.util.isp.IspModelingUtils.ispNameConflicts;
import static org.batfish.common.util.isp.IspModelingUtils.ispToRemoteInterfaceName;
import static org.batfish.common.util.isp.IspModelingUtils.makeBgpProcess;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.BgpProcess.testBgpProcess;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasInterfaceNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasNeighbors;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDeviceType;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.util.isp.IspModel.Remote;
import org.batfish.common.util.isp.IspModelingUtils.ModeledNodes;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
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
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.specifier.InterfaceLinkLocation;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IspModelingUtils} */
public class IspModelingUtilsTest {

  private static final long _ispAsn = 1L;
  private static final long _snapshotAsn = 2L;
  private static final Ip _ispIp = Ip.parse("1.1.1.1");
  private static final Ip _snapshotIp = Ip.parse("2.2.2.2");
  private static final String _ispName = getDefaultIspNodeName(_ispAsn);
  private static final String _snapshotHostname = "conf";
  private static final String _snapshotInterfaceName = "interface";

  private NetworkFactory _nf;
  private Configuration _snapshotHost;
  private BgpActivePeerConfig _snapshotActivePeer;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _snapshotHost = createBgpNode(_snapshotHostname, _snapshotInterfaceName, _snapshotIp);
    _snapshotActivePeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(_ispIp)
            .setRemoteAs(_ispAsn)
            .setLocalIp(_snapshotIp)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess())
            .build();
  }

  private Configuration createBgpNode(String hostname, String bgpIfaceName, Ip bgpInterfaceIp) {
    Configuration c =
        _nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    _nf.vrfBuilder().setName(DEFAULT_VRF_NAME).setOwner(c).build();
    _nf.interfaceBuilder()
        .setName(bgpIfaceName)
        .setOwner(c)
        .setAddress(ConcreteInterfaceAddress.create(bgpInterfaceIp, 24))
        .build();
    makeBgpProcess(bgpInterfaceIp, c.getDefaultVrf());
    return c;
  }

  @Test
  public void testNonExistentNode() {
    Warnings warnings = new Warnings(true, true, true);
    IspModelingUtils.combineIspConfigurations(
        ImmutableMap.of(_snapshotHostname, _snapshotHost),
        ImmutableList.of(
            new IspConfiguration(
                ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("conf1", "init1"))),
                IspFilter.ALLOW_ALL)),
        warnings);

    assertThat(
        warnings,
        hasRedFlag(
            hasText(
                "ISP Modeling: Non-existent border node conf1 specified in ISP configuration")));
  }

  @Test
  public void testNonExistentInterface() {
    Warnings warnings = new Warnings(true, true, true);

    IspModelingUtils.populateIspModels(
        _snapshotHost,
        ImmutableSet.of("init"),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(),
        Maps.newHashMap(),
        warnings);

    assertThat(
        warnings, hasRedFlag(hasText("ISP Modeling: Cannot find interface init on node conf")));
  }

  @Test
  public void testAddBgpPeerToIsp() {
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(_snapshotActivePeer, "iface", bgpProcess);
    BgpActivePeerConfig reversedPeer = getOnlyElement(bgpProcess.getActiveNeighbors().values());

    assertThat(
        reversedPeer,
        equalTo(
            BgpActivePeerConfig.builder()
                .setLocalIp(_ispIp)
                .setPeerAddress(_snapshotIp)
                .setLocalAs(_ispAsn)
                .setRemoteAs(_snapshotAsn)
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                        .build())
                .build()));
  }

  @Test
  public void testAddBgpPeerToIsp_preferConfederationAs() {
    BgpActivePeerConfig remotePeerConfig =
        BgpActivePeerConfig.builder()
            .setPeerAddress(_ispIp)
            .setRemoteAs(_ispAsn)
            .setLocalIp(_snapshotIp)
            .setLocalAs(_snapshotAsn)
            .setConfederation(1000L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(remotePeerConfig, "iface", bgpProcess);
    BgpActivePeerConfig reversedPeer = getOnlyElement(bgpProcess.getActiveNeighbors().values());

    assertThat(
        reversedPeer,
        equalTo(
            BgpActivePeerConfig.builder()
                .setLocalIp(_ispIp)
                .setPeerAddress(_snapshotIp)
                .setLocalAs(_ispAsn)
                .setRemoteAs(1000L)
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                        .build())
                .build()));
  }

  @Test
  public void testAddBgpPeerToIsp_Unnumbered() {
    BgpUnnumberedPeerConfig remotePeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface(_snapshotInterfaceName)
            .setRemoteAs(_ispAsn)
            .setLocalIp(LINK_LOCAL_IP)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(remotePeerConfig, "iface", bgpProcess);
    BgpUnnumberedPeerConfig reversedPeer =
        getOnlyElement(bgpProcess.getInterfaceNeighbors().values());

    assertThat(
        reversedPeer,
        equalTo(
            BgpUnnumberedPeerConfig.builder()
                .setLocalIp(LINK_LOCAL_IP)
                .setPeerInterface("iface")
                .setLocalAs(_ispAsn)
                .setRemoteAs(_snapshotAsn)
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                        .build())
                .build()));
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
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();
    BgpActivePeerConfig validPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("1.1.1.1"))
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("3.3.3.3"))
            .setLocalAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    assertFalse(
        IspModelingUtils.isValidBgpPeerConfig(
            invalidPeer, validLocalIps, ImmutableSet.of(), ALL_AS_NUMBERS));
    assertTrue(
        IspModelingUtils.isValidBgpPeerConfig(
            validPeer, validLocalIps, ImmutableSet.of(), ALL_AS_NUMBERS));
  }

  @Test
  public void testIsValidBgpPeerInterfaceNeighbor() {
    Set<Ip> validLocalIps = ImmutableSet.of(LINK_LOCAL_IP);
    BgpUnnumberedPeerConfig invalidPeer =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface("iface")
            .setRemoteAs(1L)
            .setLocalIp(Ip.parse("2.2.2.2"))
            .setLocalAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();
    BgpUnnumberedPeerConfig validPeer =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface("iface")
            .setRemoteAs(1L)
            .setLocalIp(LINK_LOCAL_IP)
            .setLocalAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();

    assertFalse(
        IspModelingUtils.isValidBgpPeerConfig(
            invalidPeer, validLocalIps, ImmutableSet.of(), ALL_AS_NUMBERS));
    assertTrue(
        IspModelingUtils.isValidBgpPeerConfig(
            validPeer, validLocalIps, ImmutableSet.of(), ALL_AS_NUMBERS));
  }

  @Test
  public void testCreateIspNode() {
    InterfaceAddress ispIfaceAddress = ConcreteInterfaceAddress.create(_snapshotIp, 24);
    IspModel ispModel =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setName(_ispName)
            .setRemotes(
                new Remote(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    ispIfaceAddress,
                    _snapshotActivePeer))
            .setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet())
            .build();

    ModeledNodes modeledNodes = new ModeledNodes();
    createIspNode(modeledNodes, ispModel, new NetworkFactory(), new BatfishLogger("output", false));
    Configuration ispConfiguration = modeledNodes.getConfigurations().get(_ispName);

    assertThat(
        ispConfiguration,
        allOf(
            hasHostname(_ispName),
            hasDeviceType(equalTo(DeviceType.ISP)),
            hasInterface(
                ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                hasAllAddresses(equalTo(ImmutableSet.of(ispIfaceAddress)))),
            hasIpAccessList(FROM_INTERNET_ACL_NAME, any(IpAccessList.class)),
            hasIpAccessList(TO_INTERNET_ACL_NAME, any(IpAccessList.class)),
            hasInterface(ISP_TO_INTERNET_INTERFACE_NAME, any(Interface.class)),
            hasVrf(DEFAULT_VRF_NAME, hasBgpProcess(allOf(hasMultipathEbgp(true))))));
    Interface toInternet = ispConfiguration.getAllInterfaces().get(ISP_TO_INTERNET_INTERFACE_NAME);
    assertThat(toInternet.getIncomingFilter(), notNullValue());
    assertThat(toInternet.getIncomingFilter().getName(), equalTo(FROM_INTERNET_ACL_NAME));
    assertThat(toInternet.getOutgoingFilter(), notNullValue());
    assertThat(toInternet.getOutgoingFilter().getName(), equalTo(TO_INTERNET_ACL_NAME));

    // compute the reverse config
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(_snapshotActivePeer, "iface", bgpProcess);
    BgpActivePeerConfig expectedIspPeerConfig =
        getOnlyElement(bgpProcess.getActiveNeighbors().values());

    assertThat(
        getOnlyElement(
            ispConfiguration
                .getVrfs()
                .get(DEFAULT_VRF_NAME)
                .getBgpProcess()
                .getActiveNeighbors()
                .values()),
        equalTo(expectedIspPeerConfig));

    assertThat(ispConfiguration.getRoutingPolicies(), hasKey(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS));

    assertThat(
        modeledNodes.getLayer1Edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    _ispName,
                    ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName)),
                new Layer1Edge(
                    _ispName,
                    ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                    _snapshotHostname,
                    _snapshotInterfaceName))));
  }

  @Test
  public void testCreateIspNode_unnumbered() {
    BgpUnnumberedPeerConfig remotePeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface(_snapshotInterfaceName)
            .setRemoteAs(_ispAsn)
            .setLocalIp(LINK_LOCAL_IP)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();
    IspModel ispModel =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setRemotes(
                new Remote(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    LINK_LOCAL_ADDRESS,
                    remotePeerConfig))
            .build();

    ModeledNodes modeledNodes = new ModeledNodes();
    createIspNode(modeledNodes, ispModel, new NetworkFactory(), new BatfishLogger("output", false));
    Configuration ispConfiguration = modeledNodes.getConfigurations().get(_ispName);

    assertThat(
        ispConfiguration,
        allOf(
            hasHostname(_ispName),
            hasDeviceType(equalTo(DeviceType.ISP)),
            hasInterface(
                ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                hasAllAddresses(equalTo(ImmutableSet.of(LINK_LOCAL_ADDRESS)))),
            hasVrf(DEFAULT_VRF_NAME, hasBgpProcess(hasMultipathEbgp(true)))));

    // compute the reverse config
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(
        remotePeerConfig,
        ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
        bgpProcess);
    BgpUnnumberedPeerConfig expectedIspPeerConfig =
        getOnlyElement(bgpProcess.getInterfaceNeighbors().values());

    assertThat(
        getOnlyElement(
            ispConfiguration
                .getVrfs()
                .get(DEFAULT_VRF_NAME)
                .getBgpProcess()
                .getInterfaceNeighbors()
                .values()),
        equalTo(expectedIspPeerConfig));

    assertThat(ispConfiguration.getRoutingPolicies(), hasKey(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS));

    assertThat(
        modeledNodes.getLayer1Edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    _ispName,
                    ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName)),
                new Layer1Edge(
                    _ispName,
                    ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                    _snapshotHostname,
                    _snapshotInterfaceName))));
  }

  @Test
  public void testCreateIspNode_invalid() {
    IspModel ispInfo = IspModel.builder().setAsn(_ispAsn).build();
    BatfishLogger logger = new BatfishLogger("debug", false);
    ModeledNodes modeledNodes = new ModeledNodes();
    createIspNode(modeledNodes, ispInfo, new NetworkFactory(), logger);
    Configuration ispConfiguration = modeledNodes.getConfigurations().get(ispInfo.getName());

    assertThat(ispConfiguration, nullValue());

    assertThat(logger.getHistory(), hasSize(1));
    assertThat(
        logger.getHistory().toString(300), equalTo("ISP information for ASN '1' is not correct"));
  }

  /** Test that null static routes are created for additional announcements to the Internet */
  @Test
  public void testCreateIspNode_additionalAnnouncements() {
    Set<Prefix> additionalPrefixes =
        ImmutableSet.of(Prefix.parse("10.1.1.1/32"), Prefix.parse("20.2.2.2/32"));
    IspModel ispInfo =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setRemotes(
                new Remote(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    ConcreteInterfaceAddress.create(_snapshotIp, 30),
                    _snapshotActivePeer))
            .setAdditionalPrefixesToInternet(additionalPrefixes)
            .build();
    ModeledNodes modeledNodes = new ModeledNodes();
    createIspNode(modeledNodes, ispInfo, new NetworkFactory(), new BatfishLogger("debug", false));
    Configuration ispConfiguration = modeledNodes.getConfigurations().get(_ispName);

    assertThat(
        ispConfiguration.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.copyOf(
                ispInfo.getAdditionalPrefixesToInternet().stream()
                    .map(
                        prefix ->
                            StaticRoute.testBuilder()
                                .setNetwork(prefix)
                                .setNextHopInterface(NULL_INTERFACE_NAME)
                                .setAdministrativeCost(HIGH_ADMINISTRATIVE_COST)
                                .build())
                    .collect(ImmutableSet.toImmutableSet()))));
  }

  @Test
  public void testPopulateIspModels() {
    Map<Long, IspModel> inputMap = Maps.newHashMap();
    IspModelingUtils.populateIspModels(
        _snapshotHost,
        ImmutableSet.of(_snapshotInterfaceName),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(),
        inputMap,
        new Warnings());

    assertThat(inputMap, hasKey(_ispAsn));

    IspModel ispInfo = inputMap.get(_ispAsn);

    assertThat(
        ispInfo,
        equalTo(
            IspModel.builder()
                .setAsn(_ispAsn)
                .setName(null)
                .setRemotes(
                    new Remote(
                        _snapshotHostname,
                        getOnlyElement(_snapshotHost.getAllInterfaces().keySet()),
                        ConcreteInterfaceAddress.create(_ispIp, 24),
                        getOnlyElement(
                            _snapshotHost
                                .getDefaultVrf()
                                .getBgpProcess()
                                .getActiveNeighbors()
                                .values())))
                .setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet())
                .build()));
    assertThat(ispInfo.getHostname(), equalTo(_ispName));
  }

  @Test
  public void testPopulateIspModels_bgpUnnumbered() {
    Configuration configuration = createBgpNode("conf", _snapshotInterfaceName, _snapshotIp);
    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(_snapshotInterfaceName)
        .setRemoteAs(_ispAsn)
        .setLocalIp(_snapshotIp)
        .setLocalAs(_snapshotAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .setBgpProcess(configuration.getDefaultVrf().getBgpProcess())
        .build();

    Map<Long, IspModel> inputMap = Maps.newHashMap();

    IspModelingUtils.populateIspModels(
        configuration,
        ImmutableSet.of(_snapshotInterfaceName),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(),
        inputMap,
        new Warnings());

    assertThat(inputMap, hasKey(_ispAsn));

    IspModel ispInfo = inputMap.get(_ispAsn);

    assertThat(
        ispInfo,
        equalTo(
            IspModel.builder()
                .setAsn(_ispAsn)
                .setName(null)
                .setRemotes(
                    new Remote(
                        configuration.getHostname(),
                        getOnlyElement(configuration.getAllInterfaces().keySet()),
                        LINK_LOCAL_ADDRESS,
                        getOnlyElement(
                            configuration
                                .getDefaultVrf()
                                .getBgpProcess()
                                .getInterfaceNeighbors()
                                .values())))
                .setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet())
                .build()));
    assertThat(ispInfo.getHostname(), equalTo(_ispName));
  }

  @Test
  public void testPopulateIspModels_customIspName() {
    Map<Long, IspModel> inputMap = Maps.newHashMap();
    IspModelingUtils.populateIspModels(
        _snapshotHost,
        ImmutableSet.of(_snapshotInterfaceName),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(new IspNodeInfo(_ispAsn, "myisp")),
        inputMap,
        new Warnings());

    assertThat(inputMap, hasKey(_ispAsn));

    IspModel ispInfo = inputMap.get(_ispAsn);

    assertThat(ispInfo.getName(), equalTo("myisp"));
  }

  @Test
  public void testPopulateIspModels_mergeAdditionalPrefixes() {
    Map<Long, IspModel> inputMap = Maps.newHashMap();
    IspModelingUtils.populateIspModels(
        _snapshotHost,
        ImmutableSet.of(_snapshotInterfaceName),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(
            new IspNodeInfo(
                _ispAsn,
                "myisp",
                ImmutableList.of(
                    new IspAnnouncement(Prefix.parse("1.1.1.1/32")),
                    new IspAnnouncement(Prefix.parse("2.2.2.2/32")))),
            new IspNodeInfo(
                _ispAsn,
                "myisp",
                ImmutableList.of(
                    new IspAnnouncement(Prefix.parse("3.3.3.3/32")),
                    new IspAnnouncement(Prefix.parse("2.2.2.2/32"))))),
        inputMap,
        new Warnings());

    assertThat(
        inputMap.get(_ispAsn).getAdditionalPrefixesToInternet(),
        equalTo(
            ImmutableSet.of(
                Prefix.parse("1.1.1.1/32"),
                Prefix.parse("2.2.2.2/32"),
                Prefix.parse("3.3.3.3/32"))));
  }

  @Test
  public void testGetAsnOfIspNode() {
    assertThat(getAsnOfIspNode(_snapshotHost), equalTo(2L));
  }

  @Test
  public void testCreateInternetNode() {
    ModeledNodes modeledNodes = new ModeledNodes();
    createInternetNode(modeledNodes);
    Configuration internet = modeledNodes.getConfigurations().get(INTERNET_HOST_NAME);
    InterfaceAddress interfaceAddress =
        ConcreteInterfaceAddress.create(
            IspModelingUtils.INTERNET_OUT_ADDRESS,
            IspModelingUtils.INTERNET_OUT_SUBNET.getPrefixLength());
    assertThat(
        internet,
        allOf(
            hasHostname(IspModelingUtils.INTERNET_HOST_NAME),
            hasDeviceType(equalTo(DeviceType.INTERNET)),
            hasInterface(
                IspModelingUtils.INTERNET_OUT_INTERFACE,
                hasAllAddresses(equalTo(ImmutableSet.of(interfaceAddress)))),
            hasVrf(
                DEFAULT_VRF_NAME,
                allOf(
                    hasStaticRoutes(
                        equalTo(
                            new ImmutableSortedSet.Builder<StaticRoute>(Comparator.naturalOrder())
                                .add(
                                    StaticRoute.testBuilder()
                                        .setNetwork(Prefix.ZERO)
                                        .setNextHopInterface(
                                            IspModelingUtils.INTERNET_OUT_INTERFACE)
                                        .setAdministrativeCost(1)
                                        .build())
                                .addAll(
                                    INTERNET_NULL_ROUTED_PREFIXES.stream()
                                        .map(
                                            prefix ->
                                                StaticRoute.testBuilder()
                                                    .setNetwork(prefix)
                                                    .setNextHopInterface(NULL_INTERFACE_NAME)
                                                    .setAdministrativeCost(1)
                                                    .build())
                                        .collect(ImmutableSet.toImmutableSet()))
                                .build())),
                    hasBgpProcess(
                        allOf(
                            hasRouterId(IspModelingUtils.INTERNET_OUT_ADDRESS),
                            hasMultipathEbgp(true)))))));

    assertThat(internet.getRoutingPolicies(), hasKey(IspModelingUtils.EXPORT_POLICY_ON_INTERNET));

    assertThat(
        internet.getLocationInfo(),
        hasEntry(
            new InterfaceLinkLocation(INTERNET_HOST_NAME, INTERNET_OUT_INTERFACE),
            INTERNET_OUT_INTERFACE_LINK_LOCATION_INFO));
  }

  @Test
  public void testGetInternetAndIspNodes_caseInsensitive() {
    Map<String, Configuration> internetAndIsps =
        getInternetAndIspNodes(
                ImmutableMap.of(_snapshotHostname, _snapshotHost),
                ImmutableList.of(
                    new IspConfiguration(
                        ImmutableList.of(
                            new BorderInterfaceInfo(NodeInterfacePair.of("CoNf", "InTeRfAcE"))),
                        IspFilter.ALLOW_ALL)),
                new BatfishLogger("output", false),
                new Warnings())
            .getConfigurations();

    // Isp and Internet nodes should be created irrespective of case used in Isp configuration
    assertThat(internetAndIsps, hasKey(_ispName));
    assertThat(internetAndIsps, hasKey(INTERNET_HOST_NAME));
  }

  @Test
  public void testGetInternetAndIspNodes() {
    ModeledNodes modeledNodes =
        getInternetAndIspNodes(
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName))),
                    IspFilter.ALLOW_ALL)),
            new BatfishLogger("output", false),
            new Warnings());
    Map<String, Configuration> configurations = modeledNodes.getConfigurations();

    assertThat(configurations, hasKey(IspModelingUtils.INTERNET_HOST_NAME));
    Configuration internetNode = configurations.get(IspModelingUtils.INTERNET_HOST_NAME);

    assertThat(
        internetNode,
        allOf(
            hasHostname(IspModelingUtils.INTERNET_HOST_NAME),
            hasInterface(
                internetToIspInterfaceName(_ispName),
                hasAllAddresses(equalTo(ImmutableSet.of(LINK_LOCAL_ADDRESS)))),
            hasVrf(
                DEFAULT_VRF_NAME,
                hasBgpProcess(
                    hasInterfaceNeighbors(
                        equalTo(
                            ImmutableMap.of(
                                internetToIspInterfaceName(_ispName),
                                BgpUnnumberedPeerConfig.builder()
                                    .setPeerInterface(internetToIspInterfaceName(_ispName))
                                    .setRemoteAs(_ispAsn)
                                    .setLocalIp(LINK_LOCAL_IP)
                                    .setLocalAs(IspModelingUtils.INTERNET_AS)
                                    .setIpv4UnicastAddressFamily(
                                        Ipv4UnicastAddressFamily.builder()
                                            .setExportPolicy(
                                                IspModelingUtils.EXPORT_POLICY_ON_INTERNET)
                                            .build())
                                    .build())))))));

    assertThat(configurations, hasKey(_ispName));
    Configuration ispNode = configurations.get(_ispName);

    ImmutableSet<InterfaceAddress> interfaceAddresses =
        ispNode.getAllInterfaces().values().stream()
            .flatMap(iface -> iface.getAllAddresses().stream())
            .collect(ImmutableSet.toImmutableSet());
    assertThat(
        interfaceAddresses,
        equalTo(ImmutableSet.of(LINK_LOCAL_ADDRESS, ConcreteInterfaceAddress.create(_ispIp, 24))));

    assertThat(
        ispNode,
        hasVrf(
            DEFAULT_VRF_NAME,
            hasBgpProcess(
                allOf(
                    hasNeighbors(
                        equalTo(
                            ImmutableMap.of(
                                _snapshotIp,
                                BgpActivePeerConfig.builder()
                                    .setPeerAddress(_snapshotIp)
                                    .setRemoteAs(_snapshotAsn)
                                    .setLocalIp(_ispIp)
                                    .setLocalAs(_ispAsn)
                                    .setIpv4UnicastAddressFamily(
                                        Ipv4UnicastAddressFamily.builder()
                                            .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                                            .build())
                                    .build()))),
                    hasInterfaceNeighbors(
                        equalTo(
                            ImmutableMap.of(
                                ISP_TO_INTERNET_INTERFACE_NAME,
                                BgpUnnumberedPeerConfig.builder()
                                    .setPeerInterface(ISP_TO_INTERNET_INTERFACE_NAME)
                                    .setRemoteAs(IspModelingUtils.INTERNET_AS)
                                    .setLocalIp(LINK_LOCAL_IP)
                                    .setLocalAs(_ispAsn)
                                    .setIpv4UnicastAddressFamily(
                                        Ipv4UnicastAddressFamily.builder()
                                            .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_INTERNET)
                                            .build())
                                    .build())))))));

    Layer1Node internetLayer1 =
        new Layer1Node(INTERNET_HOST_NAME, internetToIspInterfaceName(_ispName));
    Layer1Node ispLayer1Iface0 =
        new Layer1Node(
            _ispName, ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName));
    Layer1Node ispLayer1Iface2 = new Layer1Node(_ispName, ISP_TO_INTERNET_INTERFACE_NAME);
    Layer1Node borderLayer1 = new Layer1Node(_snapshotHostname, _snapshotInterfaceName);

    assertThat(
        modeledNodes.getLayer1Edges(),
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(borderLayer1, ispLayer1Iface0),
                new Layer1Edge(ispLayer1Iface0, borderLayer1),
                new Layer1Edge(internetLayer1, ispLayer1Iface2),
                new Layer1Edge(ispLayer1Iface2, internetLayer1))));
  }

  @Test
  public void testGetRoutingPolicyAdvertizeStatic() {
    Configuration internet =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("fakeInternet")
            .build();
    RoutingPolicy internetRoutingPolicy =
        IspModelingUtils.installRoutingPolicyAdvertiseStatic(
            IspModelingUtils.EXPORT_POLICY_ON_INTERNET,
            internet,
            new PrefixSpace(PrefixRange.fromPrefix(Prefix.ZERO)));

    PrefixSpace prefixSpace = new PrefixSpace();
    prefixSpace.addPrefix(Prefix.ZERO);
    RoutingPolicy expectedRoutingPolicy =
        _nf.routingPolicyBuilder()
            .setName(IspModelingUtils.EXPORT_POLICY_ON_INTERNET)
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
  public void testInstallRoutingPolicyForIspToCustomers() {
    Configuration isp =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("fakeIsp")
            .build();
    RoutingPolicy ispRoutingPolicy = installRoutingPolicyForIspToCustomers(isp);

    RoutingPolicy expectedRoutingPolicy =
        _nf.routingPolicyBuilder()
            .setName(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
            .setOwner(isp)
            .setStatements(
                Collections.singletonList(
                    new If(
                        new MatchProtocol(RoutingProtocol.BGP),
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))))
            .build();
    assertThat(ispRoutingPolicy, equalTo(expectedRoutingPolicy));
  }

  @Test
  public void testInstallRoutingPolicyForIspToInternet() {
    Configuration isp =
        _nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("fakeIsp")
            .build();
    PrefixSpace prefixSpace = new PrefixSpace(PrefixRange.fromPrefix(Prefix.parse("1.1.1.1/32")));
    RoutingPolicy expectedRoutingPolicy =
        _nf.routingPolicyBuilder()
            .setName(EXPORT_POLICY_ON_ISP_TO_INTERNET)
            .setOwner(isp)
            .setStatements(
                ImmutableList.of(
                    getAdvertiseBgpStatement(), getAdvertiseStaticStatement(prefixSpace)))
            .build();

    assertThat(
        installRoutingPolicyForIspToInternet(isp, prefixSpace), equalTo(expectedRoutingPolicy));
  }

  @Test
  public void testGetInternetAndIspNodes_multipleSessions() {
    Ip snapshotIp2 = Ip.parse("3.3.3.3");
    Configuration configuration2 = createBgpNode("conf2", "interface2", snapshotIp2);
    BgpActivePeerConfig.builder()
        .setBgpProcess(makeBgpProcess(Ip.ZERO, configuration2.getDefaultVrf()))
        .setPeerAddress(Ip.parse("4.4.4.4"))
        .setRemoteAs(_ispAsn)
        .setLocalIp(snapshotIp2)
        .setLocalAs(_snapshotAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .build();

    Map<String, Configuration> internetAndIsps =
        getInternetAndIspNodes(
                ImmutableMap.of(
                    _snapshotHostname, _snapshotHost, configuration2.getHostname(), configuration2),
                ImmutableList.of(
                    new IspConfiguration(
                        ImmutableList.of(
                            new BorderInterfaceInfo(
                                NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
                            new BorderInterfaceInfo(
                                NodeInterfacePair.of(configuration2.getHostname(), "interface2"))),
                        IspFilter.ALLOW_ALL)),
                new BatfishLogger("output", false),
                new Warnings())
            .getConfigurations();

    assertThat(internetAndIsps, hasKey(_ispName));

    Configuration isp = internetAndIsps.get(_ispName);
    // two interfaces for peering with the two configurations and one for peering with internet
    assertThat(isp.getAllInterfaces().entrySet(), hasSize(3));
    assertThat(
        isp.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                ispToRemoteInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                ispToRemoteInterfaceName(configuration2.getHostname(), "interface2"),
                ISP_TO_INTERNET_INTERFACE_NAME)));
  }

  @Test
  public void testGetInternetAndIspNodes_unknownBorders() {
    // passing non-existent border interfaces
    Map<String, Configuration> internetAndIsps =
        getInternetAndIspNodes(
                ImmutableMap.of(_snapshotHostname, _snapshotHost),
                ImmutableList.of(
                    new IspConfiguration(
                        ImmutableList.of(
                            new BorderInterfaceInfo(NodeInterfacePair.of("conf2", "interface2"))),
                        IspFilter.ALLOW_ALL)),
                new BatfishLogger("output", false),
                new Warnings())
            .getConfigurations();

    // no ISPs and no Internet
    assertThat(internetAndIsps, anEmptyMap());
  }

  /** Test that combining ISP configs works when two configs have an ASN in common */
  @Test
  public void testCombineIspConfigurations_commonAsn() {
    Ip ispIp2 = Ip.parse("2.1.1.1");
    Ip snapshotIp2 = Ip.parse("2.1.1.2");
    Configuration c2 = createBgpNode("c2", _snapshotInterfaceName, snapshotIp2);
    BgpActivePeerConfig.builder()
        .setPeerAddress(ispIp2)
        .setRemoteAs(_ispAsn)
        .setLocalIp(snapshotIp2)
        .setLocalAs(_snapshotAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .setBgpProcess(c2.getDefaultVrf().getBgpProcess())
        .build();

    Map<Long, IspModel> combinedMap =
        IspModelingUtils.combineIspConfigurations(
            ImmutableMap.of(_snapshotHostname, _snapshotHost, c2.getHostname(), c2),
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName))),
                    IspFilter.ALLOW_ALL),
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(c2.getHostname(), _snapshotInterfaceName))),
                    IspFilter.ALLOW_ALL)),
            new Warnings());

    assertThat(
        combinedMap,
        equalTo(
            ImmutableMap.of(
                _ispAsn,
                IspModel.builder()
                    .setAsn(_ispAsn)
                    .setRemotes(
                        new Remote(
                            _snapshotHostname,
                            getOnlyElement(_snapshotHost.getAllInterfaces().keySet()),
                            ConcreteInterfaceAddress.create(_ispIp, 24),
                            getOnlyElement(
                                _snapshotHost
                                    .getDefaultVrf()
                                    .getBgpProcess()
                                    .getActiveNeighbors()
                                    .values())),
                        new Remote(
                            c2.getHostname(),
                            getOnlyElement(c2.getAllInterfaces().keySet()),
                            ConcreteInterfaceAddress.create(ispIp2, 24),
                            getOnlyElement(
                                c2.getDefaultVrf().getBgpProcess().getActiveNeighbors().values())))
                    .setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet())
                    .build())));
  }

  @Test
  public void testIspNameConflictsGoodCase() {
    Map<Long, IspModel> ispInfoMap =
        ImmutableMap.of(
            1L,
            IspModel.builder().setAsn(1).setName("isp1").build(),
            2L,
            IspModel.builder().setAsn(2).setName("isp2").build());
    Map<String, Configuration> configurations =
        ImmutableMap.of(
            "node",
            _nf.configurationBuilder()
                .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
                .build());
    assertTrue(ispNameConflicts(configurations, ispInfoMap).isEmpty());
  }

  @Test
  public void testIspNameConflictsIspConflict() {
    Map<Long, IspModel> ispInfoMap =
        ImmutableMap.of(
            1L,
            IspModel.builder().setAsn(1).setName("isp1").build(),
            2L,
            IspModel.builder().setAsn(2).setName("isp1").build());
    Map<String, Configuration> configurations = ImmutableMap.of();

    // No conflicts when duplicate human names are used, since the ASN used for hostname is still
    // different.
    assertThat(ispNameConflicts(configurations, ispInfoMap), empty());
  }

  @Test
  public void testIspNameConflictsNodeConflict() {
    Map<Long, IspModel> ispInfoMap =
        ImmutableMap.of(1L, IspModel.builder().setAsn(1).setName("FOO").build());
    Map<String, Configuration> configurationsNoConflict =
        ImmutableMap.of(
            "FOO",
            _nf.configurationBuilder()
                .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
                .build());

    // No conflicts when node name matches ISP human name.
    assertThat(ispNameConflicts(configurationsNoConflict, ispInfoMap), empty());

    Map<String, Configuration> configurationsConflict =
        ImmutableMap.of(
            getDefaultIspNodeName(1),
            _nf.configurationBuilder()
                .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
                .build());
    // Conflict when node name matches ISP hostame.
    String message = getOnlyElement(ispNameConflicts(configurationsConflict, ispInfoMap));
    assertThat(message, containsString("ASN 1"));
  }
}
