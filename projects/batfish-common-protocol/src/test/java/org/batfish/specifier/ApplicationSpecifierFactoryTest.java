package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.batfish.specifier.parboiled.ParboiledApplicationSpecifierFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ApplicationSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        ApplicationSpecifierFactory.load(ParboiledApplicationSpecifierFactory.NAME),
        instanceOf(ParboiledApplicationSpecifierFactory.class));
  }

  @Test
  public void testLoadUnknown() {
    exception.expect(BatfishException.class);
    ApplicationSpecifierFactory.load("");
  }
}
