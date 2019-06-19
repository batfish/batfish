package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class ReverseStaticNat {

  private static final NatRuleMatchVisitor<NatRuleMatch> SRC_MATCH_TO_DST_MATCH =
      new NatRuleMatchVisitor<NatRuleMatch>() {
        @Override
        public NatRuleMatch visitNatRuleMatchDstAddr(NatRuleMatchDstAddr natRuleMatchDstAddr) {
          return null;
        }

        @Override
        public NatRuleMatch visitNatRuleMatchDstAddrName(
            NatRuleMatchDstAddrName natRuleMatchDstAddrName) {
          return null;
        }

        @Override
        public NatRuleMatch visitNatRuleMatchDstPort(NatRuleMatchDstPort natRuleMatchDstPort) {
          return null;
        }

        @Override
        public NatRuleMatch visitNatRuleMatchSrcAddr(NatRuleMatchSrcAddr natRuleMatchSrcAddr) {
          return new NatRuleMatchDstAddr(natRuleMatchSrcAddr.getPrefix());
        }

        @Override
        public NatRuleMatch visitNatRuleMatchSrcAddrName(
            NatRuleMatchSrcAddrName natRuleMatchSrcAddrName) {
          return new NatRuleMatchDstAddrName(natRuleMatchSrcAddrName.getName());
        }

        @Override
        public NatRuleMatch visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort) {
          return new NatRuleMatchDstPort(
              natRuleMatchSrcPort.getFrom(), natRuleMatchSrcPort.getTo());
        }
      };

  private static final NatRuleMatchVisitor<NatRuleThen> DST_MATCH_TO_THEN =
      new NatRuleMatchVisitor<NatRuleThen>() {
        @Override
        public NatRuleThen visitNatRuleMatchDstAddr(NatRuleMatchDstAddr natRuleMatchDstAddr) {
          return new NatRuleThenPrefix(natRuleMatchDstAddr.getPrefix(), SOURCE);
        }

        @Override
        public NatRuleThen visitNatRuleMatchDstAddrName(
            NatRuleMatchDstAddrName natRuleMatchDstAddrName) {
          return new NatRuleThenPrefixName(natRuleMatchDstAddrName.getName(), SOURCE);
        }

        @Override
        public NatRuleThen visitNatRuleMatchDstPort(NatRuleMatchDstPort natRuleMatchDstPort) {
          return null;
        }

        @Override
        public NatRuleThen visitNatRuleMatchSrcAddr(NatRuleMatchSrcAddr natRuleMatchSrcAddr) {
          return null;
        }

        @Override
        public NatRuleThen visitNatRuleMatchSrcAddrName(
            NatRuleMatchSrcAddrName natRuleMatchSrcAddrName) {
          return null;
        }

        @Override
        public NatRuleThen visitNatRuleMatchSrcPort(NatRuleMatchSrcPort natRuleMatchSrcPort) {
          return null;
        }
      };

  static Nat reverseNat(Nat nat) {
    checkArgument(nat.getType() == STATIC, "Cannot reverse nat with type " + nat.getType());
    checkArgument(nat.getPools().isEmpty(), "Static nat should not have pools");
    Nat reversedNat = new Nat(STATIC);
    // from port and to port denote the range of ports, meaning the interval [from_port, to_port],
    // not the direction of traffic. so should keep them the same in reversed nat
    reversedNat.setDefaultFromPort(nat.getDefaultFromPort());
    reversedNat.setDefaultToPort(nat.getDefaultToPort());
    reversedNat
        .getRuleSets()
        .putAll(
            nat.getRuleSets().entrySet().stream()
                .collect(
                    Collectors.toMap(Entry::getKey, entry -> reverseRuleSet(entry.getValue()))));
    return reversedNat;
  }

  @VisibleForTesting
  static NatRuleSet reverseRuleSet(NatRuleSet ruleSet) {
    NatRuleSet reversedRuleSet = new NatRuleSet(ruleSet.getName());
    NatPacketLocation fromLoc = ruleSet.getFromLocation();
    NatPacketLocation toLoc = ruleSet.getToLocation();
    reversedRuleSet.getToLocation().set(fromLoc.getType(), fromLoc.getName());
    reversedRuleSet.getFromLocation().set(toLoc.getType(), toLoc.getName());

    ruleSet.getRules().forEach(rule -> reversedRuleSet.getRules().add(reverseRule(rule)));

    return reversedRuleSet;
  }

  @VisibleForTesting
  static NatRuleMatch reverseMatchToMatch(NatRuleMatch match) {
    return match.accept(SRC_MATCH_TO_DST_MATCH);
  }

  @VisibleForTesting
  static NatRuleThen reverseMatchToThen(NatRuleMatch match) {
    return match.accept(DST_MATCH_TO_THEN);
  }

  @VisibleForTesting
  static NatRule reverseRule(NatRule rule) {
    NatRule reversedRule = new NatRule(rule.getName());
    List<NatRuleMatch> matchList = rule.getMatches();
    matchList.forEach(
        match -> {
          NatRuleMatch reversedDstMatch = reverseMatchToMatch(match);
          NatRuleThen reversedThen = reverseMatchToThen(match);
          if (reversedDstMatch != null) {
            reversedRule.getMatches().add(reversedDstMatch);
          }
          if (reversedThen != null) {
            // only one dst-match is allowed, so just set the then
            assert reversedRule.getThen() == null;
            reversedRule.setThen(reversedThen);
          }
        });
    reversedRule.getMatches().add(thenToMatch(rule.getThen()));
    return reversedRule;
  }

  @VisibleForTesting
  static NatRuleMatch thenToMatch(NatRuleThen then) {
    checkArgument(
        (then instanceof NatRuleThenPrefix) || (then instanceof NatRuleThenPrefixName),
        "Not supported type of then in static nat: " + then);
    if (then instanceof NatRuleThenPrefix) {
      NatRuleThenPrefix thenPrefix = (NatRuleThenPrefix) then;
      return new NatRuleMatchSrcAddr(thenPrefix.getPrefix());
    }

    NatRuleThenPrefixName thenPrefix = (NatRuleThenPrefixName) then;
    return new NatRuleMatchSrcAddrName(thenPrefix.getName());
  }
}
