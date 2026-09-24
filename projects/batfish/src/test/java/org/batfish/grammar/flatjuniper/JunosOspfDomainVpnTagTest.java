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

public final class JunosOspfDomainVpnTagTest {

  private static final String HOSTNAME = "junos-ospf-domain-vpn-tag";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testDomainVpnTag() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    assertThat(
        jc.getMasterLogicalSystem().getRoutingInstances().get("CUSTOMER").getOspfDomainVpnTag(),
        equalTo(4294967295L));
    assertThat(
        jc.getMasterLogicalSystem().getRoutingInstances().get("CUSTOMER6").getOspf3DomainVpnTag(),
        equalTo(1234L));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("domain-vpn-tag 4294967295"), isTodo("domain-vpn-tag 1234")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
