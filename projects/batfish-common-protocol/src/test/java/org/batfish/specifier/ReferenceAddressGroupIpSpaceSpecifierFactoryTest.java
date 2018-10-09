package org.batfish.specifier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReferenceAddressGroupIpSpaceSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void buildIllegalString() {
    exception.expect(IllegalArgumentException.class);
    // is not of the form book1:group1
    new ReferenceAddressGroupIpSpaceSpecifierFactory().buildIpSpaceSpecifier("klkl");
  }

  @Test
  public void buildNonString() {
    exception.expect(IllegalArgumentException.class);
    new ReferenceAddressGroupIpSpaceSpecifierFactory().buildIpSpaceSpecifier(1);
  }

  @Test
  public void buildValidString() {
    IpSpaceSpecifier specifier =
        new ReferenceAddressGroupIpSpaceSpecifierFactory().buildIpSpaceSpecifier("group1, book1");
    assert specifier instanceof ReferenceAddressGroupIpSpaceSpecifier;
  }
}
