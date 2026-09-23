package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PsFromNlriRouteType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyNlriRouteTypeTest {

  private static final String HOSTNAME = "policy-nlri-route-type";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);

    assertThat(
        juniperConfiguration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("ROUTE-TYPE-POLICY")
            .getTerms()
            .get("MATCH-TYPES")
            .getFroms()
            .getFromNlriRouteTypes(),
        containsInAnyOrder(
            new PsFromNlriRouteType(3), new PsFromNlriRouteType(4), new PsFromNlriRouteType(5)));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("nlri-route-type 3"), isTodo("nlri-route-type 4"), isTodo("nlri-route-type 5")));

    assertThat(
        batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME).getRoutingPolicies(),
        hasKey("ROUTE-TYPE-POLICY"));
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
