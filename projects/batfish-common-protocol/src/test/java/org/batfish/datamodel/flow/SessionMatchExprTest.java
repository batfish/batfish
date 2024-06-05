package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

public class SessionMatchExprTest {
  @Test
  public void testEquals() {
    SessionMatchExpr expr =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null);
    new EqualsTester()
        .addEqualityGroup(
            expr,
            expr,
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.TCP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.3"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.2.2"), Ip.parse("1.1.2.2"), null, null))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 4000, 5000),
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 4000, 5000))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 4040, 5000))
        .addEqualityGroup(
            new SessionMatchExpr(
                IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 4000, 5050))
        .testEquals();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullSrcPort() {
    new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), null, 5000);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullDstPort() {
    new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2"), 5000, null);
  }

  @Test
  public void testJsonSerialization() {
    SessionMatchExpr matcher =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null);
    SessionMatchExpr clone = BatfishObjectMapper.clone(matcher, SessionMatchExpr.class);
    assertThat(matcher, equalTo(clone));

    matcher =
        new SessionMatchExpr(IpProtocol.TCP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 5000, 8000);
    clone = BatfishObjectMapper.clone(matcher, SessionMatchExpr.class);
    assertThat(matcher, equalTo(clone));
  }

  @Test
  public void testToAclLineMatchExpr() {
    SessionMatchExpr matcher =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), null, null);
    MatchHeaderSpace aclExpr = (MatchHeaderSpace) matcher.toAclLineMatchExpr();
    HeaderSpace hs = aclExpr.getHeaderspace();
    HeaderSpace.Builder expectedHeaderSpace =
        HeaderSpace.builder()
            .setSrcIps(Ip.parse("1.1.1.2").toIpSpace())
            .setDstIps(Ip.parse("1.1.2.2").toIpSpace())
            .setIpProtocols(ImmutableSet.of(IpProtocol.ICMP));
    assertThat(hs, equalTo(expectedHeaderSpace.build()));

    matcher =
        new SessionMatchExpr(IpProtocol.ICMP, Ip.parse("1.1.1.2"), Ip.parse("1.1.2.2"), 4000, 5000);
    aclExpr = (MatchHeaderSpace) matcher.toAclLineMatchExpr();
    hs = aclExpr.getHeaderspace();
    expectedHeaderSpace =
        expectedHeaderSpace
            .setSrcPorts(SubRange.singleton(4000))
            .setDstPorts(SubRange.singleton(5000));
    assertThat(hs, equalTo(expectedHeaderSpace.build()));
  }
}
