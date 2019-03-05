package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.batfish.specifier.InterfaceSpecifierFilterSpecifier.Type;
import org.junit.Test;

/** Tests for {@link FlexibleFilterSpecifierFactory}. */
public class FlexibleFilterSpecifierFactoryTest {
  @Test
  public void testShorthandFilterSpecifier() {
    assertThat(
        new FlexibleFilterSpecifierFactory().buildFilterSpecifier(".*"),
        equalTo(new NameRegexFilterSpecifier(Pattern.compile(".*", Pattern.CASE_INSENSITIVE))));
  }

  @Test
  public void testInFilterOf() {
    assertThat(
        new FlexibleFilterSpecifierFactory().buildFilterSpecifier("inFilterOf(a)"),
        equalTo(
            new InterfaceSpecifierFilterSpecifier(
                Type.IN_FILTER,
                new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("a"))));
  }

  @Test
  public void testLoad() {
    assertTrue(
        FilterSpecifierFactory.load(FlexibleFilterSpecifierFactory.NAME)
            instanceof FlexibleFilterSpecifierFactory);
  }

  @Test
  public void testOutFilterOf() {
    assertThat(
        new FlexibleFilterSpecifierFactory().buildFilterSpecifier("outFilterOf(a)"),
        equalTo(
            new InterfaceSpecifierFilterSpecifier(
                Type.OUT_FILTER,
                new FlexibleInterfaceSpecifierFactory().buildInterfaceSpecifier("a"))));
  }

  @Test
  public void testRefFilterGroup() {
    assertThat(
        new FlexibleFilterSpecifierFactory().buildFilterSpecifier("rEf.FilterGroup(a, b)"),
        equalTo(new ReferenceFilterGroupFilterSpecifier("a", "b")));
  }
}
