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
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosVrfTableLabelTest {

  private static final String HOSTNAME = "vrf-table-label";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance plain =
        configuration.getMasterLogicalSystem().getRoutingInstances().get("PLAIN");
    RoutingInstance sourceClass =
        configuration.getMasterLogicalSystem().getRoutingInstances().get("SOURCE-CLASS");

    assertThat(plain.getVrfTableLabel(), equalTo(true));
    assertThat(plain.getVrfTableLabelSourceClassUsage(), equalTo(false));
    assertThat(sourceClass.getVrfTableLabel(), equalTo(true));
    assertThat(sourceClass.getVrfTableLabelSourceClassUsage(), equalTo(true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("vrf-table-label"), isTodo("vrf-table-label source-class-usage")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
