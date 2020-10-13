package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.parboiled.MatcherContext;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchervisitors.MatcherVisitor;
import org.parboiled.support.MatcherPath.Element;

public class PathElementTest {

  class MockMatcher extends AbstractMatcher {

    MockMatcher(String label) {
      super(label);
    }

    @Override
    public <V> boolean match(MatcherContext<V> matcherContext) {
      return false;
    }

    @Override
    public <R> R accept(MatcherVisitor<R> matcherVisitor) {
      return null;
    }
  }

  @Test
  public void testCreateCharAnchor() {
    Element element = new Element(new MockMatcher("\'label\'"), 1, 2); // startindex, level
    assertThat(
        PathElement.create(element, ImmutableMap.of()),
        equalTo(new PathElement(CHAR_LITERAL, "\'label\'", 2, 1)));
  }

  /** Anchor is not defined for the label and label is not a string or char literal */
  @Test
  public void testCreateNullAnchor() {
    Element element = new Element(new MockMatcher("label"), 1, 2); // startindex, level
    assertThat(
        PathElement.create(element, ImmutableMap.of()),
        equalTo(new PathElement(null, "label", 2, 1)));
  }

  @Test
  public void testCreateStringAnchor() {
    Element element = new Element(new MockMatcher("\"label\""), 1, 2); // startindex, level
    assertThat(
        PathElement.create(element, ImmutableMap.of()),
        equalTo(new PathElement(STRING_LITERAL, "\"label\"", 2, 1)));
  }

  /** Label is a known anchor */
  @Test
  public void testCreateKnownAnchor() {
    Element element = new Element(new MockMatcher("label"), 1, 2); // startindex, level
    assertThat(
        PathElement.create(element, ImmutableMap.of("label", ADDRESS_GROUP_NAME)),
        equalTo(new PathElement(ADDRESS_GROUP_NAME, "label", 2, 1)));
  }
}
