package org.batfish.grammar.cisco_asa;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.AuthenticationMethod.GROUP_USER_DEFINED;
import static org.batfish.datamodel.AuthenticationMethod.LOCAL_CASE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.TraceElements.matchedByAclLine;
import static org.batfish.datamodel.matchers.AaaAuthenticationLoginListMatchers.hasMethod;
import static org.batfish.datamodel.matchers.AaaAuthenticationLoginMatchers.hasListForKey;
import static org.batfish.datamodel.matchers.AaaAuthenticationMatchers.hasLogin;
import static org.batfish.datamodel.matchers.AaaMatchers.hasAuthentication;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.hasConjuncts;
import static org.batfish.datamodel.matchers.AndMatchExprMatchers.isAndMatchExprThat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpSpace;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVendorFamily;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasAclName;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasMemberInterfaces;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasName;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasPostTransformationIncomingFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasPreTransformationOutgoingFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasZone;
import static org.batfish.datamodel.matchers.DataModelMatchers.isIpSpaceReferenceThat;
import static org.batfish.datamodel.matchers.DataModelMatchers.isPermittedByAclThat;
import static org.batfish.datamodel.matchers.ExprAclLineMatchers.hasMatchCondition;
import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasDstIps;
import static org.batfish.datamodel.matchers.HeaderSpaceMatchers.hasSrcIps;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasEigrp;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasEncapsulationVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasOspfAreaName;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.hasLines;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.LineMatchers.hasAuthenticationLoginList;
import static org.batfish.datamodel.matchers.LineMatchers.requiresAuthentication;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.hasHeaderSpace;
import static org.batfish.datamodel.matchers.MatchHeaderSpaceMatchers.isMatchHeaderSpaceThat;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.shiftSourceIp;
import static org.batfish.datamodel.vendor_family.VendorFamilyMatchers.hasCisco;
import static org.batfish.datamodel.vendor_family.cisco.CiscoFamilyMatchers.hasAaa;
import static org.batfish.grammar.cisco_asa.AsaControlPlaneExtractor.SERIAL_LINE;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.cisco_asa.AsaConfiguration.DENY_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT;
import static org.batfish.representation.cisco_asa.AsaConfiguration.DENY_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT;
import static org.batfish.representation.cisco_asa.AsaConfiguration.PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT;
import static org.batfish.representation.cisco_asa.AsaConfiguration.PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT;
import static org.batfish.representation.cisco_asa.AsaConfiguration.PERMIT_TRAFFIC_FROM_DEVICE;
import static org.batfish.representation.cisco_asa.AsaConfiguration.asaDeniedByOutputFilterTraceElement;
import static org.batfish.representation.cisco_asa.AsaConfiguration.asaPermitHigherSecurityLevelTrafficTraceElement;
import static org.batfish.representation.cisco_asa.AsaConfiguration.asaPermitLowerSecurityLevelTraceElement;
import static org.batfish.representation.cisco_asa.AsaConfiguration.asaPermittedByOutputFilterTraceElement;
import static org.batfish.representation.cisco_asa.AsaConfiguration.asaRejectLowerSecurityLevelTraceElement;
import static org.batfish.representation.cisco_asa.AsaConfiguration.computeASASecurityLevelZoneName;
import static org.batfish.representation.cisco_asa.AsaConfiguration.computeIcmpObjectGroupAclName;
import static org.batfish.representation.cisco_asa.AsaConfiguration.computeProtocolObjectGroupAclName;
import static org.batfish.representation.cisco_asa.AsaConfiguration.computeServiceObjectAclName;
import static org.batfish.representation.cisco_asa.AsaConfiguration.computeServiceObjectGroupAclName;
import static org.batfish.representation.cisco_asa.AsaStructureType.ICMP_TYPE_OBJECT_GROUP;
import static org.batfish.representation.cisco_asa.AsaStructureType.INTERFACE;
import static org.batfish.representation.cisco_asa.AsaStructureType.IPV4_ACCESS_LIST_EXTENDED;
import static org.batfish.representation.cisco_asa.AsaStructureType.IP_ACCESS_LIST;
import static org.batfish.representation.cisco_asa.AsaStructureType.NETWORK_OBJECT;
import static org.batfish.representation.cisco_asa.AsaStructureType.NETWORK_OBJECT_GROUP;
import static org.batfish.representation.cisco_asa.AsaStructureType.PROTOCOL_OBJECT_GROUP;
import static org.batfish.representation.cisco_asa.AsaStructureType.SERVICE_OBJECT;
import static org.batfish.representation.cisco_asa.AsaStructureType.SERVICE_OBJECT_GROUP;
import static org.batfish.representation.cisco_asa.AsaStructureUsage.EXTENDED_ACCESS_LIST_NETWORK_OBJECT;
import static org.batfish.representation.cisco_asa.AsaStructureUsage.EXTENDED_ACCESS_LIST_SERVICE_OBJECT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.BDDMatchers;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.config.Settings;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.LineType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.matchers.EigrpInterfaceSettingsMatchers;
import org.batfish.datamodel.matchers.IpAccessListMatchers;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco_asa.AsaConfiguration;
import org.batfish.representation.cisco_asa.AsaNat;
import org.batfish.representation.cisco_asa.AsaNat.Section;
import org.batfish.representation.cisco_asa.EigrpProcess;
import org.batfish.representation.cisco_asa.NetworkObject;
import org.batfish.representation.cisco_asa.NetworkObjectAddressSpecifier;
import org.batfish.representation.cisco_asa.NetworkObjectGroupAddressSpecifier;
import org.batfish.representation.cisco_asa.OspfNetworkType;
import org.batfish.representation.cisco_asa.WildcardAddressSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link AsaParser} and {@link AsaControlPlaneExtractor}. */
public final class CiscoAsaGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_asa/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private @Nonnull Bgpv4Route processRouteIn(RoutingPolicy routingPolicy, Bgpv4Route route) {
    Bgpv4Route.Builder builder = route.toBuilder();
    assertTrue(routingPolicy.process(route, builder, Direction.IN));
    return builder.build();
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Flow createFlow(IpProtocol protocol, int srcPort, int dstPort) {
    return Flow.builder()
        .setIngressNode("")
        .setIpProtocol(protocol)
        .setSrcPort(srcPort)
        .setDstPort(dstPort)
        .build();
  }

  private Flow createIcmpFlow(Integer icmpType) {
    return Flow.builder()
        .setIngressNode("")
        .setIpProtocol(IpProtocol.ICMP)
        .setIcmpType(icmpType)
        .setIcmpCode(0)
        .build();
  }

