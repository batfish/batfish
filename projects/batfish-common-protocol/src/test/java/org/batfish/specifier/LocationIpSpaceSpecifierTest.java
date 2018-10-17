package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LocationIpSpaceSpecifierTest {
  @Rule public ExpectedException _expectedException = ExpectedException.none();

  @Test
  public void testNoLocations() {
    MockSpecifierContext ctxt = MockSpecifierContext.builder().build();
    IpSpaceSpecifier specifier =
        new LocationIpSpaceSpecifier(new MockLocationSpecifier(ImmutableSet.of()));
    _expectedException.expect(IllegalArgumentException.class);
    _expectedException.expectMessage("No such locations");
    specifier.resolve(ImmutableSet.of(), ctxt);
  }
}
