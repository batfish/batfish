package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nullable;

/** A {@link org.batfish.specifier.NodeSpecifier} the specifies the empty set of nodes */
public final class NoNodesNodeSpecifier implements NodeSpecifier {
  public static final NoNodesNodeSpecifier INSTANCE = new NoNodesNodeSpecifier();

  private NoNodesNodeSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof NoNodesNodeSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ImmutableSet.of();
  }
}
