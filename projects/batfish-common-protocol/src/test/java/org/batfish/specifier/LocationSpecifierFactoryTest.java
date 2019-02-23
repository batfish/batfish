package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LocationSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        LocationSpecifierFactory.load(FlexibleLocationSpecifierFactory.NAME),
        instanceOf(FlexibleLocationSpecifierFactory.class));
  }

  @Test
  public void testLoadUnknown() {
    exception.expect(BatfishException.class);
    LocationSpecifierFactory.load("");
  }
}
