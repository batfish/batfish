package org.batfish.specifier;

import java.util.Set;

public interface LocationSpecifier {
  Set<Location> resolve(SpecifierContext ctxt);
}
