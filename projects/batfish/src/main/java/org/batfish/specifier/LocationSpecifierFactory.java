package org.batfish.specifier;

public interface LocationSpecifierFactory {
  String getName();

  LocationSpecifier buildLocationSpecifier(Object input);
}
