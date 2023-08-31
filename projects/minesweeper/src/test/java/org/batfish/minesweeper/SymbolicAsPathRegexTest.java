package org.batfish.minesweeper;

import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import dk.brics.automaton.RegExp;
import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
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

  @Test
  public void testConstructorFromAsSetsMatchingRanges() {
    AsSetsMatchingRanges a1 =
        AsSetsMatchingRanges.of(false, false, ImmutableList.of(Range.closed(11L, 11L)));
    AsSetsMatchingRanges a2 =
        AsSetsMatchingRanges.of(true, false, ImmutableList.of(Range.open(11L, 14L)));
    AsSetsMatchingRanges a3 =
        AsSetsMatchingRanges.of(
            false, true, ImmutableList.of(Range.closed(11L, 11L), Range.closed(130L, 132L)));
    AsSetsMatchingRanges a4 =
        AsSetsMatchingRanges.of(true, true, ImmutableList.of(Range.closed(11L, 14L)));

    assertThat(new SymbolicAsPathRegex(a1).getRegex(), equalTo("(^| )11( |$)"));
    assertThat(new SymbolicAsPathRegex(a2).getRegex(), equalTo("(^| )<12-13>$"));
    assertThat(new SymbolicAsPathRegex(a3).getRegex(), equalTo("^11 <130-132>( |$)"));
    assertThat(new SymbolicAsPathRegex(a4).getRegex(), equalTo("^<11-14>$"));
  }

  @Test
  public void testToRegex() {
    String r1 = SymbolicAsPathRegex.toRegex(0L, Integer.MAX_VALUE);
    String r2 = SymbolicAsPathRegex.toRegex(500L, (long) Integer.MAX_VALUE + 20);
    String r3 =
        SymbolicAsPathRegex.toRegex((long) Integer.MAX_VALUE + 20, (long) Integer.MAX_VALUE + 500);
    String r4 = SymbolicAsPathRegex.toRegex(0L, (long) Math.pow(2, 32) - 1);

    assertThat(r1, equalTo("<0-2147483647>"));
    assertThat(r2, equalTo("((<500-2147483647>)|(2<147483648-147483667>))"));
    assertThat(r3, equalTo("((2<147483667-147484147>))"));
    assertThat(
        r4,
        equalTo(
            "((<0-2147483647>)|(2<147483648-999999999>)|(3<000000000-999999999>)|(4<000000000-294967295>))"));
  }
}
