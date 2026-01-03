package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link AsPathExpr} representing a potentially shortened version of another {@link AsPathExpr}
 * where all sequences of identical {@link org.batfish.datamodel.AsSet}s have been collapsed into a
 * single one.
 */
public final class DedupedAsPath extends AsPathExpr {

  public static @Nonnull DedupedAsPath of(AsPathExpr asPathExpr) {
    return new DedupedAsPath(asPathExpr);
  }

  @Override
  public <T, U> T accept(AsPathExprVisitor<T, U> visitor, U arg) {
    return visitor.visitDedupedAsPath(this, arg);
  }

  @JsonProperty(PROP_AS_PATH_EXPR)
  public @Nonnull AsPathExpr getAsPathExpr() {
    return _asPathExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof DedupedAsPath)) {
      return false;
    }
    DedupedAsPath that = (DedupedAsPath) obj;
    return _asPathExpr.equals(that._asPathExpr);
  }

  @Override
  public int hashCode() {
    return _asPathExpr.hashCode();
  }

  @JsonCreator
  private static @Nonnull DedupedAsPath create(
      @JsonProperty(PROP_AS_PATH_EXPR) @Nullable AsPathExpr asPathExpr) {
    checkArgument(asPathExpr != null, "Missing %s", PROP_AS_PATH_EXPR);
    return of(asPathExpr);
  }

  private static final String PROP_AS_PATH_EXPR = "asPathExpr";

  private final @Nonnull AsPathExpr _asPathExpr;

  private DedupedAsPath(AsPathExpr asPathExpr) {
    _asPathExpr = asPathExpr;
  }
}
