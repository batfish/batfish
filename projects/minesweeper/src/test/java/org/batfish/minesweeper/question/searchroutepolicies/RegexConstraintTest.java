package org.batfish.minesweeper.question.searchroutepolicies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RegexConstraintTest {

  @Rule public ExpectedException _exception = ExpectedException.none();

  @Test
  public void testParse() {
    RegexConstraint c1 = RegexConstraint.parse("30:40");
    RegexConstraint c2 = RegexConstraint.parse("!30:40");
    RegexConstraint c3 = RegexConstraint.parse("/^40:/");
    RegexConstraint c4 = RegexConstraint.parse("!/^40:/");

    assertThat(c1, equalTo(new RegexConstraint("^30:40$", false)));
    assertThat(c2, equalTo(new RegexConstraint("^30:40$", true)));
    assertThat(c3, equalTo(new RegexConstraint("^40:", false)));
    assertThat(c4, equalTo(new RegexConstraint("^40:", true)));
  }

  @Test
  public void testDoNotParse1() {
    _exception.expect(BatfishException.class);
    RegexConstraint.parse("/^40:");
  }

  @Test
  public void testDoNotParse2() {
    _exception.expect(BatfishException.class);
    RegexConstraint.parse("^40:/");
  }

  @Test
  public void testDoNotParse3() {
    _exception.expect(BatfishException.class);
    RegexConstraint.parse("/");
  }
}
