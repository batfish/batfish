package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReferenceFilterGroupFilterSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void buildIllegalString() {
    exception.expect(IllegalArgumentException.class);
    // is not of the form book1:group1
    new ReferenceFilterGroupFilterSpecifierFactory().buildFilterSpecifier("klkl");
  }

  @Test
  public void buildNonString() {
    exception.expect(IllegalArgumentException.class);
    new ReferenceFilterGroupFilterSpecifierFactory().buildFilterSpecifier(1);
  }

  @Test
  public void buildValidString() {
    FilterSpecifier specifier =
        new ReferenceFilterGroupFilterSpecifierFactory().buildFilterSpecifier("group1, book1");
    assertThat(specifier, equalTo(new ReferenceFilterGroupFilterSpecifier("group1", "book1")));
  }
}
