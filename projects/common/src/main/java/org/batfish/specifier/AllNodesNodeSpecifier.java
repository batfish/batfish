package org.batfish.specifier;

import java.util.Set;
import javax.annotation.Nullable;

/** A {@link NodeSpecifier} specifying all nodes in the network. */
public final class AllNodesNodeSpecifier implements NodeSpecifier {
  public static final AllNodesNodeSpecifier INSTANCE = new AllNodesNodeSpecifier();

  private AllNodesNodeSpecifier() {}

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof AllNodesNodeSpecifier;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Set<String> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().keySet();
  }
}
