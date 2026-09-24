package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV6_UNICAST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.RoutingInformationBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosRoutingOptionsRibMultipathTest {

  private static final String HOSTNAME = "routing-options-rib-multipath";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtraction() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    RoutingInformationBase inet =
        configuration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV4_UNICAST);
    RoutingInformationBase inet6 =
        configuration
            .getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getRibs()
            .get(RIB_IPV6_UNICAST);
    RoutingInformationBase riInet =
        configuration
            .getMasterLogicalSystem()
            .getRoutingInstances()
            .get("RI")
            .getRibs()
            .get("RI.inet.0");

    assertThat(inet.getMultipath(), equalTo(true));
    assertThat(inet.getMultipathAsPathCompare(), equalTo(true));
    assertThat(inet.getMultipathVpnUnequalCost(), equalTo(true));
    assertThat(inet6.getMultipath(), equalTo(true));
    assertThat(inet6.getMultipathVpnUnequalCost(), equalTo(true));
    assertThat(riInet.getMultipath(), equalTo(true));
    assertThat(riInet.getMultipathVpnUnequalCost(), equalTo(true));
    assertThat(riInet.getMultipathVpnEqualExternalInternal(), equalTo(true));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("multipath"),
            isTodo("multipath as-path-compare"),
            isTodo("multipath vpn-unequal-cost"),
            isTodo("multipath vpn-unequal-cost"),
            isTodo("multipath vpn-unequal-cost equal-external-internal")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
