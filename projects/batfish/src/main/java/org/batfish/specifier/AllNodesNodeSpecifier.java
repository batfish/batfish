package org.batfish.specifier;

import java.util.Set;

public class AllNodesNodeSpecifier implements NodeSpecifier {
  public static final AllNodesNodeSpecifier INSTANCE = new AllNodesNodeSpecifier();

  private AllNodesNodeSpecifier() {}

  @Override
  public Set<String> resolve(SpecifierContext specifierContext) {
    return specifierContext.getConfigs().keySet();
  }
}
