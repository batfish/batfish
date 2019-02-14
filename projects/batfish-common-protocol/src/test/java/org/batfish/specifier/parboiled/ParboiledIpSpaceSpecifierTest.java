package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier.computeIpSpace;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
    String ip1 = "1.1.1.1";
    String ip2 = "1.1.1.10";
    assertThat(
        computeIpSpace(new CommaIpSpaceAstNode(new IpAstNode(ip1), new IpAstNode(ip2)), _emptyCtxt),
        equalTo(AclIpSpace.union(Ip.parse(ip1).toIpSpace(), Ip.parse(ip2).toIpSpace())));
  }

  @Test
  public void testComputeIpSpaceIp() {
    String ip = "1.1.1.1";
    assertThat(computeIpSpace(new IpAstNode(ip), _emptyCtxt), equalTo(Ip.parse(ip).toIpSpace()));
  }

  @Test
  public void testComputeIpSpaceIpRange() {
    String ip1 = "1.1.1.1";
    String ip2 = "1.1.1.10";
    assertThat(
        computeIpSpace(new IpRangeAstNode(ip1, ip2), _emptyCtxt),
        equalTo(IpRange.range(Ip.parse(ip1), Ip.parse(ip2))));
  }

  @Test
  public void testComputeIpSpaceIpWildcard() {
    String wildcard = "1.1.1.1:255.255.255.255";
    assertThat(
        computeIpSpace(new IpWildcardAstNode(wildcard), _emptyCtxt),
        equalTo(new IpWildcard(wildcard).toIpSpace()));
  }

  @Test
  public void testComputeIpSpacePrefix() {
    String pfx = "1.1.1.1/24";
    assertThat(
        computeIpSpace(new PrefixAstNode(pfx), _emptyCtxt), equalTo(Prefix.parse(pfx).toIpSpace()));
  }
}
