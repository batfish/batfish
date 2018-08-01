package org.batfish.specifier;

import static org.batfish.specifier.FilterSpecifierFactory.load;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class FilterSpecifierFactoryTest {
  @Test
  public void testLoad() {
    FilterSpecifierFactory loaded = load(new ShorthandFilterSpecifierFactory().getName());
    assertThat(loaded, instanceOf(ShorthandFilterSpecifierFactory.class));
  }
}
