package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs.StaticBooleanExpr;

/** A visitor of {@link BooleanExpr} that takes 1 generic argument and returns a generic value. */
public interface BooleanExprVisitor<T, U> {

  T visitBooleanExprs(StaticBooleanExpr staticBooleanExpr, U arg);

  T visitCallExpr(CallExpr callExpr, U arg);

  T visitConjunction(Conjunction conjunction, U arg);

  T visitConjunctionChain(ConjunctionChain conjunctionChain, U arg);

  T visitDisjunction(Disjunction disjunction, U arg);

  T visitFirstMatchChain(FirstMatchChain firstMatchChain, U arg);

  T visitHasRoute(HasRoute hasRoute, U arg);

  T visitHasRoute6(HasRoute6 hasRoute6, U arg);

  T visitMatchAsPath(MatchAsPath matchAsPath, U arg);

  T visitMatchColor(MatchColor matchColor, U arg);

  T visitMatchCommunities(MatchCommunities matchCommunities, U arg);

  T visitMatchCommunitySet(MatchCommunitySet matchCommunitySet, U arg);

  T visitMatchIp6AccessList(MatchIp6AccessList matchIp6AccessList, U arg);

  T visitMatchIpv4(MatchIpv4 matchIpv4, U arg);

  T visitMatchIpv6(MatchIpv6 matchIpv6, U arg);

  T visitMatchLocalPreference(MatchLocalPreference matchLocalPreference, U arg);

  T visitMatchLocalRouteSourcePrefixLength(
      MatchLocalRouteSourcePrefixLength matchLocalRouteSourcePrefixLength, U arg);

  T visitMatchMetric(MatchMetric matchMetric, U arg);

  T visitMatchPrefix6Set(MatchPrefix6Set matchPrefix6Set, U arg);

  T visitMatchPrefixSet(MatchPrefixSet matchPrefixSet, U arg);

  T visitMatchProcessAsn(MatchProcessAsn matchProcessAsn, U arg);

  T visitMatchProtocol(MatchProtocol matchProtocol, U arg);

  T visitMatchRouteType(MatchRouteType matchRouteType, U arg);

  T visitMatchSourceVrf(MatchSourceVrf matchSourceVrf, U arg);

  T visitMatchTag(MatchTag matchTag, U arg);

  T visitNeighborIsAsPath(NeighborIsAsPath neighborIsAsPath, U arg);

  T visitNot(Not not, U arg);

  T visitOriginatesFromAsPath(OriginatesFromAsPath originatesFromAsPath, U arg);

  T visitPassesThroughAsPath(PassesThroughAsPath passesThroughAsPath, U arg);

  T visitRouteIsClassful(RouteIsClassful routeIsClassful, U arg);

  T visitWithEnvironmentExpr(WithEnvironmentExpr withEnvironmentExpr, U arg);
}
