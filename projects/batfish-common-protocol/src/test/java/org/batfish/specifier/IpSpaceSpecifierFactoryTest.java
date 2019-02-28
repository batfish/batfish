package org.batfish.specifier;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IpSpaceSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testLoad() {
    assertThat(
        IpSpaceSpecifierFactory.load(FlexibleIpSpaceSpecifierFactory.NAME),
        instanceOf(FlexibleIpSpaceSpecifierFactory.class));
  }

  @Test
  public void testLoadUnknown() {
    exception.expect(BatfishException.class);
    IpSpaceSpecifierFactory.load("");
  }
}
