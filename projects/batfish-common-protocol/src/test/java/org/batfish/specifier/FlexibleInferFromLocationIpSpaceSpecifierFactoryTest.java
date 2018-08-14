package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.junit.Test;

public class FlexibleInferFromLocationIpSpaceSpecifierFactoryTest {
  @Test
  public void testConstantIpSpace() {
    assertThat(
        new FlexibleInferFromLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier("1.1.1.1"),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder().including(new IpWildcard("1.1.1.1")).build())));
  }

  @Test
  public void testLoad() {
    assertThat(
        IpSpaceSpecifierFactory.load(FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME)
            instanceof FlexibleInferFromLocationIpSpaceSpecifierFactory,
        is(true));
  }

  @Test
  public void testNull() {
    assertThat(
        new FlexibleInferFromLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        is(InferFromLocationIpSpaceSpecifier.INSTANCE));
  }

  @Test
  public void testRefAddressGroup() {
    assertThat(
        new FlexibleInferFromLocationIpSpaceSpecifierFactory()
            .buildIpSpaceSpecifier("rEf.AddressGroup(a, b)"),
        equalTo(new ReferenceAddressGroupIpSpaceSpecifier("a", "b")));
  }
}
