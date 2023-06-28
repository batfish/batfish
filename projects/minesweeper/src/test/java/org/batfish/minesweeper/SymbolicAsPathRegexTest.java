package org.batfish.minesweeper;

import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import dk.brics.automaton.RegExp;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

/** Tests for the {@link org.batfish.minesweeper.SymbolicAsPathRegex} class. */
public class SymbolicAsPathRegexTest {

  // the commonly used _ character in Cisco regexes expands to the following regex in Java:
  // either a comma, a curly brace, the start or end of the string, or a space
  private static final String UNDERSCORE =
      StringEscapeUtils.unescapeJava(DEFAULT_UNDERSCORE_REPLACEMENT);

  @Test
  public void testToAutomatonRegex() {

    SymbolicAsPathRegex r1 = new SymbolicAsPathRegex("^40$");
    SymbolicAsPathRegex r2 = new SymbolicAsPathRegex("^$");
    // _40$
    SymbolicAsPathRegex r3 = new SymbolicAsPathRegex(UNDERSCORE + "40$");
    // ^40_
    SymbolicAsPathRegex r4 = new SymbolicAsPathRegex("^40" + UNDERSCORE);
    // _40_50_
    SymbolicAsPathRegex r5 =
        new SymbolicAsPathRegex(UNDERSCORE + "40" + UNDERSCORE + "50" + UNDERSCORE);

    assertThat(r1.toAutomaton(), equalTo(new RegExp("^^40$").toAutomaton()));
    assertThat(r2.toAutomaton(), equalTo(new RegExp("^^$").toAutomaton()));
    assertThat(r3.toAutomaton(), equalTo(new RegExp("^^((0|[1-9][0-9]*) )*40$").toAutomaton()));
    assertThat(r4.toAutomaton(), equalTo(new RegExp("^^40( (0|[1-9][0-9]*))*$").toAutomaton()));
    assertThat(
        r5.toAutomaton(),
        equalTo(new RegExp("^^((0|[1-9][0-9]*) )*40 50( (0|[1-9][0-9]*))*$").toAutomaton()));
  }

  @Test
  public void testToAutomatonJuniper() {

    SymbolicAsPathRegex r1 = new SymbolicAsPathRegex("^(^| )40$");
    SymbolicAsPathRegex r2 = new SymbolicAsPathRegex("^$");
    // .*40$
    SymbolicAsPathRegex r3 = new SymbolicAsPathRegex("^((^| )\\d+)*(^| )40$");
    // ^40.*
    SymbolicAsPathRegex r4 = new SymbolicAsPathRegex("^(^| )40((^| )\\d+)*$");

    assertThat(r1.toAutomaton(), equalTo(new RegExp("^^40$").toAutomaton()));
    assertThat(r2.toAutomaton(), equalTo(new RegExp("^^$").toAutomaton()));
    assertThat(r3.toAutomaton(), equalTo(new RegExp("^^((0|[1-9][0-9]*) )*40$").toAutomaton()));
    assertThat(r4.toAutomaton(), equalTo(new RegExp("^^40( (0|[1-9][0-9]*))*$").toAutomaton()));
  }
}
