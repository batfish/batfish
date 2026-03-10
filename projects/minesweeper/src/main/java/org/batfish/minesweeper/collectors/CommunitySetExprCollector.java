package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunityExprsSet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExprVisitor;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community-lists referenced in a {@link CommunitySetExpr}. */
@ParametersAreNonnullByDefault
public class CommunitySetExprCollector
    implements CommunitySetExprVisitor<Set<String>, Tuple<Set<String>, Configuration>> {

  private static final Logger LOGGER = LogManager.getLogger(CommunitySetExprCollector.class);

  @Override
  public Set<String> visitCommunityExprsSet(
      CommunityExprsSet communityExprsSet, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitCommunitySetDifference(
      CommunitySetDifference communitySetDifference, Tuple<Set<String>, Configuration> arg) {
    Set<String> s1 = communitySetDifference.getInitial().accept(this, arg);
    Set<String> s2 =
        communitySetDifference.getRemovalCriterion().accept(new CommunityMatchExprCollector(), arg);
    return ImmutableSet.<String>builder().addAll(s1).addAll(s2).build();
  }

  @Override
  public Set<String> visitCommunitySetExprReference(
      CommunitySetExprReference communitySetExprReference, Tuple<Set<String>, Configuration> arg) {
    String name = communitySetExprReference.getName();
    // In case we have already dereferenced this community in this visit, issue a warning and return
    // an empty set.
    if (arg.getFirst().contains(name)) {
      LOGGER.warn("Cycle detected in communities - this denotes a malformed snapshot.");
      return ImmutableSet.of();
    }

    CommunitySetExpr setExpr = arg.getSecond().getCommunitySetExprs().get(name);
    if (setExpr == null) {
      throw new BatfishException("Cannot find community set expression: " + name);
    }

    // Add this name to set of seen references.
    Set<String> newSeen = new HashSet<>(arg.getFirst());
    newSeen.add(name);

    return ImmutableSet.<String>builder()
        .add(name)
        .addAll(setExpr.accept(this, new Tuple<>(newSeen, arg.getSecond())))
        .build();
  }

  @Override
  public Set<String> visitCommunitySetReference(
      CommunitySetReference communitySetReference, Tuple<Set<String>, Configuration> arg) {
    String name = communitySetReference.getName();
    return ImmutableSet.of(name);
  }

  @Override
  public Set<String> visitCommunitySetUnion(
      CommunitySetUnion communitySetUnion, Tuple<Set<String>, Configuration> arg) {
    return communitySetUnion.getExprs().stream()
        .flatMap(e -> e.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<String> visitInputCommunities(
      InputCommunities inputCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitLiteralCommunitySet(
      LiteralCommunitySet literalCommunitySet, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of();
  }
}
