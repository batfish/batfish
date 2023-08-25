package org.batfish.minesweeper.collectors;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community-list names in a {@link CommunitySetMatchExpr}. */
@ParametersAreNonnullByDefault
public class CommunitySetMatchExprCollector
    implements CommunitySetMatchExprVisitor<Set<String>, Tuple<Set<String>, Configuration>> {
  private static final Logger LOGGER = LogManager.getLogger(CommunitySetMatchExprCollector.class);

  @Override
  public Set<String> visitCommunitySetAcl(
      CommunitySetAcl communitySetAcl, Tuple<Set<String>, Configuration> arg) {
    return visitAll(
        communitySetAcl.getLines().stream()
            .map(CommunitySetAclLine::getCommunitySetMatchExpr)
            .collect(ImmutableList.toImmutableList()),
        arg);
  }

  @Override
  public Set<String> visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, Tuple<Set<String>, Configuration> arg) {
    return visitAll(communitySetMatchAll.getExprs(), arg);
  }

  @Override
  public Set<String> visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, Tuple<Set<String>, Configuration> arg) {
    return visitAll(communitySetMatchAny.getExprs(), arg);
  }

  @Override
  public Set<String> visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference,
      Tuple<Set<String>, Configuration> arg) {
    String name = communitySetMatchExprReference.getName();
    // In case we have already dereferenced this community in this visit, issue a warning and return
    // an empty set.
    if (arg.getFirst().contains(name)) {
      LOGGER.warn("Cycle detected in communities - this denotes a malformed snapshot.");
      return ImmutableSet.of();
    }
    CommunitySetMatchExpr expr = arg.getSecond().getCommunitySetMatchExprs().get(name);
    // Expr should exist, enforced during conversion by CommunityStructuresVerifier.
    checkState(expr != null, "Undefined reference in community exprs should not be possible");

    // Add this name to set of seen references.
    Set<String> newSeen = new HashSet<>(arg.getFirst());
    newSeen.add(name);

    return ImmutableSet.<String>builder()
        .add(name)
        .addAll(expr.accept(this, new Tuple<>(newSeen, arg.getSecond())))
        .build();
  }

  @Override
  public Set<String> visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitCommunitySetNot(
      CommunitySetNot communitySetNot, Tuple<Set<String>, Configuration> arg) {
    return communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public Set<String> visitHasCommunity(
      HasCommunity hasCommunity, Tuple<Set<String>, Configuration> arg) {
    return hasCommunity.getExpr().accept(new CommunityMatchExprCollector(), arg);
  }

  @Override
  public Set<String> visitHasSize(HasSize hasSize, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  private Set<String> visitAll(
      Collection<CommunitySetMatchExpr> exprs, Tuple<Set<String>, Configuration> arg) {
    return exprs.stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
