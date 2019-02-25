package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        FilterSpecifierFactory.load(FlexibleFilterSpecifierFactory.NAME),
        instanceOf(FlexibleFilterSpecifierFactory.class));
  }

  @Test
  public void testLoadUnknown() {
    exception.expect(BatfishException.class);
    FilterSpecifierFactory.load("");
  }
}
