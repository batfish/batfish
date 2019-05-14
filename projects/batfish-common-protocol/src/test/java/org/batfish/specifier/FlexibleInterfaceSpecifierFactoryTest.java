package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.batfish.datamodel.IpWildcard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlexibleInterfaceSpecifierFactoryTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testConnectedTo() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("connectedTo(1.2.3.4)"),
        equalTo(new InterfaceWithConnectedIpsSpecifier(IpWildcard.parse("1.2.3.4").toIpSpace())));
  }

  @Test
  public void testGarbageIn() {
    exception.expect(IllegalArgumentException.class);
    // the input string won't even compile to a pattern, our last resort
    new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("f\\o");
  }

  @Test
  public void testLoad() {
    assertTrue(
        InterfaceSpecifierFactory.load(FlexibleInterfaceSpecifierFactory.NAME)
            instanceof FlexibleInterfaceSpecifierFactory);
  }

  @Test
  public void testNull() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier(null),
        equalTo(AllInterfacesInterfaceSpecifier.INSTANCE));
  }

  @Test
  public void testReferenceInterfaceGroup() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("ref.interfaceGroup(a, b)"),
        equalTo(new ReferenceInterfaceGroupInterfaceSpecifier("a", "b")));
  }

  @Test
  public void testShorthand() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("name.*"),
        equalTo(
            new NameRegexInterfaceSpecifier(Pattern.compile("name.*", Pattern.CASE_INSENSITIVE))));
  }

  @Test
  public void testType() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("type(.*)"),
        equalTo(new TypesInterfaceSpecifier(Pattern.compile(".*", Pattern.CASE_INSENSITIVE))));
  }

  @Test
  public void testVrf() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("vrf(.*)"),
        equalTo(new VrfNameRegexInterfaceSpecifier(Pattern.compile(".*"))));
  }
}
