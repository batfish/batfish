package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.IntMatchExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;

/** Represents a "from community-count" line in a {@link PsTerm} */
public final class PsFromCommunityCount extends PsFrom {
  public enum Mode {
    /** Matches routes with exactly this number of communities. */
    EXACT,
    /** Matches routes with fewer than this number of communities. */
    ORLOWER,
    /** Matches routes with greater than this number of communities. */
    ORHIGHER,
  }

  private final int _count;
  private final @Nonnull Mode _mode;

  public PsFromCommunityCount(int count, @Nonnull Mode mode) {
    checkArgument(count >= 0, "Expecting count %s >= 0", count);
    _count = count;
    _mode = mode;
  }

  @Override
  public org.batfish.datamodel.routing_policy.expr.BooleanExpr toBooleanExpr(
      JuniperConfiguration jc,
      org.batfish.datamodel.Configuration c,
      org.batfish.common.Warnings warnings) {
    IntComparator cmp;
    if (_mode == Mode.EXACT) {
      cmp = IntComparator.EQ;
    } else if (_mode == Mode.ORHIGHER) {
      cmp = IntComparator.GE;
    } else {
      assert _mode == Mode.ORLOWER;
      cmp = IntComparator.LE;
    }
    IntMatchExpr expr = new IntComparison(cmp, new LiteralInt(_count));
    return new MatchCommunities(InputCommunities.instance(), new HasSize(expr));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsFromCommunityCount)) {
      return false;
    }
    PsFromCommunityCount that = (PsFromCommunityCount) o;
    return _count == that._count && _mode == that._mode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_count, _mode);
  }
}