  private AsaConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    AsaCombinedParser asaParser = new AsaCombinedParser(src, settings);
    AsaControlPlaneExtractor extractor =
        new AsaControlPlaneExtractor(src, asaParser, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(asaParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    AsaConfiguration vendorConfiguration = (AsaConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    return SerializationUtils.clone(vendorConfiguration);
  }

  @Test
  public void testLineAuthenticationMethods() throws IOException {
    Configuration asaConfiguration = parseConfig("aaaAuthenticationAsa");
    SortedMap<String, Line> asaLines = asaConfiguration.getVendorFamily().getCisco().getLines();

    assertThat(asaLines.get("http"), not(hasAuthenticationLoginList(notNullValue())));

    assertThat(asaLines.get("ssh"), hasAuthenticationLoginList(hasMethod(GROUP_USER_DEFINED)));
    assertThat(asaLines.get("ssh"), hasAuthenticationLoginList(hasMethod(LOCAL_CASE)));

    assertThat(asaLines.get(SERIAL_LINE), not(hasAuthenticationLoginList()));

    assertThat(asaLines.get("telnet"), hasAuthenticationLoginList(hasMethod(GROUP_USER_DEFINED)));
    assertThat(asaLines.get("telnet"), hasAuthenticationLoginList(not(hasMethod(LOCAL_CASE))));
  }

  @Test
  public void testAaaAuthenticationLogin() throws IOException {
    Configuration aaaAuthAsaConfiguration = parseConfig("aaaAuthenticationAsa");
    SortedMap<String, Line> asaLines =
        aaaAuthAsaConfiguration.getVendorFamily().getCisco().getLines();
    for (Line line : asaLines.values()) {
      if (line.getLineType() == LineType.HTTP || line.getLineType() == LineType.SERIAL) {
        assertThat(line, not(requiresAuthentication()));
      } else {
        assertThat(line, requiresAuthentication());
      }
    }

    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(hasAuthentication(hasLogin(hasListForKey(hasMethod(LOCAL_CASE), "ssh")))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(hasListForKey(hasMethod(GROUP_USER_DEFINED), "ssh")))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(not(hasListForKey(hasMethod(LOCAL_CASE), "http"))))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(not(hasListForKey(hasMethod(GROUP_USER_DEFINED), "http"))))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(not(hasListForKey(hasMethod(LOCAL_CASE), SERIAL_LINE))))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(
                            not(hasListForKey(hasMethod(GROUP_USER_DEFINED), SERIAL_LINE))))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(hasListForKey(not(hasMethod(LOCAL_CASE)), "telnet")))))));
    assertThat(
        aaaAuthAsaConfiguration,
        hasVendorFamily(
            hasCisco(
                hasAaa(
                    hasAuthentication(
                        hasLogin(hasListForKey(hasMethod(GROUP_USER_DEFINED), "telnet")))))));
  }

  @Test
  public void testAsaAclObject() throws IOException {
    String hostname = "asa-acl-object";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /*
     * The produced ACL should permit if source matcher object on1, destination matches on2, and
     * service matches os1.
     */
    assertThat(
        c,
        hasIpAccessList(
            "acl1",
            hasLines(
                hasItem(
                    hasMatchCondition(
                        isAndMatchExprThat(
                            hasConjuncts(
                                containsInAnyOrder(
                                    ImmutableList.of(
                                        isMatchHeaderSpaceThat(
                                            hasHeaderSpace(
                                                allOf(
                                                    hasDstIps(
                                                        isIpSpaceReferenceThat(hasName("on2"))),
                                                    hasSrcIps(
                                                        isIpSpaceReferenceThat(hasName("on1")))))),
                                        isPermittedByAclThat(
                                            hasAclName(
                                                computeServiceObjectAclName("os1"))))))))))));

    /*
     * We expect only objects osunused1, onunused1 to have zero referrers
     */
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT, "os1", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "on1", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "on2", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT, "osunused1", 0));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "onunused1", 0));

    /*
     * We expect undefined references only to objects osfake, onfake1, onfake2
     */
    assertThat(ccae, not(hasUndefinedReference(filename, SERVICE_OBJECT, "os1")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "on1")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "on2")));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, SERVICE_OBJECT, "osfake", EXTENDED_ACCESS_LIST_SERVICE_OBJECT));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, NETWORK_OBJECT, "onfake2", EXTENDED_ACCESS_LIST_NETWORK_OBJECT));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, NETWORK_OBJECT, "onfake1", EXTENDED_ACCESS_LIST_NETWORK_OBJECT));
  }

  @Test
  public void testAsaBanner() throws IOException {
    Configuration c = parseConfig("asa_banner");
    assertThat(
        c.getVendorFamily().getCisco().getBanners().get("exec"),
        equalTo("^C\nAbove not actually a delimiter. This text is added."));
  }

  @Test
  public void testAsaEigrpNetwork() {
    AsaConfiguration config = parseVendorConfig("asa-eigrp");

    // ASN is 1
    EigrpProcess eigrpProcess = config.getDefaultVrf().getEigrpProcesses().get(1L);
    assertThat(eigrpProcess.getWildcardNetworks(), contains(IpWildcard.parse("10.0.0.0/24")));
  }

  @Test
  public void testAsaEigrpPassive() throws IOException {
    Configuration config = parseConfig("asa-eigrp");

    assertThat(
        config, hasInterface("inside", hasEigrp(EigrpInterfaceSettingsMatchers.hasPassive(true))));
  }

  @Test
  public void testAsaFailoverExtraction() {
    AsaConfiguration config = parseVendorConfig("asa-failover");
    assertThat(config.getFailover(), equalTo(true));
    assertThat(config.getFailoverSecondary(), equalTo(false));
    assertThat(config.getFailoverCommunicationInterface(), equalTo("GigabitEthernet0/2"));
    assertThat(config.getFailoverCommunicationInterfaceAlias(), equalTo("FAILOVER"));
    assertThat(config.getFailoverStatefulSignalingInterface(), equalTo("GigabitEthernet0/3"));
    assertThat(config.getFailoverStatefulSignalingInterfaceAlias(), equalTo("REPLICATION"));
    assertThat(
        config.getFailoverPrimaryAddresses(),
        hasEntry("FAILOVER", ConcreteInterfaceAddress.parse("172.16.1.1/30")));
    assertThat(
        config.getFailoverStandbyAddresses(),
        hasEntry("FAILOVER", ConcreteInterfaceAddress.parse("172.16.1.2/30")));
  }

  @Test
  public void testAsaFailoverConversion() throws IOException {
    Configuration config = parseConfig("asa-failover");
    assertThat(
        config,
        hasInterface(
            "GigabitEthernet0/2", hasAddress(ConcreteInterfaceAddress.parse("172.16.1.1/30"))));
  }

  @Test
  public void testAsaInterfaceRedundantExtraction() {
    AsaConfiguration config = parseVendorConfig("asa-interface-redundant");

    assertThat(
        config.getInterfaces(),
        hasKeys(
            "GigabitEthernet0/1",
            "GigabitEthernet0/2",
            "Redundant1",
            "Redundant1.2",
            "Redundant2",
            "Redundant2.2"));
    {
      org.batfish.representation.cisco_asa.Interface iface =
          config.getInterfaces().get("Redundant1");
      assertThat(
          iface.getMemberInterfaces(),
          containsInAnyOrder("GigabitEthernet0/1", "GigabitEthernet0/2"));
    }
    {
      org.batfish.representation.cisco_asa.Interface iface =
          config.getInterfaces().get("Redundant2");
      assertThat(iface.getMemberInterfaces(), empty());
    }
  }

  @Test
  public void testAsaInterfaceRedundantConversion() throws IOException {
    Configuration config = parseConfig("asa-interface-redundant");

    assertThat(
        config.getAllInterfaces(),
        hasKeys(
            "GigabitEthernet0/1",
            "GigabitEthernet0/2",
            "Redundant1",
            "redundant1sub",
            "Redundant2",
            "redundant2sub"));
    {
      Interface iface = config.getAllInterfaces().get("Redundant1");
      assertThat(
          iface.getDependencies(),
          containsInAnyOrder(
              new Dependency("GigabitEthernet0/1", DependencyType.AGGREGATE),
              new Dependency("GigabitEthernet0/2", DependencyType.AGGREGATE)));
      assertThat(iface.getBandwidth(), equalTo(1E9D));
      assertTrue(iface.getActive());
    }
    {
      Interface iface = config.getAllInterfaces().get("redundant1sub");
      assertThat(iface.getBandwidth(), equalTo(1E9D));
      assertTrue(iface.getActive());
    }
    {
      Interface iface = config.getAllInterfaces().get("Redundant2");
      assertThat(iface.getDependencies(), empty());
      assertThat(iface.getBandwidth(), equalTo(0.0D));
      assertFalse(iface.getActive());
    }
    {
      Interface iface = config.getAllInterfaces().get("redundant2sub");
      assertThat(iface.getBandwidth(), equalTo(0.0D));
      assertFalse(iface.getActive());
    }
  }

  @Test
  public void testAsaOspfReferenceBandwidth() throws IOException {
    Configuration manual = parseConfig("asaOspfCost");
    assertThat(
        manual.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(), equalTo(3e6d));

    Configuration defaults = parseConfig("asaOspfCostDefaults");
    assertThat(
        defaults.getDefaultVrf().getOspfProcesses().get("1").getReferenceBandwidth(),
        equalTo(
            org.batfish.representation.cisco_asa.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH_100_MBPS));
  }

  @Test
  public void testAsaFilters() throws IOException {
    String hostname = "asa-filters";
    Configuration c = parseConfig(hostname);

    String ifaceAlias = "name1";

    Flow flowPass = createFlow(IpProtocol.TCP, 1, 123);
    Flow flowFail = createFlow(IpProtocol.TCP, 1, 1);

    // Confirm access list permits only traffic matching ACL
    assertThat(
        c,
        hasInterface(ifaceAlias, hasPreTransformationOutgoingFilter(accepts(flowPass, null, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias, hasPreTransformationOutgoingFilter(not(accepts(flowFail, null, c)))));

    // FILTER_IN is applied post-transformation
    assertThat(
        c,
        hasInterface(
            ifaceAlias,
            hasPostTransformationIncomingFilter(IpAccessListMatchers.hasName("FILTER_IN"))));
  }

  @Test
  public void testAsaFiltersGlobal() throws IOException {
    String hostname = "asa-filters-global";
    Configuration c = parseConfig(hostname);

    String iface1Alias = "name1";
    String iface2Alias = "name2";

    Flow flowPass = createFlow(IpProtocol.TCP, 1, 123);
    Flow flowFail = createFlow(IpProtocol.TCP, 1, 1);

    // Confirm global ACL affects all interfaces
    assertThat(
        c,
        hasInterface(iface1Alias, hasPostTransformationIncomingFilter(accepts(flowPass, null, c))));
    assertThat(
        c,
        hasInterface(iface2Alias, hasPostTransformationIncomingFilter(accepts(flowPass, null, c))));
    assertThat(
        c,
        hasInterface(
            iface1Alias, hasPostTransformationIncomingFilter(not(accepts(flowFail, null, c)))));
    assertThat(
        c,
        hasInterface(
            iface2Alias, hasPostTransformationIncomingFilter(not(accepts(flowFail, null, c)))));
  }

  @Test
  public void testAsaFiltersGlobalReference() throws IOException {
    String hostname = "asa-filters-global";
    String filename = "configs/" + hostname;
    parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference tracking is correct for globally applied ASA access list in access group
    assertThat(ccae, hasNumReferrers(filename, IPV4_ACCESS_LIST_EXTENDED, "FILTER_GLOBAL", 1));
  }

  @Test
  public void testAsaFiltersReference() throws IOException {
    String hostname = "asa-filters";
    String filename = "configs/" + hostname;
    parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference tracking is correct for ASA access lists in access group
    assertThat(ccae, hasNumReferrers(filename, IPV4_ACCESS_LIST_EXTENDED, "FILTER_IN", 1));
    assertThat(ccae, hasNumReferrers(filename, IPV4_ACCESS_LIST_EXTENDED, "FILTER_IN4", 1));
    assertThat(ccae, hasNumReferrers(filename, IPV4_ACCESS_LIST_EXTENDED, "FILTER_OUT", 1));
    assertThat(ccae, hasNumReferrers(filename, IPV4_ACCESS_LIST_EXTENDED, "FILTER_OUT5", 1));
    assertThat(ccae, hasUndefinedReference(filename, IP_ACCESS_LIST, "FILTER_UNDEF"));
  }

  @Test
  public void testAsaSecurityLevelAndFilters() throws IOException {
    String hostname = "asa-filters";
    Configuration c = parseConfig(hostname);

    String highIface1 = "name1"; // GigabitEthernet0/1
    String lowIface2 = "name2"; // GigabitEthernet0/2
    String highIface3 = "name3"; // GigabitEthernet0/3
    String lowIface4 = "name4"; // GigabitEthernet0/4
    String lowIface5 = "name5"; // GigabitEthernet0/5

    Flow flowPass = createFlow(IpProtocol.TCP, 1, 123);
    Flow flowFail = createFlow(IpProtocol.TCP, 1, 1);
    Flow anyFlow = createFlow(IpProtocol.OSPF, 0, 0);

    // Confirm access list permits only traffic matching both ACL and security level restrictions
    // highIface1 has inbound filter permitting all IP traffic
    // highIface1 has outbound filter permitting only TCP port 123
    // highIface1 rejects all traffic from lowIface2 due to security level restriction
    assertThat(
        c,
        hasInterface(
            highIface1, hasPreTransformationOutgoingFilter(rejects(anyFlow, lowIface2, c))));

    // Confirm access list permits only traffic matching both ACL and security level restrictions
    // highIface1 has a higher security level than lowIface5
    // lowIface5 has no inbound filter
    // lowIface5 rejects all outbound traffic except TCP port 123
    assertThat(
        c,
        hasInterface(
            lowIface5, hasPreTransformationOutgoingFilter(rejects(flowFail, highIface1, c))));
    assertThat(
        c,
        hasInterface(
            lowIface5, hasPreTransformationOutgoingFilter(accepts(flowPass, highIface1, c))));

    // lowIface4 has inbound filter permitting only TCP port 123
    // highIface3 has no explicit outbound filter
    assertThat(
        c,
        hasInterface(
            lowIface4, hasPostTransformationIncomingFilter(accepts(flowPass, lowIface4, c))));
    assertThat(
        c,
        hasInterface(
            lowIface4, hasPostTransformationIncomingFilter(rejects(flowFail, lowIface4, c))));
    // any flow outbound on highIface3 from lowIface4 is allowed, assuming it was allowed incoming
    // security level restriction is removed because lowIface4 has an inbound ACL
    assertThat(
        c,
        hasInterface(
            highIface3, hasPreTransformationOutgoingFilter(accepts(anyFlow, lowIface4, c))));
  }

  @Test
  public void testAsaGh5875() throws IOException {
    Configuration c = parseConfig("asa-gh-5875");
    BDDPacket p = new BDDPacket();

    {
      String aclName = computeServiceObjectGroupAclName("IP_GROUP");
      assertThat(c, hasIpAccessList(aclName));
      IpAccessList acl = c.getIpAccessLists().get(aclName);
      assertThat(IpAccessListToBdd.toBDD(p, acl), BDDMatchers.isOne());
    }
    {
      String aclName = computeServiceObjectGroupAclName("TCP_GROUP");
      assertThat(c, hasIpAccessList(aclName));
      IpAccessList acl = c.getIpAccessLists().get(aclName);
      assertThat(IpAccessListToBdd.toBDD(p, acl), equalTo(p.getIpProtocol().value(IpProtocol.TCP)));
    }
    {
      String aclName = computeServiceObjectGroupAclName("AH_GROUP");
      assertThat(c, hasIpAccessList(aclName));
      IpAccessList acl = c.getIpAccessLists().get(aclName);
      assertThat(IpAccessListToBdd.toBDD(p, acl), equalTo(p.getIpProtocol().value(IpProtocol.AHP)));
    }
  }

  @Test
  public void testAsaGh6042() throws IOException {
    String hostname = "asa-gh-6042";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae, hasNumReferrers("configs/" + hostname, IPV4_ACCESS_LIST_EXTENDED, "acl_name", 1));
  }

  @Test
  public void testAsaServiceObject() throws IOException {
    String hostname = "asa-service-object";
    String filename = "configs/" + hostname;
    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    String osIcmpAclName = computeServiceObjectAclName("OS_ICMP");
    String osTcpAclName = computeServiceObjectAclName("OS_TCPUDP");
    String ogsAclName = computeServiceObjectGroupAclName("OGS1");
    String ogsUndefOsName = computeServiceObjectGroupAclName("OGS_UNDEF_OS");
    String ogsUndefOgsName = computeServiceObjectGroupAclName("OGS_UNDEF_OGS");

    Flow flowIcmpPass = createIcmpFlow(IcmpType.ECHO_REQUEST);
    Flow flowIcmpFail = createIcmpFlow(IcmpType.ECHO_REPLY);
    Flow flowInlinePass1 = createFlow(IpProtocol.UDP, 1, 1234);
    Flow flowInlinePass2 = createFlow(IpProtocol.UDP, 1, 1235);
    Flow flowInlinePass3 = createFlow(IpProtocol.UDP, 3, 1236);
    Flow flowInlinePass4 = createFlow(IpProtocol.UDP, 3020, 1); // cifs
    Flow flowTcpPass = createFlow(IpProtocol.TCP, 65535, 1);
    Flow flowUdpPass = createFlow(IpProtocol.UDP, 65535, 1);
    Flow flowTcpFail = createFlow(IpProtocol.TCP, 65534, 1);
    Flow flowUdpFail = createFlow(IpProtocol.UDP, 1, 1236);

    /* Confirm service objects have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT, "OS_TCPUDP", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT, "OS_ICMP", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, SERVICE_OBJECT, "OS_UNDEFINED"));
    assertThat(ccae, hasUndefinedReference(filename, SERVICE_OBJECT_GROUP, "OGS_UNDEFINED"));
    /* Confirm reference to builtin does not result in undefined reference. */
    assertThat(ccae, not(hasUndefinedReference(filename, SERVICE_OBJECT, "icmp")));

    /* Confirm IpAcls created from service objects permit and reject the correct flows */
    assertThat(c, hasIpAccessList(osTcpAclName, accepts(flowTcpPass, null, c)));
    assertThat(c, hasIpAccessList(osTcpAclName, accepts(flowUdpPass, null, c)));
    assertThat(c, hasIpAccessList(osTcpAclName, not(accepts(flowTcpFail, null, c))));
    assertThat(c, hasIpAccessList(osIcmpAclName, accepts(flowIcmpPass, null, c)));
    assertThat(c, hasIpAccessList(osIcmpAclName, not(accepts(flowIcmpFail, null, c))));

    /* Confirm object-group permits and rejects the flows determined by its constituent service objects */
    assertThat(c, hasIpAccessList(ogsAclName, accepts(flowTcpPass, null, c)));
    assertThat(c, hasIpAccessList(ogsAclName, not(accepts(flowTcpFail, null, c))));
    assertThat(c, hasIpAccessList(ogsAclName, accepts(flowInlinePass1, null, c)));
    assertThat(c, hasIpAccessList(ogsAclName, accepts(flowInlinePass2, null, c)));
    assertThat(c, hasIpAccessList(ogsAclName, accepts(flowInlinePass3, null, c)));
    assertThat(c, hasIpAccessList(ogsAclName, accepts(flowInlinePass4, null, c)));
    assertThat(c, hasIpAccessList(ogsAclName, not(accepts(flowUdpFail, null, c))));

    /* Confirm undefined references reject and do not cause crash */
    assertThat(c, hasIpAccessList(ogsUndefOsName, not(accepts(flowUdpFail, null, c))));
    assertThat(c, hasIpAccessList(ogsUndefOgsName, not(accepts(flowUdpFail, null, c))));
  }

  @Test
  public void testAsaServiceObjectInline() throws IOException {
    String hostname = "asa-service-object-inline";
    Configuration c = parseConfig(hostname);
    assertThat(c, hasConfigurationFormat(ConfigurationFormat.CISCO_ASA));

    String icmpAclName = "ACL_ICMP";
    String ospfAclName = "ACL_OSPF";

    Flow flowIcmpPass = createIcmpFlow(IcmpType.ECHO_REQUEST);
    Flow flowIcmpFail = createIcmpFlow(IcmpType.ECHO_REPLY);
    Flow flowOspfPass = createFlow(IpProtocol.OSPF, 0, 0);
    Flow flowOspfFail = createFlow(IpProtocol.UDP, 0, 0);

    /* Confirm IpAcls created from inline service objects permit and reject the correct flows */
    assertThat(c, hasIpAccessList(icmpAclName, accepts(flowIcmpPass, null, c)));
    assertThat(c, hasIpAccessList(icmpAclName, rejects(flowIcmpFail, null, c)));
    assertThat(c, hasIpAccessList(ospfAclName, accepts(flowOspfPass, null, c)));
    assertThat(c, hasIpAccessList(ospfAclName, rejects(flowOspfFail, null, c)));
  }

  @Test
  public void testAsaNestedIcmpTypeObjectGroup() throws IOException {
    String hostname = "asa-nested-icmp-type-object-group";
    String filename = "configs/" + hostname;
    String services = computeIcmpObjectGroupAclName("services");
    String mixedGroup = computeIcmpObjectGroupAclName("mixed_group");

    Flow echoReplyFlow = createIcmpFlow(IcmpType.ECHO_REPLY);
    Flow unreachableFlow = createIcmpFlow(IcmpType.DESTINATION_UNREACHABLE);
    Flow redirectFlow = createIcmpFlow(IcmpType.REDIRECT_MESSAGE);
    Flow maskReplyFlow = createIcmpFlow(IcmpType.MASK_REPLY);
    Flow otherFlow = createIcmpFlow(IcmpType.TRACEROUTE);

    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /* Confirm objects have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "echo_group", 1));
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "unreachable_group", 1));
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "redirect_group", 1));
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "services", 0));
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "mask_reply_group", 1));
    assertThat(ccae, hasNumReferrers(filename, ICMP_TYPE_OBJECT_GROUP, "mixed_group", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, ICMP_TYPE_OBJECT_GROUP, "UNDEFINED_GROUP"));
    assertThat(
        ccae, hasUndefinedReference(filename, ICMP_TYPE_OBJECT_GROUP, "UNDEFINED_GROUP_MIXED"));

    assertThat(c, hasIpAccessList(services, accepts(echoReplyFlow, null, c)));
    assertThat(c, hasIpAccessList(services, accepts(unreachableFlow, null, c)));
    assertThat(c, hasIpAccessList(services, accepts(redirectFlow, null, c)));
    assertThat(c, hasIpAccessList(mixedGroup, accepts(maskReplyFlow, null, c)));
    assertThat(c, hasIpAccessList(services, not(accepts(otherFlow, null, c))));
  }

  @Test
  public void testAsaNestedNetworkObjectGroup() throws IOException {
    String hostname = "asa-nested-network-object-group";
    String filename = "configs/" + hostname;
    Ip engHostIp = Ip.parse("10.1.1.5");
    Ip hrHostIp = Ip.parse("10.1.2.8");
    Ip financeHostIp = Ip.parse("10.1.4.89");
    Ip itIp = Ip.parse("10.2.3.4");
    Ip otherIp = Ip.parse("1.2.3.4");

    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /* Confirm service have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "eng", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "hr", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "finance", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "admin", 0));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "it", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "mixed_group", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, NETWORK_OBJECT_GROUP, "UNDEFINED_GROUP"));
    assertThat(
        ccae, hasUndefinedReference(filename, NETWORK_OBJECT_GROUP, "UNDEFINED_GROUP_MIXED"));

    assertThat(c, hasIpSpace("admin", containsIp(engHostIp, c.getIpSpaces())));
    assertThat(c, hasIpSpace("admin", containsIp(hrHostIp, c.getIpSpaces())));
    assertThat(c, hasIpSpace("admin", containsIp(financeHostIp, c.getIpSpaces())));
    assertThat(c, hasIpSpace("mixed_group", containsIp(itIp, c.getIpSpaces())));
    assertThat(c, hasIpSpace("admin", not(containsIp(otherIp, c.getIpSpaces()))));
  }

  @Test
  public void testAsaNestedProtocolObjectGroup() throws IOException {
    String hostname = "asa-nested-protocol-object-group";
    String filename = "configs/" + hostname;
    String protocols = computeProtocolObjectGroupAclName("protocols");
    String mixedGroup = computeProtocolObjectGroupAclName("mixed_group");
    int someSrcPort = 65535;
    int someDstPort = 1;

    Flow igmpFlow = createFlow(IpProtocol.IGMP, someSrcPort, someDstPort);
    Flow tcpFlow = createFlow(IpProtocol.TCP, someSrcPort, someDstPort);
    Flow ospfFlow = createFlow(IpProtocol.OSPF, someSrcPort, someDstPort);
    Flow greFlow = createFlow(IpProtocol.GRE, someSrcPort, someDstPort);
    Flow otherFlow = createFlow(IpProtocol.AHP, someSrcPort, someDstPort);

    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /* Confirm service have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "proto1", 1));
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "proto2", 1));
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "proto3", 1));
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "protocols", 0));
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "proto4", 1));
    assertThat(ccae, hasNumReferrers(filename, PROTOCOL_OBJECT_GROUP, "mixed_group", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, PROTOCOL_OBJECT_GROUP, "UNDEFINED_GROUP"));
    assertThat(
        ccae, hasUndefinedReference(filename, PROTOCOL_OBJECT_GROUP, "UNDEFINED_GROUP_MIXED"));

    assertThat(c, hasIpAccessList(protocols, accepts(igmpFlow, null, c)));
    assertThat(c, hasIpAccessList(protocols, accepts(tcpFlow, null, c)));
    assertThat(c, hasIpAccessList(protocols, accepts(ospfFlow, null, c)));
    assertThat(c, hasIpAccessList(mixedGroup, accepts(greFlow, null, c)));
    assertThat(c, hasIpAccessList(protocols, not(accepts(otherFlow, null, c))));
  }

  @Test
  public void testAsaNestedServiceObjectGroup() throws IOException {
    String hostname = "asa-nested-service-object-group";
    String filename = "configs/" + hostname;
    String services = computeServiceObjectGroupAclName("services");
    String mixedGroup = computeServiceObjectGroupAclName("mixed_group");
    int someSrcPort = 65535;
    int someDstPort = 1;

    Flow dns = createFlow(IpProtocol.UDP, someSrcPort, 53);
    Flow customPort = createFlow(IpProtocol.UDP, someSrcPort, 1234);
    Flow customPortInRange = createFlow(IpProtocol.TCP, someSrcPort, 2350);
    Flow customNestedPort = createFlow(IpProtocol.UDP, someSrcPort, 4444);
    Flow otherFlow = createFlow(IpProtocol.AHP, someSrcPort, someDstPort);

    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    /* Confirm objects have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "service1", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "service2", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "service3", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "services", 0));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "service4", 1));
    assertThat(ccae, hasNumReferrers(filename, SERVICE_OBJECT_GROUP, "mixed_group", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, SERVICE_OBJECT_GROUP, "UNDEFINED_GROUP"));
    assertThat(
        ccae, hasUndefinedReference(filename, SERVICE_OBJECT_GROUP, "UNDEFINED_GROUP_MIXED"));

    assertThat(c, hasIpAccessList(services, accepts(dns, null, c)));
    assertThat(c, hasIpAccessList(services, accepts(customPort, null, c)));
    assertThat(c, hasIpAccessList(services, accepts(customPortInRange, null, c)));
    assertThat(c, hasIpAccessList(mixedGroup, accepts(customNestedPort, null, c)));
    assertThat(c, hasIpAccessList(services, not(accepts(otherFlow, null, c))));
  }

  @Test
  public void testAsaSnmp() throws IOException {
    Configuration c = parseConfig("asa_snmp");
    assertThat(c.getSnmpSourceInterface(), equalTo("inside"));
    assertThat(c.getSnmpTrapServers(), contains("1.2.3.4"));
  }

  @Test
  public void testNetworkObject() throws IOException {
    String hostname = "network-object";
    String filename = "configs/" + hostname;
    Configuration c = parseConfig(hostname);
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    Ip on1Ip = Ip.parse("1.2.3.4");
    Ip on2IpStart = Ip.parse("2.2.2.0");
    Ip on2IpEnd = Ip.parse("2.2.2.255");
    Ip inlineIp = Ip.parse("3.3.3.3");

    /* Confirm network object IpSpaces cover the correct Ip addresses */
    assertThat(c, hasIpSpace("ON1", containsIp(on1Ip)));
    assertThat(c, hasIpSpace("ON1", not(containsIp(on2IpStart))));
    assertThat(c, hasIpSpace("ON2", containsIp(on2IpStart)));
    assertThat(c, hasIpSpace("ON2", containsIp(on2IpEnd)));
    assertThat(c, hasIpSpace("ON2", not(containsIp(on1Ip))));

    /* Confirm object-group also covers the IpSpaces its network objects cover */
    assertThat(c, hasIpSpace("OGN", containsIp(on1Ip, c.getIpSpaces())));
    assertThat(c, hasIpSpace("OGN", containsIp(inlineIp, c.getIpSpaces())));
    assertThat(c, hasIpSpace("OGN", not(containsIp(on2IpStart, c.getIpSpaces()))));

    /* Confirm network objects have the correct number of referrers */
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "ON1", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "ON2", 0));
    /* Confirm undefined reference shows up as such */
    assertThat(ccae, hasUndefinedReference(filename, NETWORK_OBJECT, "ON_UNDEFINED"));
  }

  @Test
  public void testCiscoOspfNetworkTypes() {
    AsaConfiguration config = parseVendorConfig("ospf-network-types");

    String eth0 = "Ethernet0/0";
    String eth1 = "Ethernet0/1";
    String eth2 = "Ethernet0/2";
    String eth3 = "Ethernet0/3";
    String eth4 = "Ethernet0/4";
    String eth5 = "Ethernet0/5";

    Map<String, org.batfish.representation.cisco_asa.Interface> ifaces = config.getInterfaces();
    assertThat(ifaces.keySet(), containsInAnyOrder(eth0, eth1, eth2, eth3, eth4, eth5));
    // No network set should result in a null network type
    assertThat(ifaces.get(eth0).getOspfNetworkType(), nullValue());
    // Confirm explicitly set network types show up as expected in the VS model
    assertThat(ifaces.get(eth1).getOspfNetworkType(), equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(ifaces.get(eth2).getOspfNetworkType(), equalTo(OspfNetworkType.BROADCAST));
    assertThat(ifaces.get(eth3).getOspfNetworkType(), equalTo(OspfNetworkType.NON_BROADCAST));
    assertThat(ifaces.get(eth4).getOspfNetworkType(), equalTo(OspfNetworkType.POINT_TO_MULTIPOINT));
    assertThat(
        ifaces.get(eth5).getOspfNetworkType(),
        equalTo(OspfNetworkType.POINT_TO_MULTIPOINT_NON_BROADCAST));
  }

  @Test
  public void testCiscoOspfIntervals() {
    AsaConfiguration config = parseVendorConfig("ospf-intervals");

    String eth0 = "Ethernet0/0";
    String eth1 = "Ethernet0/1";
    String eth2 = "Ethernet0/2";
    String eth3 = "Ethernet0/3";

    Map<String, org.batfish.representation.cisco_asa.Interface> ifaces = config.getInterfaces();
    assertThat(ifaces, hasKeys(eth0, eth1, eth2, eth3));

    // Confirm explicitly set hello and dead intervals show up in the VS model
    // Also confirm intervals that are not set show up as nulls in the VS model
    assertThat(ifaces.get(eth0).getOspfDeadInterval(), nullValue());
    assertThat(ifaces.get(eth0).getOspfHelloInterval(), nullValue());

    assertThat(ifaces.get(eth1).getOspfDeadInterval(), nullValue());
    assertThat(ifaces.get(eth1).getOspfHelloInterval(), equalTo(11));

    assertThat(ifaces.get(eth2).getOspfDeadInterval(), equalTo(36));
    assertThat(ifaces.get(eth2).getOspfHelloInterval(), equalTo(12));

    assertThat(ifaces.get(eth3).getOspfDeadInterval(), equalTo(42));
    assertThat(ifaces.get(eth3).getOspfHelloInterval(), nullValue());
  }

  private Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    assertThat(configs, hasKey(hostname.toLowerCase()));
    Configuration c = configs.get(hostname.toLowerCase());
    assertThat(c.getConfigurationFormat(), equalTo(ConfigurationFormat.CISCO_ASA));
    return c;
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Map<String, Configuration> configs = BatfishTestUtils.parseTextConfigs(_folder, names);
    assertTrue(
        configs.values().stream()
            .map(Configuration::getConfigurationFormat)
            .allMatch(f -> f == ConfigurationFormat.CISCO_ASA));
    return configs;
  }

  @Test
  public void testAsaInterface() throws IOException {
    String hostname = "asa-interface";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm interface's address is extracted properly
    assertThat(
        c,
        hasInterface(
            "ifname",
            hasAllAddresses(containsInAnyOrder(ConcreteInterfaceAddress.parse("3.0.0.2/24")))));

    // Confirm that interface MTU is set correctly
    assertThat(c, hasInterface("ifname", hasMtu(1400)));

    // Confirm interface definition is tracked for the alias name
    assertThat(ccae, hasDefinedStructure(filename, INTERFACE, "ifname"));
  }

  // https://github.com/batfish/batfish/issues/4124
  @Test
  public void testAsaInterfaceOspfWithInheritance() throws IOException {
    String hostname = "asa-interface-ospf";
    Configuration c = parseConfig(hostname);
    assertThat(
        c,
        hasInterface(
            "LAB-INT",
            allOf(
                hasBandwidth(0.0d),
                hasOspfAreaName(0L),
                hasInterfaceType(InterfaceType.AGGREGATE_CHILD))));
  }

  @Test
  public void testAsaInterfaceEncapsulationVlan() throws IOException {
    String hostname = "asa_interface_encapsulation_vlan";
    Configuration c = parseConfig(hostname);

    // encapsulation vlan should be read in s_interface context, and so later IP address should be
    // correctly extracted
    assertThat(
        c,
        hasInterface(
            "ifname",
            both(hasEncapsulationVlan(100))
                .and(hasAddress(ConcreteInterfaceAddress.parse("192.0.2.1/24")))));
  }

  @Test
  public void testAsaSecurityLevel() throws IOException {
    Configuration c = parseConfig("asa-security-level");
    String explicit100Interface = "all-trust";
    String insideInterface = "inside";
    String explicit45Interface = "some-trust";
    String outsideInterface = "outside";

    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    // Confirm zones are created for each level
    assertThat(c, hasZone(computeASASecurityLevelZoneName(100), hasMemberInterfaces(hasSize(2))));
    assertThat(c, hasZone(computeASASecurityLevelZoneName(45), hasMemberInterfaces(hasSize(1))));
    assertThat(c, hasZone(computeASASecurityLevelZoneName(1), hasMemberInterfaces(hasSize(1))));

    // No traffic in and out of the same interface
    assertThat(
        c,
        hasInterface(
            explicit100Interface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, explicit100Interface, c))));
    assertThat(
        c,
        hasInterface(
            insideInterface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, insideInterface, c))));
    assertThat(
        c,
        hasInterface(
            explicit45Interface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, explicit45Interface, c))));
    assertThat(
        c,
        hasInterface(
            outsideInterface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, outsideInterface, c))));

    // No traffic between interfaces with same level
    assertThat(
        c,
        hasInterface(
            insideInterface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, explicit100Interface, c))));
    assertThat(
        c,
        hasInterface(
            explicit100Interface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, insideInterface, c))));

    // Allow traffic from 100 to others
    assertThat(
        c,
        hasInterface(
            explicit45Interface,
            hasPreTransformationOutgoingFilter(accepts(newFlow, insideInterface, c))));
    assertThat(
        c,
        hasInterface(
            outsideInterface,
            hasPreTransformationOutgoingFilter(accepts(newFlow, insideInterface, c))));

    // Mid level is accepted by lower, but not higher
    assertThat(
        c,
        hasInterface(
            insideInterface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, explicit45Interface, c))));
    assertThat(
        c,
        hasInterface(
            outsideInterface,
            hasPreTransformationOutgoingFilter(accepts(newFlow, explicit45Interface, c))));

    // No traffic from outside
    assertThat(
        c,
        hasInterface(
            insideInterface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, outsideInterface, c))));
    assertThat(
        c,
        hasInterface(
            explicit45Interface,
            hasPreTransformationOutgoingFilter(rejects(newFlow, outsideInterface, c))));
  }

  @Test
  public void testAsaSecurityLevel_tracing() throws IOException {
    Configuration c = parseConfig("asa-security-level");
    String explicit100Interface = "all-trust";
    String insideInterface = "inside";

    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    BiFunction<String, String, List<TraceTree>> trace =
        (fromIface, toIface) ->
            AclTracer.trace(
                c.getAllInterfaces().get(toIface).getPreTransformationOutgoingFilter(),
                newFlow,
                fromIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    // from device
    {
      List<TraceTree> traces = trace.apply(null, insideInterface);
      assertThat(traces, contains(isTraceTree(PERMIT_TRAFFIC_FROM_DEVICE)));
    }

    // intra-security-level, but from/to different interfaces
    {
      List<TraceTree> traces = trace.apply(explicit100Interface, insideInterface);
      assertThat(traces, contains(isTraceTree(DENY_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT)));
    }

    // hairpinning
    {
      List<TraceTree> traces = trace.apply(explicit100Interface, explicit100Interface);
      assertThat(traces, contains(isTraceTree(DENY_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT)));
    }
  }

  @Test
  public void testAsaSecurityLevelPermitBoth() throws IOException {
    Configuration c = parseConfig("asa-security-level-permit-both");
    String ifaceAlias1 = "name1";
    String ifaceAlias2 = "name2";
    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    // Allow traffic in and out of the same interface
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias1, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias2, c))));

    // Allow traffic between interfaces with same level
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias2, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias1, c))));
  }

  @Test
  public void testAsaSecurityLevelPermitBoth_tracing() throws IOException {
    Configuration c = parseConfig("asa-security-level-permit-both");
    String ifaceAlias1 = "name1";
    String ifaceAlias2 = "name2";
    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    BiFunction<String, String, List<TraceTree>> trace =
        (fromIface, toIface) ->
            AclTracer.trace(
                c.getAllInterfaces().get(toIface).getPreTransformationOutgoingFilter(),
                newFlow,
                fromIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    // intra-security-level, but from/to different interfaces
    {
      List<TraceTree> traces = trace.apply(ifaceAlias1, ifaceAlias2);
      assertThat(traces, contains(isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT)));
    }

    // hairpinning
    {
      List<TraceTree> traces = trace.apply(ifaceAlias1, ifaceAlias1);
      assertThat(traces, contains(isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT)));
    }
  }

  /**
   * Test the traces AclTracer produces for ASA security-levels, when the security-level policy
   * permits, and there is an out filter on the out interface.
   */
  @Test
  public void testAsaSecurityLevelPermitTracing() throws IOException {
    Configuration c = parseConfig("asa-security-level-permit-tracing");
    String out = "out"; // security-level 50, has out filter
    String inSameLevel = "inSameLevel"; // security-level 50
    String inLowFiltered = "inLowFiltered"; // security-level 10 with ingress filter
    String inHigh = "inHigh"; // security-level 100

    Flow permitFlow = createFlow(IpProtocol.TCP, 0, 123);
    Flow defaultDenyFlow = createFlow(IpProtocol.TCP, 0, 80);
    Flow explicitDenyFlow = createFlow(IpProtocol.TCP, 0, 22);

    // out is always the egress interface
    IpAccessList filter = c.getAllInterfaces().get(out).getPreTransformationOutgoingFilter();
    BiFunction<String, Flow, List<TraceTree>> trace =
        (inIface, flow) ->
            AclTracer.trace(
                filter,
                flow,
                inIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    IpAccessList filterOut = c.getIpAccessLists().get("FILTER_OUT");

    // permitted, intra-interface
    {
      List<TraceTree> traces = trace.apply(out, permitFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT),
              isTraceTree(
                  asaPermittedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut),
                  isTraceTree(matchedByAclLine(filterOut, 0)))));
    }

    // default-denied, intra-interface
    {
      List<TraceTree> traces = trace.apply(out, defaultDenyFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT),
              isTraceTree(
                  asaDeniedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut))));
    }

    // explicitly denied, intra-interface
    {
      List<TraceTree> traces = trace.apply(out, explicitDenyFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT),
              isTraceTree(
                  asaDeniedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut),
                  isTraceTree(matchedByAclLine("deny tcp any any eq 22")))));
    }

    // permitted, inter-interface
    {
      List<TraceTree> traces = trace.apply(inSameLevel, permitFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT),
              isTraceTree(
                  asaPermittedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut),
                  isTraceTree(matchedByAclLine(filterOut, 0)))));
    }

    // denied, inter-interface
    {
      List<TraceTree> traces = trace.apply(inSameLevel, defaultDenyFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT),
              isTraceTree(
                  asaDeniedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut))));
    }

    // permitted, low-to-high (low has ingress filter)
    {
      List<TraceTree> traces = trace.apply(inLowFiltered, permitFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(asaPermitLowerSecurityLevelTraceElement(10)),
              isTraceTree(
                  asaPermittedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut),
                  isTraceTree(matchedByAclLine(filterOut, 0)))));
    }

    // denied, low-to-high (low has ingress filter)
    {
      List<TraceTree> traces = trace.apply(inLowFiltered, defaultDenyFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(asaPermitLowerSecurityLevelTraceElement(10)),
              isTraceTree(
                  asaDeniedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut))));
    }

    // permitted, high-to-low
    {
      List<TraceTree> traces = trace.apply(inHigh, permitFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(asaPermitHigherSecurityLevelTrafficTraceElement(100)),
              isTraceTree(
                  asaPermittedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut),
                  isTraceTree(matchedByAclLine(filterOut, 0)))));
    }

    // denied, high-to-low
    {
      List<TraceTree> traces = trace.apply(inHigh, defaultDenyFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(asaPermitHigherSecurityLevelTrafficTraceElement(100)),
              isTraceTree(
                  asaDeniedByOutputFilterTraceElement(
                      "configs/asa-security-level-permit-tracing", filterOut))));
    }
  }

  /**
   * Test the traces AclTracer produces for ASA security-levels, when the security-level policy
   * denies
   */
  @Test
  public void testAsaSecurityLevelDenyTracing() throws IOException {
    Configuration c = parseConfig("asa-security-level-deny-tracing");
    String out = "out";
    String inSameLevel = "inSameLevel";
    String inLowUnfiltered = "inLowUnfiltered";

    Flow flow = createFlow(IpProtocol.TCP, 0, 123);

    // out is always the egress interface
    IpAccessList filter = c.getAllInterfaces().get(out).getPreTransformationOutgoingFilter();
    Function<String, List<TraceTree>> trace =
        (inIface) ->
            AclTracer.trace(
                filter,
                flow,
                inIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    // same security level, intra-interface (hairpinning)
    {
      List<TraceTree> traces = trace.apply(out);
      assertThat(traces, contains(isTraceTree(DENY_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT)));
    }

    // same security level, inter-interface
    {
      List<TraceTree> traces = trace.apply(inSameLevel);
      assertThat(traces, contains(isTraceTree(DENY_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT)));
    }

    // lower security level, no ingress filter
    {
      List<TraceTree> traces = trace.apply(inLowUnfiltered);
      assertThat(traces, contains(isTraceTree(asaRejectLowerSecurityLevelTraceElement(10))));
    }
  }

  @Test
  public void testAsaSecurityLevelPermitInter() throws IOException {
    Configuration c = parseConfig("asa-security-level-permit-inter");
    String ifaceAlias1 = "name1";
    String ifaceAlias2 = "name2";
    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    // No traffic in and out of the same interface
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(rejects(newFlow, ifaceAlias1, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(rejects(newFlow, ifaceAlias2, c))));

    // Allow traffic between interfaces with same level
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias2, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias1, c))));
  }

  @Test
  public void testAsaSecurityLevelPermitIntra() throws IOException {
    Configuration c = parseConfig("asa-security-level-permit-intra");
    String ifaceAlias1 = "name1";
    String ifaceAlias2 = "name2";
    Flow newFlow = createFlow(IpProtocol.OSPF, 0, 0);

    // Allow traffic in and out of the same interface
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias1, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(accepts(newFlow, ifaceAlias2, c))));

    // No traffic between interfaces with same level
    assertThat(
        c,
        hasInterface(
            ifaceAlias1, hasPreTransformationOutgoingFilter(rejects(newFlow, ifaceAlias2, c))));
    assertThat(
        c,
        hasInterface(
            ifaceAlias2, hasPreTransformationOutgoingFilter(rejects(newFlow, ifaceAlias1, c))));
  }

  @Test
  public void testAsaStaticRoute() throws IOException {
    Configuration c = parseConfig("asa-static-route");

    // Confirm static route is extracted properly
    assertThat(
        c,
        hasVrf(
            "default",
            hasStaticRoutes(
                equalTo(
                    ImmutableSet.of(
                        StaticRoute.testBuilder()
                            .setNextHopIp(Ip.parse("3.0.0.1"))
                            .setNetwork(Prefix.parse("0.0.0.0/0"))
                            .setNextHopInterface("ifname")
                            .setAdministrativeCost(2)
                            .build(),
                        StaticRoute.testBuilder()
                            .setNextHopIp(Ip.parse("3.0.0.2"))
                            .setNetwork(Prefix.parse("1.0.0.0/8"))
                            .setNextHopInterface("ifname")
                            .setAdministrativeCost(3)
                            .build())))));
  }

  @Test
  public void testAsaObjectNatDynamic() throws IOException {
    // Test Transformations from ASA dynamic object NATs
    String hostname = "asa-nat-object-dynamic";
    Configuration config = parseConfig(hostname);

    Transformation.Builder nat1 =
        when(and(
                matchSrc(
                    new IpSpaceReference("source-real-1", "Match network object: 'source-real-1'")),
                matchSrcInterface("inside")))
            .apply(assignSourceIp(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.10")));
    Transformation.Builder nat2 =
        when(and(
                matchSrc(
                    new IpSpaceReference("source-real-2", "Match network object: 'source-real-2'")),
                matchSrcInterface("outside")))
            .apply(assignSourceIp(Ip.parse("192.168.2.1"), Ip.parse("192.168.2.10")));
    Transformation.Builder nat3 =
        when(and(
                matchSrc(
                    new IpSpaceReference("source-real-3", "Match network object: 'source-real-3'")),
                matchSrcInterface("inside")))
            .apply(assignSourceIp(Ip.parse("192.168.3.1"), Ip.parse("192.168.3.10")));
    Transformation.Builder nat4 =
        when(matchSrc(
                new IpSpaceReference("source-real-4", "Match network object: 'source-real-4'")))
            .apply(assignSourceIp(Ip.parse("192.168.4.1"), Ip.parse("192.168.4.10")));

    Transformation nat = config.getAllInterfaces().get("inside").getIncomingTransformation();
    assertThat(nat, nullValue());

    // 'inside' is the mapped interface for nat rules 2, 3, and 4. Sorted as 2->3->4
    nat = config.getAllInterfaces().get("inside").getOutgoingTransformation();
    assertThat(nat, equalTo(nat2.setOrElse(nat3.setOrElse(nat4.build()).build()).build()));

    nat = config.getAllInterfaces().get("outside").getIncomingTransformation();
    assertThat(nat, nullValue());

    // 'outside' is the mapped interface for nat rules 1, 3, and 4. Sorted as 1->3->4
    nat = config.getAllInterfaces().get("outside").getOutgoingTransformation();
    assertThat(nat, equalTo(nat1.setOrElse(nat3.setOrElse(nat4.build()).build()).build()));

    nat = config.getAllInterfaces().get("other").getIncomingTransformation();
    assertThat(nat, nullValue());

    // 'other' is the mapped interface for nat rules 3 and 4. Sorted as 3->4
    nat = config.getAllInterfaces().get("other").getOutgoingTransformation();
    assertThat(nat, equalTo(nat3.setOrElse(nat4.build()).build()));
  }

  @Test
  public void testAsaObjectNatReferrers() throws IOException {
    String hostname = "asa-nat-object-refs";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // check expected references
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "inside", 4));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "outside", 3));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "other", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-mapped-1", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-mapped-2", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-1", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-2", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-3", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-4", 1));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-invalid", 1));

    assertThat(ccae, hasUndefinedReference(filename, INTERFACE, "dmz"));
    assertThat(ccae, hasUndefinedReference(filename, INTERFACE, "mgmt"));
    assertThat(ccae, hasUndefinedReference(filename, NETWORK_OBJECT, "source-mapped-undef"));
    assertThat(ccae, not(hasUndefinedReference(filename, INTERFACE, "inside")));
    assertThat(ccae, not(hasUndefinedReference(filename, INTERFACE, "outside")));
    assertThat(ccae, not(hasUndefinedReference(filename, INTERFACE, "other")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-mapped-1")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-mapped-2")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-1")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-2")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-3")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-4")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-invalid")));
  }

  @Test
  public void testAsaObjectNatStatic() throws IOException {
    // Test Transformations from ASA static object NATs
    String hostname = "asa-nat-object-static";
    Configuration config = parseConfig(hostname);

    Prefix realSource1 = Prefix.parse("1.1.1.1/32");
    Prefix realSource2 = Prefix.parse("2.2.2.0/29");
    Prefix realSource3 = Prefix.parse("3.3.3.3/32");
    Prefix realSource4 = Prefix.parse("4.4.4.0/24");
    Prefix mappedSource1 = Prefix.parse("192.168.1.1/32");
    Prefix mappedSource2 = Prefix.parse("192.168.2.0/29");
    Prefix mappedSource3 = Prefix.parse("192.168.3.0/32");
    Prefix mappedSource4 = Prefix.parse("192.168.4.0/24");

    Transformation.Builder natIn1 =
        when(matchDst(mappedSource1)).apply(shiftDestinationIp(realSource1));
    Transformation.Builder natIn2 =
        when(matchDst(mappedSource2)).apply(shiftDestinationIp(realSource2));
    Transformation.Builder natIn3 =
        when(matchDst(mappedSource3)).apply(shiftDestinationIp(realSource3));
    Transformation.Builder natIn4 =
        when(matchDst(mappedSource4)).apply(shiftDestinationIp(realSource4));
    Transformation.Builder natOut1 =
        when(and(matchSrc(realSource1), matchSrcInterface("inside")))
            .apply(shiftSourceIp(mappedSource1));
    Transformation.Builder natOut2 =
        when(and(matchSrc(realSource2), matchSrcInterface("outside")))
            .apply(shiftSourceIp(mappedSource2));
    Transformation.Builder natOut3 =
        when(and(matchSrc(realSource3), matchSrcInterface("inside")))
            .apply(shiftSourceIp(mappedSource3));
    Transformation.Builder natOut4 =
        when(matchSrc(realSource4)).apply(shiftSourceIp(mappedSource4));

    // 'inside' is the mapped interface for nat rules 2, 3, and 4. Sorted as 3->2->4.
    Transformation nat = config.getAllInterfaces().get("inside").getIncomingTransformation();
    assertThat(nat, equalTo(natIn3.setOrElse(natIn2.setOrElse(natIn4.build()).build()).build()));

    nat = config.getAllInterfaces().get("inside").getOutgoingTransformation();
    assertThat(nat, equalTo(natOut3.setOrElse(natOut2.setOrElse(natOut4.build()).build()).build()));

    // 'outside' is the mapped interface for nat rules 1, 3, and 4. Sorted as 1->3->4
    nat = config.getAllInterfaces().get("outside").getIncomingTransformation();
    assertThat(nat, equalTo(natIn1.setOrElse(natIn3.setOrElse(natIn4.build()).build()).build()));

    nat = config.getAllInterfaces().get("outside").getOutgoingTransformation();
    assertThat(nat, equalTo(natOut1.setOrElse(natOut3.setOrElse(natOut4.build()).build()).build()));

    // 'other' is the mapped interface for nat rules 3 and 4. Sorted as 3->4
    nat = config.getAllInterfaces().get("other").getIncomingTransformation();
    assertThat(nat, equalTo(natIn3.setOrElse(natIn4.build()).build()));

    nat = config.getAllInterfaces().get("other").getOutgoingTransformation();
    assertThat(nat, equalTo(natOut3.setOrElse(natOut4.build()).build()));
  }

  @Test
  public void testAsaNatOrder() {
    String hostname = "asa-nat-mixed";
    AsaConfiguration config = parseVendorConfig(hostname);

    // Finishes extraction, necessary for object NATs
    config.setWarnings(new Warnings());
    config.setAnswerElement(new ConvertConfigurationAnswerElement());
    config.toVendorIndependentConfigurations();

    List<AsaNat> nats = config.getCiscoAsaNats();

    // CiscoAsaNats are comparable
    Collections.sort(nats);

    // Check that NATs are sorted by section
    assertThat(
        nats.stream().map(AsaNat::getSection).collect(Collectors.toList()),
        contains(
            Section.BEFORE,
            Section.BEFORE,
            Section.OBJECT,
            Section.OBJECT,
            Section.OBJECT,
            Section.OBJECT,
            Section.OBJECT,
            Section.OBJECT,
            Section.AFTER,
            Section.AFTER));

    // Check that twice NATs are sorted by line after section
    assertThat(
        nats.stream()
            .filter(nat2 -> nat2.getSection().equals(Section.BEFORE))
            .map(AsaNat::getLine)
            .collect(Collectors.toList()),
        contains(2, 3));
    assertThat(
        nats.stream()
            .filter(nat1 -> nat1.getSection().equals(Section.AFTER))
            .map(AsaNat::getLine)
            .collect(Collectors.toList()),
        contains(1, 4));

    List<AsaNat> objectNats =
        nats.stream()
            .filter(nat -> nat.getSection().equals(Section.OBJECT))
            .collect(Collectors.toList());

    // Check that object NATs are sorted static and then dynamic
    assertThat(
        objectNats.stream().map(AsaNat::getDynamic).collect(Collectors.toList()),
        contains(false, false, false, false, false, true));

    // Check that object NATs of a particular type (static) are sorted by their network objects
    // See CiscoAsaNat.compareTo or
    // https://www.cisco.com/c/en/us/td/docs/security/asa/asa910/configuration/firewall/asa-910-firewall-config/nat-basics.html#ID-2090-00000065
    assertThat(
        objectNats.stream()
            .filter(nat -> !nat.getDynamic())
            .map(AsaNat::getRealSourceObject)
            .map(NetworkObject::getName)
            .collect(Collectors.toList()),
        contains(
            "alphabetical",
            "source-static",
            "source-subnet29",
            "source-subnet29-b",
            "source-subnet24"));
  }

  @Test
  public void testAsaTwiceNatDynamic() {
    // Test vendor-specific parsing of ASA dynamic twice NATs
    String hostname = "asa-nat-twice-dynamic";
    AsaConfiguration config = parseVendorConfig(hostname);

    AclLineMatchExpr matchSourceSubnet =
        matchSrc(new IpSpaceReference("source-subnet", "Match network object: 'source-subnet'"));
    AclLineMatchExpr matchSourceGroup =
        matchSrc(
            new IpSpaceReference("source-group", "Match network object-group: 'source-group'"));
    AssignIpAddressFromPool assignSourceRange =
        assignSourceIp(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.10"));
    Prefix mappedDestination = Prefix.parse("3.3.3.3/32");
    Prefix realDestination = Prefix.parse("4.4.4.4/32");

    List<AsaNat> nats = config.getCiscoAsaNats();
    assertThat(nats, hasSize(4));

    // dynamic source NAT, subnet -> range
    AsaNat nat = config.getCiscoAsaNats().get(0);
    Optional<Transformation.Builder> builder =
        nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    Transformation twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSourceSubnet, matchSrcInterface("inside")))
                .apply(assignSourceRange)
                .build()));

    // dynamic source NAT and static destination NAT
    nat = config.getCiscoAsaNats().get(1);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(
                    and(matchSourceGroup, matchSrcInterface("inside")),
                    matchDst(mappedDestination)))
                .apply(ImmutableList.of(assignSourceRange, shiftDestinationIp(realDestination)))
                .build()));

    // dynamic source NAT, host -> range, no interfaces specified
    nat = config.getCiscoAsaNats().get(2);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(twice, equalTo(when(matchSourceGroup).apply(assignSourceRange).build()));

    // dynamic source NAT and static destination NAT, no interface specified
    nat = config.getCiscoAsaNats().get(3);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSourceSubnet, matchDst(mappedDestination)))
                .apply(ImmutableList.of(assignSourceRange, shiftDestinationIp(realDestination)))
                .build()));
  }

  @Test
  public void testAsaTwiceNatStatic() {
    // Test vendor-specific parsing of ASA static twice NATs
    String hostname = "asa-nat-twice-static";
    AsaConfiguration config = parseVendorConfig(hostname);

    List<AsaNat> nats = config.getCiscoAsaNats();
    assertThat(nats, hasSize(9));

    AsaNat nat = nats.get(0);
    assertThat(nat.getDynamic(), equalTo(false));
    assertThat(nat.getInsideInterface(), equalTo("inside"));
    assertThat(
        nat.getMappedDestination(), equalTo(new NetworkObjectAddressSpecifier("dest-mapped")));
    assertThat(nat.getMappedSource(), equalTo(new NetworkObjectAddressSpecifier("source-mapped")));
    assertThat(nat.getOutsideInterface(), equalTo("outside"));
    assertThat(nat.getRealDestination(), equalTo(new NetworkObjectAddressSpecifier("dest-real")));
    assertThat(nat.getRealSource(), equalTo(new NetworkObjectAddressSpecifier("source-real")));
    assertThat(nat.getTwice(), equalTo(true));

    nat = nats.get(1);
    assertThat(nat.getDynamic(), equalTo(false));
    assertThat(nat.getInsideInterface(), equalTo("inside"));
    assertThat(nat.getMappedSource(), equalTo(new NetworkObjectAddressSpecifier("source-mapped")));
    assertThat(nat.getOutsideInterface(), equalTo("outside"));
    assertThat(nat.getRealSource(), equalTo(new NetworkObjectAddressSpecifier("source-real")));
    assertThat(nat.getTwice(), equalTo(false));

    nat = nats.get(2);
    assertThat(nat.getDynamic(), equalTo(false));
    assertThat(nat.getTwice(), equalTo(false));
    assertThat(nat.getInsideInterface(), equalTo(AsaNat.ANY_INTERFACE));
    assertThat(nat.getOutsideInterface(), equalTo(AsaNat.ANY_INTERFACE));
    assertThat(
        nat.getRealSource(), equalTo(new NetworkObjectGroupAddressSpecifier("source-real-group")));
    assertThat(
        nat.getMappedSource(),
        equalTo(new NetworkObjectGroupAddressSpecifier("source-mapped-group")));

    nat = nats.get(3);
    assertTrue("NAT is active", nat.getInactive());
    assertThat(nat.getInsideInterface(), equalTo("inside"));
    assertThat(nat.getOutsideInterface(), equalTo(AsaNat.ANY_INTERFACE));

    nat = nats.get(4);
    assertThat(nat.getInsideInterface(), equalTo(AsaNat.ANY_INTERFACE));
    assertThat(nat.getOutsideInterface(), equalTo("outside"));

    nat = nats.get(5);
    assertThat(nat.getInsideInterface(), equalTo("outside"));
    assertThat(nat.getOutsideInterface(), equalTo("inside"));
    assertThat(nat.getRealSource(), equalTo(new WildcardAddressSpecifier(IpWildcard.ANY)));
    assertThat(nat.getMappedSource(), equalTo(new WildcardAddressSpecifier(IpWildcard.ANY)));

    nat = nats.get(6);
    assertThat(
        nat.getRealSource(), equalTo(new NetworkObjectAddressSpecifier("source-real-subnet")));
    assertThat(
        nat.getMappedSource(), equalTo(new NetworkObjectAddressSpecifier("source-mapped-subnet")));
  }

  @Test
  public void testAsaTwiceNatStaticSource() {
    // Test ASA twice NAT with source only
    String hostname = "asa-nat-twice-static";
    AsaConfiguration config = parseVendorConfig(hostname);

    Prefix realSourceHost = Prefix.parse("1.1.1.1/32");
    Prefix mappedSourceHost = Prefix.parse("2.2.2.2/32");
    Prefix mappedDestHost = Prefix.parse("3.3.3.3/32");
    Prefix realDestHost = Prefix.parse("4.4.4.4/32");
    Prefix realSourceSubnet = Prefix.parse("5.5.5.0/24");
    Prefix mappedSourceSubnet = Prefix.parse("6.6.6.0/24");

    // Host source NAT outgoing
    AsaNat nat = config.getCiscoAsaNats().get(1);
    Optional<Transformation.Builder> builder =
        nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    Transformation twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSrc(realSourceHost), matchSrcInterface("inside")))
                .apply(shiftSourceIp(mappedSourceHost))
                .build()));

    // Host source NAT incoming
    builder = nat.toIncomingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No incoming transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(matchDst(mappedSourceHost)).apply(shiftDestinationIp(realSourceHost)).build()));

    // Identity NAT
    nat = config.getCiscoAsaNats().get(5);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSrc(Prefix.ZERO), matchSrcInterface("outside")))
                .apply(shiftSourceIp(Prefix.ZERO))
                .build()));

    // Subnet source NAT
    nat = config.getCiscoAsaNats().get(6);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSrc(realSourceSubnet), matchSrcInterface("inside")))
                .apply(shiftSourceIp(mappedSourceSubnet))
                .build()));

    // Source NAT with no interfaces specified
    nat = config.getCiscoAsaNats().get(7);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSrc(realSourceSubnet)))
                .apply(shiftSourceIp(mappedSourceSubnet))
                .build()));
    builder = nat.toIncomingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No incoming transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(matchDst(mappedSourceSubnet))
                .apply(shiftDestinationIp(realSourceSubnet))
                .build()));

    // Source + destination NAT with no interfaces specified
    nat = config.getCiscoAsaNats().get(8);
    builder = nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchSrc(realSourceSubnet), matchDst(mappedDestHost)))
                .apply(
                    ImmutableList.of(
                        shiftSourceIp(mappedSourceSubnet), shiftDestinationIp(realDestHost)))
                .build()));
    builder = nat.toIncomingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No incoming transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchDst(mappedSourceSubnet), matchSrc(realDestHost)))
                .apply(
                    ImmutableList.of(
                        shiftDestinationIp(realSourceSubnet), shiftSourceIp(mappedDestHost)))
                .build()));
  }

  @Test
  public void testAsaTwiceNatStaticSourceAndDestination() {
    // Test ASA twice NAT with source and destination
    String hostname = "asa-nat-twice-static";
    AsaConfiguration config = parseVendorConfig(hostname);

    Prefix realSource = Prefix.parse("1.1.1.1/32");
    Prefix mappedSource = Prefix.parse("2.2.2.2/32");
    Prefix mappedDestination = Prefix.parse("3.3.3.3/32");
    Prefix realDestination = Prefix.parse("4.4.4.4/32");

    AsaNat nat = config.getCiscoAsaNats().get(0);
    Optional<Transformation.Builder> builder =
        nat.toOutgoingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No outgoing transformation", builder.isPresent(), equalTo(true));
    Transformation twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(
                    and(matchSrc(realSource), matchSrcInterface("inside")),
                    matchDst(mappedDestination)))
                .apply(
                    ImmutableList.of(
                        shiftSourceIp(mappedSource), shiftDestinationIp(realDestination)))
                .build()));

    builder = nat.toIncomingTransformation(config.getNetworkObjects(), new Warnings());
    assertThat("No incoming transformation", builder.isPresent(), equalTo(true));
    twice = builder.get().build();
    assertThat(
        twice,
        equalTo(
            when(and(matchDst(mappedSource), matchSrc(realDestination)))
                .apply(
                    ImmutableList.of(
                        shiftDestinationIp(realSource), shiftSourceIp(mappedDestination)))
                .build()));
  }

  @Test
  public void testAsaTwiceNatReferrers() throws IOException {
    String hostname = "asa-nat-twice-static";
    String filename = "configs/" + hostname;

    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // check expected references
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "inside", 6));
    assertThat(ccae, hasNumReferrers(filename, INTERFACE, "outside", 6));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "dest-mapped", 2));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "dest-real", 2));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-mapped", 3));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-mapped-subnet", 3));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real", 3));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT, "source-real-subnet", 3));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "source-mapped-group", 2));
    assertThat(ccae, hasNumReferrers(filename, NETWORK_OBJECT_GROUP, "source-real-group", 2));

    assertThat(ccae, not(hasUndefinedReference(filename, INTERFACE, "inside")));
    assertThat(ccae, not(hasUndefinedReference(filename, INTERFACE, "outside")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "dest-mapped")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "dest-real")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-mapped")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-mapped-subnet")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real")));
    assertThat(ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT, "source-real-subnet")));
    assertThat(
        ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT_GROUP, "source-mapped-group")));
    assertThat(
        ccae, not(hasUndefinedReference(filename, NETWORK_OBJECT_GROUP, "source-real-group")));
    assertThat(ccae, hasUndefinedReference(filename, NETWORK_OBJECT, "undef-source-mapped"));
    assertThat(ccae, hasUndefinedReference(filename, NETWORK_OBJECT, "undef-source-real"));
  }

  @Test
  public void testAsaSession() throws IOException {
    Configuration c = parseConfig("asa-session");
    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder("inside", "outside"));
    FirewallSessionInterfaceInfo inside =
        c.getAllInterfaces().get("inside").getFirewallSessionInterfaceInfo();
    FirewallSessionInterfaceInfo outside =
        c.getAllInterfaces().get("outside").getFirewallSessionInterfaceInfo();
    // Confirm that each interface has the correct, interface-name-specific firewall session info
    // attached to it
    assertThat(
        inside,
        equalTo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of("inside"), null, null)));
    assertThat(
        outside,
        equalTo(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of("outside"), null, null)));
  }
}
