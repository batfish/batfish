package org.batfish.specifier;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A {@link LocationSpecifier} that specifies the set union of nodes specified by two other
 * specifiers.
 */
public final class UnionLocationSpecifier implements LocationSpecifier {
  private final @Nonnull LocationSpecifier _locationSpecifier1;

  private final @Nonnull LocationSpecifier _locationSpecifier2;

  public UnionLocationSpecifier(
      @Nonnull LocationSpecifier locationSpecifier1,
      @Nonnull LocationSpecifier locationSpecifier2) {
    _locationSpecifier1 = locationSpecifier1;
    _locationSpecifier2 = locationSpecifier2;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return Sets.union(_locationSpecifier1.resolve(ctxt), _locationSpecifier2.resolve(ctxt));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionLocationSpecifier)) {
      return false;
    }
    UnionLocationSpecifier other = (UnionLocationSpecifier) o;
    return _locationSpecifier1.equals(other._locationSpecifier1)
        && _locationSpecifier2.equals(other._locationSpecifier2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_locationSpecifier1, _locationSpecifier2);
  }
}
