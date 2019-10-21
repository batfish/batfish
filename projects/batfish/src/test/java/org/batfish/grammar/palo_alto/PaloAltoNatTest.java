package org.batfish.grammar.palo_alto;

import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
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
    return getBatfishForConfigurationNames(configurationNames).loadConfigurations();
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testSourceNat() throws IOException {
    // Test source NAT for traffic from inside zone to outside zone
    Configuration c = parseConfig("source-nat");
    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/24
    String outside2Name = "ethernet1/2.2"; // 1.2.2.3/24
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    batfish.computeDataPlane();

    // This flow is NAT'd and should pass through the firewall
    Flow insideToOutside =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(inside1Name)
            .setSrcIp(Ip.parse("1.1.1.2"))
            .setDstIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is NOT NAT'd (does not match source address constraint)
    Flow insideToOutsideBadSrcIp =
        insideToOutside.toBuilder().setSrcIp(Ip.parse("1.2.1.200")).build();
    // This flow is NOT NAT'd (does not match from interface constraint)
    Flow outsideToOutsideBadIngressIface =
        insideToOutside.toBuilder().setIngressInterface(outside2Name).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine()
            .computeTraces(
                ImmutableSet.of(
                    insideToOutside, insideToOutsideBadSrcIp, outsideToOutsideBadIngressIface),
                false);

    // Flow should be NAT'd and be successful
    assertTrue(traces.get(insideToOutside).get(0).getDisposition().isSuccessful());

    // Flow not matching NAT source address restriction should not be NAT'd
    // And therefore should not be successful
    assertFalse(traces.get(insideToOutsideBadSrcIp).get(0).getDisposition().isSuccessful());

    // Flow not matching from zone, should not be NAT'd and should be unsuccessful
    assertFalse(traces.get(outsideToOutsideBadIngressIface).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testPanoramaSourceNat() throws IOException {
    /*
    Setup: Three NAT rules
    - Panorama pre-rulebase has a rule to translate source 1.1.1.2 to 1.1.1.99
    - Vsys rulebase has a rule to translate sources in 1.1.1.2/30 to 1.1.1.100
    - Panorama post-rulebase has a rule to translate sources in 1.1.1.2/28 to 1.1.1.101
    Should match at most one rule.
     */
    // Test panorama destination NAT for traffic from outside zone to inside zone
    Configuration c = parseConfig("source-nat-panorama");

    String inside1Name = "ethernet1/1.1"; // 1.1.1.3/24
    String outside1Name = "ethernet1/2.1"; // 1.2.1.3/24
    assertThat(
        c.getAllInterfaces().keySet(),
        containsInAnyOrder("ethernet1/1", inside1Name, "ethernet1/2", outside1Name));
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    batfish.computeDataPlane();

    Flow.Builder flowBuilder =
        Flow.builder()
            .setTag("test")
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
            .getTracerouteEngine()
            .computeTraces(
                ImmutableSet.of(
                    hitsPreRulebaseNatRule,
                    hitsVsysNatRule,
                    hitsPostRulebaseNatRule,
                    doesNotHitNatRule),
                false);

    // All natted flows should make it out, unnatted flow should not
    Trace hitPreRulebaseRule = traces.get(hitsPreRulebaseNatRule).get(0);
    Trace hitVsysRule = traces.get(hitsVsysNatRule).get(0);
    Trace hitPostRulebaseRule = traces.get(hitsPostRulebaseNatRule).get(0);
    assertTrue(hitPreRulebaseRule.getDisposition().isSuccessful());
    assertTrue(hitVsysRule.getDisposition().isSuccessful());
    assertTrue(hitPostRulebaseRule.getDisposition().isSuccessful());
    assertFalse(traces.get(doesNotHitNatRule).get(0).getDisposition().isSuccessful());

    // Expected translated addresses
    Ip panPreNewAddr = Ip.parse("1.1.1.99");
    Ip vsysNatNewAddr = Ip.parse("1.1.1.100");
    Ip panPostNewAddr = Ip.parse("1.1.1.101");
    ExitOutputIfaceStep hitPreRuleLastStep =
        (ExitOutputIfaceStep) Iterables.getLast(hitPreRulebaseRule.getHops().get(0).getSteps());
    ExitOutputIfaceStep hitVsysRuleLastStep =
        (ExitOutputIfaceStep) Iterables.getLast(hitVsysRule.getHops().get(0).getSteps());
    ExitOutputIfaceStep hitPostRuleLastStep =
        (ExitOutputIfaceStep) Iterables.getLast(hitPostRulebaseRule.getHops().get(0).getSteps());
    assertThat(
        hitPreRuleLastStep.getDetail().getTransformedFlow().getSrcIp(), equalTo(panPreNewAddr));
    assertThat(
        hitVsysRuleLastStep.getDetail().getTransformedFlow().getSrcIp(), equalTo(vsysNatNewAddr));
    assertThat(
        hitPostRuleLastStep.getDetail().getTransformedFlow().getSrcIp(), equalTo(panPostNewAddr));
  }

  @Test
  public void testDestNat() throws IOException {
    // Test destination NAT is applied correctly
    Configuration c = parseConfig("destination-nat");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    batfish.computeDataPlane();
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

    // This flow is NAT'd and should pass through the firewall
    Flow outsideToInsideNat =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is NOT NAT'd (does not match source address constraint)
    Flow outsideToInsideBadSrcIp =
        outsideToInsideNat.toBuilder().setSrcIp(Ip.parse("1.2.1.200")).build();
    // This flow is NOT NAT'd (does not match from-interface constraint)
    Flow insideToInsideBadIngressIface =
        outsideToInsideNat.toBuilder().setIngressInterface(inside2Name).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine()
            .computeTraces(
                ImmutableSet.of(
                    outsideToInsideNat, outsideToInsideBadSrcIp, insideToInsideBadIngressIface),
                false);

    // Flow should be NAT'd and be successful
    assertTrue(traces.get(outsideToInsideNat).get(0).getDisposition().isSuccessful());

    // Flow not matching NAT dest address constraint should not be NAT'd and therefore should not be
    // successful
    assertFalse(traces.get(outsideToInsideBadSrcIp).get(0).getDisposition().isSuccessful());

    // Flow not matching from-zone, should not be NAT'd and should be unsuccessful
    assertFalse(traces.get(insideToInsideBadIngressIface).get(0).getDisposition().isSuccessful());
  }

  @Test
  public void testPanoramaDestNatOrder() throws IOException {
    // Test panorama destination NATs are applied in the right order
    Configuration c = parseConfig("destination-nat-panorama");
    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    batfish.computeDataPlane();
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
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP);
    // This flow is NAT'd by the pre-rulebase rule and should pass through the firewall
    Flow outsideToInsideNatPreRulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.2")).build();
    // This flow is NAT'd by the rulebase rule and should pass through the firewall
    Flow outsideToInsideNatRulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.4")).build();
    // This flow is NAT'd by the post-rulebase and should pass through the firewall
    Flow outsideToInsideNatPostRulebase = flowBuilder.setSrcIp(Ip.parse("1.2.1.5")).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine()
            .computeTraces(
                ImmutableSet.of(
                    outsideToInsideNatPreRulebase,
                    outsideToInsideNatRulebase,
                    outsideToInsideNatPostRulebase),
                false);

    // First flow should be NAT'd by pre-rulebase (not rulebase) rule and be successful
    Trace preRulebase = traces.get(outsideToInsideNatPreRulebase).get(0);
    assertTrue(preRulebase.getDisposition().isSuccessful());
    ExitOutputIfaceStepDetail preRulebaseDetail =
        (ExitOutputIfaceStepDetail)
            Iterables.getLast(preRulebase.getHops().get(0).getSteps()).getDetail();
    // Confirm the dst IP was rewritten by the pre-rulebase rule
    assertThat(preRulebaseDetail.getTransformedFlow().getDstIp(), equalTo(Ip.parse("1.1.1.99")));

    // Second flow should be NAT'd by rulebase (not post-rulebase) rule and be successful
    Trace rulebase = traces.get(outsideToInsideNatRulebase).get(0);
    assertTrue(rulebase.getDisposition().isSuccessful());
    ExitOutputIfaceStepDetail rulebaseDetail =
        (ExitOutputIfaceStepDetail)
            Iterables.getLast(rulebase.getHops().get(0).getSteps()).getDetail();
    // Confirm the dst IP was rewritten by the rulebase rule
    assertThat(rulebaseDetail.getTransformedFlow().getDstIp(), equalTo(Ip.parse("1.1.1.100")));

    // Third flow should be NAT'd by rulebase (not post-rulebase) rule and be successful
    Trace postRulebase = traces.get(outsideToInsideNatPostRulebase).get(0);
    assertTrue(postRulebase.getDisposition().isSuccessful());
    ExitOutputIfaceStepDetail postRulebaseDetail =
        (ExitOutputIfaceStepDetail)
            Iterables.getLast(postRulebase.getHops().get(0).getSteps()).getDetail();
    // Confirm the dst IP was rewritten by the post-rulebase rule
    assertThat(postRulebaseDetail.getTransformedFlow().getDstIp(), equalTo(Ip.parse("1.1.1.101")));
  }
}
