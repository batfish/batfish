package org.batfish.specifier;

import static org.batfish.specifier.FlexibleLocationSpecifierFactory.parseSpecifier;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
  public void testBuilderLocationSpecifier() {
    FlexibleLocationSpecifierFactory factory = new FlexibleLocationSpecifierFactory();
    assertThat(
        factory.buildLocationSpecifier(""),
        equalTo(new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile(""))));

    LocationSpecifier leaf = new NodeNameRegexInterfaceLinkLocationSpecifier(_foo);
    LocationSpecifier union1 = new UnionLocationSpecifier(leaf, leaf);
    LocationSpecifier union2 = new UnionLocationSpecifier(union1, leaf);
    LocationSpecifier union3 = new UnionLocationSpecifier(union2, leaf);
    assertThat(
        factory.buildLocationSpecifier(null), is(AllInterfaceLinksLocationSpecifier.INSTANCE));
    assertThat(factory.buildLocationSpecifier("foo"), equalTo(leaf));
    assertThat(factory.buildLocationSpecifier("foo;foo"), equalTo(union1));
    assertThat(factory.buildLocationSpecifier("foo;foo;foo"), equalTo(union2));
    assertThat(factory.buildLocationSpecifier("foo;foo;foo;foo"), equalTo(union3));

    // any nonnull input must be a String
    exception.expect(IllegalArgumentException.class);
    factory.buildLocationSpecifier(5);
  }

  @Test
  public void testInterfaceClauseParser() {
    assertThat(
        new InterfaceClauseParser().parse("node=foo"),
        equalTo(new NodeNameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("nodeRole:dim=foo"),
        equalTo(new NodeRoleRegexInterfaceLocationSpecifier("dim", _foo)));
    assertThat(
        new InterfaceClauseParser().parse("vrf=foo"),
        equalTo(new VrfNameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("interface=foo"),
        equalTo(new NameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        new InterfaceClauseParser().parse("foo"),
        equalTo(new NodeNameRegexInterfaceLocationSpecifier(_foo)));
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
        new InterfaceLinkClauseParser().parse("interface=foo"),
        equalTo(new NameRegexInterfaceLinkLocationSpecifier(_foo)));
    assertThat(
        new InterfaceLinkClauseParser().parse("foo"),
        equalTo(new NodeNameRegexInterfaceLinkLocationSpecifier(_foo)));
  }

  @Test
  public void testParseSpecifier_intersection() {
    LocationSpecifier leaf = new NodeNameRegexInterfaceLinkLocationSpecifier(_foo);
    LocationSpecifier intersection1 = new IntersectionLocationSpecifier(leaf, leaf);
    LocationSpecifier intersection2 = new IntersectionLocationSpecifier(intersection1, leaf);
    LocationSpecifier intersection3 = new IntersectionLocationSpecifier(intersection2, leaf);
    assertThat(parseSpecifier("foo,foo"), equalTo(intersection1));
    assertThat(parseSpecifier("foo,foo,foo"), equalTo(intersection2));
    assertThat(parseSpecifier("foo,foo,foo,foo"), equalTo(intersection3));
  }

  @Test
  public void testParseSpecifier_colons() {
    LocationSpecifier iface =
        new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile("a:b:c"));
    assertThat(parseSpecifier("a:b:c"), equalTo(iface));
  }

  @Test
  public void testParseSpecifier_type() {
    LocationSpecifier iface = new NodeNameRegexInterfaceLocationSpecifier(_foo);
    LocationSpecifier ifaceLink = new NodeNameRegexInterfaceLinkLocationSpecifier(_foo);
    assertThat(parseSpecifier("interfaceLink:foo"), equalTo(ifaceLink));
    assertThat(parseSpecifier("interface:foo"), equalTo(iface));
    assertThat(parseSpecifier("foo"), equalTo(ifaceLink));
  }
}
