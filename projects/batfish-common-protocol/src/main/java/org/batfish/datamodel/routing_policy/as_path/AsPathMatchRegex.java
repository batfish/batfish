package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An {@link AsPathMatchExpr} that matches an AS-path via a Java regex. */
public final class AsPathMatchRegex extends AsPathMatchExpr {

  public static @Nonnull AsPathMatchRegex of(String regex) {
    return new AsPathMatchRegex(regex);
  }

  @Override
  public <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsPathMatchRegex(this, arg);
  }

  @JsonProperty(PROP_REGEX)
  public @Nonnull String getRegex() {
    return _regex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AsPathMatchRegex)) {
      return false;
    }
    AsPathMatchRegex that = (AsPathMatchRegex) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).omitNullValues().add("regex", _regex).toString();
  }

  private static final String PROP_REGEX = "regex";

  @JsonCreator
  private static @Nonnull AsPathMatchRegex create(
      @JsonProperty(PROP_REGEX) @Nullable String regex) {
    checkArgument(regex != null, "Missing %s", PROP_REGEX);
    return of(regex);
  }

  private final @Nonnull String _regex;

  private AsPathMatchRegex(String regex) {
    _regex = regex;
  }
}
