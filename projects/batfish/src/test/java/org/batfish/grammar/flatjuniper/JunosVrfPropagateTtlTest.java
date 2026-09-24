package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosVrfPropagateTtlTest {

  private static final String HOSTNAME = "junos-vrf-propagate-ttl";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testVrfPropagateTtl() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        jc.getMasterLogicalSystem().getRoutingInstances().get("CUSTOMER_A").getVrfPropagateTtl(),
        equalTo(false));
    assertThat(
        jc.getMasterLogicalSystem().getRoutingInstances().get("CUSTOMER_B").getVrfPropagateTtl(),
        equalTo(true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("no-vrf-propagate-ttl"), isTodo("vrf-propagate-ttl")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
