package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNumReferrers;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosInstanceExportTest {

  private static final String HOSTNAME = "instance-export";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndReferences() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    RoutingInstance tenant =
        getVendorConfiguration(batfish, HOSTNAME)
            .getMasterLogicalSystem()
            .getRoutingInstances()
            .get("TENANT");

    assertThat(tenant.getInstanceExports(), contains("EXPORT-A", "EXPORT-B"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("instance-export EXPORT-A"), isTodo("instance-export EXPORT-B")));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "EXPORT-A", 1));
    assertThat(ccae, hasNumReferrers(filename, POLICY_STATEMENT, "EXPORT-B", 1));
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
