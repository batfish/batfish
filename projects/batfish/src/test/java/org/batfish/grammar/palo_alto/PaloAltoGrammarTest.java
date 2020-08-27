package org.batfish.grammar.palo_alto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.PALO_ALTO_NESTED;
import static org.batfish.datamodel.Interface.DependencyType.BIND;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.OriginType.EGP;
import static org.batfish.datamodel.OriginType.IGP;
import static org.batfish.datamodel.OriginType.INCOMPLETE;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasAdministrativeCost;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopInterface;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AclLineMatchers.hasTraceElement;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasMemberInterfaces;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasOutgoingOriginalFlowFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasZone;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDependencies;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasEncapsulationVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasZoneName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.NssaSettingsMatchers.hasDefaultOriginateType;
import static org.batfish.datamodel.matchers.OspfAreaMatchers.hasNssa;
import static org.batfish.datamodel.matchers.OspfAreaMatchers.hasStub;
import static org.batfish.datamodel.matchers.OspfAreaMatchers.hasStubType;
import static org.batfish.datamodel.matchers.OspfProcessMatchers.hasArea;
import static org.batfish.datamodel.matchers.StaticRouteMatchers.hasNextVrf;
import static org.batfish.datamodel.matchers.StubSettingsMatchers.hasSuppressType3;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isChainOfSingleChildren;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.datamodel.matchers.VrfMatchers.hasName;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.grammar.VendorConfigurationFormatDetector.BATFISH_FLATTENED_PALO_ALTO_HEADER;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.DEFAULT_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.NULL_VRF_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.PANORAMA_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeOutgoingFilterName;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeServiceGroupMemberAclName;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_GROUP_OR_APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.EXTERNAL_LIST;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SHARED_GATEWAY;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.TEMPLATE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.IMPORT_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.STATIC_ROUTE_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.TEMPLATE_STACK_TEMPLATES;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.emptyZoneRejectTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.ifaceOutgoingTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.intrazoneDefaultAcceptTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchSecurityRuleTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.originatedFromDeviceTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.unzonedIfaceRejectTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.zoneToZoneMatchTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.zoneToZoneRejectTraceElement;
import static org.batfish.representation.palo_alto.RuleEndpoint.Type.Any;
import static org.batfish.representation.palo_alto.RuleEndpoint.Type.IP_ADDRESS;
import static org.batfish.representation.palo_alto.RuleEndpoint.Type.IP_PREFIX;
import static org.batfish.representation.palo_alto.RuleEndpoint.Type.IP_RANGE;
import static org.batfish.representation.palo_alto.RuleEndpoint.Type.REFERENCE;
import static org.batfish.representation.palo_alto.Zone.Type.EXTERNAL;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.matchers.InterfaceMatchers;
import org.batfish.datamodel.matchers.NssaSettingsMatchers;
import org.batfish.datamodel.matchers.OspfAreaMatchers;
import org.batfish.datamodel.matchers.OspfProcessMatchers;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.representation.palo_alto.AddressGroup;
import org.batfish.representation.palo_alto.AddressObject;
import org.batfish.representation.palo_alto.AddressPrefix;
import org.batfish.representation.palo_alto.AdminDistances;
import org.batfish.representation.palo_alto.Application;
import org.batfish.representation.palo_alto.BgpConnectionOptions;
import org.batfish.representation.palo_alto.BgpPeer;
import org.batfish.representation.palo_alto.BgpPeer.ReflectorClient;
import org.batfish.representation.palo_alto.BgpPeerGroup;
import org.batfish.representation.palo_alto.BgpVr;
import org.batfish.representation.palo_alto.BgpVrRoutingOptions.AsFormat;
import org.batfish.representation.palo_alto.CryptoProfile;
import org.batfish.representation.palo_alto.CryptoProfile.Type;
import org.batfish.representation.palo_alto.DeviceGroup;
import org.batfish.representation.palo_alto.EbgpPeerGroupType;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ExportNexthopMode;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ImportNexthopMode;
import org.batfish.representation.palo_alto.IbgpPeerGroupType;
import org.batfish.representation.palo_alto.Interface;
import org.batfish.representation.palo_alto.InterfaceAddress;
import org.batfish.representation.palo_alto.NatRule;
import org.batfish.representation.palo_alto.OspfArea;
import org.batfish.representation.palo_alto.OspfAreaNormal;
import org.batfish.representation.palo_alto.OspfAreaNssa;
import org.batfish.representation.palo_alto.OspfAreaNssa.DefaultRouteType;
import org.batfish.representation.palo_alto.OspfAreaStub;
import org.batfish.representation.palo_alto.OspfInterface;
import org.batfish.representation.palo_alto.OspfInterface.LinkType;
import org.batfish.representation.palo_alto.OspfVr;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.representation.palo_alto.PaloAltoStructureType;
import org.batfish.representation.palo_alto.PaloAltoStructureUsage;
import org.batfish.representation.palo_alto.PolicyRule;
import org.batfish.representation.palo_alto.PolicyRule.Action;
import org.batfish.representation.palo_alto.PolicyRuleUpdateOrigin;
import org.batfish.representation.palo_alto.RedistProfile;
import org.batfish.representation.palo_alto.RedistProfileFilter;
import org.batfish.representation.palo_alto.RedistRule;
import org.batfish.representation.palo_alto.RedistRule.AddressFamilyIdentifier;
import org.batfish.representation.palo_alto.RedistRule.RouteTableType;
import org.batfish.representation.palo_alto.RedistRuleRefNameOrPrefix;
import org.batfish.representation.palo_alto.RuleEndpoint;
import org.batfish.representation.palo_alto.SecurityRule;
import org.batfish.representation.palo_alto.ServiceBuiltIn;
import org.batfish.representation.palo_alto.StaticRoute;
import org.batfish.representation.palo_alto.Tag;
import org.batfish.representation.palo_alto.Template;
import org.batfish.representation.palo_alto.TemplateStack;
import org.batfish.representation.palo_alto.VirtualRouter;
import org.batfish.representation.palo_alto.Vsys;
import org.batfish.representation.palo_alto.Zone;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class PaloAltoGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";
  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/palo_alto/snapshots/";
  private static final BDDPacket PKT = new BDDPacket();
  private static final IpSpaceToBDD DST = PKT.getDstIpSpaceToBDD();

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      assertThat(configs, hasKey(hostname));
      return configs.get(hostname);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private PaloAltoConfiguration parsePaloAltoConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(src, settings, null);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(src, parser, new Warnings());
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
    pac.setVendor(ConfigurationFormat.PALO_ALTO);
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    pac.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    pac = SerializationUtils.clone(pac);
    pac.setAnswerElement(answerElement);
    pac.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    pac.setWarnings(new Warnings());
    return pac;
  }

  private @Nonnull PaloAltoConfiguration parseNestedConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    Warnings w = new Warnings();
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false);
    Flattener flattener =
        Batfish.flatten(
            src, logger, settings, w, PALO_ALTO_NESTED, BATFISH_FLATTENED_PALO_ALTO_HEADER);
    String fileText = flattener.getFlattenedConfigurationText();
    FlattenerLineMap lineMap = flattener.getOriginalLineMap();
    PaloAltoCombinedParser paParser = new PaloAltoCombinedParser(fileText, settings, lineMap);
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(fileText, paParser, w);
    ParserRuleContext tree = Batfish.parse(paParser, logger, settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
    pac.setVendor(ConfigurationFormat.PALO_ALTO);
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    pac.setFilename(TESTCONFIGS_PREFIX + hostname);
    pac.setAnswerElement(answerElement);
    return pac;
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private static Flow createFlow(IpProtocol protocol, int sourcePort, int destinationPort) {
    Flow.Builder fb = Flow.builder();
    fb.setIngressNode("node");
    fb.setIpProtocol(protocol);
    fb.setDstPort(destinationPort);
    fb.setSrcPort(sourcePort);
    return fb.build();
  }

  private static Flow createFlow(String sourceAddress, String destinationAddress) {
    return createFlow(sourceAddress, destinationAddress, IpProtocol.TCP, 1, 1);
  }

  private static Flow createFlow(
      String sourceAddress,
      String destinationAddress,
      IpProtocol protocol,
      int sourcePort,
      int destinationPort) {
    Flow.Builder fb = Flow.builder();
    fb.setIngressNode("node");
    fb.setSrcIp(Ip.parse(sourceAddress));
    fb.setDstIp(Ip.parse(destinationAddress));
    fb.setIpProtocol(protocol);
    fb.setDstPort(destinationPort);
    fb.setSrcPort(sourcePort);
    return fb.build();
  }

  private static void assertIpSpacesEqual(IpSpace left, IpSpace right) {
    assertThat(DST.visit(left), equalTo(DST.visit(right)));
  }

  @Test
  public void testTags() {
    PaloAltoConfiguration c = parsePaloAltoConfig("tags");

    String tag1 = "vsys_tag";
    String tag2 = "vsys_tag with spaces";
    String sharedTag = "shared_tag";

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, Tag> tags = vsys.getTags();

    Vsys sharedVsys = c.getShared();
    Map<String, Tag> sharedTags = sharedVsys.getTags();

    // Confirm vsys-specific tags are extracted correctly
    assertThat(tags.keySet(), contains(tag1, tag2));
    assertThat(tags.get(tag1).getComments(), equalTo("something"));
    assertThat(tags.get(tag2).getComments(), equalTo("comments with spaces"));

    // Confirm shared tags are extracted correctly
    assertThat(sharedTags.keySet(), contains(sharedTag));
    assertThat(sharedTags.get(sharedTag).getComments(), nullValue());
  }

  @Test
  public void testAddressGroups() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-groups");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, AddressGroup> addressGroups = vsys.getAddressGroups();
    Map<String, AddressObject> addressObjects = vsys.getAddressObjects();

    // there are four address groups defined in the file, including empty ones
    assertThat(
        addressGroups.keySet(),
        equalTo(ImmutableSet.of("group0", "group1", "group2", "group w spaces")));

    assertThat(
        addressGroups.get("group w spaces").getMembers(),
        equalTo(ImmutableSet.of("addr w spaces")));

    // the ip space of the empty group is empty
    assertThat(addressGroups.get("group0").getMembers(), equalTo(ImmutableSet.of()));
    assertThat(
        addressGroups.get("group0").getIpSpace(addressObjects, addressGroups),
        equalTo(EmptyIpSpace.INSTANCE));

    // we parsed the description and tag, addr3 is undefined but should not have been discarded
    assertThat(addressGroups.get("group1").getDescription(), equalTo("group1-desc"));
    assertThat(addressGroups.get("group1").getTags(), contains("tag1"));
    assertThat(
        addressGroups.get("group1").getMembers(),
        equalTo(ImmutableSet.of("addr1", "addr2", "addr3")));
    assertThat(
        addressGroups.get("group1").getIpSpace(addressObjects, addressGroups),
        equalTo(
            AclIpSpace.union(
                addressObjects.get("addr2").getIpSpace(),
                addressObjects.get("addr1").getIpSpace())));

    // check that we parse multiple address objects and tags on the same line correctly
    assertThat(
        addressGroups.get("group2").getMembers(), equalTo(ImmutableSet.of("addr1", "addr2")));
    assertThat(
        addressGroups.get("group2").getTags(), containsInAnyOrder("tag2", "tag with spaces"));

    // check that ip spaces were inserted properly
    Configuration viConfig = c.toVendorIndependentConfigurations().get(0);
    assertThat(
        viConfig,
        hasIpSpace(
            "group0",
            equalTo(addressGroups.get("group0").getIpSpace(addressObjects, addressGroups))));
    assertThat(
        viConfig,
        hasIpSpace(
            "group1",
            equalTo(addressGroups.get("group1").getIpSpace(addressObjects, addressGroups))));
  }

  // TODO: https://github.com/batfish/batfish/issues/4921
  @Ignore
  @Test
  public void testAddressGroupHybrid() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-group-hybrid");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, AddressGroup> addressGroups = vsys.getAddressGroups();
    Map<String, AddressObject> addressObjects = vsys.getAddressObjects();

    Vsys sharedVsys = c.getShared();
    Map<String, AddressObject> sharedAddressObjects = sharedVsys.getAddressObjects();

    assertThat(addressGroups.keySet(), equalTo(ImmutableSet.of("group")));

    // Confirm hybrid address-group contains both shared and vsys-specific addresses
    assertThat(
        addressGroups.get("group").getIpSpace(addressObjects, addressGroups),
        equalTo(
            AclIpSpace.union(
                addressObjects.get("addr").getIpSpace(),
                sharedAddressObjects.get("shared_addr").getIpSpace())));
  }

  @Test
  public void testAddressGroupDynamic() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-group-dynamic");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, AddressObject> addressObjects = vsys.getAddressObjects();
    Map<String, AddressGroup> addressGroups = vsys.getAddressGroups();

    // Confirm filter was attached to the dynamic address-group
    assertThat(addressGroups.keySet(), contains("group"));
    AddressGroup ag = addressGroups.get("group");
    assertThat(ag.getType(), equalTo(AddressGroup.Type.DYNAMIC));
    assertThat(ag.getFilter(), equalTo("'tagA' and tag1"));

    // Confirm the filter resolves to the correct IpSpace, containing only the one matching address
    assertThat(
        ag.getIpSpace(addressObjects, addressGroups),
        equalTo(addressObjects.get("addr1").getIpSpace()));
  }

  @Test
  public void testAddressGroupsNested() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-groups-nested");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, AddressGroup> addressGroups = vsys.getAddressGroups();

    assertThat(addressGroups.get("group1").getMembers(), equalTo(ImmutableSet.of("group0")));
    assertThat(addressGroups.get("group0").getMembers(), equalTo(ImmutableSet.of("group2")));
    assertThat(addressGroups.get("group2").getMembers(), equalTo(ImmutableSet.of("group0")));
  }

  @Test
  public void testAddressGroupsReference() throws IOException {
    String hostname = "address-groups-nested";
    String filename = "configs/" + hostname;

    String group0 = computeObjectName(DEFAULT_VSYS_NAME, "group0");
    String group1 = computeObjectName(DEFAULT_VSYS_NAME, "group1");

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.ADDRESS_GROUP, group0, 2));
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.ADDRESS_GROUP, group1, 0));
  }

  @Test
  public void testAddressObjectGroupInheritance() {
    String hostname = "address-object-group-inheritance";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";

    // rule1: literal 11.11.11.11 should not be allowed but 10.10.10.10 should be
    Flow rule1Permitted = createFlow("10.10.10.10", "33.33.33.33");
    Flow rule1Denied = createFlow("11.11.11.11", "33.33.33.33");

    assertThat(
        c,
        hasInterface(if1name, hasOutgoingOriginalFlowFilter(accepts(rule1Permitted, if2name, c))));
    assertThat(
        c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(rule1Denied, if2name, c))));

    // rule2: literal 22.22.22.22 should not be allowed but 10.10.10.10 should be
    Flow rule2Permitted = createFlow("44.44.44.44", "10.10.10.10");
    Flow rule2Denied = createFlow("44.44.44.44", "22.22.22.22");

    assertThat(
        c,
        hasInterface(if1name, hasOutgoingOriginalFlowFilter(accepts(rule2Permitted, if2name, c))));
    assertThat(
        c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(rule2Denied, if2name, c))));
  }

  @Test
  public void testAddressObjectGroupNameConflict() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-object-group-name-conflict");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);

    // the address object definition should win for addr1
    assertThat(vsys.getAddressObjects().keySet(), equalTo(ImmutableSet.of("addr1")));

    // the address group definition should win for group1
    assertThat(vsys.getAddressGroups().keySet(), equalTo(ImmutableSet.of("group1")));
  }

  @Test
  public void testAddressObjectGroupReferenceInRule() throws IOException {
    String hostname = "address-object-group-inheritance";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String objectName = computeObjectName(DEFAULT_VSYS_NAME, "11.11.11.11");
    String groupName = computeObjectName(DEFAULT_VSYS_NAME, "22.22.22.22");

    assertThat(
        ccae, hasNumReferrers(filename, PaloAltoStructureType.ADDRESS_OBJECT, objectName, 2));
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.ADDRESS_GROUP, groupName, 1));
  }

  @Test
  public void testAddressObjects() {
    PaloAltoConfiguration c = parsePaloAltoConfig("address-objects");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, AddressObject> addressObjects = vsys.getAddressObjects();

    // check that we parse normal object names, names with spaces, and that look like IP addresses
    assertThat(
        vsys.getAddressObjects().keySet(),
        equalTo(
            ImmutableSet.of(
                "4.3.2.1", "addr0", "addr1", "addr2", "addr3", "addr4", "addr w spaces")));

    // check that we parse the name-only object right
    assertThat(addressObjects.get("addr0").getIpSpace(), equalTo(EmptyIpSpace.INSTANCE));

    // check that we parsed description, tag, and prefix right
    // note that PA allows non-canonical prefixes
    assertThat(
        addressObjects.get("addr1").getIpSpace(),
        equalTo(Prefix.strict("10.1.1.0/24").toIpSpace()));
    assertThat(addressObjects.get("addr1").getDescription(), equalTo("addr1-desc"));
    assertThat(addressObjects.get("addr1").getTags(), contains("tag1"));

    // check that we parse the IP address and tags right
    assertThat(addressObjects.get("addr2").getIpSpace(), equalTo(Ip.parse("10.1.1.2").toIpSpace()));
    assertThat(
        addressObjects.get("addr2").getTags(), containsInAnyOrder("tag2", "tag with spaces"));

    // check that we parse the IP range right
    assertThat(
        addressObjects.get("addr3").getIpSpace(),
        equalTo(IpRange.range(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.2"))));

    // check that ip spaces were inserted properly
    Configuration viConfig = c.toVendorIndependentConfigurations().get(0);
    assertThat(viConfig, hasIpSpace("addr0", equalTo(addressObjects.get("addr0").getIpSpace())));
    assertThat(viConfig, hasIpSpace("addr1", equalTo(addressObjects.get("addr1").getIpSpace())));
    assertThat(viConfig, hasIpSpace("addr2", equalTo(addressObjects.get("addr2").getIpSpace())));
    assertThat(viConfig, hasIpSpace("addr3", equalTo(addressObjects.get("addr3").getIpSpace())));
  }

  @Test
  public void testAddressObjectReference() throws IOException {
    String hostname1 = "address-objects";
    String filename1 = "configs/" + hostname1;
    String hostname2 = "address-groups";
    String filename2 = "configs/" + hostname2;
    String hostname3 = "interface";
    String filename3 = "configs/" + hostname3;

    String name_1 = computeObjectName(DEFAULT_VSYS_NAME, "addr1");
    String name_3 = computeObjectName(DEFAULT_VSYS_NAME, "addr3");
    String name_4 = computeObjectName(DEFAULT_VSYS_NAME, "addr4");
    String name_5 = computeObjectName(DEFAULT_VSYS_NAME, "addr w spaces");
    String name_ambiguous = computeObjectName(DEFAULT_VSYS_NAME, "4.3.2.1");

    // Confirm reference count is correct for used structures
    Batfish batfish1 = getBatfishForConfigurationNames(hostname1);
    ConvertConfigurationAnswerElement ccae1 =
        batfish1.loadConvertConfigurationAnswerElementOrReparse(batfish1.getSnapshot());
    // No reference to unused structure
    assertThat(ccae1, hasNumReferrers(filename1, PaloAltoStructureType.ADDRESS_OBJECT, name_1, 0));
    // Should identify what looks like a valid object reference
    // Even though it cannot be converted as an interface address, it's a range
    assertThat(ccae1, hasNumReferrers(filename1, PaloAltoStructureType.ADDRESS_OBJECT, name_3, 1));
    // One reference from l3 interface, one from loopback
    assertThat(ccae1, hasNumReferrers(filename1, PaloAltoStructureType.ADDRESS_OBJECT, name_4, 2));
    // Should record reference for address object name that looks like a concrete IP address
    assertThat(
        ccae1, hasNumReferrers(filename1, PaloAltoStructureType.ADDRESS_OBJECT, name_ambiguous, 1));
    // Confirm undefined reference is detected
    assertThat(
        ccae1,
        hasUndefinedReference(filename1, PaloAltoStructureType.ADDRESS_OBJECT, "addr_undef"));

    Batfish batfish2 = getBatfishForConfigurationNames(hostname2);
    ConvertConfigurationAnswerElement ccae2 =
        batfish2.loadConvertConfigurationAnswerElementOrReparse(batfish1.getSnapshot());
    // Confirm reference count is correct for used structure
    assertThat(ccae2, hasNumReferrers(filename2, PaloAltoStructureType.ADDRESS_OBJECT, name_1, 2));
    assertThat(ccae2, hasNumReferrers(filename2, PaloAltoStructureType.ADDRESS_OBJECT, name_5, 1));
    // Confirm undefined reference is detected
    assertThat(
        ccae2, hasUndefinedReference(filename2, PaloAltoStructureType.ADDRESS_LIKE, "addr3"));

    Batfish batfish3 = getBatfishForConfigurationNames(hostname3);
    ConvertConfigurationAnswerElement ccae3 =
        batfish3.loadConvertConfigurationAnswerElementOrReparse(batfish3.getSnapshot());
    // Referring to the wrong type of object should result in an undefined reference
    assertThat(
        ccae3, hasUndefinedReference(filename3, PaloAltoStructureType.ADDRESS_OBJECT, "GRP1"));
  }

  @Test
  public void testApplications() {
    PaloAltoConfiguration c = parsePaloAltoConfig("applications");

    Vsys vsys = c.getVirtualSystems().get(DEFAULT_VSYS_NAME);
    Map<String, Application> applications = vsys.getApplications();

    // Should have three applications, including an empty one
    assertThat(applications.keySet(), equalTo(ImmutableSet.of("app1", "app2", "app w spaces")));

    // Check that descriptions are extracted
    assertThat(applications.get("app1").getDescription(), nullValue());
    assertThat(applications.get("app2").getDescription(), equalTo("this is a description"));
    assertThat(
        applications.get("app w spaces").getDescription(), equalTo("this is another description"));
  }

  @Test
  public void testApplicationsReference() throws IOException {
    String hostname = "applications";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String app1Name = computeObjectName(DEFAULT_VSYS_NAME, "app1");
    String app2Name = computeObjectName(DEFAULT_VSYS_NAME, "app2");

    // Confirm reference count is correct for defined structures
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.APPLICATION, app1Name, 0));
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.APPLICATION, app2Name, 1));

    // Confirm undefined application is detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, APPLICATION_GROUP_OR_APPLICATION, "undefined", SECURITY_RULE_APPLICATION));
  }

  @Test
  public void testBgpPolicesExtraction() {
    PaloAltoConfiguration c = parsePaloAltoConfig("bgp-policies");
    VirtualRouter vr = c.getVirtualRouters().get("vr1");
    assertThat(vr, notNullValue());
    BgpVr bgp = vr.getBgp();
    assertThat(bgp, notNullValue());

    assertThat(bgp.getExportPolicyRules(), hasKey("export1"));
    PolicyRule export1 = bgp.getExportPolicyRules().get("export1");
    assertThat(export1.getAction(), equalTo(Action.ALLOW));
    assertThat(export1.getUsedBy(), contains("pg1"));
    assertThat(export1.getUpdateOrigin(), equalTo(new PolicyRuleUpdateOrigin(IGP)));
    assertThat(export1.getMatchAddressPrefixSet(), not(nullValue()));
    assertThat(
        export1.getMatchAddressPrefixSet().getAddressPrefixes(),
        equalTo(ImmutableSet.of(new AddressPrefix(Prefix.ZERO, true))));
    assertThat(export1.getMatchFromPeerSet(), nullValue());

    assertThat(bgp.getImportPolicyRules(), hasKey("import1"));
    PolicyRule import1 = bgp.getImportPolicyRules().get("import1");
    assertThat(import1.getAction(), equalTo(Action.ALLOW));
    assertThat(import1.getUsedBy(), containsInAnyOrder("pg2", "pg4"));
    assertThat(import1.getUpdateOrigin(), equalTo(new PolicyRuleUpdateOrigin(OriginType.EGP)));
    assertThat(import1.getMatchAddressPrefixSet(), not(nullValue()));
    assertThat(
        import1.getMatchAddressPrefixSet().getAddressPrefixes(),
        equalTo(
            ImmutableSet.of(
                new AddressPrefix(Prefix.parse("12.12.1.1/32"), true),
                new AddressPrefix(Prefix.parse("1.21.3.0/24"), false),
                new AddressPrefix(Prefix.parse("1.1.1.1/32"), true))));
    assertThat(import1.getMatchFromPeerSet(), not(nullValue()));
    assertThat(
        import1.getMatchFromPeerSet().getFromPeers(),
        equalTo(ImmutableSet.of("evil_peer", "good_peer")));

    assertThat(bgp.getImportPolicyRules(), hasKey("import2"));
    PolicyRule import2 = bgp.getImportPolicyRules().get("import2");
    assertThat(import2.getAction(), equalTo(Action.DENY));
    assertThat(import2.getUsedBy(), contains("pg3"));
    assertThat(import2.getEnable(), equalTo(true));
    assertThat(import2.getMatchAddressPrefixSet(), not(nullValue()));
    assertThat(
        import2.getMatchAddressPrefixSet().getAddressPrefixes(),
        equalTo(ImmutableSet.of(new AddressPrefix(Prefix.parse("1.2.3.0/24"), false))));
  }

  @Test
  public void testBgpRedistProfilesAndRulesExtraction() {
    PaloAltoConfiguration c = parsePaloAltoConfig("bgp-redist-profile");

    VirtualRouter vr = c.getVirtualRouters().get("vr1");
    assertThat(vr, notNullValue());

    RedistProfile redistProfile = vr.getRedistProfiles().get("rdp1");
    assertThat(redistProfile, not(nullValue()));
    assertThat(redistProfile.getAction(), equalTo(RedistProfile.Action.NO_REDIST));
    assertThat(redistProfile.getPriority(), equalTo(2));
    RedistProfileFilter redistProfileFilter = redistProfile.getFilter();
    assertThat(redistProfileFilter, not(nullValue()));
    assertThat(
        redistProfileFilter.getDestinationPrefixes(),
        equalTo(ImmutableSet.of(Prefix.parse("1.1.1.0/24"), Prefix.parse("2.2.2.0/24"))));
    assertThat(
        redistProfileFilter.getRoutingProtocols(),
        equalTo(
            ImmutableSet.of(
                RoutingProtocol.STATIC,
                RoutingProtocol.CONNECTED,
                RoutingProtocol.BGP,
                RoutingProtocol.OSPF,
                RoutingProtocol.RIP)));

    BgpVr bgp = vr.getBgp();
    assertThat(bgp, not(nullValue()));

    RedistRule namedRedistRule = bgp.getRedistRules().get(new RedistRuleRefNameOrPrefix("rdp1"));
    assertThat(namedRedistRule, not(nullValue()));
    assertThat(namedRedistRule.getEnable(), equalTo(Boolean.TRUE));
    assertThat(namedRedistRule.getOrigin(), equalTo(OriginType.INCOMPLETE));

    RedistRule prefixRedistRule =
        bgp.getRedistRules().get(new RedistRuleRefNameOrPrefix(Prefix.parse("1.1.1.0/24")));
    assertThat(prefixRedistRule, not(nullValue()));
    assertThat(prefixRedistRule.getEnable(), equalTo(Boolean.TRUE));
    assertThat(prefixRedistRule.getOrigin(), equalTo(OriginType.EGP));
    assertThat(
        prefixRedistRule.getAddressFamilyIdentifier(), equalTo(AddressFamilyIdentifier.IPV4));
    assertThat(prefixRedistRule.getRouteTableType(), equalTo(RouteTableType.UNICAST));

    RedistRule ipAddressRedistRule =
        bgp.getRedistRules().get(new RedistRuleRefNameOrPrefix(Prefix.parse("2.2.2.2/32")));
    assertThat(ipAddressRedistRule, not(nullValue()));
    assertThat(ipAddressRedistRule.getEnable(), equalTo(Boolean.FALSE));
    assertThat(ipAddressRedistRule.getOrigin(), equalTo(IGP));
    assertThat(
        ipAddressRedistRule.getAddressFamilyIdentifier(), equalTo(AddressFamilyIdentifier.IPV6));
    assertThat(ipAddressRedistRule.getRouteTableType(), equalTo(RouteTableType.MULTICAST));
  }

  @Test
  public void testBgpExtraction() {
    PaloAltoConfiguration c = parseNestedConfig("bgp");
    VirtualRouter vr = c.getVirtualRouters().get("BGP");
    assertThat(vr, notNullValue());
    BgpVr bgp = vr.getBgp();
    assertThat(bgp, notNullValue());
    assertThat(bgp.getEnable(), equalTo(Boolean.TRUE));
    assertThat(bgp.getInstallRoute(), equalTo(Boolean.TRUE));
    assertThat(bgp.getLocalAs(), equalTo(65001L));
    assertThat(bgp.getRejectDefaultRoute(), equalTo(Boolean.FALSE));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(bgp.getRoutingOptions().getAggregateMed(), equalTo(Boolean.FALSE));
    assertThat(bgp.getRoutingOptions().getAlwaysCompareMed(), equalTo(Boolean.TRUE));
    assertThat(bgp.getRoutingOptions().getAsFormat(), equalTo(AsFormat.FOUR_BYTE_AS));
    assertThat(bgp.getRoutingOptions().getDefaultLocalPreference(), equalTo(103L));
    assertThat(bgp.getRoutingOptions().getDeterministicMedComparison(), equalTo(Boolean.FALSE));
    assertThat(bgp.getRoutingOptions().getGracefulRestartEnable(), equalTo(Boolean.FALSE));
    assertThat(bgp.getRoutingOptions().getReflectorClusterId(), equalTo(Ip.parse("1.2.3.5")));

    assertThat(bgp.getPeerGroups().keySet(), containsInAnyOrder("PG", "EBGP"));
    BgpPeerGroup pg = bgp.getPeerGroups().get("PG");
    assertThat(pg.getEnable(), equalTo(true));
    assertThat(pg.getTypeAndOptions(), instanceOf(IbgpPeerGroupType.class));

    BgpPeerGroup ebgp = bgp.getPeerGroups().get("EBGP");
    assertThat(ebgp.getEnable(), equalTo(true));
    assertThat(ebgp.getTypeAndOptions(), instanceOf(EbgpPeerGroupType.class));
    EbgpPeerGroupType ebgpOptions = (EbgpPeerGroupType) ebgp.getTypeAndOptions();
    assertThat(ebgpOptions.getExportNexthop(), equalTo(ExportNexthopMode.USE_SELF));
    assertThat(ebgpOptions.getImportNexthop(), equalTo(ImportNexthopMode.USE_PEER));
    assertThat(ebgpOptions.getRemovePrivateAs(), equalTo(false));

    assertThat(pg.getPeers().keySet(), contains("PEER"));
    BgpPeer peer = pg.getOrCreatePeerGroup("PEER");
    assertThat(peer.getEnable(), equalTo(true));
    assertThat(peer.getLocalInterface(), equalTo("ethernet1/1"));
    assertThat(peer.getLocalAddress(), equalTo(Ip.parse("1.2.3.6")));
    assertThat(peer.getPeerAddress(), equalTo(Ip.parse("5.4.3.2")));
    assertThat(peer.getPeerAs(), equalTo(54321L));
    assertThat(peer.getReflectorClient(), equalTo(ReflectorClient.NON_CLIENT));

    BgpConnectionOptions co = peer.getConnectionOptions();
    assertTrue(co.getIncomingAllow());
    assertThat(co.getRemotePort(), equalTo(0));
    assertFalse(co.getOutgoingAllow());
    assertThat(co.getLocalPort(), equalTo(4321));
  }

  @Test
  public void testBgpConversion() {
    Configuration c = parseConfig("bgp");
    assertThat(c, hasVrf("BGP", hasBgpProcess(any(BgpProcess.class))));
    BgpProcess proc = c.getVrfs().get("BGP").getBgpProcess();
    assertThat(proc, hasRouterId(Ip.parse("1.2.3.4")));
    assertThat(proc.getActiveNeighbors().keySet(), contains(Prefix.parse("5.4.3.2/32")));
    BgpActivePeerConfig peer = proc.getActiveNeighbors().get(Prefix.parse("5.4.3.2/32"));
    assertThat(peer.getDescription(), equalTo("PEER"));
    assertThat(peer.getGroup(), equalTo("PG"));
    assertThat(peer.getLocalIp(), equalTo(Ip.parse("1.2.3.6")));
    assertThat(peer.getLocalAs(), equalTo(65001L));
    assertThat(peer.getRemoteAsns(), equalTo(LongSpace.of(65001)));
    // BgpRoutingProcess requires an export policy be present
    String exportPolicyName = peer.getIpv4UnicastAddressFamily().getExportPolicy();
    assertThat(exportPolicyName, not(nullValue()));
    assertThat(c.getRoutingPolicies().get(exportPolicyName), not(nullValue()));
  }

  @Test
  public void testBgpExportImportPolicyConversion() {
    Configuration c = parseConfig("bgp-export-import-policies");

    // test process level common export policy
    String bgpCommonExportPolicyName = "~BGP_COMMON_EXPORT_POLICY:vr1~";
    RoutingPolicy bgpCommonExportPolicy = c.getRoutingPolicies().get(bgpCommonExportPolicyName);

    org.batfish.datamodel.StaticRoute.Builder srb =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(5)
            .setMetric(10L);

    Bgpv4Route.Builder bgpBuilder = Bgpv4Route.builder();
    boolean accepted = bgpCommonExportPolicy.process(srb.build(), bgpBuilder, Direction.OUT);
    assertTrue(accepted);
    assertThat(bgpBuilder.getOriginType(), equalTo(IGP));

    // rejected because of mismatching prefix
    assertFalse(
        bgpCommonExportPolicy.process(
            srb.setNetwork(Prefix.parse("4.4.4.0/24")).build(),
            Bgpv4Route.builder(),
            Direction.OUT));

    // rejected because of mismatching protocol
    assertFalse(
        bgpCommonExportPolicy.process(
            ConnectedRoute.builder()
                .setNetwork(Prefix.parse("1.1.1.0/24"))
                .setNextHopInterface("iface1")
                .build(),
            Bgpv4Route.builder(),
            Direction.OUT));

    // the common BGP export policy should export any BGP route
    assertTrue(
        bgpCommonExportPolicy.process(
            Bgpv4Route.builder()
                .setAsPath(AsPath.ofSingletonAsSets(2L))
                .setOriginatorIp(Ip.ZERO)
                .setOriginType(OriginType.INCOMPLETE)
                .setProtocol(RoutingProtocol.BGP)
                .setNextHopIp(Ip.parse("23.23.23.32"))
                .setNetwork(Prefix.ZERO)
                .setTag(0L)
                .build(),
            Bgpv4Route.builder(),
            Direction.OUT));

    // test the routing policy generated by for all export policy rules used by this peer
    String bgpExportPolicyRuleRoutingPolicyName = "~BGP_POLICY_RULE_EXPORT_POLICY:vr1:peer1~";
    RoutingPolicy routingPolicyForExportPolicy =
        c.getRoutingPolicies().get(bgpExportPolicyRuleRoutingPolicyName);

    srb =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(5)
            .setMetric(10L);
    bgpBuilder = Bgpv4Route.builder();
    accepted = routingPolicyForExportPolicy.process(srb.build(), bgpBuilder, Direction.OUT);
    assertTrue(accepted);
    assertThat(bgpBuilder.getMetric(), equalTo(2323L));
    assertThat(bgpBuilder.getOriginType(), equalTo(EGP));

    // only 1.1.1.0/24 allowed by the export policy rule
    assertFalse(
        routingPolicyForExportPolicy.process(
            srb.setNetwork(Prefix.parse("2.2.2.0/24")).build(),
            Bgpv4Route.builder(),
            Direction.OUT));

    // testing the combined routing policy generated for the peer (conjunction of above two
    // policies)
    String bgpExportPoliciesPeer1 = "~BGP_PEER_EXPORT_POLICY:vr1:peer1~";
    RoutingPolicy routingPolicyForPeer1 = c.getRoutingPolicies().get(bgpExportPoliciesPeer1);

    srb =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAdmin(5)
            .setMetric(10L);
    bgpBuilder = Bgpv4Route.builder();
    accepted = routingPolicyForPeer1.process(srb.build(), bgpBuilder, Direction.OUT);
    assertTrue(accepted);
    assertThat(bgpBuilder.getMetric(), equalTo(2323L));
    // Peer export policy rule overrides the BGP common export policy rule
    assertThat(bgpBuilder.getOriginType(), equalTo(EGP));

    // only 1.1.1.0/24 allowed by the export policy rule
    assertFalse(
        routingPolicyForPeer1.process(
            srb.setNetwork(Prefix.parse("2.2.2.0/24")).build(),
            Bgpv4Route.builder(),
            Direction.OUT));

    // testing import policy generated for peer 1
    String importPolicyPeer1Name = "~BGP_PEER_IMPORT_POLICY:vr1:peer1~";
    RoutingPolicy importRoutingPolicyPeer1 = c.getRoutingPolicies().get(importPolicyPeer1Name);

    srb =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(Prefix.parse("3.2.1.0/24"))
            .setAdmin(5)
            .setMetric(10L);

    bgpBuilder = Bgpv4Route.builder();
    accepted = importRoutingPolicyPeer1.process(srb.build(), bgpBuilder, Direction.IN);
    assertTrue(accepted);
    assertThat(bgpBuilder.getOriginType(), equalTo(INCOMPLETE));

    // rejected because of mismatching prefix
    assertFalse(
        importRoutingPolicyPeer1.process(
            srb.setNetwork(Prefix.parse("1.1.1.0/24")).build(),
            Bgpv4Route.builder(),
            Direction.IN));
  }

  @Test
  public void testOspfExtraction() {
    PaloAltoConfiguration c = parsePaloAltoConfig("ospf");
    VirtualRouter vr = c.getVirtualRouters().get("vr1");
    assertThat(vr, notNullValue());
    OspfVr ospf = vr.getOspf();

    assertThat(ospf, notNullValue());
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("0.0.0.0")));
    assertTrue(ospf.isEnable());
    assertFalse(ospf.isRejectDefaultRoute());

    Ip areaId = Ip.parse("0.0.0.1");
    assertThat(ospf.getAreas(), hasKey(areaId));
    OspfArea ospfArea = ospf.getAreas().get(areaId);
    assertThat(ospfArea.getAreaId(), equalTo(areaId));
    assertThat(ospfArea.getTypeSettings(), not(nullValue()));
    assertThat(ospfArea.getTypeSettings(), instanceOf(OspfAreaStub.class));
    OspfAreaStub stubArea = (OspfAreaStub) ospfArea.getTypeSettings();
    assertThat(stubArea.getDefaultRouteMetric(), equalTo(12));
    assertThat(stubArea.getAcceptSummary(), equalTo(Boolean.TRUE));
    assertTrue(stubArea.isDefaultRouteDisable());

    areaId = Ip.parse("0.0.0.2");
    assertThat(ospf.getAreas(), hasKey(areaId));
    ospfArea = ospf.getAreas().get(areaId);
    assertThat(ospfArea.getAreaId(), equalTo(areaId));
    assertThat(ospfArea.getTypeSettings(), not(nullValue()));
    assertThat(ospfArea.getTypeSettings(), instanceOf(OspfAreaNormal.class));

    areaId = Ip.parse("0.0.0.3");
    assertThat(ospf.getAreas(), hasKey(areaId));
    ospfArea = ospf.getAreas().get(areaId);
    assertThat(ospfArea.getAreaId(), equalTo(areaId));
    assertThat(ospfArea.getTypeSettings(), not(nullValue()));
    assertThat(ospfArea.getTypeSettings(), instanceOf(OspfAreaNssa.class));
    OspfAreaNssa nssaArea = (OspfAreaNssa) ospfArea.getTypeSettings();
    assertThat(nssaArea.getDefaultRouteMetric(), equalTo(13));
    assertThat(nssaArea.getAcceptSummary(), equalTo(Boolean.FALSE));
    assertThat(nssaArea.getDefaultRouteType(), equalTo(DefaultRouteType.EXT_2));
    assertFalse(nssaArea.isDefaultRouteDisable());

    String ifaceName = "ethernet1/3.5";
    assertThat(ospfArea.getInterfaces(), hasKey(ifaceName));
    OspfInterface ospfIface = ospfArea.getInterfaces().get(ifaceName);
    assertThat(ospfIface.getEnable(), equalTo(Boolean.FALSE));
    assertThat(ospfIface.getPassive(), equalTo(Boolean.FALSE));
    assertThat(ospfIface.getMetric(), equalTo(10));
    assertThat(ospfIface.getPriority(), equalTo(1));
    assertThat(ospfIface.getHelloInterval(), equalTo(10));
    assertThat(ospfIface.getDeadCounts(), equalTo(4));
    assertThat(ospfIface.getRetransmitInterval(), equalTo(5));
    assertThat(ospfIface.getTransitDelay(), equalTo(1));
    assertThat(ospfIface.getLinkType(), equalTo(LinkType.BROADCAST));
  }

  @Test
  public void testOspfConversion() {
    String processId = "~OSPF_PROCESS_0.0.0.0";
    String ifaceName34 = "ethernet1/3.4";
    String ifaceName35 = "ethernet1/3.5";

    Configuration c = parseConfig("ospf");

    // process
    assertThat(c.getVrfs(), hasKey("vr1"));
    assertThat(c.getVrfs().get("vr1").getOspfProcesses(), hasKey(processId));

    OspfProcess ospfProcess = c.getVrfs().get("vr1").getOspfProcesses().get(processId);

    assertThat(ospfProcess, OspfProcessMatchers.hasRouterId(equalTo(Ip.parse("0.0.0.0."))));

    // areas
    assertThat(
        ospfProcess,
        hasArea(
            1L,
            allOf(
                hasStub(hasSuppressType3(false)),
                OspfAreaMatchers.hasInterfaces(equalTo(ImmutableSortedSet.of(ifaceName34))))));
    assertThat(ospfProcess, hasArea(2L, allOf(hasStubType(equalTo(StubType.NONE)))));
    assertThat(
        ospfProcess,
        hasArea(
            3L,
            allOf(
                hasNssa(
                    allOf(
                        hasDefaultOriginateType(OspfDefaultOriginateType.EXTERNAL_TYPE2),
                        NssaSettingsMatchers.hasSuppressType3(true))),
                OspfAreaMatchers.hasInterfaces(equalTo(ImmutableSortedSet.of(ifaceName35))))));

    // interface
    assertThat(
        c.getAllInterfaces().get(ifaceName34).getOspfSettings(),
        equalTo(
            OspfInterfaceSettings.builder()
                .setAreaName(1L)
                .setCost(14)
                .setDeadInterval(8 * 15)
                .setEnabled(true)
                .setHelloInterval(15)
                .setNetworkType(OspfNetworkType.POINT_TO_POINT)
                .setPassive(true)
                .setProcess(processId)
                .build()));
    assertThat(
        c.getAllInterfaces().get(ifaceName35).getOspfSettings(),
        equalTo(
            OspfInterfaceSettings.builder()
                .setAreaName(3L)
                .setCost(10)
                .setDeadInterval(4 * 10)
                .setEnabled(false)
                .setHelloInterval(10)
                .setNetworkType(OspfNetworkType.BROADCAST)
                .setPassive(false)
                .setProcess(processId)
                .build()));
  }

  @Test
  public void testApplicationsBuiltinReference() throws IOException {
    String hostname = "applications-builtin";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String app1Name = computeObjectName(DEFAULT_VSYS_NAME, "ssh");

    // Confirm reference count is correct for defined structure that overwrites builtin
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.APPLICATION, app1Name, 1));

    // Confirm using builtin does not produce undefined reference
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, APPLICATION_GROUP_OR_APPLICATION, "ssl", SECURITY_RULE_APPLICATION)));
  }

  @Test
  public void testDnsServerInvalid() {
    _thrown.expect(BatfishException.class);
    // This should throw a BatfishException due to a malformed IP address
    parseConfig("dns-server-invalid");
  }

  @Test
  public void testDnsServers() {
    Configuration c = parseConfig("dns-server");

    // Confirm both dns servers show up
    assertThat(c.getDnsServers(), containsInAnyOrder("1.9.10.99", "100.199.200.255"));
  }

  @Test
  public void testExternalList() throws IOException {
    String hostname = "external-list";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasNumReferrers(
            filename, EXTERNAL_LIST, computeObjectName(PANORAMA_VSYS_NAME, "MY_LIST"), 1));
  }

  @Test
  public void testFilesystemConfigFormat() {
    String hostname = "config-filesystem-format";
    Configuration c = parseConfig(hostname);

    // Confirm alternate config format is parsed and extracted properly
    // Confirm config devices set-line extraction works
    assertThat(c, hasHostname(equalTo(hostname)));
    // Confirm general config set-line extraction works
    assertThat(c.getLoggingServers(), contains("2.2.2.2"));
  }

  @Test
  public void testGlobalProtectAppCryptoProfiles() {
    PaloAltoConfiguration c = parsePaloAltoConfig("global-protect-app-crypto-profiles");

    assertThat(
        c.getCryptoProfiles(),
        containsInAnyOrder(
            new CryptoProfile(
                "default",
                Type.GLOBAL_PROTECT_APP,
                IpsecAuthenticationAlgorithm.HMAC_SHA1_96,
                null,
                ImmutableList.of(EncryptionAlgorithm.AES_128_CBC),
                null,
                null)));
  }

  @Test
  public void testHostname() throws IOException {
    String filename = "basic-parsing";
    String hostname = "my-hostname";

    // Confirm hostname extraction works
    assertThat(parseTextConfigs(filename).keySet(), contains(hostname));
  }

  @Test
  public void testIgnoredLines() {
    // will fail if some lines don't parse
    parseConfig("ignored-lines");
  }

  @Test
  public void testIkeCryptoProfiles() {
    PaloAltoConfiguration c = parsePaloAltoConfig("ike-crypto-profiles");

    assertThat(
        c.getCryptoProfiles(),
        containsInAnyOrder(
            new CryptoProfile(
                "default",
                Type.IKE,
                null,
                DiffieHellmanGroup.GROUP2,
                ImmutableList.of(EncryptionAlgorithm.AES_128_CBC, EncryptionAlgorithm.THREEDES_CBC),
                IkeHashingAlgorithm.SHA1,
                123 * 3600),
            new CryptoProfile(
                "profile1",
                Type.IKE,
                null,
                DiffieHellmanGroup.GROUP19,
                ImmutableList.of(EncryptionAlgorithm.AES_256_CBC),
                IkeHashingAlgorithm.SHA_384,
                8)));
  }

  @Test
  public void testIpsecCryptoProfiles() {
    PaloAltoConfiguration c = parsePaloAltoConfig("ipsec-crypto-profiles");

    assertThat(
        c.getCryptoProfiles(),
        containsInAnyOrder(
            new CryptoProfile(
                "default",
                Type.IPSEC,
                IpsecAuthenticationAlgorithm.HMAC_SHA1_96,
                DiffieHellmanGroup.GROUP2,
                ImmutableList.of(EncryptionAlgorithm.AES_128_CBC, EncryptionAlgorithm.DES_CBC),
                null,
                60),
            new CryptoProfile(
                "profile1",
                Type.IPSEC,
                null,
                DiffieHellmanGroup.GROUP14,
                ImmutableList.of(EncryptionAlgorithm.AES_128_GCM),
                null,
                24 * 3600)));
  }

  @Test
  public void testInterfaceExctactionWarning() throws IOException {
    String hostname = "interface";
    String hostnameBadLoopback = "interface-bad-loopback-addr";

    Batfish batfish = getBatfishForConfigurationNames(hostname, hostnameBadLoopback);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae.getWarnings().keySet(), hasItem(equalTo(hostname)));
    assertThat(ccae.getWarnings().keySet(), hasItem(equalTo(hostnameBadLoopback)));
    Warnings warn = ccae.getWarnings().get(hostname);
    Warnings warnBadLoopback = ccae.getWarnings().get(hostnameBadLoopback);

    // Should see two warnings:
    // 1. About extant object, but address range can't be used for interface address
    // 2. Interface not being associated w/ virtual-router, therefore being shutdown
    assertThat(
        warn.getRedFlagWarnings().stream().map(Warning::getText).collect(Collectors.toSet()),
        containsInAnyOrder(
            String.format(
                "Interface %s is not in a virtual-router, placing in %s and shutting it down.",
                "ethernet1/3.11", NULL_VRF_NAME),
            String.format(
                "Could not convert %s AddressObject '%s' to ConcreteInterfaceAddress.",
                AddressObject.Type.IP_RANGE, "ADDR3")));

    // Should see a warning about the loopback having a non /32 address
    assertThat(
        warnBadLoopback.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .collect(Collectors.toSet()),
        contains("Loopback ip address must be /32 or without mask"));
  }

  @Test
  public void testInterfaceExtraction() {
    PaloAltoConfiguration c = parsePaloAltoConfig("interface");
    Interface e1_1 = c.getInterfaces().get("ethernet1/1");
    Interface e1_4 = c.getInterfaces().get("ethernet1/4");
    Interface e1_5 = c.getInterfaces().get("ethernet1/5");
    Interface e1_7 = c.getInterfaces().get("ethernet1/7");

    assertThat(e1_1, notNullValue());
    InterfaceAddress e1_1_addr = e1_1.getAddress();
    assertThat(e1_1_addr, notNullValue());
    assertThat(e1_1_addr.getType(), equalTo(InterfaceAddress.Type.IP_PREFIX));
    assertThat(e1_1_addr.getValue(), equalTo("1.1.1.1/24"));

    assertThat(e1_4, notNullValue());
    assertThat(e1_4.getHa(), equalTo(true));

    assertThat(e1_5, notNullValue());
    InterfaceAddress e1_5_addr = e1_5.getAddress();
    assertThat(e1_5_addr, notNullValue());
    assertThat(e1_5_addr.getType(), equalTo(InterfaceAddress.Type.REFERENCE));
    assertThat(e1_5_addr.getValue(), equalTo("ADDR2"));

    assertThat(e1_7, notNullValue());
    InterfaceAddress e1_7_addr = e1_7.getAddress();
    assertThat(e1_7_addr, notNullValue());
    // Object reference that looks like an IP address
    assertThat(e1_7_addr.getType(), equalTo(InterfaceAddress.Type.IP_ADDRESS));
    assertThat(e1_7_addr.getValue(), equalTo("10.100.100.100"));
  }

  @Test
  public void testInterfaceConversion() {
    String hostname = "interface";
    String hostnameLoopbackRef = "interface-loopback-ref";
    String hostnameLoopbackRefInvalid = "interface-loopback-ref-invalid";
    String hostnameLoopbackRefInvalidRange = "interface-loopback-ref-invalid-range";
    String eth1_1 = "ethernet1/1";
    String eth1_2 = "ethernet1/2";
    String eth1_3 = "ethernet1/3";
    String eth1_3_11 = "ethernet1/3.11";
    String eth1_4 = "ethernet1/4";
    String eth1_5 = "ethernet1/5";
    String eth1_6 = "ethernet1/6";
    String eth1_7 = "ethernet1/7";
    String eth1_8 = "ethernet1/8";
    String eth1_21 = "ethernet1/21";
    String loopback = "loopback";
    Configuration c = parseConfig(hostname);
    Configuration cLoopbackRef = parseConfig(hostnameLoopbackRef);
    Configuration cLoopbackRefInvalid = parseConfig(hostnameLoopbackRefInvalid);
    Configuration cLoopbackRefInvalidRange = parseConfig(hostnameLoopbackRefInvalidRange);

    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder(
            eth1_1, eth1_2, eth1_3, eth1_3_11, eth1_4, eth1_5, eth1_6, eth1_7, eth1_8, eth1_21,
            loopback));
    assertThat(cLoopbackRef.getAllInterfaces().keySet(), contains(loopback));
    assertThat(cLoopbackRefInvalid.getAllInterfaces().keySet(), contains(loopback));
    assertThat(cLoopbackRefInvalidRange.getAllInterfaces().keySet(), contains(loopback));

    // Confirm interface MTU is extracted
    assertThat(c, hasInterface(eth1_1, hasMtu(9001)));

    // Confirm addresses are extracted, even when specified as address-object reference
    assertThat(
        c,
        hasInterface(
            eth1_1, hasAllAddresses(contains(ConcreteInterfaceAddress.parse("1.1.1.1/24")))));
    // From address object reference
    assertThat(
        c,
        hasInterface(
            eth1_5, hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.10.12.10/24")))));
    // Bad address object reference
    assertThat(c, hasInterface(eth1_6, hasAllAddresses(emptyIterable())));
    // From ambiguous address object reference (object name looks like an IP address)
    assertThat(
        c,
        hasInterface(
            eth1_7, hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.10.11.10/32")))));
    // Address-group object reference is not allowed
    assertThat(c, hasInterface(eth1_8, hasAllAddresses(emptyIterable())));
    assertThat(
        c,
        hasInterface(
            loopback,
            allOf(
                hasAddress("7.7.7.7/32"),
                hasAllAddresses(
                    contains(
                        ConcreteInterfaceAddress.parse("7.7.7.7/32"),
                        ConcreteInterfaceAddress.parse("7.7.7.8/32"))),
                hasBandwidth(nullValue()),
                hasSpeed(nullValue()))));

    // Confirm loopback addresses are extracted, even when specified as address-object reference
    assertThat(
        cLoopbackRef,
        hasInterface(
            loopback, hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.10.10.10/32")))));
    // Bad address object references
    assertThat(cLoopbackRefInvalid, hasInterface(loopback, hasAllAddresses(emptyIterable())));
    assertThat(cLoopbackRefInvalidRange, hasInterface(loopback, hasAllAddresses(emptyIterable())));

    // Confirm comments are extracted
    assertThat(c, hasInterface(eth1_1, hasDescription("description")));
    assertThat(c, hasInterface(eth1_2, hasDescription("interface's long description")));
    assertThat(c, hasInterface(eth1_3, hasDescription("single quoted description")));
    assertThat(c, hasInterface(eth1_3_11, hasDescription("unit description")));
    assertThat(c, hasInterface(eth1_4, hasDescription(nullValue())));

    // Confirm link speed
    assertThat(c.getAllInterfaces().get(eth1_1), allOf(hasBandwidth(1e9), hasSpeed(1e9)));
    assertThat(c.getAllInterfaces().get(eth1_2), allOf(hasBandwidth(1e9), hasSpeed(1e9)));
    assertThat(c.getAllInterfaces().get(eth1_3), allOf(hasBandwidth(1e9), hasSpeed(1e9)));
    assertThat(
        c.getAllInterfaces().get(eth1_3_11), allOf(hasBandwidth(1e9), hasSpeed(nullValue())));
    assertThat(c.getAllInterfaces().get(eth1_21), allOf(hasBandwidth(1e10), hasSpeed(1e10)));

    // Confirm link status is extracted
    assertThat(c, hasInterface(eth1_1, isActive()));
    assertThat(c, hasInterface(eth1_2, not(isActive())));
    assertThat(c, hasInterface(eth1_3, isActive()));
    assertThat(c, hasInterface(eth1_3_11, not(isActive())));

    // Confirm tag extraction for units
    assertThat(c, hasInterface(eth1_3_11, hasEncapsulationVlan(11)));

    // Confirm types
    assertThat(c, hasInterface(eth1_1, hasInterfaceType(InterfaceType.PHYSICAL)));
    assertThat(c, hasInterface(eth1_2, hasInterfaceType(InterfaceType.PHYSICAL)));
    assertThat(c, hasInterface(eth1_3, hasInterfaceType(InterfaceType.PHYSICAL)));
    assertThat(c, hasInterface(eth1_3_11, hasInterfaceType(InterfaceType.LOGICAL)));
    assertThat(c, hasInterface(eth1_21, hasInterfaceType(InterfaceType.PHYSICAL)));
  }

  @Test
  public void testInterfaceUnits() {
    String hostname = "interface-units";
    String interfaceNameUnit1 = "ethernet1/1.1";
    String interfaceNameUnit2 = "ethernet1/1.2";
    Configuration c = parseConfig(hostname);

    // Confirm interface MTU is extracted
    assertThat(c, hasInterface(interfaceNameUnit1, hasMtu(Interface.DEFAULT_INTERFACE_MTU)));
    assertThat(c, hasInterface(interfaceNameUnit2, hasMtu(1234)));

    // Confirm address is extracted
    assertThat(
        c,
        hasInterface(
            interfaceNameUnit1,
            allOf(
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("1.1.1.1/24"))),
                hasDependencies(contains(new Dependency("ethernet1/1", BIND))))));
    assertThat(
        c,
        hasInterface(
            interfaceNameUnit2,
            allOf(
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("1.1.2.1/24"))),
                hasDependencies(contains(new Dependency("ethernet1/1", BIND))))));

    // Confirm comment is extracted
    assertThat(c, hasInterface(interfaceNameUnit1, hasDescription("unit 1")));
  }

  // Test for https://github.com/batfish/batfish/issues/5598.
  @Test
  public void testInterfaceAggregateExtraction() {
    String hostname = "interface-agg";
    PaloAltoConfiguration c = parseNestedConfig(hostname);
    Map<String, Interface> interfaces = c.getInterfaces();
    assertThat(
        interfaces.keySet(),
        containsInAnyOrder("ethernet1/3", "ethernet1/21", "ethernet1/22", "ae1"));
    {
      Interface iface = interfaces.get("ethernet1/3");
      assertThat(iface.getAddress().getValue(), equalTo("10.0.0.1/29"));
    }
    {
      Interface iface = interfaces.get("ethernet1/21");
      assertThat(iface.getAddress(), nullValue());
      assertThat(iface.getAggregateGroup(), equalTo("ae1"));
    }
    {
      Interface iface = interfaces.get("ethernet1/22");
      assertThat(iface.getAddress(), nullValue());
      assertThat(iface.getAggregateGroup(), equalTo("ae1"));
    }
    {
      Interface iface = interfaces.get("ae1");
      assertThat(iface.getUnits().keySet(), containsInAnyOrder("ae1.290", "ae1.200", "ae1.201"));
      assertThat(iface.getAddress(), nullValue());
      assertThat(iface.getAggregateGroup(), nullValue());
      Interface ae1_290 = iface.getUnits().get("ae1.290");
      assertThat(ae1_290.getTag(), equalTo(290));
      assertThat(ae1_290.getAddress().getValue(), equalTo("10.0.1.1/29"));
      Interface ae1_200 = iface.getUnits().get("ae1.200");
      assertThat(ae1_200.getTag(), equalTo(200));
      assertThat(ae1_200.getAddress().getValue(), equalTo("10.0.2.1/29"));
      Interface ae1_201 = iface.getUnits().get("ae1.201");
      assertThat(ae1_201.getTag(), equalTo(201));
      assertThat(ae1_201.getAddress().getValue(), equalTo("10.0.3.1/29"));
    }
  }

  // Test for https://github.com/batfish/batfish/issues/5598.
  @Test
  public void testInterfaceAggregateConversion() {
    String hostname = "interface-agg";
    Configuration c = parseConfig(hostname);
    Map<String, org.batfish.datamodel.Interface> interfaces = c.getAllInterfaces();
    assertThat(
        interfaces.keySet(),
        containsInAnyOrder(
            "ethernet1/3", "ethernet1/21", "ethernet1/22", "ae1", "ae1.290", "ae1.200", "ae1.201"));
    {
      org.batfish.datamodel.Interface e3 = interfaces.get("ethernet1/3");
      assertThat(e3.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
      assertThat(e3.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.0.1/29")));
      assertThat(e3.getChannelGroup(), nullValue());
      assertThat(e3.getChannelGroupMembers(), empty());
    }
    {
      org.batfish.datamodel.Interface e21 = interfaces.get("ethernet1/21");
      assertThat(e21.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
      assertThat(e21.getAddress(), nullValue());
      assertThat(e21.getChannelGroup(), equalTo("ae1"));
      assertThat(e21.getChannelGroupMembers(), empty());
    }
    {
      org.batfish.datamodel.Interface e22 = interfaces.get("ethernet1/22");
      assertThat(e22.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
      assertThat(e22.getAddress(), nullValue());
      assertThat(e22.getChannelGroup(), equalTo("ae1"));
      assertThat(e22.getChannelGroupMembers(), empty());
    }
    {
      org.batfish.datamodel.Interface ae1 = interfaces.get("ae1");
      assertThat(ae1, hasBandwidth(20e9));
      assertThat(ae1.getInterfaceType(), equalTo(InterfaceType.AGGREGATED));
      assertThat(ae1.getAddress(), nullValue());
      assertThat(ae1.getChannelGroup(), nullValue());
      assertThat(ae1.getChannelGroupMembers(), containsInAnyOrder("ethernet1/21", "ethernet1/22"));
      assertThat(
          ae1.getDependencies(),
          containsInAnyOrder(
              new Dependency("ethernet1/21", DependencyType.AGGREGATE),
              new Dependency("ethernet1/22", DependencyType.AGGREGATE)));
    }
    {
      org.batfish.datamodel.Interface iface = interfaces.get("ae1.290");
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.1.1/29")));
      assertThat(iface, hasBandwidth(20e9));
      assertThat(iface.getChannelGroup(), nullValue());
      assertThat(iface.getChannelGroupMembers(), empty());
      assertThat(iface.getEncapsulationVlan(), equalTo(290));
    }
    {
      org.batfish.datamodel.Interface iface = interfaces.get("ae1.200");
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.2.1/29")));
      assertThat(iface, hasBandwidth(20e9));
      assertThat(iface.getChannelGroup(), nullValue());
      assertThat(iface.getChannelGroupMembers(), empty());
      assertThat(iface.getEncapsulationVlan(), equalTo(200));
    }
    {
      org.batfish.datamodel.Interface iface = interfaces.get("ae1.201");
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.3.1/29")));
      assertThat(iface, hasBandwidth(20e9));
      assertThat(iface.getChannelGroup(), nullValue());
      assertThat(iface.getChannelGroupMembers(), empty());
      assertThat(iface.getEncapsulationVlan(), equalTo(201));
    }
  }

  @Test
  public void testInterfaceReference() throws IOException {
    String hostname = "interface-reference";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference counts are correct for both used and unused structures
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/1", 1));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/unused", 0));

    // Confirm undefined reference is detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, INTERFACE, "ethernet1/undefined", VIRTUAL_ROUTER_INTERFACE));
  }

  @Test
  public void testInterfaceVirtualRouterAssignment() {
    String hostname = "interface-virtual-router";
    Configuration c = parseConfig(hostname);

    // Make sure each interface is associated with the correct vrf
    assertThat(
        c, hasInterface("ethernet1/1", InterfaceMatchers.hasVrf(hasName(equalTo("default")))));
    assertThat(
        c, hasInterface("ethernet1/2", InterfaceMatchers.hasVrf(hasName(equalTo("somename")))));
    assertThat(
        c, hasInterface("ethernet1/3", InterfaceMatchers.hasVrf(hasName(equalTo("somename")))));

    // Make sure the orphaned vrf is associated with the "null", constructed vrf
    assertThat(
        c, hasInterface("ethernet1/4", InterfaceMatchers.hasVrf(hasName(equalTo(NULL_VRF_NAME)))));
  }

  @Test
  public void testLogSettingsSyslog() {
    Configuration c = parseConfig("log-settings-syslog");

    // Confirm all the defined syslog servers show up in VI model
    assertThat(
        c.getLoggingServers(), containsInAnyOrder("1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4"));
  }

  @Test
  public void testMgmtIface() {
    PaloAltoConfiguration c = parsePaloAltoConfig("mgmt-iface");

    assertThat(c.getMgmtIfaceAddress(), equalTo(Ip.parse("12.25.51.103")));
    assertThat(c.getMgmtIfaceGateway(), equalTo(Ip.parse("12.25.51.1")));
    assertThat(c.getMgmtIfaceNetmask(), equalTo(Ip.parse("255.255.255.128")));
  }

  @Test
  public void testNestedConfig() throws IOException {
    String hostname = "nested-config";

    // Confirm a simple extraction (hostname) works for nested config format
    assertThat(parseTextConfigs(hostname).keySet(), contains(hostname));
  }

  @Test
  public void testNestedConfigLineComments() throws IOException {
    String hostname = "nested-config-line-comments";

    // Confirm extraction works for nested configs even in the presence of line comments
    assertThat(parseTextConfigs(hostname).keySet(), contains(hostname));
  }

  @Test
  public void testLineMapMultilineToken() {
    String hostname = "multiline-token";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            PALO_ALTO_NESTED,
            BATFISH_FLATTENED_PALO_ALTO_HEADER);
    FlattenerLineMap map = flattener.getOriginalLineMap();

    String[] lines = flattener.getFlattenedConfigurationText().split("\n", -1);

    /* Line numbers for a multiline token in output text */
    int startTokenLine = 2;
    int midTokenLine = 5;
    int endTokenLine = 9;
    /* Line number for a particular set line */
    int setLine = 10;

    /* lines array is 0 indexed, line numbers are 1 indexed */
    String setLineText = lines[setLine - 1];
    String startTokenText = lines[startTokenLine - 1];
    String midTokenText = lines[midTokenLine - 1];
    String endTokenText = lines[endTokenLine - 1];
    /* Sanity check; make sure flattening produces the line we expect, where we expect them */
    assertThat(setLineText, equalTo("set config shared certificate cert-name algorithm RSA"));
    assertThat(startTokenText, containsString("BEGIN CERT"));
    assertThat(midTokenText, containsString("AAaaaaa"));
    assertThat(endTokenText, containsString("\""));

    /* Confirm original line numbers are preserved thru map, note line number here starts from 1 */
    assertThat(map.getOriginalLine(setLine, setLineText.indexOf("certificate")), equalTo(3));
    assertThat(map.getOriginalLine(setLine, setLineText.indexOf("algorithm")), equalTo(13));
    /* Start, middle, and end lines in a multiline token should be mapped to public key line */
    assertThat(
        map.getOriginalLine(startTokenLine, startTokenText.indexOf("BEGIN CERT")), equalTo(5));
    assertThat(map.getOriginalLine(midTokenLine, midTokenText.indexOf("AAaaaaa")), equalTo(5));
    assertThat(map.getOriginalLine(endTokenLine, endTokenText.indexOf("\"")), equalTo(5));

    /* Line beyond end of config should not be mapped */
    assertThat(
        map.getOriginalLine(lines.length, 0), equalTo(FlattenerLineMap.UNMAPPED_LINE_NUMBER));
  }

  @Test
  public void testNestedConfigLineMap() {
    String hostname = "nested-config";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            PALO_ALTO_NESTED,
            BATFISH_FLATTENED_PALO_ALTO_HEADER);
    FlattenerLineMap lineMap = flattener.getOriginalLineMap();
    /*
     * Flattened config should be two lines: header line and set-hostname line
     * This test is only checking content of the set-hostname line
     */
    String setLineText = flattener.getFlattenedConfigurationText().split("\n", -1)[1];

    /* Confirm original line numbers are preserved */
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("deviceconfig")), equalTo(1));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("system")), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("hostname")), equalTo(3));
    assertThat(lineMap.getOriginalLine(2, setLineText.indexOf("nested-config")), equalTo(3));
  }

  @Test
  public void testNestedConfigStructureDef() throws IOException {
    String hostname = "nested-config-structure-def";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm defined structures in nested config show up with original definition line numbers
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, INTERFACE, "ethernet1/1", contains(9, 10, 11, 12)));
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename, INTERFACE, "ethernet1/2", contains(16, 17, 18, 19)));
  }

  @Test
  public void testNtpServers() {
    Configuration c = parseConfig("ntp-server");

    // Confirm both ntp servers show up
    assertThat(c.getNtpServers(), containsInAnyOrder("1.1.1.1", "ntpservername"));
  }

  @Test
  public void testRulebaseWithPanorama() {
    String hostname = "panorama-rulebase";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";
    Flow.Builder baseBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setSrcIp(Ip.ZERO)
            .setDstIp(Ip.ZERO)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1);
    // See comments in the config for how this test works.
    Flow service1 = baseBuilder.setDstPort(1001).build();
    Flow service2 = baseBuilder.setDstPort(1002).build();
    Flow service3 = baseBuilder.setDstPort(1003).build();
    Flow service4 = baseBuilder.setDstPort(1004).build();

    // Cross-zone flows (starting e2)
    assertThat(
        c,
        hasInterface(
            if1name,
            hasOutgoingOriginalFlowFilter(
                allOf(
                    accepts(service1, if2name, c),
                    rejects(service2, if2name, c),
                    accepts(service3, if2name, c),
                    rejects(service4, if2name, c)))));

    // Intra-zone flows (starting e1)
    assertThat(
        c,
        hasInterface(
            if1name,
            hasOutgoingOriginalFlowFilter(
                allOf(
                    accepts(service1, if1name, c),
                    rejects(service2, if1name, c),
                    accepts(service3, if1name, c),
                    accepts(service4, if1name, c)))));
  }

  @Test
  public void testRulebaseInApplicationWithPanorama() {
    String hostname = "panorama-rulebase-applications";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";
    Flow.Builder tcpBaseBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setSrcIp(Ip.ZERO)
            .setDstIp(Ip.ZERO)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(1);
    Flow.Builder udpBaseBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setSrcIp(Ip.ZERO)
            .setDstIp(Ip.ZERO)
            .setIpProtocol(IpProtocol.UDP)
            .setSrcPort(1);
    // See comments in the config for how this test works.
    Flow webapplication = tcpBaseBuilder.setDstPort(80).build();
    Flow sslapplication = tcpBaseBuilder.setDstPort(443).build();
    Flow snmpapplication = udpBaseBuilder.setDstPort(161).build();
    Flow pingapplication =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setSrcIp(Ip.ZERO)
            .setDstIp(Ip.ZERO)
            .setIpProtocol(IpProtocol.ICMP)
            .setIcmpType(IcmpType.ECHO_REPLY)
            .setIcmpCode(0)
            .build();
    assertThat(
        c,
        hasInterface(
            if1name,
            hasOutgoingOriginalFlowFilter(
                allOf(
                    rejects(webapplication, if2name, c),
                    accepts(sslapplication, if2name, c),
                    accepts(snmpapplication, if2name, c),
                    accepts(pingapplication, if2name, c)))));
  }

  @Test
  public void testRulebase() {
    String hostname = "rulebase";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";
    String if3name = "ethernet1/3";
    String if4name = "ethernet1/4";
    Flow z1ToZ1permitted = createFlow("1.1.2.255", "1.1.1.2");
    Flow z1ToZ1rejectedSource = createFlow("2.2.2.2", "1.1.1.2");
    Flow z1ToZ1rejectedDestination = createFlow("1.1.2.2", "1.1.1.3");
    Flow z1ToZ1rejectedService = createFlow("1.1.2.2", "1.1.1.2", IpProtocol.TCP, 1, 999);
    Flow z2ToZ1permitted = createFlow("1.1.4.255", "1.1.1.2");
    Flow noZoneToZ1rejected = createFlow("1.1.3.2", "1.1.1.2");
    Flow fromDevicePermitted = createFlow("1.1.1.1", "1.1.1.2");

    // Confirm flow matching deny rule is rejected
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(rejects(z1ToZ1rejectedService, if2name, c))));

    // Confirm intrazone flow matching allow rule is accepted
    assertThat(
        c,
        hasInterface(if1name, hasOutgoingOriginalFlowFilter(accepts(z1ToZ1permitted, if2name, c))));
    // Confirm intrazone flow not matching allow rule (bad destination address) is rejected
    assertThat(
        c,
        hasInterface(
            if1name,
            hasOutgoingOriginalFlowFilter(rejects(z1ToZ1rejectedDestination, if2name, c))));
    // Confirm intrazone flow not matching allow rule (source address negated) is rejected
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(rejects(z1ToZ1rejectedSource, if2name, c))));

    // Confirm interzone flow matching allow rule is accepted
    assertThat(
        c,
        hasInterface(if1name, hasOutgoingOriginalFlowFilter(accepts(z2ToZ1permitted, if4name, c))));

    // Confirm flow from an unzoned interface is rejected
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(rejects(noZoneToZ1rejected, if3name, c))));

    // Confirm flow from the device itself is permitted
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(accepts(fromDevicePermitted, null, c))));
  }

  @Test
  public void testSecurityRulesNotExplicitlyMatched() {
    /*
    Setup: Device has an interface in a zone with intrazone security rules. If flows leaving the
    interface match an intrazone rule, its action should be taken (whether permit or deny). Other
    flows should fall through to the rest of the interface outgoing filter.
     */
    String hostname = "security-no-explicit-match";
    Configuration c = parseConfig(hostname);
    String ifaceName = "ethernet1/1.1";
    IpAccessList ifaceOutgoingFilter =
        c.getIpAccessLists().get(computeOutgoingFilterName(ifaceName));

    Flow notMatchingZoneSecurity =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setDstIp(Ip.parse("10.10.10.10"))
            .setSrcIp(Ip.parse("10.10.10.20"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    Flow rejectedByZoneSecurity =
        notMatchingZoneSecurity.toBuilder().setSrcIp(Ip.parse("2.2.2.2")).build();
    Flow acceptedByZoneSecurity =
        notMatchingZoneSecurity.toBuilder().setSrcIp(Ip.parse("3.3.3.3")).build();

    // Interface outgoing filter ends with an OriginatingFromDevice match.
    // If flow doesn't match zone rules, it should fall through to that.
    int lastLineIndex = ifaceOutgoingFilter.getLines().size() - 1;
    FilterResult originatingFromDeviceResult =
        ifaceOutgoingFilter.filter(
            notMatchingZoneSecurity, null, c.getIpAccessLists(), ImmutableMap.of());
    assertThat(originatingFromDeviceResult.getAction(), equalTo(LineAction.PERMIT));
    assertThat(originatingFromDeviceResult.getMatchLine(), equalTo(lastLineIndex));

    // Flow is from interface inside the zone and is rejected by zone security rules
    FilterResult rejectedByZoneSecurityResult =
        ifaceOutgoingFilter.filter(
            rejectedByZoneSecurity, ifaceName, c.getIpAccessLists(), ImmutableMap.of());
    assertThat(rejectedByZoneSecurityResult.getAction(), equalTo(LineAction.DENY));
    assertThat(rejectedByZoneSecurityResult.getMatchLine(), lessThan(lastLineIndex));

    // Flow is from interface inside the zone and is permitted by zone security rules
    FilterResult acceptedByZoneSecurityResult =
        ifaceOutgoingFilter.filter(
            acceptedByZoneSecurity, ifaceName, c.getIpAccessLists(), ImmutableMap.of());
    assertThat(acceptedByZoneSecurityResult.getAction(), equalTo(LineAction.PERMIT));
    assertThat(acceptedByZoneSecurityResult.getMatchLine(), lessThan(lastLineIndex));
  }

  @Test
  public void testShowConfig() {
    PaloAltoConfiguration vc = parseNestedConfig("show-config");
    assertThat(vc.getHostname(), equalTo("show-config-custom-hostname"));
  }

  @Test
  public void testIntrazoneFilterTraceElements() {
    String hostname = "security-no-explicit-match";
    String filename = "configs/" + hostname;
    Configuration c = parseConfig(hostname);
    String vsysName = "vsys1";
    String zoneName = "ZONE";

    // Device has a zone with intrazone security rules DENY and PERMIT
    String zoneObjName = computeObjectName(vsysName, zoneName);
    String intrazoneFilterName = zoneToZoneFilter(zoneObjName, zoneObjName);
    String zoneOutgoingFilterName = computeOutgoingFilterName(zoneObjName);
    IpAccessList intrazoneFilter = c.getIpAccessLists().get(intrazoneFilterName);
    IpAccessList zoneOutgoingFilter = c.getIpAccessLists().get(zoneOutgoingFilterName);

    // Expected trace elements in intrazone filter
    TraceElement ruleDenyTe = matchSecurityRuleTraceElement("DENY", vsysName, filename);
    TraceElement rulePermitTe = matchSecurityRuleTraceElement("PERMIT", vsysName, filename);
    TraceElement intrazoneDefaultTe = intrazoneDefaultAcceptTraceElement(vsysName, zoneName);

    // Expected trace elements in zone outgoing filter
    TraceElement intrazoneRulesTe = zoneToZoneMatchTraceElement(zoneName, zoneName, vsysName);
    TraceElement mismatchIntrazoneRulesTe =
        zoneToZoneRejectTraceElement(zoneName, zoneName, vsysName);

    assertThat(
        intrazoneFilter.getLines(),
        contains(
            hasTraceElement(ruleDenyTe),
            hasTraceElement(rulePermitTe),
            hasTraceElement(intrazoneDefaultTe)));
    assertThat(
        zoneOutgoingFilter.getLines(),
        contains(hasTraceElement(intrazoneRulesTe), hasTraceElement(mismatchIntrazoneRulesTe)));
  }

  @Test
  public void testIfaceOutgoingFilterTraceElements() {
    // Device has an interface in the zone. Ensure that outgoing filter lines have expected trace
    // elements and flows leaving the interface generate the expected ACL traces.
    String hostname = "security-no-explicit-match";
    String filename = "configs/" + hostname;
    Configuration c = parseConfig(hostname);
    String vsysName = "vsys1";
    String zoneName = "ZONE";

    // Expected trace elements in zone outgoing filter
    TraceElement intrazoneRulesTe = zoneToZoneMatchTraceElement(zoneName, zoneName, vsysName);
    TraceElement intrazoneRejectRulesTe =
        zoneToZoneRejectTraceElement(zoneName, zoneName, vsysName);

    String ifaceName = "ethernet1/1.1";
    String ifaceOutgoingFilterName = computeOutgoingFilterName(ifaceName);
    IpAccessList ifaceOutgoingFilter = c.getIpAccessLists().get(ifaceOutgoingFilterName);

    // Expected trace elements in interface outgoing filter
    TraceElement exitIfaceTe = ifaceOutgoingTraceElement(ifaceName, zoneName, vsysName);
    TraceElement originatedTe = originatedFromDeviceTraceElement();
    assertThat(
        ifaceOutgoingFilter.getLines(),
        contains(hasTraceElement(exitIfaceTe), hasTraceElement(originatedTe)));

    // Flow that does not match either security rule should fall through to intrazone default
    Flow.Builder fb = Flow.builder().setDstIp(Ip.ZERO).setIngressNode("n");
    List<TraceTree> flowTrace =
        AclTracer.trace(
            ifaceOutgoingFilter,
            fb.setSrcIp(Ip.parse("1.1.1.1")).build(),
            ifaceName,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        flowTrace,
        contains(
            isChainOfSingleChildren(
                exitIfaceTe,
                intrazoneRulesTe,
                intrazoneDefaultAcceptTraceElement(vsysName, zoneName))));

    // Flow matching DENY security rule should generate a trace pointing to that rule.
    flowTrace =
        AclTracer.trace(
            ifaceOutgoingFilter,
            fb.setSrcIp(Ip.parse("2.2.2.2")).build(), // should match DENY rule
            ifaceName,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        flowTrace,
        contains(
            isChainOfSingleChildren(
                exitIfaceTe,
                intrazoneRejectRulesTe,
                matchSecurityRuleTraceElement("DENY", vsysName, filename))));

    // Flow matching PERMIT security rule should generate a trace pointing to that rule.
    flowTrace =
        AclTracer.trace(
            ifaceOutgoingFilter,
            fb.setSrcIp(Ip.parse("3.3.3.3")).build(), // should match PERMIT rule
            ifaceName,
            c.getIpAccessLists(),
            ImmutableMap.of(),
            ImmutableMap.of());
    assertThat(
        flowTrace,
        contains(
            isChainOfSingleChildren(
                exitIfaceTe,
                intrazoneRulesTe,
                matchSecurityRuleTraceElement("PERMIT", vsysName, filename))));
  }

  @Test
  public void testUnzonedIfaceOutgoingFilterTraceElements() {
    // Device has an interface without a zone; its outgoing filter should reflect that
    String hostname = "security-no-explicit-match";
    Configuration c = parseConfig(hostname);
    String ifaceName = "ethernet1/1";

    // This ACL has an unnecessary (unreachable) second line; no need to assert on that line
    IpAccessList ifaceOutgoingFilter =
        c.getIpAccessLists().get(computeOutgoingFilterName(ifaceName));
    assertThat(
        ifaceOutgoingFilter.getLines().get(0),
        equalTo(
            ExprAclLine.REJECT_ALL
                .toBuilder()
                .setName("Not in a zone")
                .setTraceElement(unzonedIfaceRejectTraceElement(ifaceName))
                .build()));
  }

  @Test
  public void testEmptyZoneOutgoingFilterTraceElements() {
    // Device has a zone without any interfaces; intra- and cross-zone filters should reflect that
    String hostname = "security-no-explicit-match";
    Configuration c = parseConfig(hostname);
    String vsysName = "vsys1";
    String zoneName = "ZONE";
    String emptyZoneName = "EMPTY_ZONE";
    String zoneObjName = computeObjectName(vsysName, zoneName);
    String emptyZoneObjName = computeObjectName(vsysName, emptyZoneName);

    String emptyIntrazoneFilterName = zoneToZoneFilter(emptyZoneObjName, emptyZoneObjName);
    String emptyToNonEmptyFilterName = zoneToZoneFilter(emptyZoneObjName, zoneObjName);
    String nonEmptyToEmptyFilterName = zoneToZoneFilter(zoneObjName, emptyZoneObjName);
    ImmutableList.of(emptyIntrazoneFilterName, emptyToNonEmptyFilterName, nonEmptyToEmptyFilterName)
        .forEach(
            filterName -> {
              IpAccessList filter = c.getIpAccessLists().get(filterName);
              assertThat(
                  filter.getLines(),
                  contains(hasTraceElement(emptyZoneRejectTraceElement(vsysName, emptyZoneName))));
            });
  }

  @Test
  public void testRulebaseService() {
    String hostname = "rulebase-service";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if4name = "ethernet1/4";
    Flow z1ToZ2OverridePermitted = createFlow("1.1.1.1", "1.1.1.2", IpProtocol.TCP, 999, 1);
    Flow z1ToZ2HttpRejected = createFlow("1.1.1.1", "1.1.1.2", IpProtocol.TCP, 999, 80);
    Flow z1ToZ2HttpsPermitted = createFlow("1.1.1.1", "1.1.1.2", IpProtocol.TCP, 999, 443);

    // Confirm flow matching overridden service is permitted
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(accepts(z1ToZ2OverridePermitted, if4name, c))));
    // Confirm flow matching built-in service that was overridden is rejected
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(rejects(z1ToZ2HttpRejected, if4name, c))));
    // Confirm flow matching built-in service, indirectly referenced is permitted
    assertThat(
        c,
        hasInterface(
            if1name, hasOutgoingOriginalFlowFilter(accepts(z1ToZ2HttpsPermitted, if4name, c))));
  }

  @Test
  public void testRulebaseDefault() {
    String hostname = "rulebase-default";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";
    String if3name = "ethernet1/3";
    String if4name = "ethernet1/4";
    Flow z1ToZ1 = createFlow("1.1.1.2", "1.1.2.2");
    Flow z1ToZ2 = createFlow("1.1.1.2", "1.1.4.2");
    Flow z2ToZ1 = createFlow("1.1.4.2", "1.1.1.2");
    Flow noZoneToZ1 = createFlow("1.1.3.2", "1.1.1.2");
    Flow z1ToNoZone = createFlow("1.1.1.2", "1.1.3.2");

    // Confirm intrazone flow is accepted by default
    assertThat(
        c, hasInterface(if2name, hasOutgoingOriginalFlowFilter(accepts(z1ToZ1, if1name, c))));

    // Confirm interzone flows are rejected by default
    assertThat(
        c, hasInterface(if4name, hasOutgoingOriginalFlowFilter(rejects(z1ToZ2, if1name, c))));
    assertThat(
        c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(z2ToZ1, if4name, c))));

    // Confirm unzoned flows are rejected by default
    assertThat(
        c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(noZoneToZ1, if3name, c))));
    assertThat(
        c, hasInterface(if3name, hasOutgoingOriginalFlowFilter(rejects(z1ToNoZone, if1name, c))));
  }

  @Test
  public void testRulebaseIprange() {
    String hostname = "rulebase-iprange";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";

    // rule1: 11.11.11.11 should be allowed but 11.11.11.13 shouldn't be
    Flow rule1Permitted = createFlow("11.11.11.11", "33.33.33.33");
    Flow rule1Denied = createFlow("11.11.11.13", "33.33.33.33");

    assertThat(
        c,
        hasInterface(if1name, hasOutgoingOriginalFlowFilter(accepts(rule1Permitted, if2name, c))));
    assertThat(
        c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(rule1Denied, if2name, c))));
  }

  @Test
  public void testNatServiceReference() throws IOException {
    String hostname = "nat-service";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String serviceGroupName = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE_GROUP1");

    // Confirm reference count is correct for the used service group
    assertThat(
        ccae, hasNumReferrers(filename, PaloAltoStructureType.SERVICE_GROUP, serviceGroupName, 1));

    // Confirm we get an undefined reference for the undefined service/group
    assertThat(ccae, hasUndefinedReference(filename, SERVICE_OR_SERVICE_GROUP, "SERVICE_UNDEF"));
  }

  @Test
  public void testRulebaseReference() throws IOException {
    String hostname = "rulebase";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String serviceName = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE1");
    String z1Name = computeObjectName(DEFAULT_VSYS_NAME, "z1");
    String z2Name = computeObjectName(DEFAULT_VSYS_NAME, "z2");
    String zoneUndefName = computeObjectName(DEFAULT_VSYS_NAME, "ZONE_UNDEF");

    // Confirm reference count is correct for used structure
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.SERVICE, serviceName, 1));
    // 2 references in rules, 2 references from associated interfaces
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.ZONE, z1Name, 4));
    // 2 references in rules, 1 reference from associated interface
    assertThat(ccae, hasNumReferrers(filename, PaloAltoStructureType.ZONE, z2Name, 3));

    // Confirm undefined references are detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP,
            "SERVICE_UNDEF",
            PaloAltoStructureUsage.SECURITY_RULE_SERVICE));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            PaloAltoStructureType.ZONE,
            zoneUndefName,
            PaloAltoStructureUsage.SECURITY_RULE_FROM_ZONE));

    // Confirm builtins do not show up as undefined references
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, SERVICE_OR_SERVICE_GROUP, ServiceBuiltIn.ANY.getName())));
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, SERVICE_OR_SERVICE_GROUP, ServiceBuiltIn.APPLICATION_DEFAULT.getName())));
  }

  @Test
  public void testNatRulesAndReferences() {
    String hostname = "rulebase-nat";

    // Check VS model
    PaloAltoConfiguration vendorConfig = parsePaloAltoConfig(hostname);
    Map<String, NatRule> natRules =
        vendorConfig.getVirtualSystems().get(DEFAULT_VSYS_NAME).getRulebase().getNatRules();

    // In order of appearance
    String rule1Name = "RULE1";
    String rule2Name = "2nd_RULE";
    RuleEndpoint anyRuleEndpoint = new RuleEndpoint(Any, "any");
    assertThat(natRules.keySet(), contains(rule1Name, rule2Name));

    NatRule rule1 = natRules.get(rule1Name);
    assertThat(rule1.getTo(), equalTo("TO_ZONE"));
    assertThat(rule1.getFrom(), contains("any"));
    assertThat(rule1.getSource(), contains(anyRuleEndpoint));
    assertThat(rule1.getDestination(), contains(anyRuleEndpoint));
    assertThat(
        rule1.getSourceTranslation().getDynamicIpAndPort().getTranslatedAddresses(),
        contains(
            new RuleEndpoint(IP_ADDRESS, "1.1.1.1"),
            new RuleEndpoint(IP_PREFIX, "2.2.2.0/24"),
            new RuleEndpoint(IP_RANGE, "3.3.3.3-4.4.4.4")));
    assertTrue(rule1.getDisabled());

    NatRule rule2 = natRules.get(rule2Name);
    assertThat(rule2.getTo(), equalTo("TO_2"));
    assertThat(rule2.getFrom(), contains("FROM_1", "FROM_2", "FROM_3"));
    assertThat(
        rule2.getSource(),
        contains(
            new RuleEndpoint(REFERENCE, "SRC_1"),
            new RuleEndpoint(REFERENCE, "SRC_2"),
            new RuleEndpoint(REFERENCE, "SRC_3")));
    assertThat(
        rule2.getDestination(),
        contains(
            new RuleEndpoint(REFERENCE, "DST_1"),
            new RuleEndpoint(REFERENCE, "DST_2"),
            new RuleEndpoint(REFERENCE, "DST_3")));
    assertThat(
        rule2.getSourceTranslation().getDynamicIpAndPort().getTranslatedAddresses(),
        contains(
            new RuleEndpoint(REFERENCE, "SRC_1"),
            new RuleEndpoint(REFERENCE, "SRC_2"),
            new RuleEndpoint(REFERENCE, "SRC_3")));
    assertThat(
        rule2.getDestinationTranslation().getTranslatedAddress(),
        equalTo(new RuleEndpoint(REFERENCE, "DST_2")));
    assertThat(rule2.getDestinationTranslation().getTranslatedPort(), equalTo(1234));
    assertFalse(rule2.getDisabled());
  }

  @Test
  public void testSharedRulebase() {
    String hostname = "shared-rulebase";
    PaloAltoConfiguration vendorConfig = parsePaloAltoConfig(hostname);

    Map<String, NatRule> natPreRules = vendorConfig.getShared().getPreRulebase().getNatRules();
    Map<String, SecurityRule> securityPreRules =
        vendorConfig.getShared().getPreRulebase().getSecurityRules();
    Map<String, NatRule> natPostRules = vendorConfig.getShared().getPostRulebase().getNatRules();
    Map<String, SecurityRule> securityPostRules =
        vendorConfig.getShared().getPostRulebase().getSecurityRules();

    // Make sure shared pre- and post- rules are associated with the shared vsys
    assertThat(natPreRules.keySet(), contains("PRE_NAT"));
    assertThat(natPreRules.get("PRE_NAT").getTo(), equalTo("Z1"));

    assertThat(natPostRules.keySet(), contains("POST_NAT"));
    assertThat(natPostRules.get("POST_NAT").getTo(), equalTo("Z2"));

    assertThat(securityPreRules.keySet(), contains("PRE_SECURITY"));
    assertThat(securityPreRules.get("PRE_SECURITY").getTo(), contains("Z1"));

    assertThat(securityPostRules.keySet(), contains("POST_SECURITY"));
    assertThat(securityPostRules.get("POST_SECURITY").getTo(), contains("Z2"));
  }

  @Test
  public void testVsysRulebase() {
    String hostname = "vsys-rulebase";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1";
    String if2name = "ethernet1/2";
    String if3name = "ethernet1/3";
    String if4name = "ethernet1/4";
    Flow flow = createFlow("1.1.1.1", "2.2.2.2");

    // Confirm flow is rejected in vsys1
    assertThat(c, hasInterface(if1name, hasOutgoingOriginalFlowFilter(rejects(flow, if2name, c))));

    // Confirm intravsys flow is accepted in vsys2
    assertThat(c, hasInterface(if3name, hasOutgoingOriginalFlowFilter(accepts(flow, if4name, c))));
    // Confirm intervsys flow (even from same zone name) is rejected in vsys2
    assertThat(c, hasInterface(if3name, hasOutgoingOriginalFlowFilter(rejects(flow, if2name, c))));
  }

  @Test
  public void testStaticRoute() throws IOException {
    String vrName = "somename";
    String vr2Name = "vr2";
    String hostname = "static-route";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);

    // Confirm static route shows up with correct extractions
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasAdministrativeCost(equalTo(123))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasMetric(equalTo(12L))))));
    assertThat(
        c, hasVrf(vrName, hasStaticRoutes(hasItem(hasNextHopIp(equalTo(Ip.parse("1.1.1.1")))))));
    assertThat(
        c, hasVrf(vrName, hasStaticRoutes(hasItem(hasNextHopInterface(equalTo("ethernet1/1"))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasPrefix(Prefix.ZERO)))));
    assertThat(
        c,
        hasVrf(
            vr2Name,
            hasStaticRoutes(
                hasItem(allOf(hasPrefix(Prefix.strict("10.0.0.0/8")), hasNextVrf(vrName))))));
    // static-route with undefined next-vr should not be converted
    assertThat(c, hasVrf(vr2Name, hasStaticRoutes(not(hasItem(hasPrefix(Prefix.ZERO))))));
    // static-route with next-vr same as its own virtual-router should not be converted
    assertThat(
        c, hasVrf(vr2Name, hasStaticRoutes(not(hasItem(hasPrefix(Prefix.strict("1.0.0.0/8")))))));

    // static route with next hop specified as prefix
    assertThat(
        c,
        hasVrf(
            vr2Name,
            hasStaticRoutes(
                hasItem(
                    allOf(
                        hasPrefix(Prefix.strict("2.0.0.0/8")),
                        hasNextHopIp(Ip.parse("2.2.2.2")))))));

    // assert static interface route reference
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + hostname;
    assertThat(
        ccae, hasUndefinedReference(filename, INTERFACE, "ethernet1/1", STATIC_ROUTE_INTERFACE));
    assertThat(
        ccae, hasUndefinedReference(filename, PaloAltoStructureType.VIRTUAL_ROUTER, "fakevr"));
  }

  @Test
  public void testStaticRouteExtraction() {
    String hostname = "static-route";
    PaloAltoConfiguration vc = parsePaloAltoConfig(hostname);
    String vrName = "vr2";

    assertThat(vc.getVirtualRouters(), hasKey(vrName));

    assertThat(vc.getVirtualRouters().get(vrName).getStaticRoutes(), hasKey("ROUTE2"));

    StaticRoute sr = vc.getVirtualRouters().get(vrName).getStaticRoutes().get("ROUTE2");

    assertEquals(sr.getDestination(), Prefix.ZERO);
    assertEquals(sr.getNextVr(), "fakevr");
  }

  @Test
  public void testStaticRouteDefaults() {
    String hostname = "static-route-defaults";
    String vrName = "default";
    Configuration c = parseConfig(hostname);

    // Confirm static route shows up with correct defaults
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasAdministrativeCost(equalTo(10))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasMetric(equalTo(10L))))));
    assertThat(c, hasVrf(vrName, hasStaticRoutes(hasItem(hasPrefix(Prefix.strict("0.0.0.0/0"))))));
  }

  @Test
  public void testService() {
    String hostname = "service";
    Configuration c = parseConfig(hostname);

    String service1AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SERVICE1");
    String service2AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SERVICE2");
    String service3AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SERVICE3");
    String service4AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SERVICE 4");
    String serviceGroup1AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG1");
    String serviceGroup2AclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG 2");

    Flow service1Flow = createFlow(IpProtocol.TCP, 999, 1);
    Flow service2Flow = createFlow(IpProtocol.UDP, 4, 999);
    Flow service3Flow1 = createFlow(IpProtocol.UDP, 10, 5);
    Flow service3Flow2 = createFlow(IpProtocol.UDP, 9, 6);
    Flow service4Flow = createFlow(IpProtocol.SCTP, 1, 1);

    // Confirm services accept flows matching their definitions and reject others
    assertThat(c, hasIpAccessList(service1AclName, accepts(service1Flow, null, c)));
    assertThat(c, hasIpAccessList(service1AclName, rejects(service2Flow, null, c)));

    assertThat(c, hasIpAccessList(service2AclName, accepts(service2Flow, null, c)));
    assertThat(c, hasIpAccessList(service2AclName, accepts(service3Flow2, null, c)));
    assertThat(c, hasIpAccessList(service2AclName, rejects(service3Flow1, null, c)));

    assertThat(c, hasIpAccessList(service3AclName, accepts(service3Flow1, null, c)));
    assertThat(c, hasIpAccessList(service3AclName, accepts(service3Flow2, null, c)));
    assertThat(c, hasIpAccessList(service3AclName, rejects(service2Flow, null, c)));

    assertThat(c, hasIpAccessList(service4AclName, accepts(service4Flow, null, c)));
    assertThat(c, hasIpAccessList(service4AclName, rejects(service1Flow, null, c)));

    // Confirm service-group SG1 accepts flows matching its member definitions and rejects others
    assertThat(c, hasIpAccessList(serviceGroup1AclName, accepts(service1Flow, null, c)));
    assertThat(c, hasIpAccessList(serviceGroup1AclName, accepts(service2Flow, null, c)));
    assertThat(c, hasIpAccessList(serviceGroup1AclName, rejects(service3Flow1, null, c)));

    // Confirm SG2 accepts flows matching its member's member definitions and rejects others
    assertThat(c, hasIpAccessList(serviceGroup2AclName, accepts(service1Flow, null, c)));
    assertThat(c, hasIpAccessList(serviceGroup2AclName, accepts(service2Flow, null, c)));
    assertThat(c, hasIpAccessList(serviceGroup2AclName, rejects(service3Flow1, null, c)));

    // Verify transitive name.
    IpAccessList service1 = c.getIpAccessLists().get(service1AclName);
    IpAccessList sg1 = c.getIpAccessLists().get(serviceGroup1AclName);
    AclLine line1 = sg1.getLines().get(0);
    assertThat(line1.getName(), equalTo(service1.getSourceName()));
  }

  @Test
  public void testServiceReference() throws IOException {
    String hostname = "service";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String service1Name = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE1");
    String service2Name = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE2");
    String service3Name = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE3");
    String service4Name = computeObjectName(DEFAULT_VSYS_NAME, "SERVICE 4");
    String serviceGroup1Name = computeObjectName(DEFAULT_VSYS_NAME, "SG1");
    String serviceGroup2Name = computeObjectName(DEFAULT_VSYS_NAME, "SG 2");

    // Confirm structure definitions are tracked
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, service1Name));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, service2Name));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, service3Name));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, service4Name));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroup1Name));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroup2Name));

    // Confirm structure references are tracked
    assertThat(ccae, hasNumReferrers(filename, SERVICE, service1Name, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, service2Name, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, service3Name, 0));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, service4Name, 0));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_GROUP, serviceGroup1Name, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_GROUP, serviceGroup2Name, 0));

    // Confirm undefined reference shows up as such
    assertThat(
        ccae, hasUndefinedReference(filename, SERVICE_OR_SERVICE_GROUP, "SERVICE_UNDEFINED"));
  }

  @Test
  public void testServiceBuiltin() {
    String hostname = "service-built-in";
    Configuration c = parseConfig(hostname);

    String serviceGroupHttpsAclName =
        computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-HTTPS");
    String serviceGroupHttpAclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-HTTP");
    String serviceGroupAnyAclName = computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-ANY");

    Flow flowHttps = createFlow(IpProtocol.TCP, 999, 443);
    Flow flowHttp = createFlow(IpProtocol.TCP, 999, 80);
    Flow flowHttpAlt = createFlow(IpProtocol.TCP, 999, 8080);
    Flow flowOther = createFlow(IpProtocol.TCP, 999, 1);

    // Confirm HTTPS built-in accepts flows matching built-in HTTPS definition only
    assertThat(c, hasIpAccessList(serviceGroupHttpsAclName, accepts(flowHttps, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpsAclName, rejects(flowHttp, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpsAclName, rejects(flowHttpAlt, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpsAclName, rejects(flowOther, null, c)));

    // Confirm HTTP built-in accepts flows matching built-in HTTP definitions only
    assertThat(c, hasIpAccessList(serviceGroupHttpAclName, rejects(flowHttps, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpAclName, accepts(flowHttp, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpAclName, accepts(flowHttpAlt, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupHttpAclName, rejects(flowOther, null, c)));

    // Confirm any built-in accepts all flows
    assertThat(c, hasIpAccessList(serviceGroupAnyAclName, accepts(flowHttps, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupAnyAclName, accepts(flowHttp, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupAnyAclName, accepts(flowHttpAlt, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupAnyAclName, accepts(flowOther, null, c)));
  }

  @Test
  public void testServiceBuiltinReference() throws IOException {
    String hostname = "service-built-in";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String serviceHttp =
        computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.SERVICE_HTTP.getName());
    String serviceHttps =
        computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.SERVICE_HTTPS.getName());
    String serviceAny = computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.ANY.getName());

    String serviceGroupHttpName = computeObjectName(DEFAULT_VSYS_NAME, "SG-HTTP");
    String serviceGroupHttpsName = computeObjectName(DEFAULT_VSYS_NAME, "SG-HTTPS");
    String serviceGroupAnyName = computeObjectName(DEFAULT_VSYS_NAME, "SG-ANY");

    // Confirm structure definitions are tracked
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupHttpName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupHttpsName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupAnyName));

    // Confirm built-ins that are not overridden are not considered defined structures
    assertThat(ccae, not(hasDefinedStructure(filename, SERVICE, serviceHttp)));
    assertThat(ccae, not(hasDefinedStructure(filename, SERVICE, serviceHttps)));
    assertThat(ccae, not(hasDefinedStructure(filename, SERVICE, serviceAny)));

    // Confirm there are no undefined references for the built-ins
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, SERVICE_OR_SERVICE_GROUP, ServiceBuiltIn.ANY.getName())));
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, SERVICE_OR_SERVICE_GROUP, ServiceBuiltIn.SERVICE_HTTP.getName())));
    assertThat(
        ccae,
        not(
            hasUndefinedReference(
                filename, SERVICE_OR_SERVICE_GROUP, ServiceBuiltIn.SERVICE_HTTPS.getName())));
  }

  @Test
  public void testServiceBuiltinOverride() {
    String hostname = "service-built-in-override";
    Configuration c = parseConfig(hostname);

    String serviceGroupOverrideHttpsAclName =
        computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-OVERRIDE-HTTPS");
    String serviceGroupOverrideHttpAclName =
        computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-OVERRIDE-HTTP");
    String serviceGroupComboAclName =
        computeServiceGroupMemberAclName(DEFAULT_VSYS_NAME, "SG-COMBO");

    Flow flowHttp = createFlow(IpProtocol.TCP, 999, 80);
    Flow flowHttpOverride = createFlow(IpProtocol.TCP, 999, 1);
    Flow flowHttps = createFlow(IpProtocol.TCP, 999, 443);
    Flow flowHttpsOverride = createFlow(IpProtocol.TCP, 999, 2);
    Flow flowAny = createFlow(IpProtocol.UDP, 999, 999);
    Flow flowAnyOverride = createFlow(IpProtocol.TCP, 999, 3);

    // Confirm HTTPS service group accepts flows matching overridden HTTPS definition only
    assertThat(
        c, hasIpAccessList(serviceGroupOverrideHttpsAclName, accepts(flowHttpsOverride, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupOverrideHttpsAclName, rejects(flowHttps, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupOverrideHttpsAclName, rejects(flowHttp, null, c)));

    // Confirm HTTP service group accepts flows matching overridden HTTP definition only
    assertThat(
        c, hasIpAccessList(serviceGroupOverrideHttpAclName, accepts(flowHttpOverride, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupOverrideHttpAclName, rejects(flowHttp, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupOverrideHttpAclName, rejects(flowHttps, null, c)));

    // Confirm service group accepts flows matching overridden any definition only
    assertThat(c, hasIpAccessList(serviceGroupComboAclName, accepts(flowAnyOverride, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupComboAclName, rejects(flowAny, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupComboAclName, rejects(flowHttp, null, c)));
    assertThat(c, hasIpAccessList(serviceGroupComboAclName, rejects(flowHttps, null, c)));
  }

  @Test
  public void testServiceBuiltinOverrideReference() throws IOException {
    String hostname = "service-built-in-override";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String serviceAnyName = computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.ANY.getName());
    String serviceHttpName =
        computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.SERVICE_HTTP.getName());
    String serviceHttpsName =
        computeObjectName(DEFAULT_VSYS_NAME, ServiceBuiltIn.SERVICE_HTTPS.getName());
    String serviceGroupOverrideHttpName = computeObjectName(DEFAULT_VSYS_NAME, "SG-OVERRIDE-HTTP");
    String serviceGroupOverrideHttpsName =
        computeObjectName(DEFAULT_VSYS_NAME, "SG-OVERRIDE-HTTPS");
    String serviceGroupComboName = computeObjectName(DEFAULT_VSYS_NAME, "SG-COMBO");

    // Confirm structure definitions are tracked
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, serviceHttpName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, serviceHttpsName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, serviceAnyName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupOverrideHttpName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupOverrideHttpsName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE_GROUP, serviceGroupComboName));

    // Confirm structure references are tracked
    assertThat(ccae, hasNumReferrers(filename, SERVICE, serviceHttpName, 2));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, serviceHttpsName, 2));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, serviceAnyName, 1));
  }

  @Test
  public void testSharedGateway() {
    PaloAltoConfiguration c = parsePaloAltoConfig("shared-gateway");

    // Confirm shared-gateways show up in the vendor model
    Map<String, Vsys> sharedGateways = c.getSharedGateways();
    assertThat(sharedGateways, hasKey("sg1"));
    assertThat(sharedGateways, hasKey("sg2"));
    assertThat(sharedGateways, hasKey("sg3"));

    // Confirm display names show up as well
    assertThat(sharedGateways.get("sg1").getDisplayName(), equalTo("shared-gateway1"));
    assertThat(sharedGateways.get("sg2").getDisplayName(), equalTo("shared gateway2"));
    assertThat(sharedGateways.get("sg3").getDisplayName(), equalTo("invalid shared gateway"));
  }

  @Test
  public void testSharedGatewayReference() throws IOException {
    String hostname = "shared-gateway";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm structure definitions are recorded correctly, even for badly named shared-gateway
    assertThat(ccae, hasDefinedStructure(filename, SHARED_GATEWAY, "sg1"));
    assertThat(ccae, hasDefinedStructure(filename, SHARED_GATEWAY, "sg2"));
    assertThat(ccae, hasDefinedStructure(filename, SHARED_GATEWAY, "sg3"));

    // Confirm structure references are counted correctly
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/1", 2));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/2", 1));

    // Confirm the undefined interface is properly detected as an undefined reference
    assertThat(ccae, hasUndefinedReference(filename, INTERFACE, "ethernet1/3", IMPORT_INTERFACE));
  }

  @Test
  public void testVirtualRouterExtraction() {
    PaloAltoConfiguration c = parseNestedConfig("virtual-router");
    VirtualRouter vr = c.getVirtualRouters().get("VR");
    assertThat(vr, notNullValue());
    AdminDistances ads = vr.getAdminDists();
    assertThat(ads.getEbgp(), equalTo(21));
    assertThat(ads.getIbgp(), equalTo(201));
    assertThat(ads.getOspfExt(), equalTo(111));
    assertThat(ads.getOspfInt(), equalTo(31));
    assertThat(ads.getOspfV3Ext(), equalTo(112));
    assertThat(ads.getOspfV3Int(), equalTo(32));
    assertThat(ads.getRip(), equalTo(121));
    assertThat(ads.getStatic(), equalTo(11));
    assertThat(ads.getStaticv6(), equalTo(12));
  }

  @Test
  public void testVirtualRouterConversion() {
    Configuration c = parseConfig("virtual-router");
    assertThat(c, hasVrf("VR", any(Vrf.class)));
    // TODO: proper conversion test.
  }

  @Test
  public void testVirtualRouterInterfaces() {
    String hostname = "virtual-router-interfaces";
    Configuration c = parseConfig(hostname);

    assertThat(c.getAllInterfaces("default").keySet(), hasItem("ethernet1/1"));
    assertThat(c.getAllInterfaces("somename").keySet(), hasItems("ethernet1/2", "ethernet1/3"));
    assertThat(c.getAllInterfaces("some other name").keySet(), empty());
  }

  @Test
  public void testVsysImport() {
    PaloAltoConfiguration c = parsePaloAltoConfig("vsys-import");

    // Confirm vsys imports are extracted correctly
    assertThat(c.getVirtualSystems().get("vsys1").getImportedVsyses(), emptyIterable());
    assertThat(c.getVirtualSystems().get("vsys2").getImportedVsyses(), contains("vsys1"));
  }

  @Test
  public void testVsysService() {
    String hostname = "vsys-service";
    Configuration c = parseConfig(hostname);

    String vsys1ServiceName = computeServiceGroupMemberAclName("vsys1", "SERVICE1");
    String vsys2ServiceName = computeServiceGroupMemberAclName("vsys2", "SERVICE1");
    String sharedServiceName = computeServiceGroupMemberAclName(SHARED_VSYS_NAME, "SERVICE1");
    String sharedOnlyServiceName = computeServiceGroupMemberAclName(SHARED_VSYS_NAME, "SERVICE2");

    String vsys1ServiceGroupName = computeServiceGroupMemberAclName("vsys1", "SG1");
    String vsys2ServiceGroupName = computeServiceGroupMemberAclName("vsys2", "SG1");
    String sharedServiceGroupName = computeServiceGroupMemberAclName(SHARED_VSYS_NAME, "SG1");

    Flow vsys1Flow = createFlow(IpProtocol.UDP, 999, 1);
    Flow vsys2Flow = createFlow(IpProtocol.UDP, 999, 2);
    Flow sharedFlow = createFlow(IpProtocol.UDP, 999, 3);
    Flow sharedOnlyFlow = createFlow(IpProtocol.UDP, 999, 4);

    // Confirm services accept flows matching their definitions and reject others
    assertThat(c, hasIpAccessList(vsys1ServiceName, accepts(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys1ServiceName, rejects(vsys2Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys2ServiceName, accepts(vsys2Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys2ServiceName, rejects(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(sharedServiceName, accepts(sharedFlow, null, c)));
    assertThat(c, hasIpAccessList(sharedServiceName, rejects(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(sharedOnlyServiceName, accepts(sharedOnlyFlow, null, c)));
    assertThat(c, hasIpAccessList(sharedOnlyServiceName, rejects(vsys1Flow, null, c)));

    // Confirm group in vsys1 namespace references its own service, not shared or neighbor services
    assertThat(c, hasIpAccessList(vsys1ServiceGroupName, accepts(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys1ServiceGroupName, rejects(vsys2Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys1ServiceGroupName, rejects(sharedFlow, null, c)));
    // Confirm group in the vsys1 namespace correctly references the shared service
    assertThat(c, hasIpAccessList(vsys1ServiceGroupName, accepts(sharedOnlyFlow, null, c)));

    // Confirm group in vsys2 namespace references its own service, not shared or neighbor services
    assertThat(c, hasIpAccessList(vsys2ServiceGroupName, accepts(vsys2Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys2ServiceGroupName, rejects(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(vsys2ServiceGroupName, rejects(sharedFlow, null, c)));
    // Confirm group in the vsys2 namespace correctly references the shared service
    assertThat(c, hasIpAccessList(vsys2ServiceGroupName, accepts(sharedOnlyFlow, null, c)));

    // Confirm group in shared namespace references its own service, not vsys specific services
    assertThat(c, hasIpAccessList(sharedServiceGroupName, accepts(sharedFlow, null, c)));
    assertThat(c, hasIpAccessList(sharedServiceGroupName, rejects(vsys1Flow, null, c)));
    assertThat(c, hasIpAccessList(sharedServiceGroupName, rejects(vsys2Flow, null, c)));
    // Confirm group in the shared namespace correctly references the shared service
    assertThat(c, hasIpAccessList(sharedServiceGroupName, accepts(sharedOnlyFlow, null, c)));
  }

  @Test
  public void testVsysServiceReference() throws IOException {
    String hostname = "vsys-service";
    String filename = "configs/" + hostname;
    String vsys1ServiceName = computeObjectName("vsys1", "SERVICE1");
    String vsys2ServiceName = computeObjectName("vsys2", "SERVICE1");
    String sharedServiceName = computeObjectName(SHARED_VSYS_NAME, "SERVICE1");
    String sharedOnlyServiceName = computeObjectName(SHARED_VSYS_NAME, "SERVICE2");

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm structure definitions are tracked separately for each vsys
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, vsys1ServiceName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, vsys2ServiceName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, sharedServiceName));
    assertThat(ccae, hasDefinedStructure(filename, SERVICE, sharedOnlyServiceName));

    // Confirm structure references are tracked separately for each vsys
    assertThat(ccae, hasNumReferrers(filename, SERVICE, vsys1ServiceName, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, vsys2ServiceName, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, sharedServiceName, 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE, sharedOnlyServiceName, 3));
  }

  @Test
  public void testVsysZones() throws IOException {
    String hostname = "vsys-zones";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String zoneName = computeObjectName("vsys1", "z1");
    String zoneEmptyName = computeObjectName("vsys11", "z1");

    // Confirm zone definitions are recorded properly
    assertThat(ccae, hasDefinedStructure(filename, ZONE, zoneName));
    assertThat(ccae, hasDefinedStructure(filename, ZONE, zoneEmptyName));

    // Confirm interface references in zones are recorded properly
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/1", 1));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/2", 1));

    // Confirm zones contain the correct interfaces
    assertThat(
        c,
        hasZone(
            zoneName,
            hasMemberInterfaces(
                containsInAnyOrder("ethernet1/1", "ethernet1/2", "ethernet1/3.1"))));
    assertThat(c, hasZone(zoneEmptyName, hasMemberInterfaces(empty())));

    // Confirm interfaces have the correct zones
    assertThat(
        c,
        allOf(
            hasInterface("ethernet1/1", hasZoneName("z1")),
            hasInterface("ethernet1/2", hasZoneName("z1")),
            hasInterface("ethernet1/3.1", hasZoneName("z1")),
            hasInterface("ethernet1/3", hasZoneName(nullValue()))));
  }

  @Test
  public void testZoneExternal() {
    PaloAltoConfiguration c = parsePaloAltoConfig("zone-external");

    // Make sure the vsys and zones we're going to check exist
    assertThat(c.getVirtualSystems(), hasKey("vsys3"));
    SortedMap<String, Zone> zones = c.getVirtualSystems().get("vsys3").getZones();
    assertThat(zones.keySet(), containsInAnyOrder("ZONE1", "ZONE2"));

    // Make sure the external zones are correctly populated with the specified external names
    Zone z1 = zones.get("ZONE1");
    assertThat(z1.getType(), equalTo(EXTERNAL));
    assertThat(z1.getExternalNames(), containsInAnyOrder("vsys1", "sg1"));

    Zone z2 = zones.get("ZONE2");
    assertThat(z2.getType(), equalTo(EXTERNAL));
    assertThat(z2.getExternalNames(), contains("vsys2"));
  }

  @Test
  public void testZones() throws IOException {
    String hostname = "zones";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String z1Name = computeObjectName(DEFAULT_VSYS_NAME, "zone 1");
    String zEmptyName = computeObjectName(DEFAULT_VSYS_NAME, "zempty");

    // Confirm zone definitions are recorded properly
    assertThat(ccae, hasDefinedStructure(filename, ZONE, z1Name));
    assertThat(ccae, hasDefinedStructure(filename, ZONE, zEmptyName));

    // Confirm interface references in zones are recorded properly
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/1", 1));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "ethernet1/2", 1));

    // Confirm zone containing interfaces is referenced
    assertThat(ccae, hasNumReferrers(filename, ZONE, z1Name, 2));

    // Confirm zone not containing interfaces is not referenced
    assertThat(ccae, hasNumReferrers(filename, ZONE, zEmptyName, 0));

    // Confirm zones contain the correct interfaces
    assertThat(
        c, hasZone(z1Name, hasMemberInterfaces(containsInAnyOrder("ethernet1/1", "ethernet1/2"))));
    assertThat(c, hasZone(zEmptyName, hasMemberInterfaces(empty())));

    // Confirm interfaces are associated with the correct zones
    assertThat(c, hasInterface("ethernet1/1", hasZoneName(equalTo("zone 1"))));
    assertThat(c, hasInterface("ethernet1/2", hasZoneName(equalTo("zone 1"))));
    assertThat(c, hasInterface("ethernet1/3", hasZoneName(is(nullValue()))));
  }

  @Test
  public void testQuotedValues() {
    String hostname = "quoted-values";

    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);
    SortedMap<String, AddressObject> addrs = c.getVirtualSystems().get("vsys1").getAddressObjects();
    assertThat(
        addrs,
        allOf(
            hasKey("addr0"),
            hasKey("addr1"),
            hasKey("addr2"),
            hasKey("addr3"),
            hasKey("addr4"),
            hasKey("addr5"),
            hasKey("addr6"),
            hasKey("addr7")));

    String descr0 = addrs.get("addr0").getDescription();
    String descr1 = addrs.get("addr1").getDescription();
    String descr2 = addrs.get("addr2").getDescription();
    String descr3 = addrs.get("addr3").getDescription();
    String descr4 = addrs.get("addr4").getDescription();
    String descr5 = addrs.get("addr5").getDescription();
    String descr6 = addrs.get("addr6").getDescription();
    String descr7 = addrs.get("addr7").getDescription();

    // Quoted values containing quotes should be extracted
    assertThat(descr0, equalTo("quoted description with a '"));
    assertThat(descr1, equalTo("quoted description with a \""));
    assertThat(descr2, equalTo("quoted description with a \" and '"));
    assertThat(descr3, equalTo("multiline description with \" inside'\nand other stuff"));

    // Quoted and non-quoted values should be extracted correctly
    assertThat(descr4, equalTo("shortdescription"));
    assertThat(descr5, equalTo("quoted description"));
    // Missing value and empty quotes should be interpreted as empty string
    assertThat(descr6, equalTo(""));
    assertThat(descr7, equalTo(""));
  }

  @Test
  public void testApplicationGroup() {
    String hostname = "application-group";
    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);
    assertThat(
        c.getVirtualSystems().get(DEFAULT_VSYS_NAME).getApplicationGroups().get("foo").getMembers(),
        containsInAnyOrder("dns", "app1"));

    assertThat(
        c.getVirtualSystems()
            .get(DEFAULT_VSYS_NAME)
            .getApplicationGroups()
            .get("foo w spaces")
            .getMembers(),
        containsInAnyOrder("dns", "app w spaces"));
  }

  @Test
  public void testApplicationGroupReference() throws IOException {
    String hostname = "application-group";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference count is correct for applications
    assertThat(
        ccae,
        hasNumReferrers(
            filename,
            PaloAltoStructureType.APPLICATION,
            computeObjectName(DEFAULT_VSYS_NAME, "app1"),
            1));
  }

  @Test
  public void testDeviceGroup() {
    String hostname = "device-group";
    String firewallId1 = "00000001";
    String firewallId2 = "00000002";
    String firewallId3 = "00000003";
    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);

    DeviceGroup deviceGroup1 = c.getDeviceGroup("DG1");
    assertThat(deviceGroup1, notNullValue());
    Vsys panoramaDg1 = deviceGroup1.getPanorama();
    DeviceGroup deviceGroup2 = c.getDeviceGroup("DG2");
    assertThat(deviceGroup2, notNullValue());
    Vsys panoramaDg2 = deviceGroup2.getPanorama();

    // Check first device-group
    assertThat(deviceGroup1.getDevices(), containsInAnyOrder(firewallId1, firewallId2));
    assertThat(deviceGroup1.getDescription(), equalTo("long description"));
    assertThat(deviceGroup1.getParentDg(), nullValue());
    assertThat(panoramaDg1, notNullValue());
    assertThat(panoramaDg1.getPostRulebase().getSecurityRules().keySet(), contains("RULE1"));
    assertThat(panoramaDg1.getAddressObjects().keySet(), contains("ADDR1"));

    // Check second device-group
    assertThat(deviceGroup2.getDevices(), contains(firewallId3));
    assertThat(deviceGroup2.getParentDg(), equalTo("DG1"));
    assertThat(panoramaDg2, notNullValue());
    assertThat(panoramaDg2.getAddressObjects().keySet(), contains("ADDR2"));

    // Make sure post-device-group config is attached to the main config as expected
    assertThat(c.getHostname(), equalTo("device-group"));
  }

  @Test
  public void testFirewallDeviceModel() {
    String hostname = "basic-parsing";
    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Single firewall should be recognized as such, not Panorama management device
    assertThat(viConfigs, iterableWithSize(1));
    assertThat(viConfigs.get(0).getDeviceModel(), equalTo(DeviceModel.PALO_ALTO_FIREWALL));
  }

  @Test
  public void testPanoramaDeviceModel() {
    String panoramaHostname = "device-group";
    String firewallId1 = "00000001";
    String firewallId2 = "00000002";
    String firewallId3 = "00000003";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get four nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1, firewallId2, firewallId3));
    Configuration panorama =
        viConfigs.stream()
            .filter(vi -> vi.getHostname().equals(panoramaHostname))
            .findFirst()
            .get();
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();
    Configuration firewall2 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId2)).findFirst().get();
    Configuration firewall3 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId3)).findFirst().get();

    // Management device should be detected as Panorama
    assertThat(panorama.getDeviceModel(), equalTo(DeviceModel.PALO_ALTO_PANORAMA));
    // Other devices should be detected as firewalls
    assertThat(firewall1.getDeviceModel(), equalTo(DeviceModel.PALO_ALTO_FIREWALL));
    assertThat(firewall2.getDeviceModel(), equalTo(DeviceModel.PALO_ALTO_FIREWALL));
    assertThat(firewall3.getDeviceModel(), equalTo(DeviceModel.PALO_ALTO_FIREWALL));
  }

  @Test
  public void testDeviceGroupConversion() {
    String panoramaHostname = "device-group";
    String firewallId1 = "00000001";
    String firewallId2 = "00000002";
    String firewallId3 = "00000003";
    String addressObject1Name = "ADDR1";
    String addressObject2Name = "ADDR2";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get four nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1, firewallId2, firewallId3));
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();
    Configuration firewall2 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId2)).findFirst().get();
    Configuration firewall3 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId3)).findFirst().get();

    // First two firewalls should inherit definitions from first Panorama device-group
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(addressObject1Name), Ip.parse("1.2.3.4").toIpSpace());
    assertIpSpacesEqual(
        firewall2.getIpSpaces().get(addressObject1Name), Ip.parse("1.2.3.4").toIpSpace());
    // Third firewall should inherit definitions from second Panorama device-group
    assertIpSpacesEqual(
        firewall3.getIpSpaces().get(addressObject2Name), Ip.parse("2.3.4.5").toIpSpace());
  }

  @Test
  public void testPanoramaManagedDeviceFilename() {
    String panoramaHostname = "panorama-managed-device-hostname";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<PaloAltoConfiguration> managedDevices = c.getManagedConfigurations();

    assertThat(c.getFilename(), notNullValue());
    assertThat(managedDevices, not(empty()));
    for (PaloAltoConfiguration device : managedDevices) {
      assertThat(device.getFilename(), equalTo(c.getFilename()));
    }
  }

  @Test
  public void testPanoramaManagedDeviceHostname() {
    String panoramaHostname = "panorama-managed-device-hostname";
    String firewallId1 = "firewall-1";
    String firewallId2 = "firewall-2";
    String firewallId3 = "00000003";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get four nodes from the one Panorama config with the correct hostnames
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1, firewallId2, firewallId3));
  }

  @Test
  public void testPanoramaManagedDeviceHostnameWarning() throws IOException {
    String hostname = "panorama-managed-device-hostname";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Should have a warning about trying to set hostname for an unknown device id
    assertThat(ccae.getWarnings().keySet(), hasItem(equalTo(hostname)));
    Warnings warn = ccae.getWarnings().get(hostname);
    assertThat(
        warn.getRedFlagWarnings().stream().map(Warning::getText).collect(Collectors.toSet()),
        contains("Cannot set hostname for unknown device id 00000004."));
  }

  @Test
  public void testTemplate() {
    String hostname = "template";
    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);

    Template t1 = c.getOrCreateTemplate("T1");
    Template t2 = c.getOrCreateTemplate("T2");
    assertThat(t1.getDescription(), equalTo("template description"));
    assertThat(t2.getDescription(), equalTo("template 2 description"));
    assertThat(t1.getHostname(), equalTo("pan-template"));

    // Check network configuration is applied to the template as expected
    assertThat(t1.getInterfaces().keySet(), containsInAnyOrder("ethernet1/1", "ethernet1/2"));
    assertThat(t1.getVirtualRouters().keySet(), contains("default"));
    assertThat(
        t1.getVirtualRouters().get("default").getInterfaceNames(),
        containsInAnyOrder("ethernet1/1", "ethernet1/2"));

    // Check vsys configuration is applied to the template as expected
    assertThat(t1.getVirtualSystems().keySet(), contains("vsys1"));
    Vsys vsys1 = t1.getVirtualSystems().get("vsys1");
    Vsys shared = t1.getShared();
    assertThat(vsys1.getImportedInterfaces(), containsInAnyOrder("ethernet1/1", "ethernet1/2"));
    SortedMap<String, Zone> zones = vsys1.getZones();
    assertThat(zones.keySet(), containsInAnyOrder("ZONE1", "ZONE2"));
    assertThat(zones.get("ZONE1").getInterfaceNames(), contains("ethernet1/1"));
    assertThat(zones.get("ZONE2").getInterfaceNames(), contains("ethernet1/2"));
    assertThat(shared, notNullValue());
    assertThat(shared.getSyslogServer("G1", "S1").getAddress(), equalTo("10.0.0.123"));
  }

  @Test
  public void testTemplateStack() {
    String hostname = "template-stack";
    PaloAltoConfiguration c = parsePaloAltoConfig(hostname);

    TemplateStack ts1 = c.getOrCreateTemplateStack("TS1");
    TemplateStack ts2 = c.getOrCreateTemplateStack("TS2");
    TemplateStack ts3 = c.getOrCreateTemplateStack("TS3");

    assertThat(ts1.getDescription(), equalTo("ts 1 description"));
    assertThat(ts2.getDescription(), equalTo("ts 2 description"));
    assertThat(ts3.getDescription(), equalTo("ts 3 description"));

    // Check devices associated w/ each stack
    assertThat(ts1.getDevices(), containsInAnyOrder("00000001", "00000002"));
    assertThat(ts2.getDevices(), emptyIterable());
    assertThat(ts3.getDevices(), contains("00000003"));

    // Finally, check templates associated w/ each stack; order is important
    assertThat(ts1.getTemplates(), contains("T1", "T2", "T_UNDEF"));
    assertThat(ts2.getTemplates(), contains("T3"));
    assertThat(ts3.getTemplates(), emptyIterable());
  }

  /**
   * Test that inheriting from multiple templates merges and overwrites configuration as expected.
   */
  @Test
  public void testTemplateStackOverwriteConversion() {
    String panoramaHostname = "template-stack-overwrite";
    String firewallHostname = "hostname_one";
    String zone1Name = computeObjectName("vsys1", "Z1");
    String zone2Name = computeObjectName("vsys1", "Z2");
    String zone3Name = computeObjectName("vsys1", "Z3");
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get two nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallHostname));
    Configuration firewall1 =
        viConfigs.stream()
            .filter(vi -> vi.getHostname().equals(firewallHostname))
            .findFirst()
            .get();

    // Only SG1 servers and shared servers from template 1 should make it into VI model
    assertThat(
        firewall1.getLoggingServers(),
        contains("10.1.21.1", "10.1.23.1", "10.1.31.1", "10.1.33.1"));
    // Primary dns / ntp servers should be overwritten by T1, but secondary should come from T2
    assertThat(firewall1.getDnsServers(), contains("10.1.51.1", "10.2.52.1"));
    assertThat(firewall1.getNtpServers(), contains("10.1.61.1", "10.2.62.1"));
    // eth1/2 address should be from template 2, others should be from template 1
    assertThat(firewall1, hasInterface("ethernet1/1", hasAddress("10.1.41.1/30")));
    assertThat(firewall1, hasInterface("ethernet1/2", hasAddress("10.2.42.1/30")));
    assertThat(firewall1, hasInterface("ethernet1/3", hasAddress("10.1.43.1/30")));
    // Both virtual-routers should make it into VI model
    assertThat(firewall1.getVrfs().keySet(), containsInAnyOrder("default", "default2"));
    // All zones should make it into VI model
    assertThat(firewall1, hasZone(zone1Name, hasMemberInterfaces(contains("ethernet1/1"))));
    assertThat(firewall1, hasZone(zone2Name, hasMemberInterfaces(contains("ethernet1/2"))));
    assertThat(firewall1, hasZone(zone3Name, hasMemberInterfaces(contains("ethernet1/3"))));
  }

  @Test
  public void testTemplateReferences() throws IOException {
    String hostname = "template-stack";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(ccae, hasDefinedStructureWithDefinitionLines(filename, TEMPLATE, "T1", contains(1)));
    assertThat(ccae, hasDefinedStructureWithDefinitionLines(filename, TEMPLATE, "T2", contains(2)));
    assertThat(ccae, hasDefinedStructureWithDefinitionLines(filename, TEMPLATE, "T3", contains(3)));

    assertThat(
        ccae, hasUndefinedReference(filename, TEMPLATE, "T_UNDEF", TEMPLATE_STACK_TEMPLATES));
    assertThat(ccae, hasNumReferrers(filename, TEMPLATE, "T1", 1));
    assertThat(ccae, hasNumReferrers(filename, TEMPLATE, "T2", 1));
    assertThat(ccae, hasNumReferrers(filename, TEMPLATE, "T3", 1));
  }

  @Test
  public void testDeviceGroupSharedInerhitance() {
    String panoramaHostname = "device-group-shared-inheritance";
    String firewallId1 = "00000001";
    String addressObject1Name = "ADDR1";
    String addressObject2Name = "ADDR2-override";
    String addressObject3Name = "ADDR3-shared";

    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get two nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1));
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();

    // Address only defined in device-group or shared namespace should directly end up in VI config
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(addressObject1Name), Ip.parse("192.168.1.1").toIpSpace());
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(addressObject3Name), Ip.parse("192.168.2.3").toIpSpace());
    // Address defined in both device-group and shared namespace should have definition from
    // device-group overwrite shared object definition
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(addressObject2Name), Ip.parse("192.168.1.2").toIpSpace());
  }

  @Test
  public void testDeviceGroupInerhitance() {
    String panoramaHostname = "device-group-inheritance";
    String firewallId1 = "00000001";
    String firewallId2 = "00000002";
    String commonAddrObjName = "COMMON_ADDR";
    String parentAddrObjName = "PARENT_ADDR_1";
    String childAddrObjName = "CHILD_ADDR_1";

    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get three nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1, firewallId2));
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();
    Configuration firewall2 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId2)).findFirst().get();

    // firewall1 based on parent DG should have the non-overridden common addr value
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(commonAddrObjName), Ip.parse("10.10.2.20").toIpSpace());
    // firewall2 based on child DG should have the overridden common addr value
    assertIpSpacesEqual(
        firewall2.getIpSpaces().get(commonAddrObjName), Ip.parse("10.10.3.30").toIpSpace());

    // both firewalls should have the parent address object
    assertIpSpacesEqual(
        firewall1.getIpSpaces().get(parentAddrObjName), Ip.parse("10.10.2.21").toIpSpace());
    assertIpSpacesEqual(
        firewall2.getIpSpaces().get(parentAddrObjName), Ip.parse("10.10.2.21").toIpSpace());

    // only firewall2 should have the child address object
    assertThat(firewall1.getIpSpaces().keySet(), not(contains(childAddrObjName)));
    assertIpSpacesEqual(
        firewall2.getIpSpaces().get(childAddrObjName), Ip.parse("10.10.3.31").toIpSpace());
  }

  @Test
  public void testPanoramaWarning() throws IOException {
    String panoramaHostname = "panorama-warning";
    String firewallId = "00000001";

    Batfish batfish = getBatfishForConfigurationNames(panoramaHostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Should have a warning about an unknown application associated with the firewall
    assertThat(ccae.getWarnings().keySet(), hasItem(equalTo(firewallId)));
    Warnings warn = ccae.getWarnings().get(firewallId);
    assertThat(
        warn.getRedFlagWarnings().stream().map(Warning::getText).collect(Collectors.toSet()),
        contains("Unable to identify application undefined_app in vsys panorama rule RULE1"));
  }

  @Test
  public void testDeviceGroupParentWarning() throws IOException {
    String panoramaHostname = "device-group-invalid";
    String firewallId1 = "00000001";

    Batfish batfish = getBatfishForConfigurationNames(panoramaHostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Detect cyclical device-group inheritance and unknown device-group inheritance
    assertThat(ccae.getWarnings().keySet(), hasItem(equalTo(firewallId1)));
    Warnings warn1 = ccae.getWarnings().get(firewallId1);
    assertThat(
        warn1.getRedFlagWarnings().stream().map(Warning::getText).collect(Collectors.toSet()),
        containsInAnyOrder(
            "Device-group DG1 cannot be inherited more than once.",
            "Device-group DG2 cannot inherit from unknown device-group DG_INVALID."));
  }

  @Test
  public void testPanoramaConfigurationFormat() {
    String panoramaHostname = "device-group";
    String firewallId1 = "00000001";
    String firewallId2 = "00000002";
    String firewallId3 = "00000003";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get four nodes from the one Panorama config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1, firewallId2, firewallId3));
    Configuration panorama =
        viConfigs.stream()
            .filter(vi -> vi.getHostname().equals(panoramaHostname))
            .findFirst()
            .get();
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();
    Configuration firewall2 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId2)).findFirst().get();
    Configuration firewall3 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId3)).findFirst().get();

    // All should be the same configuration format
    assertThat(panorama.getConfigurationFormat(), equalTo(ConfigurationFormat.PALO_ALTO));
    assertThat(firewall1.getConfigurationFormat(), equalTo(ConfigurationFormat.PALO_ALTO));
    assertThat(firewall2.getConfigurationFormat(), equalTo(ConfigurationFormat.PALO_ALTO));
    assertThat(firewall3.getConfigurationFormat(), equalTo(ConfigurationFormat.PALO_ALTO));
  }

  @Test
  public void testDeviceGroupAttachVsys() {
    String panoramaHostname = "device-group-attach-vsys";
    String firewallId1 = "00000001";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);

    DeviceGroup deviceGroup1 = c.getOrCreateDeviceGroup("DG1");

    // Device-group should be associated with specific firewall vsys
    assertThat(deviceGroup1.getVsys().keySet(), contains(firewallId1));
    assertThat(deviceGroup1.getVsys().get(firewallId1), contains("vsys1"));
  }

  @Test
  public void testDeviceGroupAttachVsysConversion() {
    String panoramaHostname = "device-group-attach-vsys";
    String firewallId1 = "00000001";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get the panorama and firewall VI configs from the single panorama VS config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1));
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();
    assertThat(firewall1.getIpSpaces().keySet(), containsInAnyOrder("ADDR1"));
  }

  @Test
  public void testDeviceGroupAttachMultiVsys() {
    String panoramaHostname = "device-group-attach-multi-vsys";
    String firewallId1 = "00000001";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);

    DeviceGroup deviceGroup1 = c.getOrCreateDeviceGroup("DG1");
    DeviceGroup deviceGroup2 = c.getOrCreateDeviceGroup("DG2");

    // Device-group should be associated with only first firewall vsys
    assertThat(deviceGroup1.getVsys().keySet(), contains(firewallId1));
    assertThat(deviceGroup1.getVsys().get(firewallId1), contains("vsys1"));
    // Device-group should be associated with only second firewall vsys
    assertThat(deviceGroup2.getVsys().keySet(), contains(firewallId1));
    assertThat(deviceGroup2.getVsys().get(firewallId1), contains("vsys2"));
  }

  @Ignore("https://github.com/batfish/batfish/issues/5910")
  @Test
  public void testDeviceGroupAttachMultiVsysConversion() {
    String panoramaHostname = "device-group-attach-multi-vsys";
    String firewallId1 = "00000001";
    String addrVsys1 = computeObjectName("vsys1", "ADDR1");
    String addrVsys2 = computeObjectName("vsys2", "ADDR1");
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();

    // Should get the panorama and firewall VI configs from the single panorama VS config
    assertThat(
        viConfigs.stream().map(Configuration::getHostname).collect(Collectors.toList()),
        containsInAnyOrder(panoramaHostname, firewallId1));
    Configuration firewall1 =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId1)).findFirst().get();

    Map<String, IpSpace> ipSpaces = firewall1.getIpSpaces();
    assertThat(ipSpaces.keySet(), containsInAnyOrder(addrVsys1, addrVsys2));
    // Each vsys should inherit a different definition for the same address object
    assertIpSpacesEqual(ipSpaces.get(addrVsys1), Ip.parse("1.1.1.1").toIpSpace());
    assertIpSpacesEqual(ipSpaces.get(addrVsys2), Ip.parse("2.2.2.2").toIpSpace());
  }

  @Test
  public void testInterfaceRuntimeAddressConversion() throws IOException {
    String snapshotName = "runtime_data";
    String hostname = "panorama";
    String firewall1 = "firewall1";
    String firewall2 = "firewall2";
    String eth1_1 = "ethernet1/1";
    String eth1_2 = "ethernet1/2";
    String eth1_3 = "ethernet1/3";
    String eth_lo = "loopback";
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOTS_PREFIX + snapshotName, ImmutableSet.of(hostname))
                .setRuntimeDataPrefix(SNAPSHOTS_PREFIX + snapshotName)
                .build(),
            _folder);

    Configuration c1 = batfish.loadConfigurations(batfish.getSnapshot()).get(firewall1);
    Configuration c2 = batfish.loadConfigurations(batfish.getSnapshot()).get(firewall2);
    Map<String, org.batfish.datamodel.Interface> interfaces1 = c1.getAllInterfaces();
    Map<String, org.batfish.datamodel.Interface> interfaces2 = c2.getAllInterfaces();

    assertThat(interfaces1.keySet(), containsInAnyOrder(eth1_1, eth1_2, eth1_3));
    assertThat(interfaces2.keySet(), containsInAnyOrder(eth1_1, eth1_2, eth1_3, eth_lo));

    // Should use configured interface address from config where applicable
    assertThat(
        interfaces1.get(eth1_1).getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("10.1.1.1/30")));
    assertThat(
        interfaces1.get(eth1_2).getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("10.1.2.1/30")));
    // No address specified for this interface on firewall1
    assertThat(interfaces1.get(eth1_3).getConcreteAddress(), nullValue());

    // Use configured interface address from config where applicable
    assertThat(
        interfaces2.get(eth1_1).getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("10.2.1.1/30")));
    // For interfaces without configured address, address should be pulled from runtime data
    assertThat(
        interfaces2.get(eth1_2).getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.168.2.1/24")));
    assertThat(
        interfaces2.get(eth1_3).getConcreteAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.168.3.1/24")));
    // Non /32 address should not be associated with loopback
    assertThat(interfaces2.get(eth_lo).getConcreteAddress(), nullValue());
  }
}
