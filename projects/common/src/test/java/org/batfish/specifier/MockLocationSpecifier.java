package org.batfish.specifier;

import java.util.Set;

public class MockLocationSpecifier implements LocationSpecifier {

  private Set<Location> _locations;

  public MockLocationSpecifier(Set<Location> locations) {
    _locations = locations;
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return _locations;
  }
}
