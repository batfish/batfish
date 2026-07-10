package org.batfish.specifier.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParsedFilterSpecifier} */
public class ParsedFilterSpecifierTest {

  private static final String _node0Name = "node0";
  private static final String _node1Name = "node1";
  private static final MockSpecifierContext _ctxt;

  // make two different filters; add lines because the current equals() only considers lines
  private static final IpAccessList _filter0_1 =
      IpAccessList.builder()
          .setName("filter1")
          .setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL))
          .build();
  private static final IpAccessList _filter0_2 =
      IpAccessList.builder()
          .setName("filter2")
          .setLines(ImmutableList.of(ExprAclLine.REJECT_ALL))
          .build();
  private static final IpAccessList _filter1_1 =
      IpAccessList.builder()
          .setName("filter1")
          .setLines(ImmutableList.of(ExprAclLine.REJECT_ALL))
          .build();

  static {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb = nf.configurationBuilder().setHostname(_node0Name);
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration n0 = cb.build();

    nf.interfaceBuilder()
        .setOwner(n0)
        .setName("eth0")
        .setIncomingFilter(_filter0_1)
        .setOutgoingFilter(_filter0_2)
        .build();

    // second interface with no filters.
    nf.interfaceBuilder().setOwner(n0).setName("eth1").build();

    n0.getIpAccessLists()
        .putAll(
            ImmutableMap.of(_filter0_1.getName(), _filter0_1, _filter0_2.getName(), _filter0_2));

    Configuration.Builder cb1 = nf.configurationBuilder().setHostname(_node1Name);
    cb1.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration n1 = cb1.build();

    n1.getIpAccessLists().putAll(ImmutableMap.of(_filter1_1.getName(), _filter1_1));

    _ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(n0.getHostname(), n0, n1.getHostname(), n1))
            .build();
  }

  @Test
  public void testResolveDifference() {
    assertThat(
        new ParsedFilterSpecifier(
                new DifferenceFilterAstNode(
                    new NameRegexFilterAstNode("filter.*"), new NameFilterAstNode("filter1")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_2)));
    assertThat(
        new ParsedFilterSpecifier(
                new DifferenceFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameRegexFilterAstNode("filter2.*")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1)));
  }

  @Test
  public void testResolveIntersection() {
    assertThat(
        new ParsedFilterSpecifier(
                new IntersectionFilterAstNode(
                    new NameRegexFilterAstNode("filter.*"), new NameFilterAstNode("filter1")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1)));
    assertThat(
        new ParsedFilterSpecifier(
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameRegexFilterAstNode("filter2")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParsedFilterSpecifier(new NameFilterAstNode("filter1")).resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1)));
  }

  @Test
  public void testResolveNameRegex() {
    assertThat(
        new ParsedFilterSpecifier(new NameRegexFilterAstNode("filter.*1"))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1)));
  }

  @Test
  public void testResolveWithNode() {
    assertThat(
        new ParsedFilterSpecifier(
                new FilterWithNodeFilterAstNode(
                    new NameNodeAstNode(_node0Name), new NameRegexFilterAstNode(".*1")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1)));

    // node1 has no matching filter
    assertThat(
        new ParsedFilterSpecifier(
                new FilterWithNodeFilterAstNode(
                    new NameNodeAstNode(_node1Name), new NameRegexFilterAstNode(".*2")))
            .resolve(_node1Name, _ctxt),
        equalTo(ImmutableSet.of()));

    // node regex covers all nodes. resolve should only return those on node0
    assertThat(
        new ParsedFilterSpecifier(
                new FilterWithNodeFilterAstNode(
                    new NameRegexNodeAstNode("n"), new NameRegexFilterAstNode(".*")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1, _filter0_2)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParsedFilterSpecifier(
                new UnionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2")))
            .resolve(_node0Name, _ctxt),
        equalTo(ImmutableSet.of(_filter0_1, _filter0_2)));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testParseBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    ParsedFilterSpecifier.parse("@connected");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParsedFilterSpecifier.parse("filter0"),
        equalTo(new ParsedFilterSpecifier(new NameFilterAstNode("filter0"))));
  }
}
