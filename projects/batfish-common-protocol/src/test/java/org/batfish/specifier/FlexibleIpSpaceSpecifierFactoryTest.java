package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.junit.Test;

public class FlexibleIpSpaceSpecifierFactoryTest {
  @Test
  public void testLoad() {
    assertThat(
        IpSpaceSpecifierFactory.load(FlexibleIpSpaceSpecifierFactory.NAME)
            instanceof FlexibleIpSpaceSpecifierFactory,
        is(true));
  }

  @Test
  public void testNull() {
    assertThat(
        new FlexibleIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        is(InferFromLocationIpSpaceSpecifier.INSTANCE));
  }

  @Test
  public void testNonNull() {
    assertThat(
        new FlexibleIpSpaceSpecifierFactory().buildIpSpaceSpecifier("1.1.1.1"),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.1")).build())));
  }
}
