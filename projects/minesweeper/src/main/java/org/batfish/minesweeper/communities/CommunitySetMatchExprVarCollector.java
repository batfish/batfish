package org.batfish.minesweeper.communities;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Collection;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.TypesFirstAscendingSpaceSeparated;
import org.batfish.minesweeper.CommunityVar;

/** Collect all community literals and regexes in a {@link CommunitySetMatchExpr}. */
@ParametersAreNonnullByDefault
public class CommunitySetMatchExprVarCollector
    implements CommunitySetMatchExprVisitor<Set<CommunityVar>, Configuration> {
  @Override
  public Set<CommunityVar> visitCommunitySetAcl(
      CommunitySetAcl communitySetAcl, Configuration arg) {
    return visitAll(
        communitySetAcl.getLines().stream()
            .map(CommunitySetAclLine::getCommunitySetMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, Configuration arg) {
    return visitAll(communitySetMatchAll.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, Configuration arg) {
    return visitAll(communitySetMatchAny.getExprs(), arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, Configuration arg) {
    String name = communitySetMatchExprReference.getName();
    CommunitySetMatchExpr expr = arg.getCommunitySetMatchExprs().get(name);
    // Expr should exist, enforced during conversion by CommunityStructuresVerifier.
    checkState(expr != null, "Undefined reference in community exprs should not be possible");
    return expr.accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, Configuration arg) {
    if (!(communitySetMatchRegex.getCommunitySetRendering()
        instanceof TypesFirstAscendingSpaceSeparated)) {
      throw new UnsupportedOperationException(
          "Unsupported community set rendering "
              + communitySetMatchRegex.getCommunitySetRendering());
    }
    String regex = communitySetMatchRegex.getRegex();
    // a conservative check if the regex only matches on the existence of a single community in the
    // set -- the regex optionally starts with _, optionally ends with _, and in the middle never
    // matches against the characters represented by an underscore
    String underscore = StringEscapeUtils.unescapeJava(DEFAULT_UNDERSCORE_REPLACEMENT);
    if (regex.startsWith(underscore)) {
      regex = regex.substring(underscore.length());
    }
    if (regex.endsWith(underscore)) {
      regex = regex.substring(0, regex.length() - underscore.length());
    }
    Automaton regexAuto = new RegExp(regex).toAutomaton();
    Automaton digitsAndColons = new RegExp("[0-9:]+").toAutomaton();
    if (regexAuto.intersection(digitsAndColons).equals(regexAuto)) {
      return ImmutableSet.of(CommunityVar.from(communitySetMatchRegex.getRegex()));
    } else {
      throw new UnsupportedOperationException(
          "Unsupported community set regex: " + communitySetMatchRegex.getRegex());
    }
  }

  @Override
  public Set<CommunityVar> visitCommunitySetNot(
      CommunitySetNot communitySetNot, Configuration arg) {
    return communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public Set<CommunityVar> visitHasCommunity(HasCommunity hasCommunity, Configuration arg) {
    return hasCommunity.getExpr().accept(new CommunityMatchExprVarCollector(), arg);
  }

  @Override
  public Set<CommunityVar> visitHasSize(HasSize hasSize, Configuration arg) {
    return ImmutableSet.of();
  }

  private Set<CommunityVar> visitAll(Collection<CommunitySetMatchExpr> exprs, Configuration arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
