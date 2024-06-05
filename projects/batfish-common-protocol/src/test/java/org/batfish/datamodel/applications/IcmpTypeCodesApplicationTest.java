package org.batfish.datamodel.applications;

import static org.batfish.datamodel.applications.IcmpTypeCodesApplication.ALL_CODES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

public class IcmpTypeCodesApplicationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new IcmpTypeCodesApplication(0, ImmutableList.of(ALL_CODES)))
        // different type
        .addEqualityGroup(new IcmpTypeCodesApplication(1, ImmutableList.of(ALL_CODES)))
        // different code
        .addEqualityGroup(new IcmpTypeCodesApplication(0, ImmutableList.of(SubRange.singleton(8))))
        .addEqualityGroup(new IcmpTypeCodesApplication(0, ImmutableList.of(new SubRange(0, 8))))
        .addEqualityGroup(new IcmpTypeCodesApplication(0, ImmutableList.of(new SubRange(8, 0))))
        .addEqualityGroup(
            new IcmpTypeCodesApplication(
                0, ImmutableList.of(SubRange.singleton(0), SubRange.singleton(8))))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(
        new IcmpTypeCodesApplication(0, ImmutableList.of(ALL_CODES)).toString(), equalTo("icmp/0"));
    assertThat(
        new IcmpTypeCodesApplication(0, ImmutableList.of(new SubRange(0, 8))).toString(),
        equalTo("icmp/0/0-8"));
    assertThat(
        new IcmpTypeCodesApplication(0, ImmutableList.of(SubRange.singleton(4), new SubRange(0, 8)))
            .toString(),
        equalTo("icmp/0/4,0-8"));
  }

  @Test
  public void testToAclLineMatchExpr() {
    assertEquals(
        new IcmpTypeCodesApplication(0, ImmutableList.of(new SubRange(1, 10), new SubRange(20, 30)))
            .toAclLineMatchExpr(),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(0)
                .setIcmpCodes(ImmutableList.of(new SubRange(1, 10), new SubRange(20, 30)))
                .build()));
  }
}
