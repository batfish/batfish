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

/**
 * An access-control list for matching individual {@link
 * org.batfish.datamodel.bgp.community.Community} instances.
 */
public final class CommunityAcl extends CommunityMatchExpr {

  public static CommunityMatchExpr acl(List<CommunityAclLine> lines) {
    if (lines.size() == 1) {
      CommunityAclLine line = lines.get(0);
      if (line.getAction() == LineAction.PERMIT) {
        return line.getCommunityMatchExpr();
      } else {
        // Only deny line, same as matching any of nothing.
        return CommunityMatchAny.matchAny(ImmutableSet.of());
      }
    }
    return new CommunityAcl(lines);
  }

  @VisibleForTesting
  public CommunityAcl(List<CommunityAclLine> lines) {
    _lines = ImmutableList.copyOf(lines);
  }

  @JsonProperty(PROP_LINES)
  public @Nonnull List<CommunityAclLine> getLines() {
    return _lines;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityAcl)) {
      return false;
    }
    return _lines.equals(((CommunityAcl) obj)._lines);
  }

  @Override
  public int hashCode() {
    return _lines.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityAcl(this, arg);
  }

  private static final String PROP_LINES = "lines";

  @JsonCreator
  private static @Nonnull CommunityAcl create(
      @JsonProperty(PROP_LINES) @Nullable Iterable<CommunityAclLine> lines) {
    return new CommunityAcl(ImmutableList.copyOf(firstNonNull(lines, ImmutableList.of())));
  }

  private final @Nonnull List<CommunityAclLine> _lines;
}
