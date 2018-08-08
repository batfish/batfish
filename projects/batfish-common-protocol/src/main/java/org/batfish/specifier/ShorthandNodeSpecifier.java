package org.batfish.specifier;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NodesSpecifier;

/**
 * A {@link NodeSpecifier} based on the original {@link NodesSpecifier} which used a shorthand
 * notation.
 */
@ParametersAreNonnullByDefault
public final class ShorthandNodeSpecifier implements NodeSpecifier {
  @Nonnull private final NodesSpecifier _shorthandSpecifier;

  public ShorthandNodeSpecifier(NodesSpecifier shorthandSpecifier) {
    _shorthandSpecifier = shorthandSpecifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ShorthandNodeSpecifier)) {
      return false;
    }
    ShorthandNodeSpecifier that = (ShorthandNodeSpecifier) o;
    return Objects.equals(_shorthandSpecifier, that._shorthandSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_shorthandSpecifier);
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return _shorthandSpecifier.getMatchingNodes(ctxt);
  }
}
