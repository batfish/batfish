package org.batfish.specifier;

import static org.batfish.specifier.FlexibleLocationSpecifierFactory.parseSpecifier;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import org.batfish.specifier.FlexibleLocationSpecifierFactory.InterfaceClauseParser;
import org.batfish.specifier.FlexibleLocationSpecifierFactory.InterfaceLinkClauseParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlexibleLocationSpecifierFactoryTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private Pattern _foo;

  @Before
  public void setup() {
    _foo = Pattern.compile("foo");
  }

  @Test
  public void testBuilderLocationSpecifierTyped() {
    FlexibleLocationSpecifierFactory factory = new FlexibleLocationSpecifierFactory();
    assertThat(
        factory.buildLocationSpecifierTyped(""),
        equalTo(new NameRegexInterfaceLinkLocationSpecifier(Pattern.compile(""))));

    LocationSpecifier leaf = new NameRegexInterfaceLinkLocationSpecifier(_foo);
    LocationSpecifier union1 = new UnionLocationSpecifier(leaf, leaf);
    LocationSpecifier union2 = new UnionLocationSpecifier(union1, leaf);
    LocationSpecifier union3 = new UnionLocationSpecifier(union2, leaf);
    assertThat(factory.buildLocationSpecifierTyped("foo"), equalTo(leaf));
    assertThat(factory.buildLocationSpecifierTyped("foo;foo"), equalTo(union1));
    assertThat(factory.buildLocationSpecifierTyped("foo;foo;foo"), equalTo(union2));
    assertThat(factory.buildLocationSpecifierTyped("foo;foo;foo;foo"), equalTo(union3));
  }

  @Test
  public void testInterfaceClauseParser() {
    assertThat(
        new InterfaceClauseParser().parse("node=foo"),
        equalTo(new NodeNameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("vrf=foo"),
        equalTo(new VrfNameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("name=foo"),
        equalTo(new NameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("foo"),
        equalTo(new NameRegexInterfaceLocationSpecifier(_foo)));
  }

  @Test
  public void testInterfaceClauseParser_tooManyEquals() {
    exception.expect(IllegalArgumentException.class);
    new InterfaceClauseParser().parse("foo=foo=foo");
  }

  @Test
  public void testInterfaceClauseParser_unknownPropertyType() {
    exception.expect(IllegalArgumentException.class);
    new InterfaceClauseParser().parse("foo=foo");
  }

  @Test
  public void testInterfaceLinkClauseParser() {
    assertThat(
        new InterfaceLinkClauseParser().parse("node=foo"),
        equalTo(new NodeNameRegexInterfaceLinkLocationSpecifier(_foo)));
    assertThat(
        new InterfaceLinkClauseParser().parse("vrf=foo"),
        equalTo(new VrfNameRegexInterfaceLinkLocationSpecifier(_foo)));
    assertThat(
        new InterfaceLinkClauseParser().parse("name=foo"),
        equalTo(new NameRegexInterfaceLinkLocationSpecifier(_foo)));
    assertThat(
        new InterfaceLinkClauseParser().parse("foo"),
        equalTo(new NameRegexInterfaceLinkLocationSpecifier(_foo)));
  }

  @Test
  public void testParseSpecifier_intersection() {
    LocationSpecifier leaf = new NameRegexInterfaceLinkLocationSpecifier(_foo);
    LocationSpecifier intersection1 = new IntersectionLocationSpecifier(leaf, leaf);
    LocationSpecifier intersection2 = new IntersectionLocationSpecifier(intersection1, leaf);
    LocationSpecifier intersection3 = new IntersectionLocationSpecifier(intersection2, leaf);
    assertThat(parseSpecifier("foo,foo"), equalTo(intersection1));
    assertThat(parseSpecifier("foo,foo,foo"), equalTo(intersection2));
    assertThat(parseSpecifier("foo,foo,foo,foo"), equalTo(intersection3));
  }

  @Test
  public void testParseSpecifier_colons() {
    LocationSpecifier iface = new NameRegexInterfaceLinkLocationSpecifier(Pattern.compile("a:b:c"));
    assertThat(parseSpecifier("a:b:c"), equalTo(iface));
  }

  @Test
  public void testParseSpecifier_type() {
    LocationSpecifier iface = new NameRegexInterfaceLocationSpecifier(_foo);
    LocationSpecifier ifaceLink = new NameRegexInterfaceLinkLocationSpecifier(_foo);
    assertThat(parseSpecifier("interfaceLink:foo"), equalTo(ifaceLink));
    assertThat(parseSpecifier("interface:foo"), equalTo(iface));
    assertThat(parseSpecifier("foo"), equalTo(ifaceLink));
  }
}
