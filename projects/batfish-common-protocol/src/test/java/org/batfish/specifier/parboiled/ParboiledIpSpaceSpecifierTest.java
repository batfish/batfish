package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier.computeIpSpace;
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
import org.batfish.specifier.MockSpecifierContext;
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
            new AddressGroupAstNode(new StringAstNode(addressGroup), new StringAstNode(book)),
            ctxt),
        equalTo(new IpWildcard(ip).toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceComma() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.10");
    assertThat(
        computeIpSpace(new CommaIpSpaceAstNode(new IpAstNode(ip1), new IpAstNode(ip2)), _emptyCtxt),
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
