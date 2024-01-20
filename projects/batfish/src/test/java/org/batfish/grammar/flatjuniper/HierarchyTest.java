package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.flatjuniper.Hierarchy.isListPath;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.junit.Test;

/** Tests for {@link Hierarchy}. */
public class HierarchyTest {

  @Test
  public void testIsListPath() {
    assertFalse(isListPath(makeLiteralPath("policy-options")));
    assertFalse(isListPath(makeLiteralPath("policy-options", "policy-statement", "foo")));
    assertTrue(isListPath(makeLiteralPath("policy-options", "policy-statement", "foo", "term")));
    assertFalse(
        isListPath(makeLiteralPath("policy-options", "policy-statement", "foo", "term", "t")));
    assertFalse(
        isListPath(
            makeLiteralPath(
                "policy-options", "policy-statement", "foo", "term", "t", "from", "metric", "5")));

    assertTrue(
        isListPath(
            makeLiteralPath(
                "interfaces", "xe-0/0/0", "unit", "0", "family", "inet", "filter", "input-list")));

    assertTrue(isListPath(makeLiteralPath("system", "domain-search")));
  }

  private @Nonnull HierarchyPath makeLiteralPath(String... words) {
    HierarchyPath path = new HierarchyPath();
    for (String word : words) {
      path.addNode(word, -1);
    }
    return path;
  }
}
