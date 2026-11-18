package org.batfish.minesweeper.question.searchroutepolicies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class RegexConstraintTest {

  @Test
  void testEquals() {
    String r1 = "foo";
    String r2 = "bar";
    new EqualsTester()
        .addEqualityGroup(new RegexConstraint(r1, true), new RegexConstraint(r1, true))
        .addEqualityGroup(new RegexConstraint(r1, false))
        .addEqualityGroup(new RegexConstraint(r2, true))
        .testEquals();
  }

  @Test
  void testParse() {
    RegexConstraint c1 = RegexConstraint.parse("30:40");
    RegexConstraint c2 = RegexConstraint.parse("!30:40");
    RegexConstraint c3 = RegexConstraint.parse("/^40:/");
    RegexConstraint c4 = RegexConstraint.parse("!/^40:/");
    RegexConstraint c5 = RegexConstraint.parse("NAME123");
    RegexConstraint c6 = RegexConstraint.parse("!COMM_LIST");

    assertThat(c1, equalTo(new RegexConstraint("^30:40$", false)));
    assertThat(c2, equalTo(new RegexConstraint("^30:40$", true)));
    assertThat(c3, equalTo(new RegexConstraint("^40:", false)));
    assertThat(c4, equalTo(new RegexConstraint("^40:", true)));
    assertThat(
        c5,
        equalTo(new RegexConstraint("NAME123", false, RegexConstraint.RegexType.STRUCTURE_NAME)));
    assertThat(
        c6,
        equalTo(new RegexConstraint("COMM_LIST", true, RegexConstraint.RegexType.STRUCTURE_NAME)));
  }

  @Test
  void testDoNotParse1() {
    assertThrows(IllegalArgumentException.class, () -> RegexConstraint.parse("/^40:"));
  }

  @Test
  void testDoNotParse2() {
    assertThrows(IllegalArgumentException.class, () -> RegexConstraint.parse("^40:/"));
  }

  @Test
  void testDoNotParse3() {
    assertThrows(IllegalArgumentException.class, () -> RegexConstraint.parse("/"));
  }
}
