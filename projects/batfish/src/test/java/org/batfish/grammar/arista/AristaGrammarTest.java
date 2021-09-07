package org.batfish.grammar.arista;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.Ip.ZERO;
import static org.batfish.datamodel.Names.generatedBgpPeerEvpnExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasAllowRemoteAsOut;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasSendCommunity;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasSendExtendedCommunity;
import static org.batfish.datamodel.matchers.AddressFamilyMatchers.hasAddressFamilyCapabilites;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasIpv4UnicastAddressFamily;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEquivalentAsPathMatchMode;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasPassiveNeighbor;
import static org.batfish.datamodel.matchers.BgpRouteMatchers.hasCommunities;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasMlagConfig;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNoUndefinedReferences;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRedFlagWarning;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasEncapsulationVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMlagId;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVrf;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isSwitchport;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasLines;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.MlagMatchers.hasId;
import static org.batfish.datamodel.matchers.MlagMatchers.hasPeerAddress;
import static org.batfish.datamodel.matchers.MlagMatchers.hasPeerInterface;
import static org.batfish.datamodel.matchers.VniSettingsMatchers.hasBumTransportIps;
import static org.batfish.datamodel.matchers.VniSettingsMatchers.hasBumTransportMethod;
import static org.batfish.datamodel.matchers.VniSettingsMatchers.hasSourceAddress;
import static org.batfish.datamodel.matchers.VniSettingsMatchers.hasUdpPort;
import static org.batfish.datamodel.matchers.VniSettingsMatchers.hasVlan;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasL2VniSettings;
import static org.batfish.datamodel.matchers.VrfMatchers.hasName;
import static org.batfish.datamodel.routing_policy.Common.SUMMARY_ONLY_SUPPRESSION_POLICY_NAME;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.arista.AristaConfiguration.DEFAULT_LOCAL_BGP_WEIGHT;
import static org.batfish.representation.arista.AristaStructureType.INTERFACE;
import static org.batfish.representation.arista.AristaStructureType.POLICY_MAP;
import static org.batfish.representation.arista.AristaStructureType.VXLAN;
import static org.batfish.representation.arista.Conversions.nameOfSourceNatIpSpaceFromAcl;
import static org.batfish.representation.arista.OspfProcess.getReferenceOspfBandwidth;
import static org.batfish.representation.arista.eos.AristaBgpProcess.DEFAULT_VRF;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF_EXTERNAL;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF_INTERNAL;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL_TYPE_1;
import static org.batfish.representation.arista.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL_TYPE_2;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.graph.ValueGraph;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.matchers.WarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.OspfExternalType2Route;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Layer2VniConfig;
import org.batfish.datamodel.bgp.Layer3VniConfig;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.flow.TraceAndReverseFlow;
import org.batfish.datamodel.matchers.ConfigurationMatchers;
import org.batfish.datamodel.matchers.MlagMatchers;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunityContext;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExprEvaluator;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprEvaluator;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.arista.AristaConfiguration;
import org.batfish.representation.arista.AristaStaticSourceNat;
import org.batfish.representation.arista.AristaStaticSourceNat.Protocol;
import org.batfish.representation.arista.ExpandedCommunityList;
import org.batfish.representation.arista.ExpandedCommunityListLine;
import org.batfish.representation.arista.IpAsPathAccessList;
import org.batfish.representation.arista.IpAsPathAccessListLine;
import org.batfish.representation.arista.MlagConfiguration;
import org.batfish.representation.arista.PrefixList;
import org.batfish.representation.arista.PrefixListLine;
import org.batfish.representation.arista.RouteMap;
import org.batfish.representation.arista.RouteMapClause;
import org.batfish.representation.arista.RouteMapMatchCommunity;
import org.batfish.representation.arista.RouteMapSetCommunity;
import org.batfish.representation.arista.RouteMapSetCommunityDelete;
import org.batfish.representation.arista.RouteMapSetCommunityList;
import org.batfish.representation.arista.RouteMapSetCommunityListDelete;
import org.batfish.representation.arista.RouteMapSetCommunityNone;
import org.batfish.representation.arista.StandardAccessList;
import org.batfish.representation.arista.StandardAccessListActionLine;
import org.batfish.representation.arista.StandardAccessListRemarkLine;
import org.batfish.representation.arista.StandardCommunityList;
import org.batfish.representation.arista.StandardCommunityListLine;
import org.batfish.representation.arista.VrrpInterface;
import org.batfish.representation.arista.eos.AristaBgpAggregateNetwork;
import org.batfish.representation.arista.eos.AristaBgpBestpathTieBreaker;
import org.batfish.representation.arista.eos.AristaBgpDefaultOriginate;
import org.batfish.representation.arista.eos.AristaBgpNeighbor.RemovePrivateAsMode;
import org.batfish.representation.arista.eos.AristaBgpNeighborAddressFamily;
import org.batfish.representation.arista.eos.AristaBgpNetworkConfiguration;
import org.batfish.representation.arista.eos.AristaBgpPeerGroupNeighbor;
import org.batfish.representation.arista.eos.AristaBgpRedistributionPolicy;
import org.batfish.representation.arista.eos.AristaBgpV4DynamicNeighbor;
import org.batfish.representation.arista.eos.AristaBgpV4Neighbor;
import org.batfish.representation.arista.eos.AristaBgpVlan;
import org.batfish.representation.arista.eos.AristaBgpVlanAwareBundle;
import org.batfish.representation.arista.eos.AristaBgpVrf;
import org.batfish.representation.arista.eos.AristaBgpVrfEvpnAddressFamily;
import org.batfish.representation.arista.eos.AristaBgpVrfIpv4UnicastAddressFamily;
import org.batfish.representation.arista.eos.AristaBgpVrfIpv6UnicastAddressFamily;
import org.batfish.representation.arista.eos.AristaEosVxlan;
import org.batfish.representation.arista.eos.AristaRedistributeType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class AristaGrammarTest {
  private static final String DEFAULT_VRF_NAME = "default";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/arista/testconfigs/";
  private static final String TESTRIGS_PREFIX = "org/batfish/grammar/arista/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull AristaConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    AristaCombinedParser parser = new AristaCombinedParser(src, settings);
    AristaControlPlaneExtractor extractor =
        new AristaControlPlaneExtractor(
            src, parser, ARISTA, new Warnings(), new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    AristaConfiguration vendorConfiguration =
        (AristaConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      String canonicalHostname = hostname.toLowerCase();
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
      assertThat(c, hasConfigurationFormat(ARISTA));
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  /** Tests out-of-order lines, remarks, auto-numbering first and later lines. */
  @Test
  public void testStandardAccessList() {
    AristaConfiguration c = parseVendorConfig("standard-access-list");
    assertThat(c.getStandardAcls(), hasKeys("LIST1"));
    StandardAccessList list = c.getStandardAcls().get("LIST1");
    assertThat(
        list.getLines().values(),
        contains(
            new StandardAccessListActionLine(
                5, LineAction.PERMIT, "5 permit host 1.2.3.4", IpWildcard.parse("1.2.3.4")),
            new StandardAccessListRemarkLine(10, "hey there"),
            new StandardAccessListActionLine(
                15,
                LineAction.PERMIT,
                "15 permit 1.2.3.4 255.255.0.255",
                IpWildcard.parse("1.2.0.4:0.0.255.0")),
            new StandardAccessListActionLine(30, LineAction.DENY, "30 deny any", IpWildcard.ANY),
            new StandardAccessListRemarkLine(40, "last line")));
  }

  @Test
  public void testTopLevelBgpExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp");
    // Basic VRF config
    {
      AristaBgpVrf defaultVrf = config.getAristaBgp().getDefaultVrf();
      assertThat(defaultVrf.getAdvertiseInactive(), equalTo(Boolean.TRUE));
      assertTrue(defaultVrf.getShutdown());
      assertThat(defaultVrf.getRouterId(), equalTo(Ip.parse("1.2.3.4")));
      assertThat(defaultVrf.getKeepAliveTimer(), equalTo(3));
      assertThat(defaultVrf.getHoldTimer(), equalTo(9));
      assertThat(defaultVrf.getEbgpAdminDistance(), equalTo(300));
      assertThat(defaultVrf.getIbgpAdminDistance(), nullValue());
      assertThat(defaultVrf.getLocalAdminDistance(), nullValue());
      assertThat(defaultVrf.getDefaultMetric(), equalTo(100L));
      assertFalse(defaultVrf.getDefaultIpv4Unicast());
      assertThat(defaultVrf.getMaxPaths(), equalTo(2));
      assertThat(defaultVrf.getMaxPathsEcmp(), nullValue());
    }
    {
      String vrfName = "tenant_vrf";
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get(vrfName);
      assertThat(vrf.getAdvertiseInactive(), nullValue());
      assertThat(vrf.getShutdown(), nullValue());
      assertThat(vrf.getRouterId(), equalTo(Ip.parse("5.6.7.8")));
      assertThat(vrf.getKeepAliveTimer(), equalTo(6));
      assertThat(vrf.getHoldTimer(), equalTo(18));
      assertThat(vrf.getEbgpAdminDistance(), equalTo(333));
      assertThat(vrf.getIbgpAdminDistance(), equalTo(400));
      assertThat(vrf.getLocalAdminDistance(), equalTo(500));
      assertTrue(vrf.getDefaultIpv4Unicast());
      assertThat(vrf.getMaxPaths(), equalTo(3));
      assertThat(vrf.getMaxPathsEcmp(), equalTo(2));
    }
    {
      String vrfName = "tenant2_vrf";
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get(vrfName);
      assertThat(vrf.getAdvertiseInactive(), nullValue());
      assertTrue(vrf.getShutdown());
      assertThat(vrf.getRouterId(), nullValue());
    }
  }

  @Test
  public void testAristaOspfReferenceBandwidth() throws IOException {
    Configuration manual = parseConfig("aristaOspfCost");
    assertThat(
        manual.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(), equalTo(3e6d));

    Configuration defaults = parseConfig("aristaOspfCostDefaults");
    assertThat(
        defaults.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(),
        equalTo(getReferenceOspfBandwidth()));
  }

  @Test
  public void testAristaVsIosOspfRedistributeBgpSyntax() throws IOException {
    // Both an Arista and an IOS config contain these "redistribute bgp" lines in their OSPF setups:
    String l1 = "redistribute bgp";
    String l2 = "redistribute bgp route-map MAP";
    String l3 = "redistribute bgp 65100";
    String l4 = "redistribute bgp 65100 route-map MAP";
    String l5 = "redistribute bgp 65100 metric 10";
    /*
    Arista does not allow specification of an AS, while IOS requires it, and IOS allows setting
    metric and a few other settings where Arista does not (both allow route-maps). So:
    - First two lines should generate warnings on IOS but not Arista
    - Remaining lines should generate warnings on Arista but not IOS
    */
    String testrigName = "arista-vs-ios-warnings";
    String iosName = "ios-ospf-redistribute-bgp";
    String aristaName = "arista-ospf-redistribute-bgp";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    TESTRIGS_PREFIX + testrigName, ImmutableList.of(aristaName, iosName))
                .build(),
            _folder);
    batfish.getSettings().setDisableUnrecognized(false);
    ParseVendorConfigurationAnswerElement pvcae =
        batfish.loadParseVendorConfigurationAnswerElement(batfish.getSnapshot());
    assertThat(
        pvcae.getWarnings().get("configs/" + iosName).getParseWarnings(),
        contains(
            allOf(hasComment("This syntax is unrecognized"), hasText(containsString(l1))),
            allOf(hasComment("This syntax is unrecognized"), hasText(containsString(l2)))));
    assertThat(
        pvcae.getWarnings().get("configs/" + aristaName).getParseWarnings(),
        contains(
            allOf(hasComment("This syntax is unrecognized"), hasText(containsString(l3))),
            allOf(hasComment("This syntax is unrecognized"), hasText(containsString(l4))),
            allOf(hasComment("This syntax is unrecognized"), hasText(containsString(l5)))));
  }

  @Test
  public void testAristaBgpDefaultOriginatePolicy() throws IOException {
    /*
      Snapshot contains two devices, arista-originator and ios-listener, configured with
      multiple BGP peerings between them. The peers in ios-listener are all in separate VRFs
      so that it is clear which peer each BGP route came from.

      All peers on the originator use an export route-map that denies everything, to prove
      that the default-originate route is not affected by the export route-map.

      There is another route-map on the originator, SET_RM, that prepends AS 1234 and adds
      communities 111:222 and 333:444.

      Peer 1 should export a default route with SET_RM's AS and communities:
        neighbor 10.1.1.2 default-originate route-map SET_RM
        neighbor 10.1.1.2 send-community

      Peer 2 should export a default route with SET_RM's AS, but not its communities:
        neighbor 10.2.2.2 default-originate route-map SET_RM

      Peer 3 should export a regular default route:
        neighbor 10.3.3.2 default-originate route-map UNDEFINED

      Peer 4 should also export a regular default route:
        neighbor 10.4.4.2 default-originate
    */
    String testrigName = "arista-bgp-default-originate";
    List<String> configurationNames = ImmutableList.of("arista-originator", "ios-listener");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    DataPlane dp = batfish.loadDataPlane(snapshot);

    // The route that each listener peer will receive if no attributes are modified
    // (except missing peer-specific nextHopIp and receivedFromIp)
    Bgpv4Route defaultOriginateRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.BGP)
            .setAsPath(AsPath.ofSingletonAsSets(1L))
            .setAdmin(20)
            .setLocalPreference(100)
            .build();
    {
      // Peer 1 (sets default-originate route-map SET_RM and configures send-community)
      Set<AbstractRoute> listenerRoutes = dp.getRibs().get("ios-listener").get("VRF1").getRoutes();
      Ip peerIp = Ip.parse("10.1.1.1"); // local IP of originating peer
      Bgpv4Route expectedRoute =
          defaultOriginateRoute.toBuilder()
              .setAsPath(AsPath.ofSingletonAsSets(1L, 1234L))
              .setCommunities(
                  ImmutableSet.of(StandardCommunity.of(111, 222), StandardCommunity.of(333, 444)))
              .setNextHopIp(peerIp)
              .setReceivedFromIp(peerIp)
              .build();
      assertThat(listenerRoutes, hasItem(equalTo(expectedRoute)));
    }
    {
      // Peer 2 (sets default-originate route-map SET_RM, does not configure send-community)
      Set<AbstractRoute> listenerRoutes = dp.getRibs().get("ios-listener").get("VRF2").getRoutes();
      Ip peerIp = Ip.parse("10.2.2.1"); // local IP of originating peer
      Bgpv4Route expectedRoute =
          defaultOriginateRoute.toBuilder()
              .setAsPath(AsPath.ofSingletonAsSets(1L, 1234L))
              .setNextHopIp(peerIp)
              .setReceivedFromIp(peerIp)
              .build();
      assertThat(listenerRoutes, hasItem(equalTo(expectedRoute)));
    }
    {
      // Peer 3 (sets default-originate route-map UNDEFINED)
      Set<AbstractRoute> listenerRoutes = dp.getRibs().get("ios-listener").get("VRF3").getRoutes();
      Ip peerIp = Ip.parse("10.3.3.1"); // local IP of originating peer
      Bgpv4Route expectedRoute =
          defaultOriginateRoute.toBuilder().setNextHopIp(peerIp).setReceivedFromIp(peerIp).build();
      assertThat(listenerRoutes, hasItem(equalTo(expectedRoute)));
    }
    {
      // Peer 4 (sets default-originate without a route-map)
      Set<AbstractRoute> listenerRoutes = dp.getRibs().get("ios-listener").get("VRF4").getRoutes();
      Ip peerIp = Ip.parse("10.4.4.1"); // local IP of originating peer
      Bgpv4Route expectedRoute =
          defaultOriginateRoute.toBuilder().setNextHopIp(peerIp).setReceivedFromIp(peerIp).build();
      assertThat(listenerRoutes, hasItem(equalTo(expectedRoute)));
    }
  }

  @Test
  public void testBgpLocalAs() throws IOException {
    String testrigName = "arista-bgp-local-as";
    List<String> configurationNames = ImmutableList.of("r1", "r2");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations(batfish.getSnapshot());
    assertTrue(
        configurations.values().stream().allMatch(c -> c.getConfigurationFormat() == ARISTA));
    Map<Ip, Map<String, Set<String>>> ipOwners =
        batfish.getTopologyProvider().getIpOwners(batfish.getSnapshot()).getIpVrfOwners();
    ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology =
        BgpTopologyUtils.initBgpTopology(configurations, ipOwners, false, null).getGraph();

    // Edge one direction
    assertThat(
        bgpTopology
            .adjacentNodes(
                new BgpPeerConfigId("r1", DEFAULT_VRF_NAME, Prefix.parse("1.2.0.2/32"), false))
            .iterator()
            .next(),
        equalTo(new BgpPeerConfigId("r2", DEFAULT_VRF_NAME, Prefix.parse("1.2.0.1/32"), false)));

    // Edge the other direction
    assertThat(
        bgpTopology
            .adjacentNodes(
                new BgpPeerConfigId("r2", DEFAULT_VRF_NAME, Prefix.parse("1.2.0.1/32"), false))
            .iterator()
            .next(),
        equalTo(new BgpPeerConfigId("r1", DEFAULT_VRF_NAME, Prefix.parse("1.2.0.2/32"), false)));
  }

  @Test
  public void testBgpMultipathRelax() throws IOException {
    String testrigName = "arista-bgp-multipath-relax";
    List<String> configurationNames = ImmutableList.of("arista_disabled", "arista_enabled");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);
    Map<String, Configuration> configurations = batfish.loadConfigurations(batfish.getSnapshot());
    org.batfish.datamodel.BgpProcess aristaDisabled =
        configurations.get("arista_disabled").getDefaultVrf().getBgpProcess();
    org.batfish.datamodel.BgpProcess aristaEnabled =
        configurations.get("arista_enabled").getDefaultVrf().getBgpProcess();

    assertThat(
        aristaDisabled,
        hasMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.EXACT_PATH));
    assertThat(
        aristaEnabled,
        hasMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.PATH_LENGTH));

    assertThat(aristaDisabled, hasMultipathEbgp(false));
    assertThat(aristaEnabled, hasMultipathEbgp(false));
  }

  @Test
  public void testBgpRedistribution() throws IOException {
    /*
     * Config contains two VRFs:
     * - VRF1 has static route 1.1.1.1/32 and redistributes static into BGP unconditionally
     * - VRF2 has static routes 1.1.1.1/32 and 2.2.2.2/32, and redistributes static into BGP with a
     *   route-map that only permits 1.1.1.1/32
     * - Both VRFs' BGP RIBs should contain 1.1.1.1/32 as a local route.
     */
    String hostname = "bgp_redistribution";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    Prefix staticPrefix1 = Prefix.parse("1.1.1.1/32");
    Prefix staticPrefix2 = Prefix.parse("2.2.2.2/32");

    // Sanity check: Both VRFs' main RIBs should contain the static route to 1.1.1.1/32,
    // and VRF2 should also have 2.2.2.2/32
    Set<AnnotatedRoute<AbstractRoute>> vrf1Routes =
        dp.getRibs().get(hostname).get("VRF1").getTypedRoutes();
    Set<AnnotatedRoute<AbstractRoute>> vrf2Routes =
        dp.getRibs().get(hostname).get("VRF2").getTypedRoutes();
    assertThat(
        vrf1Routes, contains(allOf(hasPrefix(staticPrefix1), hasProtocol(RoutingProtocol.STATIC))));
    assertThat(
        vrf2Routes,
        contains(
            allOf(hasPrefix(staticPrefix1), hasProtocol(RoutingProtocol.STATIC)),
            allOf(hasPrefix(staticPrefix2), hasProtocol(RoutingProtocol.STATIC))));

    // Both VRFs should have 1.1.1.1/32 in BGP (and not 2.2.2.2/32)
    int bgpAdmin =
        batfish
            .loadConfigurations(batfish.getSnapshot())
            .get(hostname)
            .getVrfs()
            .get("VRF1")
            .getBgpProcess()
            .getAdminCost(RoutingProtocol.BGP);
    Bgpv4Route bgpRouteVrf1 =
        Bgpv4Route.builder()
            .setNetwork(staticPrefix1)
            .setNonRouting(true)
            .setAdmin(bgpAdmin)
            .setLocalPreference(0)
            .setNextHop(NextHopDiscard.instance())
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.parse("10.10.10.1"))
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(ZERO) // indicates local origination
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setWeight(DEFAULT_LOCAL_BGP_WEIGHT)
            .build();
    Bgpv4Route bgpRouteVrf2 =
        bgpRouteVrf1.toBuilder().setOriginatorIp(Ip.parse("10.10.10.2")).build();
    Set<Bgpv4Route> vrf1BgpRoutes = dp.getBgpRoutes().get(hostname, "VRF1");
    Set<Bgpv4Route> vrf2BgpRoutes = dp.getBgpRoutes().get(hostname, "VRF2");
    assertThat(vrf1BgpRoutes, contains(bgpRouteVrf1));
    assertThat(vrf2BgpRoutes, contains(bgpRouteVrf2));
  }

  @Test
  public void testBgpRemovePrivateAs() throws IOException {
    String testrigName = "arista-bgp-remove-private-as";
    List<String> configurationNames = ImmutableList.of("r1", "r2", "r3");

    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + testrigName, configurationNames)
                .build(),
            _folder);

    // Check that 1.1.1.1/32 appears on r3
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    assertTrue(
        batfish.loadConfigurations(snapshot).values().stream()
            .allMatch(c -> c.getConfigurationFormat() == ARISTA));
    DataPlane dp = batfish.loadDataPlane(snapshot);
    SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs =
        dp.getRibs();
    Set<AbstractRoute> r3Routes = ribs.get("r3").get(DEFAULT_VRF_NAME).getRoutes();
    Set<Prefix> r3Prefixes =
        r3Routes.stream().map(AbstractRoute::getNetwork).collect(Collectors.toSet());
    Prefix r1Loopback = Prefix.parse("1.1.1.1/32");
    assertTrue(r3Prefixes.contains(r1Loopback));

    // check that private AS is present in path in received 1.1.1.1/32 advert on r2
    Set<AbstractRoute> r2Routes = ribs.get("r2").get(DEFAULT_VRF_NAME).getRoutes();
    boolean r2HasPrivate =
        r2Routes.stream()
            .filter(r -> r.getNetwork().equals(r1Loopback))
            .flatMap(r -> ((Bgpv4Route) r).getAsPath().getAsSets().stream())
            .flatMap(asSet -> asSet.getAsns().stream())
            .anyMatch(AsPath::isPrivateAs);
    assertTrue(r2HasPrivate);

    // check that private AS is absent from path in received 1.1.1.1/32 advert on r3
    boolean r3HasPrivate =
        r3Routes.stream()
            .filter(a -> a.getNetwork().equals(r1Loopback))
            .flatMap(r -> ((Bgpv4Route) r).getAsPath().getAsSets().stream())
            .flatMap(asSet -> asSet.getAsns().stream())
            .anyMatch(AsPath::isPrivateAs);
    assertFalse(r3HasPrivate);
  }

  @Test
  public void testAristaAdvertiseInactive() throws IOException {
    Configuration config = parseConfig("arista-advertise-inactive");
    assertTrue(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("1.1.1.1"))
            .getIpv4UnicastAddressFamily()
            .getAddressFamilyCapabilities()
            .getAdvertiseInactive());
  }

  @Test
  public void testAristaNoAdvertiseInactive() throws IOException {
    Configuration config = parseConfig("arista-no-advertise-inactive");
    assertFalse(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("1.1.1.1"))
            .getIpv4UnicastAddressFamily()
            .getAddressFamilyCapabilities()
            .getAdvertiseInactive());
  }

  /**
   * Ensure that Arista redistributes a static default route even though default-originate is not
   * explicitly specified in the config
   */
  @Test
  public void testAristaDefaultRouteRedistribution() throws IOException {
    final String receiver = "ios_receiver";
    final String advertiser = "arista_advertiser";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    TESTRIGS_PREFIX + "arista-redistribute-default-route",
                    ImmutableList.of(advertiser, receiver))
                .build(),
            _folder);

    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    Set<AbstractRoute> routes =
        batfish.loadDataPlane(snapshot).getRibs().get(receiver).get(DEFAULT_VRF_NAME).getRoutes();

    assertThat(
        routes,
        hasItem(
            OspfExternalType2Route.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopInterface.of("GigabitEthernet1/0", Ip.parse("1.2.3.5")))
                .setArea(1)
                .setCostToAdvertiser(1)
                .setAdvertiser(advertiser)
                .setAdmin(110)
                .setMetric(1)
                .setLsaMetric(1)
                .build()));
  }

  @Test
  public void testAristaDefaultSwitchPort() throws IOException {
    Configuration cUndeclared = parseConfig("eos_switchport_default_mode_undeclared");
    Configuration cDefaultAccess = parseConfig("eos_switchport_default_mode_access");
    Configuration cDefaultRouted = parseConfig("eos_switchport_default_mode_routed");

    String l0Name = "Loopback0";
    String e0Name = "Ethernet0/0";
    String e1Name = "Ethernet0/1";
    String e2Name = "Ethernet0/2";
    String e3Name = "Ethernet0/3";
    String e4Name = "Ethernet0/4";
    String p0Name = "Port-Channel0";

    Interface l0Undeclared = cUndeclared.getAllInterfaces().get(l0Name);
    Interface e0Undeclared = cUndeclared.getAllInterfaces().get(e0Name);
    Interface e1Undeclared = cUndeclared.getAllInterfaces().get(e1Name);
    Interface e2Undeclared = cUndeclared.getAllInterfaces().get(e2Name);
    Interface e3Undeclared = cUndeclared.getAllInterfaces().get(e3Name);
    Interface e4Undeclared = cUndeclared.getAllInterfaces().get(e4Name);
    Interface p0Undeclared = cUndeclared.getAllInterfaces().get(p0Name);

    Interface l0DefaultAccess = cDefaultAccess.getAllInterfaces().get(l0Name);
    Interface e0DefaultAccess = cDefaultAccess.getAllInterfaces().get(e0Name);
    Interface e1DefaultAccess = cDefaultAccess.getAllInterfaces().get(e1Name);
    Interface e2DefaultAccess = cDefaultAccess.getAllInterfaces().get(e2Name);
    Interface e3DefaultAccess = cDefaultAccess.getAllInterfaces().get(e3Name);
    Interface e4DefaultAccess = cDefaultAccess.getAllInterfaces().get(e4Name);
    Interface p0DefaultAccess = cDefaultAccess.getAllInterfaces().get(p0Name);

    Interface l0DefaultRouted = cDefaultRouted.getAllInterfaces().get(l0Name);
    Interface e0DefaultRouted = cDefaultRouted.getAllInterfaces().get(e0Name);
    Interface e1DefaultRouted = cDefaultRouted.getAllInterfaces().get(e1Name);
    Interface e2DefaultRouted = cDefaultRouted.getAllInterfaces().get(e2Name);
    Interface e3DefaultRouted = cDefaultRouted.getAllInterfaces().get(e3Name);
    Interface e4DefaultRouted = cDefaultRouted.getAllInterfaces().get(e4Name);
    Interface p0DefaultRouted = cDefaultRouted.getAllInterfaces().get(p0Name);

    assertThat(l0Undeclared, isSwitchport(false));
    assertThat(l0Undeclared, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e0Undeclared, isSwitchport(true));
    assertThat(e0Undeclared, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e1Undeclared, isSwitchport(true));
    assertThat(e1Undeclared, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e2Undeclared, isSwitchport(true));
    assertThat(e2Undeclared, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e3Undeclared, isSwitchport(false));
    assertThat(e3Undeclared, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e4Undeclared, isSwitchport(true));
    assertThat(e4Undeclared, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(p0Undeclared, isSwitchport(true));
    assertThat(p0Undeclared, hasSwitchPortMode(SwitchportMode.ACCESS));

    assertThat(l0DefaultAccess, isSwitchport(false));
    assertThat(l0DefaultAccess, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e0DefaultAccess, isSwitchport(true));
    assertThat(e0DefaultAccess, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e1DefaultAccess, isSwitchport(true));
    assertThat(e1DefaultAccess, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e2DefaultAccess, isSwitchport(true));
    assertThat(e2DefaultAccess, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e3DefaultAccess, isSwitchport(false));
    assertThat(e3DefaultAccess, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e4DefaultAccess, isSwitchport(true));
    assertThat(e4DefaultAccess, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(p0DefaultAccess, isSwitchport(true));
    assertThat(p0DefaultAccess, hasSwitchPortMode(SwitchportMode.ACCESS));

    assertThat(l0DefaultRouted, isSwitchport(false));
    assertThat(l0DefaultRouted, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e0DefaultRouted, isSwitchport(false));
    assertThat(e0DefaultRouted, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e1DefaultRouted, isSwitchport(true));
    assertThat(e1DefaultRouted, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e2DefaultRouted, isSwitchport(true));
    assertThat(e2DefaultRouted, hasSwitchPortMode(SwitchportMode.ACCESS));
    assertThat(e3DefaultRouted, isSwitchport(false));
    assertThat(e3DefaultRouted, hasSwitchPortMode(SwitchportMode.NONE));
    assertThat(e4DefaultRouted, isSwitchport(true));
    assertThat(e4DefaultRouted, hasSwitchPortMode(SwitchportMode.TRUNK));
    assertThat(p0DefaultRouted, isSwitchport(false));
    assertThat(p0DefaultRouted, hasSwitchPortMode(SwitchportMode.NONE));
  }

  @Test
  public void testAristaDynamicSourceNat() throws IOException {
    Configuration c = parseConfig("arista-dynamic-source-nat");
    Interface iface = c.getAllInterfaces().get("Ethernet1");
    assertThat(iface.getIncomingTransformation(), nullValue());
    assertThat(
        iface.getOutgoingTransformation(),
        equalTo(
            when(permittedByAcl("acl1"))
                .apply(assignSourceIp(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2")))
                .build()));

    iface = c.getAllInterfaces().get("Ethernet2");
    assertThat(iface.getIncomingTransformation(), nullValue());
    assertThat(
        iface.getOutgoingTransformation(),
        equalTo(
            when(permittedByAcl("acl1"))
                .apply(assignSourceIp(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2")))
                .setOrElse(
                    when(permittedByAcl("acl2"))
                        .apply(assignSourceIp(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3")))
                        .build())
                .build()));

    iface = c.getAllInterfaces().get("Ethernet3");
    assertThat(iface.getIncomingTransformation(), nullValue());
    assertThat(
        iface.getOutgoingTransformation(),
        equalTo(
            when(permittedByAcl("acl1"))
                // due to the subnet mask, shift the pool IPs to avoid network/bcast IPs.
                .apply(assignSourceIp(Ip.parse("2.0.0.1"), Ip.parse("2.0.0.6")))
                .build()));

    iface = c.getAllInterfaces().get("Ethernet4");
    assertThat(iface.getIncomingTransformation(), nullValue());
    assertThat(
        iface.getOutgoingTransformation(),
        equalTo(
            when(permittedByAcl("acl1"))
                // overload rule, so use the interface IP
                .apply(assignSourceIp(Ip.parse("8.8.8.8"), Ip.parse("8.8.8.8")))
                .build()));
  }

  /**
   * Test that static source NAT works as expected. A flow originates from r1[loopback0] with srcIp
   * 8.8.8.8, gets routed towards r2[Ethernet1] via connected route, is src-natted to 9.9.9.9 on the
   * way out. It should be accepted at r2[Ethernet1].
   *
   * <p>The reverse flow should start out from 1.0.0.1 -> 9.9.9.9, be dest-natted on the way back
   * in, rewritten to 8.8.8.8 at r1[Ethernet1], and then accepted.
   */
  @Test
  public void testStaticSourceNatE2e() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    TESTRIGS_PREFIX + "nat-source-static", ImmutableList.of("r1", "r2"))
                .build(),
            _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    assertThat(batfish.loadConfigurations(snapshot), hasKeys("r1", "r2"));

    batfish.computeDataPlane(snapshot);

    Flow testFlow =
        Flow.builder()
            .setIngressNode("r1")
            .setIngressVrf("default")
            .setSrcIp(Ip.parse("8.8.8.8"))
            .setDstIp(Ip.parse("1.0.0.1"))
            .build();
    TraceAndReverseFlow tarf =
        getOnlyElement(
            batfish
                .getTracerouteEngine(snapshot)
                .computeTracesAndReverseFlows(ImmutableSet.of(testFlow), false)
                .get(testFlow));
    assertThat(tarf.getTrace().getDisposition(), equalTo(FlowDisposition.ACCEPTED));
    assertThat(tarf.getReverseFlow(), hasDstIp(Ip.parse("9.9.9.9")));

    TraceAndReverseFlow reverseTarf =
        getOnlyElement(
            batfish
                .getTracerouteEngine(snapshot)
                .computeTracesAndReverseFlows(ImmutableSet.of(tarf.getReverseFlow()), false)
                .get(tarf.getReverseFlow()));
    assertThat(reverseTarf.getTrace().getDisposition(), equalTo(FlowDisposition.ACCEPTED));
    assertThat(reverseTarf.getReverseFlow(), hasSrcIp(Ip.parse("8.8.8.8")));
  }

  @Test
  public void testAristaBanner() throws IOException {
    Configuration c = parseConfig("eos_banner");
    Map<String, String> banners = c.getVendorFamily().getCisco().getBanners();
    assertThat(banners.get("login"), equalTo("Some text\nEOF not alone\n"));
    assertThat(banners.get("exec"), equalTo("here is an exec banner\n"));
    assertThat(banners.get("motd"), equalTo("A pithy quote.\n"));
  }

  @Test
  public void testArista100gfullInterface() throws IOException {
    Configuration c = parseConfig("arista100gfull");
    assertThat(
        c,
        hasInterface(
            "Ethernet1/1",
            hasAllAddresses(containsInAnyOrder(ConcreteInterfaceAddress.parse("10.20.0.3/31")))));
  }

  @Test
  public void testAristaSubinterfaceMtu() throws IOException {
    Configuration c = parseConfig("aristaInterface");

    assertThat(c, hasInterface("Ethernet3/2/1.4", hasMtu(9000)));
  }

  @Test
  public void testAggregateAddressExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_aggregate_address");
    {
      AristaBgpAggregateNetwork agg =
          config.getAristaBgp().getDefaultVrf().getV4aggregates().get(Prefix.parse("1.2.33.0/24"));
      assertTrue(agg.getAdvertiseOnly());
      assertTrue(agg.getAsSet());
      assertTrue(agg.getSummaryOnly());
      assertThat(agg.getAttributeMap(), equalTo("ATTR_MAP"));
      assertThat(agg.getMatchMap(), equalTo("MATCH_MAP"));
    }
    {
      AristaBgpAggregateNetwork agg =
          config.getAristaBgp().getDefaultVrf().getV4aggregates().get(Prefix.parse("1.2.44.0/24"));
      assertThat(agg.getAdvertiseOnly(), nullValue());
      assertThat(agg.getAsSet(), nullValue());
      assertTrue(agg.getSummaryOnly());
      assertThat(agg.getAttributeMap(), nullValue());
      assertThat(agg.getMatchMap(), nullValue());
    }
    {
      AristaBgpAggregateNetwork agg =
          config.getAristaBgp().getDefaultVrf().getV4aggregates().get(Prefix.parse("1.2.55.0/24"));
      assertThat(agg, notNullValue());
    }
    {
      AristaBgpAggregateNetwork agg =
          config
              .getAristaBgp()
              .getDefaultVrf()
              .getV6aggregates()
              .get(Prefix6.parse("2001:0db8:85a3:0000:0000:8a2e:0370::/112"));
      assertThat(agg.getAdvertiseOnly(), nullValue());
      assertThat(agg.getAsSet(), nullValue());
      assertThat(agg.getSummaryOnly(), equalTo(true));
      assertThat(agg.getAttributeMap(), nullValue());
      assertThat(agg.getMatchMap(), nullValue());
    }
    {
      AristaBgpAggregateNetwork agg =
          config
              .getAristaBgp()
              .getVrfs()
              .get("FOO")
              .getV4aggregates()
              .get(Prefix.parse("5.6.7.0/24"));
      assertTrue(agg.getAsSet());
    }
    {
      AristaBgpAggregateNetwork agg =
          config
              .getAristaBgp()
              .getVrfs()
              .get("FOO")
              .getV6aggregates()
              .get(Prefix6.parse("2001:0db8:85a3:0000:0000:8a2e:0370::/112"));
      assertThat(agg.getAdvertiseOnly(), nullValue());
      assertThat(agg.getAsSet(), nullValue());
      assertThat(agg.getSummaryOnly(), nullValue());
      assertThat(agg.getAttributeMap(), equalTo("ATTR_MAP6"));
      assertThat(agg.getMatchMap(), nullValue());
    }
  }

  @Test
  public void testAggregateAddressConversion() {
    Configuration c = parseConfig("arista_bgp_aggregate_address");

    // default vrf
    Map<Prefix, BgpAggregate> defaultVrfAggs = c.getDefaultVrf().getBgpProcess().getAggregates();
    assertThat(defaultVrfAggs, aMapWithSize(3)); // ipv6 aggregate is not converted
    assertThat(
        defaultVrfAggs,
        hasEntry(
            Prefix.parse("1.2.33.0/24"),
            // TODO: Support advertise-only, as-set, and match-map
            BgpAggregate.of(
                Prefix.parse("1.2.33.0/24"),
                SUMMARY_ONLY_SUPPRESSION_POLICY_NAME,
                null,
                "ATTR_MAP")));
    assertThat(
        defaultVrfAggs,
        hasEntry(
            Prefix.parse("1.2.44.0/24"),
            BgpAggregate.of(
                Prefix.parse("1.2.44.0/24"), SUMMARY_ONLY_SUPPRESSION_POLICY_NAME, null, null)));
    assertThat(
        defaultVrfAggs,
        hasEntry(
            Prefix.parse("1.2.55.0/24"),
            BgpAggregate.of(Prefix.parse("1.2.55.0/24"), null, null, null)));

    // vrf FOO
    Map<Prefix, BgpAggregate> vrfFooAggs = c.getVrfs().get("FOO").getBgpProcess().getAggregates();
    assertThat(vrfFooAggs, aMapWithSize(1)); // ipv6 aggregate is not converted
    assertThat(
        vrfFooAggs,
        hasEntry(
            Prefix.parse("5.6.7.0/24"),
            // TODO Support as-set
            BgpAggregate.of(Prefix.parse("5.6.7.0/24"), null, null, null)));
  }

  @Test
  public void testBgpAggregateWithLocalSuppressedRoutes() throws IOException {
    /*
     * Config has static routes:
     * - 1.1.1.0/24
     * - 2.2.2.0/24
     * - 3.0.0.0/8
     * - 4.4.4.0/24
     * - 5.5.0.0/16
     *
     * BGP is configured to unconditionally redistribute static routes,
     * and has aggregates:
     * 1.1.0.0/16 (not summary-only)
     * 2.2.0.0/16 (summary-only)
     * 3.0.0.0/16 (summary-only)
     * 4.4.0.0/16 (summary-only)
     * 4.4.4.0/31 (summary-only)
     * 5.5.0.0/16 (summary-only)
     *
     * In the BGP RIB, we should see:
     * - all local routes
     * - the 3 aggregate routes with more specific local routes:
     *   - 1.1.0/0/16
     *   - 2.2.0.0/16
     *   - 4.4.0.0/16
     *
     * In the main RIB, we should see the static routes and the 3 aggregates activated in the BGP RIB.
     */
    String hostname = "bgp_aggregate";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    int localAdmin =
        batfish
            .loadConfigurations(batfish.getSnapshot())
            .get(hostname)
            .getDefaultVrf()
            .getBgpProcess()
            .getLocalAdminCost();
    Set<Bgpv4Route> bgpRibRoutes = dp.getBgpRoutes().get(hostname, Configuration.DEFAULT_VRF_NAME);
    Ip routerId = Ip.parse("1.1.1.1");
    Prefix staticPrefix1 = Prefix.parse("1.1.1.0/24");
    Prefix staticPrefix2 = Prefix.parse("2.2.2.0/24");
    Prefix staticPrefix3 = Prefix.parse("3.0.0.0/8");
    Prefix staticPrefix4 = Prefix.parse("4.4.4.0/24");
    Prefix staticPrefix5 = Prefix.parse("5.5.0.0/16");
    Prefix aggPrefix1 = Prefix.parse("1.1.0.0/16");
    Prefix aggPrefix2 = Prefix.parse("2.2.0.0/16");
    Prefix aggPrefix4General = Prefix.parse("4.4.0.0/16");
    Bgpv4Route localRoute1 =
        Bgpv4Route.builder()
            .setNetwork(staticPrefix1)
            .setNonRouting(true)
            .setAdmin(localAdmin)
            .setLocalPreference(0)
            .setNextHop(NextHopDiscard.instance())
            .setOriginatorIp(routerId)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO) // indicates local origination
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setWeight(DEFAULT_LOCAL_BGP_WEIGHT)
            .build();
    Bgpv4Route localRoute2 = localRoute1.toBuilder().setNetwork(staticPrefix2).build();
    Bgpv4Route localRoute3 = localRoute1.toBuilder().setNetwork(staticPrefix3).build();
    Bgpv4Route localRoute4 = localRoute1.toBuilder().setNetwork(staticPrefix4).build();
    Bgpv4Route localRoute5 = localRoute1.toBuilder().setNetwork(staticPrefix5).build();
    Bgpv4Route aggRoute1 =
        Bgpv4Route.builder()
            .setNetwork(aggPrefix1)
            .setAdmin(localAdmin)
            .setLocalPreference(100)
            .setNextHop(NextHopDiscard.instance())
            .setOriginatorIp(routerId)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.AGGREGATE)
            .setReceivedFromIp(Ip.ZERO) // indicates local origination
            .setSrcProtocol(RoutingProtocol.AGGREGATE)
            .setWeight(DEFAULT_LOCAL_BGP_WEIGHT)
            .build();
    Bgpv4Route aggRoute2 = aggRoute1.toBuilder().setNetwork(aggPrefix2).build();
    Bgpv4Route aggRoute4General = aggRoute1.toBuilder().setNetwork(aggPrefix4General).build();
    assertThat(
        bgpRibRoutes,
        containsInAnyOrder(
            localRoute1,
            localRoute2,
            localRoute3,
            localRoute4,
            localRoute5,
            aggRoute1,
            aggRoute2,
            aggRoute4General));

    Set<AbstractRoute> mainRibRoutes =
        dp.getRibs().get(hostname).get(Configuration.DEFAULT_VRF_NAME).getRoutes();
    assertThat(mainRibRoutes, hasItem(hasPrefix(aggPrefix1)));
    assertThat(mainRibRoutes, hasItem(hasPrefix(aggPrefix2)));
    assertThat(mainRibRoutes, hasItem(hasPrefix(aggPrefix4General)));
  }

  @Test
  public void testBgpAggregateWithLearnedSuppressedRoutes() throws IOException {
    /*
     * Snapshot contains c1, c2, and c3. c1 redistributes static routes 1.1.1.0/16 and 2.2.2.0/16
     * into BGP and advertises them to c2. c2 has aggregates 1.1.0.0/16 (not summary-only) and
     * 2.2.0.0/16 (summary-only). c2 advertises both aggregates and 1.1.1.0/16 to c3 (not
     * 2.2.2.0/16, which is suppressed by the summary-only aggregate).
     *
     * c1 should also receive c2's aggregate routes. Worth checking in addition to c3's routes
     * because the c1-c2 peering is IBGP, whereas the c2-c3 peering is EBGP.
     */
    String snapshotName = "bgp-agg-learned-contributors";
    String c1 = "c1";
    String c2 = "c2";
    String c3 = "c3";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(TESTRIGS_PREFIX + snapshotName, ImmutableList.of(c1, c2, c3))
                .build(),
            _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    // TODO: change to local bgp cost once supported
    int aggAdmin =
        batfish
            .loadConfigurations(batfish.getSnapshot())
            .get(c1)
            .getDefaultVrf()
            .getBgpProcess()
            .getAdminCost(RoutingProtocol.IBGP);

    Prefix learnedPrefix1 = Prefix.parse("1.1.1.0/24");
    Prefix learnedPrefix2 = Prefix.parse("2.2.2.0/24");
    Prefix aggPrefix1 = Prefix.parse("1.1.0.0/16");
    Prefix aggPrefix2 = Prefix.parse("2.2.0.0/16");
    {
      // Check c2 routes.
      Set<Bgpv4Route> bgpRibRoutes = dp.getBgpRoutes().get(c2, Configuration.DEFAULT_VRF_NAME);
      Bgpv4Route aggRoute1 =
          Bgpv4Route.builder()
              .setNetwork(aggPrefix1)
              .setAdmin(aggAdmin)
              .setLocalPreference(100)
              .setNextHop(NextHopDiscard.instance())
              .setOriginatorIp(Ip.parse("2.2.2.2"))
              .setOriginType(OriginType.IGP)
              .setProtocol(RoutingProtocol.AGGREGATE)
              .setReceivedFromIp(Ip.ZERO) // indicates local origination
              .setSrcProtocol(RoutingProtocol.AGGREGATE)
              .setWeight(DEFAULT_LOCAL_BGP_WEIGHT)
              .build();
      Bgpv4Route aggRoute2 = aggRoute1.toBuilder().setNetwork(aggPrefix2).build();
      assertThat(
          bgpRibRoutes,
          containsInAnyOrder(
              hasPrefix(learnedPrefix1),
              // TODO Once we mark routes as suppressed, assert this one is suppressed
              hasPrefix(learnedPrefix2),
              equalTo(aggRoute1),
              equalTo(aggRoute2)));
      Set<AbstractRoute> mainRibRoutes =
          dp.getRibs().get(c2).get(Configuration.DEFAULT_VRF_NAME).getRoutes();
      assertThat(mainRibRoutes, hasItem(hasPrefix(learnedPrefix1)));
      // Suppressed routes still go in the main RIB and are used for forwarding
      assertThat(mainRibRoutes, hasItem(hasPrefix(learnedPrefix2)));
      assertThat(mainRibRoutes, hasItem(hasPrefix(aggPrefix1)));
      assertThat(mainRibRoutes, hasItem(hasPrefix(aggPrefix2)));
    }
    {
      // Check c1 routes. (Has both learned routes because it originates them itself.)
      Set<Bgpv4Route> bgpRibRoutes = dp.getBgpRoutes().get(c1, Configuration.DEFAULT_VRF_NAME);
      assertThat(
          bgpRibRoutes,
          containsInAnyOrder(
              hasPrefix(learnedPrefix1),
              hasPrefix(learnedPrefix2),
              hasPrefix(aggPrefix1),
              hasPrefix(aggPrefix2)));
    }
    {
      // Check c3 routes.
      Set<Bgpv4Route> bgpRibRoutes = dp.getBgpRoutes().get(c3, Configuration.DEFAULT_VRF_NAME);
      assertThat(
          bgpRibRoutes,
          containsInAnyOrder(
              hasPrefix(learnedPrefix1), hasPrefix(aggPrefix1), hasPrefix(aggPrefix2)));
    }
  }

  @Test
  public void testAllowedVlans() throws IOException {
    Configuration c = parseConfig("eos-allowed-vlans");

    assertThat(
        c.getAllInterfaces().get("Port-Channel1").getAllowedVlans(),
        equalTo(IntegerSpace.of(Range.closed(1, 2))));
    assertThat(
        c.getAllInterfaces().get("Port-Channel2").getAllowedVlans(),
        equalTo(IntegerSpace.of(Range.closed(2, 3))));
    assertThat(
        c.getAllInterfaces().get("Port-Channel3").getAllowedVlans(), equalTo(IntegerSpace.EMPTY));
    assertThat(
        c.getAllInterfaces().get("Port-Channel4").getAllowedVlans(), equalTo(IntegerSpace.EMPTY));
    assertThat(
        c.getAllInterfaces().get("Port-Channel5").getAllowedVlans(),
        equalTo(IntegerSpace.of(Range.closed(1, 4094))));
    assertThat(
        c.getAllInterfaces().get("Port-Channel6").getAllowedVlans(),
        equalTo(IntegerSpace.of(Range.closed(1, 4))));
  }

  @Test
  public void testAsPathAccessListExtraction() {
    AristaConfiguration c = parseVendorConfig("as-path-access-list");
    assertThat(c.getAsPathAccessLists(), hasKeys("list1"));

    IpAsPathAccessList list1 = c.getAsPathAccessLists().get("list1");
    assertThat(list1.getLines(), hasSize(5));
    {
      IpAsPathAccessListLine line = list1.getLines().get(0);
      assertThat(line.getAction(), equalTo(LineAction.DENY));
      assertThat(line.getOriginType(), equalTo(IpAsPathAccessListLine.OriginType.ANY));
      assertThat(line.getRegex(), equalTo("_3$"));
    }
    {
      IpAsPathAccessListLine line = list1.getLines().get(1);
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getOriginType(), equalTo(IpAsPathAccessListLine.OriginType.ANY));
      assertThat(line.getRegex(), equalTo(".*"));
    }
    {
      IpAsPathAccessListLine line = list1.getLines().get(2);
      assertThat(line.getAction(), equalTo(LineAction.DENY));
      assertThat(line.getOriginType(), equalTo(IpAsPathAccessListLine.OriginType.EGP));
      assertThat(line.getRegex(), equalTo(".*"));
    }
    {
      IpAsPathAccessListLine line = list1.getLines().get(3);
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getOriginType(), equalTo(IpAsPathAccessListLine.OriginType.IGP));
      assertThat(line.getRegex(), equalTo(".*"));
    }
    {
      IpAsPathAccessListLine line = list1.getLines().get(4);
      assertThat(line.getAction(), equalTo(LineAction.DENY));
      assertThat(line.getOriginType(), equalTo(IpAsPathAccessListLine.OriginType.INCOMPLETE));
      assertThat(line.getRegex(), equalTo(".*"));
    }
  }

  @Test
  public void testEosMlagExtraction() {
    AristaConfiguration c = parseVendorConfig("eos-mlag");
    MlagConfiguration mlag = c.getEosMlagConfiguration();
    assertThat(mlag, notNullValue());
    assertThat(mlag.getDomainId(), equalTo("MLAG_DOMAIN_ID"));
    assertThat(mlag.getLocalInterface(), equalTo("Vlan4094"));
    assertThat(mlag.getPeerAddress(), equalTo(Ip.parse("1.1.1.3")));
    assertThat(mlag.getPeerAddressHeartbeat(), equalTo(Ip.parse("1.1.1.4")));
    assertThat(mlag.getPeerLink(), equalTo("Port-Channel1"));
  }

  @Test
  public void testEosMlagExtraction_newInterfaceNames() {
    AristaConfiguration c = parseVendorConfig("eos-mlag-new-interface-names");
    MlagConfiguration mlag = c.getEosMlagConfiguration();
    assertThat(mlag, notNullValue());
    assertThat(mlag.getLocalInterface(), equalTo("Vlan4094"));
    assertThat(mlag.getPeerLink(), equalTo("Port-Channel1"));
  }

  @Test
  public void testEosMlagConversion() throws IOException {
    Configuration c = parseConfig("eos-mlag");

    final String mlagName = "MLAG_DOMAIN_ID";
    assertThat(c, hasMlagConfig(mlagName, hasId(mlagName)));
    assertThat(c, hasMlagConfig(mlagName, hasPeerAddress(Ip.parse("1.1.1.3"))));
    assertThat(c, hasMlagConfig(mlagName, hasPeerInterface("Port-Channel1")));
    assertThat(c, hasMlagConfig(mlagName, MlagMatchers.hasLocalInterface("Vlan4094")));

    // Test interface config
    assertThat(c, hasInterface("Port-Channel1", hasMlagId(5)));
  }

  @Test
  public void testEosBgpPeers() throws IOException {
    String hostname = "eos-bgp-peers";
    Ip neighborWithRemoteAs = Ip.parse("1.1.1.1");
    Ip neighborWithoutRemoteAs = Ip.parse("2.2.2.2");
    Prefix dynamicNeighborWithoutRemoteAs = Prefix.parse("3.3.3.0/24");

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /*
     * All peers with and without remote-as should appear in the datamodel.
     */
    assertThat(
        c, hasDefaultVrf(hasBgpProcess(hasActiveNeighbor(neighborWithRemoteAs, hasRemoteAs(1L)))));
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasActiveNeighbor(neighborWithoutRemoteAs, hasRemoteAs(LongSpace.EMPTY)))));
    assertThat(
        ccae,
        hasRedFlagWarning(
            hostname,
            containsString(
                String.format(
                    "No remote-as configured for BGP neighbor %s in vrf default",
                    neighborWithoutRemoteAs))));
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasPassiveNeighbor(dynamicNeighborWithoutRemoteAs, hasRemoteAs(LongSpace.EMPTY)))));
    assertThat(
        ccae,
        hasRedFlagWarning(
            hostname,
            containsString(
                String.format(
                    "No acceptable remote-as for BGP neighbor %s in vrf default",
                    dynamicNeighborWithoutRemoteAs))));

    /*
     * Also ensure that default value of allowRemoteAsOut is true.
     */
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasActiveNeighbor(
                    neighborWithRemoteAs,
                    hasIpv4UnicastAddressFamily(
                        hasAddressFamilyCapabilites(hasAllowRemoteAsOut(ALWAYS)))))));
  }

  @Test
  public void testEosPortChannel() throws IOException {
    String hostname = "eos-port-channel";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(c, hasInterface("Ethernet0", hasBandwidth(40E9D)));
    assertThat(c, hasInterface("Ethernet0", hasSpeed(40E9D)));
    assertThat(c, hasInterface("Ethernet1", hasBandwidth(40E9D)));
    assertThat(c, hasInterface("Ethernet1", hasSpeed(40E9D)));
    assertThat(c, hasInterface("Ethernet2", hasBandwidth(40E9D)));
    assertThat(c, hasInterface("Ethernet2", hasSpeed(40E9D)));
    assertThat(c, hasInterface("Port-Channel1", hasBandwidth(80E9D)));
    assertThat(c, hasInterface("Port-Channel1", hasSpeed(nullValue())));
    assertThat(c, hasInterface("Port-Channel2", isActive(false)));
    // 0 since no members
    assertThat(c, hasInterface("Port-Channel2", hasBandwidth(0D)));
    assertThat(c, hasInterface("Port-Channel2", hasSpeed(nullValue())));
    assertThat(
        c.getAllInterfaces().get("Port-Channel1").getDependencies(),
        equalTo(
            ImmutableSet.of(
                new Dependency("Ethernet0", DependencyType.AGGREGATE),
                new Dependency("Ethernet1", DependencyType.AGGREGATE))));
  }

  @Test
  public void testEosVxlan() throws IOException {
    String hostnameBase = "eos-vxlan";
    String hostnameNoLoopbackAddr = "eos-vxlan-no-loopback-address";
    String hostnameNoSourceIface = "eos-vxlan-no-source-interface";

    Batfish batfish =
        getBatfishForConfigurationNames(
            hostnameBase, hostnameNoSourceIface, hostnameNoLoopbackAddr);
    // Config with proper loopback iface, VLAN-specific unicast, explicit UDP port
    Configuration configBase = batfish.loadConfigurations(batfish.getSnapshot()).get(hostnameBase);
    assertThat(configBase, hasDefaultVrf(hasL2VniSettings(hasKey(10002))));
    Layer2Vni vnisBase = configBase.getDefaultVrf().getLayer2Vnis().get(10002);

    // Config with no loopback address, using multicast, and default UDP port
    Configuration configNoLoopbackAddr =
        batfish.loadConfigurations(batfish.getSnapshot()).get(hostnameNoLoopbackAddr);
    assertThat(configNoLoopbackAddr, hasDefaultVrf(hasL2VniSettings(hasKey(10002))));
    Layer2Vni vnisNoAddr = configNoLoopbackAddr.getDefaultVrf().getLayer2Vnis().get(10002);

    // Config with no source interface and general VXLAN unicast address
    Configuration configNoSourceIface =
        batfish.loadConfigurations(batfish.getSnapshot()).get(hostnameNoSourceIface);
    assertThat(configNoSourceIface, hasDefaultVrf(hasL2VniSettings(hasKey(10002))));
    Layer2Vni vnisNoIface = configNoSourceIface.getDefaultVrf().getLayer2Vnis().get(10002);

    // Confirm VLAN-specific unicast address takes priority over the other addresses
    assertThat(vnisBase, hasBumTransportMethod(equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP)));
    assertThat(vnisBase, hasBumTransportIps(contains(Ip.parse("1.1.1.10"))));
    // Confirm source address is inherited from source interface
    assertThat(vnisBase, hasSourceAddress(equalTo(Ip.parse("1.1.1.4"))));
    // Confirm explicit UDP port is used
    assertThat(vnisBase, hasUdpPort(equalTo(5555)));
    // Confirm VLAN<->VNI mapping is applied
    assertThat(vnisBase, hasVlan(equalTo(2)));

    // Confirm multicast address is present
    assertThat(vnisNoAddr, hasBumTransportMethod(equalTo(BumTransportMethod.MULTICAST_GROUP)));
    assertThat(vnisNoAddr, hasBumTransportIps(contains(Ip.parse("227.10.1.1"))));
    // Confirm no source address is present (no address specified for loopback interface)
    assertThat(vnisNoAddr, hasSourceAddress(nullValue()));
    // Confirm default UDP port is used even though none is supplied
    assertThat(vnisNoAddr, hasUdpPort(equalTo(4789)));

    // Confirm general VXLAN flood addresses are used
    assertThat(vnisNoIface, hasBumTransportMethod(equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP)));
    assertThat(
        vnisNoIface,
        hasBumTransportIps(containsInAnyOrder(Ip.parse("1.1.1.5"), Ip.parse("1.1.1.6"))));
    // Confirm no source address is present (no interface is linked to the VXLAN)
    assertThat(vnisNoIface, hasSourceAddress(nullValue()));
  }

  @Test
  public void testEosVxlanMisconfig() throws IOException {
    String hostname = "eos-vxlan-misconfig";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration config = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    // Make sure that misconfigured VXLAN is still converted into VI model properly
    assertThat(config, hasDefaultVrf(hasL2VniSettings(hasKey(10002))));
    Layer2Vni vnisMisconfig = config.getDefaultVrf().getLayer2Vnis().get(10002);

    // No BUM IPs specified
    assertThat(vnisMisconfig, hasBumTransportIps(emptyIterable()));
    // No source interface so no source address
    assertThat(vnisMisconfig, hasSourceAddress(nullValue()));
    // Confirm default UDP port is used
    assertThat(vnisMisconfig, hasUdpPort(equalTo(4789)));
    // Confirm VLAN<->VNI mapping is applied
    assertThat(vnisMisconfig, hasVlan(equalTo(2)));
  }

  @Test
  public void testEosTrunkGroup() throws IOException {
    String hostname = "eos_trunk_group";

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    assertThat(
        c, hasInterface("Port-Channel1", hasAllowedVlans(IntegerSpace.of(Range.closed(1, 2)))));
    assertThat(c, hasInterface("Port-Channel2", hasAllowedVlans(IntegerSpace.of(99))));
  }

  @Test
  public void testEosVxlanCiscoConfig() {
    String hostname = "eos-vxlan";

    AristaConfiguration config = parseVendorConfig(hostname);

    assertThat(config, notNullValue());
    AristaEosVxlan eosVxlan = config.getEosVxlan();
    assertThat(eosVxlan, notNullValue());

    assertThat(eosVxlan.getDescription(), equalTo("vxlan vti"));
    // Confirm flood address set doesn't contain the removed address
    assertThat(
        eosVxlan.getFloodAddresses(), containsInAnyOrder(Ip.parse("1.1.1.5"), Ip.parse("1.1.1.7")));
    assertThat(eosVxlan.getMulticastGroup(), equalTo(Ip.parse("227.10.1.1")));
    assertThat(eosVxlan.getSourceInterface(), equalTo("Loopback1"));
    assertThat(eosVxlan.getUdpPort(), equalTo(5555));

    assertThat(eosVxlan.getVlanVnis(), hasEntry(equalTo(2), equalTo(10002)));

    // Confirm flood address set was overwritten as expected
    assertThat(
        eosVxlan.getVlanFloodAddresses(), hasEntry(equalTo(2), contains(Ip.parse("1.1.1.10"))));
  }

  @Test
  public void testEosVxlanReference() throws IOException {
    String hostname = "eos-vxlan";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasNumReferrers(filename, VXLAN, "Vxlan1", 1));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "Loopback1", 2));
  }

  @Test
  public void testBgpVlansExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_vlans");
    {
      AristaBgpVlanAwareBundle bundle = config.getAristaBgp().getVlanAwareBundles().get("Tenant_A");
      assertThat(bundle, notNullValue());
      assertThat(bundle.getRd(), equalTo(RouteDistinguisher.parse("192.168.255.8:10101")));
      assertThat(bundle.getRtImport(), equalTo(ExtendedCommunity.target(10101, 10101)));
      assertThat(bundle.getRtExport(), equalTo(ExtendedCommunity.target(10101, 10101)));
      assertThat(bundle.getVlans(), equalTo(IntegerSpace.builder().including(1, 110, 111).build()));
    }

    {
      AristaBgpVlan vlan = config.getAristaBgp().getVlans().get(300);
      assertThat(vlan, notNullValue());
      assertThat(vlan.getRd(), equalTo(RouteDistinguisher.parse("192.168.255.100:10103")));
      assertThat(vlan.getRtImport(), equalTo(ExtendedCommunity.target(10101, 10103)));
      assertThat(vlan.getRtExport(), equalTo(ExtendedCommunity.target(10101, 10103)));
    }
  }

  /** A version-independent test for <4.23 and 4.23+ syntax. */
  private static void testCommunityListExtraction(String hostname) {
    AristaConfiguration config = parseVendorConfig(hostname);
    assertThat(config.getStandardCommunityLists(), hasKeys("STANDARD_CL"));
    {
      StandardCommunityList list = config.getStandardCommunityLists().get("STANDARD_CL");
      assertThat(list.getLines(), hasSize(2));
      StandardCommunityListLine line0 = list.getLines().get(0);
      StandardCommunityListLine line1 = list.getLines().get(1);
      assertThat(line0.getAction(), equalTo(LineAction.PERMIT));
      assertThat(
          line0.getCommunities(),
          containsInAnyOrder(StandardCommunity.of(1, 1), StandardCommunity.GRACEFUL_SHUTDOWN));
      assertThat(line1.getAction(), equalTo(LineAction.DENY));
      assertThat(
          line1.getCommunities(),
          containsInAnyOrder(
              StandardCommunity.of(459123L),
              StandardCommunity.INTERNET,
              StandardCommunity.NO_EXPORT_SUBCONFED,
              StandardCommunity.NO_ADVERTISE,
              StandardCommunity.NO_EXPORT));
    }
    assertThat(config.getExpandedCommunityLists(), hasKeys("EXPANDED_CL"));
    {
      ExpandedCommunityList list = config.getExpandedCommunityLists().get("EXPANDED_CL");
      assertThat(list.getLines(), hasSize(2));
      ExpandedCommunityListLine line0 = list.getLines().get(0);
      ExpandedCommunityListLine line1 = list.getLines().get(1);
      assertThat(line0.getAction(), equalTo(LineAction.DENY));
      assertThat(line0.getRegex(), equalTo("_3$"));
      assertThat(line1.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line1.getRegex(), equalTo(".*"));
    }
  }

  @Test
  public void testCommunityListExtraction() {
    testCommunityListExtraction("arista_community_list_421");
    testCommunityListExtraction("arista_community_list_423");
  }

  @Test
  public void testNeighborExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_neighbors");
    {
      String peergName = "PEER_G";
      assertThat(config.getAristaBgp().getPeerGroups(), hasKey(peergName));
      AristaBgpPeerGroupNeighbor neighbor = config.getAristaBgp().getPeerGroups().get(peergName);
      assertThat(config.getAristaBgp().getPeerGroups().get(peergName).getAllowAsIn(), equalTo(3));
      assertThat(neighbor.getRemovePrivateAsMode(), nullValue());
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
    }
    {
      AristaBgpPeerGroupNeighbor pg = config.getAristaBgp().getPeerGroups().get("PEER_G2");
      assertThat(pg, notNullValue());
      assertThat(pg.getRouteReflectorClient(), equalTo(Boolean.TRUE));
    }
    {
      Ip neighborAddr = Ip.parse("1.1.1.1");
      AristaBgpV4Neighbor neighbor =
          config.getAristaBgp().getDefaultVrf().getV4neighbors().get(neighborAddr);
      assertThat(neighbor.getAllowAsIn(), nullValue());
      AristaBgpDefaultOriginate defaultOriginate = neighbor.getDefaultOriginate();
      assertThat(defaultOriginate, notNullValue());
      assertThat(defaultOriginate.getAlways(), equalTo(true));
      assertThat(defaultOriginate.getRouteMap(), nullValue());
      assertThat(neighbor.getDescription(), equalTo("SOME NEIGHBOR"));
      assertTrue(neighbor.getDontCapabilityNegotiate());
      assertThat(neighbor.getEbgpMultihop(), equalTo(Integer.MAX_VALUE));
      assertThat(neighbor.getLocalAs(), equalTo(65111L));
      assertTrue(neighbor.getNextHopSelf());
      assertTrue(neighbor.getNextHopUnchanged());
      assertThat(neighbor.getPeerGroup(), equalTo("PEER_G"));
      assertThat(neighbor.getRemoteAs(), equalTo(35L));
      assertThat(neighbor.getGenericAddressFamily().getRouteMapIn(), equalTo("RM_IN"));
      assertThat(neighbor.getGenericAddressFamily().getRouteMapOut(), equalTo("RM_OUT"));
      assertThat(neighbor.getRemovePrivateAsMode(), is(RemovePrivateAsMode.BASIC));
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
      assertTrue(neighbor.getSendCommunity());
      assertThat(neighbor.getShutdown(), nullValue());
      assertThat(neighbor.getUpdateSource(), equalTo("Loopback0"));
    }
    {
      Ip neighborAddr = Ip.parse("2.2.2.2");
      AristaBgpV4Neighbor neighbor =
          config.getAristaBgp().getDefaultVrf().getV4neighbors().get(neighborAddr);
      AristaBgpDefaultOriginate defaultOriginate = neighbor.getDefaultOriginate();
      assertThat(defaultOriginate, notNullValue());
      assertThat(defaultOriginate.getAlways(), equalTo(true));
      assertThat(defaultOriginate.getRouteMap(), equalTo("DEF_ORIG_MAP"));
      assertThat(neighbor.getEbgpMultihop(), equalTo(10));
      assertThat(neighbor.getRemoteAs(), equalTo(36L));
      assertThat(neighbor.getRemovePrivateAsMode(), is(RemovePrivateAsMode.ALL));
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
      assertThat(neighbor.getShutdown(), equalTo(Boolean.TRUE));
      // TODO: default-originate
    }
    {
      Ip neighborAddr = Ip.parse("3.3.3.3");
      AristaBgpV4Neighbor neighbor =
          config.getAristaBgp().getDefaultVrf().getV4neighbors().get(neighborAddr);
      assertThat(neighbor.getEnforceFirstAs(), equalTo(Boolean.FALSE));
      assertThat(neighbor.getPeerGroup(), equalTo("PEER_G2"));
      assertThat(neighbor.getRemovePrivateAsMode(), is(RemovePrivateAsMode.REPLACE_AS));
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
      assertThat(neighbor.getShutdown(), equalTo(Boolean.FALSE));
    }
    {
      Ip neighborAddr = Ip.parse("2.2.2.2");
      AristaBgpV4Neighbor neighbor =
          config.getAristaBgp().getVrfs().get("tenant").getV4neighbors().get(neighborAddr);
      assertThat(neighbor.getRemoteAs(), equalTo(88L));
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
    }
    {
      assertThat(config.getAristaBgp().getDefaultVrf().getListenLimit(), equalTo(10));
      Prefix neighborAddr = Prefix.parse("4.4.4.0/24");
      AristaBgpV4DynamicNeighbor neighbor =
          config.getAristaBgp().getDefaultVrf().getV4DynamicNeighbors().get(neighborAddr);
      assertThat(config.getAristaBgp().getPeerGroups(), hasKey("DYNAMIC"));
      assertThat(neighbor.getPeerGroup(), equalTo("DYNAMIC"));
      assertThat(neighbor.getRange(), equalTo(neighborAddr));
      assertThat(neighbor.getRouteReflectorClient(), nullValue());
    }
    {
      Ip neighborAddr = Ip.parse("5.5.5.5");
      AristaBgpV4Neighbor neighbor =
          config.getAristaBgp().getDefaultVrf().getV4neighbors().get(neighborAddr);
      assertThat(neighbor.getEnforceFirstAs(), equalTo(true));
      assertThat(neighbor.getRemoteAs(), nullValue());
    }
  }

  @Test
  public void testNeighborConversion() {
    Configuration c = parseConfig("arista_bgp_neighbors");
    assertThat(c.getDefaultVrf(), notNullValue());
    BgpProcess proc = c.getDefaultVrf().getBgpProcess();
    assertThat(proc, notNullValue());
    {
      Ip neighborIp = Ip.parse("1.1.1.1");
      assertThat(proc.getActiveNeighbors(), hasKey(neighborIp));
      BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(neighborIp);
      assertThat(neighbor.getClusterId(), equalTo(Ip.parse("99.99.99.99").asLong()));
      assertThat(neighbor.getEnforceFirstAs(), equalTo(true));
      assertThat(neighbor.getIpv4UnicastAddressFamily(), notNullValue());
      assertThat(neighbor.getGeneratedRoutes(), hasSize(1));
      GeneratedRoute defaultOriginate = getOnlyElement(neighbor.getGeneratedRoutes());
      assertThat(defaultOriginate.getGenerationPolicy(), nullValue());
      assertThat(defaultOriginate.getAttributePolicy(), nullValue());
      Ipv4UnicastAddressFamily af = neighbor.getIpv4UnicastAddressFamily();
      assertThat(af.getAddressFamilyCapabilities().getAllowLocalAsIn(), equalTo(true));
      assertThat(af.getAddressFamilyCapabilities().getAllowRemoteAsOut(), equalTo(ALWAYS));
      assertThat(af.getRouteReflectorClient(), equalTo(false));
    }
    {
      Ip neighborIp = Ip.parse("2.2.2.2");
      // shutdown neighbor is not converted
      assertThat(proc.getActiveNeighbors(), not(hasKey(neighborIp)));
    }
    {
      Ip neighborIp = Ip.parse("3.3.3.3");
      assertThat(proc.getActiveNeighbors(), hasKey(neighborIp));
      BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(neighborIp);
      assertThat(neighbor.getEnforceFirstAs(), equalTo(false));
      assertThat(neighbor.getGeneratedRoutes(), empty());
      Ipv4UnicastAddressFamily af = neighbor.getIpv4UnicastAddressFamily();
      assertThat(af, notNullValue());
      assertThat(af.getRouteReflectorClient(), equalTo(true));
    }
    {
      Prefix neighborPrefix = Prefix.parse("4.4.4.0/24");
      assertThat(proc.getPassiveNeighbors(), hasKey(neighborPrefix));
      BgpPassivePeerConfig neighbor = proc.getPassiveNeighbors().get(neighborPrefix);
      assertThat(neighbor.getRemoteAsns(), equalTo(LongSpace.of(4000L)));
      Ipv4UnicastAddressFamily af = neighbor.getIpv4UnicastAddressFamily();
      assertThat(af, notNullValue());
      assertTrue(af.getAddressFamilyCapabilities().getSendCommunity());
      assertThat(af.getRouteReflectorClient(), equalTo(false));
    }
    {
      Ip neighborIp = Ip.parse("5.5.5.5");
      // neighbor with missing remote-as is converted to empty longspace
      assertThat(proc.getActiveNeighbors(), hasKey(neighborIp));
      BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(neighborIp);
      assertThat(neighbor.getRemoteAsns(), equalTo(LongSpace.EMPTY));
    }
  }

  /** Test that we convert properly when v4 family is completely absent */
  @Test
  public void testNeighborConversion_no_v4() {
    Configuration c = parseConfig("arista_bgp_neighbors_no_v4");
    assertThat(c.getDefaultVrf(), notNullValue());
    BgpProcess proc = c.getDefaultVrf().getBgpProcess();
    assertThat(proc, notNullValue());
    Ip neighborIp = Ip.parse("10.0.0.11");
    assertThat(proc.getActiveNeighbors(), hasKey(neighborIp));
    BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(neighborIp);
    assertThat(neighbor.getEvpnAddressFamily(), notNullValue());
    assertThat(neighbor.getIpv4UnicastAddressFamily(), nullValue());
  }

  @Test
  public void testVrfExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_vrf");
    assertThat(config.getAristaBgp(), notNullValue());
    assertThat(
        config.getAristaBgp().getVrfs().keySet(), containsInAnyOrder(DEFAULT_VRF, "FOO", "BAR"));
    {
      AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
      assertThat(vrf.getBestpathAsPathMultipathRelax(), nullValue());
      assertThat(vrf.getBestpathTieBreaker(), nullValue());
      assertThat(vrf.getClusterId(), equalTo(Ip.parse("1.2.3.4")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("FOO");
      assertThat(vrf.getBestpathAsPathMultipathRelax(), equalTo(Boolean.TRUE));
      assertThat(vrf.getRouteDistinguisher(), equalTo(RouteDistinguisher.parse("123:123")));
      assertThat(vrf.getExportRouteTarget(), equalTo(ExtendedCommunity.target(1L, 1L)));
      assertThat(vrf.getImportRouteTarget(), equalTo(ExtendedCommunity.target(2L, 2L)));
      assertThat(vrf.getLocalAs(), equalTo(65000L));
      assertThat(vrf.getBestpathTieBreaker(), equalTo(AristaBgpBestpathTieBreaker.ROUTER_ID));
      assertThat(vrf.getClusterId(), nullValue());
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("BAR");
      assertThat(vrf.getBestpathAsPathMultipathRelax(), equalTo(Boolean.FALSE));
      assertThat(
          vrf.getBestpathTieBreaker(), equalTo(AristaBgpBestpathTieBreaker.CLUSTER_LIST_LENGTH));
      assertThat(vrf.getClusterId(), nullValue());
    }
  }

  @Test
  public void testVrfConversion() {
    Configuration c = parseConfig("arista_bgp_vrf");
    assertThat(c.getVrfs().keySet(), containsInAnyOrder(DEFAULT_VRF, "FOO", "BAR"));
    {
      BgpProcess proc = c.getDefaultVrf().getBgpProcess();
      assertThat(proc, notNullValue());
      assertThat(
          proc.getMultipathEquivalentAsPathMatchMode(),
          equalTo(MultipathEquivalentAsPathMatchMode.PATH_LENGTH));
      assertThat(proc.getTieBreaker(), equalTo(BgpTieBreaker.ROUTER_ID));
    }
    {
      BgpProcess proc = c.getVrfs().get("FOO").getBgpProcess();
      assertThat(proc, notNullValue());
      assertThat(
          proc.getMultipathEquivalentAsPathMatchMode(),
          equalTo(MultipathEquivalentAsPathMatchMode.PATH_LENGTH));
      assertThat(proc.getTieBreaker(), equalTo(BgpTieBreaker.ROUTER_ID));
    }
    {
      BgpProcess proc = c.getVrfs().get("BAR").getBgpProcess();
      assertThat(proc, notNullValue());
      assertThat(
          proc.getMultipathEquivalentAsPathMatchMode(),
          equalTo(MultipathEquivalentAsPathMatchMode.EXACT_PATH));
      assertThat(proc.getTieBreaker(), equalTo(BgpTieBreaker.CLUSTER_LIST_LENGTH));
    }
  }

  @Test
  public void testAddressFamilyExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_af");
    AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
    AristaBgpVrfIpv4UnicastAddressFamily ipv4af = vrf.getV4UnicastAf();
    AristaBgpVrfIpv6UnicastAddressFamily ipv6af = vrf.getV6UnicastAf();
    assertThat(ipv4af, notNullValue());
    AristaBgpVrfEvpnAddressFamily evpnaf = config.getAristaBgp().getDefaultVrf().getEvpnAf();
    assertThat(evpnaf, notNullValue());

    {
      assertThat(vrf.getV4neighbors(), hasKey(Ip.parse("1.1.1.1")));
      AristaBgpNeighborAddressFamily v4 = ipv4af.getNeighbor(Ip.parse("1.1.1.1"));
      assertThat(v4, notNullValue());
      assertThat(v4.getActivate(), equalTo(Boolean.TRUE));
      AristaBgpNeighborAddressFamily evpn = evpnaf.getNeighbor(Ip.parse("1.1.1.1"));
      assertThat(evpn, notNullValue());
      assertThat(evpn.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      assertThat(vrf.getV4neighbors(), hasKey(Ip.parse("2.2.2.2")));
      AristaBgpNeighborAddressFamily v4 = ipv4af.getNeighbor(Ip.parse("2.2.2.2"));
      assertThat(v4, notNullValue());
      assertThat(v4.getActivate(), equalTo(Boolean.FALSE));
      AristaBgpNeighborAddressFamily evpn = evpnaf.getNeighbor(Ip.parse("2.2.2.2"));
      assertThat(evpn, nullValue());
    }
    {
      assertThat(config.getAristaBgp().getPeerGroups(), hasKey("PG"));
      AristaBgpNeighborAddressFamily v4 = ipv4af.getPeerGroup("PG");
      assertThat(v4, nullValue());
      AristaBgpNeighborAddressFamily evpn = evpnaf.getPeerGroup("PG");
      assertThat(evpn, notNullValue());
      assertThat(evpn.getActivate(), equalTo(Boolean.TRUE));
      AristaBgpNeighborAddressFamily v6 = ipv6af.getPeerGroup("PG");
      assertThat(v6, notNullValue());
      assertThat(v6.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      Ip neighbor = Ip.parse("3.3.3.3");
      assertThat(vrf.getV4neighbors(), not(hasKey(neighbor)));
      AristaBgpVrf vrf1 = config.getAristaBgp().getVrfs().get("vrf1");
      assertThat(vrf1.getV4neighbors(), hasKey(neighbor));
      assertTrue(vrf1.getV4UnicastAf().getNeighbor(neighbor).getActivate());
      assertThat(vrf1.getV4UnicastAf().getNeighbor(neighbor).getRouteMapOut(), equalTo("RM1"));
    }
  }

  @Test
  public void testNetworkExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_network");
    {
      Prefix prefix = Prefix.parse("1.1.1.0/24");
      AristaBgpNetworkConfiguration network =
          config.getAristaBgp().getDefaultVrf().getV4UnicastAf().getNetworks().get(prefix);
      assertThat(network, notNullValue());
      assertThat(network.getRouteMap(), nullValue());
    }
    {
      Prefix prefix = Prefix.parse("1.1.2.0/24");
      AristaBgpNetworkConfiguration network =
          config.getAristaBgp().getDefaultVrf().getV4UnicastAf().getNetworks().get(prefix);
      assertThat(network, notNullValue());
      assertThat(network.getRouteMap(), nullValue());
    }
    {
      Prefix prefix = Prefix.parse("1.1.3.0/24");
      AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
      AristaBgpVrfIpv4UnicastAddressFamily v4UnicastAf = vrf.getV4UnicastAf();
      AristaBgpNetworkConfiguration network = v4UnicastAf.getNetworks().get(prefix);
      assertThat(network, notNullValue());
      assertThat(network.getRouteMap(), equalTo("RM"));
      // Ensure parser didn't go into "router bgp" context and stayed in "address family ipv4"
      assertNull(vrf.getNextHopUnchanged());
      assertTrue(v4UnicastAf.getNextHopUnchanged());
    }
  }

  @Test
  public void testVxlanExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_vxlan");
    assertThat(config.getEosVxlan().getArpReplyRelay(), equalTo(true));
    assertThat(config.getEosVxlan().getVrfToVni(), hasEntry("TENANT", 10000));
    assertThat(config.getEosVxlan().getVlanVnis(), hasEntry(1, 10001));
  }

  @Test
  public void testVxlanConversion() {
    Configuration config = parseConfig("arista_vxlan");
    {
      Layer3Vni vniSettings = config.getVrfs().get("TENANT").getLayer3Vnis().get(10000);
      assertThat(
          vniSettings.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
      assertThat(vniSettings.getBumTransportIps(), empty());
    }
    {
      Layer2Vni vniSettings = config.getVrfs().get("VRF_1").getLayer2Vnis().get(10001);
      assertThat(
          vniSettings.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
      assertThat(vniSettings.getBumTransportIps(), empty());
    }
    {
      Layer2Vni vniSettings = config.getVrfs().get("VRF_2").getLayer2Vnis().get(10002);
      assertThat(
          vniSettings.getBumTransportMethod(), equalTo(BumTransportMethod.UNICAST_FLOOD_GROUP));
      assertThat(vniSettings.getBumTransportIps(), empty());
    }
  }

  @Test
  public void testRedistributeExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_redistribute");
    {
      Map<AristaRedistributeType, AristaBgpRedistributionPolicy> redistributionPolicies =
          config.getAristaBgp().getDefaultVrf().getRedistributionPolicies();
      assertThat(
          redistributionPolicies,
          hasEntry(
              AristaRedistributeType.CONNECTED,
              new AristaBgpRedistributionPolicy(AristaRedistributeType.CONNECTED, "RM")));
    }
    {
      Map<AristaRedistributeType, AristaBgpRedistributionPolicy> redistributionPolicies =
          config.getAristaBgp().getVrfs().get("tenant").getRedistributionPolicies();
      assertThat(
          redistributionPolicies,
          hasEntry(
              AristaRedistributeType.STATIC,
              new AristaBgpRedistributionPolicy(AristaRedistributeType.STATIC, "RM2")));
    }
  }

  @Test
  public void testRedistributeOspfExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_redistribute_ospf");
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf1");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF),
          equalTo(new AristaBgpRedistributionPolicy(OSPF, "ALLOW_10")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf2");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF_INTERNAL),
          equalTo(new AristaBgpRedistributionPolicy(OSPF_INTERNAL, "ALLOW_10")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf3");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF_EXTERNAL),
          equalTo(new AristaBgpRedistributionPolicy(OSPF_EXTERNAL, "ALLOW_10")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf4");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF_NSSA_EXTERNAL),
          equalTo(new AristaBgpRedistributionPolicy(OSPF_NSSA_EXTERNAL, "ALLOW_10")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf5");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF_NSSA_EXTERNAL_TYPE_1),
          equalTo(new AristaBgpRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_1, "ALLOW_10")));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf6");
      assertThat(
          vrf.getRedistributionPolicies().get(OSPF_NSSA_EXTERNAL_TYPE_2),
          equalTo(new AristaBgpRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_2, "ALLOW_10")));
    }
  }

  @Test
  public void testRedistributeOspfConversion() {
    Configuration config = parseConfig("arista_bgp_redistribute_ospf");
    Prefix prefix = Prefix.parse("10.1.1.0/24");
    OspfIntraAreaRoute intra =
        OspfIntraAreaRoute.builder()
            .setNetwork(prefix)
            .setNextHop(NextHopDiscard.instance())
            .build();
    OspfInterAreaRoute inter =
        OspfInterAreaRoute.builder()
            .setNetwork(prefix)
            .setNextHop(NextHopDiscard.instance())
            .setArea(0)
            .build();
    OspfExternalType1Route ext1 =
        (OspfExternalType1Route)
            OspfExternalType1Route.builder()
                .setNetwork(prefix)
                .setNextHop(NextHopDiscard.instance())
                .setLsaMetric(0)
                .setArea(0)
                .setCostToAdvertiser(1)
                .setAdvertiser("node")
                .build();
    OspfExternalType2Route ext2 =
        (OspfExternalType2Route)
            OspfExternalType2Route.builder()
                .setNetwork(prefix)
                .setNextHop(NextHopDiscard.instance())
                .setLsaMetric(0)
                .setArea(0)
                .setAdvertiser("node")
                .setCostToAdvertiser(1)
                .build();
    Builder builder = Bgpv4Route.testBuilder();
    {
      String policyName = generatedBgpRedistributionPolicyName("vrf1");
      RoutingPolicy policy = config.getRoutingPolicies().get(policyName);
      assertTrue(policy.process(intra, builder, Direction.OUT));
      assertTrue(policy.process(inter, builder, Direction.OUT));
      assertTrue(policy.process(ext1, builder, Direction.OUT));
      assertTrue(policy.process(ext2, builder, Direction.OUT));
    }
    {
      String policyName = generatedBgpRedistributionPolicyName("vrf2");
      RoutingPolicy policy = config.getRoutingPolicies().get(policyName);
      assertTrue(policy.process(intra, builder, Direction.OUT));
      assertTrue(policy.process(inter, builder, Direction.OUT));
      assertFalse(policy.process(ext1, builder, Direction.OUT));
      assertFalse(policy.process(ext2, builder, Direction.OUT));
    }
    {
      String policyName = generatedBgpRedistributionPolicyName("vrf3");
      RoutingPolicy policy = config.getRoutingPolicies().get(policyName);
      assertFalse(policy.process(intra, builder, Direction.OUT));
      assertFalse(policy.process(inter, builder, Direction.OUT));
      assertTrue(policy.process(ext1, builder, Direction.OUT));
      assertTrue(policy.process(ext2, builder, Direction.OUT));
    }
    // TODO: support for nssa-external variants
  }

  @Test
  public void testHumanName() {
    Configuration c = parseConfig("arista-humanname");
    assertThat(c.getHumanName(), equalTo("ARISTA-HUMANNAME"));
  }

  @Test
  public void testInterfaceConversion() {
    Configuration c = parseConfig("arista_interface");
    assertThat(
        c,
        hasInterface(
            "Ethernet1", allOf(hasVrf(hasName(equalTo("VRF_1"))), hasEncapsulationVlan(7))));
    assertThat(c, hasInterface("Ethernet2", hasVrf(hasName(equalTo("VRF_2")))));
    assertThat(c, hasInterface("UnconnectedEthernet5"));
  }

  @Test
  public void testMulticastParsing() {
    // doesn't crash.
    parseVendorConfig("arista_multicast");
  }

  @Test
  public void testVrrpConversion() {
    Configuration c = parseConfig("arista_vrrp");
    assertThat(c.getAllInterfaces(), hasKey("Vlan20"));
    Interface i = c.getAllInterfaces().get("Vlan20");
    assertThat(i.getVrrpGroups(), hasKey(1));
    VrrpGroup group = i.getVrrpGroups().get(1);
    assertThat(group.getVirtualAddress(), equalTo(ConcreteInterfaceAddress.parse("1.2.3.4/24")));
    assertThat(group.getPriority(), equalTo(200));
  }

  @Test
  public void testVrrpExtraction() {
    AristaConfiguration c = parseVendorConfig("arista_vrrp");
    assertThat(c.getInterfaces(), hasKey("Vlan20"));
    assertThat(c.getVrrpGroups(), hasKey("Vlan20"));
    VrrpInterface vrrpI = c.getVrrpGroups().get("Vlan20");
    assertThat(vrrpI.getVrrpGroups(), hasKey(1));
    org.batfish.representation.arista.VrrpGroup g = vrrpI.getVrrpGroups().get(1);
    assertThat(g.getVirtualAddress(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(g.getPriority(), equalTo(200));
  }

  @Test
  public void testEvpnConversion() {
    Configuration c = parseConfig("arista_evpn");
    Ip[] neighborIPs = {Ip.parse("192.168.255.1"), Ip.parse("192.168.255.2")};
    for (Ip ip : neighborIPs) {
      BgpActivePeerConfig neighbor = c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(ip);
      assertThat(neighbor.getEvpnAddressFamily(), notNullValue());
      assertThat(
          neighbor.getEvpnAddressFamily().getAddressFamilyCapabilities().getAllowRemoteAsOut(),
          equalTo(ALWAYS));
      assertThat(
          neighbor.getEvpnAddressFamily().getL2VNIs(),
          equalTo(
              ImmutableSet.of(
                  Layer2VniConfig.builder()
                      .setVrf("Tenant_A_OPZone")
                      .setVni(10110)
                      .setRouteDistinguisher(RouteDistinguisher.parse("192.168.255.3:10110"))
                      .setImportRouteTarget(ExtendedCommunity.target(10110, 10110).matchString())
                      .setRouteTarget(ExtendedCommunity.target(10110, 10110))
                      .build(),
                  Layer2VniConfig.builder()
                      .setVrf("Tenant_B_OPZone")
                      .setVni(10210)
                      .setRouteDistinguisher(RouteDistinguisher.parse("192.168.255.3:10210"))
                      .setImportRouteTarget(ExtendedCommunity.target(10210, 10210).matchString())
                      .setRouteTarget(ExtendedCommunity.target(10210, 10210))
                      .build())));

      assertThat(
          neighbor.getEvpnAddressFamily().getL3VNIs(),
          equalTo(
              ImmutableSet.of(
                  Layer3VniConfig.builder()
                      .setVrf("Tenant_A_OPZone")
                      .setVni(50101)
                      .setRouteDistinguisher(RouteDistinguisher.parse("192.168.255.3:50101"))
                      .setImportRouteTarget(ExtendedCommunity.target(50101, 50101).matchString())
                      .setRouteTarget(ExtendedCommunity.target(50101, 50101))
                      .setAdvertiseV4Unicast(true)
                      .build(),
                  Layer3VniConfig.builder()
                      .setVrf("Tenant_B_OPZone")
                      .setVni(50201)
                      .setRouteDistinguisher(RouteDistinguisher.parse("192.168.255.3:50201"))
                      .setImportRouteTarget(ExtendedCommunity.target(50201, 50201).matchString())
                      .setRouteTarget(ExtendedCommunity.target(50201, 50201))
                      .setAdvertiseV4Unicast(true)
                      .build())));
    }
  }

  /**
   * Ensure that when L2 VNIs are present and no bgp VRFs are defined, we still make Bgp procesess
   * for non-default VRF to prevent crashing the dataplane computation.
   */
  @Test
  public void testEvpnConversionL2VnisOnly() {
    Configuration c = parseConfig("arista_evpn_l2_vni_only");
    assertThat(c, ConfigurationMatchers.hasVrf("vrf1", hasBgpProcess(notNullValue())));
    assertThat(c.getDefaultVrf().getLayer2Vnis(), hasKey(10030));
  }

  @Test
  public void testBgpSendCommunityExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_send_community");
    AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
    {
      Ip ip = Ip.parse("1.1.1.1");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertTrue(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertTrue(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.2");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertTrue(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertNull(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.3");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertNull(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertTrue(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.4");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertTrue(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertTrue(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.5");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertTrue(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertNull(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.6");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertNull(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertTrue(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.7");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertTrue(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertTrue(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
    {
      String peerGroupName = "PG";
      AristaBgpPeerGroupNeighbor group = config.getAristaBgp().getPeerGroup(peerGroupName);
      assertThat(group, notNullValue());
      assertTrue(group.getSendCommunity());
      assertTrue(group.getSendExtendedCommunity());
    }
    {
      Ip ip = Ip.parse("1.1.1.8");
      assertThat(vrf.getV4neighbors(), hasKey(ip));
      assertNull(vrf.getV4neighbors().get(ip).getSendCommunity());
      assertNull(vrf.getV4neighbors().get(ip).getSendExtendedCommunity());
    }
  }

  @Test
  public void testBgpSendCommunityConversion() {
    Configuration config = parseConfig("arista_bgp_send_community");
    BgpProcess proc = config.getDefaultVrf().getBgpProcess();
    {
      Ip ip = Ip.parse("1.1.1.1");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.2");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(false)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.3");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(false)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.4");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.5");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(false)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.6");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(false)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
    {
      Ip ip = Ip.parse("1.1.1.7");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
    {
      // honor inheritance
      Ip ip = Ip.parse("1.1.1.8");
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(hasAddressFamilyCapabilites(hasSendCommunity(true)))));
      assertThat(
          proc,
          hasActiveNeighbor(
              ip,
              hasIpv4UnicastAddressFamily(
                  hasAddressFamilyCapabilites(hasSendExtendedCommunity(true)))));
    }
  }

  @Test
  public void testAllowasInExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_allowas_in");
    assertThat(config.getAristaBgp().getDefaultVrf().getAllowAsIn(), equalTo(2));
  }

  @Test
  public void testAllowasInConversion() {
    Configuration c = parseConfig("arista_bgp_allowas_in");
    {
      BgpActivePeerConfig peerConfig =
          c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("1.1.1.1"));
      assertTrue(
          peerConfig
              .getIpv4UnicastAddressFamily()
              .getAddressFamilyCapabilities()
              .getAllowLocalAsIn());
    }
    {
      BgpActivePeerConfig peerConfig =
          c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("2.2.2.2"));
      assertTrue(
          peerConfig
              .getIpv4UnicastAddressFamily()
              .getAddressFamilyCapabilities()
              .getAllowLocalAsIn());
      assertTrue(
          peerConfig.getEvpnAddressFamily().getAddressFamilyCapabilities().getAllowLocalAsIn());
    }
  }

  @Test
  public void testParseCvx() {
    parseVendorConfig("arista_cvx");
    // don't crash.
  }

  @Test
  public void testParseEmail() {
    parseVendorConfig("arista_email");
    // don't crash.
  }

  @Test
  public void testParseErrdisable() {
    parseVendorConfig("arista_errdisable");
    // don't crash.
  }

  @Test
  public void testParseHardware() {
    parseVendorConfig("arista_hardware");
    // don't crash.
  }

  @Test
  public void testParseIgmp() {
    parseVendorConfig("arista_igmp");
    // don't crash.
  }

  @Test
  public void testNatParse() {
    parseVendorConfig("arista_nat");
    // don't crash.
  }

  @Test
  public void testNatConvert() {
    parseConfig("arista_nat");
    // don't crash.
  }

  @Test
  public void testParseAclShowRunAll() {
    Configuration c = parseConfig("arista_acl_show_run_all");
    // Tests that the ACLs parse.
    assertThat(c, hasIpAccessList("SOME_ACL", hasLines(hasSize(1))));
    assertThat(c, hasIpAccessList("SOME_EXT_ACL", hasLines(hasSize(1))));
  }

  @Test
  public void testParseBgpShowRunAll() {
    AristaConfiguration c = parseVendorConfig("arista_bgp_show_run_all");
    // Test relies on route-maps configured as the last line of specific address families.
    assertThat(c.getAristaBgp().getPeerGroups().keySet(), containsInAnyOrder("SOME_GROUP"));
    AristaBgpVrf defaultVrf = c.getAristaBgp().getDefaultVrf();
    Ip neighborIp = Ip.parse("192.0.2.7");
    assertThat(defaultVrf.getV4neighbors().keySet(), contains(neighborIp));
    {
      AristaBgpNeighborAddressFamily evpn = defaultVrf.getEvpnAf().getNeighbor(neighborIp);
      assertThat(evpn, notNullValue());
      assertThat(evpn.getRouteMapIn(), equalTo("EVPN_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4u = defaultVrf.getV4UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv4u, notNullValue());
      assertThat(ipv4u.getRouteMapIn(), equalTo("IPV4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6u = defaultVrf.getV6UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv6u, notNullValue());
      assertThat(ipv6u.getRouteMapIn(), equalTo("IPV6_IN"));
    }
  }

  @Test
  public void testParseBgpShowRunAll2() {
    AristaConfiguration c = parseVendorConfig("arista_bgp_show_run_all_2");
    // Test relies on route-maps configured as the last line of specific address families.
    assertThat(
        c.getAristaBgp().getPeerGroups().keySet(), containsInAnyOrder("SOME_GROUP", "OTHER_GROUP"));
    AristaBgpVrf defaultVrf = c.getAristaBgp().getDefaultVrf();
    Ip neighborIp = Ip.parse("192.0.2.7");
    assertThat(defaultVrf.getV4neighbors().keySet(), contains(neighborIp));
    {
      AristaBgpNeighborAddressFamily evpn = defaultVrf.getEvpnAf().getNeighbor(neighborIp);
      assertThat(evpn, notNullValue());
      assertThat(evpn.getRouteMapIn(), equalTo("EVPN_IN"));
    }
    {
      AristaBgpNeighborAddressFamily fs4 = defaultVrf.getFlowSpecV4Af().getNeighbor(neighborIp);
      assertThat(fs4, notNullValue());
      assertThat(fs4.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      AristaBgpNeighborAddressFamily fs6 = defaultVrf.getFlowSpecV6Af().getNeighbor(neighborIp);
      assertThat(fs6, notNullValue());
      assertThat(fs6.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      AristaBgpNeighborAddressFamily ipv4u = defaultVrf.getV4UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv4u, notNullValue());
      assertThat(ipv4u.getRouteMapIn(), equalTo("IPV4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4m = defaultVrf.getV4MulticastAf().getNeighbor(neighborIp);
      assertThat(ipv4m, notNullValue());
      assertThat(ipv4m.getRouteMapIn(), equalTo("IPV4MC_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4lu =
          defaultVrf.getV4LabeledUnicastAf().getNeighbor(neighborIp);
      assertThat(ipv4lu, notNullValue());
      assertThat(ipv4lu.getRouteMapIn(), equalTo("IPV4LU_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6lu =
          defaultVrf.getV6LabeledUnicastAf().getNeighbor(neighborIp);
      assertThat(ipv6lu, notNullValue());
      assertThat(ipv6lu.getRouteMapIn(), equalTo("IPV6LU_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6u = defaultVrf.getV6UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv6u, notNullValue());
      assertThat(ipv6u.getRouteMapIn(), equalTo("IPV6_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6m = defaultVrf.getV6MulticastAf().getNeighbor(neighborIp);
      assertThat(ipv6m, notNullValue());
      assertThat(ipv6m.getRouteMapIn(), equalTo("IPV6MC_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4sr = defaultVrf.getV4SrTeAf().getNeighbor(neighborIp);
      assertThat(ipv4sr, notNullValue());
      assertThat(ipv4sr.getRouteMapIn(), equalTo("IPV4SRTE_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6sr = defaultVrf.getV6SrTeAf().getNeighbor(neighborIp);
      assertThat(ipv6sr, notNullValue());
      assertThat(ipv6sr.getRouteMapIn(), equalTo("IPV6SRTE_IN"));
    }
    {
      AristaBgpNeighborAddressFamily vpn4 = defaultVrf.getVpnV4Af().getNeighbor(neighborIp);
      assertThat(vpn4, notNullValue());
      assertThat(vpn4.getRouteMapIn(), equalTo("VPN4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily vpn6 = defaultVrf.getVpnV6Af().getNeighbor(neighborIp);
      assertThat(vpn6, notNullValue());
      assertThat(vpn6.getRouteMapIn(), equalTo("VPN6_IN"));
    }
  }

  @Test
  public void testParseBgpShowRunAll3() {
    AristaConfiguration c = parseVendorConfig("arista_bgp_show_run_all_3");
    // Test relies on route-maps configured as the last line of specific address families.
    assertThat(c.getAristaBgp().getVrfs().keySet(), containsInAnyOrder("default", "a"));
    assertThat(c.getAristaBgp().getPeerGroups().keySet(), containsInAnyOrder("SOME_GROUP"));
    Ip neighborIp = Ip.parse("192.0.2.7");
    AristaBgpVrf defaultVrf = c.getAristaBgp().getDefaultVrf();
    assertThat(defaultVrf.getV4neighbors().keySet(), contains(neighborIp));
    {
      AristaBgpNeighborAddressFamily evpn = defaultVrf.getEvpnAf().getNeighbor(neighborIp);
      assertThat(evpn, notNullValue());
      assertThat(evpn.getRouteMapIn(), equalTo("EVPN_IN"));
    }
    {
      AristaBgpNeighborAddressFamily fs4 = defaultVrf.getFlowSpecV4Af().getNeighbor(neighborIp);
      assertThat(fs4, notNullValue());
      assertThat(fs4.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      AristaBgpNeighborAddressFamily fs6 = defaultVrf.getFlowSpecV6Af().getNeighbor(neighborIp);
      assertThat(fs6, notNullValue());
      assertThat(fs6.getActivate(), equalTo(Boolean.TRUE));
    }
    {
      AristaBgpNeighborAddressFamily ipv4u = defaultVrf.getV4UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv4u, notNullValue());
      assertThat(ipv4u.getRouteMapIn(), equalTo("IPV4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4m = defaultVrf.getV4MulticastAf().getNeighbor(neighborIp);
      assertThat(ipv4m, notNullValue());
      assertThat(ipv4m.getRouteMapIn(), equalTo("IPV4MC_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4lu =
          defaultVrf.getV4LabeledUnicastAf().getNeighbor(neighborIp);
      assertThat(ipv4lu, notNullValue());
      assertThat(ipv4lu.getRouteMapIn(), equalTo("IPV4LU_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6lu =
          defaultVrf.getV6LabeledUnicastAf().getNeighbor(neighborIp);
      assertThat(ipv6lu, notNullValue());
      assertThat(ipv6lu.getRouteMapIn(), equalTo("IPV6LU_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6u = defaultVrf.getV6UnicastAf().getNeighbor(neighborIp);
      assertThat(ipv6u, notNullValue());
      assertThat(ipv6u.getRouteMapIn(), equalTo("IPV6_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6m = defaultVrf.getV6MulticastAf().getNeighbor(neighborIp);
      assertThat(ipv6m, notNullValue());
      assertThat(ipv6m.getRouteMapIn(), equalTo("IPV6MC_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4sr = defaultVrf.getV4SrTeAf().getNeighbor(neighborIp);
      assertThat(ipv4sr, notNullValue());
      assertThat(ipv4sr.getRouteMapIn(), equalTo("IPV4SRTE_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6sr = defaultVrf.getV6SrTeAf().getNeighbor(neighborIp);
      assertThat(ipv6sr, notNullValue());
      assertThat(ipv6sr.getRouteMapIn(), equalTo("IPV6SRTE_IN"));
    }
    {
      AristaBgpNeighborAddressFamily vpn4 = defaultVrf.getVpnV4Af().getNeighbor(neighborIp);
      assertThat(vpn4, notNullValue());
      assertThat(vpn4.getRouteMapIn(), equalTo("VPN4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily vpn6 = defaultVrf.getVpnV6Af().getNeighbor(neighborIp);
      assertThat(vpn6, notNullValue());
      assertThat(vpn6.getRouteMapIn(), equalTo("VPN6_IN"));
    }
    /// vrf
    AristaBgpVrf vrfA = c.getAristaBgp().getVrfs().get("a");
    Ip neighborIpA = Ip.parse("192.0.2.8");
    assertThat(vrfA.getV4neighbors().keySet(), contains(neighborIpA));
    {
      AristaBgpNeighborAddressFamily ipv4u = vrfA.getV4UnicastAf().getNeighbor(neighborIpA);
      assertThat(ipv4u, notNullValue());
      assertThat(ipv4u.getRouteMapIn(), equalTo("A-IPV4_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv4m = vrfA.getV4MulticastAf().getNeighbor(neighborIpA);
      assertThat(ipv4m, notNullValue());
      assertThat(ipv4m.getRouteMapIn(), equalTo("A-IPV4MC_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6u = vrfA.getV6UnicastAf().getNeighbor(neighborIpA);
      assertThat(ipv6u, notNullValue());
      assertThat(ipv6u.getRouteMapIn(), equalTo("A-IPV6_IN"));
    }
    {
      AristaBgpNeighborAddressFamily ipv6m = vrfA.getV6MulticastAf().getNeighbor(neighborIpA);
      assertThat(ipv6m, notNullValue());
      assertThat(ipv6m.getRouteMapIn(), equalTo("A-IPV6MC_IN"));
    }
  }

  @Test
  public void testParseBgpShowRunAll4() {
    AristaConfiguration c = parseVendorConfig("arista_bgp_show_run_all_4");
    assertThat(c.getAristaBgp().getVrfs().keySet(), containsInAnyOrder("default", "VRF10"));
    AristaBgpVrf defaultVrf = c.getAristaBgp().getDefaultVrf();
    assertThat(defaultVrf.getV4neighbors().keySet(), empty());
    /// vrf
    AristaBgpVrf vrf = c.getAristaBgp().getVrfs().get("VRF10");
    Ip neighborIp = Ip.parse("10.2.3.4");
    assertThat(vrf.getV4neighbors().keySet(), contains(neighborIp));
    /// pg
    AristaBgpPeerGroupNeighbor pg = c.getAristaBgp().getPeerGroup("SOME_GROUP");
    assertThat(pg, notNullValue());
    assertThat(pg.getGenericAddressFamily().getRouteMapIn(), equalTo("SOME_IMPORT"));
    assertThat(pg.getGenericAddressFamily().getRouteMapOut(), equalTo("SOME_EXPORT"));
    assertThat(defaultVrf.getV4UnicastAf().getPeerGroup("SOME_GROUP").getRouteMapIn(), nullValue());
    assertThat(
        defaultVrf.getV4UnicastAf().getPeerGroup("SOME_GROUP").getRouteMapOut(), nullValue());
  }

  @Test
  public void testParseInterfaceShowRunAll() {
    Configuration c = parseConfig("arista_interface_show_run_all");
    // Test relies on the last line in each interface being this description.
    assertThat(c, hasInterface("Ethernet1/1", hasDescription("Made it to the end of Ethernet1/1")));
    assertThat(c, hasInterface("Ethernet1/2", hasDescription("Made it to the end of Ethernet1/2")));
    assertThat(c, hasInterface("Loopback0", hasDescription("Made it to the end of Loopback0")));
    assertThat(c, hasInterface("Management1", hasDescription("Made it to the end of Management1")));
  }

  @Test
  public void testParseInterfaceShowRunAll2() {
    Configuration c = parseConfig("arista_interface_show_run_all_2");
    // Test relies on the last line in each interface being this description.
    assertThat(c, hasInterface("Ethernet1/1", hasDescription("Made it to the end of Ethernet1/1")));
    assertThat(c, hasInterface("Ethernet1/3", hasDescription("Made it to the end of Ethernet1/3")));
    assertThat(c, hasInterface("Loopback0", hasDescription("Made it to the end of Loopback0")));
    assertThat(c, hasInterface("Management1", hasDescription("Made it to the end of Management1")));
  }

  @Test
  public void testParseInterfaceShowRunAll3() {
    Configuration c = parseConfig("arista_interface_show_run_all_3");
    // Test relies on the last line in each interface being this description.
    assertThat(c, hasInterface("Ethernet1/1", hasDescription("Made it to the end of Ethernet1/1")));
    assertThat(c, hasInterface("Ethernet1/3", hasDescription("Made it to the end of Ethernet1/3")));
    assertThat(c, hasInterface("Loopback0", hasDescription("Made it to the end of Loopback0")));
    assertThat(c, hasInterface("Management1", hasDescription("Made it to the end of Management1")));
  }

  @Test
  public void testParseLoggingShowRunAll() {
    Configuration c = parseConfig("arista_logging_show_run_all");
    assertThat(
        c.getLoggingServers(),
        containsInAnyOrder("1.2.3.4", "1.2.3.5", "1.2.3.6", "some_host.company.com"));
    assertThat(c.getLoggingSourceInterface(), equalTo("Loopback0"));
  }

  @Test
  public void testEnforceFirstAsExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_enforce_first_as");
    assertThat(config.getAristaBgp().getDefaultVrf().getEnforceFirstAs(), equalTo(Boolean.TRUE));
  }

  @Test
  public void testEnforceFirstAsConversion() {
    Configuration c = parseConfig("arista_bgp_enforce_first_as");
    BgpActivePeerConfig peerConfig =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("1.1.1.1"));
    assertTrue(peerConfig.getEnforceFirstAs());
  }

  @Test
  public void testNeighborPrefixListExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_neighbor_prefix_list");
    assertThat(
        config
            .getAristaBgp()
            .getDefaultVrf()
            .getV4neighbors()
            .get(Ip.parse("1.1.1.1"))
            .getGenericAddressFamily()
            .getPrefixListIn(),
        equalTo("PREFIX_LIST_IN"));
    assertThat(
        config
            .getAristaBgp()
            .getDefaultVrf()
            .getV4neighbors()
            .get(Ip.parse("1.1.1.1"))
            .getGenericAddressFamily()
            .getPrefixListOut(),
        equalTo("PREFIX_LIST_OUT"));
  }

  @Test
  public void testNeighborPrefixListConversion() {
    Configuration c = parseConfig("arista_bgp_neighbor_prefix_list");

    RoutingPolicy exportPolicy =
        c.getRoutingPolicies()
            .get(
                c.getDefaultVrf()
                    .getBgpProcess()
                    .getActiveNeighbors()
                    .get(Ip.parse("1.1.1.1"))
                    .getIpv4UnicastAddressFamily()
                    .getExportPolicy());
    RoutingPolicy importPolicy =
        c.getRoutingPolicies()
            .get(
                c.getDefaultVrf()
                    .getBgpProcess()
                    .getActiveNeighbors()
                    .get(Ip.parse("1.1.1.1"))
                    .getIpv4UnicastAddressFamily()
                    .getImportPolicy());

    // assert on the behavior of routing policies
    Builder originalRoute =
        Bgpv4Route.testBuilder()
            .setNextHopIp(Ip.parse("6.7.8.9"))
            .setAdmin(1)
            .setOriginatorIp(Ip.parse("9.8.7.6"))
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP);
    Bgpv4Route.Builder outputRouteBuilder =
        Bgpv4Route.testBuilder().setNextHopIp(UNSET_ROUTE_NEXT_HOP_IP);

    Ip sessionPropsHeadIp = Ip.parse("1.1.1.1");
    BgpSessionProperties.Builder sessionProps =
        BgpSessionProperties.builder()
            .setHeadAs(1L)
            .setTailAs(1L)
            .setHeadIp(sessionPropsHeadIp)
            .setTailIp(Ip.parse("2.2.2.2"));
    BgpSessionProperties session = sessionProps.setSessionType(SessionType.IBGP).build();

    Prefix allowedIn = Prefix.parse("10.1.2.0/24");
    Prefix allowedOut = Prefix.parse("10.7.8.0/24");
    Prefix deniedBoth = Prefix.parse("10.3.4.0/24");

    assertTrue(
        importPolicy.processBgpRoute(
            originalRoute.setNetwork(allowedIn).build(),
            outputRouteBuilder,
            session,
            Direction.IN,
            null));
    assertFalse(
        importPolicy.processBgpRoute(
            originalRoute.setNetwork(deniedBoth).build(),
            outputRouteBuilder,
            session,
            Direction.IN,
            null));
    assertTrue(
        exportPolicy.processBgpRoute(
            originalRoute.setNetwork(allowedOut).build(),
            outputRouteBuilder,
            session,
            Direction.OUT,
            null));
    assertFalse(
        exportPolicy.processBgpRoute(
            originalRoute.setNetwork(deniedBoth).build(),
            outputRouteBuilder,
            session,
            Direction.OUT,
            null));
  }

  @Test
  public void testNextHopUnchangedExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_nexthop_unchanged");
    {
      AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
      assertTrue(vrf.getV4neighbors().get(Ip.parse("9.9.9.9")).getNextHopUnchanged());
      assertNull(vrf.getV4neighbors().get(Ip.parse("8.8.8.8")).getNextHopUnchanged());
      assertTrue(vrf.getEvpnAf().getNextHopUnchanged());
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf2");
      assertTrue(vrf.getNextHopUnchanged());
      assertNull(vrf.getV4neighbors().get(Ip.parse("2.2.2.2")).getNextHopUnchanged());
      assertNull(vrf.getV4neighbors().get(Ip.parse("2.2.2.22")).getNextHopUnchanged());
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf3");
      assertNull(vrf.getNextHopUnchanged());
      assertNull(vrf.getV4neighbors().get(Ip.parse("3.3.3.3")).getNextHopUnchanged());
      assertNull(vrf.getV4neighbors().get(Ip.parse("3.3.3.33")).getNextHopUnchanged());
      assertTrue(vrf.getV4UnicastAf().getNeighbor(Ip.parse("3.3.3.3")).getNextHopUnchanged());
      assertNull(vrf.getV4UnicastAf().getNeighbor(Ip.parse("3.3.3.33")).getNextHopUnchanged());
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf4");
      assertTrue(vrf.getV4UnicastAf().getNextHopUnchanged());
    }
  }

  @Test
  public void testNextHopUnchangedConversion() {
    Configuration c = parseConfig("arista_bgp_nexthop_unchanged");
    Ip nextHopIp = Ip.parse("42.42.42.42");
    Bgpv4Route originalRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.2.3.0/24"))
            .setNextHopIp(nextHopIp)
            .setAdmin(1)
            .setOriginatorIp(nextHopIp)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("1.1.1.2");
    BgpSessionProperties session =
        BgpSessionProperties.builder()
            .setHeadAs(1)
            .setTailAs(2)
            .setHeadIp(headIp)
            .setTailIp(tailIp)
            .setSessionType(SessionType.EBGP_SINGLEHOP)
            .build();
    {
      // 9.9.9.9 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName(DEFAULT_VRF, "9.9.9.9"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(nextHopIp));
    }
    {
      // 8.8.8.8 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName(DEFAULT_VRF, "8.8.8.8"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(UNSET_ROUTE_NEXT_HOP_IP));
    }
    {
      // 8.8.8.8 for EVPN
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerEvpnExportPolicyName(DEFAULT_VRF, "8.8.8.8"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(nextHopIp));
    }
    {
      // 7.7.7.7 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName(DEFAULT_VRF, "7.7.7.7"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(UNSET_ROUTE_NEXT_HOP_IP));
    }
    {
      // 2.2.2.2 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName("vrf2", "2.2.2.2"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(nextHopIp));
    }
    {
      // 2.2.2.22 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName("vrf2", "2.2.2.22"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(nextHopIp));
    }
    {
      // 3.3.3.3 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName("vrf3", "3.3.3.3"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(nextHopIp));
    }
    {
      // 3.3.3.33 for IPv4
      RoutingPolicy policy =
          c.getRoutingPolicies().get(generatedBgpPeerExportPolicyName("vrf3", "3.3.3.33"));
      Builder builder = Bgpv4Route.testBuilder();
      policy.processBgpRoute(originalRoute, builder, session, Direction.OUT, null);
      assertThat(builder.getNextHopIp(), equalTo(UNSET_ROUTE_NEXT_HOP_IP));
    }
  }

  @Test
  public void testConfederationExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_confederations");
    {
      AristaBgpVrf vrf = config.getAristaBgp().getDefaultVrf();
      assertThat(vrf.getConfederationIdentifier(), equalTo(1111L));
      assertThat(vrf.getConfederationPeers(), equalTo(LongSpace.of(Range.closed(3L, 6L))));
    }
    {
      AristaBgpVrf vrf = config.getAristaBgp().getVrfs().get("vrf2");
      assertThat(vrf.getConfederationIdentifier(), equalTo((22L << 16) + 22));
      assertThat(
          vrf.getConfederationPeers(),
          equalTo(
              LongSpace.unionOf(
                  Range.closed((1L << 16) + 1, (1L << 16) + 2),
                  Range.singleton((3L << 16) + 3),
                  Range.singleton(44L))));
    }
  }

  @Test
  public void testConfederationConversion() {
    Configuration c = parseConfig("arista_bgp_confederations");
    {
      BgpConfederation confederation = c.getDefaultVrf().getBgpProcess().getConfederation();
      assertThat(confederation.getId(), equalTo(1111L));
      assertThat(confederation.getMembers(), equalTo(ImmutableSet.of(3L, 4L, 5L, 6L)));
    }
    {
      BgpConfederation confederation = c.getVrfs().get("vrf2").getBgpProcess().getConfederation();

      assertThat(confederation.getId(), equalTo((22L << 16) + 22));
      assertThat(
          confederation.getMembers(),
          containsInAnyOrder((1L << 16) + 1, (1L << 16) + 2, (3L << 16) + 3, 44L));
    }
  }

  @Test
  public void testEvpnImportPolicyExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_evpn_import_policy");
    AristaBgpNeighborAddressFamily neighbor =
        config.getAristaBgp().getDefaultVrf().getEvpnAf().getNeighbor(Ip.parse("2.2.2.2"));
    assertThat(neighbor.getRouteMapIn(), equalTo("ALLOW_10"));
  }

  @Test
  public void testEvpnImportPolicyConversion() {
    Configuration config = parseConfig("arista_bgp_evpn_import_policy");
    String policyName =
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("2.2.2.2"))
            .getEvpnAddressFamily()
            .getImportPolicy();
    RoutingPolicy policy = config.getRoutingPolicies().get(policyName);

    Builder builder =
        Bgpv4Route.testBuilder()
            .setNextHopIp(Ip.parse("5.5.5.5"))
            .setAdmin(1)
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP);

    Bgpv4Route acceptRoute = builder.setNetwork(Prefix.parse("10.1.1.0/24")).build();
    Bgpv4Route denyRoute = builder.setNetwork(Prefix.parse("240.1.1.0/24")).build();
    assertTrue(
        policy.processBgpRoute(acceptRoute, Bgpv4Route.testBuilder(), null, Direction.IN, null));
    assertFalse(
        policy.processBgpRoute(denyRoute, Bgpv4Route.testBuilder(), null, Direction.IN, null));
  }

  @Test
  public void testIpNatSourceStaticExtraction() {
    AristaConfiguration c = parseVendorConfig("ip-nat-source-static");
    assertThat(c.getExtendedAcls(), hasKeys("ACL", "INVALID_ACL"));
    assertThat(c.getInterfaces(), hasKeys("Ethernet1", "Ethernet2"));
    {
      org.batfish.representation.arista.Interface ethernet1 = c.getInterfaces().get("Ethernet1");
      assertThat(ethernet1.getStaticSourceNats(), hasSize(4));
      Iterator<AristaStaticSourceNat> nats = ethernet1.getStaticSourceNats().iterator();
      {
        AristaStaticSourceNat nat = nats.next();
        assertThat(nat.getOriginalIp(), equalTo(Ip.parse("1.1.1.1")));
        assertThat(nat.getOriginalPort(), nullValue());
        assertThat(nat.getExtendedAclName(), nullValue());
        assertThat(nat.getTranslatedIp(), equalTo(Ip.parse("2.2.2.2")));
        assertThat(nat.getTranslatedPort(), nullValue());
        assertThat(nat.getProtocol(), equalTo(Protocol.ANY));
      }
      {
        AristaStaticSourceNat nat = nats.next();
        assertThat(nat.getOriginalIp(), equalTo(Ip.parse("3.3.3.3")));
        assertThat(nat.getOriginalPort(), equalTo(33));
        assertThat(nat.getExtendedAclName(), nullValue());
        assertThat(nat.getTranslatedIp(), equalTo(Ip.parse("4.4.4.4")));
        assertThat(nat.getTranslatedPort(), equalTo(44));
        assertThat(nat.getProtocol(), equalTo(Protocol.ANY));
      }
      {
        AristaStaticSourceNat nat = nats.next();
        assertThat(nat.getOriginalIp(), equalTo(Ip.parse("5.5.5.5")));
        assertThat(nat.getOriginalPort(), equalTo(55));
        assertThat(nat.getExtendedAclName(), equalTo("ACL"));
        assertThat(nat.getTranslatedIp(), equalTo(Ip.parse("6.6.6.6")));
        assertThat(nat.getTranslatedPort(), equalTo(66));
        assertThat(nat.getProtocol(), equalTo(Protocol.TCP));
      }
      {
        AristaStaticSourceNat nat = nats.next();
        assertThat(nat.getOriginalIp(), equalTo(Ip.parse("7.7.7.7")));
        assertThat(nat.getOriginalPort(), equalTo(77));
        assertThat(nat.getExtendedAclName(), equalTo("ACL"));
        assertThat(nat.getTranslatedIp(), equalTo(Ip.parse("8.8.8.8")));
        assertThat(nat.getTranslatedPort(), equalTo(88));
        assertThat(nat.getProtocol(), equalTo(Protocol.UDP));
      }
    }
    {
      // Shallow test of Ethernet2, since we did extraction tests above.
      org.batfish.representation.arista.Interface ethernet2 = c.getInterfaces().get("Ethernet2");
      assertThat(ethernet2.getStaticSourceNats(), hasSize(2));
    }
  }

  @Test
  public void testIpNatSourceStaticConversion() {
    AristaConfiguration vendor = parseVendorConfig("ip-nat-source-static");
    Warnings w = new Warnings(true, true, true);
    vendor.setWarnings(w);
    Configuration c = Iterables.getOnlyElement(vendor.toVendorIndependentConfigurations());
    // Check that IP Spaces are extracted, and warnings are appropriate.
    assertThat(
        c.getIpSpaces(),
        hasKeys(
            nameOfSourceNatIpSpaceFromAcl("ACL"), nameOfSourceNatIpSpaceFromAcl("INVALID_ACL")));
    assertThat(
        c.getIpSpaces().get(nameOfSourceNatIpSpaceFromAcl("ACL")),
        equalTo(
            AclIpSpace.builder()
                .thenPermitting(IpWildcard.parse("9.9.9.9").toIpSpace())
                .thenRejecting(IpWildcard.parse("10.10.10.0/24").toIpSpace())
                .thenPermitting(IpWildcard.parse("11.11.11.11").toIpSpace())
                .build()));
    assertThat(
        c.getIpSpaces().get(nameOfSourceNatIpSpaceFromAcl("INVALID_ACL")),
        equalTo(AclIpSpace.builder().build()));
    assertThat(
        w.getRedFlagWarnings(),
        containsInAnyOrder(
            WarningMatchers.hasText(
                "INVALID_ACL line permit ip any any: destination address cannot be 'any'"),
            WarningMatchers.hasText(
                "INVALID_ACL line permit ip host 1.2.3.4 host 9.9.9.9: source address must be"
                    + " 'any'"),
            WarningMatchers.hasText(
                "INVALID_ACL line permit tcp any host 10.10.10.10: cannot filter on anything but"
                    + " destination address"),
            WarningMatchers.hasText(
                "INVALID_ACL line permit ip any host 11.11.11.11 fragments: cannot filter on"
                    + " anything but destination address"),
            WarningMatchers.hasText(
                "ip nat source commands referencing nonexistent ACL UNDEFINED_ACL are not installed"
                    + " until the ACL is created")));
    // Check that applied transformations are correct.
    {
      Transformation e1out = c.getAllInterfaces().get("Ethernet1").getOutgoingTransformation();
      assertThat(e1out.getGuard(), equalTo(AclLineMatchExprs.and(matchSrc(Ip.parse("1.1.1.1")))));
      assertThat(e1out.getTransformationSteps(), contains(assignSourceIp(Ip.parse("2.2.2.2"))));
      assertThat(e1out.getAndThen(), nullValue());
      assertThat(e1out.getOrElse(), notNullValue());
      // We do deep testing of the conversion to Transformation elsewhere.
    }
    {
      Transformation e1in = c.getAllInterfaces().get("Ethernet1").getIncomingTransformation();
      assertThat(e1in.getGuard(), equalTo(AclLineMatchExprs.and(matchDst(Ip.parse("2.2.2.2")))));
      assertThat(e1in.getTransformationSteps(), contains(assignDestinationIp(Ip.parse("1.1.1.1"))));
      assertThat(e1in.getAndThen(), nullValue());
      assertThat(e1in.getOrElse(), notNullValue());
      // We do deep testing of the conversion to Transformation elsewhere.
    }
    assertThat(
        c.getAllInterfaces().get("Ethernet2").getIncomingTransformation(),
        equalTo(
            when(and(
                    matchDst(Ip.parse("2.2.2.2")),
                    matchSrc(new IpSpaceReference(nameOfSourceNatIpSpaceFromAcl("INVALID_ACL")))))
                .apply(assignDestinationIp(Ip.parse("1.1.1.1")))
                .build()));
    assertThat(
        c.getAllInterfaces().get("Ethernet2").getOutgoingTransformation(),
        equalTo(
            when(and(
                    matchSrc(Ip.parse("1.1.1.1")),
                    matchDst(new IpSpaceReference(nameOfSourceNatIpSpaceFromAcl("INVALID_ACL")))))
                .apply(assignSourceIp(Ip.parse("2.2.2.2")))
                .build()));
  }

  @Test
  public void testIpv6AccessListParsing() {
    // Don't crash or warn
    parseConfig("ipv6_acl");
  }

  @Test
  public void testIpv6RouteParsing() {
    // Don't crash
    parseConfig("ipv6_route");
  }

  @Test
  public void testQos() throws IOException {
    String hostname = "qos";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(
        ccae,
        allOf(
            hasNumReferrers(filename, POLICY_MAP, "my-pdp-policy", 1),
            hasNumReferrers(filename, POLICY_MAP, "my-shared-pdp-policy", 1),
            hasNumReferrers(filename, POLICY_MAP, "copp-system-policy", 0),
            hasNoUndefinedReferences()));
  }

  @Test
  public void testRouteMapExtraction() {
    AristaConfiguration c = parseVendorConfig("route_map");
    assertThat(c.getRouteMaps(), hasKeys("map1", "DANAIL_PETROV_20201103", "ACTION_CHANGES"));
    {
      RouteMap rm = c.getRouteMaps().get("DANAIL_PETROV_20201103");
      assertThat(rm.getClauses(), hasKeys(10));
      RouteMapClause clause = rm.getClauses().get(10);
      assertThat(clause.getAction(), equalTo(LineAction.PERMIT));
      assertThat(clause.getMatchList(), hasSize(1));
      assertThat(clause.getSetList(), hasSize(1));
    }
    {
      RouteMap rm = c.getRouteMaps().get("ACTION_CHANGES");
      assertThat(rm.getClauses(), hasKeys(10));
      RouteMapClause clause = rm.getClauses().get(10);
      // Action changed from permit to deny
      assertThat(clause.getAction(), equalTo(LineAction.DENY));
      // Clauses were merged
      assertThat(clause.getMatchList(), hasSize(1));
      assertThat(clause.getSetList(), hasSize(1));
    }
  }

  @Test
  public void testRouteMapParsing() {
    // Don't crash
    parseConfig("route_map");
  }

  private @Nonnull Bgpv4Route processRouteIn(RoutingPolicy routingPolicy, Bgpv4Route route) {
    Bgpv4Route.Builder builder = route.toBuilder();
    assertTrue(routingPolicy.process(route, builder, Direction.IN));
    return builder.build();
  }

  private void assertRoutingPolicyDeniesRoute(RoutingPolicy routingPolicy, AbstractRoute route) {
    assertFalse(
        routingPolicy.process(
            route, Bgpv4Route.testBuilder().setNetwork(route.getNetwork()), Direction.OUT));
  }

  @Test
  public void testRouteMapExhaustive() {
    Configuration c = parseConfig("arista_route_map_exhaustive");
    assertThat(c.getRoutingPolicies(), hasKey("RM"));
    RoutingPolicy rm = c.getRoutingPolicies().get("RM");
    Bgpv4Route base =
        Bgpv4Route.testBuilder()
            .setTag(0L)
            .setSrcProtocol(RoutingProtocol.BGP)
            .setMetric(0L)
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("192.0.2.254"))
            .setNetwork(Prefix.ZERO)
            .build();
    // There are 8 paths through the route-map, let's test them all.
    // 10 deny tag 1, continue                OR    fall-through
    // 20 permit community 0:2, continue      OR    fall-through
    // 30 deny 1.2.3.4/32, terminate          OR   40 terminate
    {
      // false false false -> 40 only
      Bgpv4Route after = processRouteIn(rm, base);
      assertThat(after.getTag(), not(equalTo(10L)));
      assertThat(after.getCommunities(), not(equalTo(CommunitySet.of(StandardCommunity.of(20)))));
      assertThat(after.getMetric(), not(equalTo(30L)));
      assertThat(after.getLocalPreference(), equalTo(40L));
    }
    {
      // false false true -> 30 only
      assertRoutingPolicyDeniesRoute(
          rm, base.toBuilder().setNetwork(Prefix.parse("1.2.3.4/32")).build());
    }
    {
      // false true false -> 20, 40
      Bgpv4Route after =
          processRouteIn(
              rm,
              base.toBuilder().setCommunities(CommunitySet.of(StandardCommunity.of(2))).build());
      assertThat(after.getTag(), not(equalTo(10L)));
      assertThat(after.getCommunities(), equalTo(CommunitySet.of(StandardCommunity.of(20))));
      assertThat(after.getMetric(), not(equalTo(30L)));
      assertThat(after.getLocalPreference(), equalTo(40L));
    }
    {
      // false true true -> 20, 30
      assertRoutingPolicyDeniesRoute(
          rm,
          base.toBuilder()
              .setCommunities(CommunitySet.of(StandardCommunity.of(2)))
              .setNetwork(Prefix.parse("1.2.3.4/32"))
              .build());
    }
    {
      // true false false -> 10, 40
      Bgpv4Route after = processRouteIn(rm, base.toBuilder().setTag(1L).build());
      assertThat(after.getTag(), equalTo(10L));
      assertThat(after.getCommunities(), not(equalTo(CommunitySet.of(StandardCommunity.of(20)))));
      assertThat(after.getMetric(), not(equalTo(30L)));
      assertThat(after.getLocalPreference(), equalTo(40L));
    }
    {
      // true false true -> 10, 30
      assertRoutingPolicyDeniesRoute(
          rm, base.toBuilder().setTag(1L).setNetwork(Prefix.parse("1.2.3.4/32")).build());
    }
    {
      // true true false -> 10, 20, 40
      Bgpv4Route after =
          processRouteIn(
              rm,
              base.toBuilder()
                  .setTag(1L)
                  .setCommunities(CommunitySet.of(StandardCommunity.of(2)))
                  .build());
      assertThat(after.getTag(), equalTo(10L));
      assertThat(after.getCommunities(), equalTo(CommunitySet.of(StandardCommunity.of(20))));
      assertThat(after.getMetric(), not(equalTo(30L)));
      assertThat(after.getLocalPreference(), equalTo(40L));
    }
    {
      // true true true -> 10, 20, 30
      assertRoutingPolicyDeniesRoute(
          rm,
          base.toBuilder()
              .setTag(1L)
              .setCommunities(CommunitySet.of(StandardCommunity.of(2)))
              .setNetwork(Prefix.parse("1.2.3.4/32"))
              .build());
    }
  }

  @Test
  public void testRouteMapNakedContinue() {
    Configuration c = parseConfig("arista_route_map_naked_continue");
    assertThat(c.getRoutingPolicies(), hasKey("RM"));
    RoutingPolicy rm = c.getRoutingPolicies().get("RM");
    Bgpv4Route base =
        Bgpv4Route.testBuilder()
            .setTag(0L)
            .setSrcProtocol(RoutingProtocol.BGP)
            .setMetric(0L)
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("192.0.2.254"))
            .setNetwork(Prefix.ZERO)
            .build();
    Bgpv4Route after = processRouteIn(rm, base);
    assertThat(after.getMetric(), equalTo(3L));
    assertThat(after.getCommunities(), equalTo(CommunitySet.of(StandardCommunity.of(1))));
  }

  @Test
  public void testSnmpExtraction() {
    Configuration config = parseConfig("arista_snmp");
    assertThat(config.getSnmpTrapServers(), containsInAnyOrder("10.1.2.3"));
    SnmpServer server = config.getDefaultVrf().getSnmpServer();
    assertThat(server.getCommunities(), hasKeys("STD_COMMUNITY", "EXT_COMMUNITY"));
    SnmpCommunity std = server.getCommunities().get("STD_COMMUNITY");
    assertThat(
        std.getClientIps(),
        equalTo(
            AclIpSpace.rejecting(IpWildcard.parse("1.2.3.0/24").toIpSpace())
                .thenPermitting(IpWildcard.parse("1.0.0.0/8").toIpSpace())
                .build()));
    SnmpCommunity ext = server.getCommunities().get("EXT_COMMUNITY");
    assertThat(
        ext.getClientIps(),
        equalTo(
            AclIpSpace.rejecting(IpWildcard.parse("1.2.3.0/24").toIpSpace())
                .thenPermitting(IpWildcard.parse("1.0.0.0/8").toIpSpace())
                .build()));
  }

  @Test
  public void testPeerFilterExtraction() {
    AristaConfiguration config = parseVendorConfig("arista_bgp_peer_filter");
    assertThat(config.getPeerFilters().keySet(), containsInAnyOrder("PF", "EMPTY"));
    assertThat(
        config
            .getAristaBgp()
            .getDefaultVrf()
            .getV4DynamicNeighbors()
            .get(Prefix.parse("1.1.1.0/24"))
            .getPeerFilter(),
        equalTo("PF"));
    assertThat(
        config
            .getAristaBgp()
            .getDefaultVrf()
            .getV4DynamicNeighbors()
            .get(Prefix.parse("2.2.2.0/24"))
            .getPeerFilter(),
        equalTo("EMPTY"));
    assertThat(
        config
            .getAristaBgp()
            .getDefaultVrf()
            .getV4DynamicNeighbors()
            .get(Prefix.parse("3.3.3.0/24"))
            .getPeerFilter(),
        equalTo("DOES_NOT_EXIST"));
  }

  @Test
  public void testPeerFilterConversion() {
    Configuration config = parseConfig("arista_bgp_peer_filter");
    assertThat(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getPassiveNeighbors()
            .get(Prefix.parse("1.1.1.0/24"))
            .getRemoteAsns(),
        equalTo(
            LongSpace.builder()
                .including(Range.closed(1L, 10L))
                .excluding(Range.closed(3L, 4L))
                .build()));
    assertThat(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getPassiveNeighbors()
            .get(Prefix.parse("2.2.2.0/24"))
            .getRemoteAsns(),
        equalTo(BgpPeerConfig.ALL_AS_NUMBERS));
    assertThat(
        config
            .getDefaultVrf()
            .getBgpProcess()
            .getPassiveNeighbors()
            .get(Prefix.parse("3.3.3.0/24"))
            .getRemoteAsns(),
        equalTo(BgpPeerConfig.ALL_AS_NUMBERS));
  }

  @Test
  public void testPrefixListSeq() {
    AristaConfiguration c = parseVendorConfig("prefix-list-seq");
    assertThat(
        c.getPrefixLists(), hasKeys("NOSEQ", "NOSEQ_THEN_SEQ", "SEQ_NO_SEQ", "OUT_OF_ORDER"));
    {
      PrefixList pl = c.getPrefixLists().get("NOSEQ");
      assertThat(pl.getLines(), hasKeys(10L));
      assertThat(
          pl.getLines().values().stream()
              .map(PrefixListLine::getPrefix)
              .collect(ImmutableList.toImmutableList()),
          contains(Prefix.parse("10.0.0.0/8")));
    }
    {
      PrefixList pl = c.getPrefixLists().get("NOSEQ_THEN_SEQ");
      assertThat(pl.getLines(), hasKeys(10L, 20L));
      assertThat(
          pl.getLines().values().stream()
              .map(PrefixListLine::getPrefix)
              .collect(ImmutableList.toImmutableList()),
          contains(Prefix.parse("10.0.0.0/8"), Prefix.parse("20.0.0.0/8")));
    }
    {
      PrefixList pl = c.getPrefixLists().get("SEQ_NO_SEQ");
      assertThat(pl.getLines(), hasKeys(15L, 25L, 30L));
      assertThat(
          pl.getLines().values().stream()
              .map(PrefixListLine::getPrefix)
              .collect(ImmutableList.toImmutableList()),
          contains(
              Prefix.parse("10.0.0.0/8"), Prefix.parse("20.0.0.0/8"), Prefix.parse("30.0.0.0/8")));
    }
    {
      PrefixList pl = c.getPrefixLists().get("OUT_OF_ORDER");
      assertThat(pl.getLines(), hasKeys(10L, 20L, 30L));
      assertThat(
          pl.getLines().values().stream()
              .map(PrefixListLine::getPrefix)
              .collect(ImmutableList.toImmutableList()),
          contains(
              Prefix.parse("10.0.0.0/8"), Prefix.parse("20.0.0.0/8"), Prefix.parse("30.0.0.0/8")));
    }
  }

  @Test
  public void testIpCommunityListExpandedConversion() throws IOException {
    String hostname = "arista_ip_community_list_expanded";
    Configuration c = parseConfig(hostname);
    CommunityContext ctx = CommunityContext.builder().build();

    // Each list should be converted to both a CommunityMatchExpr and a CommunitySetMatchExpr.
    {
      // Test CommunityMatchExpr conversion
      assertThat(c.getCommunityMatchExprs(), hasKeys("cl_test"));
      CommunityMatchExprEvaluator eval = ctx.getCommunityMatchExprEvaluator();
      {
        CommunityMatchExpr expr = c.getCommunityMatchExprs().get("cl_test");

        // no single community matched by regex _1:1.*2:2_, so deny line is NOP

        // permit regex _1:1_
        assertTrue(expr.accept(eval, StandardCommunity.of(1, 1)));
        assertFalse(expr.accept(eval, StandardCommunity.of(11, 11)));

        // permit regex _2:2_
        assertTrue(expr.accept(eval, StandardCommunity.of(2, 2)));
      }
    }
    {
      // Test CommunitySetMatchExpr conversion
      assertThat(c.getCommunitySetMatchExprs(), hasKeys("cl_test"));
      CommunitySetMatchExprEvaluator eval = ctx.getCommunitySetMatchExprEvaluator();
      {
        CommunitySetMatchExpr expr = c.getCommunitySetMatchExprs().get("cl_test");

        // deny regex _1:1.*2:2_
        assertFalse(
            expr.accept(
                eval, CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2))));
        assertFalse(
            expr.accept(
                eval,
                CommunitySet.of(
                    StandardCommunity.of(1, 1),
                    StandardCommunity.of(2, 2),
                    StandardCommunity.of(3, 3))));

        // permit regex _1:1_
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.of(1, 1))));
        assertTrue(
            expr.accept(
                eval, CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(3, 3))));
        assertFalse(expr.accept(eval, CommunitySet.of(StandardCommunity.of(11, 11))));

        // permit regex _2:2_
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.of(2, 2))));
      }
    }
    {
      // Test CommunitySet conversion
      assertThat(c.getCommunitySets(), hasKeys("cl_test"));
      {
        CommunitySet set = c.getCommunitySets().get("cl_test");
        assertThat(set.getCommunities(), empty());
      }
    }
  }

  @Test
  public void testIpCommunityListExpandedExtraction() {
    String hostname = "arista_ip_community_list_expanded";
    AristaConfiguration vc = parseVendorConfig(hostname);

    assertThat(vc.getExpandedCommunityLists(), hasKeys("cl_test"));
    {
      ExpandedCommunityList cl = vc.getExpandedCommunityLists().get("cl_test");
      Iterator<ExpandedCommunityListLine> lines = cl.getLines().iterator();
      ExpandedCommunityListLine line;

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.DENY));
      assertThat(line.getRegex(), equalTo("_1:1.*2:2_"));

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getRegex(), equalTo("_1:1_"));

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getRegex(), equalTo("_2:2_"));

      assertFalse(lines.hasNext());
    }
  }

  @Test
  public void testIpCommunityListStandardConversion() throws IOException {
    String hostname = "arista_ip_community_list_standard";
    Configuration c = parseConfig(hostname);
    CommunityContext ctx = CommunityContext.builder().build();

    // Each list should be converted to both a CommunityMatchExpr and a CommunitySetMatchExpr.
    {
      // Test CommunityMatchExpr conversion
      assertThat(c.getCommunityMatchExprs(), hasKeys("cl_values", "cl_test"));
      CommunityMatchExprEvaluator eval = ctx.getCommunityMatchExprEvaluator();
      {
        CommunityMatchExpr expr = c.getCommunityMatchExprs().get("cl_values");

        // permit 1:1
        assertTrue(expr.accept(eval, StandardCommunity.of(1, 1)));
        // permit internet
        assertTrue(expr.accept(eval, StandardCommunity.INTERNET));
        // permit local-AS
        assertTrue(expr.accept(eval, StandardCommunity.NO_EXPORT_SUBCONFED));
        // permit no-advertise
        assertTrue(expr.accept(eval, StandardCommunity.NO_ADVERTISE));
        // permit no-export
        assertTrue(expr.accept(eval, StandardCommunity.NO_EXPORT));
      }
      {
        CommunityMatchExpr expr = c.getCommunityMatchExprs().get("cl_test");

        // no single community matched by 1:1 2:2, so deny line is NOP

        // permit 1:1
        assertTrue(expr.accept(eval, StandardCommunity.of(1, 1)));
        // permit 2:2
        assertTrue(expr.accept(eval, StandardCommunity.of(2, 2)));
      }
    }
    {
      // Test CommunitySetMatchExpr conversion
      assertThat(c.getCommunitySetMatchExprs(), hasKeys("cl_values", "cl_test"));
      CommunitySetMatchExprEvaluator eval = ctx.getCommunitySetMatchExprEvaluator();
      {
        CommunitySetMatchExpr expr = c.getCommunitySetMatchExprs().get("cl_values");

        // permit 1:1
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.of(1, 1))));
        // permit internet
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.INTERNET)));
        // permit local-AS
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.NO_EXPORT_SUBCONFED)));
        // permit no-advertise
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.NO_ADVERTISE)));
        // permit no-export
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.NO_EXPORT)));
      }
      {
        CommunitySetMatchExpr expr = c.getCommunitySetMatchExprs().get("cl_test");

        // deny 1:1 2:2
        assertFalse(
            expr.accept(
                eval, CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2))));
        assertFalse(
            expr.accept(
                eval,
                CommunitySet.of(
                    StandardCommunity.of(1, 1),
                    StandardCommunity.of(2, 2),
                    StandardCommunity.of(3, 3))));

        // permit 1:1
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.of(1, 1))));
        assertTrue(
            expr.accept(
                eval, CommunitySet.of(StandardCommunity.of(1, 1), StandardCommunity.of(3, 3))));

        // permit 2:2
        assertTrue(expr.accept(eval, CommunitySet.of(StandardCommunity.of(2, 2))));
      }
    }
    {
      // Test CommunitySet conversion
      assertThat(c.getCommunitySets(), hasKeys("cl_values", "cl_test"));
      {
        CommunitySet set = c.getCommunitySets().get("cl_values");
        assertThat(
            set.getCommunities(),
            containsInAnyOrder(
                StandardCommunity.of(1, 1),
                StandardCommunity.INTERNET,
                StandardCommunity.NO_EXPORT_SUBCONFED,
                StandardCommunity.NO_ADVERTISE,
                StandardCommunity.NO_EXPORT));
      }
      {
        CommunitySet set = c.getCommunitySets().get("cl_test");
        assertThat(
            set.getCommunities(),
            containsInAnyOrder(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2)));
      }
    }
  }

  @Test
  public void testIpCommunityListStandardExtraction() {
    String hostname = "arista_ip_community_list_standard";
    AristaConfiguration vc = parseVendorConfig(hostname);

    assertThat(vc.getStandardCommunityLists(), hasKeys("cl_values", "cl_test"));
    {
      StandardCommunityList cl = vc.getStandardCommunityLists().get("cl_values");
      assertThat(
          cl.getLines().stream()
              .map(StandardCommunityListLine::getCommunities)
              .map(Iterables::getOnlyElement)
              .collect(ImmutableList.toImmutableList()),
          contains(
              StandardCommunity.of(1, 1),
              StandardCommunity.INTERNET,
              StandardCommunity.NO_EXPORT_SUBCONFED,
              StandardCommunity.NO_ADVERTISE,
              StandardCommunity.NO_EXPORT));
    }
    {
      StandardCommunityList cl = vc.getStandardCommunityLists().get("cl_test");
      Iterator<StandardCommunityListLine> lines = cl.getLines().iterator();
      StandardCommunityListLine line;

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.DENY));
      assertThat(
          line.getCommunities(), contains(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2)));

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getCommunities(), contains(StandardCommunity.of(1, 1)));

      line = lines.next();
      assertThat(line.getAction(), equalTo(LineAction.PERMIT));
      assertThat(line.getCommunities(), contains(StandardCommunity.of(2, 2)));

      assertFalse(lines.hasNext());
    }
  }

  @Test
  public void testRouteMapCommunityExtraction() {
    String hostname = "arista_route_map_community";
    AristaConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getRouteMaps(),
        hasKeys(
            "match_community_standard",
            "match_community_expanded",
            "set_community",
            "set_community_additive",
            "set_community_delete",
            "set_community_none",
            "set_community_list_expanded",
            "set_community_list_standard",
            "set_community_list_standard_single",
            "set_community_list_additive_expanded",
            "set_community_list_additive_standard",
            "set_community_list_additive_standard_single",
            "set_community_list_delete_expanded",
            "set_community_list_delete_expanded_single",
            "set_community_list_delete_standard",
            "set_community_list_delete_standard_single"));
    {
      RouteMap rm = vc.getRouteMaps().get("match_community_standard");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause clause = getOnlyElement(rm.getClauses().values());
      assertThat(clause.getAction(), equalTo(LineAction.PERMIT));
      assertThat(clause.getSeqNum(), equalTo(10));
      RouteMapMatchCommunity match = clause.getMatchCommunity();
      assertThat(clause.getMatchList(), contains(match));
      assertThat(match.getListNames(), contains("community_list_standard"));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("match_community_expanded");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapMatchCommunity match = entry.getMatchCommunity();
      assertThat(entry.getMatchList(), contains(match));
      assertThat(match.getListNames(), contains("community_list_expanded"));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunity set = entry.getSetCommunity();
      assertThat(entry.getSetList(), contains(set));
      assertThat(
          set.getCommunities(), contains(StandardCommunity.of(1, 1), StandardCommunity.of(1, 2)));
      assertFalse(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_additive");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunity set = entry.getSetCommunity();
      assertThat(entry.getSetList(), contains(set));
      assertThat(
          set.getCommunities(), contains(StandardCommunity.of(1, 1), StandardCommunity.of(1, 2)));
      assertTrue(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_delete");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityDelete set = entry.getSetCommunityDelete();
      assertThat(entry.getSetList(), contains(set));
      assertThat(
          set.getCommunities(), contains(StandardCommunity.of(1, 1), StandardCommunity.of(1, 2)));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_none");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityNone set = entry.getSetCommunityNone();
      assertThat(entry.getSetList(), contains(set));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_expanded");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_expanded"));
      assertFalse(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_standard");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard"));
      assertFalse(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_standard_single");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard_single"));
      assertFalse(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_additive_expanded");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_expanded"));
      assertTrue(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_additive_standard");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard"));
      assertTrue(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_additive_standard_single");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityList set = entry.getSetCommunityList();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard_single"));
      assertTrue(set.getAdditive());
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_delete_expanded");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityListDelete set = entry.getSetCommunityListDelete();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_expanded"));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_delete_expanded_single");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityListDelete set = entry.getSetCommunityListDelete();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_expanded_single"));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_delete_standard");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityListDelete set = entry.getSetCommunityListDelete();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard"));
    }
    {
      RouteMap rm = vc.getRouteMaps().get("set_community_list_delete_standard_single");
      assertThat(rm.getClauses().keySet(), contains(10));
      RouteMapClause entry = getOnlyElement(rm.getClauses().values());
      assertThat(entry.getAction(), equalTo(LineAction.PERMIT));
      assertThat(entry.getSeqNum(), equalTo(10));
      RouteMapSetCommunityListDelete set = entry.getSetCommunityListDelete();
      assertThat(entry.getSetList(), contains(set));
      assertThat(set.getCommunityLists(), contains("community_list_standard_single"));
    }
  }

  @Test
  public void testRouteMapCommunityConversion() {
    String hostname = "arista_route_map_community";
    Configuration c = parseConfig(hostname);
    Set<String> topLevelPolicies =
        c.getRoutingPolicies().keySet().stream()
            .filter(name -> !name.contains("~"))
            .collect(ImmutableSet.toImmutableSet());
    assertThat(
        topLevelPolicies,
        containsInAnyOrder(
            "match_community_standard",
            "match_community_expanded",
            "set_community",
            "set_community_additive",
            "set_community_delete",
            "set_community_none",
            "set_community_list_expanded",
            "set_community_list_standard",
            "set_community_list_standard_single",
            "set_community_list_additive_expanded",
            "set_community_list_additive_standard",
            "set_community_list_additive_standard_single",
            "set_community_list_delete_expanded",
            "set_community_list_delete_expanded_single",
            "set_community_list_delete_standard",
            "set_community_list_delete_standard_single"));
    Ip origNextHopIp = Ip.parse("192.0.2.254");
    Bgpv4Route base =
        Bgpv4Route.testBuilder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(origNextHopIp)
            .setNetwork(Prefix.ZERO)
            .setTag(0L)
            .build();
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("match_community_standard");
      assertRoutingPolicyDeniesRoute(rp, base);
      Bgpv4Route routeOnlyOneCommunity =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(1, 1))).build();
      assertRoutingPolicyDeniesRoute(rp, routeOnlyOneCommunity);
      Bgpv4Route routeBothCommunities =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(StandardCommunity.of(1, 1), StandardCommunity.of(2, 2)))
              .build();
      assertTrue(rp.processReadOnly(routeBothCommunities));
      Bgpv4Route routeBothCommunitiesAndMore =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(2, 2),
                      StandardCommunity.of(3, 3)))
              .build();
      assertTrue(rp.processReadOnly(routeBothCommunitiesAndMore));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("match_community_expanded");
      assertRoutingPolicyDeniesRoute(rp, base);
      Bgpv4Route routeBothCommunities =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(StandardCommunity.of(1, 1), StandardCommunity.of(3, 4)))
              .build();
      assertTrue(rp.processReadOnly(routeBothCommunities));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(StandardCommunity.of(3, 3), ExtendedCommunity.target(1L, 1L)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // Standard communities should be replaced, while extended communities should be preserved.
      assertThat(
          route,
          hasCommunities(
              StandardCommunity.of(1, 1),
              StandardCommunity.of(1, 2),
              ExtendedCommunity.target(1L, 1L)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_additive");
      Bgpv4Route inRoute =
          base.toBuilder().setCommunities(ImmutableSet.of(StandardCommunity.of(3, 3))).build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(
              StandardCommunity.of(1, 1), StandardCommunity.of(1, 2), StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_delete");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      ExtendedCommunity.target(1L, 1L),
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(1, 2),
                      StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_none");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(1, 1)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_expanded");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_standard");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_standard_single");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(1, 1)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_additive_expanded");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_additive_standard");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_additive_standard_single");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(
              ExtendedCommunity.target(1L, 1L),
              StandardCommunity.of(1, 1),
              StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_delete_expanded");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      ExtendedCommunity.target(1L, 1L),
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(
              ExtendedCommunity.target(1L, 1L),
              StandardCommunity.of(1, 1),
              StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_delete_expanded_single");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      ExtendedCommunity.target(1L, 1L),
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_delete_standard");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(1, 1)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      // TODO: confirm vendor behavior
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(1, 1)));
    }
    {
      RoutingPolicy rp = c.getRoutingPolicies().get("set_community_list_delete_standard_single");
      Bgpv4Route inRoute =
          base.toBuilder()
              .setCommunities(
                  ImmutableSet.of(
                      ExtendedCommunity.target(1L, 1L),
                      StandardCommunity.of(1, 1),
                      StandardCommunity.of(3, 3)))
              .build();
      Bgpv4Route route = processRouteIn(rp, inRoute);
      assertThat(
          route.getCommunities().getCommunities(),
          containsInAnyOrder(ExtendedCommunity.target(1L, 1L), StandardCommunity.of(3, 3)));
    }
  }
}
