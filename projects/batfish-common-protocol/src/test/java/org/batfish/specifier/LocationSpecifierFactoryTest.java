package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.regex.Pattern;
import org.junit.Test;

public class LocationSpecifierFactoryTest {
  private static final String PATTERN_SRC = "foo";
  private static final Pattern PATTERN = Pattern.compile(PATTERN_SRC, Pattern.CASE_INSENSITIVE);

  @Test
  public void testAllInterfaceLinksLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(new AllInterfaceLinksLocationSpecifierFactory().getName()),
        instanceOf(AllInterfaceLinksLocationSpecifierFactory.class));
    assertThat(
        new AllInterfaceLinksLocationSpecifierFactory().getName(),
        equalTo(AllInterfaceLinksLocationSpecifierFactory.NAME));
    assertThat(
        new AllInterfaceLinksLocationSpecifierFactory().buildLocationSpecifier(null),
        is(AllInterfaceLinksLocationSpecifier.INSTANCE));
  }

  @Test
  public void testAllInterfacesLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(new AllInterfacesLocationSpecifierFactory().getName()),
        instanceOf(AllInterfacesLocationSpecifierFactory.class));
    assertThat(
        new AllInterfacesLocationSpecifierFactory().getName(),
        equalTo(AllInterfacesLocationSpecifierFactory.NAME));
    assertThat(
        new AllInterfacesLocationSpecifierFactory().buildLocationSpecifier(null),
        is(AllInterfacesLocationSpecifier.INSTANCE));
  }

  @Test
  public void testDescriptionRegexInterfaceLinkLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new DescriptionRegexInterfaceLinkLocationSpecifierFactory().getName()),
        instanceOf(DescriptionRegexInterfaceLinkLocationSpecifierFactory.class));
    assertThat(
        new DescriptionRegexInterfaceLinkLocationSpecifierFactory().getName(),
        equalTo(DescriptionRegexInterfaceLinkLocationSpecifierFactory.NAME));
    assertThat(
        new DescriptionRegexInterfaceLinkLocationSpecifierFactory()
            .buildLocationSpecifier(PATTERN_SRC),
        equalTo(new DescriptionRegexInterfaceLinkLocationSpecifier(PATTERN)));
  }

  @Test
  public void testDescriptionRegexInterfaceLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new DescriptionRegexInterfaceLocationSpecifierFactory().getName()),
        instanceOf(DescriptionRegexInterfaceLocationSpecifierFactory.class));
    assertThat(
        new DescriptionRegexInterfaceLocationSpecifierFactory().getName(),
        equalTo(DescriptionRegexInterfaceLocationSpecifierFactory.NAME));
    assertThat(
        new DescriptionRegexInterfaceLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new DescriptionRegexInterfaceLocationSpecifier(PATTERN)));
  }

  @Test
  public void testNameRegexInterfaceLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(new NameRegexInterfaceLocationSpecifierFactory().getName()),
        instanceOf(NameRegexInterfaceLocationSpecifierFactory.class));
    assertThat(
        new NameRegexInterfaceLocationSpecifierFactory().getName(),
        equalTo(NameRegexInterfaceLocationSpecifierFactory.NAME));
    assertThat(
        new NameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new NameRegexInterfaceLocationSpecifier(PATTERN)));
  }

  @Test
  public void testNameRegexInterfaceLinkLocationSpecifierFactory() {
    assertThat(
        new NameRegexInterfaceLinkLocationSpecifierFactory().getName(),
        equalTo(NameRegexInterfaceLinkLocationSpecifierFactory.NAME));
    assertThat(
        new NameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new NameRegexInterfaceLocationSpecifier(PATTERN)));
  }

  @Test
  public void testNodeNameRegexInterfaceLinkLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new NodeNameRegexInterfaceLinkLocationSpecifierFactory().getName()),
        instanceOf(NodeNameRegexInterfaceLinkLocationSpecifierFactory.class));
    assertThat(
        new NodeNameRegexInterfaceLinkLocationSpecifierFactory().getName(),
        equalTo(NodeNameRegexInterfaceLinkLocationSpecifierFactory.NAME));
    assertThat(
        new NodeNameRegexInterfaceLinkLocationSpecifierFactory()
            .buildLocationSpecifier(PATTERN_SRC),
        equalTo(new NodeNameRegexInterfaceLinkLocationSpecifier(PATTERN)));
  }

  @Test
  public void testNodeNameRegexInterfaceLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new NodeNameRegexInterfaceLocationSpecifierFactory().getName()),
        instanceOf(NodeNameRegexInterfaceLocationSpecifierFactory.class));
    assertThat(
        new NodeNameRegexInterfaceLocationSpecifierFactory().getName(),
        equalTo(NodeNameRegexInterfaceLocationSpecifierFactory.NAME));
    assertThat(
        new NodeNameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new NodeNameRegexInterfaceLocationSpecifier(PATTERN)));
  }

  @Test
  public void testNodeRoleRegexInterfaceLinkLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new NodeRoleRegexInterfaceLinkLocationSpecifierFactory().getName()),
        instanceOf(NodeRoleRegexInterfaceLinkLocationSpecifierFactory.class));
    assertThat(
        new NodeRoleRegexInterfaceLinkLocationSpecifierFactory()
            .buildLocationSpecifierTyped("roleDimension:" + PATTERN_SRC),
        equalTo(new NodeRoleRegexInterfaceLinkLocationSpecifier("roleDimension", PATTERN)));
  }

  @Test
  public void testNodeRoleRegexInterfaceLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new NodeRoleRegexInterfaceLocationSpecifierFactory().getName()),
        instanceOf(NodeRoleRegexInterfaceLocationSpecifierFactory.class));
    assertThat(
        new NodeRoleRegexInterfaceLocationSpecifierFactory()
            .buildLocationSpecifierTyped("roleDimension:" + PATTERN_SRC),
        equalTo(new NodeRoleRegexInterfaceLocationSpecifier("roleDimension", PATTERN)));
  }

  @Test
  public void testVrfNameRegexInterfaceLinkLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new VrfNameRegexInterfaceLinkLocationSpecifierFactory().getName()),
        instanceOf(VrfNameRegexInterfaceLinkLocationSpecifierFactory.class));

    assertThat(
        new VrfNameRegexInterfaceLinkLocationSpecifierFactory().getName(),
        equalTo(VrfNameRegexInterfaceLinkLocationSpecifierFactory.NAME));
    assertThat(
        new VrfNameRegexInterfaceLinkLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new VrfNameRegexInterfaceLinkLocationSpecifier(PATTERN)));
  }

  @Test
  public void testVrfNameRegexInterfaceLocationSpecifierFactory() {
    assertThat(
        LocationSpecifierFactory.load(
            new VrfNameRegexInterfaceLocationSpecifierFactory().getName()),
        instanceOf(VrfNameRegexInterfaceLocationSpecifierFactory.class));
    assertThat(
        new VrfNameRegexInterfaceLocationSpecifierFactory().getName(),
        equalTo(VrfNameRegexInterfaceLocationSpecifierFactory.NAME));
    assertThat(
        new VrfNameRegexInterfaceLocationSpecifierFactory().buildLocationSpecifier(PATTERN_SRC),
        equalTo(new VrfNameRegexInterfaceLocationSpecifier(PATTERN)));
  }
}
