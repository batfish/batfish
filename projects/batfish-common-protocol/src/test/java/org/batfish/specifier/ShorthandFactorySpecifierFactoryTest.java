package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.questions.FiltersSpecifier;
import org.junit.Test;

public class ShorthandFactorySpecifierFactoryTest {

  @Test
  public void testNonNullInput() {
    assertThat(
        new ShorthandFilterSpecifierFactory().buildFilterSpecifier("filter1"),
        equalTo(new ShorthandFilterSpecifier(new FiltersSpecifier("filter1"))));
  }

  @Test
  public void testNullInput() {
    assertThat(
        new ShorthandFilterSpecifierFactory().buildFilterSpecifier(null),
        equalTo(new ShorthandFilterSpecifier(FiltersSpecifier.ALL)));
  }
}
