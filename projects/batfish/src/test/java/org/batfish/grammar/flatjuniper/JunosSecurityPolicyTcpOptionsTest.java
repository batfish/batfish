package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.FwTerm;
import org.batfish.representation.juniper.SecurityPolicyTcpOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosSecurityPolicyTcpOptionsTest {

  private static final String HOSTNAME = "security-policy-tcp-options";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    FwTerm term =
        getVendorConfiguration(batfish, HOSTNAME)
            .getMasterLogicalSystem()
            .getSecurityPolicies()
            .get(zoneToZoneFilter("INSIDE", "OUTSIDE"))
            .getTerms()
            .get("ALLOW-TCP");
    SecurityPolicyTcpOptions options = term.getSecurityPolicyTcpOptions();

    assertThat(options, notNullValue());
    assertThat(options.getInitialTcpMss(), equalTo(1200));
    assertThat(options.getReverseTcpMss(), equalTo(1300));
    assertThat(options.getSequenceCheckRequired(), equalTo(true));
    assertThat(options.getSynCheckRequired(), equalTo(true));
    assertThat(options.getWindowScale(), equalTo(true));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    Warnings warnings = ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
