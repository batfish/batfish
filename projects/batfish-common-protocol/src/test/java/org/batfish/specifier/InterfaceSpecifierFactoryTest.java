package org.batfish.specifier;

import static org.batfish.specifier.InterfaceSpecifierFactory.load;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InterfaceSpecifierFactoryTest {
  @Test
  public void testLoad() {
    InterfaceSpecifierFactory loaded = load(new FlexibleInterfaceSpecifierFactory().getName());
    assertThat(loaded, instanceOf(FlexibleInterfaceSpecifierFactory.class));
  }
}
