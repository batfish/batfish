package org.batfish.grammar.palo_alto;

import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.RouteInfo;
import org.batfish.datamodel.flow.RoutingStep;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PaloAltoNatTest {
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

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  /**
   * Extracts transformed flow from a trace that is expected to contain one hop with one {@link
   * ExitOutputIfaceStep}. The transformed flow is taken from that step.
   */
  private static Flow getTransformedFlow(Trace trace) {
    List<Hop> hops = trace.getHops();
    assert hops.size() == 1;
    List<Step<?>> steps = hops.get(0).getSteps();
    List<ExitOutputIfaceStep> exitIfaceSteps =
        steps.stream()
            .filter(s -> s instanceof ExitOutputIfaceStep)
            .map(ExitOutputIfaceStep.class::cast)
            .collect(ImmutableList.toImmutableList());
    assert exitIfaceSteps.size() == 1;
    return exitIfaceSteps.get(0).getDetail().getTransformedFlow();
  }

  @Test
  public void testNatService() throws IOException {
    // Test service match condition is applied correctly for NAT rules
    Configuration c = parseConfig("nat-service");
    String outsideName = "ethernet1/2"; // 10.0.2.1/24
    String outsideAddr = "10.0.2.100";
    String serverAddr1 = "10.0.1.1"; // For RULE_GROUP
    String serverAddr2 = "10.0.1.2"; // For RULE_BUILTIN
    int servicePort1 = 1234;
    int servicePort2 = 2345;
    int servicePort3 = 3456;
    int builtinPort = 80;
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // This flow is NAT'd by RULE_GROUP
    Flow flowMatch1a =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(outsideName)
            .setSrcIp(Ip.parse(outsideAddr))
            .setDstIp(Ip.parse(serverAddr1))
            // Arbitrary src port
            .setSrcPort(123)
            .setDstPort(servicePort1)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is also NAT'd by RULE_GROUP
    Flow flowMatch1b = flowMatch1a.toBuilder().setDstPort(servicePort2).build();
    // This flow is not NAT'd
    Flow flowNoMatch1 = flowMatch1a.toBuilder().setDstPort(servicePort3).build();

    // This flow is NAT'd by RULE_BUILTIN
    Flow flowMatch2 =
        flowMatch1a.toBuilder().setDstIp(Ip.parse(serverAddr2)).setDstPort(builtinPort).build();
    // This flow is not NAT'd
    Flow flowNoMatch2 = flowMatch2.toBuilder().setDstPort(builtinPort + 1).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(flowMatch1a, flowMatch1b, flowMatch2, flowNoMatch1, flowNoMatch2),
                false);

    // Non-NAT'd flows should be unchanged
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowNoMatch1))), flowNoMatch1);
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowNoMatch2))), flowNoMatch2);

    // NAT'd flows should have src IP and port NAT'd
    Ip newSrcIp = Ip.parse("192.168.1.100");
    int ephemeralPort = NamedPort.EPHEMERAL_LOWEST.number();
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowMatch1a))),
        flowMatch1a.toBuilder().setSrcIp(newSrcIp).setSrcPort(ephemeralPort).build());
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowMatch1b))),
        flowMatch1b.toBuilder().setSrcIp(newSrcIp).setSrcPort(ephemeralPort).build());

    Ip newSrcIp2 = Ip.parse("192.168.1.101");
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowMatch2))),
        flowMatch2.toBuilder().setSrcIp(newSrcIp2).setSrcPort(ephemeralPort).build());
  }

  @Test
  public void testSourceNat() throws IOException {
    /* Test source NAT for traffic from inside zone to outside zone. There is one NAT rule that
      matches flows from inside to outside with src 1.1.1.2, and translates src to 1.1.1.99.
    */
    Configuration c = parseConfig("source-nat");
    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/24
    String outside2Name = "ethernet1/2.2"; // 1.2.2.3/24
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // This flow is NAT'd
    Flow insideToOutsideMatch =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inside1Name)
            .setSrcIp(Ip.parse("1.1.1.2"))
            .setDstIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is not NAT'd (does not match source address constraint)
    Flow insideToOutsideNoMatch =
        insideToOutsideMatch.toBuilder().setSrcIp(Ip.parse("1.2.1.200")).build();
    // This flow is not NAT'd (does not match from interface constraint)
    Flow outsideToOutside =
        insideToOutsideMatch.toBuilder().setIngressInterface(outside2Name).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(insideToOutsideMatch, insideToOutsideNoMatch, outsideToOutside),
                false);

    Ip newSrcIp = Ip.parse("1.1.1.99");
    int newSrcPort = NamedPort.EPHEMERAL_LOWEST.number();
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(insideToOutsideMatch))),
        insideToOutsideMatch.toBuilder().setSrcIp(newSrcIp).setSrcPort(newSrcPort).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(insideToOutsideNoMatch))),
        insideToOutsideNoMatch);

    // No ruleset applied to intrazone flows, so exit interface step has no transformed flow
    assertNull(getTransformedFlow(Iterables.getOnlyElement(traces.get(outsideToOutside))));
  }

  @Test
  public void testPanoramaSourceNatOrder() throws IOException {
    /*
    Setup: Three NAT rules
    - Panorama pre-rulebase has a rule to translate source 1.1.1.2 to 1.1.1.99
    - Vsys rulebase has a rule to translate sources in 1.1.1.2/30 to 1.1.1.100
    - Panorama post-rulebase has a rule to translate sources in 1.1.1.2/28 to 1.1.1.101
    Should match at most one rule.
     */
    Configuration c = parseConfig("source-nat-panorama");

    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/24
    String outside1Name = "ethernet1/2.1"; // 1.2.1.3/24
    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder("ethernet1/1", inside1Name, "ethernet1/2", outside1Name));
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(inside1Name)
            .setDstIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow hitsPreRulebaseNatRule = flowBuilder.setSrcIp(Ip.parse("1.1.1.2")).build();
    // Src IP in 1.1.1.2/30
    Flow hitsVsysNatRule = flowBuilder.setSrcIp(Ip.parse("1.1.1.1")).build();
    // Src IP in 1.1.1.2/28 but not 1.1.1.2/30
    Flow hitsPostRulebaseNatRule = flowBuilder.setSrcIp(Ip.parse("1.1.1.6")).build();
    // Src IP not in 1.1.1.2/28
    Flow doesNotHitNatRule = flowBuilder.setSrcIp(Ip.parse("1.1.1.30")).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    hitsPreRulebaseNatRule,
                    hitsVsysNatRule,
                    hitsPostRulebaseNatRule,
                    doesNotHitNatRule),
                false);

    Trace hitPreRulebaseRule = Iterables.getOnlyElement(traces.get(hitsPreRulebaseNatRule));
    Trace hitVsysRule = Iterables.getOnlyElement(traces.get(hitsVsysNatRule));
    Trace hitPostRulebaseRule = Iterables.getOnlyElement(traces.get(hitsPostRulebaseNatRule));
    Trace didNotHitNatRule = Iterables.getOnlyElement(traces.get(doesNotHitNatRule));

    // Expected translated addresses
    Ip panPreNewAddr = Ip.parse("1.1.1.99");
    Ip vsysNatNewAddr = Ip.parse("1.1.1.100");
    Ip panPostNewAddr = Ip.parse("1.1.1.101");
    int newSrcPort = NamedPort.EPHEMERAL_LOWEST.number();

    assertEquals(
        getTransformedFlow(hitPreRulebaseRule),
        hitsPreRulebaseNatRule.toBuilder().setSrcIp(panPreNewAddr).setSrcPort(newSrcPort).build());
    assertEquals(
        getTransformedFlow(hitVsysRule),
        hitsVsysNatRule.toBuilder().setSrcIp(vsysNatNewAddr).setSrcPort(newSrcPort).build());
    assertEquals(
        getTransformedFlow(hitPostRulebaseRule),
        hitsPostRulebaseNatRule.toBuilder()
            .setSrcIp(panPostNewAddr)
            .setSrcPort(newSrcPort)
            .build());
    assertEquals(getTransformedFlow(didNotHitNatRule), doesNotHitNatRule);
  }

  @Test
  public void testDestNat() throws IOException {
    // Test destination NAT is applied correctly. There are two NAT rules that matche flows from
    // outside to inside:
    // 1) src 1.1.1.2, and translates dst to 1.1.1.99
    // 2) src 1.1.1.22, and translates dst to 1.1.1.99:1234.
    Configuration c = parseConfig("destination-nat");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/31
    String inside2Name = "ethernet1/1.2"; // 1.1.2.3/31
    String outside1Name = "ethernet1/2.1"; // 1.2.1.3/31
    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder("ethernet1/1", inside1Name, inside2Name, "ethernet1/2", outside1Name));

    Interface outside1 = c.getAllInterfaces().get(outside1Name);
    String outside1Policy = outside1.getRoutingPolicyName();
    // Interface in OUTSIDE zone has packet policy
    assertThat(outside1Policy, notNullValue());

    // This flow is NAT'd
    Flow outsideToInsideMatch1 =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is also NAT'd
    Flow outsideToInsideMatch2 =
        outsideToInsideMatch1.toBuilder().setSrcIp(Ip.parse("1.2.1.22")).build();

    // This flow is not NAT'd (does not match source address constraint)
    Flow outsideToInsideNoMatch =
        outsideToInsideMatch1.toBuilder().setSrcIp(Ip.parse("1.2.1.200")).build();
    // This flow is not NAT'd (does not match from-interface constraint)
    Flow insideToInside =
        outsideToInsideMatch1.toBuilder().setIngressInterface(inside2Name).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    outsideToInsideMatch1,
                    outsideToInsideMatch2,
                    outsideToInsideNoMatch,
                    insideToInside),
                false);

    Ip newDstIp = Ip.parse("1.1.1.99");
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(outsideToInsideMatch1))),
        outsideToInsideMatch1.toBuilder().setDstIp(newDstIp).build());
    int newDstPort = 1234;
    // This flow should have dest IP and port translated
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(outsideToInsideMatch2))),
        outsideToInsideMatch2.toBuilder().setDstIp(newDstIp).setDstPort(newDstPort).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(outsideToInsideNoMatch))),
        outsideToInsideNoMatch);

    // No ruleset applied to intrazone flows, so exit interface step has no transformed flow
    assertNull(getTransformedFlow(Iterables.getOnlyElement(traces.get(insideToInside))));
  }

  @Test
  public void testPanoramaDestNatOrder() throws IOException {
    /* Test panorama dst NATs are applied in the right order. Each rulebase has rules:
      pre-rulebase:
        - match src 1.2.1.2 -> translate dst to 1.1.1.99
      vsys1 rulebase:
        - match src 1.2.1.2 -> translate dst to 11.11.11.11 (unreachable due to pre-rulebase rule)
        - match src 1.2.1.4 -> translate dst to 1.1.1.100
      post-rulebase:
        - match src 1.2.1.4 -> translate dst to 11.11.11.11 (unreachable for vsys1 flows)
        - match src 1.2.1.5 -> translate dst to 1.1.1.101
    */
    Ip preRulebaseTranslatedDst = Ip.parse(("1.1.1.99"));
    Ip vsys1RulebaseTranslatedDst = Ip.parse(("1.1.1.100"));
    Ip postRulebaseTranslatedDst = Ip.parse(("1.1.1.101"));

    Configuration c = parseConfig("destination-nat-panorama");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/31
    String outside1Name = "ethernet1/2.1"; // 1.2.1.3/31
    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder("ethernet1/1", inside1Name, "ethernet1/2", outside1Name));

    Interface outside1 = c.getAllInterfaces().get(outside1Name);
    String outside1Policy = outside1.getRoutingPolicyName();
    // Interface in OUTSIDE zone has packet policy
    assertThat(outside1Policy, notNullValue());

    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow matchPreRulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.2")).build();
    Flow matchVsys1Rulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.4")).build();
    Flow matchPostRulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.5")).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(matchPreRulebase, matchVsys1Rulebase, matchPostRulebase), false);

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchPreRulebase))),
        matchPreRulebase.toBuilder().setDstIp(preRulebaseTranslatedDst).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchVsys1Rulebase))),
        matchVsys1Rulebase.toBuilder().setDstIp(vsys1RulebaseTranslatedDst).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchPostRulebase))),
        matchPostRulebase.toBuilder().setDstIp(postRulebaseTranslatedDst).build());
  }

  @Test
  public void testNatNoopRules() throws IOException {
    /*
    Setup: 4 NAT rules
    - First rule matches src 1.1.1.2, does nothing
    - Second rule matches src 1.1.1.2/30, translates source only
    - Third rule matches src 1.1.1.2/28, translates dest only
    - Fourth rule matches src 1.1.1.2/24, translates both source and dest
    Only one NAT rule should be applied to each flow.
     */
    Configuration c = parseConfig("nat-match-noop-rules");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String ingressIfaceName = "ethernet1/1.1"; // 1.1.1.3/24

    // Create flows to match each rule
    Ip dstIp = Ip.parse("1.2.1.2");
    Ip matchNoopSrcIp = Ip.parse("1.1.1.2");
    Ip matchSrcTransRuleIp = Ip.parse("1.1.1.3");
    Ip matchDstTransRuleIp = Ip.parse("1.1.1.6");
    Ip matchSrcAndDstTransRuleIp = Ip.parse("1.1.1.30");
    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(ingressIfaceName)
            .setDstIp(dstIp)
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow matchesNoopRule = flowBuilder.setSrcIp(matchNoopSrcIp).build();
    Flow matchesSrcTranslationRule = flowBuilder.setSrcIp(matchSrcTransRuleIp).build();
    Flow matchesDstTranslationRule = flowBuilder.setSrcIp(matchDstTransRuleIp).build();
    Flow matchesSrcAndDstTranslationRule = flowBuilder.setSrcIp(matchSrcAndDstTransRuleIp).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(
                    matchesNoopRule,
                    matchesSrcTranslationRule,
                    matchesDstTranslationRule,
                    matchesSrcAndDstTranslationRule),
                false);

    // All translated IPs will match these translated addresses
    Ip newSrcIp = Ip.parse("1.1.1.99");
    Ip newDstIp = Ip.parse("1.2.1.99");
    int newSrcPort = NamedPort.EPHEMERAL_LOWEST.number();

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesNoopRule))), matchesNoopRule);

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesSrcTranslationRule))),
        matchesSrcTranslationRule.toBuilder().setSrcIp(newSrcIp).setSrcPort(newSrcPort).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesDstTranslationRule))),
        matchesDstTranslationRule.toBuilder().setDstIp(newDstIp).build());

    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesSrcAndDstTranslationRule))),
        matchesSrcAndDstTranslationRule.toBuilder()
            .setSrcIp(newSrcIp)
            .setSrcPort(newSrcPort)
            .setDstIp(newDstIp)
            .build());
  }

  @Test
  public void testNatDisabledRule() throws IOException {
    /*
    Setup: 2 NAT rules
    - First rule is disabled, otherwise matches src 1.1.1.2/30, translates source
    - Second rule is not disabled, matches same src 1.1.1.2/30, translates source to different addr
     */
    Configuration c = parseConfig("nat-disable");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String ingressIfaceName = "ethernet1/1.1"; // 1.1.1.3/24

    // Flow matching both rules
    Ip dstIp = Ip.parse("1.2.1.2");
    Ip matchSrcTransRuleIp = Ip.parse("1.1.1.3");
    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(ingressIfaceName)
            .setDstIp(dstIp)
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow matchesRules = flowBuilder.setSrcIp(matchSrcTransRuleIp).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(matchesRules), false);

    // Translated source IP from the non-disabled rule
    Ip newSrcIp = Ip.parse("1.1.1.99");
    int newSrcPort = NamedPort.EPHEMERAL_LOWEST.number();

    // Make sure the translated source IP matches that from the non-disabled rule
    // i.e. make sure the disabled rule was skipped
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesRules))),
        matchesRules.toBuilder().setSrcIp(newSrcIp).setSrcPort(newSrcPort).build());
  }

  @Test
  public void testNatRulesWithEmptyPools() throws IOException {
    /*
    Setup: 2 NAT rules
    - First rule matches src 1.1.1.2, has source and dest nat but both pools are empty
    - Second rule matches src 1.1.1.2/30, translates both source and dest
    Flows matching first rule should be left untransformed (should not fall through to second rule).
    TODO This behavior is only a guess.
     */
    Configuration c = parseConfig("nat-rules-empty-pool");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String ingressIfaceName = "ethernet1/1.1"; // 1.1.1.3/24

    // Create flows to match each rule
    Ip dstIp = Ip.parse("1.2.1.2");
    Ip matchEmptyPoolsSrcIp = Ip.parse("1.1.1.2");
    Ip matchSrcAndDstTransRuleIp = Ip.parse("1.1.1.3");
    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(ingressIfaceName)
            .setDstIp(dstIp)
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow matchesEmptyPoolsRule = flowBuilder.setSrcIp(matchEmptyPoolsSrcIp).build();
    Flow matchesSrcAndDstTranslationRule = flowBuilder.setSrcIp(matchSrcAndDstTransRuleIp).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(matchesEmptyPoolsRule, matchesSrcAndDstTranslationRule), false);

    Ip newSrcIp = Ip.parse("1.1.1.99");
    Ip newDstIp = Ip.parse("1.2.1.99");
    int newSrcPort = NamedPort.EPHEMERAL_LOWEST.number();

    // Flow that matches rule with empty pools should not undergo any transformation
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesEmptyPoolsRule))),
        matchesEmptyPoolsRule);

    // Flow that matches src and dst translation rule (with non-empty pools) should be transformed
    assertEquals(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(matchesSrcAndDstTranslationRule))),
        matchesSrcAndDstTranslationRule.toBuilder()
            .setSrcIp(newSrcIp)
            .setSrcPort(newSrcPort)
            .setDstIp(newDstIp)
            .build());
  }

  @Test
  public void testSecurityRulesMatchOnOriginalFlow() throws IOException {
    /*
    Setup: 2 NAT rules
     - Transform dst 2.2.2.2 -> 3.3.3.3
     - Transform dst 3.3.3.3 -> 2.2.2.2
    Security rules only permit flows to dst 2.2.2.2. Should match on the original pre-NAT flow.
     */
    Configuration c = parseConfig("security-rules-original-flow");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);
    String ingressIfaceName = "ethernet1/1.1"; // 1.1.1.3/24

    // Create flows to match each rule
    Ip dstIp1 = Ip.parse("2.2.2.2");
    Ip dstIp2 = Ip.parse("3.3.3.3");
    Flow.Builder flowBuilder =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface(ingressIfaceName)
            .setSrcIp(Ip.parse("1.1.1.1")) // src should make no difference
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    Flow toDst1 = flowBuilder.setDstIp(dstIp1).build();
    Flow toDst2 = flowBuilder.setDstIp(dstIp2).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(toDst1, toDst2), false);
    Trace toDst1Trace = Iterables.getOnlyElement(traces.get(toDst1));
    Trace toDst2Trace = Iterables.getOnlyElement(traces.get(toDst2));

    // Only the trace originally to dst 1 should have been allowed out
    assertEquals(toDst1Trace.getDisposition(), FlowDisposition.DELIVERED_TO_SUBNET);
    assertEquals(toDst2Trace.getDisposition(), FlowDisposition.DENIED_OUT);

    // Make sure both flows were transformed as expected, based on their selected routes.
    // TODO This could be less roundabout once PolicyStepDetail is fleshed out.
    RoutingStep originallyToDst1RoutingStep =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(toDst1Trace.getHops()).getSteps().stream()
                .filter(step -> step instanceof RoutingStep)
                .map(RoutingStep.class::cast)
                .collect(ImmutableList.toImmutableList()));
    RoutingStep originallyToDst2RoutingStep =
        Iterables.getOnlyElement(
            Iterables.getOnlyElement(toDst2Trace.getHops()).getSteps().stream()
                .filter(step -> step instanceof RoutingStep)
                .map(RoutingStep.class::cast)
                .collect(ImmutableList.toImmutableList()));
    RouteInfo routeUsedForFlowOriginallyToDst1 =
        Iterables.getOnlyElement(originallyToDst1RoutingStep.getDetail().getRoutes());
    RouteInfo routeUsedForFlowOriginallyToDst2 =
        Iterables.getOnlyElement(originallyToDst2RoutingStep.getDetail().getRoutes());
    assertEquals(routeUsedForFlowOriginallyToDst1.getNetwork(), Prefix.create(dstIp2, 24));
    assertEquals(routeUsedForFlowOriginallyToDst2.getNetwork(), Prefix.create(dstIp1, 24));
  }
}
