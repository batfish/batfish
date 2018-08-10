package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlexibleInterfaceSpecifierFactoryTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGarbageIn() {
    exception.expect(IllegalArgumentException.class);
    new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("fofoao:klklk:opopo:oo");
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
        equalTo(new ShorthandInterfaceSpecifier(InterfacesSpecifier.ALL)));
  }

  @Test
  public void testShorthand() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("name:.*"),
        equalTo(new ShorthandInterfaceSpecifier(new InterfacesSpecifier("name:.*"))));
  }

  @Test
  public void testVrf() {
    assertThat(
        new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("vrf(.*)"),
        equalTo(new VrfNameRegexInterfaceSpecifier(Pattern.compile(".*"))));
  }
}
