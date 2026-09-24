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

public final class JunosAutonomousSystemIndependentDomainTest {

  private static final String HOSTNAME = "autonomous-system-independent-domain";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance defaultRi = configuration.getMasterLogicalSystem().getDefaultRoutingInstance();
    RoutingInstance ri1 = configuration.getMasterLogicalSystem().getRoutingInstances().get("RI1");
    RoutingInstance ri2 = configuration.getMasterLogicalSystem().getRoutingInstances().get("RI2");

    assertThat(defaultRi.getAs(), equalTo(65000L));
    assertThat(defaultRi.getIndependentDomain(), equalTo(true));
    assertThat(defaultRi.getIndependentDomainNoAttrset(), equalTo(false));
    assertThat(ri1.getAs(), equalTo(65001L));
    assertThat(ri1.getIndependentDomain(), equalTo(true));
    assertThat(ri1.getIndependentDomainNoAttrset(), equalTo(false));
    assertThat(ri2.getAs(), equalTo(65002L));
    assertThat(ri2.getIndependentDomain(), equalTo(true));
    assertThat(ri2.getIndependentDomainNoAttrset(), equalTo(true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("independent-domain"),
            isTodo("independent-domain"),
            isTodo("independent-domain no-attrset")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
