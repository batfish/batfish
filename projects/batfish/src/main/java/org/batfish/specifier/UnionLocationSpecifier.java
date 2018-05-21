package org.batfish.specifier;

import com.google.common.collect.Sets;
import java.util.Set;

/**
 * A {@link LocationSpecifier} that specifies the set union of nodes specified by two other
 * specifiers.
 */
public class UnionLocationSpecifier implements LocationSpecifier {
  private final LocationSpecifier _locationSpecifier1;

  private final LocationSpecifier _locationSpecifier2;

  public UnionLocationSpecifier(
      LocationSpecifier locationSpecifier1, LocationSpecifier locationSpecifier2) {
    _locationSpecifier1 = locationSpecifier1;
    _locationSpecifier2 = locationSpecifier2;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return Sets.union(_locationSpecifier1.resolve(ctxt), _locationSpecifier2.resolve(ctxt));
  }
}
