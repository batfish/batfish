package org.batfish.specifier;

import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link NodeSpecifier} that specifies the set difference of nodes specified by two other
 * specifiers.
 */
public final class DifferenceNodeSpecifier implements NodeSpecifier {
  private final @Nonnull NodeSpecifier _nodeSpecifier1;

  private final @Nonnull NodeSpecifier _nodeSpecifier2;

  public DifferenceNodeSpecifier(
      @Nonnull NodeSpecifier nodeSpecifier1, @Nonnull NodeSpecifier nodeSpecifier2) {
    _nodeSpecifier1 = nodeSpecifier1;
    _nodeSpecifier2 = nodeSpecifier2;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DifferenceNodeSpecifier)) {
      return false;
    }
    DifferenceNodeSpecifier rhs = (DifferenceNodeSpecifier) obj;
    return _nodeSpecifier1.equals(rhs._nodeSpecifier1)
        && _nodeSpecifier2.equals(rhs._nodeSpecifier2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nodeSpecifier1, _nodeSpecifier2);
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return Sets.difference(_nodeSpecifier1.resolve(ctxt), _nodeSpecifier2.resolve(ctxt));
  }
}
