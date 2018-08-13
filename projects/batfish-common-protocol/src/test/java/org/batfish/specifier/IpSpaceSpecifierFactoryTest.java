package org.batfish.specifier;

import static org.batfish.specifier.IpSpaceSpecifierFactory.load;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.regex.Pattern;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.hamcrest.Matchers;
import org.junit.Test;

public class IpSpaceSpecifierFactoryTest {
  @Test
  public void testLoad() {
    IpSpaceSpecifierFactory loaded = load(new InferFromLocationIpSpaceSpecifierFactory().getName());
    assertThat(loaded, instanceOf(InferFromLocationIpSpaceSpecifierFactory.class));
  }

  @Test
  public void testConstantUniverseIpSpaceSpecifier() {
    assertThat(
        new ConstantUniverseIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        equalTo(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE)));
  }

  @Test
  public void testFlexibleInferFromLocationIpSpaceSpecifierFactory() {
    assertThat(
        load(FlexibleInferFromLocationIpSpaceSpecifierFactory.NAME),
        Matchers.instanceOf(FlexibleInferFromLocationIpSpaceSpecifierFactory.class));
    assertThat(
        new FlexibleInferFromLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        equalTo(InferFromLocationIpSpaceSpecifier.INSTANCE));
  }

  @Test
  public void testFlexibleLocationIpSpaceSpecifierFactory() {
    assertThat(
        load(FlexibleLocationIpSpaceSpecifierFactory.NAME),
        instanceOf(FlexibleLocationIpSpaceSpecifierFactory.class));
    IpSpaceSpecifier actual =
        new FlexibleLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier("[vrf(foo)]");
    assertThat(
        actual,
        equalTo(
            new LocationIpSpaceSpecifier(
                new InterfaceSpecifierInterfaceLocationSpecifier(
                    new VrfNameRegexInterfaceSpecifier(Pattern.compile("foo"))))));
  }

  @Test
  public void testFlexibleUniverseIpSpaceSpecifierFactory() {
    assertThat(
        load(FlexibleUniverseIpSpaceSpecifierFactory.NAME),
        instanceOf(FlexibleUniverseIpSpaceSpecifierFactory.class));
    assertThat(
        new FlexibleUniverseIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        equalTo(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE)));
  }

  @Test
  public void testInferFromLocationIpSpaceSpecifierFactory() {
    assertThat(
        new InferFromLocationIpSpaceSpecifierFactory().buildIpSpaceSpecifier(null),
        is(InferFromLocationIpSpaceSpecifier.INSTANCE));
  }

  @Test
  public void testNodeNameRegexConnecgtedHostsIpSpaceSpecifierFactory() {
    assertThat(
        load(NodeNameRegexConnectedHostsIpSpaceSpecifierFactory.NAME),
        Matchers.instanceOf(NodeNameRegexConnectedHostsIpSpaceSpecifierFactory.class));
    assertThat(
        new NodeNameRegexConnectedHostsIpSpaceSpecifierFactory().buildIpSpaceSpecifier("foo"),
        equalTo(new NodeNameRegexConnectedHostsIpSpaceSpecifier(Pattern.compile("foo"))));
  }

  @Test
  public void testConstantWildcardSetIpSpaceSpecifierFactory() {
    assertThat(
        load(ConstantWildcardSetIpSpaceSpecifierFactory.NAME),
        Matchers.instanceOf(ConstantWildcardSetIpSpaceSpecifierFactory.class));
    assertThat(
        new ConstantWildcardSetIpSpaceSpecifierFactory()
            .buildIpSpaceSpecifier("1.2.3.0/24 - 1.2.3.4"),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder()
                    .including(new IpWildcard("1.2.3.0/24"))
                    .excluding(new IpWildcard("1.2.3.4"))
                    .build())));
  }
}
