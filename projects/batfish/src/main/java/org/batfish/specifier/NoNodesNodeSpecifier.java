package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class NoNodesNodeSpecifier implements NodeSpecifier {
  public static final NoNodesNodeSpecifier INSTANCE = new NoNodesNodeSpecifier();

  private NoNodesNodeSpecifier() {}

  @Override
  public Set<String> resolve(SpecifierContext specifierContext) {
    return ImmutableSet.of();
  }
}
