package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.batfish.specifier.parboiled.ParboiledRoutingPolicySpecifierFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RoutingPolicySpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        RoutingPolicySpecifierFactory.load(ParboiledRoutingPolicySpecifierFactory.NAME),
        instanceOf(ParboiledRoutingPolicySpecifierFactory.class));
  }

  @Test
  public void testLoadUnknown() {
    exception.expect(BatfishException.class);
    RoutingPolicySpecifierFactory.load("");
  }
}
