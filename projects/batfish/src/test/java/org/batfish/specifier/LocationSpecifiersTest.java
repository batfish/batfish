package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Pattern;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.junit.Test;

public class LocationSpecifiersTest {
  @Test
  public void testFromInterfacesSpecifier() {
    assertThat(
        LocationSpecifiers.from(new InterfacesSpecifier("desc:foo")),
        equalTo(new DescriptionRegexInterfaceLocationSpecifier(Pattern.compile("foo"))));

    assertThat(
        LocationSpecifiers.from(new InterfacesSpecifier("name:foo")),
        equalTo(new NameRegexInterfaceLocationSpecifier(Pattern.compile("foo"))));

    assertThat(
        LocationSpecifiers.from(new InterfacesSpecifier("vrf:foo")),
        equalTo(new VrfNameRegexInterfaceLocationSpecifier(Pattern.compile("foo"))));
  }
}
