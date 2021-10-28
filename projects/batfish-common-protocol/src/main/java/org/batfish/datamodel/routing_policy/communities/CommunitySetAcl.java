package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** An access-control list for matching a {@link CommunitySet}. */
public final class CommunitySetAcl extends CommunitySetMatchExpr {

  public static CommunitySetMatchExpr acl(List<CommunitySetAclLine> lines) {
    if (lines.size() == 1) {
      CommunitySetAclLine line = lines.get(0);
      if (line.getAction() == LineAction.PERMIT) {
        return line.getCommunitySetMatchExpr();
      }
      // A single deny line -> match any of nothing.
      return CommunitySetMatchAny.matchAny(ImmutableSet.of());
    }
    return new CommunitySetAcl(lines);
  }

  @VisibleForTesting
  public CommunitySetAcl(List<CommunitySetAclLine> lines) {
    _lines = ImmutableList.copyOf(lines);
  }

  @JsonProperty(PROP_LINES)
  public @Nonnull List<CommunitySetAclLine> getLines() {
    return _lines;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetAcl)) {
      return false;
    }
    return _lines.equals(((CommunitySetAcl) obj)._lines);
  }

  @Override
  public int hashCode() {
    return _lines.hashCode();
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetAcl(this, arg);
  }

  private static final String PROP_LINES = "lines";

  @JsonCreator
  private static @Nonnull CommunitySetAcl create(
      @JsonProperty(PROP_LINES) @Nullable Iterable<CommunitySetAclLine> lines) {
    return new CommunitySetAcl(ImmutableList.copyOf(firstNonNull(lines, ImmutableList.of())));
  }

  private final @Nonnull List<CommunitySetAclLine> _lines;
}
