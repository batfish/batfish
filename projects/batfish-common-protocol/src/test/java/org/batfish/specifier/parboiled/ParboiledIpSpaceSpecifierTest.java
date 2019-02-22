package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier.computeIpSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.junit.Test;

public class ParboiledIpSpaceSpecifierTest {

  private MockSpecifierContext _emptyCtxt = MockSpecifierContext.builder().build();

  @Test
  public void testComputeIpSpaceAddressGroup() {
    String ip = "8.8.8.8";
    String addressGroup = "ag";
    String book = "book";
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setReferenceBooks(
                ImmutableSortedSet.of(
                    ReferenceBook.builder(book)
                        .setAddressGroups(
                            ImmutableList.of(
                                new AddressGroup(ImmutableSortedSet.of(ip), addressGroup)))
                        .build()))
            .build();
    assertThat(
        computeIpSpace(
            new AddressGroupIpSpaceAstNode(
                new StringAstNode(addressGroup), new StringAstNode(book)),
            ctxt),
        equalTo(new IpWildcard(ip).toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceComma() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.10");
    assertThat(
        computeIpSpace(new UnionIpSpaceAstNode(new IpAstNode(ip1), new IpAstNode(ip2)), _emptyCtxt),
        equalTo(AclIpSpace.union(ip1.toIpSpace(), ip2.toIpSpace())));
  }

  @Test
  public void testComputeIpSpaceIp() {
    Ip ip = Ip.parse("1.1.1.1");
    assertThat(computeIpSpace(new IpAstNode(ip), _emptyCtxt), equalTo(ip.toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceIpRange() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.10");
    assertThat(
        computeIpSpace(new IpRangeAstNode(ip1, ip2), _emptyCtxt), equalTo(IpRange.range(ip1, ip2)));
  }

  @Test
  public void testComputeIpSpaceLocation() {
    String node1 = "node1";
    String iface1 = "eth1";

    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname(node1).build();
    nf.interfaceBuilder().setOwner(n1).setName(iface1).build();
    nf.interfaceBuilder()
        .setOwner(n1)
        .setName(iface1)
        .setActive(true)
        .setAddress(new InterfaceAddress("3.3.3.0/24"))
        .build();

    SpecifierContext ctxt =
        MockSpecifierContext.builder().setConfigs(ImmutableMap.of(node1, n1)).build();

    assertThat(
        computeIpSpace(
            new LocationIpSpaceAstNode(
                InterfaceLocationAstNode.createFromNodeInterface(
                    new NameNodeAstNode(node1), new NameInterfaceAstNode(iface1))),
            ctxt),
        equalTo(Ip.parse("3.3.3.0").toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceIpWildcard() {
    IpWildcard wildcard = new IpWildcard("1.1.1.1:255.255.255.255");
    assertThat(
        computeIpSpace(new IpWildcardAstNode(wildcard), _emptyCtxt), equalTo(wildcard.toIpSpace()));
  }

  @Test
  public void testComputeIpSpacePrefix() {
    Prefix pfx = Prefix.parse("1.1.1.1/24");
    assertThat(computeIpSpace(new PrefixAstNode(pfx), _emptyCtxt), equalTo(pfx.toIpSpace()));
  }
}
