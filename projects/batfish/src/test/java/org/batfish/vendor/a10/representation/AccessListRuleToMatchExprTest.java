package org.batfish.vendor.a10.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.TrueExpr;
import org.junit.Test;

/** Tests of {@link AccessListRuleToMatchExpr}. */
public class AccessListRuleToMatchExprTest {
  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testSourceAddrConstraint() {
    Ip src = Ip.parse("10.9.8.7");
    AccessListRule ruleSrc =
        new AccessListRuleIp(
            AccessListRule.Action.DENY,
            new AccessListAddressHost(src),
            AccessListAddressAny.INSTANCE,
            "lineText");
    AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleSrc);
    assertThat(_tb.toBDD(matchExpr), equalTo(_tb.toBDD(AclLineMatchExprs.matchSrc(src))));
  }

  @Test
  public void testDestAddrConstraint() {
    Ip dest = Ip.parse("10.11.12.13");
    AccessListRule ruleDest =
        new AccessListRuleIp(
            AccessListRule.Action.DENY,
            AccessListAddressAny.INSTANCE,
            new AccessListAddressHost(dest),
            "lineText");
    AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleDest);
    assertThat(_tb.toBDD(matchExpr), equalTo(_tb.toBDD(AclLineMatchExprs.matchDst(dest))));
  }

  @Test
  public void testIp() {
    AccessListRule ruleIp =
        new AccessListRuleIp(
            AccessListRule.Action.DENY,
            AccessListAddressAny.INSTANCE,
            AccessListAddressAny.INSTANCE,
            "lineText");
    AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleIp);
    assertThat(_tb.toBDD(matchExpr), equalTo(_tb.toBDD(TrueExpr.INSTANCE)));
  }

  @Test
  public void testIcmp() {
    AccessListRule ruleIcmp =
        new AccessListRuleIcmp(
            AccessListRule.Action.DENY,
            AccessListAddressAny.INSTANCE,
            AccessListAddressAny.INSTANCE,
            "lineText");
    AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleIcmp);
    assertThat(
        _tb.toBDD(matchExpr),
        equalTo(_tb.toBDD(AclLineMatchExprs.matchIpProtocol(IpProtocol.ICMP))));
  }

  @Test
  public void testTcp() {
    AccessListRuleTcp ruleTcp =
        new AccessListRuleTcp(
            AccessListRule.Action.DENY,
            AccessListAddressAny.INSTANCE,
            AccessListAddressAny.INSTANCE,
            "lineText");
    // No port constraint
    {
      AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleTcp);
      assertThat(
          _tb.toBDD(matchExpr),
          equalTo(_tb.toBDD(AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP))));
    }

    // With port constraint
    {
      ruleTcp.setDestinationRange(new SubRange(443, 444));
      AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleTcp);
      assertThat(
          _tb.toBDD(matchExpr),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.and(
                      AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP),
                      AclLineMatchExprs.matchDstPort(IntegerSpace.of(Range.closed(443, 444)))))));
    }
  }

  @Test
  public void testUdp() {
    AccessListRuleUdp ruleUdp =
        new AccessListRuleUdp(
            AccessListRule.Action.DENY,
            AccessListAddressAny.INSTANCE,
            AccessListAddressAny.INSTANCE,
            "lineText");

    // No port constraint
    {
      AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleUdp);
      assertThat(
          _tb.toBDD(matchExpr),
          equalTo(_tb.toBDD(AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP))));
    }
    // With port constraint
    {
      ruleUdp.setDestinationRange(new SubRange(443, 444));
      AclLineMatchExpr matchExpr = AccessListRuleToMatchExpr.INSTANCE.visit(ruleUdp);
      assertThat(
          _tb.toBDD(matchExpr),
          equalTo(
              _tb.toBDD(
                  AclLineMatchExprs.and(
                      AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP),
                      AclLineMatchExprs.matchDstPort(IntegerSpace.of(Range.closed(443, 444)))))));
    }
  }
}
