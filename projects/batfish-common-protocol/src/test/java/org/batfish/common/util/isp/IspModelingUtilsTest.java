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
import static org.batfish.common.util.isp.IspModelingUtils.combineBorderInterfaces;
import static org.batfish.common.util.isp.IspModelingUtils.combineIspPeerings;
import static org.batfish.common.util.isp.IspModelingUtils.connectIspToInternet;
import static org.batfish.common.util.isp.IspModelingUtils.connectIspToSnapshot;
import static org.batfish.common.util.isp.IspModelingUtils.connectPeerIsps;
import static org.batfish.common.util.isp.IspModelingUtils.createInternetNode;
import static org.batfish.common.util.isp.IspModelingUtils.createIspNode;
import static org.batfish.common.util.isp.IspModelingUtils.getAdvertiseBgpStatement;
import static org.batfish.common.util.isp.IspModelingUtils.getAdvertiseStaticStatement;
import static org.batfish.common.util.isp.IspModelingUtils.getAsnOfIspNode;
import static org.batfish.common.util.isp.IspModelingUtils.getDefaultIspNodeName;
import static org.batfish.common.util.isp.IspModelingUtils.getInternetAndIspNodes;
import static org.batfish.common.util.isp.IspModelingUtils.getSnapshotConnectionForBgpPeerInfo;
import static org.batfish.common.util.isp.IspModelingUtils.getSnapshotConnectionsForBorderInterface;
import static org.batfish.common.util.isp.IspModelingUtils.inferSnapshotBgpIfaceAddress;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyForIspToCustomers;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyForIspToInternet;
import static org.batfish.common.util.isp.IspModelingUtils.internetToIspInterfaceName;
import static org.batfish.common.util.isp.IspModelingUtils.isValidBgpPeerForBorderInterfaceInfo;
import static org.batfish.common.util.isp.IspModelingUtils.ispNameConflicts;
import static org.batfish.common.util.isp.IspModelingUtils.ispPeeringInterfaceName;
import static org.batfish.common.util.isp.IspModelingUtils.ispToSnapshotInterfaceName;
import static org.batfish.common.util.isp.IspModelingUtils.makeBgpProcess;
import static org.batfish.common.util.isp.IspModelingUtils.toIspModel;
import static org.batfish.common.util.isp.IspModelingUtils.validateOrExplainProblemCreatingIspConfig;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
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
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.isp_configuration.BgpPeerInfo;
import org.batfish.datamodel.isp_configuration.BorderInterfaceInfo;
import org.batfish.datamodel.isp_configuration.IspAnnouncement;
import org.batfish.datamodel.isp_configuration.IspAttachment;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.datamodel.isp_configuration.IspFilter;
import org.batfish.datamodel.isp_configuration.IspNodeInfo;
import org.batfish.datamodel.isp_configuration.IspNodeInfo.Role;
import org.batfish.datamodel.isp_configuration.IspPeeringInfo;
import org.batfish.datamodel.isp_configuration.IspPeeringInfo.Peer;
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

  private BatfishLogger _logger;
  private NetworkFactory _nf;
  private Configuration _snapshotHost;
  private BgpActivePeerConfig _snapshotActivePeer;
  private SnapshotConnection _snapshotConnection;
  private IspModel _ispModel;

  @Before
  public void setup() {
    _logger = new BatfishLogger("output", false);
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
    _snapshotConnection =
        new SnapshotConnection(
            ImmutableList.of(
                new IspInterface(
                    ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                    ConcreteInterfaceAddress.create(_ispIp, 24),
                    new Layer1Node(_snapshotHostname, _snapshotInterfaceName),
                    null)),
            IspBgpActivePeer.create(_snapshotActivePeer));
    _ispModel =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setName(_ispName)
            .setRole(Role.TRANSIT)
            .setSnapshotConnections(_snapshotConnection)
            .setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet())
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
  public void testCombineBorderInterfaces_nonExistentNode() {
    Warnings warnings = new Warnings(true, true, true);
    combineBorderInterfaces(
        ImmutableMap.of(_snapshotHostname, _snapshotHost),
        ImmutableList.of(
            new IspConfiguration(
                ImmutableList.of(new BorderInterfaceInfo(NodeInterfacePair.of("conf1", "init1"))),
                IspFilter.ALLOW_ALL)),
        warnings);

    assertThat(warnings, hasRedFlag(hasText("ISP Modeling: Non-existent border node conf1")));
  }

  @Test
  public void testCombineBorderInterfaces_nonExistentInterface() {
    Warnings warnings = new Warnings(true, true, true);

    combineBorderInterfaces(
        ImmutableMap.of(_snapshotHostname, _snapshotHost),
        ImmutableList.of(
            new IspConfiguration(
                ImmutableList.of(
                    new BorderInterfaceInfo(NodeInterfacePair.of(_snapshotHostname, "init1"))),
                IspFilter.ALLOW_ALL)),
        warnings);

    assertThat(
        warnings, hasRedFlag(hasText("ISP Modeling: Non-existent border interface conf[init1]")));
  }

  @Test
  public void testAddBgpPeerToIsp() {
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    IspBgpActivePeer ispBgpActivePeer =
        new IspBgpActivePeer(_snapshotIp, _ispIp, _snapshotAsn, _ispAsn, false);

    addBgpPeerToIsp(ispBgpActivePeer, bgpProcess, Role.TRANSIT);
    BgpActivePeerConfig peer = getOnlyElement(bgpProcess.getActiveNeighbors().values());

    assertThat(
        peer,
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
  public void testAddBgpPeerToIsp_Unnumbered() {
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    IspBgpUnnumberedPeer ispBgpUnnumberedPeer =
        new IspBgpUnnumberedPeer("iface", _snapshotAsn, _ispAsn, false);
    addBgpPeerToIsp(ispBgpUnnumberedPeer, bgpProcess, Role.TRANSIT);
    BgpUnnumberedPeerConfig peer = getOnlyElement(bgpProcess.getInterfaceNeighbors().values());

    assertThat(
        peer,
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
  public void testAddBgpPeerToIsp_sendCommunity() {
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    IspBgpActivePeer ispBgpActivePeer =
        new IspBgpActivePeer(_snapshotIp, _ispIp, _snapshotAsn, _ispAsn, false);

    addBgpPeerToIsp(ispBgpActivePeer, bgpProcess, Role.PRIVATE_BACKBONE);
    BgpActivePeerConfig peer = getOnlyElement(bgpProcess.getActiveNeighbors().values());

    assertThat(
        peer,
        equalTo(
            BgpActivePeerConfig.builder()
                .setLocalIp(_ispIp)
                .setPeerAddress(_snapshotIp)
                .setLocalAs(_ispAsn)
                .setRemoteAs(_snapshotAsn)
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setExportPolicy(EXPORT_POLICY_ON_ISP_TO_CUSTOMERS)
                        .setAddressFamilyCapabilities(
                            AddressFamilyCapabilities.builder()
                                .setSendCommunity(true)
                                .setSendExtendedCommunity(true)
                                .build())
                        .build())
                .build()));
  }

  @Test
  public void testIsValidBgpPeerForBorderInterfaceInfo_active() {
    Set<ConcreteInterfaceAddress> interfaceAddresses =
        ImmutableSet.of(ConcreteInterfaceAddress.parse("3.3.3.1/30"));
    BgpActivePeerConfig.Builder bgpActivePeerConfig =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("3.3.3.3"))
            .setRemoteAs(1L)
            .setLocalAs(2L)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());

    // wrong explicit local IP
    assertFalse(
        isValidBgpPeerForBorderInterfaceInfo(
            bgpActivePeerConfig.setLocalIp(Ip.parse("2.2.2.2")).build(),
            interfaceAddresses,
            ImmutableSet.of(),
            ALL_AS_NUMBERS));

    // correct explicit local IP
    assertTrue(
        isValidBgpPeerForBorderInterfaceInfo(
            bgpActivePeerConfig.setLocalIp(Ip.parse("3.3.3.1")).build(),
            interfaceAddresses,
            ImmutableSet.of(),
            ALL_AS_NUMBERS));

    // no local IP, eBGP-single hop with matching IP
    assertTrue(
        isValidBgpPeerForBorderInterfaceInfo(
            bgpActivePeerConfig.setLocalIp(null).build(),
            interfaceAddresses,
            ImmutableSet.of(),
            ALL_AS_NUMBERS));

    // no local IP, eBGP-single hop, non-matching IP
    assertFalse(
        isValidBgpPeerForBorderInterfaceInfo(
            bgpActivePeerConfig.setLocalIp(null).build(),
            ImmutableSet.of(ConcreteInterfaceAddress.parse("4.4.4.1/30")),
            ImmutableSet.of(),
            ALL_AS_NUMBERS));

    // no local IP, eBGP-multihop, non-matching IP
    assertFalse(
        isValidBgpPeerForBorderInterfaceInfo(
            bgpActivePeerConfig.setLocalIp(null).setEbgpMultihop(true).build(),
            interfaceAddresses,
            ImmutableSet.of(),
            ALL_AS_NUMBERS));
  }

  @Test
  public void testIsValidBgpPeerForBorderInterfaceInfo_unnumbered() {
    // missing remote ASN
    BgpUnnumberedPeerConfig invalidPeer =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface("iface")
            .setLocalIp(LINK_LOCAL_IP)
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
        isValidBgpPeerForBorderInterfaceInfo(
            invalidPeer, ImmutableSet.of(), ImmutableSet.of(), ALL_AS_NUMBERS));
    assertTrue(
        isValidBgpPeerForBorderInterfaceInfo(
            validPeer, ImmutableSet.of(), ImmutableSet.of(), ALL_AS_NUMBERS));
  }

  @Test
  public void testCreateIspNode() {
    Configuration ispConfiguration = createIspNode(_ispModel, _logger).get();

    assertThat(
        ispConfiguration,
        allOf(
            hasHostname(_ispName),
            hasDeviceType(equalTo(DeviceType.ISP)),
            hasVrf(DEFAULT_VRF_NAME, hasBgpProcess(allOf(hasMultipathEbgp(true))))));
  }

  @Test
  public void testConnectIspToSnapshot() {
    Configuration ispConfiguration = createIspNode(_ispModel, _logger).get();

    Set<Layer1Edge> layer1Edges = connectIspToSnapshot(_ispModel, ispConfiguration, _logger);

    assertThat(
        ispConfiguration,
        allOf(
            hasInterface(
                ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                hasAllAddresses(
                    equalTo(ImmutableSet.of(ConcreteInterfaceAddress.create(_ispIp, 24)))))));

    // compute the reverse config
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(_snapshotConnection.getBgpPeer(), bgpProcess, Role.TRANSIT);
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
        layer1Edges,
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    _ispName,
                    ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName)),
                new Layer1Edge(
                    _ispName,
                    ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                    _snapshotHostname,
                    _snapshotInterfaceName))));
  }

  @Test
  public void testConnectIspToSnapshot_unnumbered() {
    BgpUnnumberedPeerConfig remotePeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface(_snapshotInterfaceName)
            .setRemoteAs(_ispAsn)
            .setLocalIp(LINK_LOCAL_IP)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .build();
    String ispIfaceName = ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName);
    IspModel ispModel =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setSnapshotConnections(
                new SnapshotConnection(
                    ImmutableList.of(
                        new IspInterface(
                            ispIfaceName,
                            LINK_LOCAL_ADDRESS,
                            new Layer1Node(_snapshotHostname, _snapshotInterfaceName),
                            null)),
                    IspBgpUnnumberedPeer.create(remotePeerConfig, ispIfaceName)))
            .build();

    Configuration ispConfiguration = createIspNode(ispModel, _logger).get();
    Set<Layer1Edge> layer1Edges = connectIspToSnapshot(ispModel, ispConfiguration, _logger);

    assertThat(
        ispConfiguration,
        allOf(
            hasInterface(
                ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                hasAllAddresses(equalTo(ImmutableSet.of(LINK_LOCAL_ADDRESS))))));

    // compute the reverse config
    BgpProcess bgpProcess = testBgpProcess(Ip.ZERO);
    addBgpPeerToIsp(
        IspBgpUnnumberedPeer.create(
            remotePeerConfig,
            ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName)),
        bgpProcess,
        ispModel.getRole());
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
        layer1Edges,
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(
                    _snapshotHostname,
                    _snapshotInterfaceName,
                    _ispName,
                    ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName)),
                new Layer1Edge(
                    _ispName,
                    ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                    _snapshotHostname,
                    _snapshotInterfaceName))));
  }

  @Test
  public void testCreateIspNode_invalid() {
    IspModel ispInfo = IspModel.builder().setAsn(_ispAsn).build();
    BatfishLogger logger = new BatfishLogger("debug", false);
    Optional<Configuration> ispConfiguration = createIspNode(ispInfo, logger);

    assertFalse(ispConfiguration.isPresent());

    assertThat(logger.getHistory(), hasSize(1));
    assertThat(
        logger.getHistory().toString(300), equalTo("ISP information for ASN '1' is not correct"));
  }

  /** Basic ISP to Internet connectivity, without additional prefixes */
  @Test
  public void testConnectIspToInternet() {
    BatfishLogger logger = new BatfishLogger("output", false);
    Configuration ispConfiguration = createIspNode(_ispModel, logger).get();
    connectIspToSnapshot(_ispModel, ispConfiguration, logger);

    Configuration internet = createInternetNode();

    Set<Layer1Edge> layer1Edges =
        connectIspToInternet(_ispAsn, _ispModel, ispConfiguration, internet);
    assertThat(
        ispConfiguration,
        allOf(
            hasIpAccessList(FROM_INTERNET_ACL_NAME, any(IpAccessList.class)),
            hasIpAccessList(TO_INTERNET_ACL_NAME, any(IpAccessList.class)),
            hasInterface(ISP_TO_INTERNET_INTERFACE_NAME, any(Interface.class))));
    Interface toInternet = ispConfiguration.getAllInterfaces().get(ISP_TO_INTERNET_INTERFACE_NAME);
    assertThat(toInternet.getIncomingFilter(), notNullValue());
    assertThat(toInternet.getIncomingFilter().getName(), equalTo(FROM_INTERNET_ACL_NAME));
    assertThat(toInternet.getOutgoingFilter(), notNullValue());
    assertThat(toInternet.getOutgoingFilter().getName(), equalTo(TO_INTERNET_ACL_NAME));

    assertThat(
        internet,
        allOf(
            hasHostname(INTERNET_HOST_NAME),
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

    ImmutableSet<InterfaceAddress> interfaceAddresses =
        ispConfiguration.getAllInterfaces().values().stream()
            .flatMap(iface -> iface.getAllAddresses().stream())
            .collect(ImmutableSet.toImmutableSet());
    assertThat(
        interfaceAddresses,
        equalTo(ImmutableSet.of(LINK_LOCAL_ADDRESS, ConcreteInterfaceAddress.create(_ispIp, 24))));

    assertThat(
        ispConfiguration,
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

    assertThat(
        ispConfiguration.getRoutingPolicies(),
        hasEntry(
            EXPORT_POLICY_ON_ISP_TO_INTERNET,
            installRoutingPolicyForIspToInternet(ispConfiguration, new PrefixSpace())));

    Layer1Node internetLayer1 =
        new Layer1Node(INTERNET_HOST_NAME, internetToIspInterfaceName(_ispName));
    Layer1Node ispLayer1 = new Layer1Node(_ispName, ISP_TO_INTERNET_INTERFACE_NAME);

    assertThat(
        layer1Edges,
        equalTo(
            ImmutableSet.of(
                new Layer1Edge(internetLayer1, ispLayer1),
                new Layer1Edge(ispLayer1, internetLayer1))));
  }

  /**
   * Test that infrastructure for additional announcements is created when connecting ISPs to the
   * Internet.
   */
  @Test
  public void testConnectIspToInternet_additionalAnnouncements() {
    Set<Prefix> additionalPrefixes =
        ImmutableSet.of(Prefix.parse("10.1.1.1/32"), Prefix.parse("20.2.2.2/32"));
    IspModel ispModel =
        IspModel.builder()
            .setAsn(_ispAsn)
            .setSnapshotConnections(_snapshotConnection)
            .setAdditionalPrefixesToInternet(additionalPrefixes)
            .build();

    Configuration ispConfiguration =
        createIspNode(ispModel, new BatfishLogger("debug", false)).get();
    Configuration internet = createInternetNode();

    connectIspToInternet(_ispAsn, ispModel, ispConfiguration, internet);

    assertThat(
        ispConfiguration.getDefaultVrf().getStaticRoutes(),
        equalTo(
            ImmutableSortedSet.copyOf(
                ispModel.getAdditionalPrefixesToInternet().stream()
                    .map(
                        prefix ->
                            StaticRoute.testBuilder()
                                .setNetwork(prefix)
                                .setNextHopInterface(NULL_INTERFACE_NAME)
                                .setAdministrativeCost(HIGH_ADMINISTRATIVE_COST)
                                .build())
                    .collect(ImmutableSet.toImmutableSet()))));

    PrefixSpace prefixSpace = new PrefixSpace();
    ispModel.getAdditionalPrefixesToInternet().forEach(prefixSpace::addPrefix);
    assertThat(
        ispConfiguration.getRoutingPolicies(),
        hasEntry(
            EXPORT_POLICY_ON_ISP_TO_INTERNET,
            installRoutingPolicyForIspToInternet(ispConfiguration, prefixSpace)));
  }

  @Test
  public void testConnectIspPeers() {
    IspModel ispModel2 =
        IspModel.builder()
            .setAsn(23)
            .setSnapshotConnections(_ispModel.getSnapshotConnections())
            .build();
    Configuration isp1 = createIspNode(_ispModel, _logger).get();
    Configuration isp2 = createIspNode(ispModel2, _logger).get();

    Set<Layer1Edge> layerEdges =
        connectPeerIsps(
            new IspPeering(_ispAsn, ispModel2.getAsn()), _ispModel, ispModel2, isp1, isp2);

    assertTrue(isp1.getAllInterfaces().containsKey(ispPeeringInterfaceName(isp2.getHostname())));
    assertTrue(isp2.getAllInterfaces().containsKey(ispPeeringInterfaceName(isp1.getHostname())));

    assertTrue(
        isp1.getDefaultVrf()
            .getBgpProcess()
            .getInterfaceNeighbors()
            .containsKey(ispPeeringInterfaceName(isp2.getHostname())));
    assertTrue(
        isp2.getDefaultVrf()
            .getBgpProcess()
            .getInterfaceNeighbors()
            .containsKey(ispPeeringInterfaceName(isp1.getHostname())));

    Layer1Node ispL1 =
        new Layer1Node(isp1.getHostname(), ispPeeringInterfaceName(isp2.getHostname()));
    Layer1Node ispL2 =
        new Layer1Node(isp2.getHostname(), ispPeeringInterfaceName(isp1.getHostname()));

    assertThat(
        layerEdges,
        equalTo(ImmutableSet.of(new Layer1Edge(ispL1, ispL2), new Layer1Edge(ispL2, ispL1))));
  }

  @Test
  public void testGetSnapshotConnectionsForBorderInterface_bgpActive() {
    List<SnapshotConnection> snapshotConnections =
        getSnapshotConnectionsForBorderInterface(
            new BorderInterfaceInfo(
                NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    assertThat(snapshotConnections, equalTo(ImmutableList.of(_snapshotConnection)));
  }

  @Test
  public void testGetSnapshotConnectionsForBorderInterface_bgpUnnumbered() {
    Configuration configuration = createBgpNode("conf", _snapshotInterfaceName, _snapshotIp);
    BgpUnnumberedPeerConfig snapshotPeer =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface(_snapshotInterfaceName)
            .setRemoteAs(_ispAsn)
            .setLocalIp(_snapshotIp)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .setBgpProcess(configuration.getDefaultVrf().getBgpProcess())
            .build();

    List<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionsForBorderInterface(
            new BorderInterfaceInfo(
                NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(configuration.getHostname(), configuration),
            new Warnings());

    String ispIfaceName = ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName);
    assertThat(
        snapshotConnection,
        equalTo(
            ImmutableList.of(
                new SnapshotConnection(
                    ImmutableList.of(
                        new IspInterface(
                            ispIfaceName,
                            LINK_LOCAL_ADDRESS,
                            new Layer1Node(_snapshotHostname, _snapshotInterfaceName),
                            null)),
                    IspBgpUnnumberedPeer.create(snapshotPeer, ispIfaceName)))));
  }

  @Test
  public void testGetSnapshotConnectionsForBorderInterface_multiplePeers() {
    Ip ispIp2 = Ip.parse("3.3.3.3");
    BgpActivePeerConfig snapshotPeer2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(ispIp2)
            .setRemoteAs(_ispAsn)
            .setLocalIp(_snapshotIp)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess())
            .build();

    List<SnapshotConnection> snapshotConnections =
        getSnapshotConnectionsForBorderInterface(
            new BorderInterfaceInfo(
                NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    String ispIfaceName = ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName);
    assertThat(
        snapshotConnections,
        equalTo(
            ImmutableList.of(
                _snapshotConnection,
                new SnapshotConnection(
                    ImmutableList.of(
                        new IspInterface(
                            ispIfaceName,
                            ConcreteInterfaceAddress.create(ispIp2, 24),
                            new Layer1Node(_snapshotHostname, _snapshotInterfaceName),
                            null)),
                    IspBgpActivePeer.create(snapshotPeer2)))));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo() {
    Optional<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment(null, _snapshotInterfaceName, null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    assertThat(snapshotConnection.get(), equalTo(_snapshotConnection));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_noAttachment() {
    Optional<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(_snapshotHostname, null, _ispIp, null, null),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    assertThat(
        snapshotConnection.get(),
        equalTo(
            new SnapshotConnection(
                ImmutableList.of(), IspBgpActivePeer.create(_snapshotActivePeer))));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_differentAttachmentInterface() {
    Interface attachIface =
        _nf.interfaceBuilder().setName("different").setOwner(_snapshotHost).build();
    Optional<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment(null, attachIface.getName(), null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    assertThat(
        snapshotConnection.get(),
        equalTo(
            new SnapshotConnection(
                ImmutableList.of(
                    new IspInterface(
                        ispToSnapshotInterfaceName(_snapshotHostname, attachIface.getName()),
                        ConcreteInterfaceAddress.create(_ispIp, 24),
                        new Layer1Node(_snapshotHostname, attachIface.getName()),
                        null)),
                IspBgpActivePeer.create(_snapshotActivePeer))));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_differentAttachmentHost() {
    Configuration attachHost =
        _nf.configurationBuilder()
            .setHostname("attach")
            .setConfigurationFormat(ConfigurationFormat.ARISTA)
            .build();
    Interface attachIface =
        _nf.interfaceBuilder().setName("attach-iface").setOwner(attachHost).build();
    Optional<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment(attachHost.getHostname(), attachIface.getName(), null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost, attachHost.getHostname(), attachHost),
            new Warnings());

    assertThat(
        snapshotConnection.get(),
        equalTo(
            new SnapshotConnection(
                ImmutableList.of(
                    new IspInterface(
                        ispToSnapshotInterfaceName(attachHost.getHostname(), attachIface.getName()),
                        ConcreteInterfaceAddress.create(_ispIp, 24),
                        new Layer1Node(attachHost.getHostname(), attachIface.getName()),
                        null)),
                IspBgpActivePeer.create(_snapshotActivePeer))));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_withVlanTag() {
    Optional<SnapshotConnection> snapshotConnection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment(null, _snapshotInterfaceName, 23)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            new Warnings());

    assertThat(
        snapshotConnection.get(),
        equalTo(
            new SnapshotConnection(
                ImmutableList.of(
                    new IspInterface(
                        ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                        ConcreteInterfaceAddress.create(_ispIp, 24),
                        new Layer1Node(_snapshotHostname, _snapshotInterfaceName),
                        23)),
                IspBgpActivePeer.create(_snapshotActivePeer))));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_missingBgpHost() {
    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                "other", null, _ispIp, null, new IspAttachment(null, _snapshotInterfaceName, null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(hasText("ISP Modeling: Non-existent border node other")));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_missingBgpPeer() {
    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                Ip.ZERO,
                null,
                new IspAttachment(null, _snapshotInterfaceName, null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(
            hasText("ISP Modeling: No BGP neighbor 0.0.0.0 found on node conf in default vrf")));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_invalidBgpPeer() {
    // add a peer with missing remote as
    BgpActivePeerConfig bgpPeer =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.ZERO)
            .setLocalIp(_snapshotIp)
            .setLocalAs(_snapshotAsn)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess())
            .build();

    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                bgpPeer.getPeerAddress(),
                null,
                new IspAttachment(null, _snapshotInterfaceName, null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(
            hasText(
                "ISP Modeling: BGP neighbor 0.0.0.0 on node conf is invalid: unable to determine"
                    + " remote AS.")));
  }

  /** Helper to generate a peer for which ISP can be generated. */
  private BgpActivePeerConfig.Builder correctBuilder() {
    return BgpActivePeerConfig.builder()
        .setPeerAddress(_ispIp)
        .setLocalIp(_snapshotIp)
        .setLocalAs(_snapshotAsn)
        .setRemoteAs(_ispAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess());
  }

  @Test
  public void testValidateOrExplainProblemCreatingIspConfig() {
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
            correctBuilder().build(), ImmutableSet.of(), ALL_AS_NUMBERS),
        equalTo(Optional.empty()));

    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().setLocalAs(null).build(), ImmutableSet.of(), ALL_AS_NUMBERS)
            .get(),
        containsString("unable to determine local AS"));
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().setLocalAs(_ispAsn).build(), ImmutableSet.of(), ALL_AS_NUMBERS)
            .get(),
        containsString("iBGP peers are not supported"));
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().setRemoteAsns(LongSpace.EMPTY).build(),
                ImmutableSet.of(),
                ALL_AS_NUMBERS)
            .get(),
        containsString("unable to determine remote AS"));
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().build(), ImmutableSet.of(), LongSpace.of(_ispAsn + 3))
            .get(),
        containsString("remote AS 1 is not allowed by the filter"));
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().setPeerAddress(null).build(), ImmutableSet.of(), ALL_AS_NUMBERS)
            .get(),
        containsString("remote IP is not configured"));
    assertThat(
        validateOrExplainProblemCreatingIspConfig(
                correctBuilder().build(), ImmutableSet.of(Ip.ZERO), ALL_AS_NUMBERS)
            .get(),
        containsString("remote IP " + _ispIp + " is not allowed by the filter"));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_missingAttachmentHost() {
    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment("other", _snapshotInterfaceName, null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(hasText("ISP Modeling: Non-existent ISP attachment node other")));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_missingAttachmentInterface() {
    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname, null, _ispIp, null, new IspAttachment(null, "other", null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(hasText("ISP Modeling: Non-existent attachment interface other on node conf")));
  }

  @Test
  public void testGetSnapshotConnectionForBgpPeerInfo_badLayer3AttachmentInterface() {
    Interface attachIface =
        _nf.interfaceBuilder()
            .setOwner(_snapshotHost)
            .setName("attach-iface")
            .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
            .build();
    Warnings warnings = new Warnings(true, true, true);
    Optional<SnapshotConnection> connection =
        getSnapshotConnectionForBgpPeerInfo(
            new BgpPeerInfo(
                _snapshotHostname,
                null,
                _ispIp,
                null,
                new IspAttachment(null, attachIface.getName(), null)),
            ImmutableSet.of(),
            ALL_AS_NUMBERS,
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            warnings);
    assertFalse(connection.isPresent());
    assertThat(
        warnings.getRedFlagWarnings(),
        contains(
            hasText(
                "ISP Modeling: The attachment interface conf[attach-iface] cannot enable the BGP"
                    + " peering to neighbor 1.1.1.1 because it is Layer3 but does not own the BGP"
                    + " peer's local IP 2.2.2.2")));
  }

  /** Test the preference order of inferSnapshotBgpIfaceAddress */
  @Test
  public void testInferSnapshotBgpIfaceAddress() {
    Ip localIp = Ip.parse("1.1.1.1");
    ConcreteInterfaceAddress addr22 = ConcreteInterfaceAddress.create(localIp, 22);
    ConcreteInterfaceAddress addr23 = ConcreteInterfaceAddress.create(localIp, 23);
    ConcreteInterfaceAddress addr24 = ConcreteInterfaceAddress.create(localIp, 24);
    ConcreteInterfaceAddress addr25 = ConcreteInterfaceAddress.create(localIp, 25);
    Interface active1 =
        _nf.interfaceBuilder()
            .setAdminUp(true)
            .setName("active1")
            .setAddresses(addr24, addr25)
            .build();

    Interface inactive1 =
        _nf.interfaceBuilder()
            .setAdminUp(false)
            .setName("inactive1")
            .setAddresses(addr22, addr23)
            .build();

    // lower address is picked
    assertThat(
        inferSnapshotBgpIfaceAddress(ImmutableList.of(active1), localIp),
        equalTo(Optional.of(addr24)));

    // active is picked even if a lower inactive is present
    assertThat(
        inferSnapshotBgpIfaceAddress(ImmutableList.of(active1, inactive1), localIp),
        equalTo(Optional.of(addr24)));

    // lower inactive is picked when nothing is active
    assertThat(
        inferSnapshotBgpIfaceAddress(ImmutableList.of(inactive1), localIp),
        equalTo(Optional.of(addr22)));
  }

  @Test
  public void testToIspModel_customIspName() {
    IspModel ispModel =
        toIspModel(
            _ispAsn, ImmutableList.of(), ImmutableList.of(new IspNodeInfo(_ispAsn, "myisp")));
    assertThat(ispModel.getName(), equalTo("myisp"));
  }

  @Test
  public void testToIspModel_role() {
    IspModel ispModel =
        toIspModel(
            _ispAsn,
            ImmutableList.of(),
            ImmutableList.of(
                new IspNodeInfo(
                    _ispAsn, "myisp", Role.PRIVATE_BACKBONE, ImmutableList.of(), null)));
    assertThat(ispModel.getRole(), equalTo(Role.PRIVATE_BACKBONE));
  }

  @Test
  public void testToIspModel_trafficFiltering() {
    IspModel ispModelPrivate =
        toIspModel(
            _ispAsn,
            ImmutableList.of(),
            ImmutableList.of(
                new IspNodeInfo(
                    _ispAsn, "myisp", Role.PRIVATE_BACKBONE, ImmutableList.of(), null)));
    assertThat(ispModelPrivate.getTrafficFiltering(), equalTo(IspTrafficFiltering.none()));

    IspModel ispModelTransit =
        toIspModel(
            _ispAsn,
            ImmutableList.of(),
            ImmutableList.of(
                new IspNodeInfo(_ispAsn, "myisp", Role.TRANSIT, ImmutableList.of(), null)));
    assertThat(
        ispModelTransit.getTrafficFiltering(),
        equalTo(IspTrafficFiltering.blockReservedAddressesAtInternet()));
  }

  @Test
  public void testToIspModel_mergeAdditionalPrefixes() {
    IspModel ispModel =
        toIspModel(
            _ispAsn,
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
                        new IspAnnouncement(Prefix.parse("2.2.2.2/32"))))));

    assertThat(
        ispModel.getAdditionalPrefixesToInternet(),
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
    Configuration internet = createInternetNode();
    InterfaceAddress interfaceAddress =
        ConcreteInterfaceAddress.create(
            IspModelingUtils.INTERNET_OUT_ADDRESS,
            IspModelingUtils.INTERNET_OUT_SUBNET.getPrefixLength());
    assertThat(
        internet,
        allOf(
            hasHostname(INTERNET_HOST_NAME),
            hasDeviceType(equalTo(DeviceType.INTERNET)),
            hasInterface(
                INTERNET_OUT_INTERFACE,
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
                                        .setNextHopInterface(INTERNET_OUT_INTERFACE)
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

    // assert that nodes are created. their content is tested with the helper functions.
    assertTrue(modeledNodes.getConfigurations().containsKey(INTERNET_HOST_NAME));
    assertTrue(modeledNodes.getConfigurations().containsKey(_ispName));
  }

  /** Check that the Internet node is not created when all ISPs are PRIVATE_BACKBONES */
  @Test
  public void testGetInternetAndIspNodes_noInternet() {
    ModeledNodes modeledNodes =
        getInternetAndIspNodes(
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(NodeInterfacePair.of("conf", "interface"))),
                    ImmutableList.of(),
                    IspFilter.ALLOW_ALL,
                    ImmutableList.of(
                        new IspNodeInfo(
                            _ispAsn,
                            _ispName,
                            Role.PRIVATE_BACKBONE,
                            ImmutableList.of(),
                            IspTrafficFiltering.none())),
                    ImmutableList.of())),
            new BatfishLogger("output", false),
            new Warnings());

    assertFalse(modeledNodes.getConfigurations().containsKey(INTERNET_HOST_NAME));
    assertTrue(modeledNodes.getConfigurations().containsKey(_ispName));
  }

  /**
   * Check the case with a an Internet-connected ISP and a non-Internet-connected ISP. The Internet
   * node should be created and only the former ISP should be connected to it.
   */
  @Test
  public void testGetInternetAndIspNodes_mixedInternet() {
    // add another interface and peer to the configuration we already have
    String snapshotInterface2 = "interface2";
    Ip snapshotIp2 = Ip.parse("3.3.3.3");
    Ip ispIp2 = Ip.parse("4.4.4.4");
    long ispAsn2 = 4L;
    String ispName2 = getDefaultIspNodeName(ispAsn2);
    _nf.interfaceBuilder()
        .setName(snapshotInterface2)
        .setOwner(_snapshotHost)
        .setAddress(ConcreteInterfaceAddress.create(snapshotIp2, 24))
        .build();
    BgpActivePeerConfig.builder()
        .setPeerAddress(ispIp2)
        .setRemoteAs(ispAsn2)
        .setLocalIp(snapshotIp2)
        .setLocalAs(_snapshotAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess())
        .build();

    ModeledNodes modeledNodes =
        getInternetAndIspNodes(
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, snapshotInterface2))),
                    ImmutableList.of(),
                    IspFilter.ALLOW_ALL,
                    ImmutableList.of(
                        new IspNodeInfo(
                            _ispAsn,
                            _ispName,
                            Role.TRANSIT,
                            ImmutableList.of(),
                            IspTrafficFiltering.none()),
                        new IspNodeInfo(
                            ispAsn2,
                            ispName2,
                            Role.PRIVATE_BACKBONE,
                            ImmutableList.of(),
                            IspTrafficFiltering.none())),
                    ImmutableList.of())),
            new BatfishLogger("output", false),
            new Warnings());

    // internet node exists and connects to only one ISP
    assertTrue(modeledNodes.getConfigurations().containsKey(INTERNET_HOST_NAME));
    Configuration internetNode = modeledNodes.getConfigurations().get(INTERNET_HOST_NAME);
    assertThat(
        internetNode.getAllInterfaces().keySet(),
        equalTo(ImmutableSet.of(internetToIspInterfaceName(_ispName), INTERNET_OUT_INTERFACE)));

    // ISP1 connects to the snapshot and to the Internet
    assertTrue(modeledNodes.getConfigurations().containsKey(_ispName));
    Configuration ispNode1 = modeledNodes.getConfigurations().get(_ispName);
    assertThat(
        ispNode1.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(
                ISP_TO_INTERNET_INTERFACE_NAME,
                ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName))));

    // ISP2 connects only to the snapshot
    assertTrue(modeledNodes.getConfigurations().containsKey(ispName2));
    Configuration ispNode2 = modeledNodes.getConfigurations().get(ispName2);
    assertThat(
        ispNode2.getAllInterfaces().keySet(),
        equalTo(
            ImmutableSet.of(ispToSnapshotInterfaceName(_snapshotHostname, snapshotInterface2))));
  }

  /** Test that ISP peerings are processed */
  @Test
  public void testGetInternetAndIspNodes_ispPeerings() {
    // add another interface and peer to the configuration we already have
    String snapshotInterface2 = "interface2";
    Ip snapshotIp2 = Ip.parse("3.3.3.3");
    Ip ispIp2 = Ip.parse("4.4.4.4");
    long ispAsn2 = 4L;
    String ispName2 = getDefaultIspNodeName(ispAsn2);
    _nf.interfaceBuilder()
        .setName(snapshotInterface2)
        .setOwner(_snapshotHost)
        .setAddress(ConcreteInterfaceAddress.create(snapshotIp2, 24))
        .build();
    BgpActivePeerConfig.builder()
        .setPeerAddress(ispIp2)
        .setRemoteAs(ispAsn2)
        .setLocalIp(snapshotIp2)
        .setLocalAs(_snapshotAsn)
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
        .setBgpProcess(_snapshotHost.getDefaultVrf().getBgpProcess())
        .build();

    ModeledNodes modeledNodes =
        getInternetAndIspNodes(
            ImmutableMap.of(_snapshotHostname, _snapshotHost),
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, _snapshotInterfaceName)),
                        new BorderInterfaceInfo(
                            NodeInterfacePair.of(_snapshotHostname, snapshotInterface2))),
                    ImmutableList.of(),
                    IspFilter.ALLOW_ALL,
                    ImmutableList.of(),
                    ImmutableList.of(new IspPeeringInfo(new Peer(_ispAsn), new Peer(ispAsn2))))),
            new BatfishLogger("output", false),
            new Warnings());

    // Having a layer1 edge means that ISP peering was processed
    // Tests of the helper function checks if the peering is properly created
    assertThat(
        modeledNodes.getLayer1Edges(),
        hasItem(
            new Layer1Edge(
                new Layer1Node(_ispName, ispPeeringInterfaceName(ispName2)),
                new Layer1Node(ispName2, ispPeeringInterfaceName(_ispName)))));
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
                ispToSnapshotInterfaceName(_snapshotHostname, _snapshotInterfaceName),
                ispToSnapshotInterfaceName(configuration2.getHostname(), "interface2"),
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
                    .setSnapshotConnections(
                        _snapshotConnection,
                        new SnapshotConnection(
                            ImmutableList.of(
                                new IspInterface(
                                    ispToSnapshotInterfaceName(
                                        c2.getHostname(), _snapshotInterfaceName),
                                    ConcreteInterfaceAddress.create(ispIp2, 24),
                                    new Layer1Node(c2.getHostname(), _snapshotInterfaceName),
                                    null)),
                            IspBgpActivePeer.create(
                                getOnlyElement(
                                    c2.getDefaultVrf()
                                        .getBgpProcess()
                                        .getActiveNeighbors()
                                        .values()))))
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

  @Test
  public void testCombineIspPeerings() {
    Map<Long, IspModel> ispModels = ImmutableMap.of(1L, _ispModel, 2L, _ispModel, 3L, _ispModel);
    Set<IspPeering> ispPeerings =
        combineIspPeerings(
            ImmutableList.of(
                new IspConfiguration(
                    ImmutableList.of(),
                    ImmutableList.of(),
                    IspFilter.ALLOW_ALL,
                    ImmutableList.of(),
                    ImmutableList.of(
                        new IspPeeringInfo(new Peer(1L), new Peer(2L)),
                        new IspPeeringInfo(new Peer(1L), new Peer(3L)))),
                new IspConfiguration(
                    ImmutableList.of(),
                    ImmutableList.of(),
                    IspFilter.ALLOW_ALL,
                    ImmutableList.of(),
                    ImmutableList.of(new IspPeeringInfo(new Peer(2L), new Peer(1L))))), // duplicate
            ispModels,
            new Warnings());

    assertThat(ispPeerings, equalTo(ImmutableSet.of(new IspPeering(1, 2), new IspPeering(1, 3))));
  }

  @Test
  public void testCombineIspPeerings_missingAsns() {
    Map<Long, IspModel> ispModels = ImmutableMap.of(_ispAsn, _ispModel);
    {
      // missing asn1
      Warnings warnings = new Warnings(true, true, true);
      combineIspPeerings(
          ImmutableList.of(
              new IspConfiguration(
                  ImmutableList.of(),
                  ImmutableList.of(),
                  IspFilter.ALLOW_ALL,
                  ImmutableList.of(),
                  ImmutableList.of(new IspPeeringInfo(new Peer(10L), new Peer(_ispAsn))))),
          ispModels,
          warnings);
      assertThat(
          warnings,
          hasRedFlag(
              hasText("ISP Modeling: Could not find ISP with ASN 10, specified for ISP peering")));
    }
    {
      // missing asn2
      Warnings warnings = new Warnings(true, true, true);
      combineIspPeerings(
          ImmutableList.of(
              new IspConfiguration(
                  ImmutableList.of(),
                  ImmutableList.of(),
                  IspFilter.ALLOW_ALL,
                  ImmutableList.of(),
                  ImmutableList.of(new IspPeeringInfo(new Peer(_ispAsn), new Peer(20))))),
          ispModels,
          warnings);
      assertThat(
          warnings,
          hasRedFlag(
              hasText("ISP Modeling: Could not find ISP with ASN 20, specified for ISP peering")));
    }
  }
}
