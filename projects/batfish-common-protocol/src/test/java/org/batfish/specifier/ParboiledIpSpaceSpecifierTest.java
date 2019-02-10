package org.batfish.specifier;

import static org.batfish.specifier.ParboiledIpSpaceSpecifier.computeIpSpace;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.specifier.parboiled.IpSpaceAstNode;
import org.batfish.specifier.parboiled.IpSpaceAstNode.Type;
import org.batfish.specifier.parboiled.LeafAstNode;
import org.junit.Test;

public class ParboiledIpSpaceSpecifierTest {

  private MockSpecifierContext _emptyCtxt = MockSpecifierContext.builder().build();

  @Test
  public void testComputerIpSpaceAddressGroup() {
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
            new IpSpaceAstNode(
                Type.ADDRESS_GROUP, new LeafAstNode(addressGroup), new LeafAstNode(book)),
            ctxt),
        equalTo(new IpWildcard(ip).toIpSpace()));
  }

  @Test
  public void testComputerIpSpaceComma() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.10");
    assertThat(
        computeIpSpace(
            new IpSpaceAstNode(Type.COMMA, new LeafAstNode(ip1), new LeafAstNode(ip2)), _emptyCtxt),
        equalTo(AclIpSpace.union(ip1.toIpSpace(), ip2.toIpSpace())));
  }

  @Test
  public void testComputeIpSpaceIp() {
    Ip ip = Ip.parse("1.1.1.1");
    assertThat(computeIpSpace(new LeafAstNode(ip), _emptyCtxt), equalTo(ip.toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceIpRange() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.10");
    assertThat(
        computeIpSpace(
            new IpSpaceAstNode(Type.RANGE, new LeafAstNode(ip1), new LeafAstNode(ip2)), _emptyCtxt),
        equalTo(IpRange.range(ip1, ip2)));
  }

  @Test
  public void testComputeIpSpaceIpWildcard() {
    IpWildcard wildcard = new IpWildcard("1.1.1.1:255.255.255.255");
    assertThat(
        computeIpSpace(new LeafAstNode(wildcard), _emptyCtxt), equalTo(wildcard.toIpSpace()));
  }

  @Test
  public void testComputeIpSpacePrefix() {
    Prefix pfx = Prefix.parse("1.1.1.1/24");
    assertThat(computeIpSpace(new LeafAstNode(pfx), _emptyCtxt), equalTo(pfx.toIpSpace()));
  }
}
