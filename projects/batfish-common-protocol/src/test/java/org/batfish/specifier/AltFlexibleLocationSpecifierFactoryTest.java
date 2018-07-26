package org.batfish.specifier;

import static org.batfish.specifier.AltFlexibleLocationSpecifierFactory.parse;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AltFlexibleLocationSpecifierFactoryTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private Pattern _bar;
  private Pattern _foo;

  @Before
  public void setup() {
    _bar = Pattern.compile("bar1/1.0");
    _foo = Pattern.compile("foo");
  }

  @Test
  public void parseCombinationTerms() {
    LocationSpecifier leaf = new NodeNameRegexInterfaceLocationSpecifier(_foo);
    LocationSpecifier difference = new DifferenceLocationSpecifier(leaf, leaf);
    LocationSpecifier union = new UnionLocationSpecifier(leaf, leaf);
    LocationSpecifier unionDiff = new DifferenceLocationSpecifier(union, leaf);
    assertThat(parse("foo - foo"), equalTo(difference));
    assertThat(parse("foo + foo"), equalTo(union));
    assertThat(parse("foo + foo - foo"), equalTo(unionDiff));
  }

  @Test
  public void parseSingleTerm() {
    assertThat(parse("foo"), equalTo(new NodeNameRegexInterfaceLocationSpecifier(_foo)));
    assertThat(
        parse("foo:bar1/1.0"),
        equalTo(
            new IntersectionLocationSpecifier(
                new NodeNameRegexInterfaceLocationSpecifier(_foo),
                new NameRegexInterfaceLocationSpecifier(_bar))));
    assertThat(
        parse("interface(bar1/1.0)"), equalTo(new NameRegexInterfaceLocationSpecifier(_bar)));
    assertThat(
        new AltFlexibleLocationSpecifierFactory()
            .buildLocationSpecifier("enter(interface(bar1/1.0))"),
        equalTo(
            new ToInterfaceLinkLocationSpecifier(new NameRegexInterfaceLocationSpecifier(_bar))));
    assertThat(
        parse("ref.noderole(foo, dim)"),
        equalTo(new NodeRoleRegexInterfaceLocationSpecifier("dim", _foo)));
    assertThat(parse("vrf(foo)"), equalTo(new VrfNameRegexInterfaceLocationSpecifier(_foo)));
  }

  @Test
  public void parseUnbalancedParenthesis() {
    exception.expect(IllegalArgumentException.class);
    parse("vrf(jjj");
  }

  @Test
  public void parseUnknownSpecifier() {
    exception.expect(IllegalArgumentException.class);
    parse("foo(foo)");
  }
}
