package org.batfish.specifier;

import java.util.Set;

/** A {@link NodeSpecifier} specifying all nodes in the network. */
public final class AllNodesNodeSpecifier implements NodeSpecifier {
  public static final AllNodesNodeSpecifier INSTANCE = new AllNodesNodeSpecifier();

  private AllNodesNodeSpecifier() {}

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().keySet();
  }
}
