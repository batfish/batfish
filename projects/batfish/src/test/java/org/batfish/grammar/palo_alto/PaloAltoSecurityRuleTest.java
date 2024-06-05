package org.batfish.grammar.palo_alto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.PANORAMA_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.aclLineMatchExprForApplication;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchAddressAnyTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchAddressGroupTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchAddressObjectTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchAddressValueTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchApplicationAnyTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchApplicationGroupTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchApplicationOverrideRuleTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchBuiltInApplicationTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchBuiltInServiceTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchDestinationAddressTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchNegatedDestinationAddressTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchSecurityRuleTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchServiceAnyTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchServiceApplicationDefaultTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchServiceTraceElement;
import static org.batfish.representation.palo_alto.PaloAltoTraceElementCreators.matchSourceAddressTraceElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.palo_alto.Application;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.representation.palo_alto.SecurityRule;
import org.batfish.representation.palo_alto.Service;
import org.batfish.representation.palo_alto.Vsys;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PaloAltoSecurityRuleTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      assertThat(configs, hasKey(hostname));
      return configs.get(hostname);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private PaloAltoConfiguration parsePaloAltoConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    PaloAltoCombinedParser parser = new PaloAltoCombinedParser(src, settings, null);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    PaloAltoControlPlaneExtractor extractor =
        new PaloAltoControlPlaneExtractor(
            src, parser, new Warnings(), new SilentSyntaxCollection());
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
    pac.setVendor(ConfigurationFormat.PALO_ALTO);
    pac.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    pac = SerializationUtils.clone(pac);
    pac.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    return pac;
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

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testDeviceGroupSharedInheritance() throws IOException {
    String panoramaHostname = "device-group-shared-inheritance";
    String firewallId = "00000001";
    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();
    Configuration firewallConfig =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId)).findFirst().get();

    Batfish batfish =
        getBatfish(ImmutableSortedMap.of(firewallConfig.getHostname(), firewallConfig), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    String if1name = "ethernet1/1"; // 192.168.0.1/16
    Builder baseFlow =
        Flow.builder()
            .setIngressNode(firewallConfig.getHostname())
            // Arbitrary ports and protocol
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    // This flow matches overridden ADDR2 source address
    Flow flowReject =
        baseFlow
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("192.168.1.2"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .build();
    // This flow matches shared ADDR3 source address
    Flow flowPermit =
        baseFlow
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("192.168.2.3"))
            .setDstIp(Ip.parse("10.0.0.2"))
            .build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flowPermit, flowReject), false);

    // Rejected due to hitting shared pre-rulebase rule (deny) before device-group rule (allow)
    assertFalse(traces.get(flowReject).get(0).getDisposition().isSuccessful());
    // Permitted due to hitting device-group post-rulebase rule (allow) before any deny rule
    assertTrue(traces.get(flowPermit).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testDeviceGroupInheritance() throws IOException {
    String panoramaHostname = "device-group-inheritance";
    String firewallId = "00000002";
    Ip parentAddr1 = Ip.parse("10.10.2.21");
    Ip parentAddr2 = Ip.parse("10.10.2.22");

    PaloAltoConfiguration c = parsePaloAltoConfig(panoramaHostname);
    List<Configuration> viConfigs = c.toVendorIndependentConfigurations();
    Configuration firewallConfig =
        viConfigs.stream().filter(vi -> vi.getHostname().equals(firewallId)).findFirst().get();
    Batfish batfish =
        getBatfish(ImmutableSortedMap.of(firewallConfig.getHostname(), firewallConfig), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    String if1name = "ethernet1/1"; // 192.168.0.1/16
    Builder baseFlow =
        Flow.builder()
            .setIngressNode(firewallConfig.getHostname())
            .setIngressInterface(if1name)
            // Arbitrary ports and protocol
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow flowParentAddr1 = baseFlow.setDstIp(parentAddr1).build();
    Flow flowParentAddr2 = baseFlow.setDstIp(parentAddr2).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flowParentAddr1, flowParentAddr2), false);

    // Firewall 00000002 should have the following security rules in the following order:
    // Pre-rule (parent)        permit addr1
    // Pre-rule (child)         deny addr1
    // Post-rule (child)        permit addr2
    // Post-rule (parent)       deny addr2

    // Child pre-rulebase deny should come after parent pre-rulebase allow
    assertThat(
        traces.get(flowParentAddr1).get(0).getDisposition(),
        equalTo(FlowDisposition.DELIVERED_TO_SUBNET));

    // Child post-rulebase allow should come before parent post-rulebase deny
    assertThat(
        traces.get(flowParentAddr2).get(0).getDisposition(),
        equalTo(FlowDisposition.DELIVERED_TO_SUBNET));
  }

  @Test
  public void testApplicationAny() throws IOException {
    String hostname = "any-application";
    Configuration c = parseConfig(hostname);

    String if1name = "ethernet1/1"; // 1.1.1.1/24
    String if2name = "ethernet1/2"; // 2.2.2.2/24
    Builder baseFlow =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    // This flow matches from and to zones in security rule
    Flow flowPermit =
        baseFlow
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("1.1.1.3"))
            .setDstIp(Ip.parse("2.2.2.3"))
            .build();
    // This flow does not match from or to zones in security rule
    Flow flowReject =
        baseFlow
            .setIngressInterface(if2name)
            .setSrcIp(Ip.parse("2.2.2.3"))
            .setDstIp(Ip.parse("1.1.1.3"))
            .build();

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flowPermit, flowReject), false);

    // Confirm flow not matching rule (bad zone) is rejected
    assertFalse(traces.get(flowReject).get(0).getDisposition().isSuccessful());
    // Confirm flow from correct zone is accepted, matching rule w/ application = any and
    // service = application-default
    assertTrue(traces.get(flowPermit).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testRulebaseTracing() {
    String hostname = "rulebase-tracing";
    String filename = "configs/" + hostname;
    Configuration c = parseConfig(hostname);
    String iface1 = "ethernet1/1";
    String crossZoneFilterName =
        zoneToZoneFilter(computeObjectName("vsys1", "z1"), computeObjectName("vsys1", "z2"));

    Flow rule1aFlow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 1);
    Flow rule1bFlow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 443);
    Flow rule2aFlow = createFlow("1.1.4.10", "1.1.1.10", IpProtocol.TCP, 0, 53);
    Flow rule2bFlow = createFlow("1.1.4.10", "1.1.1.10", IpProtocol.TCP, 0, 179);
    Flow rule3Flow = createFlow("1.1.4.10", "1.1.1.10", IpProtocol.TCP, 0, 1234);
    Flow rule4Flow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 53);
    Flow rule3pt5Flow = createFlow("1.1.1.10", "10.12.14.16", IpProtocol.TCP, 0, 80);

    IpAccessList filter = c.getIpAccessLists().get(crossZoneFilterName);
    BiFunction<String, Flow, List<TraceTree>> trace =
        (inIface, flow) ->
            AclTracer.trace(
                filter,
                flow,
                inIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    {
      List<TraceTree> traces = trace.apply(iface1, rule1aFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE1", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(matchAddressGroupTraceElement("addr_group1", "vsys1", filename))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressObjectTraceElement("addr2", "vsys1", filename))),
                  isTraceTree(matchApplicationAnyTraceElement()),
                  isTraceTree(matchServiceTraceElement()))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule1bFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE1", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(matchAddressGroupTraceElement("addr_group1", "vsys1", filename))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressObjectTraceElement("addr2", "vsys1", filename))),
                  isTraceTree(matchApplicationAnyTraceElement()),
                  isTraceTree(matchBuiltInServiceTraceElement()))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule2aFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE2", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.4.10/32"))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.1.10"))),
                  isTraceTree(
                      matchServiceApplicationDefaultTraceElement(),
                      isTraceTree(
                          matchApplicationGroupTraceElement("app_group1", "vsys1", filename))))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule2bFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE2", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.4.10/32"))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.1.10"))),
                  isTraceTree(
                      matchServiceApplicationDefaultTraceElement(),
                      isTraceTree(matchBuiltInApplicationTraceElement("bgp"))))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule3Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE3", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.4.10/32"))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("1.1.1.10"))),
                  isTraceTree(
                      matchServiceApplicationDefaultTraceElement(),
                      isTraceTree(matchApplicationAnyTraceElement())))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule3pt5Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE3pt5", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(), isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressValueTraceElement("10.12.14.16"))),
                  isTraceTree(matchBuiltInApplicationTraceElement("aol-messageboard-posting")),
                  isTraceTree(matchBuiltInServiceTraceElement()))));
    }
    {
      List<TraceTree> traces = trace.apply(iface1, rule4Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE4", "vsys1", filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(), isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(matchNegatedDestinationAddressTraceElement()),
                  isTraceTree(matchApplicationAnyTraceElement()),
                  isTraceTree(matchServiceAnyTraceElement()))));
    }
  }

  /**
   * Test that trace elements contain structure data pointing to structures in the correct
   * namespace.
   */
  @Test
  public void testPanoramaRulebaseTracing() throws IOException {
    String hostname = "panorama-rulebase-tracing";
    String filename = "configs/" + hostname;
    Configuration c = parseTextConfigs(hostname).get("00000001");
    String iface1 = "ethernet1/1";
    String crossZoneFilterName =
        zoneToZoneFilter(computeObjectName("vsys1", "z1"), computeObjectName("vsys1", "z2"));

    Flow rule1Flow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 1);
    Flow rule2Flow = createFlow("1.1.4.10", "1.1.1.10", IpProtocol.TCP, 0, 2);
    Flow rule3Flow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 53);
    Flow rule4Flow = createFlow("1.1.1.10", "1.1.4.10", IpProtocol.TCP, 0, 179);

    IpAccessList filter = c.getIpAccessLists().get(crossZoneFilterName);
    BiFunction<String, Flow, List<TraceTree>> trace =
        (inIface, flow) ->
            AclTracer.trace(
                filter,
                flow,
                inIface,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    {
      // Shared address object and service
      List<TraceTree> traces = trace.apply(iface1, rule1Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE1", PANORAMA_VSYS_NAME, filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(
                          matchAddressObjectTraceElement("addr1", SHARED_VSYS_NAME, filename))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(matchApplicationAnyTraceElement()),
                  isTraceTree(matchServiceTraceElement()))));
    }
    {
      // Device-group address object and service
      List<TraceTree> traces = trace.apply(iface1, rule2Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE2", PANORAMA_VSYS_NAME, filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(),
                      isTraceTree(
                          matchAddressObjectTraceElement("addr2", PANORAMA_VSYS_NAME, filename))),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(matchApplicationAnyTraceElement()),
                  isTraceTree(matchServiceTraceElement()))));
    }
    {
      // Shared application-group
      List<TraceTree> traces = trace.apply(iface1, rule3Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE3", PANORAMA_VSYS_NAME, filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(), isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(
                      matchServiceApplicationDefaultTraceElement(),
                      isTraceTree(
                          matchApplicationGroupTraceElement(
                              "app_group1", SHARED_VSYS_NAME, filename))))));
    }
    {
      // Device-group application-group
      List<TraceTree> traces = trace.apply(iface1, rule4Flow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  matchSecurityRuleTraceElement("RULE4", PANORAMA_VSYS_NAME, filename),
                  isTraceTree(
                      matchSourceAddressTraceElement(), isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(
                      matchDestinationAddressTraceElement(),
                      isTraceTree(matchAddressAnyTraceElement())),
                  isTraceTree(
                      matchServiceApplicationDefaultTraceElement(),
                      isTraceTree(
                          matchApplicationGroupTraceElement(
                              "app_group2", PANORAMA_VSYS_NAME, filename))))));
    }
  }

  @Test
  public void testSecurityRules() throws IOException {
    String hostname = "security-rules";
    Configuration c = parseConfig(hostname);

    int customAppPort = 1234;
    String if1name = "ethernet1/1"; // 10.0.1.1/24
    Builder baseFlow =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"));

    Flow flowPermitZ2 =
        baseFlow
            .setDstPort(customAppPort)
            // Destined for z2, which allows this traffic
            .setDstIp(Ip.parse("10.0.2.2"))
            .build();
    Flow flowPermitZ4 =
        flowPermitZ2.toBuilder()
            // Destined for z4, which allows this traffic
            .setDstIp(Ip.parse("10.0.2.2"))
            .build();
    Flow flowReject =
        baseFlow
            .setDstPort(customAppPort)
            // Destined for z3, which has a deny rule for this traffic
            .setDstIp(Ip.parse("10.0.3.2"))
            .build();

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flowPermitZ2, flowPermitZ4, flowReject), false);

    // Confirm flow matching deny rule (matching rejected to-zone) is not successful
    assertEquals(traces.get(flowReject).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    // Confirm flows matching allow rule (permitted to-zone) are successful
    assertEquals(
        traces.get(flowPermitZ2).get(0).getDisposition(), FlowDisposition.DELIVERED_TO_SUBNET);
    assertEquals(
        traces.get(flowPermitZ4).get(0).getDisposition(), FlowDisposition.DELIVERED_TO_SUBNET);
  }

  @Test
  public void testApplicationOverrideConditions() throws IOException {
    String hostname = "application-override-conditions";
    Configuration c = parseConfig(hostname);

    int customApp1Port = 7653;
    String if1name = "ethernet1/1"; // 10.0.1.1/24
    String if3name = "ethernet1/3"; // 10.0.3.1/24

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flowPermit =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .setDstIp(Ip.parse("10.0.2.2"))
            .setDstPort(customApp1Port)
            .build();
    Flow flowRejectPort =
        flowPermit.toBuilder()
            // Some dest port other than our custom app port
            .setDstPort(customApp1Port - 1)
            .build();
    Flow flowRejectFromZone = flowPermit.toBuilder().setIngressInterface(if3name).build();
    // Dest IP corresponding to a different zone (but still permitted by dest filter)
    Flow flowRejectToZone = flowPermit.toBuilder().setDstIp(Ip.parse("10.0.1.3")).build();
    Flow flowRejectSrc = flowPermit.toBuilder().setSrcIp(Ip.parse("10.0.1.3")).build();
    Flow flowRejectDst = flowPermit.toBuilder().setSrcIp(Ip.parse("10.0.2.3")).build();
    Flow flowRejectProtocol = flowPermit.toBuilder().setIpProtocol(IpProtocol.UDP).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    flowPermit,
                    flowRejectPort,
                    flowRejectFromZone,
                    flowRejectToZone,
                    flowRejectSrc,
                    flowRejectDst,
                    flowRejectProtocol),
                false);

    // Confirm flows not matching the rule are denied out
    assertEquals(traces.get(flowRejectPort).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(
        traces.get(flowRejectFromZone).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(traces.get(flowRejectToZone).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(traces.get(flowRejectSrc).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(traces.get(flowRejectDst).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(
        traces.get(flowRejectProtocol).get(0).getDisposition(), FlowDisposition.DENIED_OUT);

    // Confirm flow matching rule is successful
    assertTrue(traces.get(flowPermit).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testApplicationOverrideShadowing() throws IOException {
    String hostname = "application-override-shadowing";
    Configuration c = parseConfig(hostname);

    int customApp1Port = 7653;
    int customApp4abPort = 6542;
    int customApp4cPort = 5431;
    int sshPort = 22;
    String if1name = "ethernet1/1"; // 10.0.1.1/24
    String if3name = "ethernet1/3"; // 10.0.3.1/24

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // Matches OVERRIDE_APP_RULE1 (app CUSTOM_APP1)
    Flow flowCustomApp1 =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setDstPort(customApp1Port)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .setDstIp(Ip.parse("10.0.2.2"))
            .build();
    // Matches OVERRIDE_APP_RULE2 (app CUSTOM_APP2)
    Flow flowCustomApp2 = flowCustomApp1.toBuilder().setDstPort(sshPort).build();
    // Similar to CUSTOM_APP1, but matches OVERRIDE_APP_RULE3 (app CUSTOM_APP3) due to source addr
    Flow flowCustomApp3 = flowCustomApp1.toBuilder().setSrcIp(Ip.parse("1.0.1.2")).build();
    // Similar to CUSTOM_APP2, but matches app SSH due to source addr
    Flow flowSSH = flowCustomApp2.toBuilder().setSrcIp(Ip.parse("1.0.1.2")).build();

    // Match one source address for CUSTOM_APP4
    Flow flowCustomApp4a =
        flowCustomApp1.toBuilder()
            .setDstPort(customApp4abPort)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .build();
    // Match another source address for CUSTOM_APP4
    Flow flowCustomApp4b =
        flowCustomApp1.toBuilder()
            .setDstPort(customApp4abPort)
            .setSrcIp(Ip.parse("10.0.1.3"))
            .build();
    // Match different zone and port for CUSTOM_APP4
    Flow flowCustomApp4c =
        flowCustomApp1.toBuilder()
            .setIngressInterface(if3name)
            .setDstPort(customApp4cPort)
            .setSrcIp(Ip.parse("10.0.3.3"))
            .build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    flowCustomApp1,
                    flowCustomApp2,
                    flowCustomApp3,
                    flowSSH,
                    flowCustomApp4a,
                    flowCustomApp4b,
                    flowCustomApp4c),
                false);

    // Test application-override rule shadowing another application-override rule
    // Flow is flagged as CUSTOM_APP1
    assertEquals(traces.get(flowCustomApp1).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    // Similar to CUSTOM_APP1 but w/ different source addr, so flagged as CUSTOM_APP3
    assertTrue(traces.get(flowCustomApp3).get(0).getDisposition().isSuccessful());

    // Test application-override rule shadowing a built-in application
    // Flow is flagged as CUSTOM_APP2
    assertEquals(traces.get(flowCustomApp2).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    // Similar to CUSTOM_APP2, but w/ different source addr, so flagged as SSH (not a custom app)
    assertTrue(traces.get(flowSSH).get(0).getDisposition().isSuccessful());

    // Confirm application definition can come from multiple application-override rules
    // Flow matching OVERRIDE_APP_RULE4 (CUSTOM_APP4) is allowed
    assertTrue(traces.get(flowCustomApp4a).get(0).getDisposition().isSuccessful());
    // Flow matching OVERRIDE_APP_RULE5 (CUSTOM_APP4) is allowed, not shadowed
    assertTrue(traces.get(flowCustomApp4b).get(0).getDisposition().isSuccessful());
    // Flow matching OVERRIDE_APP_RULE6 (CUSTOM_APP4) is allowed, not shadowed
    assertTrue(traces.get(flowCustomApp4c).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testApplicationOverrideNatInteraction() throws IOException {
    String hostname = "application-override-nat-interaction";
    Configuration c = parseConfig(hostname);

    int customAppDnatPort = 7653;
    int customAppSnatPort = 6542;
    String if1name = "ethernet1/1"; // 10.0.1.1/24

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flowCustomAppDnat =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setDstPort(customAppDnatPort)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .setDstIp(Ip.parse("10.0.3.100"))
            .build();

    Flow flowCustomAppSnat =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setDstPort(customAppSnatPort)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .setDstIp(Ip.parse("10.0.2.2"))
            .build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(ImmutableSet.of(flowCustomAppDnat, flowCustomAppSnat), false);

    // Confirm CUSTOM_APP_DNAT is recognized, matching pre-DNAT addr and post-NAT zone
    assertTrue(traces.get(flowCustomAppDnat).get(0).getDisposition().isSuccessful());

    // Confirm CUSTOM_APP_SNAT flow is recognized, matching pre-SNAT addr & post-NAT zone
    assertTrue(traces.get(flowCustomAppSnat).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testApplicationOverrideServiceInteraction() throws IOException {
    String hostname = "application-override-service-interaction";
    Configuration c = parseConfig(hostname);

    int customApp7652Port = 7652;
    int customApp7653Port = 7653;
    String if1name = "ethernet1/1"; // 10.0.1.1/24
    String if3name = "ethernet1/3"; // 10.0.3.1/24
    String if4name = "ethernet1/4"; // 10.0.4.1/24

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // z1 -> z2 flow matching application and service
    Flow flowZ1toZ2 =
        Flow.builder()
            .setIngressNode(c.getHostname())
            // Arbitrary source port
            .setSrcPort(12345)
            .setDstPort(customApp7653Port)
            .setIpProtocol(IpProtocol.TCP)
            .setIngressInterface(if1name)
            .setSrcIp(Ip.parse("10.0.1.2"))
            .setDstIp(Ip.parse("10.0.2.2"))
            .build();

    // z3 -> z2 flow matching application, *not* service
    Flow flowZ3toZ2MatchApp =
        flowZ1toZ2.toBuilder()
            .setIngressInterface(if3name)
            .setSrcIp(Ip.parse("10.0.3.2"))
            .setDstPort(customApp7652Port)
            .build();
    // z3 -> z2 flow matching service, *not* application
    Flow flowZ3toZ2MatchService =
        flowZ3toZ2MatchApp.toBuilder().setDstPort(customApp7653Port).build();

    // z4 -> z2 flow matching service, *not* built-in application default port
    Flow flowZ4toZ2MatchService =
        flowZ3toZ2MatchService.toBuilder()
            .setIngressInterface(if4name)
            .setSrcIp(Ip.parse("10.0.4.2"))
            .setDstPort(customApp7653Port)
            .build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    flowZ1toZ2, flowZ3toZ2MatchApp, flowZ3toZ2MatchService, flowZ4toZ2MatchService),
                false);

    // Flow matching service and application-override rule should be permitted
    assertTrue(traces.get(flowZ1toZ2).get(0).getDisposition().isSuccessful());

    // Flow matching service should be permitted
    // Even if it doesn't match built-in application default port
    assertTrue(traces.get(flowZ4toZ2MatchService).get(0).getDisposition().isSuccessful());

    // Flow matching service or overridden application (but not both) should not be permitted
    assertEquals(
        traces.get(flowZ3toZ2MatchApp).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
    assertEquals(
        traces.get(flowZ3toZ2MatchService).get(0).getDisposition(), FlowDisposition.DENIED_OUT);
  }

  /** Test that application-groups with members in different namespaces are handled correctly. */
  @Ignore("https://github.com/batfish/batfish/issues/7698")
  @Test
  public void testPanoramaRulebaseMixedNamespace() throws IOException {
    String hostname = "panorama-rulebase-mixed-namespace";
    Configuration c = parseTextConfigs(hostname).get("00000001");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow flowDeviceApplication =
        Flow.builder()
            .setIngressNode("00000001")
            .setIngressInterface("ethernet1/1")
            .setSrcIp(Ip.parse("1.1.1.10"))
            .setDstIp(Ip.parse("1.1.4.10"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(4096)
            .setDstPort(53)
            .build();
    Flow flowSharedApplication = flowDeviceApplication.toBuilder().setDstPort(179).build();
    Flow flowDenied = flowDeviceApplication.toBuilder().setDstPort(1).build();
    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(flowDeviceApplication, flowSharedApplication, flowDenied), false);

    // Flow matching application defined in device-group namespace should be successful
    assertTrue(traces.get(flowDeviceApplication).get(0).getDisposition().isSuccessful());
    // Flow matching application defined in shared namespace should be successful
    assertTrue(traces.get(flowSharedApplication).get(0).getDisposition().isSuccessful());
    // Flow not matching any allowed application should not be successful
    assertFalse(traces.get(flowDenied).get(0).getDisposition().isSuccessful());
  }

  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  // helper for testAclLineMatchExprForApplication
  private void assertAclLineMatchExprExpectations(
      Map<String, AclLineMatchExpr> overrideMap,
      TraceElement te,
      boolean applicationDefaultService,
      SecurityRule rule,
      Optional<AclLineMatchExpr> expectation) {
    Application a =
        Application.builder("a1")
            .addService(
                Service.builder("bittorrent (tcp/dynamic")
                    .setIpProtocol(IpProtocol.TCP)
                    .addPorts(new SubRange(0, 65535))
                    .build())
            .addService(
                Service.builder("bittorrent (udp/dynamic")
                    .setIpProtocol(IpProtocol.UDP)
                    .addPorts(new SubRange(0, 65535))
                    .build())
            .build();
    Warnings w = new Warnings();

    Optional<AclLineMatchExpr> testMatcher =
        aclLineMatchExprForApplication(a, overrideMap, te, applicationDefaultService, rule, w);
    if (expectation.isPresent()) {
      assertThat(_tb.toBDD(testMatcher.get()), equalTo(_tb.toBDD(expectation.get())));
    } else {
      assertThat(testMatcher, equalTo(expectation));
    }
  }

  @Test
  public void testAclLineMatchExprForApplication() {
    // test combinations of accept/deny, app Id/app override, and default/not default
    String vsysName = "vsys1";
    TraceElement te = matchBuiltInApplicationTraceElement("bittorrent");
    TraceElement teOverride =
        matchApplicationOverrideRuleTraceElement("OVERRIDE_RULE", vsysName, "configs/a1");

    SecurityRule denyRule = new SecurityRule("rule", new Vsys(vsysName));
    denyRule.setAction(LineAction.DENY);

    SecurityRule permitRule = new SecurityRule("rule", new Vsys(vsysName));
    permitRule.setAction(LineAction.PERMIT);

    HeaderSpace.Builder headerBuilder =
        HeaderSpace.builder()
            .setDstPorts(new SubRange(0, 65535))
            .setSrcPorts(new SubRange(0, 65535));

    // Deny rule not using app override should not produce any match conditions, per special L5+
    // logic
    assertAclLineMatchExprExpectations(ImmutableMap.of(), te, false, denyRule, Optional.empty());
    assertAclLineMatchExprExpectations(ImmutableMap.of(), te, true, denyRule, Optional.empty());

    // When not matching application-default, the application can match on any port and protocol
    assertAclLineMatchExprExpectations(
        ImmutableMap.of(), te, false, permitRule, Optional.of(new TrueExpr(te)));

    // With application-default true, the application matches only on its default port and protocol
    Optional<AclLineMatchExpr> permitRuleAppIdDefaultExpectation =
        Optional.of(
            new OrMatchExpr(
                ImmutableList.of(
                    new MatchHeaderSpace(headerBuilder.setIpProtocols(IpProtocol.TCP).build()),
                    new MatchHeaderSpace(headerBuilder.setIpProtocols(IpProtocol.UDP).build())),
                te));
    assertAclLineMatchExprExpectations(
        ImmutableMap.of(), te, true, permitRule, permitRuleAppIdDefaultExpectation);

    // override tests - an overridden application uses L4 definitions and skips app-id, so we can
    // safely just match
    // its L4 definition from the overrideMap.
    AclLineMatchExpr overrideMatcher =
        new OrMatchExpr(
            ImmutableList.of(
                new MatchHeaderSpace(headerBuilder.setIpProtocols(IpProtocol.ICMP).build())),
            teOverride);
    Map<String, AclLineMatchExpr> overrideMap = ImmutableMap.of("a1", overrideMatcher);

    assertAclLineMatchExprExpectations(
        overrideMap, teOverride, false, denyRule, Optional.of(overrideMatcher));
    assertAclLineMatchExprExpectations(
        overrideMap, teOverride, true, denyRule, Optional.of(overrideMatcher));
    assertAclLineMatchExprExpectations(
        overrideMap, teOverride, false, permitRule, Optional.of(overrideMatcher));
    assertAclLineMatchExprExpectations(
        overrideMap, teOverride, true, permitRule, Optional.of(overrideMatcher));
  }
}
