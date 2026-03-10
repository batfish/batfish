package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** Specifies the null set of {@link Location}s. */
public final class NullLocationSpecifier implements LocationSpecifier {
  public static final NullLocationSpecifier INSTANCE = new NullLocationSpecifier();

  private NullLocationSpecifier() {}

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ImmutableSet.of();
  }
}
