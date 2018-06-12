package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceSpecifierFactoryTest {
  @Test
  public void testLoad() {
    IpSpaceSpecifierFactory loaded =
        IpSpaceSpecifierFactory.load(new InferFromLocationIpSpaceSpecifierFactory().getName());
    assertThat(loaded, instanceOf(InferFromLocationIpSpaceSpecifierFactory.class));
  }

  @Test
  public void testConstantUniverseIpSpaceSpecifier() {
    assertThat(
        new ConstantUniverseIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        equalTo(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE)));
  }

  @Test
  public void testInferFromLocationIpSpaceSpecifierFactory() {
    assertThat(
        new InferFromLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        is(InferFromLocationIpSpaceSpecifier.INSTANCE));
  }
}
