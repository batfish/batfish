package org.batfish.representation.cisco;

import static org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence.anyAs;
import static org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence.localAsOrConfedIfNeighborNotInConfed;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType.Type;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.ReplaceAsesInAsSequence;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * If eBGP session, replace every AS in the AS-path with local AS, or the confederation ID if in a
 * confederation and neighbor is not in the confederation.
 */
public final class RouteMapSetAsPathReplaceAnyLine extends RouteMapSetLine {

  public static @Nonnull RouteMapSetAsPathReplaceAnyLine instance() {
    return INSTANCE;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        new If(
            new MatchBgpSessionType(Type.EBGP),
            ImmutableList.of(
                new ReplaceAsesInAsSequence(anyAs(), localAsOrConfedIfNeighborNotInConfed()))));
  }

  private static final @Nonnull RouteMapSetAsPathReplaceAnyLine INSTANCE =
      new RouteMapSetAsPathReplaceAnyLine();

  private RouteMapSetAsPathReplaceAnyLine() {}
}
