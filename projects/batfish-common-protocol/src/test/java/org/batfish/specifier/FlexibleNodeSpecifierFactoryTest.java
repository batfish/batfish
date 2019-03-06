package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlexibleNodeSpecifierFactoryTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGarbageIn() {
    exception.expect(IllegalArgumentException.class);
    new FlexibleNodeSpecifierFactory().buildNodeSpecifier("f\\of"); // bad regex
  }

  @Test
  public void testLoad() {
    assertTrue(
        NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME)
            instanceof FlexibleNodeSpecifierFactory);
  }

  @Test
  public void testNull() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier(null),
        equalTo(AllNodesNodeSpecifier.INSTANCE));
  }

  @Test
  public void testRefNodeRoles() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier("rEf.NodeRole(a, b)"),
        equalTo(new RoleRegexNodeSpecifier(Pattern.compile("a", Pattern.CASE_INSENSITIVE), "b")));
  }

  @Test
  public void testNameRegexNodeSpecifier() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier("name.*"),
        equalTo(new NameRegexNodeSpecifier(Pattern.compile("name.*", Pattern.CASE_INSENSITIVE))));
  }

  @Test
  public void testDifference() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier("foo - bar"),
        equalTo(
            new DifferenceNodeSpecifier(
                new NameRegexNodeSpecifier(Pattern.compile("foo", Pattern.CASE_INSENSITIVE)),
                new NameRegexNodeSpecifier(Pattern.compile("bar", Pattern.CASE_INSENSITIVE)))));
  }
}
