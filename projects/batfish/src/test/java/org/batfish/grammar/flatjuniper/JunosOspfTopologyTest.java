package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.OspfInterfaceSettings;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosOspfTopologyTest {

  private static final String HOSTNAME = "junos-ospf-topology";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testTopology() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance ri = jc.getMasterLogicalSystem().getDefaultRoutingInstance();
    assertThat(ri.getOspfOverloadedTopologies(), contains("default", "voice"));
    OspfInterfaceSettings settings =
        jc.getMasterLogicalSystem()
            .getInterfaces()
            .get("ge-0/0/0")
            .getUnits()
            .get("ge-0/0/0.0")
            .getOspfSettings();
    assertThat(settings.getOspfTopologyCosts().get("default"), equalTo(100));
    assertThat(settings.getOspfTopologyCosts().get("voice"), equalTo(200));
    assertThat(settings.getOspfTopologyCosts().get("video"), nullValue());
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("topology voice overload"),
            isTodo("topology voice metric 200"),
            hasComment("Expected OSPF topology metric in range 1-65535, but got '0'")));

    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        c.getDefaultVrf().getOspfProcesses().get("default").getMaxMetricTransitLinks(),
        equalTo(0xFFFFL));
    assertThat(c.getAllInterfaces().get("ge-0/0/0.0").getOspfCost(), equalTo(100));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
