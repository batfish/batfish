package org.batfish.question.specifiers;

import java.util.Set;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.SpecifierContext;

public class TestIpSpaceSpecifier implements IpSpaceSpecifier {
  private final Object _input;

  public TestIpSpaceSpecifier(Object input) {
    _input = input;
  }

  Object getInput() {
    return _input;
  }

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    return null;
  }
}
