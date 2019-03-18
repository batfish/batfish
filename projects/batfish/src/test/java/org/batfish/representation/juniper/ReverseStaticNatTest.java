package org.batfish.representation.juniper;

import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Prefix;
import org.batfish.representation.juniper.Nat.Type;
import org.junit.Test;

public class ReverseStaticNatTest {

  @Test
  public void testThenToMatch() {
    Prefix prefix = Prefix.parse("1.1.1.1/24");
    NatRuleThen then = new NatRuleThenPrefix(prefix, DESTINATION);
    assertThat(ReverseStaticNat.thenToMatch(then), equalTo(new NatRuleMatchSrcAddr(prefix)));

    then = new NatRuleThenPrefixName("prefix", DESTINATION);
    assertThat(ReverseStaticNat.thenToMatch(then), equalTo(new NatRuleMatchSrcAddrName("prefix")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotSupportedThenToMatch1() {
    NatRuleThen then = new NatRuleThenPool("pool");
    ReverseStaticNat.thenToMatch(then);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotSupportedThenToMatch2() {
    NatRuleThen then = NatRuleThenOff.INSTANCE;
    ReverseStaticNat.thenToMatch(then);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotSupportedThenToMatch3() {
    NatRuleThen then = NatRuleThenInterface.INSTANCE;
    ReverseStaticNat.thenToMatch(then);
  }

  @Test
  public void testSrcMatchToDstMatch() {
    Prefix p1 = Prefix.parse("1.1.1.1/24");
    NatRuleMatch srcMatch = new NatRuleMatchSrcAddr(p1);
    assertThat(
        ReverseStaticNat.reverseMatchToMatch(srcMatch), equalTo(new NatRuleMatchDstAddr(p1)));

    String prefixName = "prefix";
    srcMatch = new NatRuleMatchSrcAddrName(prefixName);
    assertThat(
        ReverseStaticNat.reverseMatchToMatch(srcMatch),
        equalTo(new NatRuleMatchDstAddrName(prefixName)));

    srcMatch = new NatRuleMatchSrcPort(10000, 30000);
    assertThat(
        ReverseStaticNat.reverseMatchToMatch(srcMatch),
        equalTo(new NatRuleMatchDstPort(10000, 30000)));

    srcMatch = new NatRuleMatchDstAddr(p1);
    assertNull(ReverseStaticNat.reverseMatchToMatch(srcMatch));

    srcMatch = new NatRuleMatchDstAddrName(prefixName);
    assertNull(ReverseStaticNat.reverseMatchToMatch(srcMatch));

    srcMatch = new NatRuleMatchDstPort(100, 200);
    assertNull(ReverseStaticNat.reverseMatchToMatch(srcMatch));
  }

  @Test
  public void testDstMatchToThen() {
    Prefix p1 = Prefix.parse("1.1.1.1/24");
    NatRuleMatch dstMatch = new NatRuleMatchDstAddr(p1);
    assertThat(
        ReverseStaticNat.reverseMatchToThen(dstMatch), equalTo(new NatRuleThenPrefix(p1, SOURCE)));

    String prefixName = "prefix";
    dstMatch = new NatRuleMatchDstAddrName(prefixName);
    assertThat(
        ReverseStaticNat.reverseMatchToThen(dstMatch),
        equalTo(new NatRuleThenPrefixName(prefixName, SOURCE)));

    dstMatch = new NatRuleMatchDstPort(100, 200);
    assertNull(ReverseStaticNat.reverseMatchToThen(dstMatch));

    dstMatch = new NatRuleMatchSrcAddr(p1);
    assertNull(ReverseStaticNat.reverseMatchToThen(dstMatch));

    dstMatch = new NatRuleMatchSrcAddrName(prefixName);
    assertNull(ReverseStaticNat.reverseMatchToThen(dstMatch));

    dstMatch = new NatRuleMatchSrcPort(10000, 30000);
    assertNull(ReverseStaticNat.reverseMatchToThen(dstMatch));
  }

  @Test
  public void testReverseRule() {
    // src=p1, srcport=[10000,30000], dst=p2 -> translate to p3
    // the revered should be src=p3, dst=p1, dstport=[10000,30000] -> translate to p2
    Prefix p1 = Prefix.parse("1.1.1.1/24");
    Prefix p2 = Prefix.parse("2.2.2.2/24");
    Prefix p3 = Prefix.parse("3.3.3.3/24");
    NatRuleMatch srcMatch = new NatRuleMatchSrcAddr(p1);
    NatRuleMatch srcportMatch = new NatRuleMatchSrcPort(10000, 30000);
    NatRuleMatch dstMatch = new NatRuleMatchDstAddr(p2);
    NatRuleThen thenPrefix = new NatRuleThenPrefix(p3, DESTINATION);

    NatRule rule0 = new NatRule("rule0");

    rule0.getMatches().add(srcMatch);
    rule0.getMatches().add(srcportMatch);
    rule0.getMatches().add(dstMatch);
    rule0.setThen(thenPrefix);

    NatRule reversedRule = ReverseStaticNat.reverseRule(rule0);
    assertThat(reversedRule.getName(), equalTo("rule0"));
    assertThat(reversedRule.getThen(), equalTo(new NatRuleThenPrefix(p2, SOURCE)));
    assertThat(
        reversedRule.getMatches(),
        contains(
            new NatRuleMatchDstAddr(p1),
            new NatRuleMatchDstPort(10000, 30000),
            new NatRuleMatchSrcAddr(p3)));

    // src=p1, dst=p2 -> translate to p3
    // the revered should be src=p3, dst=p1 -> translate to p2
    NatRule rule1 = new NatRule("rule1");

    rule1.getMatches().add(srcMatch);
    rule1.getMatches().add(dstMatch);
    rule1.setThen(thenPrefix);

    reversedRule = ReverseStaticNat.reverseRule(rule1);
    assertThat(reversedRule.getName(), equalTo("rule1"));
    assertThat(reversedRule.getThen(), equalTo(new NatRuleThenPrefix(p2, SOURCE)));
    assertThat(
        reversedRule.getMatches(),
        contains(new NatRuleMatchDstAddr(p1), new NatRuleMatchSrcAddr(p3)));

    // src=p1 dst=p2 -> translate to p3
    // the revered should be src=p3 -> translate to p2
    NatRule rule2 = new NatRule("rule2");

    rule2.getMatches().add(dstMatch);
    rule2.setThen(thenPrefix);

    reversedRule = ReverseStaticNat.reverseRule(rule2);
    assertThat(reversedRule.getName(), equalTo("rule2"));
    assertThat(reversedRule.getThen(), equalTo(new NatRuleThenPrefix(p2, SOURCE)));
    assertThat(reversedRule.getMatches(), contains(new NatRuleMatchSrcAddr(p3)));
  }

  @Test
  public void testReverseRuleSet() {
    Prefix p1 = Prefix.parse("1.1.1.1/24");
    Prefix p2 = Prefix.parse("2.2.2.2/24");
    Prefix p3 = Prefix.parse("3.3.3.3/24");
    NatRuleMatch srcMatch = new NatRuleMatchSrcAddr(p1);
    NatRuleMatch srcportMatch = new NatRuleMatchSrcPort(10000, 30000);
    NatRuleMatch dstMatch = new NatRuleMatchDstAddr(p2);
    NatRuleThen thenPrefix = new NatRuleThenPrefix(p3, DESTINATION);

    NatRule rule0 = new NatRule("rule0");

    rule0.getMatches().add(srcMatch);
    rule0.getMatches().add(srcportMatch);
    rule0.getMatches().add(dstMatch);
    rule0.setThen(thenPrefix);

    NatRuleSet ruleSet = new NatRuleSet("ruleset");
    ruleSet.getFromLocation().setInterface("fromInterface");
    ruleSet.getToLocation().setInterface("toInterface");
    ruleSet.getRules().add(rule0);

    NatRuleSet reversedRuleSet = ReverseStaticNat.reverseRuleSet(ruleSet);
    assertThat(reversedRuleSet.getName(), equalTo("ruleset"));
    assertThat(
        reversedRuleSet.getFromLocation(),
        equalTo(NatPacketLocation.interfaceLocation("toInterface")));
    assertThat(
        reversedRuleSet.getToLocation(),
        equalTo(NatPacketLocation.interfaceLocation("fromInterface")));
    assertThat(reversedRuleSet.getRules(), hasSize(1));

    NatRule reversedRule = reversedRuleSet.getRules().get(0);
    assertThat(reversedRule.getName(), equalTo("rule0"));
    assertThat(reversedRule.getThen(), equalTo(new NatRuleThenPrefix(p2, SOURCE)));
    assertThat(
        reversedRule.getMatches(),
        contains(
            new NatRuleMatchDstAddr(p1),
            new NatRuleMatchDstPort(10000, 30000),
            new NatRuleMatchSrcAddr(p3)));

    ruleSet.getFromLocation().setZone("fromZone");
    ruleSet.getToLocation().setZone("toZone");
    reversedRuleSet = ReverseStaticNat.reverseRuleSet(ruleSet);
    assertThat(
        reversedRuleSet.getFromLocation(), equalTo(NatPacketLocation.zoneLocation("toZone")));
    assertThat(
        reversedRuleSet.getToLocation(), equalTo(NatPacketLocation.zoneLocation("fromZone")));

    ruleSet.getFromLocation().setRoutingInstance("fromRI");
    ruleSet.getToLocation().setRoutingInstance("toRI");
    reversedRuleSet = ReverseStaticNat.reverseRuleSet(ruleSet);
    assertThat(
        reversedRuleSet.getFromLocation(),
        equalTo(NatPacketLocation.routingInstanceLocation("toRI")));
    assertThat(
        reversedRuleSet.getToLocation(),
        equalTo(NatPacketLocation.routingInstanceLocation("fromRI")));
  }

  @Test
  public void testReverseNat() {
    Prefix p1 = Prefix.parse("1.1.1.1/24");
    Prefix p2 = Prefix.parse("2.2.2.2/24");
    Prefix p3 = Prefix.parse("3.3.3.3/24");
    NatRuleMatch srcMatch = new NatRuleMatchSrcAddr(p1);
    NatRuleMatch srcportMatch = new NatRuleMatchSrcPort(10000, 30000);
    NatRuleMatch dstMatch = new NatRuleMatchDstAddr(p2);
    NatRuleThen thenPrefix = new NatRuleThenPrefix(p3, DESTINATION);

    NatRule rule0 = new NatRule("rule0");

    rule0.getMatches().add(srcMatch);
    rule0.getMatches().add(srcportMatch);
    rule0.getMatches().add(dstMatch);
    rule0.setThen(thenPrefix);

    NatRuleSet ruleSet = new NatRuleSet("ruleset");
    ruleSet.getFromLocation().setInterface("fromInterface");
    ruleSet.getToLocation().setInterface("toInterface");
    ruleSet.getRules().add(rule0);

    Nat staticNat = new Nat(Type.STATIC);
    staticNat.getRuleSets().put("ruleset", ruleSet);
    Nat reversedNat = ReverseStaticNat.reverseNat(staticNat);

    assertThat(reversedNat.getType(), equalTo(Type.STATIC));
    assertThat(reversedNat.getRuleSets().keySet(), hasSize(1));

    NatRuleSet reversedRuleSet = reversedNat.getRuleSets().get("ruleset");
    assertThat(reversedRuleSet.getName(), equalTo("ruleset"));
    assertThat(
        reversedRuleSet.getFromLocation(),
        equalTo(NatPacketLocation.interfaceLocation("toInterface")));
    assertThat(
        reversedRuleSet.getToLocation(),
        equalTo(NatPacketLocation.interfaceLocation("fromInterface")));
    assertThat(reversedRuleSet.getRules(), hasSize(1));

    NatRule reversedRule = reversedRuleSet.getRules().get(0);
    assertThat(reversedRule.getName(), equalTo("rule0"));
    assertThat(reversedRule.getThen(), equalTo(new NatRuleThenPrefix(p2, SOURCE)));
    assertThat(
        reversedRule.getMatches(),
        contains(
            new NatRuleMatchDstAddr(p1),
            new NatRuleMatchDstPort(10000, 30000),
            new NatRuleMatchSrcAddr(p3)));
  }
}
