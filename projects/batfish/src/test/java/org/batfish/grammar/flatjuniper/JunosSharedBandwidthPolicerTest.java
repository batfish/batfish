package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Policer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosSharedBandwidthPolicerTest {

  private static final String HOSTNAME = "shared-bandwidth-policer";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    Policer policer =
        getVendorConfiguration(batfish, HOSTNAME)
            .getMasterLogicalSystem()
            .getPolicers()
            .get("SHARED");

    assertThat(policer.getSharedBandwidthPolicer(), equalTo(true));
    assertThat(getParseWarnings(batfish, HOSTNAME), empty());

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
