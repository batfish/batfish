package org.batfish.minesweeper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import dk.brics.automaton.RegExp;
import org.junit.Test;

/** Tests for the {@link org.batfish.minesweeper.SymbolicAsPathRegex} class. */
public class SymbolicAsPathRegexTest {
  @Test
  public void testToAutomaton() {

    SymbolicAsPathRegex r1 = new SymbolicAsPathRegex("^40$");
    SymbolicAsPathRegex r2 = new SymbolicAsPathRegex("^$");
    // _40$
    SymbolicAsPathRegex r3 = new SymbolicAsPathRegex("(,|{|}|^|$| )40$");
    // ^40_
    SymbolicAsPathRegex r4 = new SymbolicAsPathRegex("^40(,|{|}|^|$| )");
    // _40_50_
    SymbolicAsPathRegex r5 = new SymbolicAsPathRegex("(,|{|}|^|$| )40(,|{|}|^|$| )50(,|{|}|^|$| )");

    assertThat(r1.toAutomaton(), equalTo(new RegExp("^40$").toAutomaton()));
    assertThat(r2.toAutomaton(), equalTo(new RegExp("^$").toAutomaton()));
    assertThat(r3.toAutomaton(), equalTo(new RegExp("^((0|[1-9][0-9]*) )*40$").toAutomaton()));
    assertThat(r4.toAutomaton(), equalTo(new RegExp("^40( (0|[1-9][0-9]*))*$").toAutomaton()));
    assertThat(
        r5.toAutomaton(),
        equalTo(new RegExp("^((0|[1-9][0-9]*) )*40 50( (0|[1-9][0-9]*))*$").toAutomaton()));
  }
}
