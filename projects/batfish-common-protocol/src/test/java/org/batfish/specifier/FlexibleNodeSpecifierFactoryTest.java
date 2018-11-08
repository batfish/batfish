package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlexibleNodeSpecifierFactoryTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testGarbageIn() {
    exception.expect(IllegalArgumentException.class);
    new FlexibleNodeSpecifierFactory().buildNodeSpecifier("fofoao:klklk:opopo:oo");
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
  public void testShorthandNodeSpecifier() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier("name:.*"),
        equalTo(new ShorthandNodeSpecifier(new NodesSpecifier("name:.*"))));
  }

  @Test
  public void testDifference() {
    assertThat(
        new FlexibleNodeSpecifierFactory().buildNodeSpecifier("foo - bar"),
        equalTo(
            new DifferenceNodeSpecifier(
                new ShorthandNodeSpecifier(new NodesSpecifier("foo")),
                new ShorthandNodeSpecifier(new NodesSpecifier("bar")))));
  }
}
