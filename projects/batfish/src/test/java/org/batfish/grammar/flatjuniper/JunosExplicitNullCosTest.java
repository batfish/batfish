package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.parseJuniperConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Family;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosExplicitNullCosTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() {
    JuniperConfiguration bare = parseJuniperConfig(_folder, "explicit-null-cos");
    assertThat(
        bare.getMasterLogicalSystem().getDefaultRoutingInstance().getExplicitNullCosFamilies(),
        containsInAnyOrder(Family.INET, Family.INET6));
    assertThat(bare.getWarnings().getParseWarnings(), contains(isTodo("explicit-null-cos")));

    JuniperConfiguration inet = parseJuniperConfig(_folder, "explicit-null-cos-inet");
    assertThat(
        inet.getMasterLogicalSystem().getDefaultRoutingInstance().getExplicitNullCosFamilies(),
        contains(Family.INET));
    assertThat(inet.getWarnings().getParseWarnings(), contains(isTodo("explicit-null-cos inet")));

    JuniperConfiguration inet6 = parseJuniperConfig(_folder, "explicit-null-cos-inet6");
    assertThat(
        inet6.getMasterLogicalSystem().getDefaultRoutingInstance().getExplicitNullCosFamilies(),
        contains(Family.INET6));
    assertThat(inet6.getWarnings().getParseWarnings(), contains(isTodo("explicit-null-cos inet6")));
  }

  @Test
  public void testNoConversionWarnings() throws IOException {
    String hostname = "explicit-null-cos";
    Batfish batfish = getBatfish(_folder, hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(hostname, new Warnings()).getRedFlagWarnings(), empty());
  }
}
