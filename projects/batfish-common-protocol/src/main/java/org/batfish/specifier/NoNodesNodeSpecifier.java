package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** A {@link org.batfish.specifier.NodeSpecifier} the specifies the empty set of nodes */
public final class NoNodesNodeSpecifier implements NodeSpecifier {
  public static final NoNodesNodeSpecifier INSTANCE = new NoNodesNodeSpecifier();

  private NoNodesNodeSpecifier() {}

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ImmutableSet.of();
  }
}
