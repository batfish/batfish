package org.batfish.grammar.palo_alto;

import static org.batfish.main.BatfishTestUtils.getBatfish;
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
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.ip.Ip;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
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

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
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
