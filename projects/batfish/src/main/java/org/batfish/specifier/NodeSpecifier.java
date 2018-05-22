package org.batfish.specifier;

import java.util.Set;

public interface NodeSpecifier {
  Set<String> resolve(SpecifierContext specifierContext);
}
