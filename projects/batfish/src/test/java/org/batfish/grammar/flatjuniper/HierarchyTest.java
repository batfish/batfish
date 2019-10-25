package org.batfish.grammar.flatjuniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests for {@link Hierarchy#matchWithJuniperRegex(String, String)} */
public class HierarchyTest {

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
