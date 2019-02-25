package org.batfish.specifier.parboiled;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Test;

public class ParboiledFilterSpecifierTest {

  private static final String _nodeName = "node0";
  private static final MockSpecifierContext _ctxt;

  // make two different filters; add lines because the current equals() only considers lines
  private static final IpAccessList _filter1 =
      IpAccessList.builder()
          .setName("filter1")
          .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL))
          .build();
  private static final IpAccessList _filter2 =
      IpAccessList.builder()
          .setName("filter2")
          .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
          .build();

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder().setHostname(_nodeName);
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.build();

    nf.interfaceBuilder()
        .setOwner(n1)
        .setName("eth0")
        .setIncomingFilter(_filter1)
        .setOutgoingFilter(_filter2)
        .build();

    // second interface with no filters.
    nf.interfaceBuilder().setOwner(n1).setName("eth1").build();

    n1.getIpAccessLists()
        .putAll(ImmutableMap.of(_filter1.getName(), _filter1, _filter2.getName(), _filter2));

    _ctxt = MockSpecifierContext.builder().setConfigs(ImmutableMap.of(_nodeName, n1)).build();
  }

  @Test
  public void testResolveDifference() {
    assertThat(
        new ParboiledFilterSpecifier(
                new DifferenceFilterAstNode(
                    new NameRegexFilterAstNode("filter.*"), new NameFilterAstNode("filter1")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter2)));
    assertThat(
        new ParboiledFilterSpecifier(
                new DifferenceFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameRegexFilterAstNode("filter2.*")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter1)));
  }

  @Test
  public void testResolveIntersection() {
    assertThat(
        new ParboiledFilterSpecifier(
                new IntersectionFilterAstNode(
                    new NameRegexFilterAstNode("filter.*"), new NameFilterAstNode("filter1")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter1)));
    assertThat(
        new ParboiledFilterSpecifier(
                new IntersectionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameRegexFilterAstNode("filter2")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledFilterSpecifier(new NameFilterAstNode("filter1")).resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter1)));
  }

  @Test
  public void testResolveNameRegex() {
    assertThat(
        new ParboiledFilterSpecifier(new NameRegexFilterAstNode("filter.*2"))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter2)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledFilterSpecifier(
                new UnionFilterAstNode(
                    new NameFilterAstNode("filter1"), new NameFilterAstNode("filter2")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_filter1, _filter2)));
  }
}
