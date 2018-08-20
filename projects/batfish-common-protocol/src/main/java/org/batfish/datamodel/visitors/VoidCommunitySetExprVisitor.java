package org.batfish.datamodel.visitors;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunityHalvesExpr;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityConjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;

public interface VoidCommunitySetExprVisitor {

  void visitCommunityHalvesExpr(CommunityHalvesExpr communityHalvesExpr);

  void visitCommunityList(CommunityList communityList);

  void visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr);

  void visitLiteralCommunity(LiteralCommunity literalCommunity);

  void visitLiteralCommunityConjunction(LiteralCommunityConjunction literalCommunityConjunction);

  void visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet);

  void visitNamedCommunitySet(NamedCommunitySet namedCommunitySet);

  void visitRegexCommunitySet(RegexCommunitySet regexCommunitySet);
}
