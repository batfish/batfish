package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodeSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME),
        instanceOf(FlexibleNodeSpecifierFactory.class));
  }

  @Test
  public void testUnknownNodeSpecifierFactory() {
    exception.expect(BatfishException.class);
    NodeSpecifierFactory.load("");
  }
}
