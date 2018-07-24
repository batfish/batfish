package org.batfish.specifier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AddressBookIpSpaceSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void buildIllegalString() {
    exception.expect(IllegalArgumentException.class);
    // is not of the form book1:group1
    new AddressBookIpSpaceSpecifierFactory().buildIpSpaceSpecifier("klkl");
  }

  @Test
  public void buildNonString() {
    exception.expect(IllegalArgumentException.class);
    new AddressBookIpSpaceSpecifierFactory().buildIpSpaceSpecifier(new Integer(1));
  }

  @Test
  public void buildValidString() {
    IpSpaceSpecifier specifier =
        new AddressBookIpSpaceSpecifierFactory().buildIpSpaceSpecifier("book1:group1");
    assert specifier instanceof AddressGroupIpSpaceSpecifier;
  }
}
