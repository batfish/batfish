package org.batfish.datamodel.visitors;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunityHalvesExpr;
import org.batfish.datamodel.routing_policy.expr.EmptyCommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityConjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;

public interface CommunitySetExprVisitor<T> {

  //  T castToGenericCommunitySetExprVisitorReturnType(Object o);

  T visitCommunityHalvesExpr(CommunityHalvesExpr communityHalvesExpr);

  T visitCommunityList(CommunityList communityList);

  T visitEmptyCommunitySetExpr(EmptyCommunitySetExpr emptyCommunitySetExpr);

  T visitLiteralCommunity(LiteralCommunity literalCommunity);

  T visitLiteralCommunityConjunction(LiteralCommunityConjunction literalCommunityConjunction);

  T visitLiteralCommunitySet(LiteralCommunitySet literalCommunitySet);

  T visitNamedCommunitySet(NamedCommunitySet namedCommunitySet);

  T visitRegexCommunitySet(RegexCommunitySet regexCommunitySet);
}
