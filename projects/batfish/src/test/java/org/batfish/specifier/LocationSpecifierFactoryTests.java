package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Pattern;
import org.hamcrest.Matchers;
import org.junit.Test;

public class LocationSpecifierFactoryTests {
  /** Test that each Factory is discoverable by the registry. */
  @Test
  public void testLocationSpecifierFactoryRegistry() {
    LocationSpecifierFactoryRegistry registry = new LocationSpecifierFactoryRegistry();

    assertThat(
        registry.getLocationSpecifierFactory(
            new DescriptionRegexInterfaceLocationSpecifierFactory().getName()),
        Matchers.instanceOf(DescriptionRegexInterfaceLocationSpecifierFactory.class));

    assertThat(
        registry.getLocationSpecifierFactory(
            new NameRegexInterfaceLocationSpecifierFactory().getName()),
        Matchers.instanceOf(NameRegexInterfaceLocationSpecifierFactory.class));

    assertThat(
        registry.getLocationSpecifierFactory(
            new NodeNameRegexInterfaceLinkLocationSpecifierFactory().getName()),
        Matchers.instanceOf(NodeNameRegexInterfaceLinkLocationSpecifierFactory.class));

    assertThat(
        registry.getLocationSpecifierFactory(
            new NodeNameRegexInterfaceLocationSpecifierFactory().getName()),
        Matchers.instanceOf(NodeNameRegexInterfaceLocationSpecifierFactory.class));

    assertThat(
        registry.getLocationSpecifierFactory(
            new VrfNameRegexInterfaceLinkLocationSpecifierFactory().getName()),
        Matchers.instanceOf(VrfNameRegexInterfaceLinkLocationSpecifierFactory.class));

    assertThat(
        registry.getLocationSpecifierFactory(
            new VrfNameRegexInterfaceLocationSpecifierFactory().getName()),
        Matchers.instanceOf(VrfNameRegexInterfaceLocationSpecifierFactory.class));
  }

  @Test
  public void testDescriptionRegexInterfaceLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new DescriptionRegexInterfaceLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new DescriptionRegexInterfaceLocationSpecifier(pat)));
  }

  @Test
  public void testNameRegexInterfaceLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new NameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new NameRegexInterfaceLocationSpecifier(pat)));
  }

  @Test
  public void testNodeNameRegexInterfaceLinkLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new NodeNameRegexInterfaceLinkLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new NodeNameRegexInterfaceLinkLocationSpecifier(pat)));
  }

  @Test
  public void testNodeNameRegexInterfaceLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new NodeNameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new NodeNameRegexInterfaceLocationSpecifier(pat)));
  }

  @Test
  public void testVrfNameRegexInterfaceLinkLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new VrfNameRegexInterfaceLinkLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new VrfNameRegexInterfaceLinkLocationSpecifier(pat)));
  }

  @Test
  public void testVrfNameRegexInterfaceLocationSpecifierFactory() {
    Pattern pat = Pattern.compile("foo");
    assertThat(
        new VrfNameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifierTyped(pat),
        equalTo(new VrfNameRegexInterfaceLocationSpecifier(pat)));
  }
}
