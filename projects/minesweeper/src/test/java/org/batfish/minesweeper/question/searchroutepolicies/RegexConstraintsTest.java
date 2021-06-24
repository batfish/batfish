package org.batfish.minesweeper.question.searchroutepolicies;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class RegexConstraintsTest {

  @Test
  public void testEquals() {
    RegexConstraint r1 = new RegexConstraint("foo", true);
    RegexConstraint r2 = new RegexConstraint("bar", false);
    RegexConstraints c1 = new RegexConstraints();
    RegexConstraints c2 = new RegexConstraints(ImmutableList.of(r1));
    RegexConstraints c3 = new RegexConstraints(ImmutableList.of(r1, r2));
    RegexConstraints c4 = new RegexConstraints(ImmutableList.of(r2, r1));
    RegexConstraints c5 = new RegexConstraints(ImmutableList.of(r2));
    new EqualsTester()
        .addEqualityGroup(c1)
        .addEqualityGroup(c2)
        .addEqualityGroup(c3, c4)
        .addEqualityGroup(c5)
        .testEquals();
  }
}
