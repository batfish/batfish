package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An access-control list for matching individual {@link
 * org.batfish.datamodel.bgp.community.Community} instances.
 */
public final class CommunityAcl extends CommunityMatchExpr {

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
