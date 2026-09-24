package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosBgpTtlTest {

  private static final String HOSTNAME = "junos-bgp-ttl";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testTtl() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInstance ri = jc.getMasterLogicalSystem().getDefaultRoutingInstance();
    BgpGroup inherited = ri.getNamedBgpGroups().get("GTSM");
    inherited.cascadeInheritance();
    assertThat(inherited.getTtl(), equalTo(255));
    assertThat(ri.getNamedBgpGroups().get("DIRECT").getTtl(), equalTo(1));
    assertThat(ri.getNamedBgpGroups().get("INVALID-DIRECT").getTtl(), nullValue());
    assertThat(ri.getNamedBgpGroups().get("MULTIHOP").getTtl(), equalTo(2));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("ttl 255"),
            isTodo("ttl 1"),
            hasComment("Expected BGP single-hop TTL in range 1,255, but got '2'"),
            isTodo("ttl 2")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
