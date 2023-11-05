package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.flatjuniper.Hierarchy.isListPath;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.junit.Test;

/** Tests for {@link Hierarchy#matchWithJuniperRegex(String, String)} */
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

  @Test
  public void testMatchWithJuniperRegex1() {
    assertThat(Hierarchy.matchWithJuniperRegex("test", "*"), equalTo(true));
  }

  @Test
  public void testMatchWithJuniperRegex2() {
    assertThat(Hierarchy.matchWithJuniperRegex("testexpression", "*.*"), equalTo(false));
  }

  @Test
  public void testMatchWithJuniperRegex3() {
    assertThat(Hierarchy.matchWithJuniperRegex("test.expression", "*.*"), equalTo(true));
  }

  @Test
  public void testMatchWithJuniperRegex4() {
    assertThat(Hierarchy.matchWithJuniperRegex("[test", "["), equalTo(false));
  }

  @Test
  public void testMatchWithJuniperRegex5() {
    assertThat(Hierarchy.matchWithJuniperRegex("test3", "test[1-5]"), equalTo(true));
  }

  @Test
  public void testMatchWithJuniperRegex6() {
    assertThat(Hierarchy.matchWithJuniperRegex("test6", "test[1-5]"), equalTo(false));
  }

  @Test
  public void testMatchWithJuniperRegex7() {
    assertThat(Hierarchy.matchWithJuniperRegex("test4", "test[!5]"), equalTo(true));
  }

  @Test
  public void testMatchWithJuniperRegex8() {
    assertThat(Hierarchy.matchWithJuniperRegex("test5", "test[!5]"), equalTo(false));
  }

  @Test
  public void testMatchWithJuniperRegex9() {
    assertThat(Hierarchy.matchWithJuniperRegex("test5", "test[!3-4]"), equalTo(true));
  }

  @Test
  public void testMatchingJuniperRegex10() {
    assertThat(Hierarchy.matchWithJuniperRegex("test3", "test[!3-4]"), equalTo(false));
    assertThat(Hierarchy.matchWithJuniperRegex("test4", "test[!3-5]"), equalTo(false));
  }

  @Test
  public void testMatchingJuniperRegex11() {
    assertThat(Hierarchy.matchWithJuniperRegex("test5", "test?"), equalTo(true));
  }

  @Test
  public void testMatchingJuniperRegex12() {
    assertThat(Hierarchy.matchWithJuniperRegex("tes", "test?"), equalTo(false));
  }

  @Test
  public void testIPs() {
    assertThat(Hierarchy.matchWithJuniperRegex("2001:dead::beef::1553:1", "*.*"), equalTo(false));
    assertThat(Hierarchy.matchWithJuniperRegex("2001:dead::beef::1553:1", "*:*"), equalTo(true));
  }
}
