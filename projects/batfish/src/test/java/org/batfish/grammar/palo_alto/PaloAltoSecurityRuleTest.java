package org.batfish.grammar.palo_alto;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
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
        new PaloAltoControlPlaneExtractor(src, parser, new Warnings());
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    PaloAltoConfiguration pac = (PaloAltoConfiguration) extractor.getVendorConfiguration();
    pac.setVendor(ConfigurationFormat.PALO_ALTO);
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    pac.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    pac = SerializationUtils.clone(pac);
    pac.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    pac.setAnswerElement(answerElement);
    return pac;
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
}
