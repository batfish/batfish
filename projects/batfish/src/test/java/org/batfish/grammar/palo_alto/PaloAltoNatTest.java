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
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is NOT NAT'd (does not match source address constraint)
    Flow outsideToInsideBadSrcIp =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.200"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // This flow is NOT NAT'd (does not match from-interface constraint)
    Flow insideToInsideBadIngressIface =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(inside2Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();

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

    // This flow is NAT'd by the pre-rulebase rule and should pass through the firewall
    Flow outsideToInsideNatPreRulebase =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.2"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();

    // This flow is NAT'd by the rulebase rule and should pass through the firewall
    Flow outsideToInsideNatRulebase =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.4"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();

    // This flow is NAT'd by the post-rulebase and should pass through the firewall
    Flow outsideToInsideNatPostRulebase =
        Flow.builder()
            .setTag("test")
            .setIngressNode(c.getHostname())
            .setIngressInterface(outside1Name)
            .setDstIp(Ip.parse("1.1.1.2"))
            .setSrcIp(Ip.parse("1.2.1.5"))
            .setSrcPort(111)
            .setDstPort(222)
            .setIpProtocol(IpProtocol.TCP)
            .build();

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
