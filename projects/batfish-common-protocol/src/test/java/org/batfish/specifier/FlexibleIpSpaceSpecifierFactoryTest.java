package org.batfish.specifier;

import static org.batfish.specifier.FlexibleIpSpaceSpecifierFactory.parse;
import static org.batfish.specifier.FlexibleIpSpaceSpecifierFactory.parseIpSpace;
import static org.batfish.specifier.FlexibleIpSpaceSpecifierFactory.parseWildcards;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link FlexibleIpSpaceSpecifierFactory}. */
@RunWith(JUnit4.class)
public class FlexibleIpSpaceSpecifierFactoryTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testParse() {
    assertThat(
        parse("1.2.3.4"),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder().including(IpWildcard.parse("1.2.3.4")).build())));
    assertThat(
        parse("ref.addressgroup(foo,bar)"),
        equalTo(new ReferenceAddressGroupIpSpaceSpecifier("foo", "bar")));
    assertThat(
        parse("ofLocation(foo)"),
        equalTo(new LocationIpSpaceSpecifier(FlexibleLocationSpecifierFactory.parse("foo"))));
  }

  @Test
  public void testParseIpSpace_empty() {
    exception.expect(IllegalArgumentException.class);
    parseIpSpace("");
  }

  @Test
  public void testParseIpSpace_whitelist() {
    assertThat(
        parseIpSpace("1.0.0.0,2.0.0.0/8"),
        equalTo(
            IpWildcardSetIpSpace.builder()
                .including(
                    ImmutableList.of(IpWildcard.parse("1.0.0.0"), IpWildcard.parse("2.0.0.0/8")))
                .build()));
  }

  @Test
  public void testParseIpSpace_blacklist() {
    assertThat(
        parseIpSpace("1.0.0.0,2.0.0.0/8 - 3.0.0.0,4.0.0.0/32"),
        equalTo(
            IpWildcardSetIpSpace.builder()
                .including(
                    ImmutableList.of(IpWildcard.parse("1.0.0.0"), IpWildcard.parse("2.0.0.0/8")))
                .excluding(
                    ImmutableList.of(IpWildcard.parse("3.0.0.0"), IpWildcard.parse("4.0.0.0/32")))
                .build()));
  }

  @Test
  public void testParseIpSpace_blacklist_backslash() {
    assertThat(
        parseIpSpace("1.0.0.0,2.0.0.0/8 \\ 3.0.0.0,4.0.0.0/32"),
        equalTo(
            IpWildcardSetIpSpace.builder()
                .including(
                    ImmutableList.of(IpWildcard.parse("1.0.0.0"), IpWildcard.parse("2.0.0.0/8")))
                .excluding(
                    ImmutableList.of(IpWildcard.parse("3.0.0.0"), IpWildcard.parse("4.0.0.0/32")))
                .build()));
  }

  @Test
  public void testParseIpSpace_tooManySubtractions() {
    exception.expect(IllegalArgumentException.class);
    parseIpSpace("1.0.0.0 - 2.0.0.0/8 \\ 3.0.0.0 \\ 4.0.0.0/32");
  }

  @Test
  public void testParseWildcards_zero() {
    exception.expect(IllegalArgumentException.class);
    parseWildcards("");
  }

  @Test
  public void testParseWildcards_one() {
    assertThat(
        parseWildcards("1.0.0.0/8"), equalTo(ImmutableList.of(IpWildcard.parse("1.0.0.0/8"))));
  }

  @Test
  public void testParseWildcards_two() {
    assertThat(
        parseWildcards("1.0.0.0/8, 2.0.0.0/8"),
        equalTo(ImmutableList.of(IpWildcard.parse("1.0.0.0/8"), IpWildcard.parse("2.0.0.0/8"))));
  }
}
