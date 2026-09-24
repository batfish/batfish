package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ConcreteFirewallFilter;
import org.batfish.representation.juniper.FwFromInterfaceGroup;
import org.batfish.representation.juniper.FwFromInterfaceWildcard;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosFirewallInterfaceMatchTest {

  private static final String HOSTNAME = "firewall-interface-match";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration config = getVendorConfiguration(batfish, HOSTNAME);
    Interface ge0 =
        config
            .getMasterLogicalSystem()
            .getInterfaces()
            .get("ge-0/0/0")
            .getUnits()
            .get("ge-0/0/0.0");
    Interface ge1 =
        config
            .getMasterLogicalSystem()
            .getInterfaces()
            .get("ge-0/0/1")
            .getUnits()
            .get("ge-0/0/1.0");
    assertThat(ge0.getInterfaceGroup(), equalTo(5));
    assertThat(ge1.getInterfaceGroup(), equalTo(7));
    assertThat(ge1.getInterfaceGroup6(), equalTo(31));

    ConcreteFirewallFilter filter =
        (ConcreteFirewallFilter) config.getMasterLogicalSystem().getFirewallFilters().get("FILTER");
    FwFromInterfaceGroup group =
        (FwFromInterfaceGroup) filter.getTerms().get("GROUP").getFroms().get(0);
    assertThat(group.getGroup(), equalTo(SubRange.singleton(5)));
    assertThat(group.getExcept(), is(false));
    AclLineMatchExpr groupMatch =
        group.toAclLineMatchExpr(config, null, new Warnings(true, true, true));
    assertThat(groupMatch, instanceOf(MatchSrcInterface.class));
    assertThat(((MatchSrcInterface) groupMatch).getSrcInterfaces(), contains("ge-0/0/0.0"));

    FwFromInterfaceWildcard wildcard =
        (FwFromInterfaceWildcard) filter.getTerms().get("WILDCARD").getFroms().get(0);
    assertThat(wildcard.getInterfaceWildcard(), equalTo("ge-0/0/[01].0"));
    AclLineMatchExpr wildcardMatch =
        wildcard.toAclLineMatchExpr(config, null, new Warnings(true, true, true));
    assertThat(wildcardMatch, instanceOf(MatchSrcInterface.class));
    assertThat(
        ((MatchSrcInterface) wildcardMatch).getSrcInterfaces(),
        contains("ge-0/0/0.0", "ge-0/0/1.0"));

    ConcreteFirewallFilter filter6 =
        (ConcreteFirewallFilter)
            config.getMasterLogicalSystem().getFirewallFilters().get("FILTER6");
    FwFromInterfaceGroup except =
        (FwFromInterfaceGroup) filter6.getTerms().get("EXCEPT").getFroms().get(0);
    assertThat(except.getGroup(), equalTo(new SubRange(30, 33)));
    assertThat(except.getExcept(), is(true));
    AclLineMatchExpr exceptMatch =
        except.toAclLineMatchExpr(config, null, new Warnings(true, true, true));
    assertThat(exceptMatch, instanceOf(NotMatchExpr.class));
    assertThat(((NotMatchExpr) exceptMatch).getOperand(), instanceOf(MatchSrcInterface.class));
    assertThat(
        ((MatchSrcInterface) ((NotMatchExpr) exceptMatch).getOperand()).getSrcInterfaces(),
        contains("ge-0/0/1.0"));

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae.getWarnings().getOrDefault(filename, new Warnings()).getRedFlagWarnings(), empty());
  }
}
